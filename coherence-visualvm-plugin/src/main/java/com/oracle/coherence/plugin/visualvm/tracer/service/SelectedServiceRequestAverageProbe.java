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

import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceMemberData;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import com.oracle.coherence.plugin.visualvm.tracer.CustomFormatter;

import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;

/**
 * Tracer probe to return the maximum and average request average for the currently selected service.
 *
 * @author tam 2024.03.12
 */
public class SelectedServiceRequestAverageProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public SelectedServiceRequestAverageProbe(MonitoredDataResolver resolver)
        {
        super(2, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        Long[] aoResults = getSelectedServiceMaxAndAverage(model, ServiceMemberData.REQUEST_AVERAGE_DURATION);
        
        return new long[]{aoResults[0], aoResults[1]};
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(getLocalText("LBL_selected_service_request_avg"),
                getLocalText("LBL_selected_service_request_avg_desc"), ICON, 20, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(getLocalText(PREFIX) + " - " + getLocalText(LBL),
                    getMonitorsString(LBL), new CustomFormatter(1000, "ms"),
                    1000d, 0, 0),
            ProbeItemDescriptor.continuousLineFillItem(getLocalText(PREFIX) + " - " + getLocalText(LBL2),
                    getMonitorsString(LBL2), new CustomFormatter(1000, "ms"),
                    1000d, 0, 0),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL    = "GRPH_current_maximum";
    private static final String LBL2   = "GRPH_current_average";
    private static final String PREFIX = "LBL_request_average_duration";
    }
