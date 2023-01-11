/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

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
import javax.management.ObjectName;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.*;

/**
 * A class to hold detailed cache storage data.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CacheStorageManagerData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create CacheStorageManagerData passing in the number of columns.
     */
    public CacheStorageManagerData()
        {
        super(INDEXING_TOTAL_MILLIS + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
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

                Set<ObjectName> resultSet = requestSender.getCacheStorageMembers(sServiceName, selectedCache.getY(),
                        sDomainPartition);

                for (Iterator<ObjectName> iter = resultSet.iterator(); iter.hasNext(); )
                    {
                    ObjectName objName = iter.next();
                    String     sNodeId = objName.getKeyProperty("nodeId");

                    data = new CacheStorageManagerData();

                    AttributeList listAttr = requestSender.getAttributes(objName,
                        new String[]{ ATTR_LOCKS_GRANTED, ATTR_LOCKS_PENDING, ATTR_LISTENER_KEY_COUNT, ATTR_LISTENER_FILTER_COUNT });

                    String sLocksGranted    = getAttributeValueAsString(listAttr, ATTR_LOCKS_GRANTED);
                    String sLocksPending    = getAttributeValueAsString(listAttr, ATTR_LOCKS_PENDING);
                    String sKeyListeners    = getAttributeValueAsString(listAttr, ATTR_LISTENER_KEY_COUNT);
                    String sFilterListeners = getAttributeValueAsString(listAttr, ATTR_LISTENER_FILTER_COUNT);

                    data.setColumn(CacheStorageManagerData.NODE_ID, Integer.valueOf(sNodeId));
                    data.setColumn(CacheStorageManagerData.LOCKS_GRANTED,
                            Integer.parseInt(sLocksGranted == null ? "0" : sLocksGranted));
                    // locks pending may be returns as null over REST
                    data.setColumn(CacheStorageManagerData.LOCKS_PENDING,
                            Integer.parseInt(sLocksPending == null ? "0" : sLocksPending));
                    data.setColumn(CacheStorageManagerData.LISTENER_KEY_COUNT,
                            Long.parseLong(sKeyListeners == null ? "0" : sKeyListeners));
                    data.setColumn(CacheStorageManagerData.LISTENER_FILTER_COUNT,
                            Long.parseLong(sFilterListeners == null ? "0" : sFilterListeners));
                    try {
                        data.setColumn(CacheStorageManagerData.MAX_QUERY_DURATION,
                                   Long.parseLong(requestSender.getAttribute(objName, "MaxQueryDurationMillis")));
                        data.setColumn(CacheStorageManagerData.MAX_QUERY_DESCRIPTION,
                                   (String) requestSender.getAttribute(objName, "MaxQueryDescription"));
                        data.setColumn(CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG,
                                   Long.parseLong(requestSender.getAttribute(objName, "NonOptimizedQueryAverageMillis")));
                        data.setColumn(CacheStorageManagerData.OPTIMIZED_QUERY_AVG,
                                Long.parseLong(requestSender.getAttribute(objName, "OptimizedQueryAverageMillis")));
                        data.setColumn(CacheStorageManagerData.INDEX_TOTAL_UNITS,
                                Long.parseLong(requestSender.getAttribute(objName, "IndexTotalUnits")));
                        data.setColumn(CacheStorageManagerData.INDEXING_TOTAL_MILLIS,
                                Long.parseLong(requestSender.getAttribute(objName, "IndexingTotalMillis")));
                        }
                    catch (Exception eIgnore)
                       {
                       // Default values when we have potential NPE as this can happen for a front-tier via REST
                       data.setColumn(CacheStorageManagerData.MAX_QUERY_DURATION,0L);
                       data.setColumn(CacheStorageManagerData.MAX_QUERY_DESCRIPTION,"null");
                       data.setColumn(CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG,0L);
                       data.setColumn(CacheStorageManagerData.OPTIMIZED_QUERY_AVG,0L);
                       data.setColumn(CacheStorageManagerData.INDEX_TOTAL_UNITS,0L);
                       data.setColumn(CacheStorageManagerData.INDEXING_TOTAL_MILLIS,0L);
                       }

                    mapData.put(data.getColumn(0), data);
                    }

                return new ArrayList<Map.Entry<Object, Data>>(mapData.entrySet());
                }
            catch (Exception e)
                {
                LOGGER.log(Level.WARNING, "Error getting cache storage managed statistics", e);

                return null;
                }
            }
        else
            {
            // no selected service, so don't query the storage manager data
            return null;
            }
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // the report XML contains the following tokens that require substitution:
        // %SERVICE_NAME%
        // %CACHE_NAME%

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
                                 .replaceAll("%CACHE_NAME%",   selectedCache.getY());
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_STORAGE_MANAGER;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new CacheStorageManagerData();

        data.setColumn(CacheStorageManagerData.NODE_ID,
                       Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(CacheStorageManagerData.LOCKS_GRANTED,
                       Integer.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(CacheStorageManagerData.LOCKS_PENDING,
                       Integer.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(CacheStorageManagerData.LISTENER_KEY_COUNT,
                       Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(CacheStorageManagerData.LISTENER_FILTER_COUNT,
                       Long.valueOf(getNumberValue(aoColumns[6].toString())));
        data.setColumn(CacheStorageManagerData.MAX_QUERY_DURATION,
                       Long.valueOf(getNumberValue(aoColumns[7].toString())));
        data.setColumn(CacheStorageManagerData.MAX_QUERY_DESCRIPTION,
                       new String(aoColumns[7] == null ? "" : aoColumns[8].toString()));
        data.setColumn(CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG,
                       Long.valueOf(getNumberValue(aoColumns[9].toString())));
        data.setColumn(CacheStorageManagerData.OPTIMIZED_QUERY_AVG,
                       Long.valueOf(getNumberValue(aoColumns[10].toString())));

        try
            {
            data.setColumn(CacheStorageManagerData.INDEX_TOTAL_UNITS,
                       Long.valueOf(getNumberValue(aoColumns[11].toString())));
            data.setColumn(CacheStorageManagerData.INDEXING_TOTAL_MILLIS,
                       Long.valueOf(getNumberValue(aoColumns[12].toString())));
            }
        catch (Exception e)
            {
            // if we connect to a coherence version 12.1.3.X then this
            // attribute was not included in MBean and reports, so we must
            // protect against NPE
            }

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        Pair<String, String> selectedCache = model.getSelectedCache();

        if (selectedCache == null)
            {
            return null;
            }

        // see if we have domainPartition key
        String sServiceName     = null;
        String sDomainPartition = null;

        if (selectedCache != null)
            {
            String[] asServiceDetails = getDomainAndService(selectedCache.getX());
            sServiceName              = asServiceDetails[1];
            sDomainPartition          = asServiceDetails[0];
            }

        JsonNode rootNode = requestSender.getDataForStorageManagerMembers(sServiceName, sDomainPartition, selectedCache.getY());

        SortedMap<Object, Data> mapData        = new TreeMap<Object, Data>();
        JsonNode                nodeCacheItems = rootNode.get("items");

        if (nodeCacheItems != null && nodeCacheItems.isArray())
            {
            for (int i = 0; i < nodeCacheItems.size(); i++)
                {
                JsonNode nodeCacheStorage = nodeCacheItems.get(i);
                // Workaround Bug 33107052
                if (nodeCacheStorage.size() < 2)
                    {
                    continue;
                    }
                Data data = new CacheStorageManagerData();

                JsonNode locksGranted = nodeCacheStorage.get("locksGranted");
                if (locksGranted == null)
                    {
                    // Connecting to version without Bug 32134281 fix so force less efficient way
                    List<Map.Entry<Object, Data>> jmxData = getJMXData(requestSender, model);
                    if (jmxData == null || jmxData.size() == 0)
                        {
                        return null;
                        }
                    else
                        {
                        jmxData.forEach(e -> mapData.put(e.getKey(), e.getValue()));
                        }
                    return mapData;
                    }

                data.setColumn(CacheStorageManagerData.NODE_ID, nodeCacheStorage.get("nodeId").asInt());
                data.setColumn(CacheStorageManagerData.LOCKS_GRANTED, locksGranted.asInt());
                data.setColumn(CacheStorageManagerData.LOCKS_PENDING, nodeCacheStorage.get("locksPending").asInt());
                data.setColumn(CacheStorageManagerData.LISTENER_KEY_COUNT,nodeCacheStorage.get("listenerKeyCount").asInt());
                data.setColumn(CacheStorageManagerData.LISTENER_FILTER_COUNT,nodeCacheStorage.get("listenerFilterCount").asInt());
                data.setColumn(CacheStorageManagerData.MAX_QUERY_DURATION, nodeCacheStorage.get("maxQueryDurationMillis").asLong());
                data.setColumn(CacheStorageManagerData.MAX_QUERY_DESCRIPTION, nodeCacheStorage.get("maxQueryDescription").asText());
                data.setColumn(CacheStorageManagerData.NON_OPTIMIZED_QUERY_AVG, nodeCacheStorage.get("nonOptimizedQueryAverageMillis").asLong());
                data.setColumn(CacheStorageManagerData.OPTIMIZED_QUERY_AVG, nodeCacheStorage.get("optimizedQueryAverageMillis").asLong());

                try
                    {
                    data.setColumn(CacheStorageManagerData.INDEX_TOTAL_UNITS,nodeCacheStorage.get("indexTotalUnits").asLong());
                    data.setColumn(CacheStorageManagerData.INDEXING_TOTAL_MILLIS,nodeCacheStorage.get("indexingTotalMillis").asLong());
                    }
                catch (Exception ignore)
                    {
                    // ignore as attributes not available in all versions
                    }

                mapData.put(data.getColumn(0), data);
                }
            }
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -7989560126715725202L;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for locks granted.
     */
    public static final int LOCKS_GRANTED = 1;

    /**
     * Array index for locks pending.
     */
    public static final int LOCKS_PENDING = 2;

    /**
     * Array index for listener key count.
     */
    public static final int LISTENER_KEY_COUNT = 3;

    /**
     * Array index for listener filter count.
     */
    public static final int LISTENER_FILTER_COUNT = 4;

    /**
     * Array index for max query duration.
     */
    public static final int MAX_QUERY_DURATION = 5;

    /**
     * Array index for max query descriptions.
     */
    public static final int MAX_QUERY_DESCRIPTION = 6;

    /**
     * Array index for non-optimized query avg.
     */
    public static final int NON_OPTIMIZED_QUERY_AVG = 7;

    /**
     * Array index for optimized query avg.
     */
    public static final int OPTIMIZED_QUERY_AVG = 8;

    /**
     * Array index for index total units avg.
     */
    public static final int INDEX_TOTAL_UNITS = 9;
    
    /**
     * Array index for indexing total millis.
     */
    public static final int INDEXING_TOTAL_MILLIS = 10;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheStorageManagerData.class.getName());

    /**
     * Report for storage manager data.
     */
    public static final String REPORT_STORAGE_MANAGER = "reports/visualvm/cache-storage-manager-stats.xml";

    /**
     * JMX attribute name for Locks Granted.
     */
    protected static final String ATTR_LOCKS_GRANTED = "LocksGranted";

    /**
     * JMX attribute name for Locks Pending
     */
    protected static final String ATTR_LOCKS_PENDING = "LocksPending";

    /**
     * JMX attribute name for Listener Key Count.
     */
    protected static final String ATTR_LISTENER_KEY_COUNT = "ListenerKeyCount";

    /**
     * JMX attribute name for Listener Filter Count.
     */
    protected static final String ATTR_LISTENER_FILTER_COUNT = "ListenerFilterCount";
    }
