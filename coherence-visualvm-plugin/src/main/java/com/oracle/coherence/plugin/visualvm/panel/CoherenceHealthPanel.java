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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.HealthSummaryTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HealthData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HealthSummaryData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view
 * summarized health data.
 *
 * @author tam  2022.06.22
 * @since 1.4.0
 */
public class CoherenceHealthPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceHealthPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceHealthPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalHealthChecks = getTextField(10, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_health_checks", f_txtTotalHealthChecks));
        pnlHeader.add(f_txtTotalHealthChecks);

        // create the table
        f_tmodel = new HealthSummaryTableModel(VisualVMModel.HEALTH_SUMMARY_LABELS);

        f_table = new ExportableJTable(f_tmodel);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, HealthSummaryData.MEMBERS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HealthSummaryData.STARTED, new RenderHelper.HealthRenderer());
        RenderHelper.setColumnRenderer(f_table, HealthSummaryData.LIVE, new RenderHelper.HealthRenderer());
        RenderHelper.setColumnRenderer(f_table, HealthSummaryData.READY, new RenderHelper.HealthRenderer());
        RenderHelper.setColumnRenderer(f_table, HealthSummaryData.SAFE, new RenderHelper.HealthRenderer());

        RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);

        // Add some space
        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll = new JScrollPane(f_table);
        configureScrollPane(pneScroll, f_table);
        pneScroll.setOpaque(false);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(pneScroll, BorderLayout.CENTER);

        pneSplit.add(pnlTop);

        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        // no-op, done in update data
        }

    @Override
    public void updateData()
        {
        m_healthData = f_model.getData(VisualVMModel.DataType.HEALTH);

        SortedMap<Object, Data> mapData = new TreeMap<>();

        // summarise the data by name and sub-type
        m_healthData.forEach(v ->
            {
            HealthData.HealthKey key = (HealthData.HealthKey) v.getKey();
            HealthData value = (HealthData) v.getValue();
            Pair<String, String> newKey = new Pair<>(key.getName(), key.getSubType());

            HealthSummaryData data = (HealthSummaryData) mapData.get(newKey);
            if (data == null)
                {
                data = new HealthSummaryData();
                data.setColumn(HealthSummaryData.HEALTH_NAME, newKey);
                data.setColumn(HealthSummaryData.MEMBERS, 0);
                data.setColumn(HealthSummaryData.STARTED, 0);
                data.setColumn(HealthSummaryData.LIVE, 0);
                data.setColumn(HealthSummaryData.READY, 0);
                data.setColumn(HealthSummaryData.SAFE, 0);
                }

            // increment the values
            data.setColumn(HealthSummaryData.MEMBERS, (Integer)data.getColumn(HealthSummaryData.MEMBERS) + 1);
            if (Boolean.TRUE.equals(value.getColumn(HealthData.STARTED)))
               {
               data.setColumn(HealthSummaryData.STARTED, (Integer)data.getColumn(HealthSummaryData.STARTED) + 1);
               }
            if (Boolean.TRUE.equals(value.getColumn(HealthData.LIVE)))
               {
               data.setColumn(HealthSummaryData.LIVE, (Integer)data.getColumn(HealthSummaryData.LIVE) + 1);
               }
            if (Boolean.TRUE.equals(value.getColumn(HealthData.READY)))
               {
               data.setColumn(HealthSummaryData.READY, (Integer)data.getColumn(HealthSummaryData.READY) + 1);
               }
           if (Boolean.TRUE.equals(value.getColumn(HealthData.SAFE)))
               {
               data.setColumn(HealthSummaryData.SAFE, (Integer)data.getColumn(HealthSummaryData.SAFE) + 1);
               }
           
            // update the map
            mapData.put(newKey, data);
            });

        AtomicInteger totalChecks = new AtomicInteger(0);
        AtomicInteger totalOk     = new AtomicInteger(0);

        // process the data and change any values to fractions if they do not equal the member count
        mapData.forEach((k,v) ->
            {
            int cMembers = (Integer) v.getColumn(HealthSummaryData.MEMBERS);
            int cStarted = (Integer) v.getColumn(HealthSummaryData.STARTED);
            int cLive    = (Integer) v.getColumn(HealthSummaryData.LIVE);
            int cReady   = (Integer) v.getColumn(HealthSummaryData.READY);
            int cSafe    = (Integer) v.getColumn(HealthSummaryData.SAFE);

            if (cStarted != cMembers)
                {
                v.setColumn(HealthSummaryData.STARTED, String.format(FORMAT, cStarted, cMembers));
                }
            if (cLive != cMembers)
                {
                v.setColumn(HealthSummaryData.LIVE, String.format(FORMAT, cLive, cMembers));
                }
            if (cReady != cMembers)
                {
                v.setColumn(HealthSummaryData.READY, String.format(FORMAT, cReady, cMembers));
                }
            if (cSafe != cMembers)
                {
                v.setColumn(HealthSummaryData.SAFE, String.format(FORMAT, cSafe, cMembers));
                }

            totalChecks.addAndGet(cMembers * 4);
            totalOk.addAndGet(cStarted + cLive + cReady + cSafe);
            });

        // update total health
        f_txtTotalHealthChecks.setForeground(Color.black);
        String sValue = String.format(FORMAT, totalOk.get(), totalChecks.get());
        
        f_txtTotalHealthChecks.setText(sValue);
        if (totalOk.get() != totalChecks.get())
            {
            // means at least 1 is not ready so make orange
            f_txtTotalHealthChecks.setBackground(Color.orange);
            }
        else
            {
            // must be ok so make it green
            f_txtTotalHealthChecks.setBackground(Color.green);
            }
        
        f_tmodel.setDataList(new ArrayList<>(mapData.entrySet()));
        f_tmodel.fireTableDataChanged();
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

    private static final String FORMAT = "%d/%d";

    // ----- data members ----------------------------------------------------

    /**
     * The total number of health checks.
     */
    private final JTextField f_txtTotalHealthChecks;

    /**
     * A check-box to indicate if the NameService should be included in the list of proxy servers.
     */
    private JCheckBox m_cbxIncludeNameService = null;

    /**
     * The health data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_healthData;

    /**
     * The {@link HealthSummaryTableModel} to display proxy data.
     */
    protected final HealthSummaryTableModel f_tmodel;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;
    }
