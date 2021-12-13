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

import com.fasterxml.jackson.databind.JsonNode;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Logger;

import javax.management.ObjectName;

/**
 * A class to hold Federated Destination data.
 *
 * @author bb  2014.01.29
 *
 * @since  12.2.1
 */
public class FederationDestinationData
        extends FederationData
    {

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public String getReporterReport()
        {
        return REPORT_DESTINATION;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new FederationDestinationData();

        for (Column col : Column.values())
            {
            int ordinal = col.ordinal();
            switch (col)
                {
                case KEY:
                    Pair<String, String> pair = new Pair<String, String>(aoColumns[Column.SERVICE.getColumn()].toString(),
                            aoColumns[Column.PARTICIPANT.getColumn()].toString());
                    data.setColumn(ordinal, pair);
                    break;
                case SERVICE:
                case PARTICIPANT:
                case STATUS:
                    data.setColumn(ordinal, aoColumns[col.getColumn()]);
                    break;
                case TOTAL_BYTES_SENT:
                case TOTAL_MSGS_SENT:
                    data.setColumn(ordinal, Long.valueOf(getNumberValue(aoColumns[col.getColumn()] == null ? "0" :  aoColumns[col.getColumn()].toString())));
                    break;
                }
            }
        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        Set<String> setServices = retrieveFederatedServices(requestSender);

        SortedMap<Object, Data> mapData = new TreeMap<>();

        for (String sService : setServices)
            {
            String[] as               = sService.split("/");
            String   sDomainPartition = as.length == 2 ? as[0] : null;
            String   sServiceName     = as.length == 2 ? as[1] : as[0];
            JsonNode nodeRoot         = requestSender.getAggregatedOutgoingData(sServiceName, sDomainPartition);

            JsonNode nodeItems = nodeRoot.get("items");
            if (nodeItems != null && nodeItems.isArray())
                {
                for (int i = 0; i < nodeItems.size() ; i++)
                    {
                    Data     data             = new FederationDestinationData();
                    JsonNode nodeItem         = nodeItems.get(i);
                    String   sParticipantName = nodeItem.get("participantName").asText();

                    for (Column col : Column.values())
                        {
                        int ordinal = col.ordinal();
                        switch (col)
                            {
                            case KEY:
                                Pair<String, String> pair = new Pair<String, String>(sService, sParticipantName);
                                data.setColumn(ordinal, pair);
                                break;
                            case SERVICE:
                                data.setColumn(ordinal, sService);
                                break;
                            case PARTICIPANT:
                                data.setColumn(ordinal, sParticipantName);
                                break;
                            case TOTAL_BYTES_SENT:
                                data.setColumn(ordinal,
                                        Long.valueOf(getChildValue("sum", "bytesSentSecs", nodeItem)));
                                break;
                            case STATUS:
                                data.setColumn(ordinal,
                                        Long.valueOf(getChildValue("max", "status", nodeItem)));
                                break;
                            case TOTAL_MSGS_SENT:
                                data.setColumn(ordinal,
                                        Long.valueOf(getChildValue("sum", "msgsSentSecs", nodeItem)));
                                break;
                            }
                        }
                    mapData.put(data.getColumn(0), data);
                    }
                }
            }

        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 7075440287604402611L;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(FederationDestinationData.class.getName());

    /**
     * Report for destination data.
     */
    public static final String REPORT_DESTINATION = "reports/visualvm/federation-destination-stats.xml";
    }
