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

import com.oracle.coherence.plugin.visualvm.GlobalPreferences;
import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.JMXRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.panel.util.MenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.SeparatorMenuOption;
import com.oracle.coherence.plugin.visualvm.tablemodel.PersistenceNotificationsTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.PersistenceTableModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceNotificationsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_PERSISTENCE_LIST_ENABLED;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view summarized persistence data.<br>
 *
 * @author tam  2013.11.14
 * @since  12.2.1.0.0
 */
public class CoherencePersistencePanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link AbstractCoherencePanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherencePersistencePanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        // create a split pane for resizing
        JSplitPane pneSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pneSplit.setOpaque(false);

        // Create the header panel
        JPanel pnlHeader = new JPanel();

        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtTotalActiveSpaceUsed = getTextField(10, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_total_active_space", f_txtTotalActiveSpaceUsed));
        pnlHeader.add(f_txtTotalActiveSpaceUsed);

        f_txtMaxLatency = getTextField(6, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_max_latency_across_services", f_txtMaxLatency));
        pnlHeader.add(f_txtMaxLatency);

        m_cbxCollectNotifications = new JCheckBox(Localization.getLocalText("LBL_collect_notifications"));
        m_cbxCollectNotifications.setMnemonic(KeyEvent.VK_I);
        m_cbxCollectNotifications.setSelected(false);
        f_listenerCbx = new CheckBoxListener();
        m_cbxCollectNotifications.addItemListener(f_listenerCbx);
        pnlHeader.add(m_cbxCollectNotifications);

        // default to true for JMX notification collection
        m_cbxCollectNotifications.setSelected(true);

        m_btnClearNotifications = new JButton(getLocalizedText("BTN_clear_notifications"));
        m_btnClearNotifications.setMnemonic(KeyEvent.VK_C);
        m_btnClearNotifications.addActionListener(new ClearNotificationsListener());
        pnlHeader.add(m_btnClearNotifications);

        // create the table
        f_tmodel = new PersistenceTableModel(VisualVMModel.DataType.PERSISTENCE.getMetadata());

        f_table = new ExportableJTable(f_tmodel);

        f_table.setPreferredScrollableViewportSize(new Dimension(500, 150));

        // only available in 12.2.1 and above.
        if (model.getClusterVersionAsInt() >= 122100)
            {
            MenuOption optionListSnapshots = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_list_snapshots", GET_SNAPSHOTS, false, false);
            MenuOption optionListArchivedSnapshots = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_list_archived_snapshots", LIST_ARCHIVED_SNAPSHOTS, false, false);
            MenuOption optionCurrentStatus = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_current_status", GET_OPERATION_STATUS, false, false);
            MenuOption optionCreateSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_create_snapshot", CREATE_SNAPSHOT, true, true);
            MenuOption optionRecoverSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_recover_snapshot", RECOVER_SNAPSHOT, true, true);
            MenuOption optionRemoveSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_remove_snapshot", REMOVE_SNAPSHOT, true, true);
            MenuOption optionArchiveSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_archive_snapshot", ARCHIVE_SNAPSHOT, true, true);
            MenuOption optionRetrieveArchivedSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_retrieve_snapshot", RETRIEVE_ARCHIVED_SNAPSHOT, true, true);
            MenuOption optionRemoveArchivedSnapshot = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_purge_snapshot", REMOVE_ARCHIVED_SNAPSHOT, true, true);
            MenuOption separator = new SeparatorMenuOption(model, m_requestSender, f_table);

            // force recovery option only available in 12.2.1.1.0 and above
            if (model.getClusterVersionAsInt() >= 122110)
                {
                 MenuOption optionForceRecovery = new PersistenceInvokeMenuOption(model, m_requestSender, f_table,
                                                  "LBL_force_recovery", FORCE_RECOVERY, true, false);
                 f_table.setMenuOptions(new MenuOption[]
                    {optionListSnapshots, optionListArchivedSnapshots, optionCurrentStatus, separator,
                     optionCreateSnapshot, optionRecoverSnapshot, optionRemoveSnapshot, separator,
                     optionArchiveSnapshot, optionRetrieveArchivedSnapshot, optionRemoveArchivedSnapshot,
                     separator, optionForceRecovery});
                }
            else
                {
                f_table.setMenuOptions(new MenuOption[]
                       {optionListSnapshots, optionListArchivedSnapshots, optionCurrentStatus, separator,
                        optionCreateSnapshot, optionRecoverSnapshot, optionRemoveSnapshot, separator,
                        optionArchiveSnapshot, optionRetrieveArchivedSnapshot, optionRemoveArchivedSnapshot});
                }
            }

        // define renderers for the columns
        RenderHelper.setColumnRenderer(f_table, PersistenceData.TOTAL_ACTIVE_SPACE_USED,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, PersistenceNotificationsData.SERVICE,
                                       new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(f_table, PersistenceData.MAX_LATENCY, new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_table, PersistenceData.AVERAGE_LATENCY,
                                       new RenderHelper.DecimalRenderer(RenderHelper.MILLIS_FORMAT));
        RenderHelper.setColumnRenderer(f_table, PersistenceData.TOTAL_ACTIVE_SPACE_USED_MB,
                                       new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_table, JLabel.CENTER);

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

        // add the notifications model
        f_tmodelNotifications = new PersistenceNotificationsTableModel(
                VisualVMModel.DataType.PERSISTENCE_NOTIFICATIONS.getMetadata());
        f_tableNotifications = new ExportableJTable(f_tmodelNotifications);

        f_tableNotifications.setPreferredScrollableViewportSize(new Dimension(500, 150));
        RenderHelper.setColumnRenderer(f_tableNotifications, PersistenceNotificationsData.SEQUENCE,
                    new RenderHelper.IntegerRenderer());
        RenderHelper.setColumnRenderer(f_tableNotifications, PersistenceNotificationsData.SERVICE,
                    new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(f_tableNotifications, PersistenceNotificationsData.MESSAGE,
                    new RenderHelper.ToolTipRenderer());
        RenderHelper.setColumnRenderer(f_tableNotifications, PersistenceNotificationsData.DURATION,
                    new RenderHelper.IntegerRenderer());
        RenderHelper.setHeaderAlignment(f_tableNotifications, JLabel.CENTER);

        // Add some space
        f_tableNotifications.setIntercellSpacing(new Dimension(6, 3));
        f_tableNotifications.setRowHeight(f_tableNotifications.getRowHeight() + 4);

        JScrollPane scrollPaneNotifications = new JScrollPane(f_tableNotifications);
        configureScrollPane(scrollPaneNotifications, f_tableNotifications);

        // create a chart for the machine load averages
        f_persistenceLatencyGraph = GraphHelper.createPersistenceLatencyGraph();
        f_persistenceTotalSpaceGraph = GraphHelper.createPersistenceActiveTotalGraph();

        JSplitPane pneSplitPlotter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        pneSplitPlotter.setResizeWeight(0.5);

        pneSplitPlotter.add(f_persistenceTotalSpaceGraph.getChart());
        pneSplitPlotter.add(f_persistenceLatencyGraph.getChart());

        // create two tabs, one for graphs and one for notifications
        JTabbedPane pneTab = new JTabbedPane();
        pneTab.addTab(getLocalizedText("TAB_details"), pneSplitPlotter);
        pneTab.addTab(getLocalizedText("TAB_notifications"), scrollPaneNotifications);
        pneTab.setOpaque(false);

        pneSplit.add(pneTab);
        add(pneSplit);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGUI()
        {
        long         cTotalMemory    = 0;
        final String MEM_FORMAT      = "%,10.2f";
        long         cLatencyMax     = 0L;
        float        cLatencyTotal   = 0.0f;
        float        cLatencyAverage = 0.0f;
        int          c               = 0;

        if (m_persistenceData != null)
            {
            for (Entry<Object, Data> entry : m_persistenceData)
                {
                // only include services with active persistence
                if (entry.getValue().getColumn(PersistenceData.PERSISTENCE_MODE).equals("active"))
                    {
                    long cTotalMem = (Long) entry.getValue().getColumn(PersistenceData.TOTAL_ACTIVE_SPACE_USED);

                    cTotalMemory  += cTotalMem == -1 ? 0 : cTotalMem;
                    cLatencyTotal += (Float) entry.getValue().getColumn(PersistenceData.AVERAGE_LATENCY);

                    cLatencyMax = (Long) entry.getValue().getColumn(PersistenceData.MAX_LATENCY);

                    if (cLatencyMax > m_cMaxMaxLatency)
                        {
                        m_cMaxMaxLatency = cLatencyMax;
                        }

                    c++;
                    }
                }

            f_txtTotalActiveSpaceUsed.setText(String.format(MEM_FORMAT, (cTotalMemory * 1.0f) / GraphHelper.MB));
            cLatencyAverage = cLatencyTotal / c;

            }
        else
            {
            f_txtTotalActiveSpaceUsed.setText(String.format(MEM_FORMAT, 0));
            }

        GraphHelper.addValuesToPersistenceActiveTotalGraph(f_persistenceTotalSpaceGraph, cTotalMemory);
        GraphHelper.addValuesToPersistenceLatencyGraph(f_persistenceLatencyGraph, cLatencyAverage * 1000.0f);

        f_txtMaxLatency.setText(Long.toString(m_cMaxMaxLatency));

        fireTableDataChangedWithSelection(f_table, f_tmodel);
        fireTableDataChangedWithSelection(f_tableNotifications, f_tmodelNotifications);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateData()
        {
        m_persistenceData = f_model.getData(VisualVMModel.DataType.PERSISTENCE);

        if (m_persistenceData != null)
            {
            if (m_fAddNotifications)
                {
                m_fAddNotifications = false;
                f_listenerCbx.addListeners();
                }
            f_tmodel.setDataList(m_persistenceData);
            }

        if (mapNotificationsData != null)
            {
            m_persistenceNotificationData = new ArrayList<Entry<Object, Data>>(mapNotificationsData.entrySet());
            f_tmodelNotifications.setDataList(m_persistenceNotificationData);
            }
        }

    // ----- inner classes --------------------------------------------------

    /**
     * An implementation of a {@link MenuOption} providing default functionality
     * to call a JMX operation for persistence operations.
     *
     * @since 12.2.1
     * @author tam  2014.08.25
     */
    private class PersistenceInvokeMenuOption
            extends AbstractMenuOption
        {
        // ---- constructors ------------------------------------------------

        /**
         *  Construct a new implementation of a {@link MenuOption} providing default functionality.
         *
         * @param model          the {@link VisualVMModel} to get collected data from
         * @param requestSender  the {@link MBeanServerConnection} to perform additional queries
         * @param jtable         the {@link ExportableJTable} that this applies to
         * @param sLabel         the label key for the menu option from Bundle.properties
         * @param sOperation     the JMX operation to call
         * @param fConfirm       if true a confirmation should be displayed
         * @param fPrompt        if true, prompt for a snapshot name otherwise the operation
         *                       will just be called. See below for special processing of
         *                       getSnapshots().
         */
        public PersistenceInvokeMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable,
                                           String sLabel, String sOperation, boolean fConfirm, boolean fPrompt)
            {
            super(model, requestSender, jtable);
            f_sLabel     = sLabel;
            f_sOperation = sOperation;
            f_fConfirm   = fConfirm;
            f_fPrompt    = fPrompt;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return Localization.getLocalText(f_sLabel);
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e)
            {
            int      nRow           = getSelectedRow();
            String   sServiceName   = null;
            String   sQuery         = null;
            String   sSnapshotName  = null;
            String   sResult        = null;
            boolean  fGetArchived  ;
            String[] asSnapshotList = null;

            if (nRow == -1)
                {
                JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    sServiceName = (String) getJTable().getModel().getValueAt(nRow, 0);

                    String[] asParts          = ServiceData.getServiceParts(sServiceName);
                    String   sService         = asParts.length == 2 ? asParts[1] : sServiceName;
                    String   sDomainPartition = asParts.length == 2 ? asParts[0] : null;

                    if (f_fPrompt)
                        {
                        if (CREATE_SNAPSHOT.equals(f_sOperation) || !f_fShowSnapshotList)
                            {
                            sSnapshotName = JOptionPane.showInputDialog(
                                    Localization.getLocalText("LBL_enter_snapshot", new String[] {""}));
                            }
                        else
                            {
                            fGetArchived = f_sOperation.equals(RETRIEVE_ARCHIVED_SNAPSHOT) ||
                                           f_sOperation.equals(REMOVE_ARCHIVED_SNAPSHOT);

                            // get the list of snapshots or archived snapshots
                            if (fGetArchived)
                                {
                                asSnapshotList = m_requestSender.getArchivedSnapshots(sService, sDomainPartition);
                                }
                            else
                                {
                                asSnapshotList = m_requestSender.getSnapshots(sService, sDomainPartition);
                                }

                            // check to see if there is empty list of snapshots or archived snapshots
                            if (asSnapshotList == null || asSnapshotList.length == 0)
                                {
                                JOptionPane.showMessageDialog(null,
                                        Localization.getLocalText("LBL_no_snapshots",
                                                    new String[] {fGetArchived ? "archived " : ""}));
                                return;
                                }

                            // request the user to choose from an existing list
                            JList<String> listSnapshots = new JList(asSnapshotList);
                            listSnapshots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                            sSnapshotName = (String) JOptionPane.showInputDialog(
                                        null, "Snapshpot",
                                        Localization.getLocalText("LBL_enter_snapshot",
                                                new String[] {fGetArchived ? "archived " : ""}),
                                        JOptionPane.QUESTION_MESSAGE,
                                        null, // default icon
                                        asSnapshotList,
                                        asSnapshotList[0]);
                            }
                        }

                    if ((sSnapshotName != null && !"".equals(sSnapshotName)) || !f_fPrompt)
                        {
                        if (f_fConfirm)
                            {
                            String sQuestion = FORCE_RECOVERY.equals(f_sOperation) ?
                                    Localization.getLocalText("LBL_confirm_recovery") :
                                    Localization.getLocalText("LBL_confirm_snapshot",
                                    new String[] {f_sOperation, sSnapshotName});

                            if (JOptionPane.showConfirmDialog(null, sQuestion,
                                                              Localization.getLocalText("LBL_confirm_operation"),
                                                              JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                                {
                                return;
                                }
                            }

                        if (f_fPrompt)
                            {
                            m_requestSender.executePersistenceOperation(sService, sDomainPartition,
                                    f_sOperation, sSnapshotName);
                            }
                        else
                            {
                            if (GET_SNAPSHOTS.equals(f_sOperation))
                                {
                                String[] asSnapshots = (String[]) m_requestSender.getSnapshots(sService, sDomainPartition);
                                sResult = getSnapshotList(Localization.getLocalText("LBL_snapshots", new String[] {sServiceName}),
                                                          asSnapshots);
                                }
                            else if (GET_OPERATION_STATUS.equals(f_sOperation))
                                {
                                String sStatus = m_requestSender.getAttribute(new ObjectName(getFullyQualifiedName(
                                        m_requestSender, PersistenceData.getMBeanName(sServiceName)))
                                        , "OperationStatus");
                                sResult = Localization.getLocalText("LBL_current_status_result", new String[] { sServiceName, sStatus});
                                }
                            else
                                {
                                if (FORCE_RECOVERY.equals(f_sOperation))
                                    {
                                    sResult = Localization.getLocalText("LBL_recovery_forced");
                                    }
                                else
                                    {
                                    String[] asArchivedSnapshots = m_requestSender.getArchivedSnapshots(sService, sDomainPartition);
                                    sResult = getSnapshotList(Localization.getLocalText("LBL_archived_snapshots", new String[] {sServiceName}),
                                                          asArchivedSnapshots);
                                    }
                                }
                            }

                        if (sResult == null)
                            {
                            sResult = Localization.getLocalText(f_sOperation.equals(REMOVE_ARCHIVED_SNAPSHOT) ?
                                            "LBL_operation_ok" : "LBL_operation_submitted",
                                         new String[] {f_sOperation, sSnapshotName, sServiceName});
                            }
                        if (RECOVER_SNAPSHOT.equals(f_sOperation))
                            {
                            sResult = sResult + "\n\n" + Localization.getLocalText("LBL_recover_note",
                                    new String[] {sServiceName});
                            }

                        showMessageDialog(Localization.getLocalText("LBL_result"), sResult,
                                          JOptionPane.INFORMATION_MESSAGE);
                        }

                    }
                catch (Exception ee)
                    {
                    showMessageDialog(Localization.getLocalText("ERR_error_invoking",
                        new String[] {f_sOperation + " " + sQuery}), ee.getMessage() +
                            "\n" + ee.getCause(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        // ----- helpers ----------------------------------------------------

        /**
         * Return a formatted list of snapshots.
         *
         * @param sTitle  the title to display on first line
         * @param asList  the {@link String}[] of snapshots
         *
         * @return the formatted list
         */
        private String getSnapshotList(String sTitle, String[] asList)
            {
            StringBuilder sb = new StringBuilder(sTitle + "\n");
            if (asList != null)
                {
                for (int i = 0; i < asList.length; i++)
                    {
                    sb.append((i + 1) + ": " + asList[i] + "\n");
                    }
                }
            return sb.toString();
            }

        // ----- data members -----------------------------------------------

        /**
         * The label key for the menu option.
         */
        private final String f_sLabel;

        /**
         * Indicates if a confirmation should be issued.
         */
        private final boolean f_fConfirm;

        /**
         * The JMX Operation to call.
         */
        private final String f_sOperation;

        /**
         * Indicates if a snapshot name should be asked for.
         */
        private final boolean f_fPrompt;
        }

    /**
     * A class to react to button press to clear notifications.
     */
    private class ClearNotificationsListener implements ActionListener
        {
        // ----- ActionListener methods -------------------------------------

        @Override
        public void actionPerformed(ActionEvent event)
            {
            if (JOptionPane.showConfirmDialog(null, getLocalizedText("LBL_clear_confirmation"),
                    getLocalizedText("LBL_confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                mapNotificationsData.clear();
                f_mapNotifications.clear();
                nSequence = new AtomicLong(0L);
                fireTableDataChangedWithSelection(f_tableNotifications, f_tmodelNotifications);
                }
            }
        }

    /**
     * A class to react to the checkbox being changed.
     */
    private class CheckBoxListener implements ItemListener
        {
        // ----- ItemListener methods ---------------------------------------

        @Override
        public void itemStateChanged(ItemEvent event)
            {
            boolean fSelected = event.getStateChange() == ItemEvent.SELECTED;

            if (fSelected)
                {
                // go through each of the services and register a JMX NotificationListener
                if (m_persistenceData != null)
                    {
                    addListeners();
                    }
                }
            else
               {
                // de-selected so remove all JMX NotificationListeners
                f_mapJMXListeners.forEach((k, v) ->
                    {
                    try
                        {
                        if (m_requestSender instanceof JMXRequestSender)
                            {
                            ((JMXRequestSender) m_requestSender).removeNotificationListener(v.getX(), v.getY());
                            }
                        }
                    catch (Exception e)
                        {
                        throw new RuntimeException("Unable to remove notification listener on " + v.getX());
                        }
                    });
                f_mapJMXListeners.clear();
                }
            }

        /**
         * Add JMX notification listeners.
         */
        public void addListeners()
            {
            if (m_persistenceData != null) {
                for (Entry<Object, Data> entry : m_persistenceData)
                    {
                    String sServiceName = (String) entry.getKey();

                    try
                        {
                        if (m_requestSender instanceof JMXRequestSender)
                            {
                            String sQuery = getFullyQualifiedName(m_requestSender, PersistenceData.getMBeanName(sServiceName));

                            ObjectName           oBeanName = new ObjectName(sQuery);
                            NotificationListener listener  = new PersistenceNotificationListener(sServiceName);
                            ((JMXRequestSender) m_requestSender).addNotificationListener(oBeanName, listener, null, null);

                            f_mapJMXListeners.put(sServiceName, new Pair<ObjectName, NotificationListener>(oBeanName, listener));

                            }
                        }
                    catch (Exception e)
                        {
                        throw new RuntimeException("Unable to get FQN for " + sServiceName +
                                                   " " + e.getMessage());
                        }
                    }
                }
            }
        }

    /**
     * A class to react to JMX notifications.
     */
    private class PersistenceNotificationListener
                implements NotificationListener
        {

        // ----- constructors -----------------------------------------------

        /**
         * Construct a new NotificationListener for the given service.
         *
         * @param sServiceName  service name to use
         */
        public PersistenceNotificationListener(String sServiceName)
            {
            f_sServiceName = sServiceName;
            }

        // ----- NotificationListener interface -----------------------------

        @Override
        public void handleNotification(Notification notification, Object oHandback)
            {
            String sUserData = notification.getUserData().toString();

            // determine if its a begin or end notification
            String sType = notification.getType();
            String sKey  = f_sServiceName + "-" + sType;

            // ignore the recover.begin and recover.end notifications as this can be
            // confusing for the user
            if (sType.indexOf(BEGIN) > 0 && !sType.equals("recover.begin"))
                {
                // save the begin time
                f_mapNotifications.put(sKey, notification.getTimeStamp());
                }
            else if (sType.indexOf(END) > 0)
                {
                // try and find the begin notification
                sKey          = f_sServiceName + "-" + sType.replaceAll(END, BEGIN);
                Long ldtStart = f_mapNotifications.get(sKey);

                if (ldtStart != null)
                    {
                    String sBaseType = sType.replaceAll(END,"");
                    String sMessage = notification.getMessage() + (sUserData == null || sUserData.isEmpty() ? "" : sUserData);
                    f_mapNotifications.remove(sKey);

                    // Add the data to the notifications data map
                    Data data = new PersistenceNotificationsData();
                    data.setColumn(PersistenceNotificationsData.SEQUENCE, nSequence.incrementAndGet());
                    data.setColumn(PersistenceNotificationsData.SERVICE, f_sServiceName);
                    data.setColumn(PersistenceNotificationsData.OPERATION, sBaseType);
                    data.setColumn(PersistenceNotificationsData.START_TIME, (new Date(ldtStart).toString()));
                    data.setColumn(PersistenceNotificationsData.END_TIME, new Date(notification.getTimeStamp()).toString());
                    data.setColumn(PersistenceNotificationsData.DURATION, notification.getTimeStamp() - ldtStart);
                    data.setColumn(PersistenceNotificationsData.MESSAGE, sMessage);

                    mapNotificationsData.put(data.getColumn(PersistenceNotificationsData.SEQUENCE), data);
                    }
                }
            else
                {
                // ignore the notification as we could have received the end
                // without having received the begin
                }
            }

        // ----- constants --------------------------------------------------

        /**
         * Begin suffix for notifications.
         */
        private static final String BEGIN = ".begin";

        /**
         * End suffix for notifications.
         */
        private static final String END = ".end";

        // ----- data members -----------------------------------------------

        /**
         * Service name on which notification is added.
         */
        private final String f_sServiceName;
    }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 230801760279336008L;

    /**
     * JMX operation for retrieveArchivedSnapshot.
     */
    public static final String RETRIEVE_ARCHIVED_SNAPSHOT = "retrieveArchivedSnapshot";

    /**
     * JMX operation for removeArchivedSnapshot.
     */
    public static final String REMOVE_ARCHIVED_SNAPSHOT = "removeArchivedSnapshot";

    /**
     * JMX operation for listArchivedSnapshots.
     */
    public static final String LIST_ARCHIVED_SNAPSHOTS = "listArchivedSnapshots";

    /**
     * JMX operation for createSnapshot.
     */
    public static final String RECOVER_SNAPSHOT = "recoverSnapshot";

    /**
     * JMX operation for removeSnapshot.
     */
    public static final String REMOVE_SNAPSHOT = "removeSnapshot";

    /**
     * JMX operation for createSnapshot.
     */
    public static final String CREATE_SNAPSHOT = "createSnapshot";

    /**
     * JMX attribute for getSnapshots.
     */
    public static final String GET_SNAPSHOTS = "getSnapshots";

    /**
     * JMX attribute for getOperationStatus.
     */
    public static final String GET_OPERATION_STATUS = "getOperationStatus";

    /**
     * JMX operation for archiveSnapshot.
     */
    public static final String ARCHIVE_SNAPSHOT = "archiveSnapshot";

    /**
     * JMX operation for forceRecovery.
     */
    public static final String FORCE_RECOVERY = "forceRecovery";

    // ----- data members ---------------------------------------------------

    /**
     * The total amount of active space used.
     */
    private final JTextField f_txtTotalActiveSpaceUsed;

    /**
     * The current Max latency.
     */
    private final JTextField f_txtMaxLatency;

    /**
     * Maximum of the maximum latencies.
     */
    private long m_cMaxMaxLatency = 0L;

    /**
     * The persistence data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_persistenceData;

    /**
     * The persistence notification data retrieved from the {@link VisualVMModel}.
     */
    private List<Map.Entry<Object, Data>> m_persistenceNotificationData;

    /**
     * The {@link PersistenceTableModel} to display persistence data.
     */
    protected final PersistenceTableModel f_tmodel;

    /**
     * The {@link PersistenceTableModel} to display persistence notifications.
     */
    protected final PersistenceNotificationsTableModel f_tmodelNotifications;

    /**
     * The graph of persistence latency averages.
     */
    private final SimpleXYChartSupport f_persistenceLatencyGraph;

    /**
     * The graph of persistence latency averages.
     */
    private final SimpleXYChartSupport f_persistenceTotalSpaceGraph;

    /**
     * the {@link ExportableJTable} to use to display data.
     */
    protected final ExportableJTable f_table;

    /**
     * the {@link ExportableJTable} to use to notifications data.
     */
    protected final ExportableJTable f_tableNotifications;

    /**
     * A check-box to indicate if notification data should be included.
     */
    private JCheckBox m_cbxCollectNotifications = null;

    /**
     * A button to clear the notifications.
     */
    private JButton m_btnClearNotifications = null;

    /**
     * Listener for checkbox.
     */
    private final CheckBoxListener f_listenerCbx;

    /**
     * A Map of JMX Notification listeners which have been registered.
     */
    private final Map<String, Pair<ObjectName, NotificationListener>> f_mapJMXListeners = new HashMap<>();

    /**
     * A map of notifications keyed by service name and notification type.begin.
     */
    private final Map<String, Long> f_mapNotifications = new ConcurrentHashMap<>();

    /**
     * A Map containing notifications data.
     */
    private SortedMap<Object, Data> mapNotificationsData = new TreeMap<>(
            (o1, o2)->((Long) o2).compareTo((Long) o1));

    /**
     * Sequence for notifications.
     */
    private AtomicLong nSequence = new AtomicLong(0L);

    /**
     * Indicates if the list of snapshots should be shown instead of the user entering
     * the snapshot name. By default this is true, but can be changed by setting the following:
     * <pre>-J-Dcoherence.plugin.visualvm.persistence.list=false</pre>
     */
    private final boolean f_fShowSnapshotList = GlobalPreferences.sharedInstance().isPersistenceListEnabled();

    /**
     * Indicated to add notifications on startup.
     */
    private boolean m_fAddNotifications = true;
    }