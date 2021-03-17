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

import com.oracle.coherence.plugin.visualvm.VisualVMView;

import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.DataSourceViewProvider;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;

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
        // we don't do the test here as this can cause delays and
        // confusion if a user clicks on a a URL that doesn't exist
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
