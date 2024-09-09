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

package com.oracle.coherence.plugin.visualvm.tracer.federation;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationData;
import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;
import com.oracle.coherence.plugin.visualvm.tracer.CustomFormatter;
import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

/**
 * Tracer probe to return the current total messages sent per second across all federated services.
 *
 * @author tam 2024.03.03
 */
public class MsgsSentSecProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Construct the probe.
     * @param resolver {@link MonitoredDataResolver}.
     */
    public MsgsSentSecProbe(MonitoredDataResolver resolver)
        {
        super(1, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        return getSingValueSum(model, VisualVMModel.DataType.FEDERATION_DESTINATION, FederationData.Column.TOTAL_MSGS_SENT.ordinal(), ZERO_VALUES1);
        }

    /**
     * Create the descriptor for this probe.
     * @param available indicates to {@link TracerProbeDescriptor} if available
     * @return the descriptor for this probe
     */
    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText(LBL),
                Localization.getLocalText("LBL_federation_msgs_sent_desc"), ICON, 15, available);
        }

    /**
     * Create the {@link ProbeItemDescriptor}s for this probe.
     * @return the {@link ProbeItemDescriptor}s for this probe
     */
    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL),
                    getMonitorsString(LBL), new CustomFormatter(1, "msg/ s"),
                    1d, 0, 0),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL = "LBL_total_msgs_sent_sec";
    }
