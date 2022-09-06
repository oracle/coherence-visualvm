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
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.HotCachePerCacheTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.HotCacheTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HotCacheData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HotCachePerCacheData;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Map;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized elastic data statistics.
 *
 * @author nagaraju  2017.01.05
 * @since  12.2.1.3
 */
public class CoherenceHotCachePanel
    extends AbstractCoherencePanel
    {
    //--------- constructors ----------------------------------------------------------------------------
    /**
     * Create the layout for the {@link CoherenceHotCachePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceHotCachePanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // Create top level split pane for resizing
        JSplitPane pneSplit2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit2.setOpaque(false);
        // create another split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        f_tmodel = new HotCacheTableModel(VisualVMModel.DataType.HOTCACHE.getMetadata());

        final ExportableJTable table = new ExportableJTable(f_tmodel, model);
        table.setPreferredScrollableViewportSize(new Dimension(500, table.getRowHeight() * 5));

        table.setIntercellSpacing(new Dimension(6,3));
        table.setRowHeight(table.getRowHeight()+4);

        JScrollPane pneScroll = new JScrollPane(table);
        configureScrollPane(pneScroll,table);
        pneSplit.add(pneScroll);

        f_pneTabs = new JTabbedPane();
        f_pneTabs.setOpaque(false);

        populateTabs(f_pneTabs, getLocalizedText("LBL_none_selected"));

        pneSplit.add(f_pneTabs);
        pneSplit2.add(pneSplit);

        f_tpercacheModel = new HotCachePerCacheTableModel(VisualVMModel.DataType.HOTCACHE_PERCACHE.getMetadata());
        f_percachetable = new ExportableJTable(f_tpercacheModel, model);
        f_percachetable.setPreferredScrollableViewportSize(new Dimension(700, f_percachetable.getRowHeight() * 5));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_percachetable, HotCachePerCacheData.Count, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_percachetable, HotCachePerCacheData.Max, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_percachetable, HotCachePerCacheData.Min, new RenderHelper.IntegerRenderer());

        RenderHelper.setHeaderAlignment(table, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_percachetable, SwingConstants.CENTER);

        f_percachetable.setIntercellSpacing(new Dimension(6,3));
        f_percachetable.setRowHeight(f_percachetable.getRowHeight() + 4);
        JScrollPane pneScroll8 = new JScrollPane(f_percachetable);

        JSplitPane pnebottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pnebottom.setOpaque(false);
        pnebottom.add(pneScroll8);
        populateBottomPanel(pnebottom, null);

        pneSplit2.add(pnebottom);
        add(pneSplit2);

        f_rowSelectionDataModel = table.getSelectionModel();
        m_rowSelectionPercacheDataModel = f_percachetable.getSelectionModel();

        f_listener = new SelectRowListSelectionListener(table, f_pneTabs, f_percachetable, pnebottom);

        f_rowSelectionDataModel.addListSelectionListener(f_listener);
        m_rowSelectionPercacheDataModel.addListSelectionListener(f_listener);

        }

    /**
     * Populate the tabs on a change of hotcache member name.
     *
     * @param pneTabs  the {@link JTabbedPane} to update
     * @param sSelectedMember   the service name to display
     */
    private void populateTabs(JTabbedPane pneTabs, String sSelectedMember)
        {
        // remove any existing tabs
        int cTabs = pneTabs.getTabCount();

        for (int i = 0; i < cTabs; i++)
            {
            pneTabs.removeTabAt(0);
            }

        m_hotcacheGraph1 = GraphHelper.createHotcacheValueinNanosGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_ExecTimePerOp"), m_hotcacheGraph1.getChart());

        m_hotcacheGraph2 = GraphHelper.createHotcacheValueinNanosGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_ExecTimePerTr"), m_hotcacheGraph2.getChart());

        m_hotcacheGraph3 = GraphHelper.createHotcacheValueGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_InvPerOp"), m_hotcacheGraph3.getChart());

        m_hotcacheGraph4 = GraphHelper.createHotcacheValueinNanosGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_LastExecTimePerOp"), m_hotcacheGraph4.getChart());

        m_hotcacheGraph5 = GraphHelper.createHotcacheValueinMillisGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_LastOpRepLag"), m_hotcacheGraph5.getChart());

        m_hotcacheGraph6 = GraphHelper.createHotcacheValueinMillisGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_OpRepLag"), m_hotcacheGraph6.getChart());

        m_hotcacheGraph7 = GraphHelper.createHotcacheValueGraph(sSelectedMember);
        pneTabs.addTab(Localization.getLocalText("LBL_OpPerTr"), m_hotcacheGraph7.getChart());
        }

    /**
     * Create a graph and add it to bottompanel on change of CacheOperation.
     *
     * @param pnebottom  the {@link JSplitPane} to update
     * @param sSelectedHotcachePercacheOperation   the cacheoperation to display
     */
    private void populateBottomPanel(JSplitPane pnebottom, String sSelectedHotcachePercacheOperation)
        {
        //remove existing plot
        int lastLoc = pnebottom.getDividerLocation();
        if(pnebottom.getComponentCount()==3)
            {
            pnebottom.remove(2);
            }
        m_hotcacheGraph8 = GraphHelper.createHotcacheValueinNanosGraph(sSelectedHotcachePercacheOperation);
        pnebottom.setDividerLocation(lastLoc);
        pnebottom.add(m_hotcacheGraph8.getChart());
        }

    @Override
    public void updateGUI()
        {
        f_tmodel.fireTableDataChanged();
        fireTableDataChangedWithSelection(f_percachetable, f_tpercacheModel);

        if(f_model.getSelectedHotCacheMember() != null)
            {
            f_listener.updateRowSelection();
            }
        }

    @Override
    public void updateData()
        {
        m_hotcacheData = f_model.getData(VisualVMModel.DataType.HOTCACHE);
        if(m_hotcacheData != null)
            {
            f_tmodel.setDataList(m_hotcacheData);
            f_tmodel.fireTableDataChanged();
            }
        m_hotcachepercacheData = f_model.getData(VisualVMModel.DataType.HOTCACHE_PERCACHE);
        if(m_hotcachepercacheData != null)
            {
            f_tpercacheModel.setDataList(m_hotcachepercacheData);
            for (Map.Entry<Object, Data> entry : m_hotcacheData)
                {
                if (entry.getKey().equals(f_model.getSelectedHotCacheMember()))
                    {
                    if((long) entry.getValue().getColumn(HotCacheData.Max1) != Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph1,(double) entry.getValue().getColumn(HotCacheData.Mean1), (long) entry.getValue().getColumn(HotCacheData.Max1), (long) entry.getValue().getColumn(HotCacheData.Min1));
                        }
                    if((long) entry.getValue().getColumn(HotCacheData.Max2)!=Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph2,(double) entry.getValue().getColumn(HotCacheData.Mean2), (long) entry.getValue().getColumn(HotCacheData.Max2), (long) entry.getValue().getColumn(HotCacheData.Min2));
                        }
                    if((int) entry.getValue().getColumn(HotCacheData.Max3)!=Integer.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph3,(double) entry.getValue().getColumn(HotCacheData.Mean3), Integer.valueOf((int) entry.getValue().getColumn(HotCacheData.Max3)).longValue(), Integer.valueOf((int) entry.getValue().getColumn(HotCacheData.Min3)).longValue());
                        }
                    if((long) entry.getValue().getColumn(HotCacheData.Max4)!=Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph4,(double) entry.getValue().getColumn(HotCacheData.Mean4), (long) entry.getValue().getColumn(HotCacheData.Max4), (long) entry.getValue().getColumn(HotCacheData.Min4));
                        }
                    if((long) entry.getValue().getColumn(HotCacheData.Max5)!=Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph5,(double) entry.getValue().getColumn(HotCacheData.Mean5), (long) entry.getValue().getColumn(HotCacheData.Max5), (long) entry.getValue().getColumn(HotCacheData.Min5));
                        }
                    if((long) entry.getValue().getColumn(HotCacheData.Max6)!=Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph6, (double) entry.getValue().getColumn(HotCacheData.Mean6), (long) entry.getValue().getColumn(HotCacheData.Max6), (long) entry.getValue().getColumn(HotCacheData.Min6));
                        }
                    if((int) entry.getValue().getColumn(HotCacheData.Max7)!=Integer.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph7, (double) entry.getValue().getColumn(HotCacheData.Mean7), Integer.valueOf((int) entry.getValue().getColumn(HotCacheData.Max7)).longValue(), Integer.valueOf((int) entry.getValue().getColumn(HotCacheData.Min7)).longValue());
                        }
                    break;
                    }
                }
            if(f_model.getSelectedHotCachePerCacheOperation() != null)
                {
                for(Map.Entry<Object, Data> entry : m_hotcachepercacheData)
                    {
                    if(entry.getKey().equals(f_model.getSelectedHotCachePerCacheOperation()) && (long) entry.getValue().getColumn(HotCachePerCacheData.Max) != Long.MIN_VALUE)
                        {
                        GraphHelper.addValuesToHotcacheGraph(m_hotcacheGraph8, (double) entry.getValue().getColumn(HotCachePerCacheData.Mean), (long) entry.getValue().getColumn(HotCachePerCacheData.Max), (long) entry.getValue().getColumn(HotCachePerCacheData.Min));
                        break;
                        }
                    }
                }
            }
        }

    /**
     * Inner class to change the pnetabs and percachetable according to row selection in table.
     * Also to change the CacheOperation graph of bottompanel according to row selection in percachetable.
     */
    private class SelectRowListSelectionListener
        implements ListSelectionListener
        {
        // ----- constructors -----------------------------------------------

        /**
         * Create a new listener.
         *
         * @param table   the {@link ExportableJTable} that has different hotcache members in different rows.
         * @param pneTabs the {@link JTabbedPane} that changes acc hotcache member selected.
         * @param percachetable the {@link ExportableJTable} that changes according to hotcache member selected.
         * @param pnebottom the {@link JSplitPane} that contains percachetable and a graph.
         */
        public SelectRowListSelectionListener(ExportableJTable table, JTabbedPane pneTabs, ExportableJTable percachetable, JSplitPane pnebottom)
            {
            this.f_table = table;
            this.f_pneTabs = pneTabs;
            this.f_percachetable = percachetable;
            this.f_pnebottom = pnebottom;
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
            //select the hotcache member table
            if (selectionModel == f_rowSelectionDataModel && !selectionModel.isSelectionEmpty())
                {
                //get the selected row index
                m_nSelectedRow = selectionModel.getMinSelectionIndex();
                // get the member at the selected row, which is the first column
                String sSelectedMember = (String) f_table.getValueAt(m_nSelectedRow, 0);

                if(!sSelectedMember.equals(f_model.getSelectedHotCacheMember()))
                    {
                    //set selected hotcache member
                    f_model.setSelectedHotCacheMember(sSelectedMember);
                    //set the cacheOperation to null as cache operation is not selected yet
                    f_model.setSelectedHotCachePerCacheOperation(null);
                    f_model.eraseHotCachePerCacheData();
                    f_tpercacheModel.setDataList(null);
                    f_tpercacheModel.fireTableDataChanged();
                    populateTabs(f_pneTabs, sSelectedMember);
                    populateBottomPanel(f_pnebottom, null);
                    }
                }
            //select the percache operation table
            else if(selectionModel == m_rowSelectionPercacheDataModel && !selectionModel.isSelectionEmpty())
                {
                //get the selected row index
                m_nPercacheSelectedRow = selectionModel.getMinSelectionIndex();
                String sSelectedHotcachePercacheOperation = (String) f_percachetable.getValueAt(m_nPercacheSelectedRow, 0);
                if(!sSelectedHotcachePercacheOperation.equals(f_model.getSelectedHotCachePerCacheOperation()))
                    {
                    // set the selected cache Operation
                    f_model.setSelectedHotCachePerCacheOperation(sSelectedHotcachePercacheOperation);
                    populateBottomPanel(f_pnebottom, sSelectedHotcachePercacheOperation);
                    }
                }
            }

        /**
         * Re-select the last selected row.
         */
        public void updateRowSelection()
            {
            f_table.addRowSelectionInterval(m_nSelectedRow, m_nSelectedRow);
            }

        /**
         * Hotcache member table.
         */
        private final ExportableJTable f_table;

        /**
         * TabbedPane in the middle panel.
         */
        private final JTabbedPane f_pneTabs;

        /**
         * Hotcache percache table.
         */
        private final ExportableJTable f_percachetable;

        /**
         * Bottom Panel.
         */
        private final JSplitPane f_pnebottom;

        /**
         * The currently selected row index in hotcache table.
         */
        private int m_nSelectedRow;

        /**
         * The currently selected row index in hotcache percache table.
         */
        private int m_nPercacheSelectedRow;
        }

    // ----- data members ---------------------------------------------------
    /**
     * The {@link HotCacheTableModel} to display hotcache data.
     */
    protected final HotCacheTableModel f_tmodel;

    /**
     * The {@link HotCachePerCacheTableModel} to display hotcache percache data.
     */
    protected final HotCachePerCacheTableModel f_tpercacheModel;

    /**
     * the {@link ExportableJTable} to use to display hotcachepercache data.
     */
    protected final ExportableJTable f_percachetable;

    /**
     * The statistics hotcache data retrieved from the {@link VisualVMModel}.
     */
    private transient java.util.List<Map.Entry<Object, Data>> m_hotcacheData;

    /**
     * The statistics hotcachepercache data retrieved from the {@link VisualVMModel}.
     */
    private transient java.util.List<Map.Entry<Object, Data>> m_hotcachepercacheData;

    /**
     * The graph of Execution Time Per Operation statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph1;

    /**
     * The graph of Execution Time Per Transaction Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph2;

    /**
     * The graph of Number Of Invocations Per Operation Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph3;

    /**
     * The graph of Last ExecutionTime Per Operation Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph4;

    /**
     * The graph of Last Operation ReplicationLag Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph5;

    /**
     * The graph of Operation ReplicationLag Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph6;

    /**
     * The graph of Number Of Operations Per Transaction Statistics.
     */
    private transient SimpleXYChartSupport m_hotcacheGraph7;

    /**
     * The graph of PerCacheOperation Statistics
     */
    private transient SimpleXYChartSupport m_hotcacheGraph8;

    /**
     * The tabbed panel.
     */
    private JTabbedPane f_pneTabs;

    /**
     * The row selection listener.
     */
    private final transient SelectRowListSelectionListener f_listener;

    /**
     * The {@link ListSelectionModel} of table.
     */
    private final transient ListSelectionModel f_rowSelectionDataModel;

    /**
     * The {@link ListSelectionModel} of percachetable.
     */
    private ListSelectionModel m_rowSelectionPercacheDataModel;
    }
