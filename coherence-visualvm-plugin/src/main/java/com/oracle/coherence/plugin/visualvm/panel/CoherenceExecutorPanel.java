/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map.Entry;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.ExecutorTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ExecutorData;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view
 * summarized executor data.
 *
 * @author tam  2021.08.11
 * @since 1.2.0
 */
public class CoherenceExecutorPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceExecutorPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceExecutorPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalExecutors = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_executors", f_txtTotalExecutors));
        pnlHeader.add(f_txtTotalExecutors);

        f_txtTotalRunningTasks = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_running_tasks", f_txtTotalRunningTasks));
        pnlHeader.add(f_txtTotalRunningTasks);

        f_txtTotalCompltedTasks = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_completed_tasks", f_txtTotalCompltedTasks));
        pnlHeader.add(f_txtTotalCompltedTasks);
        
        // create the table
        f_tmodel = new ExecutorTableModel(VisualVMModel.DataType.EXECUTOR.getMetadata());

        f_table = new ExportableJTable(f_tmodel, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // define renderers for the columns

        RenderHelper.setColumnRenderer(f_table, ExecutorData.TASKS_COMPLETED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, ExecutorData.TASKS_REJECTED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, ExecutorData.TASKS_IN_PROGRESS, new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        // Add some space
        setTablePadding(f_table);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll = new JScrollPane(f_table);
        configureScrollPane(pneScroll, f_table);
        pneScroll.setOpaque(false);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(pneScroll, BorderLayout.CENTER);

        JSplitPane pneSplitPlotter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitPlotter.setResizeWeight(0.5);
        pneSplitPlotter.setOpaque(false);

        // create a chart for the count of executing and completed tasks
        f_executorGraph = GraphHelper.createInProgressExecutorTasksGraph();
        f_completedTasksGraph = GraphHelper.createCompletedTasksGraph();

        pneSplitPlotter.add(f_executorGraph.getChart());
        pneSplitPlotter.add(f_completedTasksGraph.getChart());

        pneSplit.add(pnlTop);
        pneSplit.add(pneSplitPlotter);

        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        final String MEM_FORMAT = "%,d";
        long  cTotalRunningTasks = 0L;
        long  nCompletedCount    = 0L;
        long  nFailedCount       = 0L;

        if (m_executorData != null)
            {
            f_txtTotalExecutors.setText(String.format("%5d", m_executorData.size()));

            for (Entry<Object, Data> entry : m_executorData)
                {
                cTotalRunningTasks += (Long) entry.getValue().getColumn(ExecutorData.TASKS_IN_PROGRESS);
                nCompletedCount    += (Long) entry.getValue().getColumn(ExecutorData.TASKS_COMPLETED);
                nFailedCount        += (Long) entry.getValue().getColumn(ExecutorData.TASKS_REJECTED);
                }

            f_txtTotalRunningTasks.setText(String.format(MEM_FORMAT, cTotalRunningTasks));
            f_txtTotalCompltedTasks.setText(String.format(MEM_FORMAT, nCompletedCount));
            }
        else
            {
            f_txtTotalExecutors.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalRunningTasks.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalCompltedTasks.setText(String.format(MEM_FORMAT, 0));
            }

        fireTableDataChangedWithSelection(f_table, f_tmodel);

        GraphHelper.addValuesToInProgressExecutorTasksGraph(f_executorGraph, cTotalRunningTasks);

        long ldtLastUpdate = f_model.getLastUpdate();
        if (ldtLastUpdate > m_cLastUpdateTime)
            {
            if (m_cLastCompletedCount == -1L)
                {
                m_cLastCompletedCount = nCompletedCount;
                m_cLastFailedCount    = nFailedCount;
                }

            // get delta values
            long nDeltaCompletedCount = nCompletedCount - m_cLastCompletedCount;
            long nDeltaFailedCount    = nFailedCount - m_cLastFailedCount;

            GraphHelper.addValuesToCompletedTasksGraph(f_completedTasksGraph,
                    nDeltaCompletedCount  < 0 ? 0 : nDeltaCompletedCount,
                    nDeltaFailedCount      < 0 ? 0 : nDeltaFailedCount);

            // set the last values to calculate deltas
            m_cLastCompletedCount = nCompletedCount;
            m_cLastFailedCount    = nFailedCount;
            m_cLastUpdateTime     = ldtLastUpdate;
            }
        }

    @Override
    public void updateData()
        {
        m_executorData = f_model.getData(VisualVMModel.DataType.EXECUTOR);

        f_tmodel.setDataList(m_executorData);
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

    // ----- data members ----------------------------------------------------

    /**
     * The total number of executor services running in the cluster.
     */
    private final JTextField f_txtTotalExecutors;

    /**
     * The total number of running tasks.
     */
    private final JTextField f_txtTotalRunningTasks;

    /**
     * The total number of completed tasks.
     */
    private final JTextField f_txtTotalCompltedTasks;

    /**
     * The graph of running tasks.
     */
    private final SimpleXYChartSupport f_executorGraph;

    /**
     * The graph of completed tasks.
     */
    private final SimpleXYChartSupport f_completedTasksGraph;

    /**
     * The executor statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_executorData;

    /**
     * The {@link ExecutorTableModel} to display executor data.
     */
    protected final ExecutorTableModel f_tmodel;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * Last receive count.
     */
    private long m_cLastCompletedCount = -1L;

    /**
     * Last failed count.
     */
    private long m_cLastFailedCount = -1L;

    /**
     * Last update time for stats.
     */
    private long m_cLastUpdateTime = -1L;
    }
