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
 * A {@link TracerPackage} to show service related probes.
 * 
 * @author tam 2024.03.06
 */
public class ServiceMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {

    // ----- constructors ---------------------------------------------------

    /**
     * Construct a monitor package.
     * @param application {@link Application} to monitor
     */
    public ServiceMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_pendingRequestsProbeDescriptor = PartitionedPendingRequestsProbe.createDescriptor(f_model != null);
        m_endangeredProbeDescriptor      = EndangeredPartitionsProbe.createDescriptor(f_model != null);
        m_unbalancedProbeDescriptor      = UnbalancedPartitionsProbe.createDescriptor(f_model != null);
        m_vulnerableProbeDescriptor      = VulnerablePartitionsProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
            m_pendingRequestsProbeDescriptor,
            m_endangeredProbeDescriptor,
            m_unbalancedProbeDescriptor,
            m_vulnerableProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_pendingRequestsProbeDescriptor)
            {
            if (m_PendingRequestsProbe == null)
                {
                m_PendingRequestsProbe = new PartitionedPendingRequestsProbe(this);
                }
            return m_PendingRequestsProbe;
            }
        else if (descriptor == m_endangeredProbeDescriptor)
            {
            if (m_endangeredProbe == null)
                {
                m_endangeredProbe = new EndangeredPartitionsProbe(this);
                }
            return m_endangeredProbe;
            }
        else if (descriptor == m_unbalancedProbeDescriptor)
            {
            if (m_unbalancedProbe == null)
                {
                m_unbalancedProbe = new UnbalancedPartitionsProbe(this);
                }
            return m_unbalancedProbe;
            }
        else if (descriptor == m_vulnerableProbeDescriptor)
            {
            if (m_vulnerableProbe == null)
                {
                m_vulnerableProbe = new VulnerablePartitionsProbe(this);
                }
            return m_vulnerableProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_service_probe");
    private static final String DESCR = Localization.getLocalText("LBL_service_probe_description");
    private static final int POSITION = 20505;

    private TracerProbeDescriptor         m_pendingRequestsProbeDescriptor;
    private TracerProbeDescriptor         m_endangeredProbeDescriptor;
    private TracerProbeDescriptor         m_unbalancedProbeDescriptor;
    private TracerProbeDescriptor         m_vulnerableProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_PendingRequestsProbe;
    private AbstractCoherenceMonitorProbe m_endangeredProbe;
    private AbstractCoherenceMonitorProbe m_unbalancedProbe;
    private AbstractCoherenceMonitorProbe m_vulnerableProbe;

    private final VisualVMModel f_model;
    }
