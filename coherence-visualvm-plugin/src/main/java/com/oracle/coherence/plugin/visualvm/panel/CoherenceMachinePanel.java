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

import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.tablemodel.MachineTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MachineData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.util.List;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized machine data.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CoherenceMachinePanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceMachinePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceMachinePanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalMachines = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_machines", f_txtTotalMachines));
        pnlHeader.add(f_txtTotalMachines);

        f_txtTotalClusterCores = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_cores", f_txtTotalClusterCores));
        pnlHeader.add(f_txtTotalClusterCores);

        // create the table
        f_tmodel = new MachineTableModel(VisualVMModel.DataType.MACHINE.getMetadata());

        f_table = new ExportableJTable(f_tmodel, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, MachineData.FREE_PHYSICAL_MEMORY, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, MachineData.TOTAL_PHYSICAL_MEMORY, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, MachineData.SYSTEM_LOAD_AVERAGE,
                                       new RenderHelper.DecimalRenderer(RenderHelper.LOAD_AVERAGE_FORMAT));
        RenderHelper.setColumnRenderer(f_table, MachineData.PERCENT_FREE_MEMORY, new RenderHelper.FreeMemoryRenderer());
        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        // Add some space
        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(f_table);
        configureScrollPane(scrollPane, f_table);

        JPanel      pnlTop     = new JPanel();

        pnlTop.setLayout(new BorderLayout());
        pnlTop.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(scrollPane, BorderLayout.CENTER);

        pneSplit.add(pnlTop);

        // create a chart for the machine load averages
        f_machineGraph = GraphHelper.createMachineLoadAverageGraph(model);

        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        pnlPlotter.add(f_machineGraph.getChart());

        pneSplit.add(pnlPlotter);
        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        final String MEM_FORMAT        = "%,d";

        int          cTotalCores       = 0;
        int          count             = 0;
        double       cLoadAverage      = 0;
        double       cMax              = -1;
        double       cTotalLoadAverage = 0;

        // work out the max and average load averages for the graph
        if (m_machineData != null)
            {
            f_txtTotalMachines.setText(String.format("%5d", m_machineData.size()));

            for (Entry<Object, Data> entry : m_machineData)
                {
                count++;
                cTotalCores       += (Integer) entry.getValue().getColumn(MachineData.PROCESSOR_COUNT);
                cLoadAverage      = (Double) entry.getValue().getColumn(MachineData.SYSTEM_LOAD_AVERAGE);
                cTotalLoadAverage += cLoadAverage;

                if (cMax == -1 || cLoadAverage > cMax)
                    {
                    cMax = cLoadAverage;
                    }
                }

            f_txtTotalClusterCores.setText(String.format(MEM_FORMAT, cTotalCores));

            // update graph
            GraphHelper.addValuesToLoadAverageGraph(f_machineGraph, (float) cMax,
                (float) (cTotalLoadAverage == 0 ? 0 : cTotalLoadAverage / count));
            }
        else
            {
            f_txtTotalClusterCores.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalMachines.setText(String.format(MEM_FORMAT, 0));
            }

        fireTableDataChangedWithSelection(f_table, f_tmodel);
        }

    @Override
    public void updateData()
        {
        m_machineData = f_model.getData(VisualVMModel.DataType.MACHINE);

        if (m_machineData != null)
            {
            f_tmodel.setDataList(m_machineData);
            }
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

    // ----- data members ---------------------------------------------------

    /**
     * The total number of cores in the cluster.
     */
    private final JTextField f_txtTotalClusterCores;

    /**
     * The total number of machines in the cluster.
     */
    private final JTextField f_txtTotalMachines;

    /**
     * The graph of machine load averages.
     */
    private transient final SimpleXYChartSupport f_machineGraph;

    /**
     * The machine statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_machineData;

    /**
     * The {@link MachineTableModel} to display machine data.
     */
    protected final MachineTableModel f_tmodel;

    /**
     * The {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;
    }
