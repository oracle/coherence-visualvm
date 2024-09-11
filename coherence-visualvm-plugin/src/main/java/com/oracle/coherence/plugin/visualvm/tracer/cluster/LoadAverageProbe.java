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

import java.util.List;
import java.util.Map;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;
import static com.oracle.coherence.plugin.visualvm.helper.GraphHelper.GRPH_CURRENT_AVERAGE;
import static com.oracle.coherence.plugin.visualvm.helper.GraphHelper.GRPH_MAXIMUM;
import static com.oracle.coherence.plugin.visualvm.panel.AbstractCoherencePanel.getClusterLoadAverage;


/**
 * Tracer probe to return average and max load average.
 * 
 * @author tam 2024.03.03
 */
public class LoadAverageProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Construct the probe.
     * @param resolver {@link MonitoredDataResolver}.
     */
    public LoadAverageProbe(MonitoredDataResolver resolver)
        {
        super(2, LoadAverageProbe.createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        List<Map.Entry<Object, Data>> data = model.getData(VisualVMModel.DataType.MACHINE);
        if (data == null || data.isEmpty())
            {
            return ZERO_VALUES2;
            }

        Object[] aoValues = getClusterLoadAverage(data);

       int    cCount            = (Integer) aoValues[0];
       double cTotalLoadAverage = (Double) aoValues[1];
       double cMax              = (Double) aoValues[2];

        return new long[]
            {
            (long) (cMax * 1000),
            (long) ((cTotalLoadAverage / (cCount * 1.0f)) * 1000)
            };
        }

    /**
     * Create the descriptor for this probe.
     * @param available indicates to {@link TracerProbeDescriptor} if available
     * @return the descriptor for this probe
     */
    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(getLocalText("GRPH_load_average"),
                getLocalText("LBL_load_average_desc"), ICON, 15, available);
        }

    /**
     * Create the {@link ProbeItemDescriptor}s for this probe.
     * @return the {@link ProbeItemDescriptor}s for this probe
     */
    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(getLocalText(LBL) + " - " + getLocalText(GRPH_MAXIMUM),
                    getMonitorsString(GRPH_MAXIMUM), CUSTOM_FORMATTER,
                    1000, 0, 0),
            ProbeItemDescriptor.continuousLineFillItem(getLocalText(LBL) + " - " + getLocalText(GRPH_CURRENT_AVERAGE),
                    getMonitorsString(GRPH_CURRENT_AVERAGE), CUSTOM_FORMATTER,
                    1000, 0,  0)
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL = "LBL_load_average";
    }
