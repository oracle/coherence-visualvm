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

package com.oracle.coherence.plugin.visualvm.tracer.cluster;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.VisualVMView;
import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.application.Application;

import org.graalvm.visualvm.modules.tracer.TracerPackage;
import org.graalvm.visualvm.modules.tracer.TracerProbe;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

import static com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe.ICON;


/**
 * A {@link TracerPackage} to show cluster related probes.
 * 
 * @author tam 2024.03.04
 */
public class ClusterMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {
    
    public ClusterMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_heapProbeDescriptor            = StorageMembersHeapProbe.createDescriptor(f_model != null);
        m_clusterSizeProbeDescriptor     = ClusterSizeProbe.createDescriptor(f_model != null);
        m_departureCountProbeDescriptor  = DepartureCountProbe.createDescriptor(f_model != null);
        m_packetPublisherProbeDescriptor = PacketPublisherProbe.createDescriptor(f_model != null);
        m_packetReceiverProbeDescriptor  = PacketReceiverProbe.createDescriptor(f_model != null);
        m_loadAverageProbeDescriptor     =  LoadAverageProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
                m_heapProbeDescriptor,
                m_clusterSizeProbeDescriptor,
                m_departureCountProbeDescriptor,
                m_packetPublisherProbeDescriptor,
                m_packetReceiverProbeDescriptor,
                m_loadAverageProbeDescriptor,
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_heapProbeDescriptor)
            {
            if (m_heapProbe == null)
                {
                m_heapProbe = new StorageMembersHeapProbe(this);
                }
            return m_heapProbe;
            }
        else if (descriptor == m_clusterSizeProbeDescriptor)
            {
            if (m_clusterSizeProbe == null)
                {
                m_clusterSizeProbe = new ClusterSizeProbe(this);
                }
            return m_clusterSizeProbe;
            }
        else if (descriptor == m_departureCountProbeDescriptor)
            {
            if (m_departureCountProbe == null)
                {
                m_departureCountProbe = new DepartureCountProbe(this);
                }
            return m_departureCountProbe;
            }
        else if (descriptor == m_packetPublisherProbeDescriptor)
            {
            if (m_packetPublisherProbe == null)
                {
                m_packetPublisherProbe = new PacketPublisherProbe(this);
                }
            return m_packetPublisherProbe;
            }
        else if (descriptor == m_packetReceiverProbeDescriptor)
            {
            if (m_packetReceiverProbe == null)
                {
                m_packetReceiverProbe = new PacketReceiverProbe(this);
                }
            return m_packetReceiverProbe;
            }
        else if (descriptor == m_loadAverageProbeDescriptor)
            {
            if (m_loadAverageProbe == null)
                {
                m_loadAverageProbe = new LoadAverageProbe(this);
                }
            return m_loadAverageProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_cluster_probe");
    private static final String DESCR = Localization.getLocalText("LBL_cluster_probe_description");
    private static final int POSITION = 500;

    private TracerProbeDescriptor         m_heapProbeDescriptor;
    private TracerProbeDescriptor         m_clusterSizeProbeDescriptor;
    private TracerProbeDescriptor         m_departureCountProbeDescriptor;
    private TracerProbeDescriptor         m_packetPublisherProbeDescriptor;
    private TracerProbeDescriptor         m_packetReceiverProbeDescriptor;
    private TracerProbeDescriptor         m_loadAverageProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_heapProbe;
    private AbstractCoherenceMonitorProbe m_clusterSizeProbe;
    private AbstractCoherenceMonitorProbe m_departureCountProbe;
    private AbstractCoherenceMonitorProbe m_packetPublisherProbe;
    private AbstractCoherenceMonitorProbe m_packetReceiverProbe;
    private AbstractCoherenceMonitorProbe m_loadAverageProbe;

    private final VisualVMModel f_model;
    }
