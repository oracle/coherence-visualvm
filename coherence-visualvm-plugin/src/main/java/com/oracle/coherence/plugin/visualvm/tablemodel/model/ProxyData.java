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

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ArrayNode;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import java.util.ArrayList;
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

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * A class to hold basic proxy data.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class ProxyData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ProxyData passing in the number of columns.
     */
    public ProxyData()
        {
        super(TOTAL_MSG_SENT + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<>();
        Data                    data;

        try
            {
            Set<ObjectName> proxyNamesSet = requestSender.getAllProxyServerMembers();

            for (Iterator<ObjectName> nodIter = proxyNamesSet.iterator(); nodIter.hasNext(); )
                {
                ObjectName proxyNameObjName = (ObjectName) nodIter.next();

                String sServiceName     = proxyNameObjName.getKeyProperty("name");
                String sDomainPartition = proxyNameObjName.getKeyProperty("domainPartition");
                String sProtocol        = PROTOCOL_TCP;

                if (model.getClusterVersionAsInt() >= 122110)
                    {
                    sProtocol = (String) requestSender.getAttribute(proxyNameObjName, "Protocol");
                    }

                // only include the NameService if the model tells us we should  and
                // if its not the NameService, include anyway
                if ((!"NameService".equals(sServiceName) || model.isIncludeNameService()) &&
                    PROTOCOL_TCP.equals(sProtocol))
                    {
                    data = new ProxyData();

                    String sActualServiceName = (sDomainPartition == null ? "" : sDomainPartition + SERVICE_SEP) +
                                                sServiceName;

                    AttributeList listAttr = requestSender.getAttributes(proxyNameObjName,
                       new String[] { ATTR_HOSTIP, ATTR_CONNECTION_COUNT, ATTR_OUTGOING_MSG_BACKLOG,
                                      ATTR_TOTAL_BYTE_REC, ATTR_TOTAL_BYTE_SENT, ATTR_TOTAL_MSG_REC,
                                      ATTR_TOTAL_MSG_SENT });

                    data.setColumn(ProxyData.NODE_ID, Integer.valueOf(proxyNameObjName.getKeyProperty("nodeId")));
                    data.setColumn(ProxyData.SERVICE_NAME, sActualServiceName);

                    data.setColumn(ProxyData.HOST_PORT, (String) getAttributeValueAsString(listAttr, ATTR_HOSTIP));
                    data.setColumn(ProxyData.CONNECTION_COUNT, Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_CONNECTION_COUNT)));
                    data.setColumn(ProxyData.OUTGOING_MSG_BACKLOG, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_OUTGOING_MSG_BACKLOG)));
                    data.setColumn(ProxyData.TOTAL_BYTES_RECEIVED, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_BYTE_REC)));
                    data.setColumn(ProxyData.TOTAL_BYTES_SENT, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_BYTE_SENT)));
                    data.setColumn(ProxyData.TOTAL_MSG_RECEIVED, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_MSG_REC)));
                    data.setColumn(ProxyData.TOTAL_MSG_SENT, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_MSG_SENT)));

                    mapData.put((String) getAttributeValueAsString(listAttr, ATTR_HOSTIP), data);
                    }
                }

            return new ArrayList<>(mapData.entrySet());
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, "Error getting proxy statistics", e);

            return null;
            }
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_PROXY;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        String sServiceName = aoColumns[3].toString();
        Data   data         = null;

        // only include the NameService if the model tells us we should  and
        // if its not the NameService, include anyway
        if (("NameService".equals(sServiceName) && model.isIncludeNameService()) || !"NameService".equals(sServiceName))
            {
            data = new ProxyData();

            data.setColumn(ProxyData.HOST_PORT, aoColumns[2].toString());
            data.setColumn(ProxyData.SERVICE_NAME, aoColumns[3].toString());
            data.setColumn(ProxyData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[4].toString())));

            data.setColumn(ProxyData.CONNECTION_COUNT, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
            data.setColumn(ProxyData.OUTGOING_MSG_BACKLOG, Long.valueOf(getNumberValue(aoColumns[6].toString())));
            data.setColumn(ProxyData.TOTAL_BYTES_RECEIVED, Long.valueOf(getNumberValue(aoColumns[7].toString())));
            data.setColumn(ProxyData.TOTAL_BYTES_SENT, Long.valueOf(getNumberValue(aoColumns[8].toString())));
            data.setColumn(ProxyData.TOTAL_MSG_RECEIVED, Long.valueOf(getNumberValue(aoColumns[9].toString())));
            data.setColumn(ProxyData.TOTAL_MSG_SENT, Long.valueOf(getNumberValue(aoColumns[10].toString())));

            if (aoColumns.length == 12 && aoColumns[11] != null)
                {
                // domain partition is present
                data.setColumn(ProxyData.SERVICE_NAME, aoColumns[11].toString() + SERVICE_SEP +
                                                       aoColumns[3].toString());
                }
            }

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        JsonNode                rootNode             = requestSender.getDataForProxyMembers();
        SortedMap<Object, Data> mapData              = new TreeMap<Object, Data>();
        JsonNode                nodeProxyMemberItems = rootNode.get("items");
        if (nodeProxyMemberItems != null && nodeProxyMemberItems.isArray())
            {
            for (int k = 0; k < ((ArrayNode) nodeProxyMemberItems).size(); k++)
                {
                JsonNode proxyNode = (JsonNode) nodeProxyMemberItems.get(k);

                String sServiceName = proxyNode.get("name").asText();
                String sProtocol    = proxyNode.get("protocol").asText();
                if (("NameService".equals(sServiceName) && model.isIncludeNameService())
                        || !"NameService".equals(sServiceName) &&
                        PROTOCOL_TCP.equals(sProtocol))
                    {
                    ProxyData data = new ProxyData();

                    data.setColumn(ProxyData.HOST_PORT, proxyNode.get("hostIP").asText());
                    data.setColumn(ProxyData.SERVICE_NAME, sServiceName);
                    data.setColumn(ProxyData.NODE_ID,
                            Integer.valueOf(proxyNode.get("nodeId").asText()));
                    data.setColumn(ProxyData.CONNECTION_COUNT,
                            Integer.valueOf(proxyNode.get("connectionCount").asText()));
                    data.setColumn(ProxyData.OUTGOING_MSG_BACKLOG,
                            Long.valueOf(proxyNode.get("outgoingMessageBacklog").asText()));
                    data.setColumn(ProxyData.TOTAL_BYTES_RECEIVED,
                            Long.valueOf(proxyNode.get("totalBytesReceived").asText()));
                    data.setColumn(ProxyData.TOTAL_BYTES_SENT,
                            Long.valueOf(proxyNode.get("totalBytesSent").asText()));
                    data.setColumn(ProxyData.TOTAL_MSG_RECEIVED,
                            Long.valueOf(proxyNode.get("totalMessagesReceived").asText()));
                    data.setColumn(ProxyData.TOTAL_MSG_SENT,
                            Long.valueOf(proxyNode.get("totalMessagesSent").asText()));

                    JsonNode sDomainPartition = proxyNode.get("domainPartition");
                    if (sDomainPartition != null)
                        {
                        // domain partition is present
                        data.setColumn(ProxyData.SERVICE_NAME, sDomainPartition + SERVICE_SEP +
                                                               sServiceName);
                        }

                    mapData.put(data.getColumn(0), data);
                    }
                }
            }
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802484301825295L;

    /**
     * Array index for host/port.
     */
    public static final int HOST_PORT = 0;

    /**
     * Array index for service name.
     */
    public static final int SERVICE_NAME = 1;

    /**
     * Array index for Node id.
     */
    public static final int NODE_ID = 2;

    /**
     * Array index for connection count.
     */
    public static final int CONNECTION_COUNT = 3;

    /**
     * Array index for outgoing message backlog.
     */
    public static final int OUTGOING_MSG_BACKLOG = 4;

    /**
     * Array index for total bytes received.
     */
    public static final int TOTAL_BYTES_RECEIVED = 5;

    /**
     * Array index for total bytes sent.
     */
    public static final int TOTAL_BYTES_SENT = 6;

    /**
     * Array index for total messages received.
     */
    public static final int TOTAL_MSG_RECEIVED = 7;

    /**
     * Array index for total messages sent.
     */
    public static final int TOTAL_MSG_SENT = 8;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ProxyData.class.getName());

    /**
     * Report for proxy server data.
     */
    public static final String REPORT_PROXY = "reports/visualvm/proxy-stats.xml";

    /**
     * JMX attribute name for HostIP.
     */
    private static final String ATTR_HOSTIP = "HostIP";

    /**
     * JMX attribute name for Connection Count.
     */
    private static final String ATTR_CONNECTION_COUNT = "ConnectionCount";

    /**
     * JMX attribute name for Outgoing Message Backlog.
     */
    private static final String ATTR_OUTGOING_MSG_BACKLOG = "OutgoingMessageBacklog";

    /**
     * JMX attribute name for Outgoing Total Bytes Received.
     */
    private static final String ATTR_TOTAL_BYTE_REC = "TotalBytesReceived";

    /**
     * JMX attribute name for Outgoing Total Bytes Sent.
     */
    private static final String ATTR_TOTAL_BYTE_SENT = "TotalBytesSent";

    /**
     * JMX attribute name for Outgoing Total Messages Received.
     */
    private static final String ATTR_TOTAL_MSG_REC = "TotalMessagesReceived";

    /**
     * JMX attribute name for Outgoing Total Messages Sent.
     */
    private static final String ATTR_TOTAL_MSG_SENT = "TotalMessagesSent";

    /**
     * Protocol for ConnectionManager MBean for proxy server.
     */
    private static final String PROTOCOL_TCP = "tcp";
    }
