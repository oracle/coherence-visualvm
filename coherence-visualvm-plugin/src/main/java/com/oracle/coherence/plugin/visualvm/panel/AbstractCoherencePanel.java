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
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.NodeStorageData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import com.oracle.coherence.plugin.visualvm.panel.util.ExportableJTable;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.AbstractData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 * An abstract implementation of a {@link JPanel} which provides basic support
 * to be displayed as JVisualVM plug-in.
 *
 * @author tam  2013.11.14
 * @since 12.1.3
 */
public abstract class AbstractCoherencePanel
        extends JPanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create an instance of the panel which will be used to display data
     * retrieved from JMX.
     *
     * @param manager the {@link LayoutManager} to use
     * @param model   the {@link VisualVMModel} to interrogate for data
     */
    public AbstractCoherencePanel(LayoutManager manager, VisualVMModel model)
        {
        super(manager);
        f_model = model;
        this.setOpaque(false);
        }

    // ----- AbstractCoherencePanel methods ----------------------------------

    /**
     * Called to update any GUI related artifacts. Called from done().
     */
    public abstract void updateGUI();

    /**
     * Update any data. Called from doInBackground().
     */
    public abstract void updateData();

    // ----- accessors -------------------------------------------------------

    /**
     * Set the {@link MBeanServerConnection} to get JMX data from.
     *
     * @param requestSender the {@link RequestSender} to get JMX data from
     */
    public void setRequestSender(RequestSender requestSender)
        {
        this.m_requestSender = requestSender;
        }

    /**
     * Create a {@link JLabel} with the specified localized text and set the
     * {@link Component} that the label is for to help with accessibility.
     *
     * @param sKey      the key to look up in Bundle.properties
     * @param component the {@link Component} that the label is for or null
     * @return a {@link JLabel} with the specified text
     */
    protected JLabel getLocalizedLabel(String sKey, Component component)
        {
        JLabel label = new JLabel();
        label.setOpaque(false);

        label.setText(Localization.getLocalText(sKey) + ":");

        if (component != null)
            {
            label.setLabelFor(component);
            }

        return label;
        }

    /**
     * Create a {@link JLabel} with the specified localized text.
     *
     * @param sKey the key to look up in Bundle.properties
     * @return a {@link JLabel} with the specified text
     */
    protected JLabel getLocalizedLabel(String sKey)
        {
        return getLocalizedLabel(sKey, (Component) null);
        }

    /**
     * Return localized text given a key.
     *
     * @param sKey the key to look up in Bundle.properties
     * @return localized text given a key
     */
    protected String getLocalizedText(String sKey)
        {
        return Localization.getLocalText(sKey);
        }

    /**
     * Return a label which is just a filler.
     *
     * @return a label which is just a filler
     */
    protected JLabel getFiller()
        {
        JLabel label = new JLabel();

        label.setText(FILLER);

        return label;
        }

    /**
     * Create a {@link JTextField} with the specified width and make it right
     * aligned.
     *
     * @param width the width for the {@link JTextField}
     * @return the newly created text field
     */
    protected JTextField getTextField(int width)
        {
        return getTextField(width, JTextField.RIGHT);
        }

    /**
     * Create a {@link JTextField} with the specified width and specified
     * alignment.
     *
     * @param width the width for the {@link JTextField}
     * @param align either {@link JTextField}.RIGHT or LEFT
     * @return the newly created text field
     */
    protected JTextField getTextField(int width, int align)
        {
        JTextField textField = new JTextField();

        textField.setEditable(false);
        textField.setColumns(width);
        textField.setHorizontalAlignment(align);

        textField.setOpaque(false);

        return textField;
        }

    /**
     * Fire a tableDataChanged but save and re-apply any selection.
     *
     * @param table the {@link ExportableJTable} to save selection for
     * @param model the {@link AbstractTableModel} to refresh
     */
    protected void fireTableDataChangedWithSelection(ExportableJTable table, AbstractTableModel model)
        {
        int nSelectedRow = table.getListener().getSelectedRow();

        model.fireTableDataChanged();
        table.getListener().setSelectedRow(nSelectedRow);
        }

    /**
     * Configure a {@link javax.swing.JScrollPane} with common settings.
     *
     * @param pneScroll the scroll pane to configure
     * @param table     the table to get background from
     */
    protected void configureScrollPane(JScrollPane pneScroll, JTable table)
        {
        pneScroll.getViewport().setBackground(table.getBackground());
        }

    /**
     * Get a full qualified MBean name.
     *
     * @param requestSender the {@link RequestSender} to perform additional
     *                      queries
     * @param sQuery        the query to execute
     * @return the fully qualified MBean name
     * @throws Exception the relevant exception
     */
    protected String getFullyQualifiedName(RequestSender requestSender, String sQuery)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = requestSender.getCompleteObjectName(new ObjectName(sQuery));

        for (Object oResult : setResult)
            {
            return oResult.toString();
            }

        return null;
        }

    /**
     * Returns true if the node is storage-enabled.
     *
     * @param nodeId the node id to check
     * @return true if the node is storage-enabled
     */
    protected boolean isNodeStorageEnabled(int nodeId)
        {
        for (Map.Entry<Object, Data> entry : f_model.getData(VisualVMModel.DataType.NODE_STORAGE))
            {
            if ((Integer) entry.getValue().getColumn(NodeStorageData.NODE_ID) == nodeId)
                {
                return (Boolean) entry.getValue().getColumn(NodeStorageData.STORAGE_ENABLED);
                }
            }

        // no node id found
        return false;
        }

    // ----- inner classes --------------------------------------------------

    /**
     * A menu option to display a detailed list of attributes from the currently
     * selected row.
     */
    protected class ShowDetailMenuOption
            extends AbstractMenuOption
        {
        /**
         * {@inheritDoc}
         */
        public ShowDetailMenuOption(VisualVMModel model, ExportableJTable jtable, int nSelectedItem)
            {
            super(model, m_requestSender, jtable);
            f_nSelectedItem = nSelectedItem;

            // setup the table
            m_tmodel = new DefaultTableModel(new Object[] {Localization.getLocalText("LBL_name"),
                                                           Localization.getLocalText("LBL_value")}, 2)
                {
                @Override
                public boolean isCellEditable(int row, int column)
                    {
                    return false;
                    }
                };
            m_table = new ExportableJTable(m_tmodel);
            RenderHelper.setHeaderAlignment(m_table, JLabel.CENTER);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            m_table.setPreferredScrollableViewportSize(new Dimension((int) (Math.max((int) (screenSize.getWidth() * 0.5),
                                                                                     800)), m_table.getRowHeight() * 20));

            m_table.setIntercellSpacing(new Dimension(6, 3));
            m_table.setRowHeight(m_table.getRowHeight() + 4);

            m_pneMessage = new JScrollPane(m_table);
            configureScrollPane(m_pneMessage, m_table);
            AbstractMenuOption.setResizable(m_pneMessage);
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMenuItem()
            {
            return m_sMenuLabel;
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent actionEvent)
            {
            int nRow = getSelectedRow();
            Object oValue = null;
            String sQuery = null;

            if (nRow == -1)
                {
                JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_must_select_row"));
                }
            else
                {
                try
                    {
                    oValue = getJTable().getModel().getValueAt(nRow, 0);

                    // determine any specific values to substitute
                    if (f_nSelectedItem == SELECTED_NODE)
                        {
                        sQuery = "Coherence:type=Node,nodeId=" + oValue + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_SERVICE)
                        {
                        String sSelectedService = AbstractCoherencePanel.this.f_model.getSelectedService();

                        // extract domainPartition and service name if we have one
                        String[] asServiceDetails = AbstractData.getDomainAndService(sSelectedService);
                        String sDomainPartition = asServiceDetails[0];
                        sSelectedService = asServiceDetails[1];

                        sQuery = "Coherence:type=Service,name=" + sSelectedService +
                                 (sDomainPartition != null
                                  ? ",domainPartition=" + sDomainPartition
                                  : "") + ",nodeId=" + oValue + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_CACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=Cache,service=" + getServiceName(selectedCache.getX()) + ",name=" + selectedCache.getY() +
                                 ",tier=back,nodeId=" + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_STORAGE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=StorageManager,service=" + getServiceName(selectedCache.getX()) + ",cache=" + selectedCache.getY() +
                                 ",nodeId=" + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_JCACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedJCache();
                        sQuery = "javax.cache:type=CacheStatistics,CacheManager=" + selectedCache.getX() + ",Cache=" + selectedCache.getY() + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_FRONT_CACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=Cache,service=" + getServiceName(selectedCache.getX()) + ",name=" + selectedCache.getY() +
                                 ",tier=front,nodeId=" + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }

                    // remove any existing rows
                    m_tmodel.getDataVector().removeAllElements();
                    m_tmodel.fireTableDataChanged();
                    populateAllAttributes(sQuery);
                    m_pneMessage.getVerticalScrollBar().setValue(0);
                    m_tmodel.fireTableDataChanged();

                    JOptionPane.showMessageDialog(null, m_pneMessage, Localization.getLocalText("LBL_details"),
                                                  JOptionPane.INFORMATION_MESSAGE);

                    }
                catch (Exception e)
                    {
                    showMessageDialog(Localization.getLocalText("LBL_error"), e.getMessage(),
                                      JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    }
                }
            }

        // ----- helpers --------------------------------------------------------

        /**
         * Return the domainPartition key or empty string if the service doesn't
         * contain one.
         *
         * @param sSelectedServiceName service name to interrogate
         * @return the domain partition key or empty string
         */
        protected String getDomainPartitionKey(String sSelectedServiceName)
            {
            String[] asServiceDetails = AbstractData.getDomainAndService(sSelectedServiceName);
            String sDomainPartition = asServiceDetails[0];

            return (sDomainPartition != null
                    ? ",domainPartition=" + sDomainPartition
                    : "");
            }

        /**
         * Return the service part of a the selected service.
         *
         * @param sSelectedServiceName service name to interrogate
         * @return the service name to return
         */
        protected String getServiceName(String sSelectedServiceName)
            {
            String[] asParts = AbstractData.getDomainAndService(sSelectedServiceName);
            return asParts[1];
            }

        /**
         * Populate all of the attributes for the given query.
         *
         * @param sQuery the query to run
         * @throws Exception if any relevant error
         */
        protected void populateAllAttributes(String sQuery)
                throws Exception
            {
            int row = 0;

            Set<ObjectName> setObjects = m_requestSender.getCompleteObjectName(new ObjectName(sQuery));

            for (Iterator<ObjectName> iter = setObjects.iterator(); iter.hasNext(); )
                {
                ObjectName objName = (ObjectName) iter.next();
                m_tmodel.insertRow(row++, new Object[] {"JMX Key", objName.toString()});

                List<Attribute> lstAttr = m_requestSender.getAllAttributes(objName);

                for (Attribute attr : lstAttr)
                    {
                    Object oValue = attr.getValue();
                    if (oValue instanceof Object[])
                        {
                        oValue = Arrays.toString((Object[]) oValue);
                        }
                    m_tmodel.insertRow(row++, new Object[] {attr.getName(), oValue});
                    }
                }

            }

        // ----- data members ---------------------------------------------------

        /**
         * The selected item to build query from.
         */
        protected int f_nSelectedItem;

        /**
         * The {@link TableModel} to display detail data.
         */
        private DefaultTableModel m_tmodel;

        /**
         * the {@link ExportableJTable} to use to display detail data.
         */
        private ExportableJTable m_table;

        /**
         * The scroll pane to display the table in.
         */
        final JScrollPane m_pneMessage;
        }

    /**
     * Merge the destination and origin data into a list. For each federation
     * service, only those participants which at least have one origin or one
     * destination will show in this list.
     *
     * @return a merged list of entries which contain names, status and some
     * aggregation stats of service / participant pairs
     */
    protected List<Map.Entry<Object, Data>> getMergedFederationData()
        {
        // get data of destinations and origins
        List<Map.Entry<Object, Data>> fedDstData = f_model.getData(VisualVMModel.DataType.FEDERATION_DESTINATION);
        List<Map.Entry<Object, Data>> fedOriginData = f_model.getData(VisualVMModel.DataType.FEDERATION_ORIGIN);

        if (fedDstData == null)
            {
            return fedOriginData;
            }
        else if (fedOriginData == null)
            {
            return fedDstData;
            }
        else
            {
            // remove duplicate entries
            for (Map.Entry<Object, Data> entryOrig : fedOriginData)
                {
                Pair key = (Pair) entryOrig.getKey();
                boolean fFound = false;

                // merge destination data and origin data into one entry (in destination data list)
                for (Map.Entry<Object, Data> entryDst : fedDstData)
                    {
                    if (entryDst.getKey().equals(key))
                        {
                        Data dstData = entryDst.getValue();
                        dstData.setColumn(FederationData.Column.TOTAL_BYTES_RECEIVED.ordinal(),
                                          entryOrig.getValue().getColumn(FederationData.Column.TOTAL_BYTES_RECEIVED.ordinal()));
                        dstData.setColumn(FederationData.Column.TOTAL_MSGS_RECEIVED.ordinal(),
                                          entryOrig.getValue().getColumn(FederationData.Column.TOTAL_MSGS_RECEIVED.ordinal()));
                        fFound = true;
                        break;
                        }
                    }
                if (!fFound)
                    {
                    // add to dst list
                    fedDstData.add(entryOrig);
                    }
                }
            return fedDstData;
            }
        }

    /**
     * Returns the "Cluster Status HA" which is just the worst satusHA of all
     * services.
     *
     * @param serviceData service data
     * @return the "Cluster Status HA"
     */
    protected int getClusterStatusHA(List<Map.Entry<Object, Data>> serviceData)
        {
        // start at best value of SITE-SAFE and get a "cluster statusHA" by working backwards
        int bestStatusHA = STATUSHA_VALUES.length;

        for (Map.Entry<Object, Data> entry : serviceData)
            {
            if (!"n/a".equals(entry.getValue().getColumn(ServiceData.STATUS_HA)))
                {
                int statusHAIndex = getStatusHAIndex(entry.getValue().getColumn(ServiceData.STATUS_HA).toString());

                if (statusHAIndex < bestStatusHA)
                    {
                    bestStatusHA = statusHAIndex;
                    }
                }
            }
        return bestStatusHA;
        }

    /**
     * Returns the status HA index from 0-4. 0 being ENDANGERED and 4 being
     * SITE-SAFE.
     *
     * @param sStatusHA the textual version of statusHA
     * @return the index that the textual version matches
     */
    protected int getStatusHAIndex(String sStatusHA)
        {
        for (int i = 0; i < STATUSHA_VALUES.length; i++)
            {
            if (STATUSHA_VALUES[i].equals(sStatusHA))
                {
                return i;
                }
            }

        return -1;
        }

    /**
     * Return the storage details.
     *
     * @param memberData member data.
     * @return the storage results.
     */
    protected Object[] getStorageDetails(List<Map.Entry<Object, Data>> memberData)
        {
        // [0] = cTotalMemory
        // [1] = cTotalMemoryUsed
        // [2] = cStorageCount
        // [3] = cTotalMembers
        // [4] = Edition

        Object[] aoResults = new Object[] {0, 0, 0, 0, null};
        int cTotalMemory = 0;
        int cTotalMemoryUsed = 0;
        int cStorageCount = 0;
        String sEdition;

        Map<String, AtomicInteger> mapEditionCount = new TreeMap<>();
        for (Map.Entry<Object, Data> entry : memberData)
            {
            // only include memory if node is storage enabled
            if (isNodeStorageEnabled((Integer) entry.getValue().getColumn(MemberData.NODE_ID)))
                {
                cStorageCount++;
                cTotalMemory += (Integer) entry.getValue().getColumn(MemberData.MAX_MEMORY);
                cTotalMemoryUsed += (Integer) entry.getValue().getColumn(MemberData.USED_MEMORY);
                }

            String sThisEdition = (String) entry.getValue().getColumn(MemberData.PRODUCT_EDITION);
            mapEditionCount.computeIfAbsent(sThisEdition, k -> new AtomicInteger(0)).incrementAndGet();
            }

        // check most common case
        if (mapEditionCount.size() == 1)
            {
            sEdition = mapEditionCount.keySet().iterator().next();
            }
        else
            {
            // we have one or more editions (unlikely) so get the most popular one
            int nMax = -1;
            sEdition = "";
            for (Map.Entry<String, AtomicInteger> entry : mapEditionCount.entrySet())
                {
                int count = entry.getValue().get();
                if (count > nMax)
                    {
                    nMax = count;
                    sEdition = entry.getKey();
                    }
                }
            }

        aoResults[0] = cTotalMemory;
        aoResults[1] = cTotalMemoryUsed;
        aoResults[2] = cStorageCount;
        aoResults[3] = memberData.size();
        aoResults[4] = sEdition;

        return aoResults;
        }

    /**
     * Returns the persistence data.
     * @param persistenceData the persistence data
     * @return the persistence data
     */
    protected Object[] getPersistenceData(List<Map.Entry<Object, Data>> persistenceData)
        {
        Object[] aoResults = new Object[] {};
        // [0] = cTotalMemory (long)
        // [1] = cLatencyMax  (long)
        // [2] = cLatencyTotal(float)
        // [3] = count

        long cTotalMemory = 0;
        long cLatencyMax = 0L;
        float cLatencyTotal = 0.0f;
        int count = 0;
        long cMaxLatencyMax = 0;
        for (Map.Entry<Object, Data> entry : persistenceData)
            {
            // only include services with active persistence
            if (entry.getValue().getColumn(PersistenceData.PERSISTENCE_MODE).equals("active"))
                {
                long cTotalMem = (Long) entry.getValue().getColumn(PersistenceData.TOTAL_ACTIVE_SPACE_USED);

                cTotalMemory += cTotalMem == -1 ? 0 : cTotalMem;
                cLatencyTotal += (Float) entry.getValue().getColumn(PersistenceData.AVERAGE_LATENCY);

                cLatencyMax = (Long) entry.getValue().getColumn(PersistenceData.MAX_LATENCY);

                if (cLatencyMax > cMaxLatencyMax)
                    {
                    cMaxLatencyMax = cLatencyMax;
                    }
                count++;
                }
            }

        return new Object[] {cTotalMemory, cMaxLatencyMax, cLatencyTotal, count};
        }

    /**
     * Returns an integer value of zero if the object is null, otherwise the
     * value as an int is returned. This is used because sometimes null values
     * are returned as columns when the version of Coherence doesn't support
     * it.
     *
     * @param oValue the value check may be null or valid Integer
     * @return a value of zero if null otherwise the value as an int
     */
    protected int getNullEntry(Object oValue)
        {
        return (oValue == null ? Integer.valueOf(0) : (Integer) oValue);
        }

    /**
     * Format a memory value.
     *
     * @param nValue in MB
     * @return a formatted value
     */
    protected String getMemoryFormat(int nValue)
        {
        return String.format(MEM_FORMAT, nValue);
        }

    /**
     * Format a memory value.
     *
     * @param nValue in MB
     * @return a formatted value
     */
    protected String getMemoryFormat(long nValue)
        {
        return String.format(MEM_FORMAT, nValue);
        }

    /**
     * Format a memory value.
     *
     * @param sValue in MB
     * @return a formatted value
     */
    protected String getMemoryFormat(String sValue)
        {
        return String.format(MEM_FORMAT, Long.parseLong(sValue));
        }

    /**
     * Format a memory value.
     *
     * @param sValue in MB
     * @return a formatted value
     */
    protected String getPercentFormat(String sValue)
        {
        return String.format("%5.2f%%", Float.parseFloat(sValue) * 100);
        }

    /**
     * Format a publisher value.
     *
     * @param sValue publisher value
     * @return a formatted value
     */
    protected String getPublisherValue(String sValue)
        {
        return String.format("%1.3f%%", Float.parseFloat(sValue));
        }

    /**
     * Format a latency value.
     *
     * @param sValue latency value
     * @return a formatted value
     */
    protected String getLatencyValue(String sValue)
        {
        return String.format("%7.3f%%", Float.parseFloat(sValue));
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7607701492285533521L;

    /**
     * Filler for spacing.
     */
    protected static final String FILLER = "   ";

    /**
     * Comma number formatter.
     */
    protected final String COMMA_NUMBER_FORMAT = "%,d";

    /**
     * Indicates to select selected service.
     */
    public static final int SELECTED_NODE = 0;

    /**
     * Indicates to select selected service.
     */
    public static final int SELECTED_SERVICE = 1;

    /**
     * Indicates to select selected cache.
     */
    public static final int SELECTED_CACHE = 2;

    /**
     * Indicates to select selected storage.
     */
    public static final int SELECTED_STORAGE = 3;

    /**
     * Indicates to select JCache.
     */
    public static final int SELECTED_JCACHE = 4;

    /**
     * Indicates to select selected front cache.
     */
    public static final int SELECTED_FRONT_CACHE = 5;

    /**
     * Text value of statusHA.
     */
    protected static final String[] STATUSHA_VALUES = new String[] {"ENDANGERED", "NODE-SAFE", "MACHINE-SAFE",
                                                                    "RACK-SAFE", "SITE-SAFE"};

    protected static final String MEM_FORMAT = "%,d";

    // ----- data members ---------------------------------------------------

    /**
     * The request sender to use.
     */
    protected RequestSender m_requestSender;

    /**
     * The visualVM model.
     */
    protected final VisualVMModel f_model;
    }
