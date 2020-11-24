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
import com.oracle.coherence.plugin.visualvm.tablemodel.HttpProxyMemberTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.HttpProxyTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.ServiceMemberTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpProxyMemberData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized http proxy server statistics.
 *
 * @author tam  2015.08.28
 * @since 12.2.1.1
 */
public class CoherenceHttpProxyPanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceServicePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceHttpProxyPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        f_tmodel = new HttpProxyTableModel(VisualVMModel.DataType.HTTP_PROXY.getMetadata());
        f_tmodelDetail = new HttpProxyMemberTableModel(VisualVMModel.DataType.HTTP_PROXY_DETAIL.getMetadata());

        f_table = new ExportableJTable(f_tmodel);
        f_tableDetail = new ExportableJTable(f_tmodelDetail);

        // set renderers
        RenderHelper.setIntegerRenderer(f_table, HttpProxyData.MEMBER_COUNT);
        RenderHelper.setIntegerRenderer(f_table, HttpProxyData.TOTAL_ERROR_COUNT);
        RenderHelper.setIntegerRenderer(f_table, HttpProxyData.TOTAL_REQUEST_COUNT);

        RenderHelper.setMillisRenderer(f_table, HttpProxyData.AVERAGE_REQ_TIME);
        RenderHelper.setMillisRenderer(f_table, HttpProxyData.AVERAGE_REQ_PER_SECOND);

        RenderHelper.setIntegerRenderer(f_tableDetail, HttpProxyMemberData.NODE_ID);
        RenderHelper.setIntegerRenderer(f_tableDetail, HttpProxyMemberData.TOTAL_ERROR_COUNT);
        RenderHelper.setIntegerRenderer(f_tableDetail, HttpProxyMemberData.TOTAL_REQUEST_COUNT);

        RenderHelper.setMillisRenderer(f_tableDetail, HttpProxyMemberData.AVG_REQ_TIME);
        RenderHelper.setMillisRenderer(f_tableDetail, HttpProxyMemberData.REQ_PER_SECOND);

        RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);
        RenderHelper.setHeaderAlignment(f_tableDetail, JLabel.CENTER);
        f_table.setPreferredScrollableViewportSize(new Dimension(500, f_table.getRowHeight() * 5));
        f_tableDetail.setPreferredScrollableViewportSize(new Dimension(500, 125));

        // Add some space
        f_table.setIntercellSpacing(new Dimension(6, 3));
        f_table.setRowHeight(f_table.getRowHeight() + 4);

        f_tableDetail.setIntercellSpacing(new Dimension(6, 3));
        f_tableDetail.setRowHeight(f_table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll       = new JScrollPane(f_table);
        JScrollPane pneScrollDetail = new JScrollPane(f_tableDetail);
        configureScrollPane(pneScroll, f_table);
        configureScrollPane(pneScrollDetail, f_tableDetail);

        pneSplit.add(pneScroll);

        // create the detail pane
        JPanel pneDetail = new JPanel();
        pneDetail.setOpaque(false);

        pneDetail.setLayout(new BorderLayout());

        JPanel detailHeaderPanel = new JPanel();
        detailHeaderPanel.setOpaque(false);

        f_txtSelectedService = getTextField(22, JTextField.LEFT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_selected_service", f_txtSelectedService));
        detailHeaderPanel.add(f_txtSelectedService);

        final JSplitPane pneSplitDetail = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitDetail.setResizeWeight(0.5);

        pneDetail.add(detailHeaderPanel, BorderLayout.PAGE_START);
        pneSplitDetail.add(pneScrollDetail);

        final JTabbedPane pneDetailTabs = new JTabbedPane();
        populateTabs(pneDetailTabs, getLocalizedText("LBL_none_selected"));

        pneSplitDetail.add(pneDetailTabs);

        pneDetail.add(pneSplitDetail, BorderLayout.CENTER);

        pneSplit.add(pneDetail);
        add(pneSplit);

        // add a listener for the selected row
        ListSelectionModel rowSelectionModel = f_table.getSelectionModel();

        f_listener = new SelectRowListSelectionListener(f_table, pneDetailTabs);
        rowSelectionModel.addListSelectionListener(f_listener);
        }

    /**
     * Inner class to change the the information displayed on the detailModel
     * table when the master changes.
     */
    private class SelectRowListSelectionListener
            implements ListSelectionListener
        {
        // ----- constructors -----------------------------------------------

        /**
         * Create a new listener to changes the detail table.
         *
         * @param table         the {@link ExportableJTable} that is to be selected
         * @param pneDetailTabs the {@link JTabbedPane} to attach to
         */
        public SelectRowListSelectionListener(ExportableJTable table, JTabbedPane pneDetailTabs)
            {
            this.table = table;
            this.pneDetailTabs = pneDetailTabs;
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

            if (!selectionModel.isSelectionEmpty())
                {
                nSelectedRow = selectionModel.getMinSelectionIndex();

                // get the service at the selected row, which is the first column
                String sSelectedService = (String) table.getValueAt(nSelectedRow, 0);

                if (!sSelectedService.equals(f_model.getSelectedHttpProxyService()))
                    {
                    f_model.setSelectedHttpProxyService(sSelectedService);
                    f_model.eraseServiceMemberData();
                    f_tmodelDetail.setDataList(null);
                    f_tmodelDetail.fireTableDataChanged();

                    f_txtSelectedService.setText(sSelectedService);
                    f_txtSelectedService.setToolTipText(sSelectedService);

                    populateTabs(pneDetailTabs, sSelectedService);

                    m_cLastErrorCount       = -1L;
                    m_cLastRequestCount     = -1L;
                    m_cLastUpdateTime       = -1L;
                    m_cLastResponse1xxCount = -1L;
                    m_cLastResponse2xxCount = -1L;
                    m_cLastResponse3xxCount = -1L;
                    m_cLastResponse4xxCount = -1L;
                    m_cLastResponse5xxCount = -1L;
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

        /**
         * ExportableJTable that this listener applies to.
         */
        private ExportableJTable table;

        /**
         * The JTabbedPane that this listener applies to.
         */
        private JTabbedPane pneDetailTabs;

        /**
         * The currently selected row.
         */
        private int nSelectedRow;
        }

    /**
     * Populate the tabs on a change of service name.
     *
     * @param pneDetailTabs the {@link JTabbedPane} to update
     * @param sServiceName  the service name to display
     */
    private void populateTabs(JTabbedPane pneDetailTabs, String sServiceName)
        {
        // don't recreate tabs if we are updating GUI
        synchronized (this)
            {
            // remove any existing tabs
            int cTabs = pneDetailTabs.getTabCount();

            for (int i = 0; i < cTabs; i++)
                {
                pneDetailTabs.removeTabAt(0);
                }

            m_requestTimeGraph = GraphHelper.createAverageRequestTimeGraph(sServiceName);
            pneDetailTabs.addTab(getLocalizedText("LBL_average_request_time"),
                    m_requestTimeGraph.getChart());

            m_requestsPerSecondGraph = GraphHelper.createAverageRequestsPerSecondGraph(sServiceName);
            pneDetailTabs.addTab(getLocalizedText("LBL_average_request_per_second"),
                    m_requestsPerSecondGraph.getChart());

            m_requestsGraph = GraphHelper.createHttpRequestGraph(sServiceName);
            pneDetailTabs.addTab(getLocalizedText("LBL_request_history"),
                    m_requestsGraph.getChart());

            m_responseGraph = GraphHelper.createHttpResponseGraph(sServiceName);
            pneDetailTabs.addTab(getLocalizedText("LBL_response_history"),
                    m_responseGraph.getChart());
            }
        }

    @Override
    public void updateGUI()
        {
        f_tmodel.fireTableDataChanged();
        fireTableDataChangedWithSelection(f_tableDetail, f_tmodelDetail);

        if (f_model.getSelectedHttpProxyService() != null)
            {
            f_listener.updateRowSelection();
            }
        }

    @Override
    public void updateData()
        {
        m_httpProxyData = f_model.getData(VisualVMModel.DataType.HTTP_PROXY);

        if (m_httpProxyData != null)
            {
            f_tmodel.setDataList(m_httpProxyData);
            }

        m_httpProxyMemberData = f_model.getData(
                VisualVMModel.DataType.HTTP_PROXY_DETAIL);

        synchronized (this)
            {
            if (m_httpProxyMemberData != null)
                {
                f_tmodelDetail.setDataList(m_httpProxyMemberData);

                float nTotalRequestTime  = 0.0f;
                float nMaxRequestTime    = 0.0f;
                int   nCount             = 0;
                float nTotalReqPerSecond = 0.0f;
                float nMaxReqPerSecond   = 0.0f;
                long  nErrorCount        = 0L;
                long  nRequestCount      = 0L;
                long  nResponse1xxCount  = 0L;
                long  nResponse2xxCount  = 0L;
                long  nResponse3xxCount  = 0L;
                long  nResponse4xxCount  = 0L;
                long  nResponse5xxCount  = 0L;

                for (Map.Entry<Object, Data> entry : m_httpProxyMemberData)
                    {
                    float cMillisAverage = (Float) entry.getValue().getColumn(
                            HttpProxyMemberData.AVG_REQ_TIME);
                    nTotalRequestTime += cMillisAverage;
                    nCount++;
                    nErrorCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.TOTAL_ERROR_COUNT);
                    nRequestCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.TOTAL_REQUEST_COUNT);
                    nResponse1xxCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.RESPONSE_COUNT_1);
                    nResponse2xxCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.RESPONSE_COUNT_2);
                    nResponse3xxCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.RESPONSE_COUNT_3);
                    nResponse4xxCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.RESPONSE_COUNT_4);
                    nResponse5xxCount += (Long) entry.getValue().getColumn(HttpProxyMemberData.RESPONSE_COUNT_5);

                    if (cMillisAverage > nMaxRequestTime)
                        {
                        nMaxRequestTime = cMillisAverage;
                        }

                    float nRequestsPerSecond = (Float) entry.getValue().getColumn(HttpProxyMemberData.REQ_PER_SECOND);
                    nTotalReqPerSecond += nRequestsPerSecond;

                    if (nRequestsPerSecond > nMaxReqPerSecond)
                        {
                        nMaxReqPerSecond = nRequestsPerSecond;
                        }
                    }

                if (m_requestTimeGraph != null)
                    {
                    GraphHelper.addValuesToAverageRequestTimeGraph(
                            m_requestTimeGraph,
                            nMaxRequestTime,
                            (nCount == 0 ? 0 : nTotalRequestTime / nCount));
                    }

                if (m_requestsPerSecondGraph != null)
                    {
                    GraphHelper.addValuesToAverageRequestsPerSecondGraph(
                            m_requestsPerSecondGraph,
                            nMaxReqPerSecond,
                            (nCount == 0 ? 0 : nTotalReqPerSecond / nCount));
                    }

                if (m_requestsGraph != null)
                    {
                    // only update the graph if the value from the model
                    // has been changed
                    long ldtLastUpdate = f_model.getLastUpdate();
                    if (ldtLastUpdate > m_cLastUpdateTime)
                        {
                        if (m_cLastRequestCount == -1L)
                            {
                            m_cLastRequestCount = nRequestCount;
                            m_cLastErrorCount = nErrorCount;
                            }

                        if (m_cLastResponse1xxCount == -1L)
                            {
                            m_cLastResponse1xxCount = nResponse1xxCount;
                            m_cLastResponse2xxCount = nResponse2xxCount;
                            m_cLastResponse3xxCount = nResponse3xxCount;
                            m_cLastResponse4xxCount = nResponse4xxCount;
                            m_cLastResponse5xxCount = nResponse5xxCount;
                            }

                        // get delta values
                        long nDeltaErrorCount       = nErrorCount - m_cLastErrorCount;
                        long nDeltaRequestCount     = nRequestCount - m_cLastRequestCount;
                        long nDeltaResponse1xxCount = nResponse1xxCount - m_cLastResponse1xxCount;
                        long nDeltaResponse2xxCount = nResponse2xxCount - m_cLastResponse2xxCount;
                        long nDeltaResponse3xxCount = nResponse3xxCount - m_cLastResponse3xxCount;
                        long nDeltaResponse4xxCount = nResponse4xxCount - m_cLastResponse4xxCount;
                        long nDeltaResponse5xxCount = nResponse5xxCount - m_cLastResponse5xxCount;

                        GraphHelper.addValuesToHttpRequestGraph(m_requestsGraph,
                                nDeltaRequestCount < 0 ? 0 : nDeltaRequestCount,
                                nDeltaErrorCount < 0 ? 0 : nDeltaErrorCount);

                        GraphHelper.addValuesToHttpResponseGraph(m_responseGraph,
                                nDeltaResponse1xxCount <
                                0 ? 0 : nDeltaResponse1xxCount,
                                nDeltaResponse2xxCount <
                                0 ? 0 : nDeltaResponse2xxCount,
                                nDeltaResponse3xxCount <
                                0 ? 0 : nDeltaResponse3xxCount,
                                nDeltaResponse4xxCount <
                                0 ? 0 : nDeltaResponse4xxCount,
                                nDeltaResponse5xxCount <
                                0 ? 0 : nDeltaResponse5xxCount);

                        // set the last values to calculate deltas
                        m_cLastErrorCount = nErrorCount;
                        m_cLastRequestCount = nRequestCount;
                        m_cLastUpdateTime = ldtLastUpdate;
                        m_cLastResponse1xxCount = nResponse1xxCount;
                        m_cLastResponse2xxCount = nResponse2xxCount;
                        m_cLastResponse3xxCount = nResponse3xxCount;
                        m_cLastResponse4xxCount = nResponse4xxCount;
                        m_cLastResponse5xxCount = nResponse5xxCount;
                        }
                    }
                }
            }

        String sSelectedService = f_model.getSelectedHttpProxyService();

        if (sSelectedService == null)
            {
            f_txtSelectedService.setText("");
            f_txtSelectedService.setToolTipText("");
            }
        else
            {
            f_txtSelectedService.setText(sSelectedService);
            }
        }

    // ----- data members ---------------------------------------------------

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * The {@link HttpProxyTableModel} to display http proxy data.
     */
    protected final HttpProxyTableModel f_tmodel;

    /**
     * The {@link ExportableJTable} to use to display detail data.
     */
    private final ExportableJTable f_tableDetail;

    /**
     * The {@link ServiceMemberTableModel} to display service member data.
     */
    protected final HttpProxyMemberTableModel f_tmodelDetail;

    /**
     * The row selection listener.
     */
    private final SelectRowListSelectionListener f_listener;

    /**
     * The http proxy statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpProxyData;

    /**
     * The proxy member statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_httpProxyMemberData;

    /**
     * The currently selected service from the service table.
     */
    private final JTextField f_txtSelectedService;

    /**
     * The graph of request time.
     */
    private SimpleXYChartSupport m_requestTimeGraph;

    /**
     * The graph of average requests per second time.
     */
    private SimpleXYChartSupport m_requestsPerSecondGraph;

    /**
     * The graph of requests and errors
     */
    private SimpleXYChartSupport m_requestsGraph;

    /**
     * The graphs of response codes
     */
    private SimpleXYChartSupport m_responseGraph;

    /**
     * Last error count.
     */
    private long m_cLastErrorCount = -1L;

    /**
     * Last request count.
     */
    private long m_cLastRequestCount = -1L;

    /**
     * Last update time for stats.
     */
    private long m_cLastUpdateTime = -1L;

    /**
     * Last status 100-199 count.
     */
    private long m_cLastResponse1xxCount = -1L;

    /**
     * Last status 200-299 count.
     */
    private long m_cLastResponse2xxCount = -1L;

    /**
     * Last status 300-399 count.
     */
    private long m_cLastResponse3xxCount = -1L;

    /**
     * Last status 400-499 count.
     */
    private long m_cLastResponse4xxCount = -1L;

    /**
     * Last status 500-599 count.
     */
    private long m_cLastResponse5xxCount = -1L;
    }
