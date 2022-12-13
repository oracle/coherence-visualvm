/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package functional.topics;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicData;
import com.oracle.coherence.plugin.visualvm.tests.AbstractVisualVMTest;
import com.tangosol.coherence.component.application.console.Coherence;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Tests for basic data retriever functionality using the VisualVM model API against Topics only.
 *
 * @author  tam  2022.12.06
 * @since   1.6.0
 */
public abstract class AbstractTopicsDataRetrieverTest
        extends AbstractVisualVMTest
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create a new test.
     *
     */
    public AbstractTopicsDataRetrieverTest()
        {
        setModel(VisualVMModel.getInstance());
        }

    // ----- test methods ---------------------------------------------------

    /**
     * Shutdown tests after a test run.
     * Note: the startup is done in the class that extends this test.
     */
    @AfterClass
    public static void _shutdown()
        {
        shutdownCacheServers();
        }

    @Test
    public void testTopicsData()
        {
        ProtectionDomain domain = Coherence.class.getProtectionDomain();
        CodeSource source = domain == null ? null : domain.getCodeSource();
        URL urlSrc = source == null ? null : source.getLocation();
        System.out.println("--> Coherence component loaded from: " + urlSrc);

        // startup the Topics

        System.err.println("Starting Topics");
        s_memberA1.invoke(()->
            {
            RunTopics runTopics = new RunTopics();
            runTopics.startTopics();
            return null;
            });


        System.err.println("Topics Started");

        // wait for topics to fire up
        Base.sleep(30_000L);

        try
            {
            runTestTopicsData();
            
            if ("true".equals(System.getProperty("pause.topics")))
                {
                System.out.println("Pausing test");
                Base.sleep(Long.MAX_VALUE);
                }
            }
        catch (Exception e)
            {
            e.printStackTrace();

            throw new RuntimeException("Test failed: " + e.getMessage());
            }
        }

    /**
     * Test the retrieval of ClusterData via the VisualVMModel.
     */
    private void testClusterData()
        {
        List<Map.Entry<Object, Data>> clusterData;

        VisualVMModel model = getModel();
        assertClusterReady();
        waitForRefresh();

        // refresh the statistics
        model.refreshStatistics(getRequestSender());
        clusterData = model.getData(VisualVMModel.DataType.CLUSTER);

        validateData(VisualVMModel.DataType.CLUSTER, clusterData, 1);

        setCurrentDataType(VisualVMModel.DataType.CLUSTER);

        // ensure we have correct values
        for (Map.Entry<Object, Data> entry : clusterData)
            {
            validateColumn(ClusterData.CLUSTER_NAME, entry, CLUSTER_NAME);
            validateColumn(ClusterData.CLUSTER_SIZE, entry, 2);
            validateColumn(ClusterData.VERSION, entry, CacheFactory.VERSION);
            validateColumn(ClusterData.DEPARTURE_COUNT, entry, 0L);
            }
        }

    /**
     * Test the retrieval of TopicsData via the VisualVMModel.
     */
    @SuppressWarnings("unchecked")
    private void runTestTopicsData()
        {
        List<Map.Entry<Object, Data>> topicsData;

        VisualVMModel model = getModel();
        assertClusterReady();
        waitForRefresh();

        // refresh the statistics
        model.refreshStatistics(getRequestSender());
        topicsData = model.getData(VisualVMModel.DataType.TOPICS);

        validateData(VisualVMModel.DataType.TOPICS, topicsData, 1);

        setCurrentDataType(VisualVMModel.DataType.TOPICS);

        // ensure we have correct values
        for (Map.Entry<Object, Data> entry : topicsData)
            {
            Pair<String, String> key = (Pair<String, String>) getColumn(TopicData.TOPIC_NAME, entry);
            assertEquals("Service must be PartitionedTopic", "PartitionedTopic", key.getX());
            }
        }
        
    }
