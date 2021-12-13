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

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * A class to hold detailed cache data.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CacheDetailData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create CacheDetailData passing in the number of columns.
     */
    public CacheDetailData()
        {
        super(HIT_PROBABILITY + 1);
        }

    /**
     * Create CacheDetailData passing in the cache tier type.
     *
     * @param  type  the cache tier type,  which is either FRONT_TIER or BACK_TIER.
     * @param columns the number of columns
     */
    public CacheDetailData(CacheType type, int columns)
        {
        super(columns);
        m_type = type;
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> cacheMap = new TreeMap<Object, Data>();
        Data                    data;
        Pair<String, String> selectedCache = model.getSelectedCache();

        if (selectedCache != null)
            {
            try
                {
                // see if we have domainPartition key
                String[] asServiceDetails = getDomainAndService(selectedCache.getX());
                String   sDomainPartition = asServiceDetails[0];
                String   sServiceName     = asServiceDetails[1];

                Set<ObjectName> resultSet = requestSender.getCacheMembers(sServiceName, selectedCache.getY(),
                        sDomainPartition);

                for (Iterator<ObjectName> iter = resultSet.iterator(); iter.hasNext(); )
                    {
                    ObjectName objName = (ObjectName) iter.next();

                    // check which cache tier data we should extract.
                    if (objName.getKeyProperty("tier").equals(m_type.getValue()))
                        {
                        data = populateData(requestSender, objName);
                        cacheMap.put(data.getColumn(0), data);
                        }
                    }

                return new ArrayList<Map.Entry<Object, Data>>(cacheMap.entrySet());
                }
            catch (Exception e)
                {
                LOGGER.log(Level.WARNING, "Error getting cache statistics", e);

                return null;
                }
            }
        else
            {
            // no selected service, so don't query the detail data
            return null;
            }
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_CACHE_DETAIL;
        }

    /**
     * Get the data through JMX query.
     *
     * @param  requestSender  server the {@link MBeanServerConnection} to use to query
     * @param  objName        the ObjectName of the MBean.
     *
     * @return  A Data object
     *
     * @throws  Exception in case of error
     */
    public Data populateData(RequestSender requestSender, ObjectName objName)
        throws Exception
        {
        Data data = new CacheDetailData();

        AttributeList listAttr = requestSender.getAttributes(objName,
            new String[]{ ATTR_SIZE, ATTR_UNITS, ATTR_UNIT_FACTOR, ATTR_CACHE_HITS,
                          ATTR_CACHE_MISSES, ATTR_TOTAL_GETS, ATTR_TOTAL_PUTS,
                          ATTR_HIT_PROBABILITY });

        data.setColumn(CacheDetailData.NODE_ID, Integer.valueOf(objName.getKeyProperty("nodeId")));

        data.setColumn(CacheDetailData.SIZE, Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_SIZE)));
        data.setColumn(CacheDetailData.MEMORY_BYTES, Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_UNITS)) * 1L *
                                                     Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_UNIT_FACTOR)));
        data.setColumn(CacheDetailData.CACHE_HITS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_CACHE_HITS)));
        data.setColumn(CacheDetailData.CACHE_MISSES, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_CACHE_MISSES)));
        data.setColumn(CacheDetailData.TOTAL_GETS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_GETS)));
        data.setColumn(CacheDetailData.TOTAL_PUTS,  Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_PUTS)));
        data.setColumn(CacheDetailData.HIT_PROBABILITY, Double.parseDouble(getAttributeValueAsString(listAttr, ATTR_HIT_PROBABILITY)));

        return data;
        }

    /**
     * Get the Data through reports.
     *
     * @param  aoColumns  the Object array
     * @return  A Data Object
     */
    public Data populateData(Object[] aoColumns)
        {
        Data data = new CacheDetailData();

        data.setColumn(CacheDetailData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(CacheDetailData.SIZE, Integer.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(CacheDetailData.MEMORY_BYTES, Integer.valueOf(getNumberValue(aoColumns[6].toString())));
        data.setColumn(CacheDetailData.TOTAL_GETS, Long.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(CacheDetailData.TOTAL_PUTS, Long.valueOf(getNumberValue(aoColumns[8].toString())));
        data.setColumn(CacheDetailData.CACHE_HITS, Long.valueOf(getNumberValue(aoColumns[9].toString())));
        data.setColumn(CacheDetailData.CACHE_MISSES, Integer.valueOf(getNumberValue(aoColumns[10].toString())));
        data.setColumn(CacheDetailData.HIT_PROBABILITY, Float.valueOf(aoColumns[11].toString()));

        return data;
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // the report XML contains the following tokens that require substitution:
        // %SERVICE_NAME%
        // %CACHE_NAME%
        // %TIER_TYPE%

        Pair<String, String> selectedCache = model.getSelectedCache();

        // see if we have domainPartition key
        String sServiceName     = null;
        String sDomainPartition = null;

        if (selectedCache != null)
            {
            String[] asServiceDetails = getDomainAndService(selectedCache.getX());
            sServiceName              = asServiceDetails[1];
            sDomainPartition          = asServiceDetails[0];
            }

        return sServiceName == null ? sReporterXML :
                sReporterXML.replaceAll("%SERVICE_NAME%", sServiceName +
                                        (sDomainPartition != null ? ",domainPartition=" + sDomainPartition : "") )
                            .replaceAll("%CACHE_NAME%", selectedCache.getY())
                            .replaceAll("%TIER_TYPE%", m_type.getValue());
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender) throws Exception
        {
        Pair<String, String> selectedCache = model.getSelectedCache();

        if (selectedCache == null)
            {
            return null;
            }

        return getAggregatedDataFromHttpQueryingInternal(requestSender, selectedCache);
        }

    /**
     * Internal implemented on getAggregatedDataFromHttpQuerying
     *
     * @param requestSender  the request sender to use
     * @param selectedCache  the selected cache
     *
     * @return the result using REST api
     *
     * @throws Exception in case of error
     */
    public SortedMap<Object, Data> getAggregatedDataFromHttpQueryingInternal(
            HttpRequestSender requestSender, Pair<String,String> selectedCache) throws Exception
        {
        String[] asServiceDetails       = getDomainAndService(selectedCache.getX());
        String sServiceName             = asServiceDetails[1];
        String sDomainPartition         = asServiceDetails[0];
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();

        JsonNode rootNode         = requestSender.getDataForCacheMembers(sServiceName, selectedCache.getY(), sDomainPartition);
        JsonNode nodeCacheMembers = rootNode.get("items");

        if (nodeCacheMembers != null && nodeCacheMembers.isArray())
            {
            for (int i = 0; i < nodeCacheMembers.size(); i++)
                {
                JsonNode nodeCacheMember = nodeCacheMembers.get(i);

                if (nodeCacheMember.get("tier").asText().equals(m_type.getValue()))
                    {
                    Data data = new CacheDetailData();

                    data.setColumn(CacheDetailData.NODE_ID,
                            Integer.valueOf(nodeCacheMember.get("nodeId").asText()));
                    data.setColumn(CacheDetailData.SIZE,
                            Integer.valueOf(nodeCacheMember.get("size").asText()));
                    data.setColumn(CacheDetailData.MEMORY_BYTES,
                            nodeCacheMember.get("units").intValue() * nodeCacheMember.get("unitFactor").intValue());
                    data.setColumn(CacheDetailData.TOTAL_GETS,
                            Long.valueOf(nodeCacheMember.get("totalGets").asText()));
                    data.setColumn(CacheDetailData.TOTAL_PUTS,
                            Long.valueOf(nodeCacheMember.get("totalPuts").asText()));
                    data.setColumn(CacheDetailData.CACHE_HITS,
                            Long.valueOf(nodeCacheMember.get("cacheHits").asText()));
                    data.setColumn(CacheDetailData.CACHE_MISSES,
                            Integer.valueOf(nodeCacheMember.get("cacheMisses").asText()));
                    data.setColumn(CacheDetailData.HIT_PROBABILITY,
                            Float.valueOf(nodeCacheMember.get("hitProbability").floatValue()));

                    mapData.put(data.getColumn(0), data);
                    }
                }
            }

        return mapData;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = null;

        if (model.getSelectedCache() != null )
            {
            data = populateData(aoColumns);
            }

        return data;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7989960126715125202L;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for size.
     */
    public static final int SIZE = 1;

    /**
     * Array index for memory.
     */
    public static final int MEMORY_BYTES = 2;

    /**
     * Array index for total gets.
     */
    public static final int TOTAL_GETS = 3;

    /**
     * Array index for total puts.
     */
    public static final int TOTAL_PUTS = 4;

    /**
     * Array index for cache hits.
     */
    public static final int CACHE_HITS = 5;

    /**
     * Array index for cache misses.
     */
    public static final int CACHE_MISSES = 6;

    /**
     * Array index for hit probability.
     */
    public static final int HIT_PROBABILITY = 7;

    /**
     * Report for cache details data;
     */
    public static final String REPORT_CACHE_DETAIL = "reports/visualvm/cache-detail-stats.xml";

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheDetailData.class.getName());

    /**
     * The cache tier type.
     */
    private CacheType m_type = CacheType.BACK_TIER;

    /**
     * Enum for cache tier types.
     */
    public enum CacheType
        {
        BACK_TIER("back"),
        FRONT_TIER("front");

        private CacheType(String value)
            {
            m_sValue = value;
            }

        /**
         * Get the value of this cache tier type.
         *
         * @return a String value associates with the CacheType
         */
        public String getValue()
            {
            return m_sValue;
            }

        /**
         * The value associates with the CacheType.
         */
        private String m_sValue;
        }

    /**
     * JMX attribute name for Size.
     */
    protected static final String ATTR_SIZE = "Size";

    /**
     * JMX attribute name for Units.
     */
    protected static final String ATTR_UNITS = "Units";

    /**
     * JMX attribute name for Unit Factor.
     */
    protected static final String ATTR_UNIT_FACTOR = "UnitFactor";

    /**
     * JMX attribute name for Cache Hits.
     */
    protected static final String ATTR_CACHE_HITS = "CacheHits";

    /**
     * JMX attribute name for Cache Misses.
     */
    protected static final String ATTR_CACHE_MISSES = "CacheMisses";

    /**
     * JMX attribute name for Total Gets.
     */
    protected static final String ATTR_TOTAL_GETS = "TotalGets";

    /**
     * JMX attribute name for Total Puts.
     */
    protected static final String ATTR_TOTAL_PUTS = "TotalPuts";

    /**
     * JMX attribute name for Hit Probability.
     */
    protected static final String ATTR_HIT_PROBABILITY = "HitProbability";
    }
