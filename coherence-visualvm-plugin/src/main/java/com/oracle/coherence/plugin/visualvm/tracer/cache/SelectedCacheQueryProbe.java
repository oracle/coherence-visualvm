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
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheStorageManagerData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceMemberData;
import com.oracle.coherence.plugin.visualvm.tracer.AbstractCoherenceMonitorProbe;
import com.oracle.coherence.plugin.visualvm.tracer.CustomFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbeDescriptor;

/**
 * Tracer probe to return the query information for the currently selected cache.
 *
 * @author tam 2024.03.12
 */
public class SelectedCacheQueryProbe
        extends AbstractCoherenceMonitorProbe
    {
    // ----- constructors ---------------------------------------------------

    public SelectedCacheQueryProbe(MonitoredDataResolver resolver)
        {
        super(3, createItemDescriptors(), resolver);
        }

    // ---- TracerProbe methods ---------------------------------------------

    @Override
    public long[] getValues(VisualVMModel model)
        {
        return new long[]{
                getSelectedCacheMax(model, VisualVMModel.DataType.CACHE_STORAGE_MANAGER, CacheStorageManagerData.MAX_QUERY_DURATION),
                getSelectedCacheAverage(model, VisualVMModel.DataType.CACHE_STORAGE_MANAGER, CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG),
                getSelectedCacheAverage(model, VisualVMModel.DataType.CACHE_STORAGE_MANAGER, CacheStorageManagerData.OPTIMIZED_QUERY_AVG),
            };
        }

    public static TracerProbeDescriptor createDescriptor(boolean available)
        {
        return new TracerProbeDescriptor(Localization.getLocalText("LBL_selected_cache_query"),
                Localization.getLocalText("LBL_selected_cache_query_desc"), ICON, 20, available);
        }

    private static ProbeItemDescriptor[] createItemDescriptors()
        {
        return new ProbeItemDescriptor[]
            {
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL),
                    getMonitorsString(LBL), new CustomFormatter(1000, "ms"),
                    1000d, 0, 1),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL2),
                    getMonitorsString(LBL2), new CustomFormatter(1000, "ms"),
                    1000d, 0, 1),
            ProbeItemDescriptor.continuousLineFillItem(Localization.getLocalText(LBL3),
                    getMonitorsString(LBL3), new CustomFormatter(1000, "ms"),
                    1000d, 0, 1),
            };
        }

    // ----- constants ------------------------------------------------------

    private static final String LBL  = "LBL_max_query_millis";
    private static final String LBL2 = "LBL_non_opt_avge";
    private static final String LBL3 = "LBL_opt_avge";
    }
