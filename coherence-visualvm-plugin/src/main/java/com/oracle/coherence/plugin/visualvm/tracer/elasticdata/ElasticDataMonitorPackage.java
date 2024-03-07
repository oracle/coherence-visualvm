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

package com.oracle.coherence.plugin.visualvm.tracer.elasticdata;

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
 * A {@link TracerPackage} to show elastic data related probes.
 * 
 * @author tam 2024.03.07
 */
public class ElasticDataMonitorPackage
        extends TracerPackage<Application> implements AbstractCoherenceMonitorProbe.MonitoredDataResolver {

    public ElasticDataMonitorPackage(Application application)
        {
        super(NAME, DESCR, ICON, POSITION);
        this.f_model = VisualVMView.getModelForApplication(application);
        }

    // ---- TracerPackage methods -------------------------------------------

    @Override
    public TracerProbeDescriptor[] getProbeDescriptors() {
        m_ramournalMemoryProbeDescriptor   = RamJournalMemoryProbe.createDescriptor(f_model != null);
        m_flashjournalSpaceProbeDescriptor = FlashJournalSpaceProbe.createDescriptor(f_model != null);
        m_ramjournalFilesProbeDescriptor   = RamJournalFilesProbe.createDescriptor(f_model != null);
        m_flashjournalFilesProbeDescriptor = FlashJournalFilesProbe.createDescriptor(f_model != null);

        return new TracerProbeDescriptor[] {
                m_ramournalMemoryProbeDescriptor,
                m_flashjournalSpaceProbeDescriptor,
                m_ramjournalFilesProbeDescriptor,
                m_flashjournalFilesProbeDescriptor
        };
    }

    @Override
    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor)
        {
        if (descriptor == m_ramournalMemoryProbeDescriptor)
            {
            if (m_ramjournalMemoryProbe == null)
                {
                m_ramjournalMemoryProbe = new RamJournalMemoryProbe(this);
                }
            return m_ramjournalMemoryProbe;
            }
        else if (descriptor == m_flashjournalSpaceProbeDescriptor)
            {
            if (m_flashjournalSpaceProbe == null)
                {
                m_flashjournalSpaceProbe = new FlashJournalSpaceProbe(this);
                }
            return m_flashjournalSpaceProbe;
            }
        else if (descriptor == m_flashjournalFilesProbeDescriptor)
            {
            if (m_flashjournalFilesProbe == null)
                {
                m_flashjournalFilesProbe = new FlashJournalFilesProbe(this);
                }
            return m_flashjournalFilesProbe;
            }
        else if (descriptor == m_ramjournalFilesProbeDescriptor)
            {
            if (m_ramjournalFilesProbe == null)
                {
                m_ramjournalFilesProbe = new RamJournalFilesProbe(this);
                }
            return m_ramjournalFilesProbe;
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

    private static final String NAME = Localization.getLocalText("LBL_elasticdata_probe");
    private static final String DESCR = Localization.getLocalText("LBL_elasticdata_probe_description");
    private static final int POSITION = 570;

    private TracerProbeDescriptor m_ramournalMemoryProbeDescriptor;
    private TracerProbeDescriptor m_flashjournalSpaceProbeDescriptor;
    private TracerProbeDescriptor m_ramjournalFilesProbeDescriptor;
    private TracerProbeDescriptor m_flashjournalFilesProbeDescriptor;

    private AbstractCoherenceMonitorProbe m_ramjournalMemoryProbe;
    private AbstractCoherenceMonitorProbe m_flashjournalSpaceProbe;
    private AbstractCoherenceMonitorProbe m_ramjournalFilesProbe;
    private AbstractCoherenceMonitorProbe m_flashjournalFilesProbe;

    private final VisualVMModel f_model;
    }
