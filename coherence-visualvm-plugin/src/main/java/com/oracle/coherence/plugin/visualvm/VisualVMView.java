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
import com.oracle.coherence.plugin.visualvm.panel.AbstractCoherencePanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceClusterSnapshotPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceExecutorPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceGrpcProxyPanel;
import com.oracle.coherence.plugin.visualvm.panel.CoherenceHealthPanel;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.core.datasupport.Stateful;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.JmxModelFactory;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 * The implementation of the {@link DataSourceView} for displaying the Coherence
 * Cluster Snapshot tab.
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
     * @param application {@link Application} instance
     */
    public VisualVMView(Application application)
        {
        super(application, "Oracle Coherence", new ImageIcon(ImageUtilities.loadImage(IMAGE_PATH, true)).getImage(), 60,
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
        super(dataSource, "Oracle Coherence", new ImageIcon(ImageUtilities.loadImage(IMAGE_PATH, true)).getImage(), 60,
              false);
        String sUrl = dataSource.getUrl();
        requestSender = new HttpRequestSender(sUrl);

        // BUG 29213475 - Check for a valid HttpRequestSender URL before we start the refresh
        String sMessage = Localization.getLocalText("ERR_Invalid_URL", sUrl);
        try
            {
            JsonNode rootClusterMembers = ((HttpRequestSender) requestSender).getListOfClusterMembers();
            if (rootClusterMembers == null)
                {
                LOGGER.warning(sMessage);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
                }
            }
        catch (Exception e)
            {
            sMessage = sMessage + "\nError: " + e.getMessage();
            LOGGER.warning(sMessage);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
            }
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Create the new {@link DataViewComponent} which will display all the
     * Coherence related information.
     */
    @Override
    protected DataViewComponent createComponent()
        {
        final VisualVMModel model = VisualVMModel.getInstance();

        boolean fClusterSnapshotEnabled = com.oracle.coherence.plugin.visualvm.GlobalPreferences
                .sharedInstance().isClusterSnapshotEnabled();

        // Data area for master view
        JEditorPane generalDataArea = new JEditorPane();
        generalDataArea.setEditable(false);

        // do an initial refresh of the data so we can see if we need to display
        // the proxy server tab
        model.refreshStatistics(requestSender);
        model.setIsFirstRefresh(false);

        // we then construct the panels after the initial refresh so we can utilize
        // any information we have gathered in the startup

        final CoherenceClusterSnapshotPanel pnlClusterSnapshot =
                fClusterSnapshotEnabled
                ? new CoherenceClusterSnapshotPanel(model)
                : null;
        final CoherenceClusterOverviewPanel pnlClusterOverview = new CoherenceClusterOverviewPanel(model);
        final CoherenceMachinePanel pnlMachine = new CoherenceMachinePanel(model);
        final CoherenceMemberPanel pnlMember = new CoherenceMemberPanel(model);
        final CoherenceServicePanel pnlService = new CoherenceServicePanel(model);
        final CoherenceCachePanel pnlCache = new CoherenceCachePanel(model);
        final CoherenceTopicPanel pnlTopic = new CoherenceTopicPanel(model);
        final CoherenceProxyPanel pnlProxy = new CoherenceProxyPanel(model);
        final CoherenceHotCachePanel pnlHotCache = new CoherenceHotCachePanel(model);
        final CoherencePersistencePanel pnlPersistence = new CoherencePersistencePanel(model);
        final CoherenceHttpSessionPanel pnlHttpSession = new CoherenceHttpSessionPanel(model);
        final CoherenceFederationPanel pnlFederation = new CoherenceFederationPanel(model);
        final CoherenceElasticDataPanel pnlElasticData = new CoherenceElasticDataPanel(model);
        final CoherenceJCachePanel pnlJCache = new CoherenceJCachePanel(model);
        final CoherenceHttpProxyPanel pnlHttpProxy = new CoherenceHttpProxyPanel(model);
        final CoherenceExecutorPanel pnlExecutor = new CoherenceExecutorPanel(model);
        final CoherenceGrpcProxyPanel pnlGrpcProxy = new CoherenceGrpcProxyPanel(model);
        final CoherenceHealthPanel pnlHealth = new CoherenceHealthPanel(model);

        String sClusterVersion = model.getClusterVersion();
        String sClusterName = null;

        List<Map.Entry<Object, Data>> clusterData = model.getData(VisualVMModel.DataType.CLUSTER);
        for (Map.Entry<Object, Data> entry : clusterData)
            {
            sClusterName = entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString();
            break;
            }

        // Master view:
        DataViewComponent.MasterView masterView =
                new DataViewComponent.MasterView(
                        Localization.getLocalText("LBL_cluster_information",
                                                  sClusterName, sClusterVersion), null, generalDataArea);

        // Configuration of master view:
        DataViewComponent.MasterViewConfiguration masterConfiguration =
                new DataViewComponent.MasterViewConfiguration(false);

        // Add the master view and configuration view to the component:
        m_dvc = new DataViewComponent(masterView, masterConfiguration);

        m_dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(Localization.getLocalText(
                "LBL_cluster_overview"), false), DataViewComponent.TOP_RIGHT);

        // Add detail views to the components
        if (pnlClusterSnapshot != null)
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_cluster_snapshot"),
                                                                   null, 10, pnlClusterSnapshot, null), DataViewComponent.TOP_RIGHT);
            }
        DataViewComponent.DetailsView clusterOverview = new DataViewComponent.DetailsView(Localization.getLocalText("LBL_cluster_overview"),
                                                  null, 10, pnlClusterOverview, null);
        m_dvc.addDetailsView(clusterOverview, DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_machines"),
                                                               null, 10, pnlMachine, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_members"),
                                                               null, 10, pnlMember, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_services"),
                                                               null, 10, pnlService, null), DataViewComponent.TOP_RIGHT);
        m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_caches"),
                                                               null, 10, pnlCache, null), DataViewComponent.TOP_RIGHT);

        // add the default panels
        if (pnlClusterSnapshot != null)
            {
            f_setPanels.add(pnlClusterSnapshot);
            pnlClusterSnapshot.setRequestSender(requestSender);
            }
        f_setPanels.add(pnlClusterOverview);
        f_setPanels.add(pnlMachine);
        f_setPanels.add(pnlMember);
        f_setPanels.add(pnlService);
        f_setPanels.add(pnlCache);

        if (model.isHotcacheConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_hotcache"),
                                                                   null, 10, pnlHotCache, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlHotCache);
            }

        // selectively add tabs based upon used functionality
        if (model.isFederationCongfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_federation"),
                                                                   null, 10, pnlFederation, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlFederation);
            }

        if (model.isCoherenceExtendConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_proxy_servers"),
                                                                   null, 10, pnlProxy, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlProxy);
            }

        if (model.isHttpProxyConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_http_proxy_servers"),
                                                                   null, 10, pnlHttpProxy, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlHttpProxy);
            }

        if (model.isTopicsConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_topics"),
                                                                   null, 10, pnlTopic, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlTopic);
            }

        if (model.isPersistenceConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_persistence"),
                                                                   null, 10, pnlPersistence, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlPersistence);
            }

        if (model.isCoherenceWebConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_Coherence_web"),
                                                                   null, 10, pnlHttpSession, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlHttpSession);
            }

        if (model.isElasticDataConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_elastic_data"),
                                                                   null, 10, pnlElasticData, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlElasticData);
            }

        if (model.isJCacheConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_JCache"),
                                                                   null, 10, pnlJCache, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlJCache);
            }

        if (model.isExecutorConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_executors"),
                                                                   null, 10, pnlExecutor, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlExecutor);
            }

        if (model.isGrpcProxyConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_grpc"),
                                                                   null, 10, pnlGrpcProxy, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlGrpcProxy);
            }

        if (model.isHealthConfigured())
            {
            m_dvc.addDetailsView(new DataViewComponent.DetailsView(Localization.getLocalText("LBL_health"),
                                                                   null, 10, pnlHealth, null), DataViewComponent.TOP_RIGHT);
            f_setPanels.add(pnlHealth);
            }

        // update the request sender
        pnlClusterOverview.setRequestSender(requestSender);
        pnlMachine.setRequestSender(requestSender);
        pnlMember.setRequestSender(requestSender);
        pnlService.setRequestSender(requestSender);
        pnlCache.setRequestSender(requestSender);
        pnlHotCache.setRequestSender(requestSender);
        pnlFederation.setRequestSender(requestSender);
        pnlProxy.setRequestSender(requestSender);
        pnlTopic.setRequestSender(requestSender);
        pnlPersistence.setRequestSender(requestSender);
        pnlHttpSession.setRequestSender(requestSender);
        pnlElasticData.setRequestSender(requestSender);
        pnlJCache.setRequestSender(requestSender);
        pnlExecutor.setRequestSender(requestSender);
        pnlGrpcProxy.setRequestSender(requestSender);
        pnlHealth.setRequestSender(requestSender);

        // display a warning if we are connected to a WLS domain and we can
        // see more that 1 domainPartition key. This code relies on us
        // using JMX queries rather than the reporter.
        if (model.getDomainPartitions().size() > 1)
            {
            JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_mt_warning"));
            }

        m_dvc.selectDetailsView(clusterOverview);

        // create a timer that will refresh the TAB's as required every 3 seconds
        // the reason for 3 seconds is so that with the min cluster data refresh time of
        // 5 seconds, the updates will be smoother
        m_timer = new Timer(3000, new ActionListener()
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
                            if (m_application == null || m_application.getState() == Stateful.STATE_AVAILABLE)
                                {
                                // Schedule the SwingWorker to update the GUI
                                model.refreshStatistics(requestSender);

                                // refresh only the panels that were activated on startup
                                for (AbstractCoherencePanel panel : f_setPanels)
                                    {
                                    panel.updateData();
                                    panel.updateGUI();
                                    }
                                }
                            }
                        catch (Exception ex)
                            {
                            LOGGER.log(Level.WARNING, "Error while refreshing tabs. ", e);
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
    private Timer m_timer;

    /**
     * Application.
     */
    private Application m_application;

    /**
     * Indicates if the refresh is running.
     */
    private boolean refreshRunning;

    /**
     * The Request Sender to use.
     */
    private RequestSender requestSender = null;

    /**
     * Set of panels to refresh and update.
     */
    private final Set<AbstractCoherencePanel> f_setPanels = new LinkedHashSet<>();
    }
