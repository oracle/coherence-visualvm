/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.tablemodel.model;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.JMXUtils;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.swing.JOptionPane;

import static com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender.CLUSTER_PREFIX;

/**
 * A class to hold basic cluster data.
 *
 * @author tam  2013.11.14
 * @since 12.1.3
 */
public class ClusterData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ClusterData passing in the number of columns.
     */
    public ClusterData()
        {
        super(CLUSTER_SIZE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        return new ArrayList<>(getJMXDataMap(requestSender, model).entrySet());
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_CLUSTER;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new ClusterData();

        data.setColumn(ClusterData.CLUSTER_NAME, aoColumns[2]);
        data.setColumn(ClusterData.LICENSE_MODE, aoColumns[3]);
        data.setColumn(ClusterData.VERSION, aoColumns[4]);
        data.setColumn(ClusterData.DEPARTURE_COUNT, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(ClusterData.CLUSTER_SIZE, Integer.valueOf(getNumberValue(aoColumns[6].toString())));

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        return getJMXDataMap(requestSender, model);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
     * Retrieve JMX data.
     *
     * @param requestSender   {@link RequestSender}
     * @param model           {@link VisualVMModel}
     *
     * @return JMX data
     */
    protected SortedMap<Object, Data> getJMXDataMap(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<>();
        HttpRequestSender httpRequestSender = null;

        try
            {
            Set<ObjectName> clusterSet = requestSender.getAllClusters();

            if (requestSender instanceof HttpRequestSender)
                {
                httpRequestSender = (HttpRequestSender) requestSender;
                }

            if (clusterSet.size() > 1)
                {
                // choose the cluster to connect to if we have more than one
                ArrayList<String> listClusters = new ArrayList<>();

                for (ObjectName cluster : clusterSet)
                    {
                    listClusters.add(cluster.getCanonicalName().replaceAll("Coherence:", "")
                                            .replaceAll("cluster=", "")
                                            .replaceAll("type=Cluster","")
                                            .replaceAll(",", ""));
                    }

                String[] asClusters = listClusters.toArray(new String[0]);
                Arrays.sort(asClusters);
                 // request the user to choose from an existing list

                String sSelectedCluster = (String) JOptionPane.showInputDialog(
                        null, "Cluster",
                        Localization.getLocalText("LBL_select_cluster"),
                        JOptionPane.QUESTION_MESSAGE,
                        null, // default icon
                        asClusters,
                        asClusters[0]);


                if (sSelectedCluster != null)
                    {
                    clusterSet = Collections.singleton(new ObjectName(CLUSTER_PREFIX + sSelectedCluster));
                    if (httpRequestSender != null)
                        {
                        httpRequestSender.setClusterName(sSelectedCluster);
                        }
                    }
                }

            for (Iterator<ObjectName> cacheNameIter = clusterSet.iterator(); cacheNameIter.hasNext(); )
                {
                ClusterData data           = new ClusterData();
                ObjectName  clusterObjName = cacheNameIter.next();

                AttributeList listAttr = requestSender.getAttributes(clusterObjName,
                        new String[] { ATTR_CLUSTER_NAME, ATTR_LICENSE_MODE, ATTR_VERSION,
                                ATTR_DEPARTURE_COUNT, ATTR_CLUSTER_SIZE });

                String sClusterName = JMXUtils.getAttributeValueAsString(listAttr, ATTR_CLUSTER_NAME);

                // if we are using http request sender and the cluster name has not been set, then set it
                // so it can be used in the subsequent calls to be added to the base management URL
                // if we are connected to a WebLogic Server cluster via REST
                if (httpRequestSender != null && httpRequestSender.getClusterName() == null)
                    {
                    httpRequestSender.setClusterName(sClusterName);
                    }

                data.setColumn(ClusterData.CLUSTER_NAME, sClusterName);
                data.setColumn(ClusterData.LICENSE_MODE, JMXUtils.getAttributeValueAsString(listAttr, ATTR_LICENSE_MODE));
                data.setColumn(ClusterData.VERSION, JMXUtils.getAttributeValueAsString(listAttr, ATTR_VERSION));
                data.setColumn(ClusterData.DEPARTURE_COUNT,
                        Long.parseLong(JMXUtils.getAttributeValueAsString(listAttr, ATTR_DEPARTURE_COUNT)));
                data.setColumn(ClusterData.CLUSTER_SIZE,
                        Integer.parseInt(JMXUtils.getAttributeValueAsString(listAttr, ATTR_CLUSTER_SIZE)));

                mapData.put(data.getColumn(ClusterData.CLUSTER_NAME), data);
                }

            return mapData;
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, "Error getting cluster statistics", e);

            return null;
            }
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 7685333150479196716L;

    /**
     * Array index for cluster name.
     */
    public static final int CLUSTER_NAME = 0;

    /**
     * Array index for license mode.
     */
    public static final int LICENSE_MODE = 1;

    /**
     * Array index for version.
     */
    public static final int VERSION = 2;

    /**
     * Array index for departure count.
     */
    public static final int DEPARTURE_COUNT = 3;

    /**
     * Array index for cluster size;
     */
    public static final int CLUSTER_SIZE = 4;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ClusterData.class.getName());

    /**
     * Report for cluster data.
     */
    public static final String REPORT_CLUSTER = "reports/visualvm/cluster-stats.xml";

    /**
     * JMX attribute name for Cluster Name.
     */
    private static final String ATTR_CLUSTER_NAME = "ClusterName";

    /**
     * JMX attribute name for License Mode.
     */
    private static final String ATTR_LICENSE_MODE = "LicenseMode";

    /**
     * JMX attribute name for Version.
     */
    private static final String ATTR_VERSION = "Version";

    /**
     * JMX attribute name for Memebrs Departure Count.
     */
    private static final String ATTR_DEPARTURE_COUNT = "MembersDepartureCount";

    /**
     * JMX attribute name for Cluster Size.
     */
    private static final String ATTR_CLUSTER_SIZE = "ClusterSize";
    }
