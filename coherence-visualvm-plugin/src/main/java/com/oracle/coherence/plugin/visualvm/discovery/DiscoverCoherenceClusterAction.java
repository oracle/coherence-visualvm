/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.discovery;

import com.oracle.coherence.plugin.visualvm.Localization;

import com.oracle.coherence.plugin.visualvm.VisualVMView;
import com.oracle.coherence.plugin.visualvm.impl.CoherenceClusterProvider;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javax.swing.SwingWorker;
import org.graalvm.visualvm.core.ui.actions.SingleDataSourceAction;
import org.graalvm.visualvm.host.Host;
import org.openide.awt.StatusDisplayer;

import java.awt.event.ActionEvent;

import java.util.Map;
import java.util.Set;;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 * Class to add a right-click option on a Host in VisualVM to discover Coherence clusters via name service.
 *
 * @author tam 2022.10.13
 */
public class DiscoverCoherenceClusterAction
        extends SingleDataSourceAction<Host>
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Construct a new instance.
     */
    private DiscoverCoherenceClusterAction() {
        super(Host.class);
        String sLabel = Localization.getLocalText("LBL_discover_cluster");

        putValue(NAME, sLabel);
        putValue(SHORT_DESCRIPTION,sLabel);
    }

    // ----- SingleDataSourceAction methods ---------------------------------

    @Override
    protected void actionPerformed(Host host, ActionEvent actionEvent)
        {
        String sHostName = host.getHostName();

        status[0] = StatusDisplayer.getDefault().setStatusText(Localization.getLocalText("LBL_discovering_clusters", sHostName),5);

        try
            {
            new DiscoverClusters(host).execute();
            }
        catch (Exception e)
           {
           LOGGER.log(Level.WARNING, "Error running discover clusters", e);
           }
        }

    /**
     * Inner class to discover clusters.
     */
    private class DiscoverClusters
            extends SwingWorker<Object, Object>
        {
        // ----- constructors -----------------------------------------------

        protected DiscoverClusters(Host host)
            {
            f_host = host;
            }

        // ----- SwingWorker methods -----------------------------------------

        @Override
        protected Object doInBackground() throws Exception
            {
            String                          sHostName = f_host.getHostName();

            try
                {

                Map<String, String> mapClusters = DiscoveryUtils.discoverManagementURLS(sHostName, DEFAULT_NS_PORT);

                if (mapClusters.isEmpty())
                    {
                    JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_no_clusters", sHostName));
                    return null;
                    }

                StringBuilder sb = new StringBuilder(Localization.getLocalText("LBL_confirm_add_clusters"));
                mapClusters.forEach((k,v) -> sb.append("- ").append(k).append(": ").append(v).append("\n"));
                status[0].clear(20);

                if (JOptionPane.showConfirmDialog(null, sb.toString(),
                                                                  Localization.getLocalText("LBL_confirm_operation"),
                                                                  JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                   {
                   return null;
                   }

                mapClusters.forEach((k,v) -> CoherenceClusterProvider.createCoherenceClusterDataSource(v, k));
                }
            catch (Exception e)
                {
                JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_error_discovering", sHostName), "Warning", WARNING_MESSAGE);
                LOGGER.log(Level.WARNING, "Unable to discover clusters", e);
                }
            return null;
            }

        // ----- data members ---------------------------------------------------

        private final Host f_host;
        }

    @Override
    protected boolean isEnabled(Host host) {
      return host != Host.UNKNOWN_HOST;
    }

    @Override
    protected void updateState(Set<Host> selectedHosts)
        {
        if (tracksSelection)
            {
            super.updateState(selectedHosts);
            }
        }

    // ----- accessors ------------------------------------------------------

    public static synchronized DiscoverCoherenceClusterAction alwaysEnabled()
        {
        if (alwaysEnabled == null)
            {
            alwaysEnabled = new DiscoverCoherenceClusterAction();
            alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(VisualVMView.NODE_ICON));
            alwaysEnabled.putValue("iconBase", VisualVMView.IMAGE_PATH);  // NOI18N
            }
        return alwaysEnabled;
        }

    public static synchronized DiscoverCoherenceClusterAction selectionAware()
        {
        if (selectionAware == null)
            {
            selectionAware = new DiscoverCoherenceClusterAction();
            selectionAware.tracksSelection = true;
            }
        return selectionAware;
        }

    // ----- constants ------------------------------------------------------

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(DiscoverCoherenceClusterAction.class.getName());

    /**
     * Default NS port.
     */
    private static final int DEFAULT_NS_PORT = 7574;

    private StatusDisplayer.Message[] status = new StatusDisplayer.Message[1];

    // ----- data members ---------------------------------------------------

    private static DiscoverCoherenceClusterAction alwaysEnabled;
    private static DiscoverCoherenceClusterAction selectionAware;
    private boolean tracksSelection = false;
    }
