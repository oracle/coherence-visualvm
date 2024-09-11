/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.coherence.plugin.visualvm.helper;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Various methods to help in interpreting the ownership results.
 *
 * @author tam  2024.09.04
 * @since  1.7.2
 *
 */
public class PartitionHelper
    {
    // ----- constructors ---------------------------------------------------

    private PartitionHelper()
        {
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Parse the partition ownership returned from the REST endpoint.
     * <pre>
     * {@code http://host:port/management/coherence/cluster/services/{serviceName}/members/{memberID}/ownership?links=\&verbose=true}
     * </pre>
     * 
     * @param sOwnershipJson  ownership text in Json format
     *
     * @return a {@link Map} keyed by member id, with an array of Integers with 0 being primary and 1+ being backups.
     *         member id of -1 are orphaned.
     */
    public static Map<Integer, PartitionOwnership> parsePartitionOwnership(String sOwnershipJson)
        throws PartitionParsingException
        {
        Map<Integer, PartitionOwnership> mapOwnership = new HashMap<>();

        String sOwnership = null;

        // convert to Json
        ObjectMapper mapper = new ObjectMapper();
        try
            {
            JsonNode jsonNode = mapper.readTree(sOwnershipJson);
            JsonNode jsonOwnership = jsonNode.get("ownership");
            if (jsonOwnership == null)
                {
                throw new PartitionParsingException("Unable to find ownership node in [" + jsonNode + "]");
                }

            sOwnership = jsonOwnership.asText();
            if (sOwnership.length() == 0)
                {
                throw new PartitionParsingException("empty ownership node");
                }

              String []asParts = sOwnership.split("<br/>");

            int nCurrentMember = -2;
            int nLength = asParts.length;
            int i = 0;
            PartitionOwnership ownership = null;

            while (i < nLength)
                {
                String sLine = asParts[i];

                if (sLine.contains("*** Member:"))
                    {
                    // start of a member's ownership format is
                    // *** Member:  1 total=19 (primary=6, backup=13)
                    Matcher matcher = MEMBER_PATTERN.matcher(sLine);
                    if (matcher.find())
                        {
                        int nMemberId  = Integer.parseInt(matcher.group(1));
                        int nTotal     = Integer.parseInt(matcher.group(2));
                        int nPrimary   = Integer.parseInt(matcher.group(3));
                        int nBackups   = Integer.parseInt(matcher.group(4));

                        ownership = new PartitionOwnership(nMemberId, nTotal, nPrimary, nBackups);
                        mapOwnership.put(nMemberId, ownership);
                        nCurrentMember = nMemberId;
                        }
                    else
                        {
                        throw new PartitionParsingException("Unable to pase line [" + sLine + "]");
                        }
                    }
                else if (sLine.contains("*** Orphans"))
                    {
                    nCurrentMember = -1;  // signifies orphans
                    ownership = new PartitionOwnership(-1, 0, 0, 0);
                    mapOwnership.put(-1, ownership);
                    }
                else if (sLine.contains("Primary["))
                    {
                    ownership = mapOwnership.get(nCurrentMember);
                    ownership.getPartitionMap().put(PartitionOwnership.PRIMARY, extractPartitions(sLine));
                    }
                else if (sLine.contains("Backup["))
                    {
                    int nBackup = extractBackup(sLine);
                    if (nBackup == -1)
                        {
                        throw new PartitionParsingException("negative backup from " + sLine);
                        }
                    
                    ownership = mapOwnership.get(nCurrentMember);
                    ownership.getPartitionMap().put(nBackup, extractPartitions(sLine));
                    }

                i++;
                }

            }
        catch (Exception e)
            {
            throw new PartitionParsingException("Unable to parse json text [" + sOwnershipJson + "] ", e);
            }

        return mapOwnership;
        }

    /**
     * Create a string representation of the ownership for display.
     * @param sServiceName  service name
     * @param mapOwnership   {@link Map} of {@link PartitionOwnership}
     * @return a string representation
     */
    public static String toString(String sServiceName, Map<Integer, PartitionOwnership> mapOwnership)
        {
        StringBuilder sb = new StringBuilder();
        sb.append("Partition Ownership for Service: ").append(sServiceName).append("\n");

        mapOwnership.forEach((k,v) ->
            {
            String sMember = k == -1 ? "Orphaned" : "Member " + k;

            sb.append(String.format("%s: primaries=%d, backups=%d\n", sMember, v.getPrimaryPartitions(), v.getBackupPartitions()));

            final AtomicInteger counter = new AtomicInteger(0);
            v.getPartitionMap().forEach((k1,v1) ->
                {
                int nCount = counter.getAndIncrement();
                if (nCount == 0)
                    {
                    sb.append(" - Primary ");
                    }
                else
                    {
                    sb.append(" - Backup ").append(nCount);
                    }
                int[] naPartitions = Arrays.stream(v1).mapToInt(Integer::intValue).toArray();
                sb.append("  ").append(PartitionHelper.formatPartitions(naPartitions))
                        .append("\n");
                });
            });

        return sb.toString();
        }

    protected static Integer[] extractPartitions(String sString)
        {
        ArrayList<Integer> listPartitions = new ArrayList<>();
        String[] asParts = removePrefix(sString).split(", ");

        Arrays.stream(asParts).filter(s -> !s.isEmpty()).forEach(s -> listPartitions.add(Integer.parseInt(s)));
        
        return listPartitions.toArray(new Integer[0]);
        }

    /**
     * Extracts the backup from the backup string.
     * @param sBackupString string to parse
     * @return backup number or -1 if invalid
     */
    protected static int extractBackup(String sBackupString)
        {
        // examples:
        // Backup[1]#008: 333, 444, 5555
        // Backup[1]#008: 001, 002, 006, 007, 012, 013, 018, 019
        Matcher matcher = BACKUP_PATTERN.matcher(sBackupString);
        if (matcher.find())
            {
            return Integer.parseInt(matcher.group(1));
            }
        return -1;
        }

    protected static String removePrefix(String sString)
        {
        sString = sString.replace("+", " ");
        return !sString.contains(":") ? "" : sString.replaceFirst("^.*?:\\s", "");
        }

    public static String formatPartitions(int[] partitions) {
        if (partitions.length == 0) {
            return "-";
        }

        Arrays.sort(partitions);

        List<String> result = new ArrayList<>();
        int          start  = partitions[0];
        int prev = partitions[0];

        for (int i = 1; i < partitions.length; i++)
            {
            if (partitions[i] == prev + 1)
                {
                prev = partitions[i];
                }
            else
                {
                if (start == prev)
                    {
                    result.add(String.valueOf(start));
                    }
                else
                    {
                    result.add(String.format("%d..%d", start, prev));
                    }
                start = partitions[i];
                prev = partitions[i];
                }
            }

        if (start == prev)
            {
            result.add(String.valueOf(start));
            }
        else
            {
            result.add(String.format("%d..%d", start, prev));
            }

        return String.join(", ", result);
        }
        
    public static class PartitionParsingException extends Exception
        {
        // ----- constructors ---------------------------------------------------

        public PartitionParsingException(String sMessage)
            {
            super(sMessage);
            }

        public PartitionParsingException(String sMessage, Throwable t)
            {
            super(sMessage, t);
            }
        }
        
    // ----- data members ---------------------------------------------------

    // ----- constants ------------------------------------------------------

    /**
     * The logger to use.
     */
    private static final Logger LOGGER       = Logger.getLogger(PartitionHelper.class.getName());

    private static final String  MEMBER_REGEX   = "Member:\\s+(\\d+)\\s+total=(\\d+)\\s+\\(primary=(\\d+),\\s+backup=(\\d+)\\)";
    private static final Pattern MEMBER_PATTERN = Pattern.compile(MEMBER_REGEX);

    private static final String  BACKUP_REGEX   =  "\\[(\\d+)\\]";
    private static final Pattern BACKUP_PATTERN = Pattern.compile(BACKUP_REGEX);
    }
