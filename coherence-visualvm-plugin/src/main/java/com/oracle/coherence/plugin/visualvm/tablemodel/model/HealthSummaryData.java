/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.tablemodel.model;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

/**
 * A class to hold summary health data.
 *
 * @author tam  2022.06.22
 * @since  1.4.0
 */
public class HealthSummaryData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create PersistenceData passing in the number of columns.
     */
    public HealthSummaryData()
        {
        super(CLASS_NAME + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_HEALTH;    // see comment below
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        return null;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        return new TreeMap<>();
        }

    private static final long serialVersionUID = 7769559573242105947L;

    /**
     * Array index for health name.
     */
    public static final int HEALTH_NAME = 0;

    /**
     * Array index for members.
     */
    public static final int MEMBERS = 1;

    /**
     * Array index for started.
     */
    public static final int STARTED = 2;

    /**
     * Array index for live.
     */
    public static final int LIVE = 3;

    /**
     * Array index for ready.
     */
    public static final int READY = 4;

    /**
     * Array index for safe
     */
    public static final int SAFE = 5;

    /**
     * Array index for description.
     */
    public static final int CLASS_NAME = 6;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(HealthSummaryData.class.getName());

    /**
     * Report for cluster data.
     */
    public static final String REPORT_HEALTH = "reports/visualvm/health-stats.xml";
    }
