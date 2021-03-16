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

package com.oracle.coherence.plugin.visualvm.datasource;

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMView;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import javax.swing.SwingUtilities;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.DataSourceViewProvider;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;

/**
 * The {@link DataSourceViewProvider} for {@link CoherenceClusterDataSource}.
 *
 * @author shyaradh 12.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
public class CoherenceClusterDataSourceViewProvider
        extends DataSourceViewProvider<CoherenceClusterDataSource>
    {
    // ----- DataSourceViewProvider methods ---------------------------------

    @Override
    protected boolean supportsViewFor(CoherenceClusterDataSource coherenceClusterDataSource)
        {
        String sUrl = coherenceClusterDataSource.getUrl();
        HttpRequestSender requestSender = new HttpRequestSender(sUrl);
        final StatusDisplayer.Message[] status = new StatusDisplayer.Message[1];

        String sText = getLocalText("LBL_testing_connection", sUrl);
        SwingUtilities.invokeLater(()->status[0] = StatusDisplayer.getDefault().setStatusText(sText, 2400));

        // BUG 29213475 - Check for a valid HttpRequestSender URL before we start the refresh
        String sMessage = Localization.getLocalText("ERR_Invalid_URL", sUrl);
        try
            {
            JsonNode rootClusterMembers = requestSender.getListOfClusterMembers();
            if (rootClusterMembers == null)
                {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
                }
            }
        catch (Exception e)
            {
            sMessage = sMessage + "\nError: " + e.getMessage();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
            return false;
            }
        finally
            {
            if (status[0] != null )
                {
                status[0].clear(1000);
                }
            }
        return true;
        }

    @Override
    protected DataSourceView createView(CoherenceClusterDataSource coherenceClusterDataSource)
        {
        return new VisualVMView(coherenceClusterDataSource);
        }

    // ----- CoherenceClusterDataSourceViewProvider methods --------------

    /**
     * Register the view provider with the manager.
     */
    public static void register()
        {
        DataSourceViewsManager.sharedInstance().addViewProvider(INSTANCE, CoherenceClusterDataSource.class);
        }

    /**
     * Unregister the view provider with the manager.
     */
    public static void unregister()
        {
        DataSourceViewsManager.sharedInstance().removeViewProvider(INSTANCE);
        }

    // ----- constants ------------------------------------------------------

    /**
     * The singleton instance of {@link CoherenceClusterDataSourceViewProvider}.
     */
    final protected static CoherenceClusterDataSourceViewProvider INSTANCE =
            new CoherenceClusterDataSourceViewProvider();
    }
