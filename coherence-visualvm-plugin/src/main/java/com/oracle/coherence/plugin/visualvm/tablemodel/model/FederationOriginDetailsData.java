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

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to hold Federated Origin data.
 *
 * @author bb  2014.01.29
 *
 * @since  12.2.1
 */
public class FederationOriginDetailsData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ServiceData passing in the number of columns.
     */
    public FederationOriginDetailsData()
        {
        super(Column.values().length);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    @SuppressWarnings("rawtypes")
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_DESTINATION_DETAILS;
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // the report XML contains the following tokens that require substitution:
        // %SERVICE_NAME%
        // %PARTICIPANT_NAME%
        Pair<String, String> serviceParticipant = model.getSelectedServiceParticipant();

        String query = serviceParticipant == null ? sReporterXML :
               sReporterXML.replaceAll("%SERVICE_NAME%", serviceParticipant.getX())
                           .replaceAll("%PARTICIPANT_NAME%", serviceParticipant.getY());

        return query;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new FederationOriginDetailsData();

        for (Column col : Column.values())
            {
            int ordinal = col.ordinal();
            switch (col)
                {
                case NODE_ID:
                    data.setColumn(ordinal, aoColumns[col.getColumn()]);
                    break;
                case TOTAL_BYTES_RECEIVED:
                case TOTAL_RECORDS_RECEIVED:
                case TOTAL_ENTRIES_RECEIVED:
                case TOTAL_MSG_RECEIVED:
                case TOTAL_MSG_UNACKED:
                case MSG_APPLY_TIME_PERCENTILE_MILLIS:
                case RECORD_BACKLOG_DELAY_TIME_PERCENTILE_MILLIS:
                    data.setColumn(ordinal, Long.valueOf(getNumberValue(aoColumns[col.getColumn()].toString())));
                    break;
                default:
                    LOGGER.log(Level.SEVERE, "Unknown column name " + col);
                }
            }
        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender) throws Exception
        {
        Pair<String, String> serviceParticipant = model.getSelectedServiceParticipant();
        if (serviceParticipant == null)
            {
            return null;
            }

        String sServiceName = serviceParticipant.getX();
        String sParticipantName = serviceParticipant.getY();

        JsonNode rootNode = requestSender.getIncomingDataForParticipant(sServiceName, sParticipantName);

        JsonNode nodeParticipantItems = rootNode.get("items");

        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        if (nodeParticipantItems != null && nodeParticipantItems.isArray())
            {
            for (int j = 0; j < nodeParticipantItems.size() ; j++)
                {
                JsonNode participantNode = nodeParticipantItems.get(j);
                Data data = new FederationOriginDetailsData();

                for (Column col : Column.values())
                    {
                    int ordinal = col.ordinal();
                    switch (col)
                        {
                        case NODE_ID:
                            data.setColumn(ordinal, participantNode.get("nodeId").asText());
                            break;
                        case TOTAL_BYTES_RECEIVED:
                            data.setColumn(ordinal, Long.valueOf(participantNode.get("totalBytesReceived").asText()));
                            break;
                        case TOTAL_RECORDS_RECEIVED:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode.get("totalRecordsReceived").asText()));
                            break;
                        case TOTAL_ENTRIES_RECEIVED:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode.get("totalEntriesReceived").asText()));
                            break;
                        case TOTAL_MSG_RECEIVED:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode.get("totalMsgReceived").asText()));
                            break;
                        case TOTAL_MSG_UNACKED:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode.get("totalMsgUnacked").asText()));
                            break;
                        case MSG_APPLY_TIME_PERCENTILE_MILLIS:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode.get("msgApplyTimePercentileMillis").asText()));
                            break;
                        case RECORD_BACKLOG_DELAY_TIME_PERCENTILE_MILLIS:
                            data.setColumn(ordinal,
                                    Long.valueOf(participantNode
                                            .get("recordBacklogDelayTimePercentileMillis").asText()));
                            break;
                        default:
                            LOGGER.log(Level.SEVERE, "Unknown column name " + col);
                        }
                    }
                mapData.put(data.getColumn(0), data);
                }
            }
        return mapData;

        }
    /**
     * Defines the data collected from origin MBeans.
     */
    public enum Column
        {
        NODE_ID(1),
        TOTAL_BYTES_RECEIVED(2),
        TOTAL_RECORDS_RECEIVED(3),
        TOTAL_ENTRIES_RECEIVED(4),
        TOTAL_MSG_RECEIVED(5),
        TOTAL_MSG_UNACKED(6),
        MSG_APPLY_TIME_PERCENTILE_MILLIS(7),
        RECORD_BACKLOG_DELAY_TIME_PERCENTILE_MILLIS(8);

        Column(int nCol)
            {
            f_nCol = nCol;
            }

        /**
         * Returns the column number for this enum.
         *
         * @return the column number
         */
        public int getColumn()
            {
            return f_nCol;
            }

        /**
         * The column number associates with thw enum.
         */
        protected final int f_nCol;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -1525794029894938136L;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(FederationOriginDetailsData.class.getName());

    /**
     * Report for destination data.
     */
    public static final String REPORT_DESTINATION_DETAILS = "reports/visualvm/federation-origin-detail-stats.xml";
    }
