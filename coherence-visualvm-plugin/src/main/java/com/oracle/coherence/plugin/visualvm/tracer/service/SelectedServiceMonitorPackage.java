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

package com.oracle.coherence.plugin.visualvm.tracer.service;

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
 * A {@link TracerPackage} to show service related probes for the currently selected service.
 * 
 * @author tam 2024.03.12
 */
public class SelectedServiceMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {

    // ----- constructors ---------------------------------------------------

    /**
     * Construct a monitor package.
     * @param application {@link Application} to monitor
     */
    public SelectedServiceMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_threadCountProbeDescriptor    = SelectedServiceThreadCountProbe.createDescriptor(f_model != null);
        m_threadUtilProbeDescriptor     = SelectedServiceThreadUtilizationProbe.createDescriptor(f_model != null);
        m_taskAverageProbeDescriptor    = SelectedServiceTaskAverageProbe.createDescriptor(f_model != null);
        m_requestAverageProbeDescriptor = SelectedServiceRequestAverageProbe.createDescriptor(f_model != null);
        m_taskBacklogProbeDescriptor    = SelectedServiceTaskBackLogProbe.createDescriptor(f_model != null);
        m_partitionsProbeDescriptor     = SelectedServicePartitionsProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
            m_threadCountProbeDescriptor,
            m_threadUtilProbeDescriptor,
            m_taskAverageProbeDescriptor,
            m_requestAverageProbeDescriptor,
            m_taskBacklogProbeDescriptor,
            m_partitionsProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_threadCountProbeDescriptor)
            {
            if (m_threadCountProbe == null)
                {
                m_threadCountProbe = new SelectedServiceThreadCountProbe(this);
                }
            return m_threadCountProbe;
            }
        else if (descriptor == m_threadUtilProbeDescriptor)
            {
            if (m_threadUtilProbe == null)
                {
                m_threadUtilProbe = new SelectedServiceThreadUtilizationProbe(this);
                }
            return m_threadUtilProbe;
            }
        else if (descriptor == m_taskAverageProbeDescriptor)
            {
            if (m_taskAverageProbe == null)
                {
                m_taskAverageProbe = new SelectedServiceTaskAverageProbe(this);
                }
            return m_taskAverageProbe;
            }
        else if (descriptor == m_requestAverageProbeDescriptor)
            {
            if (m_requestAverageProbe == null)
                {
                m_requestAverageProbe = new SelectedServiceRequestAverageProbe(this);
                }
            return m_requestAverageProbe;
            }
        else if (descriptor == m_taskBacklogProbeDescriptor)
            {
            if (m_taskBacklogProbe == null)
                {
                m_taskBacklogProbe = new SelectedServiceTaskBackLogProbe(this);
                }
            return m_taskBacklogProbe;
            }
        else if (descriptor == m_partitionsProbeDescriptor)
            {
            if (m_partitionsProbe == null)
                {
                m_partitionsProbe = new SelectedServicePartitionsProbe(this);
                }
            return m_partitionsProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_selected_service_probe");
    private static final String DESCR = Localization.getLocalText("LBL_selected_service_probe_description");
    private static final int POSITION = 20506;

    private TracerProbeDescriptor         m_threadCountProbeDescriptor;
    private TracerProbeDescriptor         m_threadUtilProbeDescriptor;
    private TracerProbeDescriptor         m_taskAverageProbeDescriptor;
    private TracerProbeDescriptor         m_requestAverageProbeDescriptor;
    private TracerProbeDescriptor         m_taskBacklogProbeDescriptor;
    private TracerProbeDescriptor         m_partitionsProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_threadCountProbe;
    private AbstractCoherenceMonitorProbe m_threadUtilProbe;
    private AbstractCoherenceMonitorProbe m_taskAverageProbe;
    private AbstractCoherenceMonitorProbe m_requestAverageProbe;
    private AbstractCoherenceMonitorProbe m_taskBacklogProbe;
    private AbstractCoherenceMonitorProbe m_partitionsProbe;

    private final VisualVMModel f_model;
    }
