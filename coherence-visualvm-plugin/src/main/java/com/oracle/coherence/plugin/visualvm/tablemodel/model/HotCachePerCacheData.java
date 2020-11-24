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
import com.oracle.coherence.plugin.visualvm.helper.JMXUtils;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import javax.management.AttributeList;
import javax.management.ObjectName;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import java.util.*;

/**
 * A class to hold hotcachepercache data.
 *
 * @author nagaraju  2017.02.08
 *
 */
public class HotCachePerCacheData
        extends AbstractData
    {
    //------ constructors ----------------------------------------------------------------------------------------
    /**
     * Create HotCachePerCacheData passing in the number of columns.
     */
    public HotCachePerCacheData()
        {
        super(5);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        String sMember = model.getSelectedHotCacheMember();
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Data                    data;
        try
            {
            if (sMember == null)
                {
                return new ArrayList<>(mapData.entrySet());
                }

            Set<ObjectName> hotCacheNamesSet = requestSender.getHotCachePerCacheAdapters(sMember);

            for (Iterator<ObjectName> nodIter = hotCacheNamesSet.iterator(); nodIter.hasNext(); )
                {
                ObjectName hotCacheNameObjName = (ObjectName) nodIter.next();

                AttributeList listAttr = requestSender.getAttributes(hotCacheNameObjName,
                        new String[]{ATTR_PERCACHESTATISTICS});

                TabularData tabularData = ((TabularData) JMXUtils.getAttributeValue(listAttr,ATTR_PERCACHESTATISTICS));
                SortedMap<String,Object[]> tabledata = extractdata(tabularData);

                for (Map.Entry<String, Object[]> entry : tabledata.entrySet())
                    {
                    data = new HotCachePerCacheData();
                    data.setColumn(HotCachePerCacheData.CacheOperation, entry.getKey());
                    data.setColumn(HotCachePerCacheData.Count, ((Number)(entry.getValue())[0]).longValue());
                    data.setColumn(HotCachePerCacheData.Max, ((Number)(entry.getValue())[1]).longValue());
                    data.setColumn(HotCachePerCacheData.Mean, ((Number)(entry.getValue())[2]).doubleValue());
                    data.setColumn(HotCachePerCacheData.Min, ((Number)(entry.getValue())[3]).longValue());

                    mapData.put(entry.getKey(), data);
                    }

                }
            return new ArrayList<>(mapData.entrySet());
            }
        catch(Exception e)
            {
            return null;
            }
        }

    /**
     * Extracts the data from perCacheStatistics(TabularData) and puts it into a map with
     * key as "FullyQualifiedCacheName/OperationType" and values as {count, overage, max and min}
     *
     * @param tabularData {@link TabularData} to process
     * @return Map
     */
    public SortedMap<String,Object[]> extractdata(TabularData tabularData)
        {
        TabularData innertabulardata;
        CompositeData innercompositedata2;

        SortedMap<String, Object[]> map = new TreeMap<>();
        if (tabularData != null)
            {
            Collection<CompositeData> values = (Collection<CompositeData>) tabularData.values();

            for (CompositeData compositeData : values)
                {
                Set<String> keys = compositeData.getCompositeType().keySet();
                String[] skey = new String[2];
                for (String key : keys)
                    {

                    if((compositeData.get(key)).getClass()==String.class)
                        {
                        skey[0] = (String) compositeData.get(key);
                        }
                    else if((compositeData.get(key)).getClass()==TabularDataSupport.class)
                        {
                        innertabulardata = (TabularData)compositeData.get(key);
                        if(innertabulardata!=null)
                            {
                            Collection<CompositeData> innervalues = (Collection<CompositeData>) innertabulardata.values();
                            for(CompositeData innercompositedata : innervalues)
                                {
                                Set<String> innerkeys = innercompositedata.getCompositeType().keySet();
                                for(String innerkey : innerkeys)
                                    {
                                    if(innercompositedata.get(innerkey).getClass()==String.class)
                                        {
                                        skey[1] = (String) innercompositedata.get(innerkey);
                                        }
                                    else if(innercompositedata.get(innerkey).getClass()==CompositeDataSupport.class)
                                        {
                                        Object[] stats = new Object[4];
                                        innercompositedata2 = (CompositeData) innercompositedata.get(innerkey);

                                        stats[0] = innercompositedata2.get("count");
                                        stats[1] = innercompositedata2.get("max");
                                        stats[2] = innercompositedata2.get("average");
                                        stats[3] = innercompositedata2.get("min");

                                        map.put(new String(skey[0]+"/"+skey[1]),stats);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return map;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReporterReport()
        {
        return null;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data   data         = null;
        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender) throws Exception
        {
        // no reports being used, hence using default functionality provided in getJMXData
        return null;
        }

    /**
     * Array index for "Cache/Operation".
     */
    public static final int CacheOperation = 0;

    /**
     * Array index for Count.
     */
    public static final int Count = 1;

    /**
     * Array index for Max.
     */
    public static final int Max = 2;

    /**
     * Array index for Mean.
     */
    public static final int Mean = 3;

    /**
     * Array index for Min.
     */
    public static final int Min = 4;

    /**
     * JMX attribute name for PerCacheStatistics.
     */
    protected static final String ATTR_PERCACHESTATISTICS = "PerCacheStatistics";
    }
