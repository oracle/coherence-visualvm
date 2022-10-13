/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.discovery;

import com.tangosol.discovery.NSLookup;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for discovering Coherence clusters via tne name service.
 *
 * @author tam 2022.10.13
 */
public class DiscoveryUtils
    {
    // ----- constructors ---------------------------------------------------

    private DiscoveryUtils()
        {
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Use the name service to look up the first HTTP management URL for any local or remote clusters on the give port.
     * @param sHost hostname to connect to
     * @param nPort name service port
     *              
     * @return at {@link Map} of cluster and HTTP URLS
     */
    public static Map<String, String> discoverManagementURLS(String sHost, int nPort)
        {
        Map<String, String>  mapUrls     = new HashMap<>();
        Map<String, Integer> mapClusters = new HashMap<>();

        try
            {
            InetSocketAddress socketAddr = new InetSocketAddress(sHost, nPort);

            // get the local cluster name on the target host/port
            String sCluster = NSLookup.lookup(CLUSTER_NAME, socketAddr, TIMEOUT_MS);

            if (sCluster == null || "".equals(sCluster))
                {
                return mapUrls;
                }

            mapClusters.put(sCluster, nPort);

            // lookup any foreign clusters that are registered with this Name Service
            String sForeignClusters = NSLookup.lookup(sCluster, NS_PREFIX + CLUSTER_FOREIGN, socketAddr, TIMEOUT_MS);

            // determine the local name service port for each foreign cluster
            for (String s : parseResults(sForeignClusters))
                {
                String sLocalNSPort = NSLookup.lookup(NS_PREFIX + CLUSTER_FOREIGN + "/" + s + NS_LOCAL_PORT,
                        socketAddr, TIMEOUT_MS);
                if (sLocalNSPort == null)
                    {
                    throw new IllegalArgumentException("Unable to get local NS port from cluster " + s);
                    }

                mapClusters.put(s, Integer.parseInt(sLocalNSPort));
                }

            // now we have a Set of the local and foreign clusters with NS ports, find the management URLS
            for (Map.Entry<String, Integer> entry : mapClusters.entrySet())
                {
                String  sClusterName = entry.getKey();
                Integer nNSPort = entry.getValue();

                Collection<URL> colHttpUrls = NSLookup.lookupHTTPManagementURL(sClusterName, new InetSocketAddress(sHost, nNSPort));
                if (colHttpUrls.isEmpty())
                    {
                    LOGGER.log(Level.INFO, "No management over REST endpoints found for cluster {0", sClusterName);
                    }
                else
                    {
                    // add the first HTTP management URL we find for each cluster
                    mapUrls.put(sClusterName, colHttpUrls.iterator().next().toString());
                    }
                }
            }
        catch (Exception e)
           {
           LOGGER.log(Level.WARNING, String.format("Error connecting to %s:%s", sHost, nPort), e);
           }

        return mapUrls;
        }

    /**
     * Parse the results from discovery API.
     * @param sResults results string
     *
     * @return {@link Set} of results
     */
    private static Set<String> parseResults(String sResults)
        {
        Set<String> setResults = new HashSet<>();
        if (sResults == null || "[]".equals(sResults))
            {
            return setResults;
            }

        // parse the results as they are in the format of  "[cluster1, cluster2, clusterN]"
        String[] asResults = sResults.substring(1, sResults.length() - 1).split(", ");
        setResults.addAll(Arrays.asList(asResults));

        return setResults;
    }

    // ----- constants ------------------------------------------------------

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(DiscoveryUtils.class.getName());

    /**
     * Timeout for NS Lookup requests.
     */
    private static final int TIMEOUT_MS = 10_000;  // 10 seconds

    /**
     * Various NS Lookup strings.
     */
    private static final String NS_PREFIX = "NameService/string/";
    private static final String CLUSTER_NAME = "Cluster/name";
    private static final String CLUSTER_FOREIGN = "Cluster/foreign";
    private static final String NS_LOCAL_PORT = "/NameService/localPort";
    }
