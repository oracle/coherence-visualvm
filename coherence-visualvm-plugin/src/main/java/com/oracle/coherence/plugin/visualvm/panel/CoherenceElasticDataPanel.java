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

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FlashJournalData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.RamJournalData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized elastic data statistics.
 *
 * @author tam  2014.04.21
 * @since  12.1.3
 */
public class CoherenceElasticDataPanel
        extends AbstractCoherencePanel
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceElasticDataPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceElasticDataPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        JPanel pnlHeader = new JPanel(new GridLayout(1, 2));
        pnlHeader.setOpaque(false);

        f_barRamUsage = createProgressBar();
        f_barFlashUsage = createProgressBar();

        JPanel pnlRamHeader   = new JPanel(new FlowLayout());
        pnlRamHeader.setOpaque(false);

        JPanel pnlFlashHeader = new JPanel(new FlowLayout());
        pnlFlashHeader.setOpaque(false);

        f_lblRam = new JLabel("");
        f_lblFlash = new JLabel("");
        f_lblRam.setToolTipText(getLocalizedText("LBL_journal_files_used"));
        f_lblFlash.setToolTipText(getLocalizedText("LBL_journal_files_used"));

        pnlRamHeader.add(getLocalizedLabel("LBL_ram_journal_files"));
        pnlRamHeader.add(f_barRamUsage);
        pnlRamHeader.add(f_lblRam);
        pnlFlashHeader.add(getLocalizedLabel("LBL_flash_journal_files"));
        pnlFlashHeader.add(f_barFlashUsage);
        pnlFlashHeader.add(f_lblFlash);

        MouseOverAction mouseOverAction = new MouseOverAction(this);

        f_barFlashUsage.addMouseListener(mouseOverAction);
        f_barRamUsage.addMouseListener(mouseOverAction);

        pnlHeader.add(pnlRamHeader);
        pnlHeader.add(pnlFlashHeader);

        JPanel pnlData = new JPanel();

        pnlData.setLayout(new GridLayout(2, 2));

        // create a chart for ram journal memory
        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        f_ramJournalMemoryGraph = GraphHelper.createRamJournalMemoryGraph();

        // ramJournalMemoryGraph.getChart().setPreferredSize(new Dimension(500, 300));
        pnlData.add(f_ramJournalMemoryGraph.getChart());

        // create a chart for flash journal memory
        f_flashJournalMemoryGraph = GraphHelper.createFlashJournalMemoryGraph();
        pnlData.add(f_flashJournalMemoryGraph.getChart());

        // create a chart for ram journal compactions
        f_ramJournalCompactionGraph = GraphHelper.createRamJournalCompactionGraph();
        pnlData.add(f_ramJournalCompactionGraph.getChart());

        // create a chart for flash journal compactions
        f_flashJournalCompactionGraph = GraphHelper.createFlashJournalCompactionGraph();
        pnlData.add(f_flashJournalCompactionGraph.getChart());

        add(pnlHeader, BorderLayout.PAGE_START);
        add(pnlData, BorderLayout.CENTER);

        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        int  cCurrentRamFileCount   = 0;
        int  cMaxRamFileCount       = 0;
        int  cCurrentFlashFileCount = 0;
        int  cMaxFlashFileCount     = 0;
        long cTotalRamUsed          = 0L;
        long cTotalFlashUsed        = 0L;
        long cCommittedRam          = 0L;
        long cCommittedFlash        = 0L;
        int  cRamCompaction         = 0;
        int  cFlashCompaction       = 0;
        int  cRamExhaustive         = 0;
        int  cFlashExhaustive       = 0;

        if (m_ramJournalData != null)
            {
            for (Map.Entry<Object, Data> entry : m_ramJournalData)
                {
                cCurrentRamFileCount += (Integer) entry.getValue().getColumn(RamJournalData.FILE_COUNT);
                cMaxRamFileCount     += (Integer) entry.getValue().getColumn(RamJournalData.MAX_FILES);
                cTotalRamUsed        += (Long) entry.getValue().getColumn(RamJournalData.TOTAL_DATA_SIZE);
                cCommittedRam        += (Long) entry.getValue().getColumn(RamJournalData.TOTAL_COMMITTED_BYTES);
                cRamCompaction       += getNullEntry(entry.getValue().getColumn(RamJournalData.COMPACTION_COUNT));
                cRamExhaustive       += getNullEntry(entry.getValue().getColumn(RamJournalData.EXHAUSTIVE_COMPACTION_COUNT));
                }
            }

        if (m_flashJournalData != null)
            {
            for (Map.Entry<Object, Data> entry : m_flashJournalData)
                {
                cCurrentFlashFileCount += (Integer) entry.getValue().getColumn(FlashJournalData.FILE_COUNT);
                cMaxFlashFileCount     += (Integer) entry.getValue().getColumn(FlashJournalData.MAX_FILES);
                cTotalFlashUsed        += (Long) entry.getValue().getColumn(FlashJournalData.TOTAL_DATA_SIZE);
                cCommittedFlash        += (Long) entry.getValue().getColumn(FlashJournalData.TOTAL_COMMITTED_BYTES);
                cFlashCompaction       += getNullEntry(entry.getValue().getColumn(FlashJournalData.COMPACTION_COUNT));
                cFlashExhaustive       += getNullEntry(entry.getValue().getColumn(FlashJournalData.EXHAUSTIVE_COMPACTION_COUNT));
                }
            }

        // if last value never set then set to current value
        if (m_cLastRamCompaction == -1)
            {
            m_cLastRamCompaction   = cRamCompaction;
            m_cLastRamExhaustive   = cRamExhaustive;
            m_cLastFlashCompaction = cFlashCompaction;
            m_cLastFlashExhaustive = cFlashExhaustive;
            }

        // initialize every time otherwise changes in membership will cause odd %
        f_barFlashUsage.setMaximum(cMaxFlashFileCount);
        f_barRamUsage.setMaximum(cMaxRamFileCount);

        f_barRamUsage.setValue(cCurrentRamFileCount);
        f_lblRam.setText(Integer.toString(cCurrentRamFileCount) + " / " + Integer.toString(cMaxRamFileCount));

        f_barFlashUsage.setValue(cCurrentFlashFileCount);
        f_lblFlash.setText(Integer.toString(cCurrentFlashFileCount) + " / " + Integer.toString(cMaxFlashFileCount));

        GraphHelper.addValuesToRamJournalMemoryGraph(f_ramJournalMemoryGraph, cCommittedRam, cTotalRamUsed);
        GraphHelper.addValuesToFlashJournalMemoryGraph(f_flashJournalMemoryGraph, cCommittedFlash, cTotalFlashUsed);

        // add delta values
        int cRamCompactionDelta   = cRamCompaction - m_cLastRamCompaction;
        int cRamExhaustiveDelta   = cRamExhaustive - m_cLastRamExhaustive;
        int cFlashCompactionDelta = cFlashCompaction - m_cLastFlashCompaction;
        int cFlashExhaustiveDelta = cFlashExhaustive - m_cLastFlashExhaustive;

        GraphHelper.addValuesToRamJournalCompactionGraph(f_ramJournalCompactionGraph,
            cRamCompactionDelta < 0 ? 0 : cRamCompactionDelta, cRamExhaustiveDelta < 0 ? 0 : cRamExhaustiveDelta);
        GraphHelper.addValuesToFlashJournalCompactionGraph(f_flashJournalCompactionGraph,
            cFlashCompactionDelta < 0 ? 0 : cFlashCompactionDelta,
            cFlashExhaustiveDelta < 0 ? 0 : cFlashExhaustiveDelta);

        // set the last values to calculate deltas
        m_cLastFlashCompaction = cFlashCompaction;
        m_cLastFlashExhaustive = cFlashExhaustive;
        m_cLastRamCompaction   = cRamCompaction;
        m_cLastRamExhaustive   = cRamExhaustive;
        }

    @Override
    public void updateData()
        {
        m_ramJournalData = f_model.getData(VisualVMModel.DataType.RAMJOURNAL);
        m_flashJournalData = f_model.getData(VisualVMModel.DataType.FLASHJOURNAL);
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Create a {@link JProgressBar} to display file count usage.
     *
     * @return a {@link JProgressBar} to display file count usage
     */
    private JProgressBar createProgressBar()
        {
        JProgressBar barProgress = new JProgressBar();

        barProgress.setMinimum(0);
        barProgress.setMaximum(1);
        barProgress.setStringPainted(true);

        Dimension dim = new Dimension(200, 20);

        barProgress.setPreferredSize(dim);
        barProgress.setSize(dim);
        barProgress.setToolTipText(Localization.getLocalText("LBL_click_for_detail"));

        return barProgress;
        }

    // ----- inner classes --------------------------------------------------

    /**
     * A class to provide mouse-click functionality to display details
     * for either flash journal or ram journal data.
     */
    private class MouseOverAction
            implements MouseListener
        {
        /**
         * Create the {@link MouseListener} implementation to show details
         * when a progress bar is hovered over.
         *
         * @param panel the panel to use
         */
        public MouseOverAction(CoherenceElasticDataPanel panel)
            {
            f_pnlElasticData = panel;
            f_tmodel = new DefaultTableModel(new Object[]
                {
                Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_journal_files"),
                Localization.getLocalText("LBL_total_data_size"), Localization.getLocalText("LBL_committed"),
                Localization.getLocalText("LBL_compactions"),
                Localization.getLocalText("LBL_current_collector_load_factor"),
                Localization.getLocalText("LBL_max_file_size")
                }, COLUMN_COUNT)
                    {
                    @Override
                    public boolean isCellEditable(int row, int column)
                        {
                        return false;
                        }
                    };
            f_table = new ExportableJTable(f_tmodel);

            RenderHelper.setIntegerRenderer(f_table, 0);                                      // node id
            setColumnRenderer(f_table, 1, Localization.getLocalText("TTIP_used_maximum"));    // Journal files
            RenderHelper.setColumnRenderer(f_table, 2, new RenderHelper.BytesRenderer());     // total data size
            setColumnRenderer(f_table, 3, Localization.getLocalText("TTIP_used_maximum"));    // Committed
            setColumnRenderer(f_table, 4, Localization.getLocalText("TTIP_compactions"));     // Compactions
            setColumnRenderer(f_table, 5, null);                                              // load factor
            RenderHelper.setColumnRenderer(f_table, 6, new RenderHelper.BytesRenderer());     // max file size

            RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            f_table.setPreferredScrollableViewportSize(new Dimension((int) (Math.max((int) (screenSize.getWidth() * 0.5),
                800)), f_table.getRowHeight() * 10));

            f_table.setIntercellSpacing(new Dimension(6, 3));
            f_table.setRowHeight(f_table.getRowHeight() + 4);

            f_pneMessage = new JScrollPane(f_table);
            configureScrollPane(f_pneMessage, f_table);
            AbstractMenuOption.setResizable(f_pneMessage);
            }

        /**
         * Set a column renderer for right aligned and optionally set tool tip text.
         *
         * @param exportableJTable the {@link ExportableJTable} to apply to
         * @param nColumn          column number to right align
         * @param sToolTip         tool tip - null if nothing
         */
        private void setColumnRenderer(ExportableJTable exportableJTable, int nColumn, String sToolTip)
            {
            DefaultTableCellRenderer rndRightAlign = new DefaultTableCellRenderer();

            if (sToolTip != null)
                {
                rndRightAlign.setToolTipText(sToolTip);
                }

            rndRightAlign.setHorizontalAlignment(JLabel.RIGHT);
            exportableJTable.getColumnModel().getColumn(nColumn).setCellRenderer(rndRightAlign);
            }

        // ----- MouseListener methods --------------------------------------

        @Override
        public void mouseEntered(MouseEvent e)
            {
            }

        @Override
        public void mousePressed(MouseEvent e)
            {
            }

        @Override
        public void mouseReleased(MouseEvent e)
            {
            }

        /**
         * Display a dialog box with a table showing detailed information
         * node by node for either ram or flash journal.
         *
         * @param e the {@link MouseEvent} that caused this action
         */
        @Override
        public void mouseClicked(MouseEvent e)
            {
            boolean fisRamBar = e.getSource().equals(f_pnlElasticData.f_barRamUsage);
            int     row       = 0;

            // remove any existing rows
            f_tmodel.getDataVector().removeAllElements();
            f_tmodel.fireTableDataChanged();

            java.util.List<Map.Entry<Object, Data>> tableData = f_pnlElasticData.f_model.getData(fisRamBar
                                                                    ? VisualVMModel.DataType.RAMJOURNAL
                                                                    : VisualVMModel.DataType.FLASHJOURNAL);

            // loop through the model and format nicely
            for (Map.Entry<Object, Data> entry : tableData)
                {
                Data data = entry.getValue();
                String sJournalFiles = data.getColumn(RamJournalData.FILE_COUNT).toString() + " / "
                                       + data.getColumn(RamJournalData.MAX_FILES).toString();
                String sCommitted =
                    RenderHelper.getRenderedBytes((Long) data.getColumn(RamJournalData.TOTAL_COMMITTED_BYTES)) + " / "
                    + RenderHelper.getRenderedBytes((Long) data.getColumn(RamJournalData.MAX_COMMITTED_BYTES));
                String sCompactions = getNullEntry(data.getColumn(RamJournalData.COMPACTION_COUNT)) + " / " +
                                      getNullEntry(data.getColumn(RamJournalData.EXHAUSTIVE_COMPACTION_COUNT));

                f_tmodel.insertRow(row++, new Object[]
                    {
                    data.getColumn(RamJournalData.NODE_ID), sJournalFiles,
                    data.getColumn(RamJournalData.TOTAL_DATA_SIZE), sCommitted, sCompactions,
                    data.getColumn(RamJournalData.CURRENT_COLLECTION_LOAD_FACTOR),
                    data.getColumn(RamJournalData.MAX_FILE_SIZE)
                    });
                }

            f_tmodel.fireTableDataChanged();

            JOptionPane.showMessageDialog(null, f_pneMessage,
                                          Localization.getLocalText(fisRamBar
                                              ? "LBL_ram_journal_detail"
                                              : "LBL_flash_journal_detail"), JOptionPane.INFORMATION_MESSAGE);
            }

        @Override
        public void mouseExited(MouseEvent e)
            {
            }

        // ----- static -----------------------------------------------------

        /**
         * Column count.
         */
        private static final int COLUMN_COUNT = 7;

        // ----- data members -----------------------------------------------

        /**
         * The panel used to display the elastic data information.
         */
        private final CoherenceElasticDataPanel f_pnlElasticData;

        /**
         * The {@link TableModel} to display detail data.
         */
        private final DefaultTableModel f_tmodel;

        /**
         * the {@link ExportableJTable} to use to display detail data.
         */
        private final ExportableJTable f_table;

        /**
         * The scroll pane to display the table in.
         */
        private final JScrollPane f_pneMessage;
        }

    // ----- data members ---------------------------------------------------

    /**
     * Indicates if we have set the bounds for the progress bars.
     */
    private boolean m_fProgressBarInitialized = false;

    /**
     * Indication of how much RAM is used.
     */
    private final JProgressBar f_barRamUsage;

    /**
     * Indication of how much flash is used.
     */
    private final JProgressBar f_barFlashUsage;

    /**
     * Shows files used out of total.
     */
    private final JLabel f_lblRam;

    /**
     * Shows files used out of total.
     */
    private final JLabel f_lblFlash;

    /**
     * The graph of overall ramjournal memory.
     */
    private final SimpleXYChartSupport f_ramJournalMemoryGraph;

    /**
     * The graph of overall ramjournal compactions.
     */
    private final SimpleXYChartSupport f_ramJournalCompactionGraph;

    /**
     * The ramjournal data retrieved from the {@link VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_ramJournalData;

    /**
     * The graph of overall flashjournal memory.
     */
    private final SimpleXYChartSupport f_flashJournalMemoryGraph;

    /**
     * The graph of overall flashjournal compactions.
     */
    private final SimpleXYChartSupport f_flashJournalCompactionGraph;

    /**
     * The flashjournal data retrieved from the {@link VisualVMModel}.
     */
    private java.util.List<Map.Entry<Object, Data>> m_flashJournalData;

    /**
     * Last ramjournal compaction count.
     */
    int m_cLastRamCompaction = -1;

    /**
     * Last flashjournal compaction count.
     */
    int m_cLastFlashCompaction = -1;

    /**
     * Last ramjournal exhaustive compaction count.
     */
    int m_cLastRamExhaustive = -1;

    /**
     * Last ramjournal exhaustive compaction count.
     */
    int m_cLastFlashExhaustive = -1;
    }
