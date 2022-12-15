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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.DialogHelper;
import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicSubscriberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicSubscriberGroupsData;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
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
    protected AbstractCoherencePanel(LayoutManager manager, VisualVMModel model)
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
        return getTextField(width, SwingConstants.RIGHT);
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
     * Set table padding.
     * @param table {@link ExportableJTable} to set padding for
     */
    protected void setTablePadding(ExportableJTable table)
        {
        table.setIntercellSpacing(new Dimension(6, 3));
        table.setRowHeight(table.getRowHeight() + 4);
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
        Set<ObjectName> setResults = requestSender.getCompleteObjectName(new ObjectName(sQuery));

        if (setResults.size() != 1)
            {
            return null;
            }

        Object oResult = setResults.iterator().next();
        return oResult == null ? null : oResult.toString();
        }

    /**
     * Returns true if the node is storage-enabled.
     *
     * @param nodeId the node id to check
     * @return true if the node is storage-enabled
     */
    protected boolean isNodeStorageEnabled(int nodeId)
        {
        List<Map.Entry<Object, Data>> nodeStorageData = f_model.getData(VisualVMModel.DataType.NODE_STORAGE);

        if (nodeStorageData != null)
            {
            for (Map.Entry<Object, Data> entry : nodeStorageData)
                {
                if ((Integer) entry.getValue().getColumn(NodeStorageData.NODE_ID) == nodeId)
                    {
                    return (Boolean) entry.getValue().getColumn(NodeStorageData.STORAGE_ENABLED);
                    }
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
            m_table = new ExportableJTable(m_tmodel, f_model);
            RenderHelper.setHeaderAlignment(m_table, SwingConstants.CENTER);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            m_table.setPreferredScrollableViewportSize(new Dimension((Math.max((int) (screenSize.getWidth() * 0.5),
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
            String sExtra = "";
            boolean fAllAttributes = true;

            if (nRow == -1)
                {
                DialogHelper.showInfoDialog( Localization.getLocalText("LBL_must_select_row"));
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
                                  : "") + NODE_ID + oValue + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_CACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=Cache,service=" + getServiceName(selectedCache.getX()) + NAME + selectedCache.getY() +
                                 ",tier=back,nodeId=" + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_STORAGE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=StorageManager,service=" + getServiceName(selectedCache.getX()) + ",cache=" + selectedCache.getY() +
                                 NODE_ID + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_JCACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedJCache();
                        sQuery = "javax.cache:type=CacheStatistics,CacheManager=" + selectedCache.getX() + ",Cache=" + selectedCache.getY() + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_FRONT_CACHE)
                        {
                        Pair<String, String> selectedCache = AbstractCoherencePanel.this.f_model.getSelectedCache();
                        sQuery = "Coherence:type=Cache,service=" + getServiceName(selectedCache.getX()) + NAME + selectedCache.getY() +
                                 ",tier=front,nodeId=" + oValue + getDomainPartitionKey(selectedCache.getX()) + ",*";
                        }
                    else if (f_nSelectedItem == SELECTED_SUBSCRIBER || f_nSelectedItem == SELECTED_SUBSCRIBER_CHANNELS)
                        {
                        oValue = getJTable().getModel().getValueAt(nRow, TopicSubscriberData.NODE_ID);
                        Object oSubscriberId = getJTable().getModel().getValueAt(nRow, TopicSubscriberData.SUBSCRIBER);
                        Pair<String, String> selectedTopic = AbstractCoherencePanel.this.f_model.getSelectedTopic();
                        sQuery = "Coherence:type=PagedTopicSubscriber,service=" + getServiceName(selectedTopic.getX()) + ",topic=" + selectedTopic.getY() +
                                 NODE_ID + oValue + ",id=" + oSubscriberId + ",*";
                        fAllAttributes = f_nSelectedItem != SELECTED_SUBSCRIBER_CHANNELS;
                        }
                    else if (f_nSelectedItem == SELECTED_SUBSCRIBER_GROUP || f_nSelectedItem == SELECTED_SUB_GRP_CHANNELS)
                        {
                        oValue = getJTable().getModel().getValueAt(nRow, TopicSubscriberGroupsData.NODE_ID);
                        Object oSubscriberGroup = getJTable().getModel().getValueAt(nRow, TopicSubscriberGroupsData.SUBSCRIBER_GROUP);
                        Pair<String, String> selectedTopic = AbstractCoherencePanel.this.f_model.getSelectedTopic();
                        sQuery = "Coherence:type=PagedTopicSubscriberGroup,service=" + getServiceName(selectedTopic.getX()) + ",topic=" + selectedTopic.getY() +
                                 NODE_ID + oValue + NAME + oSubscriberGroup + ",*";
                        fAllAttributes = f_nSelectedItem != SELECTED_SUB_GRP_CHANNELS;
                        }
                    else if (f_nSelectedItem == SELECTED_TOPIC_DETAIL || f_nSelectedItem == SELECTED_TOPIC_CHANNELS)
                        {
                        oValue = getJTable().getModel().getValueAt(nRow, TopicDetailData.NODE_ID);
                        Pair<String, String> selectedTopic = AbstractCoherencePanel.this.f_model.getSelectedTopic();
                        sQuery = "Coherence:type=PagedTopic,service=" + getServiceName(selectedTopic.getX()) + NAME + selectedTopic.getY() +
                                 NODE_ID + oValue + ",*";
                        fAllAttributes = f_nSelectedItem != SELECTED_TOPIC_CHANNELS;
                        }

                    // remove any existing rows
                    m_tmodel.getDataVector().removeAllElements();
                    m_tmodel.fireTableDataChanged();
                    if (fAllAttributes)
                        {
                        populateAllAttributes(sQuery);
                        }
                    else {
                        populateChannelAttributes(sQuery);
                        }

                    m_pneMessage.getVerticalScrollBar().setValue(0);
                    m_tmodel.fireTableDataChanged();

                    String sTitle = fAllAttributes ? Localization.getLocalText("LBL_details") :
                                    Localization.getLocalText("LBL_channel_details") + sExtra;


                    JOptionPane.showMessageDialog(null, m_pneMessage, sTitle, JOptionPane.INFORMATION_MESSAGE);
                    }
                catch (Exception e)
                    {
                    DialogHelper.showWarningDialog(e.getMessage());
                    LOGGER.log(Level.WARNING, Localization.getLocalText("LBL_error"), e);
                    }
                }
            }

        // ----- helpers --------------------------------------------------------

        /**
         * Populate all the attributes for the given query.
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
                ObjectName objName = iter.next();
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

        /**
         * Populate only the channel attributes for the given query.
         *
         * @param sQuery the query to run
         * @throws Exception if any relevant error
         */
        protected void populateChannelAttributes(String sQuery)
                throws Exception
            {
            final AtomicInteger row = new AtomicInteger(0);

            Set<ObjectName> setObjects = m_requestSender.getCompleteObjectName(new ObjectName(sQuery));

            for (Iterator<ObjectName> iter = setObjects.iterator(); iter.hasNext(); )
                {
                ObjectName objName = iter.next();
                m_tmodel.insertRow(row.getAndIncrement(), new Object[] {"JMX Key", objName.toString()});

                List<Attribute> lstAttr = m_requestSender.getAllAttributes(objName);

                for (Attribute attr : lstAttr)
                    {
                    if (attr.getName().equalsIgnoreCase("Channels"))
                        {
                        Object oValue = attr.getValue();
                        if (oValue instanceof TabularDataSupport)
                            {
                            TabularDataSupport data = (TabularDataSupport) oValue;
                            data.values().forEach(o ->
                                {
                                if (o instanceof CompositeDataSupport)
                                    {
                                    CompositeDataSupport cds = (CompositeDataSupport) o;
                                    cds.getCompositeType().keySet().forEach(k ->
                                         m_tmodel.insertRow(row.getAndIncrement(), new Object[] {k, cds.get(k)}));
                                    }
                                });
                            }
                        else if (m_requestSender instanceof HttpRequestSender)
                            {
                            // decode the channel JSON
                            Map<String, Object> mapResults = processJSON(oValue.toString());
                            mapResults.forEach((k,v) -> m_tmodel.insertRow(row.getAndIncrement(), new Object[] {k, v}));
                            }
                        }
                    }
                }
            }

        /**
         * proces channel JSON and return in a sorted map.
         * @param sJson JSON to process
         * @return results
         */
        @SuppressWarnings({"unchecked", "raw"})
        private Map<String, Object> processJSON(String sJson)
            {
            final Map<String, Object> mapResults = new TreeMap<>();
            ObjectMapper              mapper     = new ObjectMapper();
            try
                {
                Object jsonData = mapper.readValue(sJson, Object.class);
                if (jsonData instanceof Map)
                    {
                    Map mapData = (Map) jsonData;
                    int nMaxChannels = mapData.size() - 1;
                    for (int i = 0; i <= nMaxChannels; i++)
                        {
                        // get the entries in order
                        Map<String, Object> values = (Map<String, Object>) mapData.get("[" + i + "]");
                        int nChannel = (int) values.get("Channel");
                        values.forEach((k1, v1) -> mapResults.put(String.format("%02d:%s", nChannel, k1), v1));
                        }
                    }
                }
            catch (Exception eIgnore)
                {
                // ignore
                }
            return mapResults;
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
                Pair<?, ?> key = (Pair<?,?>) entryOrig.getKey();
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
     * Return the domainPartition key or empty string if the service doesn't
     * contain one.
     *
     * @param sSelectedServiceName service name to interrogate
     * @return the domain partition key or empty string
     */
    protected static String getDomainPartitionKey(String sSelectedServiceName)
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
    protected static String getServiceName(String sSelectedServiceName)
        {
        String[] asParts = AbstractData.getDomainAndService(sSelectedServiceName);
        return asParts[1];
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
        // [0] = cTotalActiveSpace (long)
        // [1] = cTotalBackupSpace (long)
        // [2] = cLatencyMax  (long)
        // [3] = cLatencyTotal(float)
        // [4] = count

        long cTotalActiveSpace = 0;
        long cTotalBackupSpace = 0;
        long cLatencyMax = 0L;
        float cLatencyTotal = 0.0f;
        int count = 0;
        long cMaxLatencyMax = 0;
        for (Map.Entry<Object, Data> entry : persistenceData)
            {
            // only include services with active persistence
            String sPersistenceMode = (String) entry.getValue().getColumn(PersistenceData.PERSISTENCE_MODE);

            if (PersistenceData.isActivePersistence(sPersistenceMode))
                {
                long cTotalActive = (Long) entry.getValue().getColumn(PersistenceData.TOTAL_ACTIVE_SPACE_USED);
                long cTotalBackup = (Long) entry.getValue().getColumn(PersistenceData.TOTAL_BACKUP_SPACE_USED_MB);

                cTotalActiveSpace += cTotalActive == -1 ? 0 : cTotalActive;
                cTotalBackupSpace += cTotalBackup == -1 ? 0 : cTotalBackup * GraphHelper.MB;

                cLatencyTotal += (Float) entry.getValue().getColumn(PersistenceData.AVERAGE_LATENCY);

                cLatencyMax = (Long) entry.getValue().getColumn(PersistenceData.MAX_LATENCY);

                if (cLatencyMax > cMaxLatencyMax)
                    {
                    cMaxLatencyMax = cLatencyMax;
                    }
                count++;
                }
            }

        return new Object[] {cTotalActiveSpace, cTotalBackupSpace, cMaxLatencyMax, cLatencyTotal, count};
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
        return (oValue == null ? 0 : (Integer) oValue);
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
     * @param oValue value
     * @return a formatted value
     */
    protected String getMemoryFormat(Object oValue)
        {
        return oValue instanceof String || oValue instanceof Number ? String.format(MEM_FORMAT, Long.parseLong(oValue.toString())) : "";
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
        return String.format("%1.3f", Float.parseFloat(sValue));
        }

    /**
     * Format a latency value.
     *
     * @param oValue latency value
     * @return a formatted value
     */
    protected String getLatencyValue(Object oValue)
        {
        return oValue instanceof Float || oValue instanceof String || oValue instanceof Number
               ? String.format("%7.3f", Float.parseFloat(oValue.toString())) : "";
        }


    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7607701492285533521L;

    /**
     * Filler for spacing.
     */
    protected static final String FILLER = "   ";

    /**
     * Indicates to show selected node.
     */
    public static final int SELECTED_NODE = 0;

    /**
     * Indicates to show selected service.
     */
    public static final int SELECTED_SERVICE = 1;

    /**
     * Indicates to show selected cache.
     */
    public static final int SELECTED_CACHE = 2;

    /**
     * Indicates to show selected storage.
     */
    public static final int SELECTED_STORAGE = 3;

    /**
     * Indicates to show select JCache.
     */
    public static final int SELECTED_JCACHE = 4;

    /**
     * Indicates to show selected front cache.
     */
    public static final int SELECTED_FRONT_CACHE = 5;

    /**
     * Indicates to show selected subscriber.
     */
    public static final int SELECTED_SUBSCRIBER = 6;

    /**
     * Indicates to show selected subscriber group.
     */
    public static final int SELECTED_SUBSCRIBER_GROUP = 7;

    /**
     * Indicates to show selected topic detail.
     */
    public static final int SELECTED_TOPIC_DETAIL = 8;

    /**
     * Indicates to show selected topic channels.
     */
    public static final int SELECTED_TOPIC_CHANNELS = 9;

    /**
     * Indicates to show selected subscriber channels.
     */
    public static final int SELECTED_SUBSCRIBER_CHANNELS = 10;

    /**
     * Indicates to show selected subscriber grp channels.
     */
    public static final int SELECTED_SUB_GRP_CHANNELS = 11;

    /**
     * Text value of statusHA.
     */
    protected static final String[] STATUSHA_VALUES = new String[] {"ENDANGERED", "NODE-SAFE", "MACHINE-SAFE",
                                                                    "RACK-SAFE", "SITE-SAFE"};

    protected static final String MEM_FORMAT = "%,d";
    protected static final String NODE_ID    = ",nodeId=";
    protected static final String NAME       = ",name=";

    // ----- data members ---------------------------------------------------

    /**
     * The request sender to use.
     */
    protected RequestSender m_requestSender;

    /**
     * The visualVM model.
     */
    protected final VisualVMModel f_model;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractCoherencePanel.class.getName());

    }
