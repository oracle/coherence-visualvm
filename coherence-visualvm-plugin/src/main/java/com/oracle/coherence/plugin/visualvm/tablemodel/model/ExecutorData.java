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
import java.util.logging.Logger;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

/**
 * A class to hold basic Executor data.
 *
 * @author tam  2021.08.11
 * @since  1.2.0
 */
public class ExecutorData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ExecutorData passing in the number of columns.
     */
    public ExecutorData()
        {
        super(HEAP_FREE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
    * {@inheritDoc}
    */
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        // never used as report will provide data
        return null;
        }

    /**
     * {@inheritDoc}
     */
    public String getReporterReport()
        {
        return REPORT_EXECUTOR;
        }

    /**
     * {@inheritDoc}
     */
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new ExecutorData();

        data.setColumn(ExecutorData.NAME, aoColumns[1].toString());
        data.setColumn(ExecutorData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(ExecutorData.TASKS_COMPLETED, Long.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(ExecutorData.TASKS_REJECTED, Long.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(ExecutorData.TASKS_IN_PROGRESS, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(ExecutorData.STATE, aoColumns[6].toString());

        long nHeapMax  = Long.parseLong(getNumberValue(aoColumns[7].toString())) / GraphHelper.MB;
        long nHeapUsed = Long.parseLong(getNumberValue(aoColumns[8].toString())) / GraphHelper.MB;
        
        data.setColumn(ExecutorData.HEAP_MAX, nHeapMax);
        data.setColumn(ExecutorData.HEAP_USED, nHeapUsed);
        data.setColumn(ExecutorData.HEAP_FREE, nHeapMax - nHeapUsed);

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
//        JsonNode                rootNode             = requestSender.getDataForProxyMembers();
//        SortedMap<Object, Data> mapData              = new TreeMap<Object, Data>();
//        JsonNode                nodeProxyMemberItems = rootNode.get("items");
//        if (nodeProxyMemberItems != null && nodeProxyMemberItems.isArray())
//            {
//            for (int k = 0; k < ((ArrayNode) nodeProxyMemberItems).size(); k++)
//                {
//                JsonNode proxyNode = (JsonNode) nodeProxyMemberItems.get(k);
//
//                String sServiceName = proxyNode.get("name").asText();
//                String sProtocol    = proxyNode.get("protocol").asText();
//                if (("NameService".equals(sServiceName) && model.isIncludeNameService())
//                        || !"NameService".equals(sServiceName) &&
//                        PROTOCOL_TCP.equals(sProtocol))
//                    {
//                    ExecutorData data = new ExecutorData();
//
//                    data.setColumn(ExecutorData.HOST_PORT, proxyNode.get("hostIP").asText());
//                    data.setColumn(ExecutorData.SERVICE_NAME, sServiceName);
//                    data.setColumn(ExecutorData.NODE_ID,
//                            Integer.valueOf(proxyNode.get("nodeId").asText()));
//                    data.setColumn(ExecutorData.CONNECTION_COUNT,
//                            Integer.valueOf(proxyNode.get("connectionCount").asText()));
//                    data.setColumn(ExecutorData.OUTGOING_MSG_BACKLOG,
//                            Long.valueOf(proxyNode.get("outgoingMessageBacklog").asText()));
//                    data.setColumn(ExecutorData.TOTAL_BYTES_RECEIVED,
//                            Long.valueOf(proxyNode.get("totalBytesReceived").asText()));
//                    data.setColumn(ExecutorData.TOTAL_BYTES_SENT,
//                            Long.valueOf(proxyNode.get("totalBytesSent").asText()));
//                    data.setColumn(ExecutorData.TOTAL_MSG_RECEIVED,
//                            Long.valueOf(proxyNode.get("totalMessagesReceived").asText()));
//                    data.setColumn(ExecutorData.TOTAL_MSG_SENT,
//                            Long.valueOf(proxyNode.get("totalMessagesSent").asText()));
//
//                    JsonNode sDomainPartition = proxyNode.get("domainPartition");
//                    if (sDomainPartition != null)
//                        {
//                        // domain partition is present
//                        data.setColumn(ExecutorData.SERVICE_NAME, sDomainPartition + SERVICE_SEP +
//                                                                  sServiceName);
//                        }
//
//                    mapData.put(data.getColumn(0), data);
//                    }
//                }
//            }
//        return mapData;
            return null;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802484301825295L;

    /**
     * Array index for name.
     */
    public static final int NAME = 0;

    /**
     * Array index for Node id.
     */
    public static final int NODE_ID = 1;

    /**
     * Array index for state
     */
    public static final int STATE = 2;

    /**
     * Array index for tasks in progress.
     */
    public static final int TASKS_IN_PROGRESS = 3;

    /**
     * Array index for tasks completed.
     */
    public static final int TASKS_COMPLETED = 4;

    /**
     * Array index for tasks rejected.
     */
    public static final int TASKS_REJECTED = 5;

    /**
     * Array index for heap max.
     */
    public static final int HEAP_MAX = 6;

    /**
     * Array index for heap used
     */
    public static final int HEAP_USED = 7;

    /**
     * Array index for free used
     */
    public static final int HEAP_FREE = 8;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ExecutorData.class.getName());

    /**
     * Report for proxy server data.
     */
    public static final String REPORT_EXECUTOR = "reports/visualvm/executor-stats.xml";
    }