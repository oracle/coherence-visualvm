/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved.
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
 * A class to hold topic subscriber data.
 *
 * @author tam  2020.02.08
 * @since  1.0.1
 */
public class TopicSubscriberData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create TopicDTopicSubscriberData passing in the number of columns.
     *
     */
    public TopicSubscriberData()
        {
        super(TYPE + 1);
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
        return REPORT_TOPIC_SUBSCRIBERS;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicSubscriberData();

        data.setColumn(NODE_ID, Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(SUBSCRIBER, Long.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(STATE, aoColumns[4].toString());
        data.setColumn(CHANNELS, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
        Object sSubscriberGroup = aoColumns[6];
        data.setColumn(SUBSCRIBER_GROUP, sSubscriberGroup != null ? sSubscriberGroup.toString() : "n/a");
        data.setColumn(RECEIVED, Long.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(ERRORS, Long.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(BACKLOG, Long.valueOf(getNumberValue(aoColumns[9].toString())));
        data.setColumn(TYPE, aoColumns[10].toString());

        return data;
        }


    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        return preProcessReporterXMLCommon(model, sReporterXML);
        }

    public static String preProcessReporterXMLCommon(VisualVMModel model, String sReporterXML)
        {
        // the report XML contains the following tokens that require substitution:
        // %SERVICE_NAME%
        // %TOPIC_NAME%
        Pair<String, String> selectedTopic = model.getSelectedTopic();

        if (selectedTopic != null)
            {
            return sReporterXML.replaceAll("%SERVICE_NAME%", escape(selectedTopic.getX()))
                               .replaceAll("%TOPIC_NAME%", escape(selectedTopic.getY()));
            }

        return sReporterXML;
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

        JsonNode rootNode  = requestSender.getDataForTopicSubscribers(selectedTopic.getX(), selectedTopic.getY());
        JsonNode nodeItems = rootNode.get("items");

        if (nodeItems != null && nodeItems.isArray())
            {
            for (int k = 0; k < (nodeItems).size(); k++)
                {
                JsonNode node = nodeItems.get(k);

                TopicSubscriberData data = new TopicSubscriberData();

                data.setColumn(SUBSCRIBER, node.get("id").asLong());
                data.setColumn(NODE_ID, node.get("nodeId").asInt());
                data.setColumn(STATE, node.get("stateName").asText());
                data.setColumn(CHANNELS, node.get("channelCount").asInt());

                String sSubType = node.get("subType").asText();
                String sSubscriberGroup = "n/a";

                if ("Durable".equals(sSubType))
                    {
                    JsonNode jsonNode = node.get("subscriberGroup");
                    sSubscriberGroup  = jsonNode != null ? jsonNode.asText() : "n/a";
                    }

                data.setColumn(SUBSCRIBER_GROUP, sSubscriberGroup);
                data.setColumn(RECEIVED, node.get("receivedCount").asLong());
                data.setColumn(ERRORS, node.get("receiveErrors").asLong());
                data.setColumn(BACKLOG, node.get("backlog").asLong());
                data.setColumn(TYPE, sSubType);

                mapData.put(data.getColumn(SUBSCRIBER), data);
                }
            }
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 6427775621469258645L;

    /**
     * Report for topics data.
     */
    public static final String REPORT_TOPIC_SUBSCRIBERS = "reports/visualvm/topic-subscribers.xml";

    /**
     * Array index for subscriber.
     */
    public static final int SUBSCRIBER = 0;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 1;

    /**
     * Array index for state.
     */
    public static final int STATE = 2;

    /**
     * Array index for channels.
     */
    public static final int CHANNELS = 3;

    /**
     * Array index for subscriber group.
     */
    public static final int SUBSCRIBER_GROUP = 4;

    /**
     * Array index for received.
     */
    public static final int RECEIVED = 5;

    /**
     * Array index for errors.
     */
    public static final int ERRORS = 6;

    /**
     * Array index for backlog.
     */
    public static final int BACKLOG = 7;
    /**
     * Array index for type.
     */
    public static final int TYPE = 8;
    
    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicSubscriberData.class.getName());
    }
