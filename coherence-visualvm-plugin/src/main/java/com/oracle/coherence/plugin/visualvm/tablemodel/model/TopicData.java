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

import java.util.List;
import java.util.Map;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;


/**
 * A class to hold basic topic data.
 *
 * @author tam  2020.02.08
 * @since  1.0.1
 */
public class TopicData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create TopicData passing in the number of columns.
     *
     */
    public TopicData()
        {
        super(SUBSCRIBER_RECEIVES + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender sender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Data                    data;

        return null;
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_TOPICS;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new TopicData();

        // the identifier for this row is the service name and cache name
        Pair<String, String> key = new Pair<>(aoColumns[2].toString(),
                aoColumns[3].toString().replaceAll("\\$topic\\$", ""));

        data.setColumn(TopicData.TOPIC_NAME, key);
        data.setColumn(TopicData.SIZE, Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(TopicData.MEMORY_USAGE_BYTES, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(TopicData.MEMORY_USAGE_MB, Integer.valueOf(getNumberValue(aoColumns[6].toString())) / 1024 / 1024);

        if (aoColumns[7] != null)
            {
            data.setColumn(TopicData.AVG_OBJECT_SIZE, Integer.valueOf(getNumberValue(aoColumns[7].toString())));
            }
        else
            {
            data.setColumn(TopicData.AVG_OBJECT_SIZE, 0);
            }

        data.setColumn(TopicData.PUBLISHER_SENDS, Long.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(TopicData.SUBSCRIBER_RECEIVES, Long.valueOf(getNumberValue(aoColumns[9].toString())));

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        final SortedMap<Object, Data> mapData    = new TreeMap<>();
        CacheDetailData               detailData = new CacheDetailData();

        // When using HTTP we just rely on the already collected cache data from the model
        // except we also need total puts/gets across all caches
        List<Map.Entry<Object, Data>> cacheData = model.getData(VisualVMModel.DataType.CACHE);
        for (Map.Entry<Object, Data> entry : cacheData)
            {
            long cPublisherSends     = 0L;
            long cSubscriberReceives = 0L;

            Pair<String, String> cache = (Pair<String, String>) entry.getKey();

            if (cache.getY().contains("$topic$"))
                {
                // found a topic cache
                Data data = new TopicData();
                Pair<String, String> key = new Pair<>(cache.getX(), cache.getY().replaceAll("\\$topic\\$", ""));
                data.setColumn(TopicData.TOPIC_NAME, key);
                data.setColumn(TopicData.AVG_OBJECT_SIZE, 0);
                data.setColumn(TopicData.SIZE, entry.getValue().getColumn(CacheData.SIZE));
                data.setColumn(TopicData.MEMORY_USAGE_BYTES, entry.getValue().getColumn(CacheData.MEMORY_USAGE_BYTES));
                data.setColumn(TopicData.MEMORY_USAGE_MB, entry.getValue().getColumn(CacheData.MEMORY_USAGE_MB));
                Object avgObjectSize = entry.getValue().getColumn(CacheData.AVG_OBJECT_SIZE);
                data.setColumn(TopicData.AVG_OBJECT_SIZE, avgObjectSize == null ? 0 : getNumberValue(avgObjectSize.toString()));

                // get the cache detail data
                SortedMap<Object, Data> cacheDetails = detailData.getAggregatedDataFromHttpQueryingInternal(requestSender, cache);

                for (Data detail : cacheDetails.values())
                    {
                    cPublisherSends += (Long) detail.getColumn(CacheDetailData.TOTAL_PUTS);
                    cSubscriberReceives += (Long) detail.getColumn(CacheDetailData.TOTAL_GETS);
                    }

                data.setColumn(TopicData.PUBLISHER_SENDS, cPublisherSends);
                data.setColumn(TopicData.SUBSCRIBER_RECEIVES, cSubscriberReceives);
                mapData.put(cache, data);
                }
            }

        return mapData;
        }

    @Override
    protected SortedMap<Object, Data> postProcessReporterData(SortedMap<Object, Data> mapData, VisualVMModel model)
        {
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 6427775621469258645L;

    /**
     * Report for topics data.
     */
    public static final String REPORT_TOPICS = "reports/visualvm/topics-size-stats.xml";

    /**
     * Array index for topic name.
     */
    public static final int TOPIC_NAME = 0;

    /**
     * Array index for topic size.
     */
    public static final int SIZE = 1;

    /**
     * Array index for memory usage in bytes.
     */
    public static final int MEMORY_USAGE_BYTES = 2;

    /**
     * Array index for memory usage in MB.
     */
    public static final int MEMORY_USAGE_MB = 3;

    /**
     * Array index for average object size.
     */
    public static final int AVG_OBJECT_SIZE = 4;

    /**
     * Array index for publisher sends.
     */
    public static final int PUBLISHER_SENDS = 5;

    /**
     * Array index for subscriber receives.
     */
    public static final int SUBSCRIBER_RECEIVES = 6;
    
    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(TopicData.class.getName());

    /**
     * Attribute for "MemoryUnits".
     */
    private static final String MEMORY_UNITS = "MemoryUnits";
    }
