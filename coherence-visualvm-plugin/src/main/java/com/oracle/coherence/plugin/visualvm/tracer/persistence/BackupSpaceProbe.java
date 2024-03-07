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

package com.oracle.coherence.plugin.visualvm.tracer.persistence;

import static com.oracle.coherence.plugin.visualvm.helper.GraphHelper.MB;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

/**
 * Tracer probe to return the total backup space used across all persistence services.
 *
 * @author tam 2024.03.07
 */
public class BackupSpaceProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public BackupSpaceProbe(MonitoredDataResolver resolver)
        {
        super(1, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        long[] anValue = getSingValueSum(model, VisualVMModel.DataType.PERSISTENCE, PersistenceData.TOTAL_BACKUP_SPACE_USED_MB, ZERO_VALUES1);
        anValue[0] = anValue[0] * MB;
        return anValue;
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText(LBL),
                Localization.getLocalText("LBL_backup_space_desc"), ICON, 20, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL),
                    getMonitorsString(LBL), ItemValueFormatter.DEFAULT_BYTES,
                    1d, 0, 1),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL = "LBL_backup_space";
    }
