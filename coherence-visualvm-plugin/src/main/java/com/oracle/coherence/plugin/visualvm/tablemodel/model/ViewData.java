/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * A class to hold basic view data.
 *
 * @author tam  2024.02.26
 * @since  1.7.0
 */
public class ViewData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ProxyData passing in the number of columns.
     */
    public ViewData()
        {
        super(CACHE_VALUES + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<>();
        Data                    data;
        Pair<String, String>    selectedCache = model.getSelectedCache();

        try
            {
            Set<ObjectName> viewNamesSet = requestSender.getViewMembers(selectedCache.getX(), selectedCache.getY());

            for (Iterator<ObjectName> nodIter = viewNamesSet.iterator(); nodIter.hasNext(); )
                {
                ObjectName viewNameObjName = (ObjectName) nodIter.next();

                data = new ViewData();
                
                AttributeList listAttr = requestSender.getAttributes(viewNameObjName,
                   new String[] { ATTR_TRANSFORMED, ATTR_FILTER, ATTR_TRANSFORMER, ATTR_CACHE_VALUES,
                                  ATTR_SIZE, ATTR_READ_ONLY, ATTR_RECONNECT_INTERVAL});

                data.setColumn(ViewData.NODE_ID, Integer.valueOf(viewNameObjName.getKeyProperty("nodeId")));
                data.setColumn(ViewData.SIZE, Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_SIZE)));

                data.setColumn(ViewData.RECONNECT_INTERVAL, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_SIZE)));
                data.setColumn(ViewData.FILTER, getAttributeValueAsString(listAttr, ATTR_FILTER));
                data.setColumn(ViewData.TRANSFORMED, getAttributeValueAsString(listAttr, ATTR_TRANSFORMED));
                data.setColumn(ViewData.TRANSFORMER, getAttributeValueAsString(listAttr, ATTR_TRANSFORMER));
                data.setColumn(ViewData.READ_ONLY, getAttributeValueAsString(listAttr, ATTR_READ_ONLY));
                data.setColumn(ViewData.CACHE_VALUES, getAttributeValueAsString(listAttr, ATTR_CACHE_VALUES));

                mapData.put(data.getColumn(ViewData.NODE_ID), data);
                }

            return new ArrayList<>(mapData.entrySet());
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, "Error getting view statistics", e);

            return null;
            }
        }

    @Override
    public String getReporterReport()
        {
        return REPORT_VIEW;
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // the report XML contains the following tokens that require substitution:
        // %SERVICE_NAME%
        // %VIEW_NAME%

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
                sReporterXML.replaceAll("%SERVICE_NAME%", escape(sServiceName) +
                                        (sDomainPartition != null ? ",domainPartition=" + sDomainPartition : "") )
                            .replaceAll("%VIEW_NAME%", escape(selectedCache.getY()));
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new ViewData();

        data.setColumn(NODE_ID, Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(SIZE, Integer.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(RECONNECT_INTERVAL, Long.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(FILTER, aoColumns[5].toString());
        data.setColumn(TRANSFORMED, Boolean.toString(Boolean.parseBoolean(aoColumns[6].toString())));
        Object sTransformer = aoColumns[7];
        data.setColumn(TRANSFORMER, "".equals(sTransformer) || sTransformer == null ? "n/a" : sTransformer.toString());
        data.setColumn(READ_ONLY, Boolean.toString(Boolean.parseBoolean(aoColumns[8].toString())));
        data.setColumn(CACHE_VALUES, Boolean.toString(Boolean.parseBoolean(aoColumns[9].toString())));

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        SortedMap<Object, Data> mapData    = new TreeMap<Object, Data>();
        Pair<String, String> selectedCache = model.getSelectedCache();

        if (selectedCache == null)
            {
            return mapData;
            }

        JsonNode rootNode  = requestSender.getDataForViews(selectedCache.getX(), selectedCache.getY());

        JsonNode viewMemberItems = rootNode.get("items");
        if (viewMemberItems != null && viewMemberItems.isArray())
            {
            for (int k = 0; k < (viewMemberItems).size(); k++)
                {
                JsonNode viewNode = viewMemberItems.get(k);

                ViewData data = new ViewData();

                data.setColumn(NODE_ID, viewNode.get("nodeId").asInt());
                data.setColumn(SIZE,  viewNode.get("size").asInt());
                data.setColumn(RECONNECT_INTERVAL, viewNode.get("reconnectInterval").asLong());
                data.setColumn(FILTER, viewNode.get("filter").asText());
                data.setColumn(TRANSFORMED, Boolean.toString(viewNode.get("transformed").asBoolean()));
                data.setColumn(READ_ONLY, Boolean.toString(viewNode.get("readOnly").asBoolean()));
                data.setColumn(CACHE_VALUES, Boolean.toString(viewNode.get("cacheValues").asBoolean()));
                JsonNode transformer = viewNode.get("transformer");
                data.setColumn(TRANSFORMER, transformer == null ? "n/a" : Boolean.toString(transformer.asBoolean()));

                mapData.put(data.getColumn(NODE_ID), data);
                }
            }
        return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789858484301825295L;

    /**
     * Array index for Node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for Size.
     */
    public static final int SIZE = 1;

    /**
     * Array index for ReconnectInterval.
     */
    public static final int RECONNECT_INTERVAL = 2;

    /**
     * Array index for Filter.
     */
    public static final int FILTER = 3;

    /**
     * Array index for Transformed.
     */
    public static final int TRANSFORMED = 4;

    /**
     * Array index for Transformer.
     */
    public static final int TRANSFORMER = 5;

    /**
     * Array index for ReadOnly
     */
    public static final int READ_ONLY = 6;

    /**
     * Array index for CacheValues.
     */
    public static final int CACHE_VALUES = 7;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ViewData.class.getName());

    /**
     * Report for proxy server data.
     */
    public static final String REPORT_VIEW = "reports/visualvm/view-details.xml";

    /**
     * JMX attribute name for Size.
     */
    private static final String ATTR_SIZE = "Size";

    /**
     * JMX attribute name for ReconnectInterval.
     */
    private static final String ATTR_RECONNECT_INTERVAL = "ReconnectInterval";

    /**
     * JMX attribute name for Filter.
     */
    private static final String ATTR_FILTER= "Filter";

    /**
     * JMX attribute name for Transformed.
     */
    private static final String ATTR_TRANSFORMED = "Transformed";
    
    /**
     * JMX attribute name for Transformer.
     */
    private static final String ATTR_TRANSFORMER = "Transformer";

    /**
     * JMX attribute name for ReadOnly.
     */
    private static final String ATTR_READ_ONLY = "ReadOnly";

    /**
     * JMX attribute name for CacheValues.
     */
    private static final String ATTR_CACHE_VALUES = "CacheValues";

    /**
     * JMX attribute name for Node Id.
     */
    private static final String ATTR_NODE_ID = "NodeId";
    }
