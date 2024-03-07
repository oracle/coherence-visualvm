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

package com.oracle.coherence.plugin.visualvm.tracer.federation;

import static com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe.ICON;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.VisualVMView;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;;

import org.graalvm.visualvm.application.Application;

import org.graalvm.visualvm.modules.tracer.TracerPackage;
import org.graalvm.visualvm.modules.tracer.TracerProbe;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;


/**
 * A {@link TracerPackage} to show federation related probes.
 * 
 * @author tam 2024.03.06
 */
public class FederationMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {

    public FederationMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_bytesSentPerSecondProbeDescriptor = BytesSentSecProbe.createDescriptor(f_model != null);
        m_msgsSentPerSecondProbeDescriptor  = MsgsSentSecProbe.createDescriptor(f_model != null);
        m_bytesRecPerSecondProbeDescriptor  = BytesReceivedSecProbe.createDescriptor(f_model != null);
        m_msgsRecPerSecondProbeDescriptor   = MsgsReceivedSecProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
                m_bytesSentPerSecondProbeDescriptor,
                m_msgsSentPerSecondProbeDescriptor,
                m_bytesRecPerSecondProbeDescriptor,
                m_msgsRecPerSecondProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_bytesSentPerSecondProbeDescriptor)
            {
            if (m_bytesSentPerSecondProbe == null)
                {
                m_bytesSentPerSecondProbe = new BytesSentSecProbe(this);
                }
            return m_bytesSentPerSecondProbe;
            }
        else if (descriptor == m_msgsSentPerSecondProbeDescriptor)
            {
            if (m_msgsSentPerSecondProbe == null)
                {
                m_msgsSentPerSecondProbe = new MsgsSentSecProbe(this);
                }
            return m_msgsSentPerSecondProbe;
            }
        else if (descriptor == m_bytesRecPerSecondProbeDescriptor)
            {
            if (m_bytesRecPerSecondProbe == null)
                {
                m_bytesRecPerSecondProbe = new BytesReceivedSecProbe(this);
                }
            return m_bytesRecPerSecondProbe;
            }
        else if (descriptor == m_msgsRecPerSecondProbeDescriptor)
            {
            if (m_msgsRecPerSecondProbe == null)
                {
                m_msgsRecPerSecondProbe = new MsgsReceivedSecProbe(this);
                }
            return m_msgsRecPerSecondProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_federation_probe");
    private static final String DESCR = Localization.getLocalText("LBL_federation_probe_description");
    private static final int POSITION = 550;

    private TracerProbeDescriptor m_bytesSentPerSecondProbeDescriptor;
    private TracerProbeDescriptor m_msgsSentPerSecondProbeDescriptor;
    private TracerProbeDescriptor m_bytesRecPerSecondProbeDescriptor;
    private TracerProbeDescriptor m_msgsRecPerSecondProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_bytesSentPerSecondProbe;
    private AbstractCoherenceMonitorProbe m_msgsSentPerSecondProbe;
    private AbstractCoherenceMonitorProbe m_bytesRecPerSecondProbe;
    private AbstractCoherenceMonitorProbe m_msgsRecPerSecondProbe;

    private final VisualVMModel f_model;
    }
