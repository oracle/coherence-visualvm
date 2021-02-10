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
package com.oracle.coherence.plugin.visualvm;

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.datasource.CoherenceClusterDataSource;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.JMXRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceTopicPanel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceHttpProxyPanel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceCachePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceClusterOverviewPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceElasticDataPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceHttpSessionPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceJCachePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceMachinePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceMemberPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherencePersistencePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceProxyPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceServicePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceFederationPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceHotCachePanel;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.core.options.GlobalPreferences;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.JmxModelFactory;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * The implementation of the {@link DataSourceView} for displaying the
 * Coherence Cluster Snapshot tab.
 *
 * @author tam  2013.11.14
 */
public class VisualVMView
        extends DataSourceView
    {

    // ----- constructors ----------------------------------------------------

    /**
     * Creates the new instance of the tab.
     *
     * @param application  {@link Application} instance
     */
    public VisualVMView(Application application)
        {
        super(application, "Oracle Coherence", new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60,
              false);
        if (application == null)
            {
            throw new RuntimeException("Application is null");
            }
        m_application = application;

        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);

        requestSender = new JMXRequestSender(jmx.getMBeanServerConnection());
        }

    /**
     * Creates the new instance of the tab.
     *
     * @param dataSource the Coherence management data source
     */
    public VisualVMView(CoherenceClusterDataSource dataSource)
        {
        super(dataSource, "Oracle Coherence", new ImageIcon(Utilities.loadImage(IMAGE_PATH, true)).getImage(), 60,
                false);
        String   sUrl =  dataSource.getUrl();
        requestSender = new HttpRequestSender(sUrl);

        // BUG 29213475 - Check for a valid HttpRequestSender URL before we start the refresh
        String sMessage = Localization.getLocalText("ERR_Invalid_URL", new String[] { sUrl });
        try
            {
            JsonNode rootClusterMembers = ((HttpRequestSender) requestSender).getListOfClusterMembers();
            if (rootClusterMembers == null)
                {
                LOGGER.warning(sMessage);
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(sMessage));
                }
            }
        catch (Exception e)
            {
            LOGGER.warning(sMessage);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
            throw new RuntimeException(sMessage, e);
            }
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Create the new {@link DataViewComponent} which will display all the
     * Coherence related information.
     */
    protected DataViewComponent createComponent()
        {
        final VisualVMModel model = VisualVMModel.getInstance();

        // Data area for master view
        JEditorPane       generalDataArea  = new JEditorPane();
        generalDataArea.setEditable(false);

        // do an initial refresh of the data so we can see if we need to display
        // the proxy server tab
        model.refreshStatistics(requestSender);
        model.setIsFirstRefresh(false);

        // we then construct the panels after the initial refresh so we can utilize
        // any information we have gathered in the startup

        final CoherenceClusterOverviewPanel pnlClusterOverview = new CoherenceClusterOverviewPanel(model);
        final CoherenceMachinePanel         pnlMachine         = new CoherenceMachinePanel(model);
        final CoherenceMemberPanel          pnlMember          = new CoherenceMemberPanel(model);
        final CoherenceServicePanel         pnlService         = new CoherenceServicePanel(model);
        final CoherenceCachePanel           pnlCache           = new CoherenceCachePanel(model);
        final CoherenceTopicPanel           pnlTopic           = new CoherenceTopicPanel(model);
        final CoherenceProxyPanel           pnlProxy           = new CoherenceProxyPanel(model);
        final CoherenceHotCachePanel        pnlHotCache        = new CoherenceHotCachePanel(model);
        final CoherencePersistencePanel     pnlPersistence     = new CoherencePersistencePanel(model);
        final CoherenceHttpSessionPanel     pnlHttpSession     = new CoherenceHttpSessionPanel(model);
        final CoherenceFederationPanel      pnlFederation      = new CoherenceFederationPanel(model);
        final CoherenceElasticDataPanel     pnlElasticData     = new CoherenceElasticDataPanel(model);
        final CoherenceJCachePanel          pnlJCache          = new CoherenceJCachePanel(model);
        final CoherenceHttpProxyPanel       pnlHttpProxy       = new CoherenceHttpProxyPanel(model);

        String sClusterVersion = model.getClusterVersion();
        String sClusterName    = null;

        List<Map.Entry<Object, Data>> clusterData = model.getData(VisualVMModel.DataType.CLUSTER);
        for (Map.Entry <Object, Data > entry : clusterData)
            {
            sClusterName = entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString();
            break;
            }

        // Master view:
        DataViewComponent.MasterView masterView =
            new DataViewComponent.MasterView(Localization.getLocalText("LBL_cluster_information",
                                new String[] {sClusterName, sClusterVersion } ), null,
                                generalDataArea);

        // Configuration of master view:
        DataViewComponent.MasterViewConfiguration masterConfiguration =
            new DataViewComponent.MasterViewConfiguration(false);

        // Add the master view and configuration view to the component:
        m_dvc = new DataViewComponent(masterView, masterConfiguration);

        m_dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(Localization.getLocalText(
                "LBL_cluster_overview"), false), DataViewComponent.TOP_RIGHT);

        // Add detail views to the components
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_cluster_overview"),
                null, 10, pnlClusterOverview, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_machines"),
                null, 10, pnlMachine, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_members"),
                null, 10, pnlMember, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_services"),
                null, 10, pnlService, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_caches"),
                null, 10, pnlCache, null), DataViewComponent.TOP_RIGHT);

        if(model.isHotcacheConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_hotcache"),
                    null, 10, pnlHotCache, null), DataViewComponent.TOP_RIGHT);
            }

        // selectively add tabs based upon used functionality
        if (model.isFederationCongfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_federation"),
                    null, 10, pnlFederation, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isCoherenceExtendConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_proxy_servers"),
                    null, 10, pnlProxy, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isHttpProxyConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_http_proxy_servers"),
                    null, 10, pnlHttpProxy, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isTopicsConfigured())
            {
              m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_topics"),
                null, 10, pnlTopic, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isPersistenceConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_persistence"),
                    null, 10, pnlPersistence, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isCoherenceWebConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_Coherence_web"),
                    null, 10, pnlHttpSession, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isElasticDataConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_elastic_data"),
                    null, 10, pnlElasticData, null), DataViewComponent.TOP_RIGHT);
            }

        if (model.isJCacheConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_JCache"),
                    null, 10, pnlJCache, null), DataViewComponent.TOP_RIGHT);
            }

        // update the request sender
        pnlClusterOverview.setRequestSender(requestSender);
        pnlMachine.setRequestSender(requestSender);
        pnlMember.setRequestSender(requestSender);
        pnlService.setRequestSender(requestSender);
        pnlCache.setRequestSender(requestSender);
        pnlProxy.setRequestSender(requestSender);
        pnlHotCache.setRequestSender(requestSender);
        pnlPersistence.setRequestSender(requestSender);
        pnlHttpSession.setRequestSender(requestSender);
        pnlFederation.setRequestSender(requestSender);
        pnlTopic.setRequestSender(requestSender);
        pnlJCache.setRequestSender(requestSender);

        // display a warning if we are connected to a WLS domain and we can
        // see more that 1 domainPartition key. This code relies on us
        // using JMX queries rather than the reporter.
        if (model.getDomainPartitions().size() > 1)
            {
            JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_mt_warning"));
            }

        // create a timer that will refresh the TAB's as required
        m_timer = new Timer(GlobalPreferences.sharedInstance().getMonitoredDataPoll() * 1000, new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (refreshRunning)
                    {
                    return;
                    }

                refreshRunning = true;
                RequestProcessor.getDefault().post(new Runnable()
                    {
                    public void run()
                        {
                        try
                            {
                            // application may be null inside the constructor
                            if (m_application == null || m_application.getState() == Application.STATE_AVAILABLE)
                                {
                                // Schedule the SwingWorker to update the GUI
                                model.refreshStatistics(requestSender);

                                pnlClusterOverview.updateData();
                                pnlClusterOverview.updateGUI();
                                pnlMember.updateData();
                                pnlMember.updateGUI();
                                pnlService.updateData();
                                pnlService.updateGUI();
                                pnlCache.updateData();
                                pnlCache.updateGUI();

                                if(model.isHotcacheConfigured())
                                    {
                                    pnlHotCache.updateData();
                                    pnlHotCache.updateGUI();
                                    }

                                if (model.isFederationCongfigured())
                                    {
                                    pnlFederation.updateData();
                                    pnlFederation.updateGUI();
                                    }

                                if (model.isCoherenceExtendConfigured())
                                    {
                                    pnlProxy.updateData();
                                    pnlProxy.updateGUI();
                                    }

                                pnlMachine.updateData();
                                pnlMachine.updateGUI();

                                if (model.isPersistenceConfigured())
                                    {
                                    pnlPersistence.updateData();
                                    pnlPersistence.updateGUI();
                                    }

                                if (model.isTopicsConfigured()) {
                                    pnlTopic.updateData();
                                    pnlTopic.updateGUI();
                                }

                                if (model.isCoherenceWebConfigured())
                                    {
                                    pnlHttpSession.updateData();
                                    pnlHttpSession.updateGUI();
                                    }

                                if (model.isElasticDataConfigured())
                                    {
                                    pnlElasticData.updateData();
                                    pnlElasticData.updateGUI();
                                    }

                                if (model.isJCacheConfigured())
                                    {
                                    pnlJCache.updateData();
                                    pnlJCache.updateGUI();
                                    }

                                if (model.isHttpProxyConfigured())
                                    {
                                    pnlHttpProxy.updateData();
                                    pnlHttpProxy.updateGUI();
                                    }
                                }
                            }
                        catch (Exception ex)
                            {
                            LOGGER.warning("Error while refreshing tabs. " + ex.toString());
                            ex.printStackTrace();
                            }
                        finally
                            {
                            refreshRunning = false;
                            }
                        }
                    });
                }
            });
        m_timer.setInitialDelay(800);
        m_timer.start();

        return m_dvc;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removed()
        {
        m_timer.stop();
        }

    /**
     * Called on removal.
     *
     * @param app {@link Application} to remove
     */
    public void dataRemoved(Application app)
        {
        m_timer.stop();
        }

    // ----- constants ------------------------------------------------------

    /**
     * The Coherence standard icon to use.
     */
    public static final String IMAGE_PATH = "com/oracle/coherence/plugin/visualvm/coherence_grid_icon.png";

    /**
     * The Coherence icon.
     */
    public static final Image NODE_ICON = Utilities.loadImage(IMAGE_PATH, true);

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(VisualVMView.class.getName());

    // ----- data members ---------------------------------------------------

    /**
     * Component used to display the tabs.
     */
    private DataViewComponent m_dvc;

    /**
     * Timer used to refresh the screen
     */
    private Timer       m_timer;
    private Application m_application;

    /**
     * Indicates if the refresh is running.
     */
    private boolean refreshRunning;

    /**
     * The Request Sender to use.
     */
    private RequestSender requestSender = null;
    }
