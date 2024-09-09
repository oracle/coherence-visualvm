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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PartitionOwnership
    {
    public PartitionOwnership(int nMemberId, int nTotalPartitions, int nPrimaryPartitions, int nBackupPartitions)
        {
        this.f_nMemberId          = nMemberId;
        this.f_nTotalPartitions   = nTotalPartitions;
        this.f_nPrimaryPartitions = nPrimaryPartitions;
        this.f_nBackupPartitions  = nBackupPartitions;
        }

    // ----- accessors ------------------------------------------------------

    public int getMemberId()
        {
        return f_nMemberId;
        }

    public int getTotalPartitions()
        {
        return f_nTotalPartitions;
        }

    public int getPrimaryPartitions()
        {
        return f_nPrimaryPartitions;
        }

    public int getBackupPartitions()
        {
        return f_nBackupPartitions;
        }

    public Map<Integer, Integer[]> getPartitionMap()
        {
        return f_mapPartitions;
        }

    // ---- Object interface ------------------------------------------------

    @Override
    public String toString()
        {
        StringBuilder sb = new StringBuilder();
        sb.append("PartitionOwnership{")
                .append(f_nMemberId == -1 ? "Orphaned" :"MemberId=" + f_nMemberId)
                .append(", TotalPartitions=").append(f_nTotalPartitions)
                .append(", PrimaryPartitions=").append(f_nPrimaryPartitions)
                .append(", BackupPartitions=").append(f_nBackupPartitions)
                .append(", ");
        f_mapPartitions.forEach((k,v) ->
            {
            if (k == 0)
                {
                sb.append("Primary: ");
                }
            else
                {
                sb.append("Backup ").append(k).append(": ");
                }

            // now go through the map of primary's and backups
            Arrays.stream(v).sequential().forEach(s -> sb.append(s).append(", "));
            });

        return sb.toString();
        }

    // ----- helpers --------------------------------------------------------

    public static int backup(int nBackupNumber)
        {
        return nBackupNumber + 1;
        }

    // ----- constants ------------------------------------------------------

    public static final int PRIMARY = 0;

    // ----- data members ---------------------------------------------------

    private final int f_nMemberId;
    private final int f_nTotalPartitions;
    private final int f_nPrimaryPartitions;
    private final int f_nBackupPartitions;

    /**
     * A {@link Map} of partition ownership keyed by {@link Integer} where
     * 0 = primary
     * 1 = first backup
     * 2 = second backup....
     */
    private final Map<Integer, Integer[]> f_mapPartitions = new HashMap<>();

    }
