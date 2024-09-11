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
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.JMXRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.SeparatorMenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.CacheDetailTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.CacheStorageManagerTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.CacheTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.CacheViewTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheFrontDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Tuple;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheStorageManagerData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.ViewData;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view summarized cache
 * size statistics.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CoherenceCachePanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceCachePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceCachePanel(VisualVMModel model)
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

        f_txtTotalCaches = getTextField(5);
        pnlHeader.add(getLocalizedLabel("LBL_total_caches", f_txtTotalCaches));
        pnlHeader.add(f_txtTotalCaches);

        f_txtTotalMemory = getTextField(10);
        pnlHeader.add(getLocalizedLabel("LBL_total_data", f_txtTotalMemory));
        pnlHeader.add(f_txtTotalMemory);
        f_txtTotalMemory.setToolTipText(getLocalizedText("TTIP_cache_size"));

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.setOpaque(false);

        // create any table models required
        f_tmodel            = new CacheTableModel(VisualVMModel.DataType.CACHE.getMetadata());
        f_tmodelDetail      = new CacheDetailTableModel(VisualVMModel.DataType.CACHE_DETAIL.getMetadata());
        f_tmodelFrontDetail = new CacheDetailTableModel(VisualVMModel.DataType.CACHE_FRONT_DETAIL.getMetadata());
        f_tmodelStorage     = new CacheStorageManagerTableModel(VisualVMModel.DataType.CACHE_STORAGE_MANAGER.getMetadata());
        f_tmodelViews       = new CacheViewTableModel(VisualVMModel.DataType.VIEW.getMetadata());

        final ExportableJTable table = new ExportableJTable(f_tmodel, model);
        f_tableDetail                = new ExportableJTable(f_tmodelDetail, model);
        f_tableFrontDetail           = new ExportableJTable(f_tmodelFrontDetail, model);
        f_tableStorage               = new ExportableJTable(f_tmodelStorage, model);
        f_tableViews                 = new ExportableJTable(f_tmodelViews, model);

        table.setPreferredScrollableViewportSize(new Dimension(500, table.getRowHeight() * 5));
        f_tableDetail.setPreferredScrollableViewportSize(new Dimension(500, f_tableDetail.getRowHeight() * 3));
        f_tableFrontDetail.setPreferredScrollableViewportSize(new Dimension(500, f_tableFrontDetail.getRowHeight() * 3));
        f_tableStorage.setPreferredScrollableViewportSize(new Dimension(500, f_tableStorage.getRowHeight() * 3));
        f_tableViews.setPreferredScrollableViewportSize(new Dimension(500, f_tableStorage.getRowHeight() * 3));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(table, CacheData.CACHE_NAME, new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(table, CacheData.SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(table, CacheData.AVG_OBJECT_SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(table, CacheData.MEMORY_USAGE_MB, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(table, CacheData.MEMORY_USAGE_BYTES, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(table, CacheData.UNIT_CALCULATOR, new RenderHelper.UnitCalculatorRenderer());

        RenderHelper.setHeaderAlignment(table, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_tableDetail, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_tableFrontDetail, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_tableStorage, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_tableViews, SwingConstants.CENTER);

        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.CACHE_HITS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.CACHE_MISSES, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.MEMORY_BYTES, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.TOTAL_GETS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.TOTAL_PUTS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.SIZE, new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(f_tableDetail, CacheDetailData.HIT_PROBABILITY,
                                       new RenderHelper.CacheHitProbabilityRateRenderer());

        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.CACHE_HITS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.CACHE_MISSES, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.TOTAL_GETS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.TOTAL_PUTS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.SIZE, new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(f_tableFrontDetail, CacheFrontDetailData.HIT_PROBABILITY,
                                       new RenderHelper.CacheHitProbabilityRateRenderer());

        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.LOCKS_GRANTED,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.LOCKS_PENDING,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.LISTENER_KEY_COUNT,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.LISTENER_FILTER_COUNT,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.MAX_QUERY_DURATION,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.OPTIMIZED_QUERY_AVG,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.MAX_QUERY_DESCRIPTION,
                                       new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.INDEX_TOTAL_UNITS,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableStorage, CacheStorageManagerData.INDEXING_TOTAL_MILLIS,
                                       new RenderHelper.IntegerRenderer());

        RenderHelper.setColumnRenderer(f_tableViews, ViewData.NODE_ID, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableViews, ViewData.SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableViews, ViewData.RECONNECT_INTERVAL, new RenderHelper.IntegerRenderer());

        table.setIntercellSpacing(new Dimension(6, 3));
        table.setRowHeight(table.getRowHeight() + 4);

        table.setMenuOptions(
            new MenuOption[]{
                    new ShowHeatMapMenuOption(model, m_requestSender, table, ShowHeatMapMenuOption.TYPE_SIZE),
                    new ShowHeatMapMenuOption(model, m_requestSender, table, ShowHeatMapMenuOption.TYPE_MEMORY),
                    new SeparatorMenuOption(model, m_requestSender, table),
                    new InvokeCacheOperationMenuOpen(model, m_requestSender, table, TRUNCATE),
                    new InvokeCacheOperationMenuOpen(model, m_requestSender, table, CLEAR)
            });

        setTablePadding(f_tableViews);
        setTablePadding(f_tableDetail);
        f_tableDetail.setMenuOptions(new MenuOption[] {new ShowDetailMenuOption(model, f_tableDetail, SELECTED_CACHE)});

        setTablePadding(f_tableFrontDetail);
        f_tableFrontDetail.setMenuOptions(new MenuOption[] {new ShowDetailMenuOption(model, f_tableFrontDetail, SELECTED_FRONT_CACHE)});

        setTablePadding(f_tableStorage);
        
        MenuOption showDetailMenuOption = new ShowDetailMenuOption(model, f_tableStorage, SELECTED_STORAGE);
        MenuOption showIndexInfoMenuOption = new ShowIndexInfoMenuOption(model, m_requestSender, f_tableStorage);

        if (model.getRequestSender() instanceof JMXRequestSender)
            {
            // add json and csv options
            f_tableStorage.setMenuOptions(new MenuOption[] {
                showDetailMenuOption,
                showIndexInfoMenuOption,
                new ShowPartitionStatsMenuOption(model, m_requestSender, f_tableStorage, "json"),
                new ShowPartitionStatsMenuOption(model, m_requestSender, f_tableStorage, "csv")
               });
            }
        else
            {
            // generic json option
            f_tableStorage.setMenuOptions(new MenuOption[] {
                showDetailMenuOption,
                showIndexInfoMenuOption,
                new ShowPartitionStatsMenuOption(model, m_requestSender, f_tableStorage, "json")
               });
            }

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane        = new JScrollPane(table);
        JScrollPane scrollPaneDetail  = new JScrollPane(f_tableDetail);
        JScrollPane scrollPaneStorage = new JScrollPane(f_tableStorage);

        f_scrollPaneViews       = new JScrollPane(f_tableViews);
        f_scrollPaneFrontDetail = new JScrollPane(f_tableFrontDetail);

        configureScrollPane(scrollPane, table);
        configureScrollPane(scrollPaneDetail, f_tableDetail);
        configureScrollPane(f_scrollPaneFrontDetail, f_tableFrontDetail);
        configureScrollPane(f_scrollPaneViews, f_tableViews);
        configureScrollPane(scrollPaneStorage, f_tableStorage);

        scrollPane.setOpaque(false);
        scrollPaneDetail.setOpaque(false);
        scrollPaneStorage.setOpaque(false);
        f_scrollPaneFrontDetail.setOpaque(false);
        f_scrollPaneViews.setOpaque(false);

        pnlTop.add(scrollPane, BorderLayout.CENTER);
        pneSplit.add(pnlTop);

        JPanel bottomPanel       = new JPanel(new BorderLayout());
        JPanel detailHeaderPanel = new JPanel();
        bottomPanel.setOpaque(false);
        detailHeaderPanel.setOpaque(false);

        f_txtSelectedCache = getTextField(30, SwingConstants.LEFT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_selected_service_cache", f_txtSelectedCache));
        detailHeaderPanel.add(f_txtSelectedCache);

        f_txtMaxQueryDuration = getTextField(5);
        detailHeaderPanel.add(getLocalizedLabel("LBL_max_query_millis", f_txtMaxQueryDuration));
        detailHeaderPanel.add(f_txtMaxQueryDuration);

        f_txtMaxQueryDescription = getTextField(30, SwingConstants.LEFT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_max_query_desc", f_txtMaxQueryDescription));
        detailHeaderPanel.add(f_txtMaxQueryDescription);

        bottomPanel.add(detailHeaderPanel, BorderLayout.PAGE_START);
        bottomPanel.setOpaque(false);

        f_pneTab = new JTabbedPane();
        f_pneTab.setOpaque(false);

        f_pneTab.addTab(getLocalizedText("TAB_cache"), scrollPaneDetail);
        f_pneTab.addTab(getLocalizedText("TAB_storage"), scrollPaneStorage);

        bottomPanel.add(f_pneTab, BorderLayout.CENTER);

        pneSplit.add(bottomPanel);

        add(pneSplit);

        // add a listener for the selected row
        ListSelectionModel rowSelectionModel = table.getSelectionModel();

        f_listener = new SelectRowListSelectionListener(table);
        rowSelectionModel.addListSelectionListener(f_listener);
        }

    // ---- AbstractCoherencePanel methods ----------------------------------

    @Override
    public void updateData()
        {
        m_cacheData            = f_model.getData(VisualVMModel.DataType.CACHE);
        m_cacheDetailData      = f_model.getData(VisualVMModel.DataType.CACHE_DETAIL);
        m_cacheFrontDetailData = f_model.getData(VisualVMModel.DataType.CACHE_FRONT_DETAIL);
        m_cacheStorageData     = f_model.getData(VisualVMModel.DataType.CACHE_STORAGE_MANAGER);
        m_viewData             = f_model.getData(VisualVMModel.DataType.VIEW);

        // zero out memory if the selected cache is FIXED unit calculator
        Tuple selectedCache = f_model.getSelectedCache();
        if (selectedCache != null)
            {
            boolean isFixed = false;
            String  sCache  = selectedCache.toString();
            // find the cacheData for this cache
            for (Entry<Object, Data> entry : m_cacheData)
                {
                if (entry.getKey().toString().equals(sCache) && entry.getValue().getColumn(CacheData.UNIT_CALCULATOR).equals("FIXED"))
                    {
                    isFixed = true;
                    break;
                    }
                }

            if (isFixed)
                {
                List<Entry<Object, Data>> tempList = new ArrayList<>();

                if (m_cacheDetailData != null)
                    {
                    // zero out the values for memory and update the list
                    for (Entry<Object, Data> entry : m_cacheDetailData)
                        {
                        entry.getValue().setColumn(CacheDetailData.MEMORY_BYTES, Integer.valueOf(0));
                        tempList.add(entry);
                        }
                    }

                m_cacheDetailData = tempList;
                }
            }

        f_tmodel.setDataList(m_cacheData);
        f_tmodelDetail.setDataList(m_cacheDetailData);
        f_tmodelStorage.setDataList(m_cacheStorageData);

        // check if near cache is configured
        m_isNearCacheConfigured = m_cacheFrontDetailData != null && !m_cacheFrontDetailData.isEmpty();

        // check if view cache is configured
        m_isViewCacheConfigured = m_viewData != null && !m_viewData.isEmpty();

        if (m_isNearCacheConfigured)
            {
            f_tmodelFrontDetail.setDataList(m_cacheFrontDetailData);
            }

        if (m_isViewCacheConfigured)
            {
            f_tmodelViews.setDataList(m_viewData);
            }

        // if we are currently displaying the heat map then update it
        if (m_currentHeatMap != null)
            {
            m_currentHeatMap.updateData();
            m_currentHeatMap.m_pnlHeatMap.repaint();
            }
        }

    @Override
    public void updateGUI()
        {
        // If the near cache is configured, we have to make sure the front cache detail tab
        // is added. Otherwise, we have to remove it.
        if (m_isNearCacheConfigured)
            {
            if (!m_isFrontCacheTabAdded && f_pneTab != null)
                {
                f_pneTab.addTab(getLocalizedText("TAB_front_cache_detail"), f_scrollPaneFrontDetail);
                m_isFrontCacheTabAdded = true;
                }
            }
        else
            {
            if (m_isFrontCacheTabAdded && f_pneTab != null)
                {
                f_pneTab.remove(f_scrollPaneFrontDetail);
                m_isFrontCacheTabAdded = false;
                }
            }

        if (m_isViewCacheConfigured)
           {
            if (!m_isViewTabAdded && f_pneTab != null)
                {
                f_pneTab.addTab(getLocalizedText("TAB_view_caches"), f_scrollPaneViews);
                m_isViewTabAdded = true;
                }
            }
        else
            {
            if (m_isViewTabAdded && f_pneTab != null)
                {
                f_pneTab.remove(f_scrollPaneViews);
                m_isViewTabAdded = false;
                }
            }

        if (m_cacheData != null)
            {
            f_txtTotalCaches.setText(String.format("%5d", m_cacheData.size()));

            float cTotalCacheSize = 0.0f;

            for (Entry<Object, Data> entry : m_cacheData)
                {
                cTotalCacheSize += Float.valueOf((Integer) entry.getValue().getColumn(CacheData.MEMORY_USAGE_MB));
                }

            f_txtTotalMemory.setText(String.format("%,10.2f", cTotalCacheSize));
            }
        else
            {
            f_txtTotalCaches.setText("0");
            f_txtTotalMemory.setText(String.format("%,10.2f", 0.0));
            }

        Tuple selectedCache = f_model.getSelectedCache();

        if (selectedCache == null)
            {
            f_txtSelectedCache.setText("");
            }
        else
            {
            f_txtSelectedCache.setText(selectedCache.toString());
            }

        if (m_cacheStorageData != null)
            {
            long   lMaxQueryMillis      = 0L;
            String sMaxQueryDescription = "";

            for (Entry<Object, Data> entry : m_cacheStorageData)
                {
                Object oValue  = entry.getValue().getColumn(CacheStorageManagerData.MAX_QUERY_DURATION);
                long lMaxValue = oValue != null ? (Long) oValue : 0L;

                if (lMaxValue > lMaxQueryMillis)
                    {
                    lMaxQueryMillis      = lMaxValue;
                    oValue               = entry.getValue().getColumn(CacheStorageManagerData.MAX_QUERY_DESCRIPTION);
                    sMaxQueryDescription = oValue != null ? (String) oValue : "";
                    }
                }

            f_txtMaxQueryDescription.setText(sMaxQueryDescription);
            f_txtMaxQueryDescription.setToolTipText(sMaxQueryDescription);
            f_txtMaxQueryDuration.setText(String.format("%5d", lMaxQueryMillis));
            }

        f_tmodel.fireTableDataChanged();

        fireTableDataChangedWithSelection(f_tableDetail, f_tmodelDetail);
        fireTableDataChangedWithSelection(f_tableFrontDetail, f_tmodelFrontDetail);
        fireTableDataChangedWithSelection(f_tableStorage, f_tmodelStorage);
        fireTableDataChangedWithSelection(f_tableViews, f_tmodelViews);

        if (f_model.getSelectedCache() != null)
            {
            f_listener.updateRowSelection();
            }
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
            this.m_table = table;
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
                m_nSelectedRow = selectionModel.getMinSelectionIndex();

                // get the service at the selected row, which is the first column
                Pair<String, String> selectedCache = (Pair<String, String>) m_table.getValueAt(m_nSelectedRow, 0);

                if (!selectedCache.equals(f_model.getSelectedCache()))
                    {
                    String sSelectedCache = selectedCache.toString();
                    f_model.setSelectedCache(selectedCache);
                    f_txtSelectedCache.setText(sSelectedCache);
                    f_txtSelectedCache.setToolTipText(sSelectedCache);
                    f_tmodelDetail.setDataList(null);
                    f_tmodelDetail.fireTableDataChanged();

                    f_tmodelFrontDetail.setDataList(null);
                    f_tmodelFrontDetail.fireTableDataChanged();

                    f_tmodelViews.setDataList(null);
                    f_tmodelViews.fireTableDataChanged();

                    f_txtMaxQueryDescription.setText("");
                    f_txtMaxQueryDuration.setText("");
                    m_cacheData = null;

                    // force immediate refresh
                    f_model.setImmediateRefresh(true);
                    }
                }
            }

        /**
         * Re-select the last selected row.
         */
        public void updateRowSelection()
            {
            m_table.addRowSelectionInterval(m_nSelectedRow, m_nSelectedRow);
            }

        private ExportableJTable m_table;

        /**
         * The currently selected row.
         */
        private int m_nSelectedRow;
        }

    /**
     * Right-click option to show index information.
     */
    protected class ShowIndexInfoMenuOption
        extends AbstractMenuOption
        {

        // ----- constructors -----------------------------------------------

       /**
         * Create a new menu option for displaying index information.
         *
         * @param model          the {@link VisualVMModel} to get collected data from
         * @param requestSender  the {@link MBeanServerConnection} to perform additional queries
         * @param jtable         the {@link ExportableJTable} that this applies to
         */
       public ShowIndexInfoMenuOption(VisualVMModel model, RequestSender requestSender,
                                      ExportableJTable jtable)
           {
           super(model, requestSender, jtable);
           f_sMenuItem = getLocalizedText("LBL_index_info");
           }

       // ----- AbstractMenuOption methods ---------------------------------

       @Override
       public String getMenuItem()
           {
           return f_sMenuItem;
           }

       @Override
       public void actionPerformed(ActionEvent e)
           {
           int nRow = getSelectedRow();
           Set<Integer> setNodes = new HashSet<>();
           boolean fIndexBuildAvailable = false;
           Pair<String, String> selectedCache = f_model.getSelectedCache();

           try
               {
               StringBuilder sb = new StringBuilder("Index details for: " + selectedCache)
                       .append('\n').append('\n');

               long  cMillisTotal     = 0;
               long  cCount           = 0;
               float cMillisAverage   = 0.0f;
               long  cIndexTotalUnits = 0L;
               long  cMaxUnits        = 0L;
               long  cMaxMillis       = 0L;

               for (Map.Entry<Object, Data> entry : m_cacheStorageData)
                   {
                   cCount++;
                   Object oValue     = entry.getValue().getColumn(CacheStorageManagerData.INDEXING_TOTAL_MILLIS);
                   long   cUnits     = (Long) entry.getValue().getColumn(CacheStorageManagerData.INDEX_TOTAL_UNITS);
                   long   cMillis    = oValue == null ? 0L : (Long) oValue;
                   fIndexBuildAvailable = oValue != null;
                   
                   cIndexTotalUnits += cUnits;
                   cMillisTotal     += cMillis;

                   if (cUnits > cMaxUnits)
                       {
                       cMaxUnits = cUnits;
                       }

                   if (cMillis > cMaxMillis)
                       {
                       cMaxMillis = cMillis;
                       }

                   setNodes.add((Integer) entry.getValue().getColumn(CacheStorageManagerData.NODE_ID));
                   }

               if (cCount != 0)
                   {
                   cMillisAverage = cMillisTotal * 1.0f / cCount;
                   sb.append(getLocalizedText("LBL_index_units_bytes"))
                     .append(": ")
                     .append(getMemoryFormat(cIndexTotalUnits))
                     .append('\n')
                     .append(getLocalizedText("LBL_index_units_mb"))
                     .append(": ")
                     .append(getMemoryFormat(cIndexTotalUnits / GraphHelper.MB))
                     .append('\n');

                   if (fIndexBuildAvailable)
                       {
                       sb.append(getLocalizedText("LBL_average_indexing_total_millis"))
                          .append(": ")
                          .append(String.format("%,.3f", cMillisAverage))
                          .append('\n')
                          .append(getLocalizedText("LBL_max_indexing_total_millis"))
                          .append(": ")
                          .append(String.format("%,d", cMaxMillis))
                          .append("\n");
                        }
                   
                   // retrieve the first index info which will be the same on all nodes
                   for (int nNode : setNodes)
                       {
                       String sQuery = "Coherence:type=StorageManager,service=" +
                                       getServiceName(selectedCache.getX()) + ",cache=" + selectedCache.getY() +
                                       ",nodeId=" + nNode + getDomainPartitionKey(selectedCache.getX()) + ",*";

                       Set<ObjectName> setObjects = m_requestSender.getCompleteObjectName(new ObjectName(sQuery));

                       for (Iterator<ObjectName> iter = setObjects.iterator(); iter.hasNext(); )
                           {
                           ObjectName objName = iter.next();

                           List<Attribute> lstAttr = m_requestSender.getAllAttributes(objName);

                           for (Attribute attr : lstAttr)
                              {
                              if ("IndexInfo".equalsIgnoreCase(attr.getName()))
                                  {
                                  // HttpRequestSender responds with a String and JMX with a String[]
                                  String sValue = m_requestSender instanceof HttpRequestSender
                                                  ? (String) attr.getValue()
                                                  : String.join("\n", (String[]) attr.getValue());
                                  sb.append("\nNode: ")
                                    .append(nNode)
                                    .append(" IndexInfo\n")
                                    .append(sValue);
                                  break;
                                  }
                              }
                           }
                       }
                   }

               showMessageDialog(Localization.getLocalText("LBL_index_info"),
                                                                sb.toString(), JOptionPane.INFORMATION_MESSAGE);
               }
           catch (Exception ee)
               {
               showMessageDialog(Localization.getLocalText("LBL_error"),
                                ee.getMessage(), JOptionPane.ERROR_MESSAGE);
               }

           }

       // ----- data members ------------------------------------------------

        /**
         * Menu option description.
         */
        private final String f_sMenuItem;
       }

    /**
     * Right-click option to show index information.
     */
    protected class ShowPartitionStatsMenuOption
        extends AbstractMenuOption
        {

        // ----- constructors -----------------------------------------------

       /**
         * Create a new menu option for displaying partition stats information.
         *
         * @param model          the {@link VisualVMModel} to get collected data from
         * @param requestSender  the {@link MBeanServerConnection} to perform additional queries
         * @param jtable         the {@link ExportableJTable} that this applies to
         * @param sFormat        the format of json or csv
         */
       public ShowPartitionStatsMenuOption(VisualVMModel model, RequestSender requestSender,
                                      ExportableJTable jtable, String sFormat)
           {
           super(model, requestSender, jtable);
           f_sFormat     = sFormat;
           String sLabel = "csv".equals(f_sFormat) ? "LBL_cache_partition_stats_csv" : "LBL_cache_partition_stats_json";
           f_sMenuItem = getLocalizedText(sLabel);
           }

       // ----- AbstractMenuOption methods ---------------------------------

       @Override
       public String getMenuItem()
           {
           return f_sMenuItem;
           }

       @Override
       public void actionPerformed(ActionEvent e)
           {
           String sResult = "unknown";
           Pair<String, String> selectedCache = f_model.getSelectedCache();

           try
               {
               sResult = m_requestSender.invokeReportPartitionsStatsOperation(selectedCache.getX(), selectedCache.getY(), f_sFormat);
               showMessageDialog(Localization.getLocalText("LBL_cache_partition_stats"),
                                                                sResult, JOptionPane.INFORMATION_MESSAGE);
               }
           catch (Exception ee)
               {
               showMessageDialog(Localization.getLocalText("LBL_error"),
                                ee.getMessage(), JOptionPane.ERROR_MESSAGE);
               }
           }

        @Override
        public boolean equals(Object o)
            {
            if (this == o)
                {
                return true;
                }

            if (o == null || getClass() != o.getClass())
                {
                return false;
                }

            ShowPartitionStatsMenuOption that = (ShowPartitionStatsMenuOption) o;

            if (!Objects.equals(f_sMenuItem, that.f_sMenuItem))
                {
                return false;
                }
               return Objects.equals(f_sFormat, that.f_sFormat);
            }

            @Override
            public int hashCode()
                {
                int result = f_sMenuItem != null ? f_sMenuItem.hashCode() : 0;
                result = 31 * result + (f_sFormat != null ? f_sFormat.hashCode() : 0);
                return result;
                }

            // ----- data members ------------------------------------------------

       /**
        * Menu option description.
        */
       private final String f_sMenuItem;

       /**
        * output format.
        */
       private final String f_sFormat;
       }

    /**
     * Menu option to invoke clear or truncate against a cache. Only available in most recent versions of Coherence.
     */
    protected class InvokeCacheOperationMenuOpen
           extends AbstractMenuOption
       {
       // ----- constructors -----------------------------------------------

       public InvokeCacheOperationMenuOpen(VisualVMModel model, RequestSender requestSender,
                                           ExportableJTable jtable, String sOperation)
            {
            super(model, requestSender, jtable);
            f_sOperation = sOperation;
            }

       @Override
       public String getMenuItem()
           {
           return getLocalizedText(f_sOperation.equals(TRUNCATE) ? "LBL_truncate" : "LBL_clear");
           }

       @Override
       public void actionPerformed(ActionEvent e)
           {
           int nRow = getSelectedRow();

           if (nRow == -1)
               {
               DialogHelper.showInfoDialog(Localization.getLocalText("LBL_must_select_row"));
               }
           else
               {
               Pair<String, String> selectedCache = f_model.getSelectedCache();
               String sQuestion = Localization.getLocalText("LBL_confirm_cache_operation", f_sOperation, selectedCache.toString());

                if (!DialogHelper.showConfirmDialog(sQuestion))
                    {
                    return;
                    }
               try
                   {
                   m_requestSender.invokeStorageManagerOperation(selectedCache.getX(), selectedCache.getY(), f_sOperation);
                   showMessageDialog(Localization.getLocalText("LBL_result"), Localization.getLocalText("LBL_cache_operation_completed", f_sOperation),
                                          JOptionPane.INFORMATION_MESSAGE);
                   }
               catch (Exception ee)
                   {
                   showMessageDialog(Localization.getLocalText("ERR_cannot_run_cache_operation", selectedCache.toString()),
                                 ee.getMessage() + "\n" + ee.getCause(), JOptionPane.ERROR_MESSAGE);
                   }
               }
           }

           // ----- data members ------------------------------------------------

           private final String f_sOperation;
       }

    /**
     * A menu option to display a heat map for the cache sizes or
     * primary storage used.
     */
    protected class ShowHeatMapMenuOption
            extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * Create a new menu option for displaying heat map.
         *
         * @param model          the {@link VisualVMModel} to get collected data from
         * @param requestSender  the {@link MBeanServerConnection} to perform additional queries
         * @param jtable         the {@link ExportableJTable} that this applies to
         * @param nType          the type of the heat map either SIZE or MEMORY
         */
        public ShowHeatMapMenuOption (VisualVMModel model, RequestSender requestSender,
                                      ExportableJTable jtable, int nType)
            {
            super(model, requestSender, jtable);
            f_nType = nType;

            f_sMenuItem  = getLocalizedText(nType == TYPE_SIZE ? "LBL_size_heat_map"       : "LBL_memory_heat_map");
            f_sTitle     = getLocalizedText(nType == TYPE_SIZE ? "LBL_title_size_heat_map" : "LBL_title_memory_heat_map");
            }

        // ----- AbstractMenuOption methods ---------------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
           {
           return f_sMenuItem;
           }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed (ActionEvent e)
            {
            if (m_cacheData != null && !m_cacheData.isEmpty())
                {
                updateData();

                if (m_cTotal == 0L)
                    {
                    DialogHelper.showInfoDialog(getLocalizedText("LBL_no_caches"));
                    }
                else
                    {
                    JPanel pnlHeatMap = new HeatMapJPanel();

                    try
                        {
                        m_currentHeatMap = this;
                        m_pnlHeatMap     = pnlHeatMap;
                        JOptionPane.showMessageDialog(null, pnlHeatMap,
                                f_sTitle,
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    finally
                        {
                        m_currentHeatMap = null;
                        m_pnlHeatMap     = null;
                        }
                    }
                }
            else
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_no_data"));
                }
            }

        /**
         * Update the data for the heat map, can be called by choosing the
         * right-click or by regular refresh if the JPanel is visible.
         */
        public synchronized void updateData()
            {
            // build a linked list with a Pair<X,Y> where X = cache name (which is Pair<String, String>) and
            // Y is the count of size or memory
            f_listValues.clear();
            m_cTotal = 0L;

            for (Entry<Object, Data> entry : m_cacheData)
                {
                long cValue;
                if (f_nType == TYPE_SIZE)
                    {
                    cValue = (long) ((Integer) entry.getValue().getColumn(CacheData.SIZE));
                    }
                else
                    {
                    cValue = Long.parseLong(entry.getValue().getColumn(CacheData.MEMORY_USAGE_BYTES).toString());
                    }

                m_cTotal += cValue;
                Pair<Pair<String, String>, Long> cache = new Pair<>((Pair<String, String>) entry.getValue().getColumn(CacheData.CACHE_NAME), cValue);

                if (f_listValues.isEmpty())
                    {
                    f_listValues.add(cache);
                    }
                else
                    {
                    int nLocation = 0;
                    boolean fAdded = false;

                    // Find where the value is in the list
                    Iterator<Pair<Pair<String, String>, Long>> iter = f_listValues.iterator();
                    while (iter.hasNext())
                        {
                        Pair<Pair<String, String>, Long> entryHeatMap = iter.next();
                        if (entryHeatMap.getY() <= cache.getY())
                            {
                            // add new value at the current position
                            f_listValues.add(nLocation, cache);
                            fAdded = true;
                            break;
                            }
                        else
                            {
                            // value must be added at least after this one
                            nLocation++;
                            }
                        }

                    // if we have not added then add to end of list
                    if (!fAdded)
                        {
                        f_listValues.addLast(cache);
                        }
                    }
                }
            }

        // ----- inner classes ----------------------------------------------

        /**
         * Extension of JPanel to display the HeatMap.
         */
        protected class HeatMapJPanel extends JPanel
            {
            /**
             * Construct a new JPanel for the heap map.
             */
            public HeatMapJPanel()
                {
                super();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int nWidth           = (int) (screenSize.getWidth()  * 0.5);
                int nHeight          = (int) (screenSize.getHeight() * 0.5);
                AbstractMenuOption.setResizable(this);

                setPreferredSize(new Dimension(nWidth, nHeight));

                // add mouse listeners to display tooltips for cache names
                addMouseMotionListener(new MouseMotionListener()
                    {
                    @Override
                    public void mouseDragged (MouseEvent e)
                        {
                        }

                    @Override
                    public void mouseMoved (MouseEvent e)
                       {
                       mapToolTips.forEach( (k,v) ->
                           {
                           if (k.contains(e.getPoint()))
                               {
                               setToolTipText(v);
                               }
                           });
                       ToolTipManager.sharedInstance().mouseMoved(e);
                       }
                    });
                }

            @Override
            public void paintComponent(Graphics g)
                {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setFont(f_font);
                m_nRange = MIN_RANGE;

                int nMapWidth  = getWidth()  - 10;
                int nMapHeight = getHeight() - 10;
                int nStartX = 5;
                int nStartY = 5;
                int nLastStartX;
                int nLastStartY;

                g2.clearRect(nStartX, nStartY, nMapWidth, nMapHeight);
                g2.draw(new Rectangle2D.Double(nStartX, nStartY, nMapWidth,
                        nMapHeight));

                float   nTotalPercentLeft = 100.0f;
                boolean fVerticalSplit    = true;
                mapToolTips.clear();

                // iterate through all values which will start with largest
                Iterator<Pair<Pair<String, String>, Long>> iter = f_listValues.iterator();
                while (iter.hasNext())
                    {
                    Pair<Pair<String, String>, Long> entryHeatMap = iter.next();
                    long                 nValue = entryHeatMap.getY();
                    Pair<String, String> cache  = entryHeatMap.getX();

                    // get the percent of this value of the total
                    float nPercent = nValue * 1.0f / m_cTotal * 100.0f;

                    if (fVerticalSplit && nTotalPercentLeft <= 50 && nTotalPercentLeft - nPercent > 0)
                        {
                        fVerticalSplit = false;
                        }
                    else if (!fVerticalSplit && nTotalPercentLeft <= 20)
                        {
                        fVerticalSplit = true;
                        }

                    Rectangle2D rectangle;
                    nLastStartX = nStartX;
                    nLastStartY = nStartY;

                    // calculate the size of the rectangle we are going to use to
                    // represent this value
                    if (fVerticalSplit)
                        {
                        int nNewWidth = (int) (nMapWidth * 1.0f * nPercent / nTotalPercentLeft);
                        rectangle     = new Rectangle2D.Double(nStartX, nStartY, nNewWidth, nMapHeight);
                        nStartX      += nNewWidth;
                        nMapWidth    -= nNewWidth;
                        }
                    else
                        {
                        // split horizontal
                        int nNewHeight = (int) (nMapHeight * 1.0f * nPercent / nTotalPercentLeft);
                        rectangle      = new Rectangle2D.Double(nStartX, nStartY, nMapWidth, nNewHeight);
                        nStartY       += nNewHeight;
                        nMapHeight    -= nNewHeight;
                        }

                    if (rectangle != null)
                        {
                        g2.setColor(getColour());
                        g2.fill(rectangle);

                        g2.setColor(Color.black);
                        g2.draw(rectangle);

                        String sCache      = cache.toString();
                        String sCacheShort = cache.getY();

                        StringBuilder sb = new StringBuilder(sCache).append(" - ")
                                .append(RenderHelper.INTEGER_FORMAT.format(nValue))
                                .append(f_nType == TYPE_MEMORY ? " bytes" : " objects")
                                .append(" (")
                                .append(RenderHelper.LOAD_AVERAGE_FORMAT.format(nPercent))
                                .append("%)");

                        String sCaption = sb.toString();

                        mapToolTips.put(rectangle, sCaption);

                        // if we have enough room, add the cache name
                        FontMetrics fm = g.getFontMetrics();
                        if (rectangle.getWidth()  > fm.stringWidth(sCaption) + 20 &&
                            rectangle.getHeight() > fm.getHeight() + 20)
                            {
                            g2.drawString(sCaption, nLastStartX + 10, nLastStartY + 20);
                            }
                        else if (rectangle.getWidth()  > fm.stringWidth(sCache) + 20 &&
                                 rectangle.getHeight() > fm.getHeight() + 20)
                            {
                            g2.drawString(sCache, nLastStartX + 10, nLastStartY + 20);
                            }
                        else if (rectangle.getWidth()  > fm.stringWidth(sCacheShort) + 20 &&
                                 rectangle.getHeight() > fm.getHeight() + 20)
                            {
                            g2.drawString(sCacheShort, nLastStartX + 10, nLastStartY + 20);
                            }
                        }

                    nTotalPercentLeft -= nPercent;
                    }
                }

            /**
             * Return a color that is based upon the incrementing range
             * m_nRange.
             *
             * @return a Color
             */
            private Color getColour()
                {
                m_nRange += INC_RANGE;
                if (m_nRange > MAX_RANGE)
                    {
                    m_nRange = MIN_RANGE;
                    }

                return Color.getHSBColor(m_nRange , 0.8f, 0.8f);
                }

            // ----- constants ----------------------------------------------

            /**
             * Beginning value for heat map Colour.
             */
            private static final float MIN_RANGE = 0.400f;

            /**
             * Max value for heat map Colour.
             */
            private static final float MAX_RANGE = 0.8f;

            /**
             * Incremental value for heat map Colour.
             */
            private static final float INC_RANGE = 0.0333f;

            // ----- data members -------------------------------------------

            /**
             * Tool tips for the caches.
             */
            private final Map<Rectangle2D, String> mapToolTips = new HashMap<>();

            /**
             * The Font to use to
             */
            private final Font f_font = new Font("Arial", Font.PLAIN, 12);

            /**
             * The current range for the heat map Colour.
             */
            private float m_nRange = MIN_RANGE;
            }

        // ----- constants --------------------------------------------------

        /**
         * Indicates a size based heat map.
         */
        protected static final int TYPE_SIZE = 0;

        /**
         * Indicates a memory based heat map.
         */
        protected static final int TYPE_MEMORY = 1;

        // ----- data members -------------------------------------------------

        /**
         * The list of caches and values.
         */
        private final LinkedList<Pair<Pair<String, String>, Long>> f_listValues = new LinkedList<>();

        /**
         * Current total for all caches.
         */
        private long m_cTotal = 0L;

        /**
         * The type of the heat map to display.
         */
        private final int f_nType;

        /**
         * Menu option description.
         */
        private final String f_sMenuItem;

        /**
         * Title for heat map.
         */
        private final String f_sTitle;

        /**
         * Current HeatMapJPanel.
         */
        protected JPanel m_pnlHeatMap;
        }

    // ---- constants -------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412496L;

    /**
     * Truncate cache operation.
     */
    public static final String TRUNCATE = "truncateCache";

    /**
     * clear cache operation.
     */
    public static final String CLEAR = "clearCache";

    // ----- data members ---------------------------------------------------

    /**
     * Flag to indicate if near cache is configured.
     */
    private boolean m_isNearCacheConfigured = false;

    /**
     * Flag to indicate if near view is configured.
     */
    private boolean m_isViewCacheConfigured = false;

    /**
     * Flag to indicate if we already added the front
     * cache detail tab.
     */
    private boolean m_isFrontCacheTabAdded = false;

    /**
    /**
     * Flag to indicate if we already added the view cache tab.
     */
    private boolean m_isViewTabAdded = false;

    /**
     * The tabbed panel.
     */
    private final JTabbedPane f_pneTab;

    /**
     * The scroll panel which we use to display front
     * cache details tab.
     */
    private final JScrollPane f_scrollPaneFrontDetail;

    /**
     * The scroll panel which we use to display front
     * view details tab.
     */
    private final JScrollPane f_scrollPaneViews;

    /**
     * Total number of caches.
     */
    private final JTextField f_txtTotalCaches;

    /**
     * Total primary copy memory used by caches.
     */
    private final JTextField f_txtTotalMemory;

    /**
     * Selected Cache.
     */
    private final JTextField f_txtSelectedCache;

    /**
     * Max query duration across nodes.
     */
    private final JTextField f_txtMaxQueryDuration;

    /**
     * Max query description across nodes.
     */
    private final JTextField f_txtMaxQueryDescription;

    /**
     * The {@link CacheTableModel} to display cache data.
     */
    protected final CacheTableModel f_tmodel;

    /**
     * The {@link CacheDetailTableModel} to display detail cache data.
     */
    protected final CacheDetailTableModel f_tmodelDetail;

    /**
     * The {@link CacheDetailTableModel} to display detail front cache data.
     */
    protected final CacheDetailTableModel f_tmodelFrontDetail;

    /**
     * The {@link CacheStorageManagerTableModel} to display cache storage data.
     */
    protected final CacheStorageManagerTableModel f_tmodelStorage;

    /**
     * The {@link CacheViewTableModel} to display cache viuews data.
     */
    protected final CacheViewTableModel f_tmodelViews;


    /**
     * The cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_cacheData = null;

    /**
     * The detailed cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_cacheDetailData = null;

    /**
     * The detailed front cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_cacheFrontDetailData = null;

    /**
     * The storage cache data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_cacheStorageData = null;

    /**
     * The storage view data retrieved from the {@link VisualVMModel}.
     */
    private transient List<Entry<Object, Data>> m_viewData = null;

    /**
     * The row selection listener.
     */
    private final SelectRowListSelectionListener f_listener;

    /**
     * The {@link ExportableJTable} to use to display cache detail data.
     */
    private final ExportableJTable f_tableDetail;

    /**
     * The {@link ExportableJTable} to use to display front cache data.
     */
    private final ExportableJTable f_tableFrontDetail;

    /**
     * The {@link ExportableJTable} to use to display storage data.
     */
    private final ExportableJTable f_tableStorage;

    /**
     * The {@link ExportableJTable} to use to display cache view data.
     */
    private final ExportableJTable f_tableViews;

    /**
     * Currently displaying heat map.
     */
    private ShowHeatMapMenuOption m_currentHeatMap;
    }
