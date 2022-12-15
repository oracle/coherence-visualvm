/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.panel;


import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ExecutorData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.GrpcProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MachineData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.RamJournalData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicData;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import static com.oracle.coherence.plugin.visualvm.helper.RenderHelper.getRenderedBytes;


/**
 * An implementation of an {@link AbstractCoherencePanel} to view a cluster
 * snapshot.
 *
 * @author tam  2021.02.23
 * @since 1.0.1
 */
public class CoherenceClusterSnapshotPanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceClusterSnapshotPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceClusterSnapshotPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        setOpaque(false);
        f_htmlTextArea = new JEditorPane("text/html", "")
            {
            @Override
            public Dimension getPreferredScrollableViewportSize()
                {
                return new Dimension(200, 200);
                }
            };
        f_htmlTextArea.setEditable(false);
        f_htmlTextArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

        JScrollPane scrollPane = new JScrollPane(f_htmlTextArea);
        add(scrollPane);
        }

    // ----- AbstractCoherencePanel methods ----------------------------------

    @Override
    public void updateGUI()
        {
        try
            {
            StringBuilder sb =
                    new StringBuilder(htmlHead())
                            .append(clusterOverview())
                            .append(HR)
                            .append(machinesOverview())
                            .append(HR)
                            .append(membersOverview())
                            .append(HR)
                            .append(servicesOverview())
                            .append(HR)
                            .append(cachesOverview())
                            .append(HR);

            if (f_model.isCoherenceExtendConfigured())
                {
                sb.append(proxyServerOverview()).append(HR);
                }
            if (f_model.isPersistenceConfigured())
                {
                sb.append(persistenceOverview()).append(HR);
                }
            if (f_model.isHttpProxyConfigured())
                {
                sb.append(httpProxyOverview()).append(HR);
                }
            if (f_model.isFederationCongfigured())
                {
                sb.append(federationOverview()).append(HR);
                }
            if (f_model.isElasticDataConfigured())
                {
                sb.append(elasticDataOverview("RAM"))
                  .append(elasticDataOverview("FLASH"))
                  .append(HR);
                }
            if (f_model.isExecutorConfigured())
                {
                sb.append(executorOverview()).append(HR);
                }
            if (f_model.isTopicsConfigured())
                {
                sb.append(topicsOverview()).append(HR);
                }
            if (f_model.isGrpcProxyConfigured())
                {
                sb.append(grpcOverview()).append(HR);
                }

            String sCurrent = sb.append("</body></html>").toString();
            if (!sCurrent.equals(m_lastValue))
                {
                // the value has changed so update this stops flicker if the data has not been refreshed
                f_htmlTextArea.setText(sCurrent);
                m_lastValue = sCurrent;
                }
            }
        catch (Exception e)
            {
            // in the case of error just log the message and don't throw the exception otherwise the
            // whole plugin will not display
            LOGGER.warning("Failed to render cluster snapshot " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            }
        }

    // ----- helpers --------------------------------------------------------

    private String htmlHead()
        {
        return "<html>\n"
               + "<head>\n"
               + "<style>\n"
               + "table, th, td {\n"
               + "  border: 0px solid black;\n"
               + "  border-collapse: collapse;\n"
               + "}\n"
               + "th, td {\n"
               + "  padding: 5px;\n"
               + "}\n"
               + "th {\n"
               + "  text-align: left;\n"
               + "}\n"
               + "</style>\n"
               + "</head>\n"
               + "<body>";
        }

    /**
     * Returns a cluster overview.
     *
     * @return a cluster overview.
     */
    private String clusterOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_cluster_overview")));

        sb.append(tableStart());
        for (Map.Entry<Object, Data> entry : m_clusterData)
            {
            sb.append(tableRow(getLabel("LBL_cluster_name"), entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString()))
              .append(tableRow(getLabel("LBL_refresh_date"), new Date(f_model.getLastUpdate()).toString()))
              .append(tableRow(getLabel("LBL_version"), entry.getValue().getColumn(ClusterData.VERSION).toString()))
              .append(tableRow(getLabel("LBL_license_mode"), entry.getValue().getColumn(ClusterData.LICENSE_MODE).toString()))
              .append(tableRow(getLabel("LBL_members"),
                               String.format("%d", (Integer) entry.getValue().getColumn(ClusterData.CLUSTER_SIZE))));
            }

        // get storage members
        Object[] aoStorageDetails = getStorageDetails(m_memberData);

        int cTotalMemory = (int) aoStorageDetails[0];
        int cTotalMemoryUsed = (int) aoStorageDetails[1];
        int cStorageCount = (int) aoStorageDetails[2];
        String sEdition = (String) aoStorageDetails[4];
        sb.append(tableRow(getLabel("LBL_total_storage_members"), getMemoryFormat(cStorageCount)));

        // get cluster status HA
        int bestStatusHA = getClusterStatusHA(m_serviceData);
        sb.append(tableRow(getLabel("LBL_cluster_statusha"), bestStatusHA >= 0
                                                             ? STATUSHA_VALUES[bestStatusHA]
                                                             : "n/a"));

        sb.append(tableRow(getLabel("LBL_total_cluster_memory"), getMemoryFormat(cTotalMemory)));
        sb.append(tableRow(getLabel("LBL_total_cluster_memory_used"), getMemoryFormat(cTotalMemoryUsed)));
        sb.append(tableRow(getLabel("LBL_edition"), sEdition));

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a machines overview.
     *
     * @return a machines overview
     */
    private String machinesOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_machines")));

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.MACHINE));

        for (Map.Entry<Object, Data> entry : m_machineData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(MachineData.MACHINE_NAME).toString()))
                    .append(td(entry.getValue().getColumn(MachineData.PROCESSOR_COUNT).toString()))
                    .append(td(entry.getValue().getColumn(MachineData.SYSTEM_LOAD_AVERAGE).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MachineData.TOTAL_PHYSICAL_MEMORY).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MachineData.FREE_PHYSICAL_MEMORY).toString())))
                    .append(td(getPercentFormat(entry.getValue().getColumn(MachineData.PERCENT_FREE_MEMORY).toString())))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a members overview.
     *
     * @return a members overview
     */
    private String membersOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_members")));

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.MEMBER));

        for (Map.Entry<Object, Data> entry : m_memberData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(MemberData.NODE_ID).toString()))
                    .append(td(entry.getValue().getColumn(MemberData.ADDRESS).toString()))
                    .append(td(entry.getValue().getColumn(MemberData.PORT).toString()))
                    .append(td(entry.getValue().getColumn(MemberData.ROLE_NAME).toString()))
                    .append(td(getPublisherValue(entry.getValue().getColumn(MemberData.PUBLISHER_SUCCESS).toString())))
                    .append(td(getPublisherValue(entry.getValue().getColumn(MemberData.RECEIVER_SUCCESS).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MemberData.SENDQ_SIZE).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MemberData.MAX_MEMORY).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MemberData.USED_MEMORY).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(MemberData.FREE_MEMORY).toString())))
                    .append(td(entry.getValue().getColumn(MemberData.STORAGE_ENABLED).toString()))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a services overview.
     *
     * @return a services overview
     */
    private String servicesOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_services")));

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.SERVICE));

        for (Map.Entry<Object, Data> entry : m_serviceData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(ServiceData.SERVICE_NAME).toString()))
                    .append(td(entry.getValue().getColumn(ServiceData.STATUS_HA).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.MEMBERS).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.STORAGE_MEMBERS))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.PARTITION_COUNT))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.PARTITIONS_ENDANGERED))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.PARTITIONS_VULNERABLE))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.PARTITIONS_UNBALANCED))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ServiceData.REQUESTS_PENDING))))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a caches overview.
     *
     * @return a caches overview
     */
    private String cachesOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_caches")));

        float cTotalCacheSize = 0.0f;

        for (Map.Entry<Object, Data> entry : m_cacheData)
            {
            cTotalCacheSize += Float.valueOf((Integer) entry.getValue().getColumn(CacheData.MEMORY_USAGE_MB));
            }

        sb.append(tableStart())
                .append(tableRow(getLabel("LBL_total_caches"), getMemoryFormat(m_cacheData.size())))
                .append(tableRow(getLabel("LBL_total_data"), String.format("%,10.2f", cTotalCacheSize)))
                .append(tableEnd());

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.CACHE));

        for (Map.Entry<Object, Data> entry : m_cacheData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(CacheData.CACHE_NAME).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(CacheData.SIZE))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(CacheData.MEMORY_USAGE_BYTES))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(CacheData.MEMORY_USAGE_MB))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(CacheData.AVG_OBJECT_SIZE))))
                    .append(td((String) entry.getValue().getColumn(CacheData.UNIT_CALCULATOR)))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a proxy server overview.
     *
     * @return a proxy server overview
     */
    private String proxyServerOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_proxy_servers")));

        int cTotalConnections = 0;

        for (Map.Entry<Object, Data> entry : m_cacheData)
            {
            cTotalConnections += (Integer) entry.getValue().getColumn(ProxyData.CONNECTION_COUNT);
            }

        sb.append(tableStart())
                .append(tableRow(getLabel("LBL_total_proxy_servers"), getMemoryFormat(m_proxyData.size())))
                .append(tableRow(getLabel("LBL_total_connections"), getMemoryFormat(cTotalConnections)))
                .append(tableEnd());

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.PROXY));

        for (Map.Entry<Object, Data> entry : m_proxyData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(ProxyData.HOST_PORT).toString()))
                    .append(td(entry.getValue().getColumn(ProxyData.SERVICE_NAME).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.NODE_ID).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.CONNECTION_COUNT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.OUTGOING_MSG_BACKLOG).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.TOTAL_BYTES_RECEIVED).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.TOTAL_BYTES_SENT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.TOTAL_MSG_RECEIVED).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(ProxyData.TOTAL_MSG_SENT).toString())))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a persistence overview.
     *
     * @return a persistence overview
     */
    private String persistenceOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_persistence")));

        Object[] persistenceData = getPersistenceData(m_persistenceData);
        long cTotalActive = (Long) persistenceData[0];
        long cTotalBackup = (Long) persistenceData[1];
        float cLatencyMax = (Float) persistenceData[3];

        sb.append(tableStart())
                .append(tableRow(getLabel("LBL_total_active_space"), getMemoryFormat(cTotalActive)))
                .append(tableRow(getLabel("LBL_total_backup_space"), getMemoryFormat(cTotalBackup)))
                .append(tableRow(getLabel("LBL_max_latency_across_services"), getLatencyValue(Float.toString(cLatencyMax))))
                .append(tableEnd());

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.PERSISTENCE));

        for (Map.Entry<Object, Data> entry : m_persistenceData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(PersistenceData.SERVICE_NAME).toString()))
                    .append(td(entry.getValue().getColumn(PersistenceData.PERSISTENCE_MODE).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(PersistenceData.TOTAL_ACTIVE_SPACE_USED).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(PersistenceData.TOTAL_ACTIVE_SPACE_USED_MB).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(PersistenceData.TOTAL_BACKUP_SPACE_USED_MB).toString())))
                    .append(td(getLatencyValue(entry.getValue().getColumn(PersistenceData.AVERAGE_LATENCY).toString())))
                    .append(td(getLatencyValue(entry.getValue().getColumn(PersistenceData.MAX_LATENCY).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(PersistenceData.SNAPSHOT_COUNT).toString())))
                    .append(td(entry.getValue().getColumn(PersistenceData.STATUS).toString()))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a Http proxy overview.
     *
     * @return a Http Proxy overview
     */
    private String httpProxyOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_http_proxy_servers")));

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.HTTP_PROXY));

        for (Map.Entry<Object, Data> entry : m_httpProxyData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(HttpProxyData.SERVICE_NAME).toString()))
                    .append(td(entry.getValue().getColumn(HttpProxyData.HTTP_SERVER_TYPE).toString()))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(HttpProxyData.MEMBER_COUNT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(HttpProxyData.TOTAL_REQUEST_COUNT))))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(HttpProxyData.TOTAL_ERROR_COUNT))))
                    .append(td(getLatencyValue(entry.getValue().getColumn(HttpProxyData.AVERAGE_REQ_PER_SECOND))))
                    .append(td(getLatencyValue(entry.getValue().getColumn(HttpProxyData.AVERAGE_REQ_TIME))))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns an Executor overview.
     *
     * @return an Executor overview
     */
    private String executorOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_executors")));

        sb.append(tableStart());

        sb.append(columnHeaders(VisualVMModel.DataType.EXECUTOR));

        for (Map.Entry<Object, Data> entry : m_executorData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(ExecutorData.NAME).toString()))
                    .append(td(entry.getValue().getColumn(ExecutorData.EXECUTOR_COUNT).toString()))
                    .append(td(entry.getValue().getColumn(ExecutorData.TASKS_IN_PROGRESS).toString()))
                    .append(td(entry.getValue().getColumn(ExecutorData.TASKS_COMPLETED).toString()))
                    .append(td(entry.getValue().getColumn(ExecutorData.TASKS_REJECTED).toString()))
                    .append(td(entry.getValue().getColumn(ExecutorData.DESCRIPTION).toString()))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a topics overview.
     *
     * @return a topics overview
     */
    private String topicsOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_topics")));

        sb.append(tableStart());

        sb.append(columnHeaders(VisualVMModel.DataType.TOPICS));

        for (Map.Entry<Object, Data> entry : m_topicData)
            {
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(TopicData.TOPIC_NAME).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.CHANNELS).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.PUBLISHED_TOTAL).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.PAGE_CAPACITY).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.RECONNECT_RETRY).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.RECONNECT_TIMEOUT).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.RECONNECT_WAIT).toString()))
                    .append(td(entry.getValue().getColumn(TopicData.RETAIN_CONSUMED).toString()))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a gRPC overview.
     *
     * @return a gRPC overview
     */
    private String grpcOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_grpc")));

        long  nSentCount        = 0L;
        long  nRecCount         = 0L;

       for (Map.Entry<Object, Data> entry : m_grpcData)
            {
            nSentCount        += (Long) entry.getValue().getColumn(GrpcProxyData.RESPONSES_SENT_COUNT);
            nRecCount         += (Long) entry.getValue().getColumn(GrpcProxyData.MESSAGES_RECEIVED_COUNT);
            }

       sb.append(tableStart())
                .append(tableRow(getLabel("LBL_total_grpc_servers"), Integer.toString(m_grpcData.size())))
                .append(tableRow(getLabel("LBL_total_grpc_msg_rec"), getMemoryFormat(Long.toString(nRecCount))))
                .append(tableRow(getLabel("LBL_total_grpc_resp_sent"), getMemoryFormat(Long.toString(nSentCount))))
                .append(tableEnd());

        sb.append(tableStart());

        sb.append(columnHeaders(VisualVMModel.DataType.GRPC_PROXY));

        for (Map.Entry<Object, Data> entry : m_grpcData)
            {
            sb.append("<tr>")
                    .append(td(getMemoryFormat(entry.getValue().getColumn(GrpcProxyData.NODE_ID).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(GrpcProxyData.SUCCESSFUL_REQUEST_COUNT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(GrpcProxyData.ERROR_REQUEST_COUNT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(GrpcProxyData.RESPONSES_SENT_COUNT).toString())))
                    .append(td(getMemoryFormat(entry.getValue().getColumn(GrpcProxyData.MESSAGES_RECEIVED_COUNT).toString())))
                    .append(td(getLatencyValue(entry.getValue().getColumn(GrpcProxyData.REQUEST_DURATION_MEAN).toString())))
                    .append(td(getLatencyValue(entry.getValue().getColumn(GrpcProxyData.MESSAGE_DURATION_MEAN).toString())))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a federation overview.
     *
     * @return a federation overview
     */
    private String federationOverview()
        {
        StringBuilder sb = new StringBuilder(title(getLabel("LBL_federation")));

        sb.append(tableStart());

        // table headers
        sb.append(columnHeaders(VisualVMModel.DataType.FEDERATION_DESTINATION));

        for (Map.Entry<Object, Data> entry : m_federationData)
            {
            Long cBytesSent = (Long) entry.getValue().getColumn(FederationData.Column.TOTAL_BYTES_SENT.ordinal());
            Long cMsgsSent = (Long) entry.getValue().getColumn(FederationData.Column.TOTAL_MSGS_SENT.ordinal());
            Long cBytesRec = (Long) entry.getValue().getColumn(FederationData.Column.TOTAL_BYTES_RECEIVED.ordinal());
            Long cMsgsRec = (Long) entry.getValue().getColumn(FederationData.Column.TOTAL_MSGS_RECEIVED.ordinal());
            sb.append("<tr>")
                    .append(td(entry.getValue().getColumn(FederationData.Column.SERVICE.ordinal()).toString()))
                    .append(td(entry.getValue().getColumn(FederationData.Column.PARTICIPANT.ordinal()).toString()))
                    .append(td(entry.getValue().getColumn(FederationData.Column.STATUS.ordinal()).toString()))
                    .append(td(getMemoryFormat(cBytesSent == null
                                               ? "0"
                                               : cBytesSent.toString())))
                    .append(td(getMemoryFormat(cMsgsSent == null
                                               ? "0"
                                               : cMsgsSent.toString())))
                    .append(td(getMemoryFormat(cBytesRec == null
                                               ? "0"
                                               : cBytesRec.toString())))
                    .append(td(getMemoryFormat(cMsgsRec == null
                                               ? "0"
                                               : cMsgsRec.toString())))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Returns a elastic data overview.
     *
     * @param sType either "RAM" or "FLASH"
     * @return a elastic data overview
     */
    private String elasticDataOverview(String sType)
        {
        boolean fIsRamJournal = "RAM".equals(sType);
        StringBuilder sb = new StringBuilder(title(getLabel(fIsRamJournal
                                                            ? "LBL_ram_journal_detail"
                                                            : "LBL_flash_journal_detail")));

        sb.append(tableStart());

        // custom table headers
        sb.append("<tr>")
                .append(th(getLocalizedText("LBL_node_id")))
                .append(th(getLocalizedText("LBL_journal_files")))
                .append(th(getLocalizedText("LBL_total_data_size")))
                .append(th(getLocalizedText("LBL_committed")))
                .append(th(getLocalizedText("LBL_compactions")))
                .append(th(getLocalizedText("LBL_current_collector_load_factor")))
                .append(th(getLocalizedText("LBL_max_file_size")))
                .append("</tr>");

        List<Map.Entry<Object, Data>> tableData = f_model.getData(fIsRamJournal
                                                                  ? VisualVMModel.DataType.RAMJOURNAL
                                                                  : VisualVMModel.DataType.FLASHJOURNAL);


        for (Map.Entry<Object, Data> entry : tableData)
            {
            Data data = entry.getValue();
            String sJournalFiles = data.getColumn(RamJournalData.FILE_COUNT).toString() + " / "
                                   + data.getColumn(RamJournalData.MAX_FILES).toString();
            String sCommitted =
                    getRenderedBytes((Long) data.getColumn(RamJournalData.TOTAL_COMMITTED_BYTES)) + " / "
                    + getRenderedBytes((Long) data.getColumn(RamJournalData.MAX_COMMITTED_BYTES));
            String sCompactions = getNullEntry(data.getColumn(RamJournalData.COMPACTION_COUNT)) + " / " +
                                  getNullEntry(data.getColumn(RamJournalData.EXHAUSTIVE_COMPACTION_COUNT));
            sb.append("<tr>")
                    .append(td(getMemoryFormat(entry.getValue().getColumn(RamJournalData.NODE_ID).toString())))
                    .append(td(sJournalFiles))
                    .append(td(getRenderedBytes((Long) entry.getValue().getColumn(RamJournalData.TOTAL_DATA_SIZE))))
                    .append(td(sCommitted))
                    .append(td(sCompactions))
                    .append(td(getPercentFormat(entry.getValue().getColumn(RamJournalData.CURRENT_COLLECTION_LOAD_FACTOR).toString())))
                    .append(td(getRenderedBytes((Long) entry.getValue().getColumn(RamJournalData.MAX_FILE_SIZE))))
                    .append("</tr>");
            }

        return sb.append(tableEnd()).toString();
        }

    /**
     * Render the column headers for the {@link VisualVMModel.DataType}.
     *
     * @param dataType the {@link VisualVMModel.DataType}
     * @return table header row
     */
    private String columnHeaders(VisualVMModel.DataType dataType)
        {
        StringBuilder sb = new StringBuilder("<tr>");
        String[] asColumns = dataType.getMetadata();

        for (int i = 0; i < asColumns.length; i++)
            {
            sb.append(th(asColumns[i]));
            }

        return sb.append("</tr>").toString();
        }

    /**
     * Returns text for a label.
     * @param sKey the key for Bundle
     * @return text for a label
     */
    private String getLabel(String sKey)
        {
        return Localization.getLocalText(sKey);
        }

    /**
     * Created a HTML title.
     *
     * @param sTitle title
     * @return a HTML title
     */
    private String title(String sTitle)
        {
        return "<h2>" + sTitle + "</h2>";
        }

    /**
     * Creates a HTML table row.
     *
     * @param sLabel label
     * @param sValue the tex
     * @return a HTML table row
     */
    private String tableRow(String sLabel, String sValue)
        {
        return "<tr>" + td(label(sLabel)) + td(sValue) + "</tr>";
        }

    /**
     * Creates a label.
     *
     * @param sLabel label
     * @return a label
     */
    private String label(String sLabel)
        {
        return "<b>" + sLabel + ":" + "</b";
        }

    /**
     * Creates a HTML TD.
     *
     * @param sValue value for the TD text
     * @return a HTML TD
     */
    private String td(String sValue)
        {
        return "<td>" + sValue + "</td>";
        }

    /**
     * Creates a HTML TH.
     *
     * @param sValue value for the TH text
     * @return a HTML TH
     */
    private String th(String sValue)
        {
        return "<th>" + sValue + "</th>";
        }

    /**
     * Creates a HTML table start.
     *
     * @return a HTML table start
     */
    private String tableStart()
        {
        return "<table>";
        }

    /**
     * Creates a HTML table end.
     *
     * @return a HTML table end
     */
    private String tableEnd()
        {
        return "</table>";
        }

    @Override
    public void updateData()
        {
        m_memberData = f_model.getData(VisualVMModel.DataType.MEMBER);
        m_clusterData = f_model.getData(VisualVMModel.DataType.CLUSTER);
        m_serviceData = f_model.getData(VisualVMModel.DataType.SERVICE);
        m_machineData = f_model.getData(VisualVMModel.DataType.MACHINE);
        m_cacheData = f_model.getData(VisualVMModel.DataType.CACHE);
        m_cacheDetailData = f_model.getData(VisualVMModel.DataType.CACHE_DETAIL);
        m_cacheFrontDetailData = f_model.getData(VisualVMModel.DataType.CACHE_FRONT_DETAIL);
        m_cacheStorageData = f_model.getData(VisualVMModel.DataType.CACHE_STORAGE_MANAGER);
        m_proxyData = f_model.getData(VisualVMModel.DataType.PROXY);
        m_ramJournalData = f_model.getData(VisualVMModel.DataType.RAMJOURNAL);
        m_flashJournalData = f_model.getData(VisualVMModel.DataType.FLASHJOURNAL);
        m_federationData = getMergedFederationData();

        // check if there is a row selected in the tableFed
        if (f_model.getSelectedServiceParticipant() != null)
            {
            // get outbound details data
            m_fedDestinationDetailsData = f_model.getData(VisualVMModel.DataType.FEDERATION_DESTINATION_DETAILS);

            // get inbound details data
            m_fedOriginDetailData = f_model.getData(VisualVMModel.DataType.FEDERATION_ORIGIN_DETAILS);
            }

        m_hotcacheData         = f_model.getData(VisualVMModel.DataType.HOTCACHE);
        m_hotcachepercacheData = f_model.getData(VisualVMModel.DataType.HOTCACHE_PERCACHE);
        m_httpProxyData        = f_model.getData(VisualVMModel.DataType.HTTP_PROXY);
        m_httpProxyMemberData  = f_model.getData(VisualVMModel.DataType.HTTP_PROXY_DETAIL);
        m_httpSessionData      = f_model.getData(VisualVMModel.DataType.HTTP_SESSION);
        m_configData           = f_model.getData(VisualVMModel.DataType.JCACHE_CONFIG);
        m_statsData            = f_model.getData(VisualVMModel.DataType.JCACHE_STATS);
        m_persistenceData      = f_model.getData(VisualVMModel.DataType.PERSISTENCE);
        m_topicData            = f_model.getData(VisualVMModel.DataType.TOPICS);
        m_executorData         = f_model.getData(VisualVMModel.DataType.EXECUTOR);
        m_grpcData             = f_model.getData(VisualVMModel.DataType.GRPC_PROXY);
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -761252043492412546L;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(CoherenceClusterSnapshotPanel.class.getName());

    private static final String HR = "<hr>";

    // ----- data members ---------------------------------------------------

    /**
     * HTML Text area.
     */
    private final JEditorPane f_htmlTextArea;

    /**
     * The member statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_memberData;

    /**
     * The cluster statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_clusterData;

    /**
     * The service statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_serviceData;

    /**
     * The machine statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_machineData;

    /**
     * The cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_cacheData;

    /**
     * The detailed cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_cacheDetailData;

    /**
     * The detailed front cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_cacheFrontDetailData;

    /**
     * The storage cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_cacheStorageData;

    /**
     * The proxy statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_proxyData;

    /**
     * The ramjournal data retrieved from the {@link VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_ramJournalData;

    /**
     * The flashjournal data retrieved from the {@link VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_flashJournalData;

    /**
     * The merged federation data from the destination data and origin data.
     */
    private List<Map.Entry<Object, Data>> m_federationData;

    /**
     * The destination detail data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_fedDestinationDetailsData;

    /**
     * The origin detail data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_fedOriginDetailData;

    /**
     * The statistics hotcache data retrieved from the {@link VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_hotcacheData;

    /**
     * The statistics hotcachepercache data retrieved from the {@link
     * VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_hotcachepercacheData;

    /**
     * The http proxy statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpProxyData;

    /**
     * The proxy member statistics data retrieved from the {@link
     * VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpProxyMemberData;

    /**
     * The executor statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_executorData;

    /**
     * The gRPC statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_grpcData;

    /**
     * The machine statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpSessionData;

    /**
     * The JCache configuration data.
     */
    private List<Map.Entry<Object, Data>> m_configData;

    /**
     * The JCache statistics data.
     */
    private List<Map.Entry<Object, Data>> m_statsData;

    /**
     * The persistence data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_persistenceData;

    /**
     * The topic data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_topicData;

    /**
     * Last value.
     */
    private String m_lastValue;
    }
