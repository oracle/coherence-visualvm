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

package com.oracle.coherence.plugin.visualvm.tracer.proxy;

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
 * A {@link TracerPackage} to show Proxy related probes.
 * 
 * @author tam 2024.03.04
 */
public class ProxyMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {

    public ProxyMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_proxyConnectionsProbeDescriptor = ProxyConnectionCountProbe.createDescriptor(f_model != null);
        m_proxyOutgoingMsgProbeDescriptor = ProxyOutgoingMsgBacklogProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
            m_proxyConnectionsProbeDescriptor,
            m_proxyOutgoingMsgProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_proxyConnectionsProbeDescriptor)
            {
            if (m_proxyConnectionsProbe == null)
                {
                m_proxyConnectionsProbe = new ProxyConnectionCountProbe(this);
                }
            return m_proxyConnectionsProbe;
            }
        else if (descriptor == m_proxyOutgoingMsgProbeDescriptor)
            {
            if (m_proxyOutgoingMsgBacklogProbe == null)
                {
                m_proxyOutgoingMsgBacklogProbe = new ProxyOutgoingMsgBacklogProbe(this);
                }
            return m_proxyOutgoingMsgBacklogProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_proxy_probe");
    private static final String DESCR = Localization.getLocalText("LBL_proxy_probe_description");
    private static final int POSITION = 20520;

    private TracerProbeDescriptor         m_proxyConnectionsProbeDescriptor;
    private TracerProbeDescriptor         m_proxyOutgoingMsgProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_proxyConnectionsProbe;
    private AbstractCoherenceMonitorProbe m_proxyOutgoingMsgBacklogProbe;

    private final VisualVMModel f_model;
    }
