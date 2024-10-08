/*
 *  Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package com.oracle.coherence.plugin.visualvm.tracer.cluster;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

import java.util.List;
import java.util.Map;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;
import static com.oracle.coherence.plugin.visualvm.helper.GraphHelper.MB;
import static com.oracle.coherence.plugin.visualvm.panel.AbstractCoherencePanel.getMemberMemoryRateData;

/**
 * Tracer probe to return maximum and used storage heap for a cluster.
 * 
 * @author tam 2024.03.03
 */
public class StorageMembersHeapProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Construct the probe.
     * @param resolver {@link MonitoredDataResolver}.
     */
    public StorageMembersHeapProbe(MonitoredDataResolver resolver)
        {
        super(2, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        List<Map.Entry<Object, Data>> data = model.getData(VisualVMModel.DataType.MEMBER);
        if (data == null || data.isEmpty())
            {
            return ZERO_VALUES2;
            }
        Object[] aoValues = getMemberMemoryRateData(model, model.getData(VisualVMModel.DataType.MEMBER));

        int cTotalMemory        = (Integer) aoValues[1];
        int cTotalMemoryUsed    = (Integer) aoValues[2];
        
        return new long[]
            {
            (long) cTotalMemory * MB,
            (long) cTotalMemoryUsed * MB,
            };
        }

    /**
     * Create the descriptor for this probe.
     * @param available indicates to {@link TracerProbeDescriptor} if available
     * @return the descriptor for this probe
     */
    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(getLocalText("GRPH_cluster_memory_details"),
                getLocalText("LBL_storage_members_heap_desc"), ICON, 15, available);
        }

    /**
     * Create the {@link ProbeItemDescriptor}s for this probe.
     * @return the {@link ProbeItemDescriptor}s for this probe
     */
    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(getLocalText("GRPH_total_cluster_memory"),
                    getMonitorsString("GRPH_total_cluster_memory"), ItemValueFormatter.DEFAULT_BYTES,
                    1d, 0,  MB),
            ProbeItemDescriptor.continuousLineFillItem(getLocalText("GRPH_used_cluster_memory"),
                    getMonitorsString("GRPH_used_cluster_memory"), ItemValueFormatter.DEFAULT_BYTES,
                    1d, 0, MB)
            };
        }
    }
