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

import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import org.graalvm.visualvm.core.model.AbstractModelProvider;

/**
 * The {@link AbstractModelProvider} for {@link CoherenceClusterDataSourceDescriptor}.
 *
 * @author shyaradh 12.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
public class CoherenceClusterDataSourceDescriptorProvider
        extends AbstractModelProvider<DataSourceDescriptor, DataSource>
    {
    // ----- AbstractModelProvider methods ----------------------------------

    @Override
    public DataSourceDescriptor createModelFor(DataSource dataSource)
        {
        if (dataSource instanceof CoherenceClusterDataSource)
            {
            return new CoherenceClusterDataSourceDescriptor((CoherenceClusterDataSource) dataSource);
            }
        return null;
        }

    // ----- CoherenceClusterDataSourceDescriptorProvider methods --------

    /**
     * Register the provider with the factory.
     */
    public static void register()
        {
        DataSourceDescriptorFactory.getDefault().registerProvider(INSTANCE);
        }

    /**
     * Unregister the provider with the factory.
     */
    public static void unregister()
        {
        DataSourceDescriptorFactory.getDefault().unregisterProvider(INSTANCE);
        }

    // ----- constants ------------------------------------------------------

    /**
     * The static singleton instance of {@link CoherenceClusterDataSourceDescriptorProvider}.
     */
    final private static CoherenceClusterDataSourceDescriptorProvider INSTANCE = new CoherenceClusterDataSourceDescriptorProvider();
    }
