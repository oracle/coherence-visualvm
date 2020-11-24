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

package com.oracle.coherence.plugin.visualvm.impl;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMView;
import com.oracle.coherence.plugin.visualvm.datasource.CoherenceClustersDataSource;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.graalvm.visualvm.core.ui.actions.SingleDataSourceAction;

/**
 * The {@link SingleDataSourceAction} for adding a Coherence cluster to the {@link CoherenceClustersDataSource}.
 *
 * @author sr 12.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
public class AddCoherenceClusterAction
        extends SingleDataSourceAction<CoherenceClustersDataSource>
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create an instance of {@link AddCoherenceClusterAction}.
     */
    private AddCoherenceClusterAction()
        {
        super(CoherenceClustersDataSource.class);
        putValue(NAME, Localization.getLocalText("LBL_Add_Coherence_Cluster"));
        putValue(SHORT_DESCRIPTION, Localization.getLocalText("TTIP_Add_Coherence_Cluster"));
        }

    // ----- SingleDataSourceAction methods ---------------------------------

    @Override
    protected void actionPerformed(CoherenceClustersDataSource coherenceApplicationsDataSource, ActionEvent actionEvent)
        {
        CoherenceClusterConfigurator coherenceClusterConfigurator =
                CoherenceClusterConfigurator.defineApplication();
        if (coherenceClusterConfigurator != null)
            {
            CoherenceClusterProvider.createCoherenceClusterDataSource(coherenceClusterConfigurator.getAppUrl(),
                    coherenceClusterConfigurator.getClusterName());
            }
        }

    @Override
    protected boolean isEnabled(CoherenceClustersDataSource coherenceApplicationsDataSource)
        {
        return true;
        }

    // ----- AddCoherenceClusterAction methods --------------------------

    /**
     * Return the always enabled action. This method is specified in the layer.xml.
     *
     * @return the always enabled action
     */
    public static synchronized AddCoherenceClusterAction alwaysEnabled()
        {
        if (s_alwaysEnabled == null)
            {
            s_alwaysEnabled = new AddCoherenceClusterAction();
            s_alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(VisualVMView.NODE_ICON));
            s_alwaysEnabled.putValue("iconBase", VisualVMView.IMAGE_PATH);
            }
        return s_alwaysEnabled;
        }

    /**
     * Return the selection aware action. This method is specified in the layer.xml.
     *
     * @return the selection aware action
     */
    public static synchronized AddCoherenceClusterAction selectionAware()
        {
        if (s_selectionAware == null)
            {
            s_selectionAware = new AddCoherenceClusterAction();
            }
        return s_selectionAware;
        }


    // ----- data members ---------------------------------------------------

    /**
     * The always enabled application action.
     */
    // This variable name is exposed to layer.xml, so it does not follow source code naming convention.
    private static AddCoherenceClusterAction s_alwaysEnabled;

    /**
     * The selection awarw application action.
     */
    // This variable name is exposed to layer.xml, so it does not follow source code naming convention.
    private static AddCoherenceClusterAction s_selectionAware;
    }
