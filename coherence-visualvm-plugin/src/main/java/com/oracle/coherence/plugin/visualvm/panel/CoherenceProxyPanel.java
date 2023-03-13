/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.ProxyTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ProxyConnectionData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ProxyData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * An implementation of an {@link AbstractCoherencePanel} to view
 * summarized proxy server data.
 *
 * @author tam  2013.11.14
 */
public class CoherenceProxyPanel
        extends AbstractCoherencePanel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceProxyPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceProxyPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalProxyServers = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_proxy_servers", f_txtTotalProxyServers));
        pnlHeader.add(f_txtTotalProxyServers);

        f_txtTotalConnections = getTextField(5, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_connections", f_txtTotalConnections));
        pnlHeader.add(f_txtTotalConnections);

        // special processing for Name Service
        if (model.is1213AndAbove())
            {
            if (model.getClusterVersionAsInt() >= 122100)
                {
                // NameService no longer shows up under ConnectionManagerMBean in
                // 12.2.1 and above so disable checkbox entirely
                m_cbxIncludeNameService = null;
                }
            else
                {
                // NameService was visible in 12.1.3 as a service in ConnectionManagerMBean
                // so allow user to choose whether to display or not
                m_cbxIncludeNameService = new JCheckBox(Localization.getLocalText("LBL_include_name_service"));
                m_cbxIncludeNameService.setMnemonic(KeyEvent.VK_N);
                m_cbxIncludeNameService.setSelected(false);
                pnlHeader.add(m_cbxIncludeNameService);
                }
            }

        // create the table
        f_tmodel = new ProxyTableModel(VisualVMModel.DataType.PROXY.getMetadata());

        f_table = new ExportableJTable(f_tmodel, model);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        f_table.setMenuOptions(new MenuOption[] {new RightClickMenuOption(model, m_requestSender, f_table) });

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, ProxyData.TOTAL_BYTES_RECEIVED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, ProxyData.TOTAL_BYTES_SENT, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, ProxyData.TOTAL_MSG_RECEIVED, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, ProxyData.TOTAL_MSG_SENT, new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

        // Create the scroll pane and add the table to it.
        JScrollPane pneScroll = new JScrollPane(f_table);
        configureScrollPane(pneScroll, f_table);
        setTablePadding(f_table);
        pneScroll.setOpaque(false);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        pnlTop.add(pnlHeader, BorderLayout.PAGE_START);
        pnlTop.add(pneScroll, BorderLayout.CENTER);

        JSplitPane pneSplitPlotter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pneSplitPlotter.setResizeWeight(0.5);
        pneSplitPlotter.setOpaque(false);

        // create a chart for the count of proxy server connections
        f_proxyGraph = GraphHelper.createTotalProxyConnectionsGraph();
        f_proxyStatsGraph = GraphHelper.createProxyServerStatsGraph();

        pneSplitPlotter.add(f_proxyGraph.getChart());
        pneSplitPlotter.add(f_proxyStatsGraph.getChart());

        pneSplit.add(pnlTop);
        pneSplit.add(pneSplitPlotter);

        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    @Override
    public void updateGUI()
        {
        final String MEM_FORMAT = "%,d";
        int   cTotalConnections = 0;
        long  nSentCount        = 0L;
        long  nRecCount         = 0L;

        if (m_proxyData != null)
            {
            f_txtTotalProxyServers.setText(String.format("%5d", m_proxyData.size()));

            for (Entry<Object, Data> entry : m_proxyData)
                {
                cTotalConnections += (Integer) entry.getValue().getColumn(ProxyData.CONNECTION_COUNT);
                nSentCount        += (Long) entry.getValue().getColumn(ProxyData.TOTAL_BYTES_SENT);
                nRecCount         += (Long) entry.getValue().getColumn(ProxyData.TOTAL_BYTES_RECEIVED);
                }

            f_txtTotalConnections.setText(String.format(MEM_FORMAT, cTotalConnections));
            }
        else
            {
            f_txtTotalProxyServers.setText(String.format(MEM_FORMAT, 0));
            f_txtTotalConnections.setText(String.format(MEM_FORMAT, 0));
            }

        fireTableDataChangedWithSelection(f_table, f_tmodel);

        GraphHelper.addValuesToTotalProxyConnectionsGraph(f_proxyGraph, cTotalConnections);

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

            GraphHelper.addValuesToProxyServerStatsGraph(f_proxyStatsGraph,
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
        // update the model to indicate if we are going to include the NameService
        f_model.setIncludeNameService(m_cbxIncludeNameService != null && m_cbxIncludeNameService.isSelected());

        m_proxyData = f_model.getData(VisualVMModel.DataType.PROXY);

        f_tmodel.setDataList(m_proxyData);
        }

   // ----- inner classes --------------------------------------------------

    /**
     * Right-click option to show proxy service connections.
     */
    protected class RightClickMenuOption
            extends AbstractMenuOption
        {
        // ----- constructors -----------------------------------------------

        /**
         * {@inheritDoc}
         */
        public RightClickMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable)
            {
            super(model, requestSender, jtable);

            f_tmodel = new DefaultTableModel(new Object[]
                    {
                    Localization.getLocalText("LBL_UUID"),
                    Localization.getLocalText("LBL_connection_millis"), Localization.getLocalText("LBL_connection_time"),
                    Localization.getLocalText("LBL_remote_address"), Localization.getLocalText("LBL_data_sent"),
                    Localization.getLocalText("LBL_data_rec"), Localization.getLocalText("LBL_backlog"),
                    Localization.getLocalText("LBL_client_process"), Localization.getLocalText("LBL_role")
                    }, COLUMN_COUNT) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            f_table = new ExportableJTable(f_tmodel, f_model);

            RenderHelper.setColumnRenderer(f_table, ProxyConnectionData.UUID, new RenderHelper.ToolTipRenderer());
            RenderHelper.setIntegerRenderer(f_table, 1);    // connection millis
            setColumnRenderer(f_table, 2, null);     // Connection Time
            RenderHelper.setColumnRenderer(f_table, 4, new RenderHelper.BytesRenderer());     // bytes sent
            RenderHelper.setColumnRenderer(f_table, 5, new RenderHelper.BytesRenderer());     // bytes rec
            setColumnRenderer(f_table, 6, null);     // Client process
            setColumnRenderer(f_table, 7, null);     // Backlog

            RenderHelper.setHeaderAlignment(f_table, SwingConstants.CENTER);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            f_table.setPreferredScrollableViewportSize(new Dimension((Math.max((int) (screenSize.getWidth() * 0.5),
                800)), f_table.getRowHeight() * 10));

            setTablePadding(f_table);

            f_pneMessage = new JScrollPane(f_table);
            configureScrollPane(f_pneMessage, f_table);
            AbstractMenuOption.setResizable(f_pneMessage);
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return Localization.getLocalText("LBL_proxy_connections");
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int nRow = getSelectedRow();
            String sService = null;
            int    nNodeId;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog(getLocalizedText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    sService    = (String) getJTable().getModel().getValueAt(nRow, 1);
                    nNodeId     = (Integer) getJTable().getModel().getValueAt(nRow, 2);
                    m_sDescription = sService + " and nodeId " + nNodeId;

                    int row = 0;

                    // remove any existing rows
                    f_tmodel.getDataVector().removeAllElements();
                    f_tmodel.fireTableDataChanged();

                    SortedMap<Object, Data> mapData = new TreeMap<>();

                    Set<ObjectName> proxyConnectionsSet = m_requestSender.getProxyConnections(sService, nNodeId);

                    for (Iterator<ObjectName> nodIter = proxyConnectionsSet.iterator(); nodIter.hasNext(); ) {
                        ObjectName    proxyNameObjName = nodIter.next();
                        Data          data             = new ProxyConnectionData();
                        String        sUUID            = proxyNameObjName.getKeyProperty("UUID");
                        AttributeList listAttr         = null;

                        if (m_requestSender instanceof JMXRequestSender)
                            {
                            listAttr = m_requestSender.getAttributes(proxyNameObjName,
                                new String[] {ATTR_CONNECTION_TIME_MILLIS, ATTR_REMOTE_ADDRESS, ATTR_CLIENT_ROLE, ATTR_BACKLOG,
                                        ATTR_REMOTE_PORT, ATTR_BYTES_SENT, ATTR_BYTES_REC, ATTR_CLIENT_PROCESS_NAME});
                            }

                        long cMillis = Long.parseLong(getKeyProperty(listAttr, proxyNameObjName, ATTR_CONNECTION_TIME_MILLIS));
                        Instant start = Instant.ofEpochMilli(0);
                        Instant end = Instant.ofEpochMilli(cMillis);
                        String  sConnTime = Duration.between(start, end).toString().replace("PT", "");

                        data.setColumn(ProxyConnectionData.UUID, sUUID);
                        data.setColumn(ProxyConnectionData.CONN_MILLIS, cMillis);
                        data.setColumn(ProxyConnectionData.CONN_TIME, sConnTime);
                        String sRemoteAddress = getKeyProperty(listAttr, proxyNameObjName, ATTR_REMOTE_ADDRESS);
                        int    nRemotePort    = Integer.parseInt(getKeyProperty(listAttr, proxyNameObjName, ATTR_REMOTE_PORT));

                        data.setColumn(ProxyConnectionData.REMOTE_ADDRESS_PORT, String.format("%s:%d", sRemoteAddress, nRemotePort));
                        data.setColumn(ProxyConnectionData.BACKLOG,
                                Long.parseLong(getKeyProperty(listAttr, proxyNameObjName, ATTR_BACKLOG)));
                        data.setColumn(ProxyConnectionData.DATA_SENT,
                                Long.parseLong(getKeyProperty(listAttr, proxyNameObjName, ATTR_BYTES_SENT)));
                        data.setColumn(ProxyConnectionData.DATA_REC,
                                Long.parseLong(getKeyProperty(listAttr, proxyNameObjName, ATTR_BYTES_REC)));
                        data.setColumn(ProxyConnectionData.BACKLOG,
                                Long.parseLong(getKeyProperty(listAttr, proxyNameObjName, ATTR_BACKLOG)));
                        data.setColumn(ProxyConnectionData.CLIENT_ROLE, getKeyProperty(listAttr, proxyNameObjName, ATTR_CLIENT_ROLE));
                        data.setColumn(ProxyConnectionData.CLIENT_PROCESS, getKeyProperty(listAttr, proxyNameObjName, ATTR_CLIENT_PROCESS_NAME));
                        mapData.put(sUUID, data);
                    }

                    // loop through the model and format nicely
                    for (Map.Entry<Object, Data> entry : mapData.entrySet())
                        {
                        Data data = entry.getValue();

                        f_tmodel.insertRow(row++, new Object[]
                            {
                            data.getColumn(ProxyConnectionData.UUID),
                            data.getColumn(ProxyConnectionData.CONN_MILLIS),
                            data.getColumn(ProxyConnectionData.CONN_TIME),
                            data.getColumn(ProxyConnectionData.REMOTE_ADDRESS_PORT),
                            data.getColumn(ProxyConnectionData.DATA_SENT),
                            data.getColumn(ProxyConnectionData.DATA_REC),
                            data.getColumn(ProxyConnectionData.BACKLOG),
                            data.getColumn(ProxyConnectionData.CLIENT_PROCESS),
                            data.getColumn(ProxyConnectionData.CLIENT_ROLE),
                            });
                       }

                    f_tmodel.fireTableDataChanged();

                    JOptionPane.showMessageDialog(null, f_pneMessage,
                            Localization.getLocalText("LBL_details_service", String.format("%s / Node Id: %d", sService, nNodeId)),
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("LBL_error"),
                                      ee.toString(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        private String getKeyProperty(AttributeList listAttr, ObjectName objectName, String sAttribute)
            {
            if (m_requestSender instanceof HttpRequestSender)
                {
                // change first letter to lower case
                String sAttr = sAttribute.substring(0,1).toLowerCase() + sAttribute.substring(1);
                return objectName.getKeyProperty(sAttr);
                }
            // use JMX request sender
            return getAttributeValueAsString(listAttr, sAttribute);
            }

        // ----- static -----------------------------------------------------

        // various attributes
        private static final String ATTR_CONNECTION_TIME_MILLIS = "ConnectionTimeMillis";
        private static final String ATTR_REMOTE_ADDRESS         = "RemoteAddress";
        private static final String ATTR_REMOTE_PORT            = "RemotePort";
        private static final String ATTR_BYTES_SENT             = "TotalBytesSent";
        private static final String ATTR_BYTES_REC              = "TotalBytesReceived";
        private static final String ATTR_CLIENT_ROLE            = "ClientRole";
        private static final String ATTR_BACKLOG                = "OutgoingByteBacklog";
        private static final String ATTR_CLIENT_PROCESS_NAME    = "ClientProcessName";

        /**
         * Column count.
         */
        private static final int COLUMN_COUNT = ProxyConnectionData.CLIENT_ROLE + 1;

        // ----- data members ---------------------------------------------------

        /**
         * Description for the table.
         */
        private String m_sDescription;

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

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7612569043492412546L;

    // ----- data members ----------------------------------------------------

    /**
     * The total number of proxy servers (tcp-acceptors) running in the cluster.
     */
    private final JTextField f_txtTotalProxyServers;

    /**
     * The total number of proxy server connections.
     */
    private final JTextField f_txtTotalConnections;

    /**
     * A check-box to indicate if the NameService should be included in the list of proxy servers.
     */
    private JCheckBox m_cbxIncludeNameService = null;

    /**
     * The graph of proxy server connections.
     */
    private final transient SimpleXYChartSupport f_proxyGraph;

    /**
     * The graph of proxy server stats.
     */
    private final transient SimpleXYChartSupport f_proxyStatsGraph;

    /**
     * The proxy statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_proxyData;

    /**
     * The {@link ProxyTableModel} to display proxy data.
     */
    protected final ProxyTableModel f_tmodel;

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
