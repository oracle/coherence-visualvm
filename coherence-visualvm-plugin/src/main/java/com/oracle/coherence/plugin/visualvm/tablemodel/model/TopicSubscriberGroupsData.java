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
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;


/**
 * A class to hold topic subscriber groups data.
 *
 * @author tam  2020.02.12
 * @since  1.0.1
 */
public class TopicSubscriberGroupsData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create TopicSubscriberGroupsData passing in the number of columns.
     *
     */
    public TopicSubscriberGroupsData()
        {
        super(FIFTEEN_MIN + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender sender, VisualVMModel model)
        {
        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_SUBSCRIBER_GROUPS;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicSubscriberGroupsData();

        data.setColumn(TopicSubscriberGroupsData.SUBSCRIBER_GROUP, aoColumns[2].toString());
        data.setColumn(TopicSubscriberGroupsData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(TopicSubscriberGroupsData.CHANNELS, Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(TopicSubscriberGroupsData.POLLED, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(TopicSubscriberGroupsData.MEAN, Float.valueOf(aoColumns[6].toString()));
        data.setColumn(TopicSubscriberGroupsData.ONE_MIN, Float.valueOf(aoColumns[7].toString()));
        data.setColumn(TopicSubscriberGroupsData.FIVE_MIN, Float.valueOf(aoColumns[8].toString()));
        data.setColumn(TopicSubscriberGroupsData.FIFTEEN_MIN, Float.valueOf(aoColumns[9].toString()));

        return data;
        }


    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        return TopicSubscriberData.preProcessReporterXMLCommon(model, sReporterXML);
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        SortedMap<Object, Data> mapData = new TreeMap<>();

        Pair<String, String> selectedTopic = model.getSelectedTopic();

        if (selectedTopic == null)
            {
            return mapData;
            }

        JsonNode rootNode  = requestSender.getDataForTopicSubscriberGroups(selectedTopic.getX(), selectedTopic.getY());
        JsonNode nodeItems = rootNode.get("items");

        if (nodeItems != null && nodeItems.isArray())
            {
            for (int k = 0; k < (nodeItems).size(); k++)
                {
                JsonNode node = nodeItems.get(k);

                TopicSubscriberGroupsData data = new TopicSubscriberGroupsData();

                data.setColumn(TopicSubscriberGroupsData.SUBSCRIBER_GROUP, node.get("name").asText());
                data.setColumn(TopicSubscriberGroupsData.CHANNELS, node.get("channelCount").asInt());
                data.setColumn(TopicSubscriberGroupsData.NODE_ID, node.get("nodeId").asInt());
                data.setColumn(TopicSubscriberGroupsData.POLLED, node.get("polledCount").asLong());
                data.setColumn(TopicSubscriberGroupsData.MEAN, Float.valueOf(node.get("polledMeanRate").asText()));
                data.setColumn(TopicSubscriberGroupsData.ONE_MIN, Float.valueOf(node.get("polledOneMinuteRate").asText()));
                data.setColumn(TopicSubscriberGroupsData.FIVE_MIN, Float.valueOf(node.get("polledFiveMinuteRate").asText()));
                data.setColumn(TopicSubscriberGroupsData.FIFTEEN_MIN, Float.valueOf(node.get("polledFifteenMinuteRate").asText()));

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
    public static final String REPORT_SUBSCRIBER_GROUPS = "reports/visualvm/topic-subscriber-groups.xml";

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for subscriber group.
     */
    public static final int SUBSCRIBER_GROUP = 1;

    /**
     * Array index for channels.
     */
    public static final int CHANNELS = 2;

    /**
     * Array index for polled.
     */
    public static final int POLLED = 3;

    /**
     * Array index for mean.
     */
    public static final int MEAN = 4;

    /**
     * Array index for one min.
     */
    public static final int ONE_MIN = 5;

    /**
     * Array index for five min.
     */
    public static final int FIVE_MIN = 6;

    /**
     * Array index for fifteen min.
     */
    public static final int FIFTEEN_MIN = 7;
    
    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicSubscriberGroupsData.class.getName());
    }
