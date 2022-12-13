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
import com.oracle.coherence.plugin.visualvm.helper.DialogHelper;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.ServiceMemberTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.ServiceTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceMemberData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.AbstractData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized proxy server statistics.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CoherenceServicePanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceServicePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceServicePanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        f_tmodel = new ServiceTableModel(VisualVMModel.DataType.SERVICE.getMetadata());
        f_tmodelDetail = new ServiceMemberTableModel(VisualVMModel.DataType.SERVICE_DETAIL.getMetadata());

        final ExportableJTable table = new ExportableJTable(f_tmodel, model);
        f_tableDetail = new ExportableJTable(f_tmodelDetail, model);

        if (model.getClusterVersionAsInt() >= 122100)
            {
            // only available in 12.2.1 and above
            table.setMenuOptions(new MenuOption[]{
                    new RightClickMenuOption(model, m_requestSender, table, REPORT_DISTRIBUTIONS),
                    new RightClickMenuOption(model, m_requestSender, table, SHOW_PARTITION_STATS)});
            }
        else if (model.getClusterVersionAsInt() >= 121200)
            {
            // only available in 12.1.2 and above
            table.setMenuOptions(new MenuOption[] {new RightClickMenuOption(model, m_requestSender, table, REPORT_DISTRIBUTIONS) });
            }

        // define renderers for the columns
        RenderHelper.setColumnRenderer(table, ServiceData.SERVICE_NAME, new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(table, ServiceData.STATUS_HA, new RenderHelper.StatusHARenderer());
        RenderHelper.setIntegerRenderer(table, ServiceData.PARTITION_COUNT);
        RenderHelper.setIntegerRenderer(table, ServiceData.PARTITIONS_ENDANGERED);
        RenderHelper.setIntegerRenderer(table, ServiceData.PARTITIONS_VULNERABLE);
        RenderHelper.setIntegerRenderer(table, ServiceData.PARTITIONS_UNBALANCED);
        RenderHelper.setIntegerRenderer(table, ServiceData.REQUESTS_PENDING);

        RenderHelper.setColumnRenderer(f_tableDetail, ServiceMemberData.REQUEST_AVERAGE_DURATION,
                                       new RenderHelper.DecimalRenderer());
        RenderHelper.setColumnRenderer(f_tableDetail, ServiceMemberData.TASK_AVERAGE_DURATION,
                                       new RenderHelper.DecimalRenderer());

        RenderHelper.setHeaderAlignment(table, SwingConstants.CENTER);
        RenderHelper.setHeaderAlignment(f_tableDetail, SwingConstants.CENTER);

        table.setPreferredScrollableViewportSize(new Dimension(500, table.getRowHeight() * 5));
        f_tableDetail.setPreferredScrollableViewportSize(new Dimension(700, 125));

        f_tableDetail.setMenuOptions(new MenuOption[] {new ShowDetailMenuOption(model, f_tableDetail, SELECTED_SERVICE) });

        // Add some space
        table.setIntercellSpacing(new Dimension(6, 3));
        table.setRowHeight(table.getRowHeight() + 4);

        f_tableDetail.setIntercellSpacing(new Dimension(6, 3));
        f_tableDetail.setRowHeight(table.getRowHeight() + 4);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll       = new JScrollPane(table);
        JScrollPane pneScrollDetail = new JScrollPane(f_tableDetail);
        configureScrollPane(pneScroll, table);
        configureScrollPane(pneScrollDetail, f_tableDetail);

        pneSplit.add(pneScroll);
        pneSplit.setOpaque(false);

        // create the detail pane
        JPanel pneDetail = new JPanel();

        pneDetail.setLayout(new BorderLayout());
        pneDetail.setOpaque(false);

        JPanel detailHeaderPanel = new JPanel();
        detailHeaderPanel.setOpaque(false);

        f_txtSelectedService = getTextField(22, SwingConstants.LEFT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_selected_service", f_txtSelectedService));
        detailHeaderPanel.add(f_txtSelectedService);

        f_txtTotalThreads = getTextField(5, SwingConstants.RIGHT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_total_threads", f_txtTotalThreads));
        detailHeaderPanel.add(f_txtTotalThreads);

        f_txtTotalIdle = getTextField(5, SwingConstants.RIGHT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_total_idle", f_txtTotalIdle));
        detailHeaderPanel.add(f_txtTotalIdle);

        f_txtTotalThreadUtil = getTextField(5, SwingConstants.RIGHT);
        detailHeaderPanel.add(getLocalizedLabel("LBL_total_utilization", f_txtTotalThreadUtil));
        detailHeaderPanel.add(f_txtTotalThreadUtil);

        final JSplitPane pneSplitDetail = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitDetail.setOpaque(false);

        pneDetail.add(detailHeaderPanel, BorderLayout.PAGE_START);
        pneSplitDetail.add(pneScrollDetail);

        final JTabbedPane pneDetailTabs = new JTabbedPane();
        pneDetailTabs.setOpaque(false);

        populateTabs(pneDetailTabs, getLocalizedText("LBL_none_selected"));

        pneSplitDetail.add(pneDetailTabs);

        pneDetail.add(pneSplitDetail, BorderLayout.CENTER);

        pneSplit.add(pneDetail);
        add(pneSplit);

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_tableDetail, ServiceMemberData.THREAD_UTILISATION_PERCENT,
                                       new RenderHelper.ThreadUtilRenderer());

        // add a listener for the selected row
        ListSelectionModel rowSelectionModel = table.getSelectionModel();

        m_listener = new SelectRowListSelectionListener(table, pneDetailTabs);
        rowSelectionModel.addListSelectionListener(m_listener);
        }

    /**
     * Populate the tabs on a change of service name.
     *
     * @param pneDetailTabs  the {@link JTabbedPane} to update
     * @param sServiceName   the service name to display
     */
    private void populateTabs(JTabbedPane pneDetailTabs, String sServiceName)
        {
        // remove any existing tabs
        int cTabs = pneDetailTabs.getTabCount();

        for (int i = 0; i < cTabs; i++)
            {
            pneDetailTabs.removeTabAt(0);
            }

        m_threadUtilGraph = GraphHelper.createThreadUtilizationGraph(sServiceName);
        pneDetailTabs.addTab(getLocalizedText("LBL_thread_utilization"), m_threadUtilGraph.getChart());

        m_taskAverageGraph = GraphHelper.createTaskDurationGraph(sServiceName);
        pneDetailTabs.addTab(getLocalizedText("LBL_task_average_duration"), m_taskAverageGraph.getChart());

        m_taskBacklogGraph = GraphHelper.createTaskBacklogGraph(sServiceName);
        pneDetailTabs.addTab(getLocalizedText("LBL_task_backlog"), m_taskBacklogGraph.getChart());

        m_requestAverageGraph = GraphHelper.createRequestDurationGraph(sServiceName);
        pneDetailTabs.addTab(getLocalizedText("LBL_request_average_duration"), m_requestAverageGraph.getChart());

        m_servicePartitionsGraph = GraphHelper.createServicePartitionGraph(sServiceName);
        pneDetailTabs.addTab(getLocalizedText("LBL_service_partitions"), m_servicePartitionsGraph.getChart());
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        f_tmodel.fireTableDataChanged();

        fireTableDataChangedWithSelection(f_tableDetail, f_tmodelDetail);

        if (f_model.getSelectedService() != null)
            {
            m_listener.updateRowSelection();
            }

        }

    @Override
    public void updateData()
        {
        m_serviceData = f_model.getData(VisualVMModel.DataType.SERVICE);

        if (m_serviceData != null)
            {
            f_tmodel.setDataList(m_serviceData);
            }

        m_serviceMemberData = f_model.getData(VisualVMModel.DataType.SERVICE_DETAIL);

        // the serviceMemberData is only populated if a service has been selected
        // in the first table
        if (m_serviceMemberData != null)
            {
            f_tmodelDetail.setDataList(m_serviceMemberData);

            int   nTotalThreads        = 0;
            int   nTotalIdle           = 0;
            float nTotalTaskAverage    = 0.0f;
            float nMaxTaskAverage      = 0.0f;
            int   cTotalTaskAverage    = 0;
            float nTotalRequestAverage = 0.0f;
            float nMaxRequestAverage   = 0.0f;
            float cAverage             = 0.0f;
            int   cTotalRequestAverage = 0;
            int   nTotalTaskBacklog    = 0;
            int   nMaxTaskBacklog      = 0;

            for (Entry<Object, Data> entry : m_serviceMemberData)
                {
                int cThread = (Integer) entry.getValue().getColumn(ServiceMemberData.THREAD_COUNT);
                int cIdle   = (Integer) entry.getValue().getColumn(ServiceMemberData.THREAD_IDLE_COUNT);

                nTotalThreads += (cThread != -1 ? cThread : 0);
                nTotalIdle    += (cIdle != -1 ? cIdle : 0);

                // only include task averages where there is a thread count
                if (cThread > 0)
                    {
                    // update values for taks average duration
                    cTotalTaskAverage++;

                    cAverage          = (Float) entry.getValue().getColumn(ServiceMemberData.TASK_AVERAGE_DURATION);

                    nTotalTaskAverage += cAverage;

                    if (cAverage > nMaxTaskAverage)
                        {
                        nMaxTaskAverage = cAverage;
                        }

                    // update values for task backlog
                    int cTaskBacklog = (Integer) entry.getValue().getColumn(ServiceMemberData.TASK_BACKLOG);

                    nTotalTaskBacklog += cTaskBacklog;

                    if (cTaskBacklog > nMaxTaskBacklog)
                        {
                        nMaxTaskBacklog = cTaskBacklog;
                        }
                    }

                // calculate request average always, as thread count no relevant
                cTotalRequestAverage++;
                cAverage             = (Float) entry.getValue().getColumn(ServiceMemberData.REQUEST_AVERAGE_DURATION);
                nTotalRequestAverage += cAverage;

                if (cAverage > nMaxRequestAverage)
                    {
                    nMaxRequestAverage = cAverage;
                    }
                }

            // update the totals
            float threadUtil = nTotalThreads == 0 ? 0f : (float) (nTotalThreads - nTotalIdle) / nTotalThreads;

            if (m_threadUtilGraph != null)
                {
                GraphHelper.addValuesToThreadUtilizationGraph(m_threadUtilGraph, (long) (threadUtil * 100));
                }

            if (m_taskAverageGraph != null)
                {
                GraphHelper.addValuesToTaskDurationGraph(m_taskAverageGraph, nMaxTaskAverage,
                    (cTotalTaskAverage == 0 ? 0 : (nTotalTaskAverage / cTotalTaskAverage)));
                }

            if (m_taskBacklogGraph != null)
                {
                GraphHelper.addValuesToTaskBacklogGraph(m_taskBacklogGraph, nMaxTaskBacklog,
                    (cTotalTaskAverage == 0 ? 0 : (nTotalTaskBacklog / cTotalTaskAverage)));
                }

            if (m_requestAverageGraph != null)
                {
                GraphHelper.addValuesToRequestDurationGraph(m_requestAverageGraph, nMaxRequestAverage,
                    (cTotalRequestAverage == 0 ? 0 : (nTotalRequestAverage / cTotalRequestAverage)));
                }

            if (m_servicePartitionsGraph != null)
                {
                for (Entry<Object, Data> entry : m_serviceData)
                    {
                    // get the service details for the selected service
                    if (entry.getKey().equals(f_model.getSelectedService()))
                        {
                        GraphHelper.addValuesToServicePartitionGraph(m_servicePartitionsGraph,
                                (Integer)entry.getValue().getColumn(ServiceData.PARTITIONS_ENDANGERED),
                                (Integer)entry.getValue().getColumn(ServiceData.PARTITIONS_VULNERABLE),
                                (Integer)entry.getValue().getColumn(ServiceData.PARTITIONS_UNBALANCED),
                                (Integer)entry.getValue().getColumn(ServiceData.REQUESTS_PENDING));
                        break;
                        }
                    }
                }

            setThreadValues(nTotalThreads, nTotalIdle, threadUtil);

            }
        else
            {
            setThreadValues(0, 0, 0f);
            }

        String sSelectedService = f_model.getSelectedService();

        if (sSelectedService == null)
            {
            f_txtSelectedService.setText("");
            }
        else
            {
            f_txtSelectedService.setText(sSelectedService);
            }
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Update the thread values on the selected service pane.
     *
     * @param nTotalThreads  the total number of threads available across the selected service
     * @param nTotalIdle     the total number of idle threads across the selected service
     * @param nThreadUtil    the percentage thread utilization across the selected service
     */
    private void setThreadValues(int nTotalThreads, int nTotalIdle, float nThreadUtil)
        {
        f_txtTotalThreads.setText(String.format("%,5d", nTotalThreads));
        f_txtTotalIdle.setText(String.format("%,5d", nTotalIdle));
        f_txtTotalThreadUtil.setText(String.format("%3.1f%%", nThreadUtil * 100));
        }

    // ----- inner classes --------------------------------------------------

    /**
     * Right-click option for service table.
     */
    protected class RightClickMenuOption
            extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public RightClickMenuOption(VisualVMModel model, RequestSender requestSender,
            ExportableJTable jtable, int nOption)
            {
            super(model, requestSender, jtable);
            f_nOption = nOption;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return getLocalizedText(f_nOption == REPORT_DISTRIBUTIONS
                    ? "LBL_report_sched_dist"
                    : "LBL_partition_stats");
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            String sService    = null;
            String sRawService = null;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    sRawService    = (String) getJTable().getModel().getValueAt(nRow, 0);
                    String sResult = null;

                    String[] asParts          = AbstractData.getServiceParts(sRawService);
                    String   sDomainPartition = asParts.length == 1 ? null : asParts[0];
                    sService                  = sDomainPartition == null ? sRawService : asParts[1];

                    if (f_nOption == REPORT_DISTRIBUTIONS)
                        {
                        sResult = m_requestSender.getScheduledDistributions(sService, sDomainPartition);
                        }
                    else if (f_nOption == SHOW_PARTITION_STATS)
                        {
                        Set<Object[]> setResults = m_requestSender.getPartitionAssignmentAttributes(sService, sDomainPartition);

                        if (setResults.size() == 1)
                            {
                            Object[] aoResults = setResults.iterator().next();

                            StringBuilder sb = new StringBuilder(
                                     Localization.getLocalText("LBL_partitions_stats_title", sRawService));

                            sb.append("\n\n")
                                .append(getLocalizedText("LBL_avg_partition_size"))
                                .append(formatLong(aoResults[0]))
                                .append("\n")
                                .append(getLocalizedText("LBL_max_partition_size"))
                                .append(formatLong(aoResults[1]))
                                .append("\n")
                                .append(getLocalizedText("LBL_avg_storage_size"))
                                .append(formatLong(aoResults[2]))
                                .append("\n")
                                .append(getLocalizedText("LBL_max_storage_size"))
                                .append(formatLong(aoResults[3]))
                                .append("\n")
                                .append(getLocalizedText("LBL_max_Load_node"))
                                .append(formatLong(aoResults[4]))
                                .append("\n");

                            sResult = sb.toString();
                            }
                        }
                    else
                        {
                        throw new RuntimeException("Invalid option " + f_nOption);
                        }

                    showMessageDialog(Localization.getLocalText("LBL_details_service", sRawService),
                                                                sResult, JOptionPane.INFORMATION_MESSAGE);
                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("ERR_cannot_run", sRawService),
                                      getSanitizedMessage(ee), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        /**
         * Return a sanitized message to make common errors more meaningful.
         * @param e {@link Exception} to get message from
         * @return final message
         */
        private String getSanitizedMessage(Exception e)
            {
            String sError = e.getMessage();
            return sError.contains("name cannot be null") ? "Node no longer available or operation not valid for service type." : sError;
            }

        /**
         * Format a long value
         *
         * @param oValue the value to convert
         *
         * @return a converted value if a Long otherwise .toString()
         */
        private String formatLong(Object oValue)
            {
            final String SEP = "    ";

            if (oValue instanceof Long)
                {
                return SEP + RenderHelper.INTEGER_FORMAT.format((Long)oValue);
                }
            else
                {
                return SEP + oValue.toString();
                }
            }

        // ----- data members -------------------------------------------

        /**
         * The selected option.
         */
        final int f_nOption;
        }

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
         * @param table         the {@link ExportableJTable} that is to be selected
         * @param pneDetailTabs the {@link JTabbedPane} to attach to
         */
        public SelectRowListSelectionListener(ExportableJTable table, JTabbedPane pneDetailTabs)
            {
            this.table         = table;
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

                if (!sSelectedService.equals(f_model.getSelectedService()))
                    {
                    f_model.setSelectedService(sSelectedService);
                    f_model.eraseServiceMemberData();
                    f_tmodelDetail.setDataList(null);
                    f_tmodelDetail.fireTableDataChanged();

                    f_txtSelectedService.setText(sSelectedService);
                    setThreadValues(0, 0, 0f);

                    populateTabs(pneDetailTabs, sSelectedService);
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
        private JTabbedPane      pneDetailTabs;

        /**
         * The currently selected row.
         */
        private int nSelectedRow;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412396L;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(CoherenceServicePanel.class.getName());

    /**
     * Right click option for reporting distributions.
     */
    private final int REPORT_DISTRIBUTIONS = 0;

    /**
     * Right click option for showing partitions statistics.
     */
    private final int SHOW_PARTITION_STATS = 1;

    // ----- data members ---------------------------------------------------

    /**
     * The currently selected service from the service table.
     */
    private final JTextField f_txtSelectedService;

    /**
     * The total number of threads available across all services.
     */
    private final JTextField f_txtTotalThreads;

    /**
     * The total number of idle threads across the selected service
     */
    private final JTextField f_txtTotalIdle;

    /**
     * The percentage thread utilization across the selected service.
     */
    private final JTextField f_txtTotalThreadUtil;

    /**
     * The {@link ServiceTableModel} to display service data.
     */
    protected final ServiceTableModel f_tmodel;

    /**
     * The {@link ServiceMemberTableModel} to display service member data.
     */
    protected final ServiceMemberTableModel f_tmodelDetail;

    /**
     * The service statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_serviceData;

    /**
     * The service member statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_serviceMemberData;

    /**
     * The graph of thread utilization percent.
     */
    private transient SimpleXYChartSupport m_threadUtilGraph = null;

    /**
     * The graph of task average duration.
     */
    private transient SimpleXYChartSupport m_taskAverageGraph = null;

    /**
     * The graph of task backlog.
     */
    private transient SimpleXYChartSupport m_taskBacklogGraph = null;

    /**
     * The graph of request average.
     */
    private transient SimpleXYChartSupport m_requestAverageGraph = null;

    /**
     * The graph of service partitions.
     */
    private transient SimpleXYChartSupport m_servicePartitionsGraph = null;

    /**
     * The row selection listener.
     */
    private SelectRowListSelectionListener m_listener;

    /**
     * the {@link ExportableJTable} to use to display detail data.
     */
    private final ExportableJTable f_tableDetail;
    }
