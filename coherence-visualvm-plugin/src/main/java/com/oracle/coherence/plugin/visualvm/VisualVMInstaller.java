/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.coherence.plugin.visualvm.datasource.CoherenceClusterDataSourceDescriptorProvider;
import com.oracle.coherence.plugin.visualvm.datasource.CoherenceClusterDataSourceViewProvider;
import com.oracle.coherence.plugin.visualvm.datasource.CoherenceClustersDataSource;

import com.oracle.coherence.plugin.visualvm.impl.CoherenceClusterProvider;

import com.oracle.coherence.plugin.visualvm.tracer.cache.CacheMonitorPackage;
import com.oracle.coherence.plugin.visualvm.tracer.cluster.ClusterMonitorPackage;
import com.oracle.coherence.plugin.visualvm.tracer.proxy.ProxyMonitorPackage;
import com.oracle.coherence.plugin.visualvm.tracer.service.ServiceMonitorPackage;

import org.graalvm.visualvm.application.Application;

import org.graalvm.visualvm.modules.tracer.TracerPackage;
import org.graalvm.visualvm.modules.tracer.TracerPackageProvider;
import org.graalvm.visualvm.modules.tracer.TracerSupport;

import org.openide.modules.ModuleInstall;

/**
 * Installer module for the Coherence plugin.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class VisualVMInstaller
        extends ModuleInstall
    {

    // ----- ModuleInstall methods ------------------------------------------

    /**
     * {@inheritDoc }
     */
    @Override
    public void restored()
        {
        VisualVMViewProvider.initialize();
        CoherenceClustersDataSource.register();
        CoherenceClusterDataSourceDescriptorProvider.register();
        CoherenceClusterDataSourceViewProvider.register();
        CoherenceClusterProvider.initCoherenceClustersDataSource();
        CoherenceApplicationTypeFactory.initialize();

        // register the tracer probes
        if (m_provider == null)
            {
            m_provider = new TracerPackageProviderImpl();
            }

        TracerSupport.getInstance().registerPackageProvider(m_provider);
        }

    /**
     * {@inheritDoc }
     */
    @Override
    public void uninstalled()
        {
        VisualVMViewProvider.unregister();
        CoherenceClustersDataSource.unregister();
        CoherenceClusterDataSourceDescriptorProvider.unregister();
        CoherenceClusterDataSourceViewProvider.unregister();
        CoherenceApplicationTypeFactory.shutdown();

        // un-register the tracer probes
        if (m_provider == null)
            {
            m_provider = new TracerPackageProviderImpl();
            }

        TracerSupport.getInstance().unregisterPackageProvider(m_provider);
        }
        
    /**
     * Provider of Coherence tracer probes.
     */
    private static class TracerPackageProviderImpl
            extends TracerPackageProvider<Application>
        {

        TracerPackageProviderImpl()
            {
            super(Application.class);
            }

        public TracerPackage<Application>[] getPackages(Application application)
            {
            return new TracerPackage[]
                {
                new ClusterMonitorPackage(application),
                new ProxyMonitorPackage(application),
                new ServiceMonitorPackage(application),
                new CacheMonitorPackage(application)
                };
            }
        }

    private transient TracerPackageProviderImpl m_provider;
    }
