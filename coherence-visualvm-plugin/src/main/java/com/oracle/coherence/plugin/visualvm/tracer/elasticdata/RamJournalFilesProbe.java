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

package com.oracle.coherence.plugin.visualvm.tracer.elasticdata;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.RamJournalData;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

/**
 * Tracer probe to return the ram journal files used.
 *
 * @author tam 2024.03.07
 */
public class RamJournalFilesProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public RamJournalFilesProbe(MonitoredDataResolver resolver)
        {
        super(2, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        long nRamJournalFileCount = getSingValueSum(model, VisualVMModel.DataType.RAMJOURNAL, RamJournalData.FILE_COUNT, ZERO_VALUES1)[0];
        long nRamJournaMaxFiles   = getSingValueSum(model, VisualVMModel.DataType.RAMJOURNAL, RamJournalData.MAX_FILES, ZERO_VALUES1)[0];
        return new long[] {nRamJournalFileCount, nRamJournaMaxFiles};
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText("LBL_ram_journal_files"),
                Localization.getLocalText("LBL_ramjournal_files_desc"), ICON, 15, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL1),
                    getMonitorsString(LBL1), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 1),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL2),
                    getMonitorsString(LBL2), ItemValueFormatter.DEFAULT_DECIMAL,
                    1d, 0, 1),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL1 = "LBL_journal_files_used";
    private static final String LBL2 = "LBL_max_journal_files";
    }
