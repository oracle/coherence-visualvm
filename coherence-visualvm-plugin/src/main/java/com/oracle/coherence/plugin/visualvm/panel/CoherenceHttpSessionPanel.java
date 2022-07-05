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
import com.oracle.coherence.plugin.visualvm.tablemodel.HttpSessionTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpSessionData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to Coherence*Web
 * statistics.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CoherenceHttpSessionPanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceHttpSessionPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceHttpSessionPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();

        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalApplications = getTextField(10, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_applications", f_txtTotalApplications));
        pnlHeader.add(f_txtTotalApplications);

        f_txtMaxReapDuration = getTextField(6, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_max_reap_duration", f_txtMaxReapDuration));
        pnlHeader.add(f_txtMaxReapDuration);

        // create the table
        f_tmodel = new HttpSessionTableModel(VisualVMModel.DataType.HTTP_SESSION.getMetadata());

        f_table = new ExportableJTable(f_tmodel);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.SESSION_TIMEOUT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.AVG_SESSION_SIZE, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.AVG_REAPED_SESSIONS, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.AVG_REAP_DURATION, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.LAST_REAP_DURATION_MAX,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, HttpSessionData.SESSION_UPDATES, new RenderHelper.IntegerRenderer());

        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        // Add some space
        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(f_table);
        configureScrollPane(scrollPane, f_table);

        JPanel pnlTop = new JPanel();

        pnlTop.setLayout(new BorderLayout());
        pnlTop.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(scrollPane, BorderLayout.CENTER);

        pneSplit.add(pnlTop);

        // create a chart for the machine load averages
        f_sessionCountGraph = GraphHelper.createSessionCountGraph();
        f_reapDurationGraph = GraphHelper.createReapDurationGraph();

        JSplitPane pneSplitPlotter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        pneSplitPlotter.setResizeWeight(0.5);

        pneSplitPlotter.add(f_sessionCountGraph.getChart());
        pneSplitPlotter.add(f_reapDurationGraph.getChart());

        pneSplit.add(pneSplitPlotter);
        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        int          cTotalSessionCount  = 0;
        int          cTotalOverflowCount = 0;
        int          c                   = 0;
        long         cMaxMaxLatency      = 0L;
        long         cCurrentAverage     = 0L;
        Set<String>  setSessionCaches    = new HashSet<String>();
        Set<String>  setOverflowCaches   = new HashSet<String>();

        final String FORMAT              = "%5d";

        if (m_httpSessionData != null)
            {
            for (Map.Entry<Object, Data> entry : m_httpSessionData)
                {
                c++;

                // add to the the list of session and overflow caches so we can
                // find out the total count of these later
                String sSessionCache  = entry.getValue().getColumn(HttpSessionData.SESSION_CACHE_NAME).toString();
                String sOverflowCache = entry.getValue().getColumn(HttpSessionData.OVERFLOW_CACHE_NAME).toString();

                setSessionCaches.add(sSessionCache);
                setOverflowCaches.add(sOverflowCache);

                long nDuration = (Long) entry.getValue().getColumn(HttpSessionData.LAST_REAP_DURATION_MAX);

                cCurrentAverage += nDuration;

                if (nDuration > cMaxMaxLatency)
                    {
                    cMaxMaxLatency = nDuration;
                    }
                }

            cCurrentAverage = (c == 0 ? 0 : cCurrentAverage / c);

            f_txtTotalApplications.setText(String.format(FORMAT, c));

            // go through each of the caches and total up the session and overflow counts
            for (String sCache : setSessionCaches)
                {
                cTotalSessionCount += HttpSessionData.getCacheCount(f_model, sCache);
                }

            for (String sCache : setOverflowCaches)
                {
                cTotalOverflowCount += HttpSessionData.getCacheCount(f_model, sCache);
                }
            }
        else
            {
            f_txtTotalApplications.setText(String.format(FORMAT, 0));
            }

        GraphHelper.addValuesToSessionCountGraph(f_sessionCountGraph, cTotalSessionCount, cTotalOverflowCount);
        GraphHelper.addValuesToReapDurationGraph(f_reapDurationGraph, cMaxMaxLatency, cCurrentAverage);

        // GraphHelper.addValuesToPersistenceLatencyGraph(persistenceLatencyGraph, cLatencyAverage * 1000.0f);

        f_txtMaxReapDuration.setText(Long.toString(cMaxMaxLatency));

        fireTableDataChangedWithSelection(f_table, f_tmodel);
        }

    @Override
    public void updateData()
        {
        m_httpSessionData = f_model.getData(VisualVMModel.DataType.HTTP_SESSION);

        if (m_httpSessionData != null)
            {
            f_tmodel.setDataList(m_httpSessionData);
            }

        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 9148388883048820567L;

    // ----- data members ----------------------------------------------------

    /**
     * The total amount of active space used.
     */
    private final JTextField f_txtTotalApplications;

    /**
     * The current Max latency.
     */
    private final JTextField f_txtMaxReapDuration;

    /**
     * The machine statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpSessionData;

    /**
     * The {@link HttpSessionTableModel} to display HTTP session data.
     */
    protected final HttpSessionTableModel f_tmodel;

    /**
     * The graph of session counts.
     */
    private final SimpleXYChartSupport f_sessionCountGraph;

    /**
     * The graph of overflow session counts.
     */
    private final SimpleXYChartSupport f_reapDurationGraph;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;
    }
