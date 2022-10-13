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

package com.oracle.coherence.plugin.visualvm.tests;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceCacheServer;
import com.oracle.bedrock.runtime.coherence.JMXManagementMode;

import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.Logging;
import com.oracle.bedrock.runtime.coherence.options.Multicast;;
import com.oracle.bedrock.runtime.coherence.options.WellKnownAddress;

import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.java.profiles.JmxProfile;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.testsupport.junit.TestLogs;
import com.oracle.coherence.plugin.visualvm.discovery.DiscoveryUtils;

import com.tangosol.util.Base;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Map;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;


/**
 * Tests for discover cluster.
 *
 * @author tam 2022.10.13
 */
public abstract class AbstractDiscoveryTest
    {

    @Test
    public void testDiscovery()
        {
        s_availablePortIterator = LocalPlatform.get().getAvailablePorts();
        s_nManagementPort1 = s_availablePortIterator.next();
        s_nManagementPort2 = s_availablePortIterator.next();

        LocalPlatform platform        = LocalPlatform.get();

        OptionsByType optionsByTypeCluster1 = createCacheServerOptions("cluster1", s_nManagementPort1);
        OptionsByType optionsByTypeCluster2 = createCacheServerOptions("cluster2", s_nManagementPort2);

        // initial test of discovery when nothing is running
        Map<String, String> mapUrls = DiscoveryUtils.discoverManagementURLS(LOCALHOST, DEFAULT_NS_PORT);
        assertEquals(mapUrls.size(), 0);

        // start one member and ensure we can discover this cluster
        s_cluster1Member = platform.launch(CoherenceCacheServer.class, optionsByTypeCluster1.asArray());
        Eventually.assertThat(invoking(s_cluster1Member).getClusterSize(), is(1));
        Base.sleep(10_000L);

        mapUrls = DiscoveryUtils.discoverManagementURLS(LOCALHOST, DEFAULT_NS_PORT);
        assertEquals(mapUrls.size(), 1);
        assertEquals(mapUrls.get("cluster1"), getManagementURL(s_nManagementPort1));

        // start the second member and ensure we discover both clusters
        s_cluster2member = platform.launch(CoherenceCacheServer.class, optionsByTypeCluster2.asArray());
        Eventually.assertThat(invoking(s_cluster2member).getClusterSize(), is(1));

        // sleep to ensure that the second cluster registers with the cluster on the default NS port
        Base.sleep(10_000L);

        mapUrls = DiscoveryUtils.discoverManagementURLS(LOCALHOST, DEFAULT_NS_PORT);
        assertEquals(mapUrls.size(), 2);
        assertEquals(mapUrls.get("cluster1"), getManagementURL(s_nManagementPort1));
        assertEquals(mapUrls.get("cluster2"), getManagementURL(s_nManagementPort2));
        }

    private String getManagementURL(int nPort)
        {
        return "http://" + LOCALHOST + ":" + nPort + "/management/coherence/cluster";
        }

    @AfterClass
    public static void shutdown()
       {
       AbstractVisualVMTest.destroyMember(s_cluster1Member);
       AbstractVisualVMTest.destroyMember(s_cluster2member);
       }

    // ----- helpers --------------------------------------------------------

    /**
     * Establish {@link OptionsByType} to use launching cache servers.
     *
     * @param sClusterName      the cluster name
     * @param nManagementPort   management port
     *
     * @return an {@link OptionsByType}
     */
    protected static OptionsByType createCacheServerOptions(String sClusterName, int nManagementPort)
        {
        OptionsByType optionsByType = OptionsByType.empty();

        optionsByType.addAll(JMXManagementMode.ALL,
                             JmxProfile.enabled(),
                             LocalStorage.enabled(),
                             LocalHost.of(LOCALHOST),
                             WellKnownAddress.of(LOCALHOST),
                             Multicast.ttl(0),
                             Logging.at(9),
                             DisplayName.of(sClusterName),
                             ClusterName.of(sClusterName),
                             SystemProperty.of("partition.count", Integer.toString(17)),
                             SystemProperty.of("coherence.management.http", "all"),
                             SystemProperty.of("coherence.management", "dynamic"),
                             SystemProperty.of("coherence.management.http.host", LOCALHOST),
                             SystemProperty.of("coherence.management.http.port", nManagementPort),
                             s_logs.builder());

        return optionsByType;
        }

    // ----- data members ---------------------------------------------------


    @ClassRule
    public static final TestLogs s_logs = new TestLogs(AbstractDiscoveryTest.class);

    // ----- constants ------------------------------------------------------

    protected static String LOCALHOST = "127.0.0.1";

    protected static int DEFAULT_NS_PORT = 7574;

    protected static AvailablePortIterator s_availablePortIterator;
    protected static CoherenceCacheServer  s_cluster1Member = null;
    protected static CoherenceCacheServer  s_cluster2member = null;

    protected static int s_nManagementPort1;
    protected static int s_nManagementPort2;
    }
