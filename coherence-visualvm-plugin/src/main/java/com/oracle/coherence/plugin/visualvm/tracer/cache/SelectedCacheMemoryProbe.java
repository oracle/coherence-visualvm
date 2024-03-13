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

package com.oracle.coherence.plugin.visualvm.tracer.cache;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheStorageManagerData;

import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;

import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

/**
 * Tracer probe to return the total memory and index memory across all services.
 *
 * @author tam 2024.03.13
 */
public class SelectedCacheMemoryProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public SelectedCacheMemoryProbe(MonitoredDataResolver resolver)
        {
        super(2, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        return new long[]{
                getSelectedCacheSum(model, VisualVMModel.DataType.CACHE_DETAIL, CacheDetailData.MEMORY_BYTES),
                getSelectedCacheSum(model, VisualVMModel.DataType.CACHE_STORAGE_MANAGER, CacheStorageManagerData.INDEX_TOTAL_UNITS)};
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText("LBL_selected_cache_memory"),
                Localization.getLocalText("LBL_selected_cache_size_desc"), ICON, 10, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL1),
                    getMonitorsString(LBL1), ItemValueFormatter.DEFAULT_BYTES,
                    1d, 0, 1),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL2),
                    getMonitorsString(LBL2), ItemValueFormatter.DEFAULT_BYTES,
                    1d, 0, 1),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL1 = "LBL_total_cache";
    private static final String LBL2 = "LBL_index_memory";
    }
