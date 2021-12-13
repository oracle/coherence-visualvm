/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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
import java.awt.GridLayout;
import java.util.List;
import java.util.Map.Entry;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.TopicTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicData;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view summarized topics
 * statistics.
 *
 * @author tam  2020.02.08
 * @since  1.0.1
 */
public class CoherenceTopicPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceTopicPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceTopicPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Populate the header panel
        JPanel pnlTop    = new JPanel(new BorderLayout());
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalTopics = getTextField(5);
        pnlHeader.add(getLocalizedLabel("LBL_total_topics", f_txtTotalTopics));
        pnlHeader.add(f_txtTotalTopics);

        f_txtTotalMemory = getTextField(10);
        pnlHeader.add(getLocalizedLabel("LBL_total_data", f_txtTotalMemory));
        pnlHeader.add(f_txtTotalMemory);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.setOpaque(false);

        // create any table models required
        f_tmodel = new TopicTableModel(VisualVMModel.DataType.TOPICS_DETAIL.getMetadata());
        f_table = new ExportableJTable(f_tmodel);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 5));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, TopicData.SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.AVG_OBJECT_SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.MEMORY_USAGE_MB, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.MEMORY_USAGE_BYTES, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.PUBLISHER_SENDS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.SUBSCRIBER_RECEIVES, new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);

        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll = new JScrollPane(f_table);
        configureScrollPane(pneScroll, f_table);
        pneScroll.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(pneScroll, BorderLayout.CENTER);
        
        f_unconsumedGraph = GraphHelper.createTotalUnconsumedMessagesGraph();
        f_topicsRatesGraph = GraphHelper.createTopicsRateGraph();
        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        pnlPlotter.add(f_topicsRatesGraph.getChart());

        pneSplit.add(pnlTop);
        pneSplit.add(pnlPlotter);
        add(pneSplit);
        }

    // ---- AbstractCoherencePanel methods ----------------------------------

    @Override
    public void updateData()
        {
        m_topicData = f_model.getData(VisualVMModel.DataType.TOPICS_DETAIL);
        f_tmodel.setDataList(m_topicData);
        }

    @Override
    public void updateGUI()
        {
        long nSendCount = 0;
        long nRecCount = 0;
        long  cTotalUnconsumed = 0;

        if (m_topicData != null)
            {
            f_txtTotalTopics.setText(String.format("%5d", m_topicData.size()));

            float cTotalTopicsSize = 0.0f;

            for (Entry<Object, Data> entry : m_topicData)
                {
                cTotalTopicsSize += Float.valueOf((Long) entry.getValue().getColumn(TopicData.MEMORY_USAGE_BYTES));
                cTotalUnconsumed += Float.valueOf((Integer) entry.getValue().getColumn(TopicData.SIZE));
                nSendCount += (Long) entry.getValue().getColumn(TopicData.PUBLISHER_SENDS);
                nRecCount += (Long) entry.getValue().getColumn(TopicData.SUBSCRIBER_RECEIVES);
                }

            f_txtTotalMemory.setText(String.format("%,10.2f", cTotalTopicsSize / 1024 / 1024));
            }
        else
            {
            f_txtTotalTopics.setText("0");
            f_txtTotalMemory.setText(String.format("%,10.2f", 0.0));
            }

        GraphHelper.addValuesToTotalUnconsumedTopicsGraph(f_unconsumedGraph, cTotalUnconsumed);
        
        fireTableDataChangedWithSelection(f_table, f_tmodel);

        long ldtLastUpdate = f_model.getLastUpdate();
        if (ldtLastUpdate > m_cLastUpdateTime)
            {
            if (m_cLastRecCount == -1L)
                {
                m_cLastRecCount  = nRecCount;
                m_cLastSendCount = nSendCount;
                }
            // get delta values
            long nDeltaRecCount  = nRecCount - m_cLastRecCount;
            long nDeltaSendCount = nSendCount - m_cLastSendCount;

            GraphHelper.addValuesToTopicsRateGraph(f_topicsRatesGraph,
                    nDeltaSendCount < 0 ? 0 : nDeltaSendCount,
                    nDeltaRecCount  < 0 ? 0 : nDeltaRecCount);

            // set the last values to calculate deltas
            m_cLastRecCount   = nRecCount;
            m_cLastSendCount  = nSendCount;
            m_cLastUpdateTime = ldtLastUpdate;
            }
        }

    // ---- constants -------------------------------------------------------

    private static final long serialVersionUID = -761256904492412496L;

    // ----- data members ---------------------------------------------------

    /**
     * Total number of topics.
     */
    private final JTextField f_txtTotalTopics;

    /**
     * Total primary copy memory used by topics.
     */
    private final JTextField f_txtTotalMemory;

    /**
     * The {@link TopicTableModel} to display topic data.
     */
    protected final TopicTableModel f_tmodel;
    
    /**
     * The topic data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_topicData = null;

    /**
     * The graph of unconsumed messages.
     */
    private final SimpleXYChartSupport f_unconsumedGraph;

    /**
     * The graph of topics rates.
     */
    private final SimpleXYChartSupport f_topicsRatesGraph;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * Last update time for stats.
     */
    private long m_cLastUpdateTime = -1L;

    /**
     * Last publisher send count.
     */
    private long m_cLastSendCount = -1L;

    /**
     * Last subscriber receive count.
     */
    private long m_cLastRecCount = -1L;
    }
