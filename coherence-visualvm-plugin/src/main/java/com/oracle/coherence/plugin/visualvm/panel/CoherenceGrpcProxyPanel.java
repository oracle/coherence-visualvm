/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.coherence.plugin.visualvm.tablemodel.GrpcProxyTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.GrpcProxyData;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view
 * summarized gRPC proxy server data.
 *
 * @author tam  2022.02.07
 * @since 1.3.0
 */
public class CoherenceGrpcProxyPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceGrpcProxyPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceGrpcProxyPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalGrpcProxyServers = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_grpc_servers", f_txtTotalGrpcProxyServers));
        pnlHeader.add(f_txtTotalGrpcProxyServers);

        f_txtTotalMsgRec = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_grpc_msg_rec", f_txtTotalMsgRec));
        pnlHeader.add(f_txtTotalMsgRec);

        f_txtTotalRespSent = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_grpc_resp_sent", f_txtTotalRespSent));
        pnlHeader.add(f_txtTotalRespSent);
        
        // create the table
        f_tmodel = new GrpcProxyTableModel(VisualVMModel.DataType.GRPC_PROXY.getMetadata());

        f_table = new ExportableJTable(f_tmodel, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.NODE_ID, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.SUCCESSFUL_REQUEST_COUNT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.ERROR_REQUEST_COUNT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.RESPONSES_SENT_COUNT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.MESSAGES_RECEIVED_COUNT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.REQUEST_DURATION_MEAN, new RenderHelper.DecimalRenderer());
        RenderHelper.setColumnRenderer(f_table, GrpcProxyData.MESSAGE_DURATION_MEAN, new RenderHelper.DecimalRenderer());
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

        // create a chart for the count of proxy server connections
        f_GrpcMessagesGraph = GraphHelper.createGrpcMessagesGraph();
        f_grpcProxyMeanGraph = GraphHelper.createMeanGrpcStatsGraph();

        pneSplitPlotter.add(f_GrpcMessagesGraph.getChart());
        pneSplitPlotter.add(f_grpcProxyMeanGraph.getChart());

        pneSplit.add(pnlTop);
        pneSplit.add(pneSplitPlotter);

        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        final String MEM_FORMAT = "%,d";
        float cTotalReqMean     = 0f;
        float cTotalMsgMean     = 0f;
        long  nSentCount        = 0L;
        long  nRecCount         = 0L;
        int   cCount            = 0;

        if (m_GrpcData != null)
            {
            f_txtTotalGrpcProxyServers.setText(String.format("%5d", m_GrpcData.size()));

            for (Entry<Object, Data> entry : m_GrpcData)
                {
                cCount++;
                nSentCount        += (Long) entry.getValue().getColumn(GrpcProxyData.RESPONSES_SENT_COUNT);
                nRecCount         += (Long) entry.getValue().getColumn(GrpcProxyData.MESSAGES_RECEIVED_COUNT);
                cTotalReqMean     += (Float) entry.getValue().getColumn(GrpcProxyData.REQUEST_DURATION_MEAN);
                cTotalMsgMean     += (Float) entry.getValue().getColumn(GrpcProxyData.MESSAGE_DURATION_MEAN);
                }

            f_txtTotalMsgRec.setText(String.format(MEM_FORMAT, nRecCount));
            f_txtTotalRespSent.setText(String.format(MEM_FORMAT, nSentCount));

            if (cCount > 0)
                {
                cTotalReqMean /= cCount;
                cTotalMsgMean /= cCount;
                GraphHelper.addValuesToMeanGrpcStatsGraph(f_grpcProxyMeanGraph, cTotalReqMean, cTotalMsgMean);
                }
            }
        else
            {
            f_txtTotalMsgRec.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalRespSent.setText(String.format(MEM_FORMAT, 0));
            }

        fireTableDataChangedWithSelection(f_table, f_tmodel);


        long ldtLastUpdate = f_model.getLastUpdate();
        if (ldtLastUpdate > m_cLastUpdateTime)
            {
            if (m_cLastRecCount == -1L)
                {
                m_cLastRecCount  = nRecCount;
                m_cLastSentCount = nSentCount;
                }

            // get delta values
            long nDeltaRecCount  = nRecCount - m_cLastRecCount;
            long nDeltaSentCount = nSentCount - m_cLastSentCount;

            GraphHelper.addValuesToGrpcMessagesGraph(f_GrpcMessagesGraph,
                    nDeltaSentCount < 0 ? 0 : nDeltaSentCount,
                    nDeltaRecCount  < 0 ? 0 : nDeltaRecCount);

            // set the last values to calculate deltas
            m_cLastRecCount   = nRecCount;
            m_cLastSentCount  = nSentCount;
            m_cLastUpdateTime = ldtLastUpdate;
            }
        }

    @Override
    public void updateData()
        {
        m_GrpcData = f_model.getData(VisualVMModel.DataType.GRPC_PROXY);

        f_tmodel.setDataList(m_GrpcData);
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569058792412546L;

    // ----- data members ----------------------------------------------------

    /**
     * The total number of gRPC Proxy servers running in the cluster.
     */
    private final JTextField f_txtTotalGrpcProxyServers;

    /**
     * The total number of messages received.
     */
    private final JTextField f_txtTotalMsgRec;

    /**
     * The total number of responses sent.
     */
    private final JTextField f_txtTotalRespSent;

    /**
     * The graph of gRPC Proxy Messages.
     */
    private final SimpleXYChartSupport f_GrpcMessagesGraph;

    /**
     * The graph of gRPC proxy Mean.
     */
    private final SimpleXYChartSupport f_grpcProxyMeanGraph;

    /**
     * The gRPC Proxy statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_GrpcData;

    /**
     * The {@link GrpcProxyTableModel} to display proxy data.
     */
    protected final GrpcProxyTableModel f_tmodel;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * Last sent count.
     */
    private long m_cLastSentCount = -1L;

    /**
     * Last receive count.
     */
    private long m_cLastRecCount = -1L;

    /**
     * Last update time for stats.
     */
    private long m_cLastUpdateTime = -1L;
    }
