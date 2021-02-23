/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * A class to hold JCache statistics information.
 *
 * @author tam  2014.09.22
 * @since   12.1.3
 */
public class JCacheStatisticsData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create JCacheStatisticsData passing in the number of columns.
     */
    public JCacheStatisticsData()
        {
        super(CACHE_MISS_PERCENTAGE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
     * {@inheritDoc}
     */
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        // only available via report
        return null;
        }

    /**
     * {@inheritDoc}
     */
    public String getReporterReport()
        {
        return REPORT_JCACHE_CONFIGURATION;
        }

    /**
     * {@inheritDoc}
     */
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new JCacheStatisticsData();

        // the identifier for this row is the configuration name and cache name
        Pair<String, String> key = new Pair<String, String>(aoColumns[2].toString(), aoColumns[3].toString());

        data.setColumn(JCacheStatisticsData.CACHE_MANAGER, key);
        data.setColumn(JCacheStatisticsData.CACHE_GETS, Long.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(JCacheStatisticsData.CACHE_PUTS, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(JCacheStatisticsData.CACHE_REMOVALS, Long.valueOf(getNumberValue(aoColumns[6].toString())));
        data.setColumn(JCacheStatisticsData.CACHE_HITS, Long.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(JCacheStatisticsData.CACHE_MISSES, Long.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(JCacheStatisticsData.CACHE_EVICTIONS, Long.valueOf(getNumberValue(aoColumns[9].toString())));
        data.setColumn(JCacheStatisticsData.AVERAGE_GET_TIME, Float.valueOf(aoColumns[10].toString()));
        data.setColumn(JCacheStatisticsData.AVERAGE_PUT_TIME, Float.valueOf(aoColumns[11].toString()));
        data.setColumn(JCacheStatisticsData.AVERAGE_REMOVE_TIME, Float.valueOf(aoColumns[12].toString()));
        data.setColumn(JCacheStatisticsData.CACHE_HIT_PERCENTAGE, Float.valueOf(aoColumns[13].toString()));
        data.setColumn(JCacheStatisticsData.CACHE_MISS_PERCENTAGE, Float.valueOf(aoColumns[14].toString()));

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender) throws Exception
        {
        return null;
        }

    // ----- constants ------------------------------------------------------

    /**
     * Array index for cache manager.
     */
    public static final int CACHE_MANAGER = 0;

    /**
     * Array index for cache gets.
     */
    public static final int CACHE_GETS = 1;

    /**
     * Array index for cache puts.
     */
    public static final int CACHE_PUTS = 2;

    /**
     * Array index for cache removes.
     */
    public static final int CACHE_REMOVALS = 3;

    /**
     * Array index for cache hits.
     */
    public static final int CACHE_HITS = 4;

    /**
     * Array index for cache misses.
     */
    public static final int CACHE_MISSES = 5;

    /**
     * Array index for cache evictions.
     */
    public static final int CACHE_EVICTIONS = 6;

    /**
     * Array index for average get time.
     */
    public static final int AVERAGE_GET_TIME = 7;

    /**
     * Array index for average put time.
     */
    public static final int AVERAGE_PUT_TIME = 8;

    /**
     * Array index for average remove time.
     */
    public static final int AVERAGE_REMOVE_TIME = 9;

    /**
     * Array index for cache hit percentage.
     */
    public static final int CACHE_HIT_PERCENTAGE = 10;

    /**
     * Array index for cache miss percentage.
     */
    public static final int CACHE_MISS_PERCENTAGE = 11;

    /**
     * Report for cluster data.
     */
    public static final String REPORT_JCACHE_CONFIGURATION = "reports/visualvm/jcache-statistics.xml";
    }
