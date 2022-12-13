/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        super(RETAIN_CONSUMED + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender sender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Data                    data;

        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_TOPICS;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicData();

        // the identifier for this row is the service name and topic name
        Pair<String, String> key = new Pair<>(aoColumns[2].toString(), aoColumns[3].toString());

        data.setColumn(TopicData.TOPIC_NAME, key);
        data.setColumn(TopicData.PUBLISHED_TOTAL, Long.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(TopicData.CHANNELS, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(TopicData.PAGE_CAPACITY, Integer.valueOf(getNumberValue(aoColumns[6].toString())));
        data.setColumn(TopicData.RECONNECT_RETRY, Integer.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(TopicData.RECONNECT_TIMEOUT, Integer.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(TopicData.RECONNECT_WAIT, Integer.valueOf(getNumberValue(aoColumns[9].toString())));
        data.setColumn(TopicData.RETAIN_CONSUMED, aoColumns[11].toString());

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        JsonNode                rootNode  = requestSender.getDataForTopics();
        SortedMap<Object, Data> mapData   = new TreeMap<>();
        JsonNode                nodeItems = rootNode.get("items");

        if (nodeItems != null && nodeItems.isArray())
            {
            for (int k = 0; k < (nodeItems).size(); k++)
                {
                JsonNode node = nodeItems.get(k);

                String sServiceName = node.get("service").asText();
                String sTopicName   = node.get("name").asText();

                TopicData data = new TopicData();
                Pair<String, String> key = new Pair<>(sServiceName, sTopicName);

                data.setColumn(TopicData.TOPIC_NAME, key);

                data.setColumn(TopicData.CHANNELS, Integer.valueOf(getNumberValue(getChildValue("average", "channelCount", node))));
                data.setColumn(TopicData.PUBLISHED_TOTAL, Long.valueOf(getNumberValue(getChildValue("sum", "publishedCount", node))));
                data.setColumn(TopicData.PAGE_CAPACITY, Long.valueOf(getNumberValue(getChildValue("average", "pageCapacity", node))));
                data.setColumn(TopicData.RECONNECT_RETRY, Long.valueOf(getNumberValue(getChildValue("average", "reconnectRetry", node))));
                data.setColumn(TopicData.RECONNECT_TIMEOUT, Long.valueOf(getNumberValue(getChildValue("average", "reconnectTimeout", node))));
                data.setColumn(TopicData.RECONNECT_WAIT, Long.valueOf(getNumberValue(getChildValue("average", "reconnectWait", node))));
                data.setColumn(TopicData.RETAIN_CONSUMED, getFirstMemberOfArray(node, "retainConsumed"));

                mapData.put(data.getColumn(0), data);
                }
            }
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 6427775621469258645L;

    /**
     * Report for topics data.
     */
    public static final String REPORT_TOPICS = "reports/visualvm/topics-summary.xml";

    /**
     * Array index for topic name.
     */
    public static final int TOPIC_NAME = 0;

    /**
     * Array index for channels.
     */
    public static final int CHANNELS = 1;

    /**
     * Array index for published total.
     */
    public static final int PUBLISHED_TOTAL = 2;

    /**
     * Array index for page capacity.
     */
    public static final int PAGE_CAPACITY = 3;

    /**
     * Array index for reconnect retry.
     */
    public static final int RECONNECT_RETRY = 4;

    /**
     * Array index for reconnect timeout.
     */
    public static final int RECONNECT_TIMEOUT = 5;

    /**
     * Array index for reconnect wait.
     */
    public static final int RECONNECT_WAIT = 6;

    /**
     * Array index for retain consumed.
     */
    public static final int RETAIN_CONSUMED = 7;
    
    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicData.class.getName());
    }
