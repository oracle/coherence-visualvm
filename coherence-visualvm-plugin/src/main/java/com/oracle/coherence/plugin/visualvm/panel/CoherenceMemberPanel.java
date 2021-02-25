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

package com.oracle.coherence.plugin.visualvm.panel;


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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;


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

        f_table = new ExportableJTable(f_tmodel);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 4));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, MemberData.PUBLISHER_SUCCESS, new RenderHelper.SuccessRateRenderer());
        RenderHelper.setColumnRenderer(f_table, MemberData.RECEIVER_SUCCESS, new RenderHelper.SuccessRateRenderer());
        RenderHelper.setColumnRenderer(f_table, MemberData.SENDQ_SIZE, new RenderHelper.IntegerRenderer());

        RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);

        // Add some space
        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        MenuOption menuDetail = new ShowDetailMenuOption(model, f_table, SELECTED_NODE);

        // reportNodeDetails only available in 12.2.1 and above
        if (model.getClusterVersionAsInt() >= 122100)
            {
            f_table.setMenuOptions(new MenuOption[] {menuDetail, new ReportNodeStateMenuOption(model, m_requestSender, f_table)});
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateData()
        {
        List<Entry<Object, Data>> tempList = new ArrayList<>();

        // go through and set storage enabled column
        for (Entry<Object, Data> entry : f_model.getData(VisualVMModel.DataType.MEMBER))
            {
            Data data = entry.getValue();
            int nodeId = (Integer) entry.getKey();
            if (!isNodeStorageEnabled(nodeId))
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

            if (nRow == -1)
                {
                JOptionPane.showMessageDialog(null, getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    nNodeId = (Integer) getJTable().getModel().getValueAt(nRow, 0);

                    String sResult = m_requestSender.getNodeState(nNodeId);

                    showMessageDialog(getLocalizedText("LBL_state_for_node") + " " + nNodeId, sResult, JOptionPane.INFORMATION_MESSAGE);
                    }
                catch (Exception ee)
                    {
                    showMessageDialog("Error running reportNodeState for Node " + nNodeId, ee.getMessage(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

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
