/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.JCacheConfigurationTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.JCacheStatisticsData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.JCacheConfigurationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized JCache statistics.
 *
 * @author tam  2014.09.22
 * @since  12.1.3
 */
public class CoherenceJCachePanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceJCachePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceJCachePanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing of top and bottom components
        JSplitPane pneSplitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        f_tmodel = new JCacheConfigurationTableModel(VisualVMModel.DataType.JCACHE_CONFIG.getMetadata());

        final ExportableJTable table = new ExportableJTable(f_tmodel, model);

        RenderHelper.setColumnRenderer(table, JCacheConfigurationData.CACHE_MANAGER,
                                       new RenderHelper.ToolTipRenderer());

        table.setPreferredScrollableViewportSize(new Dimension(300, table.getRowHeight() * 5));

        // Add some space
        setTablePadding(table);

        MenuOption optionShowDetails = new ShowDetailMenuOption(model, table, SELECTED_JCACHE);

        optionShowDetails.setMenuLabel(Localization.getLocalText("LBL_show_jcache_details"));

        // add menu option to show details
        table.setMenuOptions(new MenuOption[] {optionShowDetails});

        // Add top panel to screen
        JPanel      pnlTop    = new JPanel(new BorderLayout());
        JScrollPane pneScroll = new JScrollPane(table);

        pnlTop.add(pneScroll, BorderLayout.CENTER);
        pnlTop.setOpaque(true);

        pneSplitMain.add(pnlTop);
        pneSplitMain.setOpaque(false);

        // add bottom panel to screen
        f_pnlBottom = new JPanel(new BorderLayout());

        JPanel pnlBottomHeader = new JPanel();
        pnlBottomHeader.setOpaque(false);

        f_txtSelectedCache = getTextField(40, JTextField.LEFT);
        pnlBottomHeader.add(getLocalizedLabel("LBL_selected_config_cache", f_txtSelectedCache));
        pnlBottomHeader.add(f_txtSelectedCache);
        f_pnlBottom.add(pnlBottomHeader, BorderLayout.PAGE_START);

        addSplitPaneAndGraph();
        pneSplitMain.add(f_pnlBottom);

        add(pneSplitMain);

        // add a listener for the selected row
        ListSelectionModel rowSelectionModel = table.getSelectionModel();

        f_listener = new SelectRowListSelectionListener(table);
        rowSelectionModel.addListSelectionListener(f_listener);
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Add the split panel and a new graph on the bottom.
     */
    protected void addSplitPaneAndGraph()
        {
        if (m_pneSplitBottom != null)
            {
            f_pnlBottom.remove(m_pneSplitBottom);
            }

        Pair<String, String> selectedJCache = f_model.getSelectedJCache();
        String               sCacheName     = selectedJCache == null ? "None Selected" : selectedJCache.getY();

        m_pneSplitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        m_operationAverageGraph = GraphHelper.createJCacheAverageGraph(sCacheName);
        m_hitRateGraph = GraphHelper.createJCacheHitPercentageGraph(sCacheName);
        m_pneSplitBottom.setResizeWeight(0.3);

        m_pneSplitBottom.add(m_operationAverageGraph.getChart());
        m_pneSplitBottom.add(m_hitRateGraph.getChart());

        f_pnlBottom.add(m_pneSplitBottom, BorderLayout.CENTER);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        f_tmodel.fireTableDataChanged();

        Pair<String, String> selectedJCache = f_model.getSelectedJCache();

        if (selectedJCache != null)
            {
            f_listener.updateRowSelection();

            // update the graph
            if (m_statsData != null)
                {
                float cAveragePut    = 0.0f;
                float cAverageGet    = 0.0f;
                float cAverageRemove = 0.0f;
                float cHitRate       = 0.0f;

                for (Map.Entry<Object, Data> entry : m_statsData)
                    {
                    if (entry.getKey().equals(selectedJCache))
                        {
                        cAverageGet = (Float) entry.getValue().getColumn(JCacheStatisticsData.AVERAGE_GET_TIME) * 1000f;
                        cAveragePut = (Float) entry.getValue().getColumn(JCacheStatisticsData.AVERAGE_PUT_TIME) * 1000f;
                        cAverageRemove = (Float) entry.getValue().getColumn(JCacheStatisticsData.AVERAGE_REMOVE_TIME)
                                         * 1000f;
                        cHitRate = (Float) entry.getValue().getColumn(JCacheStatisticsData.CACHE_HIT_PERCENTAGE);
                        break;    // should only be one entry
                        }
                    }

                GraphHelper.addValuesToJCacheAverageGraph(m_operationAverageGraph, cAveragePut, cAverageGet,
                    cAverageRemove);
                GraphHelper.addValuesToJCacheHitPercentagGraph(m_hitRateGraph, (long) cHitRate);
                }
            }
        }

    @Override
    public void updateData()
        {
        m_configData = f_model.getData(VisualVMModel.DataType.JCACHE_CONFIG);
        m_statsData = f_model.getData(VisualVMModel.DataType.JCACHE_STATS);

        if (m_configData == null && m_statsData != null)
            {
            // populate the configData with the stats data if none exists
            SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();

            for (Map.Entry<Object, Data> entry : m_statsData)
                {
                Data data = new JCacheConfigurationData();
                data.setColumn(JCacheConfigurationData.CACHE_MANAGER, entry.getKey());
                mapData.put(entry.getKey(), data);
                }
                m_configData = new ArrayList<Map.Entry<Object, Data>>(mapData.entrySet());
            }

        if (m_configData != null)
            {
            f_tmodel.setDataList(m_configData);
            }
        }

    // ---- inner classes ---------------------------------------------------

    /**
     * Inner class to change the the information displayed on the graphs
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
            this.f_table = table;
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
                Pair<String, String> selectedCache = (Pair<String, String>) f_table.getValueAt(m_nSelectedRow, 0);

                if (!selectedCache.equals(f_model.getSelectedJCache()))
                    {
                    String sSelectedCache = selectedCache.toString();

                    f_model.setSelectedJCache(selectedCache);
                    f_txtSelectedCache.setText(sSelectedCache);
                    f_txtSelectedCache.setToolTipText(sSelectedCache);

                    // Update the Graphs
                    addSplitPaneAndGraph();
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
         * The {@link ExportableJTable} to use to display detail data.
         */
        private final ExportableJTable f_table;

        /**
         * The currently selected row.
         */
        private int m_nSelectedRow;
        }

    // ----- data members ---------------------------------------------------

    /**
     * Selected Cache.
     */
    private final JTextField f_txtSelectedCache;

    /**
     * The {@link JCacheConfigurationTableModel} to display JCache data.
     */
    protected final JCacheConfigurationTableModel f_tmodel;

    /**
     * The JCache configuration data.
     */
    private List<Map.Entry<Object, Data>> m_configData;

    /**
     * The JCache statistics data.
     */
    private List<Map.Entry<Object, Data>> m_statsData;

    /**
     * The row selection listener.
     */
    private final SelectRowListSelectionListener f_listener;

    /**
     * The graph of average put/get/remove times.
     */
    private SimpleXYChartSupport m_operationAverageGraph;

    /**
     * The graph of hit rate percentage.
     */
    private SimpleXYChartSupport m_hitRateGraph;

    /**
     * Bottom panel.
     */
    private final JPanel f_pnlBottom ;

    /**
     * Bottom split pane.
     */
    private JSplitPane m_pneSplitBottom;
    }
