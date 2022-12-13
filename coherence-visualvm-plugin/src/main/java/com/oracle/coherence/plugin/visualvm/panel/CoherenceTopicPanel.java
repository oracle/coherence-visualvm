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
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.TopicSubscriberGroupTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.TopicSubscriberTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.TopicTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicData;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicSubscriberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicSubscriberGroupsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Tuple;
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

import static com.oracle.coherence.plugin.visualvm.helper.RenderHelper.INTEGER_FORMAT;
import static com.oracle.coherence.plugin.visualvm.helper.RenderHelper.RATE_FORMAT;

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

        f_txtTotalPublishedCount = getTextField(10);
        pnlHeader.add(getLocalizedLabel("LBL_total_published_count", f_txtTotalPublishedCount));
        pnlHeader.add(f_txtTotalPublishedCount);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.setOpaque(false);

        // create any table models required
        f_tmodel = new TopicTableModel(VisualVMModel.DataType.TOPICS.getMetadata());
        f_table = new ExportableJTable(f_tmodel, model);
        
        f_tmodelSubscribers = new TopicSubscriberTableModel(VisualVMModel.DataType.TOPIC_SUBSCRIBERS.getMetadata());
        f_tmodelSubscriberGroups = new TopicSubscriberGroupTableModel(VisualVMModel.DataType.TOPIC_SUBSCRIBER_GROUPS.getMetadata());

        f_tableSubscribers = new ExportableJTable(f_tmodelSubscribers, model);
        f_tableSubscriberGroups = new ExportableJTable(f_tmodelSubscriberGroups, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 5));
        f_tableSubscribers.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 5));
        f_tableSubscriberGroups.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 5));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, TopicData.PUBLISHED_TOTAL, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.PAGE_CAPACITY, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.RECONNECT_RETRY, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.CHANNELS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.RECONNECT_TIMEOUT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, TopicData.RECONNECT_WAIT, new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        setTablePadding(f_table);
        setTablePadding(f_tableSubscribers);
        setTablePadding(f_tableSubscriberGroups);

        RenderHelper.setColumnRenderer(f_tableSubscribers, TopicSubscriberData.NODE_ID, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscribers, TopicSubscriberData.CHANNELS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscribers, TopicSubscriberData.RECEIVED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscribers, TopicSubscriberData.ERRORS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscribers, TopicSubscriberData.BACKLOG, new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_tableSubscribers, SwingConstants.CENTER);

        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.NODE_ID, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.CHANNELS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.POLLED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.MEAN, new RenderHelper.DecimalRenderer(RATE_FORMAT));
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.ONE_MIN, new RenderHelper.DecimalRenderer(RATE_FORMAT));
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.FIVE_MIN, new RenderHelper.DecimalRenderer(RATE_FORMAT));
        RenderHelper.setColumnRenderer(f_tableSubscriberGroups, TopicSubscriberGroupsData.FIFTEEN_MIN, new RenderHelper.DecimalRenderer(RATE_FORMAT));
        RenderHelper.setHeaderAlignment(f_tableSubscriberGroups, SwingConstants.CENTER);

        f_tableSubscribers.setMenuOptions(new MenuOption[] {new ShowDetailMenuOption(model, f_tableSubscribers, SELECTED_SUBSCRIBER) });
        f_tableSubscriberGroups.setMenuOptions(new MenuOption[] {new ShowDetailMenuOption(model, f_tableSubscriberGroups, SELECTED_SUBSCRIBER_GROUP) });

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll = new JScrollPane(f_table);
        configureScrollPane(pneScroll, f_table);
        pneScroll.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(pneScroll, BorderLayout.CENTER);

        // split pane for graph and then detail for selected topic
        final JSplitPane pneSplitDetail = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitDetail.setOpaque(false);
        pneSplitDetail.setResizeWeight(0.6);
        
        f_topicsRatesGraph = GraphHelper.createTopicsRateGraph();
        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        pnlPlotter.add(f_topicsRatesGraph.getChart());
        pneSplitDetail.add(pnlPlotter);

        // create the panel for the selected topic and details
        JPanel bottomPanel       = new JPanel(new BorderLayout());
        JPanel detailHeaderPanel = new JPanel();
        bottomPanel.setOpaque(false);
        detailHeaderPanel.setOpaque(false);

        f_txtSelectedTopic = getTextField(25, SwingConstants.LEFT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_selected_service_topic", f_txtSelectedTopic));
        detailHeaderPanel.add(f_txtSelectedTopic);

        bottomPanel.add(detailHeaderPanel, BorderLayout.PAGE_START);
        bottomPanel.setOpaque(false);

        f_pneTab = new JTabbedPane();
        f_pneTab.setOpaque(false);

        JScrollPane scrollPaneSubscribers  = new JScrollPane(f_tableSubscribers);
        JScrollPane scrollPaneSubscriberGroups = new JScrollPane(f_tableSubscriberGroups);

        f_pneTab.addTab(getLocalizedText("TAB_subscribers"), scrollPaneSubscribers);
        f_pneTab.addTab(getLocalizedText("TAB_subscriber_groups"), scrollPaneSubscriberGroups);

        bottomPanel.add(f_pneTab, BorderLayout.CENTER);

        pneSplitDetail.add(bottomPanel);

        pneSplit.add(pnlTop);
        pneSplit.add(pneSplitDetail);
        add(pneSplit);

        // add a listener for the selected row
        ListSelectionModel rowSelectionModel = f_table.getSelectionModel();
        f_listener = new SelectRowListSelectionListener(f_table);
        rowSelectionModel.addListSelectionListener(f_listener);
        }

    // ---- AbstractCoherencePanel methods ----------------------------------

    @Override
    public void updateData()
        {
        m_topicData = f_model.getData(VisualVMModel.DataType.TOPICS);
        f_tmodel.setDataList(m_topicData);

        m_topicSubscriberData = f_model.getData(VisualVMModel.DataType.TOPIC_SUBSCRIBERS);
        f_tmodelSubscribers.setDataList(m_topicSubscriberData);

        m_topicSubscriberGroupData = f_model.getData(VisualVMModel.DataType.TOPIC_SUBSCRIBER_GROUPS);
        f_tmodelSubscriberGroups.setDataList(m_topicSubscriberGroupData);

        if (f_model.getSelectedTopic() != null)
            {
            f_listener.updateRowSelection();
            }
        }

    @Override
    public void updateGUI()
        {
        long  cTotalPublished = 0;

        if (m_topicData != null)
            {
            f_txtTotalTopics.setText(String.format("%5d", m_topicData.size()));

            for (Entry<Object, Data> entry : m_topicData)
                {
                cTotalPublished += (Long) entry.getValue().getColumn(TopicData.PUBLISHED_TOTAL);
                }

            f_txtTotalPublishedCount.setText(INTEGER_FORMAT.format(cTotalPublished));
            }
        else
            {
            f_txtTotalTopics.setText("0");
            f_txtTotalPublishedCount.setText(String.format("%,10.2f", 0.0));
            }
        
        fireTableDataChangedWithSelection(f_table, f_tmodel);

        long ldtLastUpdate = f_model.getLastUpdate();
        if (ldtLastUpdate > m_cLastUpdateTime)
            {
            if (m_cLastPublishCount == -1L)
                {
                m_cLastPublishCount = cTotalPublished;
                }
            // get delta values
            long nDeltaPublishedCount = cTotalPublished - m_cLastPublishCount;

            GraphHelper.addValuesToTopicsRateGraph(f_topicsRatesGraph, nDeltaPublishedCount < 0 ? 0 : nDeltaPublishedCount);

            // set the last values to calculate deltas
            m_cLastPublishCount = cTotalPublished;
            m_cLastUpdateTime = ldtLastUpdate;
            }

        Tuple selectedTopic = f_model.getSelectedTopic();

        if (selectedTopic == null)
            {
            f_txtSelectedTopic.setText("");
            }
        else
            {
            f_txtSelectedTopic.setText(selectedTopic.toString());
            }

        fireTableDataChangedWithSelection(f_tableSubscribers, f_tmodelSubscribers);
        fireTableDataChangedWithSelection(f_tableSubscriberGroups, f_tmodelSubscriberGroups);
        }

    // ---- inner classes ---------------------------------------------------

    /**
     * Inner class to change the information displayed on the detailModel
     * table when the master changes.
     */
    private class SelectRowListSelectionListener
            implements ListSelectionListener
        {

        // ----- constructors -----------------------------------------------

        /**
         * Create a new listener to changes the detail table.
         *
         * @param table  the table that is to be selected
         */
        public SelectRowListSelectionListener(ExportableJTable table)
            {
            this.table = table;
            }


        // ----- ListSelectionListener methods ------------------------------

        /**
         * Change and clear the detailModel on selection of a cache.
         *
         * @param e  the {@link ListSelectionEvent} to respond to
         */
        @SuppressWarnings("unchecked")
        public void valueChanged(ListSelectionEvent e)
            {
            if (e.getValueIsAdjusting())
                {
                return;
                }

            ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();

            if (!selectionModel.isSelectionEmpty())
                {
                nSelectedRow = selectionModel.getMinSelectionIndex();

                // get the service at the selected row, which is the first column
                Pair<String, String> selectedTopic = (Pair<String, String>) table.getValueAt(nSelectedRow, 0);

                if (!selectedTopic.equals(f_model.getSelectedTopic()))
                    {
                    String sSelectedTopic = selectedTopic.toString();
                    f_model.setSelectedTopic(selectedTopic);

                    f_txtSelectedTopic.setText(sSelectedTopic);
                    f_txtSelectedTopic.setToolTipText(sSelectedTopic);

                    f_tmodelSubscribers.setDataList(null);
                    f_tmodelSubscribers.fireTableDataChanged();

                    f_tmodelSubscriberGroups.setDataList(null);
                    f_tmodelSubscriberGroups.fireTableDataChanged();

                    m_topicData = null;
                    }
                }
            }

        /**
         * Re-select the last selected row.
         */
        public void updateRowSelection()
            {
            table.addRowSelectionInterval(nSelectedRow, nSelectedRow);
            }

        private ExportableJTable table;

        /**
         * The currently selected row.
         */
        private int nSelectedRow;
        }


    // ---- constants -------------------------------------------------------

    private static final long serialVersionUID = -761256904492412496L;

    // ----- data members ---------------------------------------------------

    /**
     * Total number of topics.
     */
    private final JTextField f_txtTotalTopics;

    /**
     * Total published count
     */
    private final JTextField f_txtTotalPublishedCount;

    /**
     * The {@link TopicTableModel} to display topic data.
     */
    protected final TopicTableModel f_tmodel;

    /**
     * The {@link TopicSubscriberTableModel} to display subscribers.
     */
    protected final TopicSubscriberTableModel f_tmodelSubscribers;

    /**
     * The {@link TopicSubscriberGroupTableModel} to display subscriber groups.
     */
    protected final TopicSubscriberGroupTableModel f_tmodelSubscriberGroups;

    /**
     * The topic data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_topicData = null;

    /**
     * The topic subscriber data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_topicSubscriberData = null;

    /**
    /**
     * The topic subscriber group data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_topicSubscriberGroupData = null;

    /**
     * The graph of topics rates.
     */
    private final transient SimpleXYChartSupport f_topicsRatesGraph;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * The {@link ExportableJTable} to use to display subscribers.
     */
    private final ExportableJTable f_tableSubscribers;

    /**
     * The {@link ExportableJTable} to use to display subscriber groups.
     */
    private final ExportableJTable f_tableSubscriberGroups;

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
    private long m_cLastPublishCount = -1L;

    /**
     * Selected Topic.
     */
    private final JTextField f_txtSelectedTopic;
    
    /**
     * The tabbed panel.
     */
    private final JTabbedPane f_pneTab;

    /**
     * The row selection listener.
     */
    private final SelectRowListSelectionListener f_listener;

    }
