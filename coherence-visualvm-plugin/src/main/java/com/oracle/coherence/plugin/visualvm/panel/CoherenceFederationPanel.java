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


package com.oracle.coherence.plugin.visualvm.panel;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.DialogHelper;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.SeparatorMenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.FederationInboundTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.FederationOutboundTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.FederationTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationDestinationDetailsData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationOriginDetailsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized federatrion service statistics.
 *
 * @author cl  2014.02.17
 * @since  12.2.1
 */
public class CoherenceFederationPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceFederationPanel}.
     *
     * @param model the {@link VisualVMModel} to use for this panel
     */
    public CoherenceFederationPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplitFed = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplitFed.setOpaque(false);

        // create a tab pane for inbound and outbound tabs
        JTabbedPane pneTabDetail = new JTabbedPane();
        pneTabDetail.setOpaque(false);

        // create two split panes for the inbound and outbound tabs
        JSplitPane pneSplitInbound  = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane pneSplitOutbound = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplitInbound.setOpaque(false);
        pneSplitOutbound.setOpaque(false);

        // create two split panes for details inside inbound and outbound panes
        JSplitPane pneSplitInboundDetail  = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane pneSplitOutboundDetail = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitInboundDetail.setOpaque(false);
        pneSplitOutboundDetail.setOpaque(false);

        // create table models for the inbound and outbound details
        m_tmodelFed      = new FederationTableModel(VisualVMModel.DataType.FEDERATION_DESTINATION.getMetadata());
        m_tmodelInbound  = new FederationInboundTableModel(VisualVMModel.DataType.FEDERATION_ORIGIN_DETAILS.getMetadata());
        m_tmodelOutbound = new FederationOutboundTableModel(VisualVMModel.DataType.FEDERATION_DESTINATION_DETAILS.getMetadata());

        // create exportable JTables for each table models
        final ExportableJTable tableFed      = new ExportableJTable(m_tmodelFed, model);
        final ExportableJTable tableInbound  = new ExportableJTable(m_tmodelInbound, model);
        final ExportableJTable tableOutbound = new ExportableJTable(m_tmodelOutbound, model);

        // create the scroll pane and add the table to it.
        JScrollPane pneScrollFed      = new JScrollPane(tableFed);
        JScrollPane pneScrollInbound  = new JScrollPane(tableInbound);
        JScrollPane pneScrollOutbound = new JScrollPane(tableOutbound);
        configureScrollPane(pneScrollFed, tableFed);
        configureScrollPane(pneScrollInbound, tableInbound);
        configureScrollPane(pneScrollOutbound, tableOutbound);

        // configure menu options
        Set<MenuOption> setMenuOptions = new LinkedHashSet<>();
        setMenuOptions.add(new StartMenuOption(model, m_requestSender, tableFed, StartMenuOption.START));

        if (model.getClusterVersionAsInt() >= 122103)
            {
            // add startwithSync & StartWIthNoBacklog available in patchset 12.2.1.0.3 and above
            setMenuOptions.add(new StartMenuOption(model, m_requestSender, tableFed, StartMenuOption.START_WITH_SYNC));
            setMenuOptions.add(new StartMenuOption(model, m_requestSender, tableFed, StartMenuOption.START_WITH_NO_BACKLOG));
            }

        setMenuOptions.add(new StopMenuOption(model, m_requestSender, tableFed, "LBL_stop_menu", "stop"));
        setMenuOptions.add(new PauseMenuOption(model, m_requestSender, tableFed, "LBL_pause_menu", "pause"));
        setMenuOptions.add(new SeparatorMenuOption(model, m_requestSender, tableFed));
        setMenuOptions.add(new ReplicateAllMenuOption(model, m_requestSender, tableFed, "LBL_replicate_all_menu", "replicateAll"));
        setMenuOptions.add(new RetrievePendingMessagesMenuOption(model, m_requestSender, tableFed, INCOMING_LABEL));
        setMenuOptions.add(new RetrievePendingMessagesMenuOption(model, m_requestSender, tableFed, OUTGOING_LABEL));

        // add right-click menu options
        tableFed.setMenuOptions(setMenuOptions.toArray(new MenuOption[setMenuOptions.size()]));

        // create a detail pane
        JPanel pneFedDetail = new JPanel(new BorderLayout());
        pneFedDetail.setOpaque(false);

        // create containers for inbound and outbounf details
        JPanel pneInboundDetail = new JPanel(new BorderLayout());
        JPanel pneOutboundDetail = new JPanel(new BorderLayout());
        pneInboundDetail.setOpaque(false);
        pneOutboundDetail.setOpaque(false);

        // create a panel to hold the textfields for outbound
        JPanel txtPanelOutboundDetailTotal = new JPanel();
        txtPanelOutboundDetailTotal.setBackground(Color.white);

        // create several textfields for the detail stats for outbound
        m_txtOutboundMaxBandwidth = getTextField(7, JTextField.LEFT);
        m_txtOutboundSendTimeOut  = getTextField(7, JTextField.LEFT);
        m_txtOutboundGeoIp        = getTextField(7, JTextField.LEFT);
        m_txtOutboundErrorDesp    = getTextField(7, JTextField.LEFT);

        // create labels for the textfields
        JLabel labelOutboundMaxBandwidth = getLocalizedLabel("LBL_max_bandwidth", m_txtOutboundMaxBandwidth);
        JLabel labelOutboundSendTimeOut  = getLocalizedLabel("LBL_send_time_out", m_txtOutboundSendTimeOut);
        JLabel labelOutboundGeoIp        = getLocalizedLabel("LBL_geo_ip", m_txtOutboundGeoIp);
        JLabel labelOutboundErrorDesp    = getLocalizedLabel("LBL_error_description", m_txtOutboundErrorDesp);

        // set group layout for four textfields in outbound tab
        GroupLayout layout = new GroupLayout(txtPanelOutboundDetailTotal);
        txtPanelOutboundDetailTotal.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup().
            addComponent(labelOutboundMaxBandwidth).
            addComponent(labelOutboundSendTimeOut).
            addComponent(labelOutboundGeoIp).
            addComponent(labelOutboundErrorDesp)
            );

        hGroup.addGroup(layout.createParallelGroup().
            addComponent(m_txtOutboundMaxBandwidth).
            addComponent(m_txtOutboundSendTimeOut).
            addComponent(m_txtOutboundGeoIp).
            addComponent(m_txtOutboundErrorDesp)
            );

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
            addComponent(labelOutboundMaxBandwidth).
            addComponent(m_txtOutboundMaxBandwidth)
            );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
            addComponent(labelOutboundSendTimeOut).
            addComponent(m_txtOutboundSendTimeOut)
            );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
            addComponent(labelOutboundGeoIp).
            addComponent(m_txtOutboundGeoIp)
            );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
            addComponent(labelOutboundErrorDesp).
            addComponent(m_txtOutboundErrorDesp)
            );

        layout.setVerticalGroup(vGroup);

        // create panes for the outbound and inbound graphs
        final JTabbedPane pneTabInboundGraph = new JTabbedPane();
        final JTabbedPane pneTabOutboundGraph = new JTabbedPane();
        pneTabInboundGraph.setOpaque(false);
        pneTabOutboundGraph.setOpaque(false);

        // create graphs in tabs
        populateOutboundTabs(pneTabOutboundGraph);
        populateInboundTabs(pneTabInboundGraph);
        pneTabOutboundGraph.setOpaque(false);
        pneTabInboundGraph.setOpaque(false);

        // reder
        RenderHelper.setColumnRenderer(tableFed, FederationData.Column.STATUS.ordinal() - 1, new RenderHelper.FedServiceStateRenderer());

        RenderHelper.setColumnRenderer(tableFed, 3, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(tableFed, 4, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(tableFed, 5, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(tableFed, 6, new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.STATE.ordinal(),
            new RenderHelper.FedNodeStateRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.CURRENT_BANDWIDTH.ordinal(),
            new RenderHelper.DecimalRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.TOTAL_BYTES_SENT.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.TOTAL_ENTRIES_SENT.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.TOTAL_RECORDS_SENT.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.TOTAL_MSG_SENT.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableOutbound, FederationDestinationDetailsData.Column.TOTAL_MSG_UNACKED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableInbound, FederationOriginDetailsData.Column.TOTAL_BYTES_RECEIVED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableInbound, FederationOriginDetailsData.Column.TOTAL_RECORDS_RECEIVED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableInbound, FederationOriginDetailsData.Column.TOTAL_ENTRIES_RECEIVED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableInbound, FederationOriginDetailsData.Column.TOTAL_MSG_RECEIVED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(tableInbound, FederationOriginDetailsData.Column.TOTAL_MSG_UNACKED.ordinal(),
            new RenderHelper.IntegerRenderer());

        RenderHelper.setHeaderAlignment(tableFed, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(tableInbound, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(tableOutbound, SwingConstants.CENTER);

        // set sizes
        tableFed.setPreferredScrollableViewportSize(new Dimension(500, tableFed.getRowHeight() * 4));
        tableInbound.setPreferredScrollableViewportSize(new Dimension(500, tableInbound.getRowHeight() * 5));
        tableOutbound.setPreferredScrollableViewportSize(new Dimension(500, tableOutbound.getRowHeight() * 5));

        setTablePadding(tableFed);
        setTablePadding(tableInbound);
        setTablePadding(tableOutbound);

        // adding and nesting
        pneSplitOutboundDetail.add(txtPanelOutboundDetailTotal);

        pneSplitInboundDetail.add(pneTabInboundGraph);
        pneSplitOutboundDetail.add(pneTabOutboundGraph);

        pneInboundDetail.add(pneSplitInboundDetail);
        pneOutboundDetail.add(pneSplitOutboundDetail);

        pneSplitInbound.add(pneScrollInbound);
        pneSplitOutbound.add(pneScrollOutbound);

        pneSplitInbound.add(pneInboundDetail);
        pneSplitOutbound.add(pneOutboundDetail);

        pneTabDetail.addTab(getLocalizedText("TAB_outbound"), pneSplitOutbound);
        pneTabDetail.addTab(getLocalizedText("TAB_inbound"), pneSplitInbound);

        pneFedDetail.add(pneTabDetail, BorderLayout.CENTER);

        pneScrollFed.setOpaque(false);
        pneSplitFed.setOpaque(false);
        pneSplitFed.add(pneScrollFed);
        pneSplitFed.add(pneFedDetail);

        add(pneSplitFed);

        // add listener actions
        m_rowSelectModelFed = tableFed.getSelectionModel();
        m_rowSelectModelOutboundDetails = tableOutbound.getSelectionModel();
        m_rowSelectModelInboundDetails = tableInbound.getSelectionModel();

        m_listener = new SelectRowListSelectionListener(tableFed, tableOutbound, tableInbound, pneTabOutboundGraph, pneTabInboundGraph);

        m_rowSelectModelFed.addListSelectionListener(m_listener);
        m_rowSelectModelOutboundDetails.addListSelectionListener(m_listener);
        m_rowSelectModelInboundDetails.addListSelectionListener(m_listener);
        }

    /**
     * Populate the graphs in the outbound detail tabs.
     *
     * @param pneDetailTabs the {@link JTabbedPane} to update
     */
    private void populateOutboundTabs(JTabbedPane pneDetailTabs)
        {
        // remove any existing tabs
        int cTabs = pneDetailTabs.getTabCount();

        for (int i = 0; i < cTabs; i++)
            {
            pneDetailTabs.removeTabAt(0);
            }

        m_bandwidthUtilGraph = GraphHelper.createBandwidthUtilGraph();
        pneDetailTabs.addTab(getLocalizedText("LBL_bandwidth_utilization"), m_bandwidthUtilGraph.getChart());

        m_recordBacklogDelayGraph = GraphHelper.createOutboundPercentileGraph();
        pneDetailTabs.addTab(getLocalizedText("LBL_replication_percentile_millis"), m_recordBacklogDelayGraph.getChart());
        }

    /**
     * Populate the graphs in the inbound detail tabs.
     *
     * @param pneDetailTabs the {@link JTabbedPane} to update
     */
    private void populateInboundTabs(JTabbedPane pneDetailTabs)
        {
        // remove any existing tabs
        int cTabs = pneDetailTabs.getTabCount();

        for (int i = 0; i < cTabs; i++)
            {
            pneDetailTabs.removeTabAt(0);
            }

        m_graphInboundPercentile = GraphHelper.createInboundPercentileGraph();
        pneDetailTabs.addTab(getLocalizedText("LBL_replication_percentile_millis"), m_graphInboundPercentile.getChart());
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        // update the data display in each table
        m_tmodelFed.fireTableDataChanged();
        m_tmodelOutbound.fireTableDataChanged();
        m_tmodelInbound.fireTableDataChanged();

        // re-select the selected rows
        if (f_model.getSelectedServiceParticipant() != null)
            {
            m_listener.updateRowSelections();
            }
        }

    @Override
    public void updateData()
        {
        // get the merged data for tableFed
        m_federationData = getMergedFederationData();

        if (m_federationData != null)
            {
            // update data for tableFEd
            m_tmodelFed.setDataList(m_federationData);
            m_tmodelFed.fireTableDataChanged();
            }

        // check if there is a row selected in the tableFed
        if (f_model.getSelectedServiceParticipant() != null)
            {
            // get outbound details data
            m_fedDestinationDetailsData = f_model.getData(VisualVMModel.DataType.FEDERATION_DESTINATION_DETAILS);

            // get inbound details data
            m_fedOriginDetailData = f_model.getData(VisualVMModel.DataType.FEDERATION_ORIGIN_DETAILS);

            // update outbound details data
            m_tmodelOutbound.setDataList(m_fedDestinationDetailsData);
            m_tmodelOutbound.fireTableDataChanged();

            // update inbound details data
            m_tmodelInbound.setDataList(m_fedOriginDetailData);
            m_tmodelInbound.fireTableDataChanged();

            // update the outbound tab
            String sSelectedNode = f_model.getSelectedNodeOutbound();
            if(sSelectedNode != null)
                {
                if (m_fedDestinationDetailsData != null)
                    {
                    for (Entry<Object, Data> entry : m_fedDestinationDetailsData)
                        {
                        String sNodeId = (String) entry.getValue().getColumn(FederationDestinationDetailsData.Column.NODE_ID.ordinal());
                        if (sNodeId.equals(sSelectedNode))
                            {
                            Long backlog           = (Long) entry.getValue().getColumn(FederationDestinationDetailsData.Column.RECORD_BACKLOG_DELAY_TIME_PERCENTILE_MILLIS.ordinal());
                            Long network           = (Long) entry.getValue().getColumn(FederationDestinationDetailsData.Column.MSG_NETWORK_ROUND_TRIP_TIME_PERCENTILE_MILLIS.ordinal());
                            Long apply             = (Long) entry.getValue().getColumn(FederationDestinationDetailsData.Column.MSG_APPLY_TIME_PERCENTILE_MILLIS.ordinal());
                            Float currentBandwidth = (Float) entry.getValue().getColumn(FederationDestinationDetailsData.Column.CURRENT_BANDWIDTH.ordinal());
                            String maxBandwidth    = (String) entry.getValue().getColumn(FederationDestinationDetailsData.Column.MAX_BANDWIDTH.ordinal());
                            String sErrorDesp      = (String) entry.getValue().getColumn(FederationDestinationDetailsData.Column.ERROR_DESCRIPTION.ordinal());

                            // add new values  to graphs
                            GraphHelper.addValuesToOutboundPercentileDelayGraph(m_recordBacklogDelayGraph, backlog, network, apply);
                            GraphHelper.addValuesToBandwidthUtilGraph(m_bandwidthUtilGraph, Float.valueOf(maxBandwidth), currentBandwidth);

                            // update the error description in textfield
                            m_txtOutboundErrorDesp.setText(sErrorDesp);
                            m_txtOutboundErrorDesp.setToolTipText(sErrorDesp);
                            }
                        }
                    }
                }

            // update the inbound tab
            sSelectedNode = f_model.getSelectedNodeInbound();
            if(sSelectedNode != null)
                {
                if (m_fedOriginDetailData != null)
                    {
                    for (Entry<Object, Data> entry : m_fedOriginDetailData)
                        {
                        String sNodeId = (String) entry.getValue().getColumn(FederationOriginDetailsData.Column.NODE_ID.ordinal());
                        if (sNodeId.equals(sSelectedNode))
                            {
                            Long apply   = (Long) entry.getValue().getColumn(FederationOriginDetailsData.Column.MSG_APPLY_TIME_PERCENTILE_MILLIS.ordinal());
                            Long backlog = (Long) entry.getValue().getColumn(FederationOriginDetailsData.Column.RECORD_BACKLOG_DELAY_TIME_PERCENTILE_MILLIS.ordinal());

                            // add the new value to the graph
                            GraphHelper.addValuesToInboundPercentileGraph(m_graphInboundPercentile, backlog, apply);
                            }
                        }
                    }
                }
            }
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Set the text content for several textfields.
     *
     * @param sMaxBandwidth  the maxbandwidth
     * @param sSendTimeout   the send time out
     * @param sGeoIp         the Geo IP
     * @param sErrorDesp     the error description
     */
    private void setTextDetailsValue(String sMaxBandwidth, String sSendTimeout,
                                        String sGeoIp, String sErrorDesp)
        {
        m_txtOutboundMaxBandwidth.setText("0.0".equals(sMaxBandwidth) ? "Not Set" : sMaxBandwidth);
        m_txtOutboundSendTimeOut.setText(sSendTimeout);
        m_txtOutboundGeoIp.setText(sGeoIp);
        m_txtOutboundErrorDesp.setText(sErrorDesp);
        m_txtOutboundErrorDesp.setToolTipText(sErrorDesp);
        }

    // ----- inner classes --------------------------------------------------

    /**
     * MenuOption for start jmx operation in FederationManager.
     */
    private class StartMenuOption
        extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public StartMenuOption(VisualVMModel model, RequestSender requestSender,
            ExportableJTable jtable, int nStartType)
            {
            super(model, requestSender, jtable);
            f_nStartType = nStartType;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return getLocalizedText(f_nStartType  == START           ? "LBL_start_menu" :
                                    (f_nStartType == START_WITH_SYNC ? "LBL_start_menu_with_sync" :
                                                                       "LBL_start_menu_with_no_backlog"));
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            String sService = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText(MUST_SELECT_ROW));
                }
            else
                {
                try
                    {
                    sService            = (String) getJTable().getModel().getValueAt(nRow, 0);
                    String sParticipant = (String) getJTable().getModel().getValueAt(nRow, 1);

                    String sOperation   =  f_nStartType == START ? "start" :
                                          (f_nStartType == START_WITH_SYNC ? "startWithSync" : "startWithNoBacklog");

                    if (confirmOperation(sOperation.toUpperCase(), sParticipant))
                        {
                        m_requestSender.invokeFederationOperation(sService, sOperation, sParticipant);
                        showMessageDialog(Localization.getLocalText("LBL_details_service", sService),
                                          Localization.getLocalText("LBL_operation_result_done", sOperation.toUpperCase(), sParticipant),
                                          JOptionPane.INFORMATION_MESSAGE, 400, 50);
                        }
                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("ERR_cannot_run", sService),
                                      ee.getMessage() + "\n" + ee.getCause(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        // ---- constants ---------------------------------------------------

        /**
         * Indicates normal start.
         */
        public static final int START = 0;

        /**
         * Indicates start with sync.
         */
        public static final int START_WITH_SYNC = 1;

        /**
         * Indicates start with no backlog.
         */
        public static final int START_WITH_NO_BACKLOG = 2;

        // ---- data members ------------------------------------------------

        /**
         * Type of start operation. 0 = normal start, 1 = start with SYNC, 2 = start  with no backlog
         */
        private final int f_nStartType;
        }

    /**
     * MenuOption for various generic options.
     */
    private class GenericOperationMenuOption
        extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public GenericOperationMenuOption(VisualVMModel model, RequestSender requestSender,
            ExportableJTable jtable, String sLabel, String sOperation)
            {
            super(model, requestSender, jtable);

            f_sOperation = sOperation;
            f_sLabel     = sLabel;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return getLocalizedText(f_sLabel);
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            String sService = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText(MUST_SELECT_ROW));
                }
            else
                {
                try
                    {
                    sService            = (String) getJTable().getModel().getValueAt(nRow, 0);
                    String sParticipant = (String) getJTable().getModel().getValueAt(nRow, 1);

                    if (confirmOperation(f_sOperation, sParticipant))
                        {
                        m_requestSender.invokeFederationOperation(sService, f_sOperation, sParticipant);

                        showMessageDialog(Localization.getLocalText("LBL_details_service", sService),
                                          Localization.getLocalText("LBL_operation_result_done", f_sOperation, sParticipant),
                                          JOptionPane.INFORMATION_MESSAGE, 400, 50);
                        }
                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("ERR_cannot_run", sService),
                                      ee.getMessage() + "\n" + ee.getCause(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            private final String f_sOperation;
            private final String f_sLabel;
        }

    public class StopMenuOption
            extends GenericOperationMenuOption
        {
        // ----- constructors -----------------------------------------------

        public StopMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable, String sLabel, String sOperation)
            {
            super(model, requestSender, jtable, sLabel, sOperation);
            }
        }

    public class PauseMenuOption
            extends GenericOperationMenuOption
        {
        // ----- constructors -----------------------------------------------

        public PauseMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable, String sLabel, String sOperation)
            {
            super(model, requestSender, jtable, sLabel, sOperation);
            }
        }

    public class ReplicateAllMenuOption
            extends GenericOperationMenuOption
        {
        // ----- constructors -----------------------------------------------

        public ReplicateAllMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable, String sLabel, String sOperation)
            {
            super(model, requestSender, jtable, sLabel, sOperation);
            }
        }

    /**
     * MenuOption for retrievePending*Messages jmx operation in FederationManager.
     */
    private class RetrievePendingMessagesMenuOption
        extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public RetrievePendingMessagesMenuOption(VisualVMModel model, RequestSender requestSender,
            ExportableJTable jtable, String sLabel)
            {
            super(model, requestSender, jtable);

            f_sLabel  = sLabel;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return getLocalizedText("LBL_retrieve_incoming_menu");
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            String sService = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText(MUST_SELECT_ROW));
                }
            else
                {
                try
                    {
                    sService  = (String) getJTable().getModel().getValueAt(nRow, 0);
                    boolean fIncoming = f_sLabel.equals(INCOMING_LABEL);
                    String  sMessage = fIncoming ? "incoming" : "outgoing";

                    if (DialogHelper.showConfirmDialog(Localization.getLocalText(f_sLabel, sService) + "?"))
                        {
                        Integer cResult;

                        if (fIncoming)
                           {
                           cResult =  m_requestSender.retrievePendingIncomingMessages(sService);
                           }
                        else
                           {
                           cResult =  m_requestSender.retrievePendingOutgoingMessages(sService);
                           }

                        showMessageDialog(Localization.getLocalText("LBL_details_service", sService),
                                          Localization.getLocalText("LBL_result_is", cResult.toString()),
                                          JOptionPane.INFORMATION_MESSAGE, 400, 50);
                        }
                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("ERR_cannot_run", sService),
                                      ee.getMessage() + "\n" + ee.getCause(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            private final String f_sLabel;
        }

    /**
     * Inner class to monitor and change all data displays when row selection events
     * happen in this tab.
     */
    private class SelectRowListSelectionListener
            implements ListSelectionListener
        {

        // ----- constructors -----------------------------------------------

        /**
         * Create the listener to monitor row selection events.
         *
         * @param  tableFed            the {@link ExportableJTable} that is to be selected
         * @param  tableOutbound       the {@link ExportableJTable} that is to be selected
         * @param  tableInbound        the {@link ExportableJTable} that is to be selected
         * @param  pneTabOutboundGraph the {@link JTabbedPane} to update
         * @param  pneTabInboundGraph  the {@link JTabbedPane} to update
         */
        public SelectRowListSelectionListener(ExportableJTable tableFed, ExportableJTable tableOutbound,
                                              ExportableJTable tableInbound, JTabbedPane pneTabOutboundGraph,
                                              JTabbedPane pneTabInboundGraph)
            {
            m_tableFed            = tableFed;
            m_tableOutbound       = tableOutbound;
            m_tableInbound        = tableInbound;
            m_pneTabOutboundGraph = pneTabOutboundGraph;
            m_pneTabInboundGraph  = pneTabInboundGraph;
            }

        // ----- ListSelectionListener methods ------------------------------

        /**
         * React when a row is selected.
         *
         * @param e {@link ListSelectionEvent} to respond to
         */
        public void valueChanged(ListSelectionEvent e)
            {
            if (e.getValueIsAdjusting())
                {
                return;
                }

            ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();

            if (selectionModel.isSelectionEmpty())
                {
                return;
                }

            // select the destination detail (outbound) table
            if (selectionModel == m_rowSelectModelOutboundDetails && m_fedDestinationDetailsData != null)
                {
                // will update the selected row
                m_updateRowOutbound = true;

                // get the selected row index
                m_nSelectedRowOutbound = selectionModel.getMinSelectionIndex();

                Map.Entry<Object, Data> entry = m_fedDestinationDetailsData.get(m_nSelectedRowOutbound);
                Data data = entry.getValue();
                String sSelectedNode = (String) data.getColumn(FederationDestinationDetailsData.Column.NODE_ID.ordinal());

                // the selected node has changed
                if (!sSelectedNode.equals(f_model.getSelectedNodeOutbound()))
                    {
                    // update the selected node ID in VisualVMModel
                    f_model.setSelectedNodeOutbound(sSelectedNode);

                    String sMaxBandwidth = (String) data.getColumn(FederationDestinationDetailsData.Column.MAX_BANDWIDTH.ordinal());
                    String sSendTimeout  = (String) data.getColumn(FederationDestinationDetailsData.Column.SEND_TIMEOUT_MILLIS.ordinal());
                    String sGeoIp        = (String) data.getColumn(FederationDestinationDetailsData.Column.GEO_IP.ordinal());
                    String sErrorDesp    = (String) data.getColumn(FederationDestinationDetailsData.Column.ERROR_DESCRIPTION.ordinal());

                    // set textfields content
                    setTextDetailsValue(sMaxBandwidth, sSendTimeout, sGeoIp, sErrorDesp);

                    // update graphs
                    populateOutboundTabs(m_pneTabOutboundGraph);

                    // force immediate refresh
                    f_model.setImmediateRefresh(true);
                    }

                return;
                }
            // select the origin detail (inbound) table
            else if (selectionModel == m_rowSelectModelInboundDetails && m_fedOriginDetailData != null)
                {
                //will update the selected row
                m_updateRowInbound = true;

                // get the selected row index
                m_nSelectedRowInbound = selectionModel.getMinSelectionIndex();

                Map.Entry<Object, Data> entry = m_fedOriginDetailData.get(m_nSelectedRowInbound);
                String sSelectedNode = (String) entry.getValue().getColumn(FederationOriginDetailsData.Column.NODE_ID.ordinal());

                // the selected node has changed
                if (!sSelectedNode.equals(f_model.getSelectedNodeInbound()))
                    {
                    // update the selected node ID in VisualVMModel
                    f_model.setSelectedNodeInbound(sSelectedNode);

                    // update graphs
                    populateInboundTabs(m_pneTabInboundGraph);
                    }

                return;
                }
            // select the federation table
            else if (selectionModel == m_rowSelectModelFed)
                {
                // will update the selected row
                m_updateRowFed = true;

                // get the selected row index
                m_nSelectedRowFed = selectionModel.getMinSelectionIndex();

                // get the service at the selected row, which is the first column
                String sSelectedService = (String) m_tableFed.getValueAt(m_nSelectedRowFed, 0);
                String sParticipant     = (String) m_tableFed.getValueAt(m_nSelectedRowFed, 1);

                Pair<String, String> serviceParticipant = new Pair(sSelectedService, sParticipant);

                // selected service/participant has changed
                if (!serviceParticipant.equals(f_model.getSelectedServiceParticipant()))
                    {
                    // update the selected service / participant pair in VisualVMModel
                    f_model.setSelectedServiceParticipant(serviceParticipant);

                    // get rid of old details data for inbound and outbound
                    f_model.eraseFederationDetailsData();

                    // update details data display
                    m_tmodelOutbound.setDataList(null);
                    m_tmodelOutbound.fireTableDataChanged();
                    m_tmodelInbound.setDataList(null);
                    m_tmodelInbound.fireTableDataChanged();

                    // update textfields
                    setTextDetailsValue("", "", "", "");

                    // update graphs
                    populateOutboundTabs(m_pneTabOutboundGraph);
                    populateInboundTabs(m_pneTabInboundGraph);

                    // force immediate refresh
                    f_model.setImmediateRefresh(true);
                    }
                }
            }

        /**
         * Re-select the last selected rows.
         */
        public void updateRowSelections()
            {
            if (m_updateRowFed)
                {
                m_tableFed.addRowSelectionInterval(m_nSelectedRowFed, m_nSelectedRowFed);
                }
            if (m_updateRowOutbound)
                {
                m_tableOutbound.addRowSelectionInterval(m_nSelectedRowOutbound, m_nSelectedRowOutbound);
                }
            if (m_updateRowInbound)
                {
                m_tableInbound.addRowSelectionInterval(m_nSelectedRowInbound, m_nSelectedRowInbound);
                }
            }

        /**
         * The {@link ExportableJTable} that is to be selected.
         */
        private ExportableJTable m_tableFed;

        /**
         * The {@link ExportableJTable} that is to be selected.
         */
        private ExportableJTable m_tableOutbound;

        /**
         * The {@link ExportableJTable} that is to be selected.
         */
        private ExportableJTable m_tableInbound;

        /**
         * The {@link JTabbedPane} to update.
         */
        private JTabbedPane m_pneTabOutboundGraph;

        /**
         * The {@link JTabbedPane} to update.
         */
        private JTabbedPane m_pneTabInboundGraph;

        /**
         * Whether the row in tableFed is selected.
         */
        private boolean m_updateRowFed = false;

        /**
         * Whether the row in tableOutbound is selected.
         */
        private boolean m_updateRowOutbound = false;

        /**
         * Whether the row in tableInbound is selected.
         */
        private boolean m_updateRowInbound = false;

        /**
         * The selected row index in tableFed.
         */
        private int m_nSelectedRowFed;

        /**
         * The selected row index in tableOutbound.
         */
        private int m_nSelectedRowOutbound;

        /**
         * The selected row index in tableInbound.
         */
        private int m_nSelectedRowInbound;
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Ask a question to confirm the operation against a participant.
     *
     * @param sOperation    operation to perform
     * @param sParticipant  the participant to execute against
     *
     * @return true if the user selected Yes
     */
    private boolean confirmOperation(String sOperation, String sParticipant)
        {
        return DialogHelper.showConfirmDialog(
                Localization.getLocalText("LBL_operation_result_menu", sOperation, sParticipant));
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 6174232358605085951L;

    private static final String INCOMING_LABEL = "LBL_retrieve_incoming_menu";
    private static final String OUTGOING_LABEL = "LBL_retrieve_outgoing_menu";
    private static final String MUST_SELECT_ROW = "LBL_must_select_row";

    // ----- data members ---------------------------------------------------

    /**
     * The textfield holds the max bandwidth data from destination.
     */
    private JTextField m_txtOutboundMaxBandwidth;

    /**
     * The textfield holds the send time out data from destination.
     */
    private JTextField m_txtOutboundSendTimeOut;

    /**
     * The textfield holds the Geo-Ip data from destination.
     */
    private JTextField m_txtOutboundGeoIp;

    /**
     * The textfield holds the error description data from destination.
     */
    private JTextField m_txtOutboundErrorDesp;

    /**
     * The {@link FederationTableModel} to display the merged destination and origin data.
     */
    private FederationTableModel m_tmodelFed;

    /**
     * The {@link FederationInboundTableModel} to display the inbound detail data.
     */
    private FederationInboundTableModel m_tmodelInbound;

    /**
     * The {@link FederationOutboundTableModel} to display the outbound detail data.
     */
    private FederationOutboundTableModel m_tmodelOutbound;

    /**
     * The merged federation data from the destination data and origin data.
     */
    private List<Entry<Object, Data>> m_federationData;

    /**
     * The destination detail data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_fedDestinationDetailsData;

    /**
     * The origin detail data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_fedOriginDetailData;

    /**
     * The graph of record backlog delay percentiles.
     */
    private SimpleXYChartSupport m_recordBacklogDelayGraph = null;

    /**
     * The graph of bandwidth utilization percentiles.
     */
    private SimpleXYChartSupport m_bandwidthUtilGraph = null;

    /**
     * The graph of inbound message apply time percentiles.
     */
    private SimpleXYChartSupport m_graphInboundPercentile = null;

    /**
     * The row selection listener.
     */
    private SelectRowListSelectionListener m_listener;

    /**
     * The {@link ListSelectionModel} of tableFed.
     */
    private ListSelectionModel m_rowSelectModelFed;

    /**
     * The {@link ListSelectionModel} of tableOutbound.
     */
    private ListSelectionModel m_rowSelectModelOutboundDetails;

    /**
     * The {@link ListSelectionModel} of tableInbound.
     */
    private ListSelectionModel m_rowSelectModelInboundDetails;
    }
