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

package com.oracle.coherence.plugin.visualvm.tracer.service;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;
import java.util.Map;

/**
 * Tracer probe to return the partition information for the currently selected service.
 *
 * @author tam 2024.03.12
 */
public class SelectedServicePartitionsProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public SelectedServicePartitionsProbe(MonitoredDataResolver resolver)
        {
        super(4, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        String sSelectedService = model.getSelectedService();

        for (Map.Entry<Object, Data> entry : model.getData(VisualVMModel.DataType.SERVICE))
            {
            // get the service details for the selected service
            if (entry.getKey().equals(sSelectedService))
                {
                return new long[]{
                        (Integer) entry.getValue().getColumn(ServiceData.PARTITIONS_ENDANGERED),
                        (Integer) entry.getValue().getColumn(ServiceData.PARTITIONS_VULNERABLE),
                        (Integer) entry.getValue().getColumn(ServiceData.PARTITIONS_UNBALANCED),
                        (Integer) entry.getValue().getColumn(ServiceData.REQUESTS_PENDING)
                    };
                }
            }
        return new long[] {0L, 0L, 0L, 0L};
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText("LBL_selected_service_partitions"),
                Localization.getLocalText("LBL_selected_service_partitions_desc"), ICON, 30, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL1),
                    getMonitorsString(LBL1), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 0),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL2),
                    getMonitorsString(LBL2), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 0),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL3),
                    getMonitorsString(LBL3), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 0),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL4),
                    getMonitorsString(LBL4), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 0)
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL1  = "LBL_endangered";
    private static final String LBL2  = "LBL_vulnerable";
    private static final String LBL3  = "LBL_unbalanced";
    private static final String LBL4  = "LBL_pending";
    }
