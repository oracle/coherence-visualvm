/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.util.Map;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;


/**
 * A class to hold basic topic data.
 *
 * @author tam  2020.02.08
 * @since  1.0.1
 */
public class TopicData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create TopicData passing in the number of columns.
     *
     */
    public TopicData()
        {
        super(SUBSCRIBER_RECEIVES + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender sender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Data                    data;

        return null;
        }

    /**
     * {@inheritDoc}
     */
    public String getReporterReport()
        {
        return REPORT_TOPICS;
        }

    /**
     * {@inheritDoc}
     */
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicData();

        // the identifier for this row is the service name and cache name
        Pair<String, String> key = new Pair<>(aoColumns[2].toString(),
                aoColumns[3].toString().replaceAll("\\$topic\\$", ""));

        data.setColumn(TopicData.TOPIC_NAME, key);
        data.setColumn(TopicData.SIZE, Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(TopicData.MEMORY_USAGE_BYTES, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(TopicData.MEMORY_USAGE_MB, Integer.valueOf(getNumberValue(aoColumns[6].toString())) / 1024 / 1024);

        if (aoColumns[7] != null)
            {
            data.setColumn(TopicData.AVG_OBJECT_SIZE, Integer.valueOf(getNumberValue(aoColumns[7].toString())));
            }
        else
            {
            data.setColumn(TopicData.AVG_OBJECT_SIZE, 0);
            }

        data.setColumn(TopicData.PUBLISHER_SENDS, Long.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(TopicData.SUBSCRIBER_RECEIVES, Long.valueOf(getNumberValue(aoColumns[9].toString())));

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        // minimize the number of round-trips by querying each of the services and getting the cache details
        // this will be one per service
//        List<Map.Entry<Object, Data>> serviceData = model.getData(VisualVMModel.DataType.SERVICE);
//        if (serviceData != null && serviceData.size() > 0)
//            {
//            final SortedMap<Object, Data> mapData = new TreeMap<>();
//
//            for (Map.Entry<Object, Data> service : serviceData)
//                {
//                String   sService            = (String) service.getKey();
//                String[] asServiceDetails    = getDomainAndService(sService);
//                String   sDomainPartition    = asServiceDetails[0];
//                String   sServiceName        = asServiceDetails[1];
//
//                JsonNode listOfServiceCaches = requestSender.getListOfServiceCaches(sServiceName, sDomainPartition);
//                JsonNode itemsNode           = listOfServiceCaches.get("items");
//                boolean  fisDistributed      = model.getDistributedCaches().contains(sServiceName);
//
//                if (itemsNode != null && itemsNode.isArray())
//                    {
//                    for (int i = 0; i < ((ArrayNode) itemsNode).size(); i++)
//                        {
//                        Data     data         = new TopicData();
//                        JsonNode cacheDetails = itemsNode.get(i);
//                        String   sCacheName   = cacheDetails.get("name").asText();
//
//                        Pair<String, String> key = new Pair<>(sServiceName, sCacheName);
//
//                        JsonNode nodeSize = cacheDetails.get("size");
//                        if (nodeSize == null)
//                            {
//                            // Connecting to version without Bug 32134281 fix so force less efficient way
//                            return null;
//                            }
//                        int nCacheSize = nodeSize.asInt();
//
//                        String sUnitCalculator = getChildValue("true", "memoryUnits", cacheDetails) != null
//                                ? "BINARY" : "FIXED";
//                        int nMembers = Integer.parseInt(getChildValue("count", "averageMissMillis", cacheDetails));
//
//                        data.setColumn(CACHE_NAME, key);
//
//                        // if is replicated so the actual size has been aggregated so we must divide by the member count
//                        data.setColumn(SIZE, fisDistributed ? nCacheSize : nCacheSize / nMembers);
//
//                        data.setColumn(UNIT_CALCULATOR, sUnitCalculator);
//                        long cMemoryUsageBytes = Long.parseLong(getFirstMemberOfArray(cacheDetails, "unitFactor"))
//                                            * cacheDetails.get("units").asLong();
//                        data.setColumn(MEMORY_USAGE_BYTES, cMemoryUsageBytes);
//                        data.setColumn(MEMORY_USAGE_MB, (int) (cMemoryUsageBytes / 1024 / 1024));
//
//                        if (data.getColumn(TopicData.UNIT_CALCULATOR).equals("FIXED"))
//                            {
//                            data.setColumn(TopicData.AVG_OBJECT_SIZE, 0);
//                            data.setColumn(TopicData.MEMORY_USAGE_BYTES, 0);
//                            data.setColumn(TopicData.MEMORY_USAGE_MB, 0);
//                            }
//                        else
//                            {
//                            if (nCacheSize != 0)
//                                {
//                                data.setColumn(TopicData.AVG_OBJECT_SIZE, (int) (cMemoryUsageBytes / nCacheSize));
//                                }
//                            }
//
//                        mapData.put(key, data);
//                        }
//                    }
//                }
//                return mapData;
//            }

        return null;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SortedMap<Object, Data> postProcessReporterData(SortedMap<Object, Data> mapData, VisualVMModel model)
        {
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 6427775621469258645L;

    /**
     * Report for topics data.
     */
    public static final String REPORT_TOPICS = "reports/visualvm/topics-size-stats.xml";

    /**
     * Array index for topic name.
     */
    public static final int TOPIC_NAME = 0;

    /**
     * Array index for topic size.
     */
    public static final int SIZE = 1;

    /**
     * Array index for memory usage in bytes.
     */
    public static final int MEMORY_USAGE_BYTES = 2;

    /**
     * Array index for memory usage in MB.
     */
    public static final int MEMORY_USAGE_MB = 3;

    /**
     * Array index for average object size.
     */
    public static final int AVG_OBJECT_SIZE = 4;

    /**
     * Array index for publisher sends.
     */
    public static final int PUBLISHER_SENDS = 5;

    /**
     * Array index for subscriber receives.
     */
    public static final int SUBSCRIBER_RECEIVES = 6;
    
    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicData.class.getName());

    /**
     * Attribute for "MemoryUnits".
     */
    private static final String MEMORY_UNITS = "MemoryUnits";
    }
