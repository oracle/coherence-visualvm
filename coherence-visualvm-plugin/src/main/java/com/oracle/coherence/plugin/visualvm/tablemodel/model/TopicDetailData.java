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

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import static com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicSubscriberData.preProcessReporterXMLCommon;


/**
 * A class to hold topic detail data.
 *
 * @author tam  2022.12.14
 * @since  1.6.0
 */
public class TopicDetailData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create TopicDTopicSubscriberData passing in the number of columns.
     *
     */
    public TopicDetailData()
        {
        super(RETAIN_CONSUMED + 1);
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
        return REPORT_TOPIC_DETAIL;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicDetailData();

        data.setColumn(TopicDetailData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(TopicDetailData.PUBLISHED_TOTAL, Long.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(TopicDetailData.CHANNELS, Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(TopicDetailData.PAGE_CAPACITY, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(TopicDetailData.RECONNECT_RETRY, Integer.valueOf(getNumberValue(aoColumns[6].toString())));
        data.setColumn(TopicDetailData.RECONNECT_TIMEOUT, Integer.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(TopicDetailData.RECONNECT_WAIT, Integer.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(TopicDetailData.RETAIN_CONSUMED, aoColumns[9].toString());

        return data;
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        return preProcessReporterXMLCommon(model, sReporterXML);
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

        JsonNode rootNode  = requestSender.getDataForTopicsMembers(selectedTopic);
        JsonNode nodeItems = rootNode.get("items");

        if (nodeItems != null && nodeItems.isArray())
            {
            for (int k = 0; k < (nodeItems).size(); k++)
                {
                JsonNode node = nodeItems.get(k);

                TopicDetailData data = new TopicDetailData();
                
                data.setColumn(TopicDetailData.NODE_ID, node.get("nodeId").asInt());
                data.setColumn(TopicDetailData.CHANNELS, node.get("channelCount").asInt());
                data.setColumn(TopicDetailData.PUBLISHED_TOTAL, node.get("publishedCount").asLong());
                data.setColumn(TopicDetailData.PAGE_CAPACITY, node.get("pageCapacity").asInt());
                data.setColumn(TopicDetailData.RECONNECT_RETRY, node.get("reconnectRetry").asLong());
                data.setColumn(TopicDetailData.RECONNECT_TIMEOUT, node.get("reconnectTimeout").asLong());
                data.setColumn(TopicDetailData.RECONNECT_WAIT, node.get("reconnectWait").asLong());
                data.setColumn(TopicDetailData.RETAIN_CONSUMED, node.get("retainConsumed").asText());

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
    public static final String REPORT_TOPIC_DETAIL = "reports/visualvm/topic-detail.xml";

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;
    
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
    }
