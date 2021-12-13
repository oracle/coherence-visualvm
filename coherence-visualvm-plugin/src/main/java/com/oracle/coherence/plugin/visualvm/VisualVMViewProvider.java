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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.DataSourceViewProvider;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.JmxModelFactory;

/**
 * Class to provide the view for Coherence JVisualVM plugin.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class VisualVMViewProvider
        extends DataSourceViewProvider<Application>
    {

    // ----- DataSourceViewProvider methods ---------------------------------

    /**
     * Returns true or false indicating if the JMX connection is actually for
     * a Coherence cluster or not.
     *
     * @return true if a Coherence cluster otherwise false
     */
    @Override
    public boolean supportsViewFor(Application application)
        {
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);

        // system property "PROP_DISABLE_MBEAN_CHECK" is for disabling the MBean check when connecting to
        // WebLogic Server as sometimes the Coherence MBean does not show up immediately and as a result
        // the Coherence tab never gets displayed

        if (jmx != null && jmx.getConnectionState() == JmxModel.ConnectionState.CONNECTED)
            {
            if (GlobalPreferences.sharedInstance().isMBeanCheckDisabled())
                {
                return true;
                }

            MBeanServerConnection connection = jmx.getMBeanServerConnection();

            try
                {
                if (connection.isRegistered(new ObjectName("Coherence:type=Cluster")))
                    {
                    return true;
                    }
                }
            catch (Exception ex)
                {
                ex.printStackTrace();
                }
            }

        return false;
        }

    @Override
    protected DataSourceView createView(Application application)
        {
        return new VisualVMView(application);
        }

    /**
     * Supports Save views.
     *
     * @param app {@link Application}
     *
     * @return true if support for save views
     */
    public boolean supportsSaveViewsFor(Application app)
        {
        return false;
        }

    /**
     * Save views.
     *
     * @param appSource source {@link Application}
     * @param appDest   destination {@link Application}
     */
    public void saveViews(Application appSource, Application appDest)
        {
        }

    /**
     * Initialize a new the view provider.
     */
    static void initialize()
        {
        DataSourceViewsManager.sharedInstance().addViewProvider(s_instance, Application.class);
        }

    /**
     * Unregister the view provider.
     */
    static void unregister()
        {
        DataSourceViewsManager.sharedInstance().removeViewProvider(s_instance);
        }

    // ----- constants ------------------------------------------------------

    private static DataSourceViewProvider<Application> s_instance = new VisualVMViewProvider();
    }
