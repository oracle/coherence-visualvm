/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.tracer.cache;

import static com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe.ICON;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.VisualVMView;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.application.Application;

import org.graalvm.visualvm.modules.tracer.TracerPackage;
import org.graalvm.visualvm.modules.tracer.TracerProbe;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;


/**
 * A {@link TracerPackage} to show cache related probes for the currently selected cache.
 * 
 * @author tam 2024.03.12
 */
public class SelectedCacheMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {
    // ----- constructors ---------------------------------------------------

    /**
     * Construct a monitor package.
     * @param application {@link Application} to monitor
     */
    public SelectedCacheMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_sizeProbeDescriptor      = SelectedCacheCountProbe.createDescriptor(f_model != null);
        m_memoryProbeDescriptor    = SelectedCacheMemoryProbe.createDescriptor(f_model != null);
        m_listenersProbeDescriptor = SelectedCacheListenersProbe.createDescriptor(f_model != null);
        m_queryProbeDescriptor     = SelectedCacheQueryProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
            m_sizeProbeDescriptor,
            m_memoryProbeDescriptor,
            m_listenersProbeDescriptor,
            m_queryProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_sizeProbeDescriptor)
            {
            if (m_sizeProbe == null)
                {
                m_sizeProbe = new SelectedCacheCountProbe(this);
                }
            return m_sizeProbe;
            }
        else if (descriptor == m_memoryProbeDescriptor)
            {
            if (m_memoryProbe == null)
                {
                m_memoryProbe = new SelectedCacheMemoryProbe(this);
                }
            return m_memoryProbe;
            }
        else if (descriptor == m_listenersProbeDescriptor)
            {
            if (m_listenersProbe == null)
                {
                m_listenersProbe = new SelectedCacheListenersProbe(this);
                }
            return m_listenersProbe;
            }
        else if (descriptor == m_queryProbeDescriptor)
            {
            if (m_queryProbe == null)
                {
                m_queryProbe = new SelectedCacheQueryProbe(this);
                }
            return m_queryProbe;
            }
        else
            {
            return null;
            }
        }

    // ---- AbstractCoherenceMonitorProbe.MonitoredDataResolver interface ---

    @Override
    public VisualVMModel getMonitoredData()
        {
        return f_model;
        }

    // ----- constants ------------------------------------------------------

    private static final String NAME = Localization.getLocalText("LBL_selected_cache_probe");
    private static final String DESCR = Localization.getLocalText("LBL_selected_cache_probe_description");
    private static final int POSITION = 20511;

    private TracerProbeDescriptor         m_sizeProbeDescriptor;
    private TracerProbeDescriptor         m_memoryProbeDescriptor;
    private TracerProbeDescriptor         m_listenersProbeDescriptor;
    private TracerProbeDescriptor         m_queryProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_sizeProbe;
    private AbstractCoherenceMonitorProbe m_memoryProbe;
    private AbstractCoherenceMonitorProbe m_listenersProbe;
    private AbstractCoherenceMonitorProbe m_queryProbe;

    private final VisualVMModel f_model;
    }
