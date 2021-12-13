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

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * A class to hold JCache configuration information.
 *
 * @author tam  2014.09.22
 * @since   12.1.3
 */
public class JCacheConfigurationData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create JCacheConfigurationData passing in the number of columns.
     */
    public JCacheConfigurationData()
        {
        super(STORE_BY_VALUE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        // only available via report
        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_JCACHE_CONFIGURATION;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new JCacheConfigurationData();
        // the identifier for this row is the configuration name and cache name
        Pair<String, String> key = new Pair<String, String>(aoColumns[2].toString(), aoColumns[3].toString());

        data.setColumn(JCacheConfigurationData.CACHE_MANAGER, key);
        data.setColumn(JCacheConfigurationData.KEY_TYPE, aoColumns[4]);
        data.setColumn(JCacheConfigurationData.VALUE_TYPE, aoColumns[5]);
        data.setColumn(JCacheConfigurationData.STATISTICS_ENABLED, aoColumns[6]);
        data.setColumn(JCacheConfigurationData.READ_THROUGH, aoColumns[7]);
        data.setColumn(JCacheConfigurationData.WRITE_THROUGH, aoColumns[8]);
        data.setColumn(JCacheConfigurationData.STORE_BY_VALUE, aoColumns[9]);

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        return null;
        }

    // ----- constants ------------------------------------------------------

    /**
     * Array index for cache manager.
     */
    public static final int CACHE_MANAGER = 0;

    /**
     * Array index for key type.
     */
    public static final int KEY_TYPE = 1;

    /**
     * Array index for value type.
     */
    public static final int VALUE_TYPE = 2;

    /**
     * Array index for statistics enabled.
     */
    public static final int STATISTICS_ENABLED = 3;

    /**
     * Array index for read through.
     */
    public static final int READ_THROUGH = 4;

    /**
     * Array index for write through.
     */
    public static final int WRITE_THROUGH = 5;

    /**
     * Array index for store by value.
     */
    public static final int STORE_BY_VALUE = 6;

    /**
     * Report for cluster data.
     */
    public static final String REPORT_JCACHE_CONFIGURATION = "reports/visualvm/jcache-configuration.xml";
    }
