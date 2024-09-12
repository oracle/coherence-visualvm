/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.coherence.plugin.visualvm.tablemodel.MemberTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MemberData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.logging.Logger;
import com.oracle.coherence.plugin.visualvm.threaddump.ThreadDumpImpl;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import javax.swing.event.ChangeListener;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;
import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.ui.DataSourceWindowManager;
import org.openide.awt.StatusDisplayer;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;


/**
 * An implementation of an {@link AbstractCoherencePanel} to view summarized
 * member data.
 *
 * @author tam  2013.11.14
 * @since 12.1.3
 */
public class CoherenceMemberPanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceMemberPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceMemberPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();

        GridLayout layHeader = new GridLayout(5, 5);

        layHeader.setHgap(30);
        layHeader.setVgap(2);
        pnlHeader.setLayout(layHeader);
        pnlHeader.setOpaque(false);

        // row 1
        f_txtClusterName = getTextField(10, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_cluster_name", f_txtClusterName));
        pnlHeader.add(f_txtClusterName);

        pnlHeader.add(getFiller());

        f_txtLicenseMode = getTextField(5, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_license_mode", f_txtLicenseMode));
        pnlHeader.add(f_txtLicenseMode);

        // row 2
        f_txtVersion = getTextField(10, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_version", f_txtVersion));
        pnlHeader.add(f_txtVersion);

        pnlHeader.add(getFiller());

        f_txtEdition = getTextField(5, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_edition", f_txtEdition));
        pnlHeader.add(f_txtEdition);

        // row 3
        f_txtTotalMembers = getTextField(4);
        pnlHeader.add(getLocalizedLabel("LBL_total_members"));
        pnlHeader.add(f_txtTotalMembers);

        pnlHeader.add(getFiller());

        f_txtTotalMemory = getTextField(6);
        pnlHeader.add(getLocalizedLabel("LBL_total_cluster_memory", f_txtTotalMemory));
        pnlHeader.add(f_txtTotalMemory);

        // row 4
        f_txtTotalStorageMembers = getTextField(4);
        pnlHeader.add(getLocalizedLabel("LBL_total_storage_members"));
        pnlHeader.add(f_txtTotalStorageMembers);

        pnlHeader.add(getFiller());

        f_txtTotalMemoryUsed = getTextField(6);
        pnlHeader.add(getLocalizedLabel("LBL_total_cluster_memory_used", f_txtTotalMemoryUsed));
        pnlHeader.add(f_txtTotalMemoryUsed);

        // row 5
        f_txtDepartureCount = getTextField(5);
        pnlHeader.add(getLocalizedLabel("LBL_member_departure_count", f_txtDepartureCount));
        pnlHeader.add(f_txtDepartureCount);

        pnlHeader.add(getFiller());

        f_txtTotalMemoryAvail = getTextField(6);
        pnlHeader.add(getLocalizedLabel("LBL_total_cluster_memory_avail", f_txtTotalMemoryAvail));
        pnlHeader.add(f_txtTotalMemoryAvail);

        pnlHeader.setBorder(new CompoundBorder(new TitledBorder(getLocalizedText("LBL_overview")),
                                               new EmptyBorder(10, 10, 10, 10)));

        // create the table
        f_tmodel = new MemberTableModel(VisualVMModel.DataType.MEMBER.getMetadata());

        f_table = new ExportableJTable(f_tmodel, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 4));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, MemberData.PUBLISHER_SUCCESS, new RenderHelper.SuccessRateRenderer());
        RenderHelper.setColumnRenderer(f_table, MemberData.RECEIVER_SUCCESS, new RenderHelper.SuccessRateRenderer());
        RenderHelper.setColumnRenderer(f_table, MemberData.SENDQ_SIZE, new RenderHelper.IntegerRenderer());

        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        // Add some space
        setTablePadding(f_table);

        MenuOption menuDetail = new ShowDetailMenuOption(model, f_table, SELECTED_NODE);

        // reportNodeDetails only available in 12.2.1 and above
        if (model.getClusterVersionAsInt() >= 122100)
            {
            f_table.setMenuOptions(new MenuOption[] {
                    menuDetail,
                    new ReportEnvironmentMenuOption(model, m_requestSender, f_table),
                    new ReportNodeStateMenuOption(model, m_requestSender, f_table),
                    new ReportNodeStateMultiMenuOption(model, m_requestSender, f_table),
                    new GetDescriptionMenuOption(model, m_requestSender, f_table)});
            }
        else
            {
            f_table.setMenuOptions(new MenuOption[] {menuDetail});
            }

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(f_table);
        configureScrollPane(scrollPane, f_table);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(pnlHeader, BorderLayout.PAGE_START);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        // create a chart for the total cluster memory
        f_memoryGraph = GraphHelper.createClusterMemoryGraph();

        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        pnlPlotter.add(f_memoryGraph.getChart());

        pneSplit.add(topPanel);
        pneSplit.add(pnlPlotter);

        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ----------------------------------

    @Override
    public void updateGUI()
        {
        int cTotalMemory = 0;
        int cTotalMemoryUsed = 0;
        int cStorageCount = 0;
        String sEdition = "";

        if (m_memberData != null)
            {
            Object[] aoStorageDetails = getStorageDetails(m_memberData);
            
            cTotalMemory = (int) aoStorageDetails[0];
            cTotalMemoryUsed = (int) aoStorageDetails[1];
            cStorageCount = (int) aoStorageDetails[2];
            sEdition = (String) aoStorageDetails[4];
            f_txtTotalMembers.setText(String.format("%5d", m_memberData.size()));
            f_txtTotalMemory.setText(String.format(MEM_FORMAT, cTotalMemory));
            f_txtTotalMemoryUsed.setText(String.format(MEM_FORMAT, cTotalMemoryUsed));
            f_txtTotalMemoryAvail.setText(String.format(MEM_FORMAT, cTotalMemory - cTotalMemoryUsed));
            }
        else
            {
            f_txtTotalMembers.setText("");
            f_txtTotalMemory.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalMemoryUsed.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalMemoryAvail.setText(String.format(MEM_FORMAT, 0));
            }

        if (m_clusterData != null)
            {
            for (Entry<Object, Data> entry : m_clusterData)
                {
                f_txtClusterName.setText(entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString());
                f_txtLicenseMode.setText(entry.getValue().getColumn(ClusterData.LICENSE_MODE).toString());
                f_txtVersion.setText(entry.getValue().getColumn(ClusterData.VERSION).toString().replaceFirst(" .*$", ""));
                f_txtDepartureCount.setText(entry.getValue().getColumn(ClusterData.DEPARTURE_COUNT).toString());
                }
            }

        f_txtEdition.setText(sEdition);
        f_txtTotalStorageMembers.setText(String.format("%5d", cStorageCount));

        fireTableDataChangedWithSelection(f_table, f_tmodel);

        // update the memory graph
        if (cTotalMemory != 0)
            {
            GraphHelper.addValuesToClusterMemoryGraph(f_memoryGraph, cTotalMemory, cTotalMemoryUsed);
            }
        }

    @Override
    public void updateData()
        {
        List<Entry<Object, Data>> tempList   = new ArrayList<>();
        List<Entry<Object, Data>> memberData = f_model.getData(VisualVMModel.DataType.MEMBER);

        // ensure we have member data to process
        if (memberData != null)
            {
            // go through and set storage enabled column
            for (Entry<Object, Data> entry : memberData)
                {
                Data data = entry.getValue();
                int nodeId = (Integer) entry.getKey();
                if (!isNodeStorageEnabled(f_model, nodeId))
                    {
                    data.setColumn(MemberData.STORAGE_ENABLED, "false");
                    }

                tempList.add(entry);
                }

            m_memberData = tempList;
            m_clusterData = f_model.getData(VisualVMModel.DataType.CLUSTER);

            if (m_memberData != null)
                {
                f_tmodel.setDataList(m_memberData);
                }
            }
        }

    // ----- inner classes ReportEnvironmentMenuOption ----------------------

    /**
     * A class to call the environment operation on the selected ClusterNode
     * MBean and display the details.
     */
    private class ReportEnvironmentMenuOption
            extends AbstractMenuOption
        {

        /**
         * {@inheritDoc}
         */
        public ReportEnvironmentMenuOption(VisualVMModel model, RequestSender requestSender,
                                           ExportableJTable jtable)
            {
            super(model, requestSender, jtable);
            }

        // ----- MenuOptions methods ----------------------------------------

        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            Integer nNodeId = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    nNodeId        = (Integer) getJTable().getModel().getValueAt(nRow, 0);
                    String sResult = generateHeader(nNodeId) + m_requestSender.reportEnvironment(nNodeId);
                    showMessageDialog(getLocalizedText("LBL_environment_for_node") + " " + nNodeId,
                        sResult, JOptionPane.INFORMATION_MESSAGE, 500, 400, true);
                    }
                catch (Exception ee)
                    {
                    showMessageDialog("Error running reportEnvironment for Node " + nNodeId, getSanitizedMessage(ee), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        @Override
        public String getMenuItem()
            {
            return getLocalizedText("LBL_report_node_environment");
            }
        }

   // ----- inner classes GetDescriptionMenuOption ----------------------

    /**
     * A class to call the description operation on the selected ClusterNode
     * MBean and display the details.
     */
    private class GetDescriptionMenuOption
            extends AbstractMenuOption
        {

        /**
         * {@inheritDoc}
         */
        public GetDescriptionMenuOption(VisualVMModel model, RequestSender requestSender,
                                           ExportableJTable jtable)
            {
            super(model, requestSender, jtable);
            }

           // ----- MenuOptions methods ----------------------------------------

        @Override
        public String getMenuItem()
            {
            return getLocalizedText("LBL_show_description");
            }

        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            Integer nNodeId = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    nNodeId        = (Integer) getJTable().getModel().getValueAt(nRow, 0);
                    String sResult = generateHeader(nNodeId) + m_requestSender.getNodeDescription(nNodeId);

                    showMessageDialog(getLocalizedText("LBL_environment_for_node") + " " + nNodeId,
                        sResult, JOptionPane.INFORMATION_MESSAGE, 500, 400, true);
                    }
                catch (Exception ee)
                    {
                    showMessageDialog("Error running reportEnvironment for Node " + nNodeId, getSanitizedMessage(ee), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

    // ----- inner classes ReportNodeDetailsMenuOption ----------------------

    /**
     * A class to call the reportNodeState operation on the selected ClusterNode
     * MBean and display the details.
     */
    private class ReportNodeStateMenuOption
            extends AbstractMenuOption
        {

        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public ReportNodeStateMenuOption(VisualVMModel model, RequestSender requestSender,
                                         ExportableJTable jtable)
            {
            super(model, requestSender, jtable);
            }

        // ----- MenuOptions methods ----------------------------------------

        @Override
        public String getMenuItem()
            {
            return getLocalizedText("LBL_report_node_state");
            }

        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            Integer nNodeId = null;
            final StatusDisplayer.Message[] status = new StatusDisplayer.Message[1];

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    nNodeId = (Integer) getJTable().getModel().getValueAt(nRow, 0);

                    if (this instanceof ReportNodeStateMultiMenuOption)
                        {
                        // ask the user for the number of thread dumps and the time between
                        JPanel   panel        = new JPanel();
                        JSpinner spinnerCount = new JSpinner();
                        JSpinner spinnerDelay = new JSpinner();

                        JTextField txtMessage = getTextField(15, JTextField.LEFT);
                        txtMessage.setEditable(false);
                        txtMessage.setEnabled(false);

                        ChangeListener changeListener = event ->
                            {
                            int nCount = (Integer) spinnerCount.getValue();
                            int nDelay = (Integer) spinnerDelay.getValue();
                            txtMessage.setText(getLocalText("LBL_wait", Integer.toString((nCount - 1) * nDelay)));
                            };

                        spinnerCount.setModel(new SpinnerNumberModel(5, 2, 100, 1));
                        spinnerCount.addChangeListener(changeListener);

                        JLabel lblCount = getLocalizedLabel("LBL_number_thread_dumps");
                        lblCount.setLabelFor(spinnerCount);

                        panel.add(lblCount);
                        panel.add(spinnerCount);

                        panel.add(Box.createHorizontalStrut(15));

                        JLabel lblDelay = getLocalizedLabel("LBL_time_between");
                        spinnerDelay.setModel(new SpinnerNumberModel(10, 5, 100, 5));
                        lblDelay.setLabelFor(spinnerDelay);
                        spinnerDelay.addChangeListener(changeListener);

                        // trigger the change
                        changeListener.stateChanged(null);

                        panel.add(lblDelay);
                        panel.add(spinnerDelay);

                        panel.add(Box.createHorizontalStrut(15));
                        panel.add(txtMessage);

                        final int nNode = nNodeId;

                        int result = JOptionPane.showConfirmDialog(null, panel,
                                                                   getLocalizedText("LBL_multi"), JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION)
                            {
                            int nCount = (Integer) spinnerCount.getValue();
                            int nDelay = (Integer) spinnerDelay.getValue();

                            String sMessage = "Generating " + nCount + " thread dumps " + nDelay + " seconds apart\n";
                            LOGGER.info(sMessage);
                            status[0] = StatusDisplayer.getDefault().setStatusText(sMessage,5);

                            DialogHelper.showInfoDialog(getLocalText("LBL_thread_dump_confirmation"));

                            StringBuilder sb = new StringBuilder(sMessage);

                            Timer timer = new Timer(nDelay * 1000, null);

                            // generate the first thread dump and then run the timer for subsequent thread dumps
                            generateThreadDump(sb, 1, nCount, nNode, status);

                            timer.addActionListener(new ActionListener()
                                {
                                public void actionPerformed(ActionEvent e)
                                    {
                                    m_nCounter++;
                                    try
                                        {
                                        generateThreadDump(sb, m_nCounter, nCount, nNode, status);
                                        }
                                    catch (Exception exception)
                                        {
                                        sb.append(exception.getMessage());
                                        timer.stop();
                                        }

                                    if (m_nCounter == nCount)
                                        {
                                        timer.stop();
                                        status[0] = StatusDisplayer.getDefault().setStatusText(getLocalText("LBL_thread_dump_completed"),5);
                                        status[0].clear(5000);
                                        showThreadDumpInVisualVM(nNode, sb.toString());
                                        }
                                    }

                                private int m_nCounter = 1;
                                });

                            timer.setRepeats(true);
                            timer.start();
                            }
                        }
                    else
                        {
                        showThreadDumpInVisualVM(nNodeId, m_requestSender.getNodeState(nNodeId));
                        }
                    }
                catch (Exception ee)
                    {
                    showMessageDialog("Error running reportNodeState for Node " + nNodeId, getSanitizedMessage(ee), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            // ------ helpers -----------------------------------------------

            /**
             * Show the therad dump result in VisualVM thread dump viewer.
             * @param nNode        node id
             * @param sThreadDump  thread dump content
             */
            private void showThreadDumpInVisualVM(int nNode, String sThreadDump)
                {
                // create a temp file
                try
                    {
                    String sPrefix     = "node-" + nNode + "-";
                    File  fileTempDir = new File(System.getProperty("java.io.tmpdir"));
                    File  fileTemp    = File.createTempFile(sPrefix, null, fileTempDir);
                    boolean fResult1  = fileTemp.setReadable(false);
                    boolean fResult2 = fileTemp.setWritable(false);
                    boolean fResult3 = fileTemp.setExecutable(false);
                    boolean fResult4 = fileTemp.setReadable(true, true);
                    boolean fResult5 = fileTemp.setWritable(true, true);
                    boolean fResult6 = fileTemp.setExecutable(true, true);

                    if (!fResult1 || !fResult2 || !fResult3 || !fResult4 || !fResult5 || !fResult6)
                        {
                        throw new RuntimeException("unable to set file permissions for " + fileTemp.getAbsolutePath());
                        }

                    try (PrintWriter pw = new PrintWriter(fileTemp, "UTF-8"))
                        {
                        pw.write(sThreadDump);
                        }

                    final ThreadDumpImpl threadDump = new ThreadDumpImpl(fileTemp, null);

                    DataSource.EVENT_QUEUE.post(new Runnable()
                        {
                        public void run() { DataSourceWindowManager.sharedInstance().openDataSource(threadDump); }
                        });
                    }
                catch (IOException e)
                    {
                    LOGGER.warning(e.getMessage());
                    }
                }
            }

    /**
     * Generate a thread dump and save the output in the {@link StringBuilder}.
     *
     * @param sb       {@link StringBuilder} to save output in
     * @param nCounter current counter of thread dumps
     * @param nMax     max number of thread dumps
     * @param status   {@link StatusDisplayer.Message} to display messages
     *
     * @throws Exception if any errors
     */
    private void generateThreadDump(StringBuilder sb, int nCounter, int nMax, int nNode, StatusDisplayer.Message[] status)
            throws Exception
        {
        String sNodeState = m_requestSender.getNodeState(nNode);
        status[0] = StatusDisplayer.getDefault().setStatusText(getLocalText("LBL_thread_dump_progress",
                                            Integer.toString(nCounter), Integer.toString(nMax),
                                            String.format("%3.1f", (nCounter * 1f / nMax) * 100.0f )), 5);

        sb.append("*** START THREAD DUMP ").append(nCounter).append(" - ")
                .append(new Date(System.currentTimeMillis()))
                .append("\n")
                .append(sNodeState)
                .append("\n*** END THREAD DUMP ").append(nCounter).append("\n");
        }

    /**
     * Generate a header for the thread dumps.
     * @param nNode node id
     * @return a header
     */
    private String generateHeader(int nNode)
        {
        StringBuilder sb = new StringBuilder("Cluster Details\n");

        // get the cluster details
        for (Map.Entry<Object, Data> entry : f_model.getData(VisualVMModel.DataType.CLUSTER))
            {
            sb.append(getLocalText("LBL_cluster_name")).append(": ")
              .append(entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString()).append("\n")
              .append(getLocalText("LBL_version")).append(": ")
              .append(entry.getValue().getColumn(ClusterData.VERSION).toString()).append("\n")
              .append(getLocalText("LBL_license_mode")).append(": ")
              .append(entry.getValue().getColumn(ClusterData.LICENSE_MODE).toString()).append("\n")
              .append(getLocalText("LBL_members")).append(": ")
              .append(String.format("%d", (Integer) entry.getValue().getColumn(ClusterData.CLUSTER_SIZE)));
            }

        sb.append("\n\nMember Details\n");

        String[] asColumns = VisualVMModel.DataType.MEMBER.getMetadata();
        for (Entry<Object, Data> entry : f_model.getData(VisualVMModel.DataType.MEMBER))
            {
            if ((Integer) entry.getValue().getColumn(MemberData.NODE_ID) == nNode)
                {
                // this is the node
                sb.append(asColumns[MemberData.NODE_ID]).append(": ")
                    .append(entry.getValue().getColumn(MemberData.NODE_ID).toString()).append("\n")
                    .append(asColumns[MemberData.ADDRESS]).append(": ")
                    .append(entry.getValue().getColumn(MemberData.ADDRESS).toString()).append("\n")
                    .append(asColumns[MemberData.PORT]).append(": ")
                    .append(entry.getValue().getColumn(MemberData.PORT).toString()).append("\n")
                    .append(asColumns[MemberData.ROLE_NAME]).append(": ")
                    .append(entry.getValue().getColumn(MemberData.ROLE_NAME).toString()).append("\n")
                    .append(asColumns[MemberData.PUBLISHER_SUCCESS]).append(": ")
                    .append(getPublisherValue(entry.getValue().getColumn(MemberData.PUBLISHER_SUCCESS).toString())).append("\n")
                    .append(asColumns[MemberData.RECEIVER_SUCCESS]).append(": ")
                    .append(getPublisherValue(entry.getValue().getColumn(MemberData.RECEIVER_SUCCESS).toString())).append("\n")
                    .append(asColumns[MemberData.SENDQ_SIZE]).append(": ")
                    .append(getMemoryFormat(entry.getValue().getColumn(MemberData.SENDQ_SIZE).toString())).append("\n")
                    .append(asColumns[MemberData.MAX_MEMORY]).append(": ")
                    .append(getMemoryFormat(entry.getValue().getColumn(MemberData.MAX_MEMORY).toString())).append("\n")
                    .append(asColumns[MemberData.USED_MEMORY]).append(": ")
                    .append(getMemoryFormat(entry.getValue().getColumn(MemberData.USED_MEMORY).toString())).append("\n")
                    .append(asColumns[MemberData.FREE_MEMORY]).append(": ")
                    .append(getMemoryFormat(entry.getValue().getColumn(MemberData.FREE_MEMORY).toString())).append("\n")
                    .append(asColumns[MemberData.STORAGE_ENABLED]).append(": ")
                    .append(entry.getValue().getColumn(MemberData.STORAGE_ENABLED).toString()).append("\n\n");
                }
            }

        return sb.toString();
        }

     /**
     * A class to call the reportNodeState operation on the selected ClusterNode
     * MBean and display the details.
     */
    private class ReportNodeStateMultiMenuOption
            extends ReportNodeStateMenuOption
        {

        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public ReportNodeStateMultiMenuOption(VisualVMModel model, RequestSender requestSender,
                                         ExportableJTable jtable)
            {
            super(model, requestSender, jtable);
            }

        @Override
        public String getMenuItem()
            {
            return getLocalizedText("LBL_report_node_state_multi");
            }
        }

    /**
     * Return a sanitized message to make common errors more meaningful.
     * @param e {@link Exception} to get message from
     *
     * @return final message
     */
    private String getSanitizedMessage(Exception e)
        {
        String sError = e.getMessage();

        if (sError == null)
            {
            return Localization.getLocalText("LBL_operation_not_available");
            }

        return sError.contains("name cannot be null") ? Localization.getLocalText("LBL_node_not_available") : sError;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(CoherenceMemberPanel.class.getName());


    // ----- data members ---------------------------------------------------

    /**
     * The total number of members in the cluster.
     */
    private final JTextField f_txtTotalMembers;

    /**
     * Total number of storage-enabled members.
     */
    private final JTextField f_txtTotalStorageMembers;

    /**
     * The total amount of memory allocated in the cluster by all
     * storage-enabled members.
     */
    private final JTextField f_txtTotalMemory;

    /**
     * The total amount of memory available in the cluster by all
     * storage-enabled members.
     */
    private final JTextField f_txtTotalMemoryAvail;

    /**
     * The total amount of memory used in the cluster by all storage-enabled
     * members.
     */
    private final JTextField f_txtTotalMemoryUsed;

    /**
     * The name of the cluster.
     */
    private final JTextField f_txtClusterName;

    /**
     * The license mode of the cluster.
     */
    private final JTextField f_txtLicenseMode;

    /**
     * The edition of the cluster.
     */
    private final JTextField f_txtEdition;

    /**
     * The total number of members departed.
     */
    private final JTextField f_txtDepartureCount;

    /**
     * The Coherence version of the cluster.
     */
    private final JTextField f_txtVersion;

    /**
     * The graph of cluster memory.
     */
    private final SimpleXYChartSupport f_memoryGraph;

    /**
     * The member statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_memberData;

    /**
     * The cluster statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_clusterData;

    /**
     * The {@link MemberTableModel} to display member data.
     */
    protected final MemberTableModel f_tmodel;

    /**
     * The {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;
    }
