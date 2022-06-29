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
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

/**
 * A class to hold health data.
 *
 * @author tam  2022.06.22
 * @since  1.4.0
 */
public class HealthData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create PersistenceData passing in the number of columns.
     */
    public HealthData()
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
        Data data = new HealthData();

        HealthKey key = new HealthKey((String) aoColumns[3], (String) aoColumns[4], Integer.parseInt(getNumberValue(aoColumns[2].toString())));

        data.setColumn(HealthData.HEALTH_NAME, key);
        data.setColumn(HealthData.NODE_ID, Integer.valueOf(getNumberValue(aoColumns[2].toString())));
        data.setColumn(HealthData.SUB_TYPE, aoColumns[4]);
        data.setColumn(HealthData.STARTED, Boolean.valueOf(aoColumns[5].toString()));
        data.setColumn(HealthData.LIVE, Boolean.valueOf(aoColumns[6].toString()));
        data.setColumn(HealthData.READY, Boolean.valueOf(aoColumns[7].toString()));
        data.setColumn(HealthData.SAFE, Boolean.valueOf(aoColumns[8].toString()));
        data.setColumn(HealthData.CLASS_NAME, aoColumns[9].toString());

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel model, HttpRequestSender requestSender)
            throws Exception
        {
        SortedMap<Object, Data> mapData           = new TreeMap<>();
        JsonNode                allStorageMembers = requestSender.getAllHealthMembers();
        JsonNode healthItemsNode  = allStorageMembers.get("items");

        Data                    data;

        if (healthItemsNode != null && healthItemsNode.isArray())
            {
            for (int i = 0; i < ((ArrayNode) healthItemsNode).size(); i++)
                {
                JsonNode details = healthItemsNode.get(i);

                data = new HealthData();
                String sName    = details.get("name").asText();
                String sSubType = details.get("subType").asText();
                int    nNodeId  = details.get("nodeId").asInt();

                HealthKey key = new HealthKey(sName, sSubType, nNodeId);

                data.setColumn(HealthData.HEALTH_NAME, key);
                data.setColumn(HealthData.NODE_ID, nNodeId);
                data.setColumn(HealthData.SUB_TYPE, sSubType);
                data.setColumn(HealthData.STARTED, details.get("started").asBoolean());
                data.setColumn(HealthData.LIVE, details.get("live").asBoolean());
                data.setColumn(HealthData.READY, details.get("ready").asBoolean());
                data.setColumn(HealthData.SAFE, details.get("safe").asBoolean());
                data.setColumn(HealthData.CLASS_NAME, details.get("className").asText());

                mapData.put(key, data);
                }
            }
            return mapData;
        }

    /**
     * Key to be used for the Health Data.
     */
    public static class HealthKey implements Comparable<HealthKey>
        {

        public HealthKey(String sName, String sSubType, int nNodeId)
            {
            f_sName = sName;
            f_sSubType = sSubType;
            f_nNodeId = nNodeId;
            }

        public int getNodeId() {
            return f_nNodeId;
        }

        public String getName() {
            return f_sName;
        }

        public String getSubType() {
            return f_sSubType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HealthKey healthKey = (HealthKey) o;

            if (f_nNodeId != healthKey.f_nNodeId) return false;
            if (!Objects.equals(f_sName, healthKey.f_sName)) return false;
            return Objects.equals(f_sSubType, healthKey.f_sSubType);
        }

        @Override
        public int hashCode() {
            int result = f_sName != null ? f_sName.hashCode() : 0;
            result = 31 * result + (f_sSubType != null ? f_sSubType.hashCode() : 0);
            result = 31 * result + f_nNodeId;
            return result;
        }

        @Override
        public String toString()
            {
            return f_sName + "/" + f_sSubType + "/" + f_nNodeId;
            }

        @Override
        public int compareTo(HealthKey o) {
            int i = Integer.compare(o.f_nNodeId, this.f_nNodeId);
            if (i != 0 )
                {
                return i;
                }
            i = o.f_sName.compareTo(this.f_sName);

            if (i != 0 )
                {
                return i;
                }

            return o.f_sSubType.compareTo(this.f_sSubType);
        }

        private final String f_sName;
        private final String f_sSubType;
        private final int    f_nNodeId;
        }
        
    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 7769559573242105947L;

    /**
     * Array index for health name.
     */
    public static final int HEALTH_NAME = 0;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 1;

    /**
     * Array index for sub-type.
     */
    public static final int SUB_TYPE = 2;

    /**
     * Array index for started.
     */
    public static final int STARTED = 3;

    /**
     * Array index for live.
     */
    public static final int LIVE = 4;

    /**
     * Array index for ready.
     */
    public static final int READY = 5;

    /**
     * Array index for safe
     */
    public static final int SAFE = 6;

    /**
     * Array index for description.
     */
    public static final int CLASS_NAME = 7;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(HealthData.class.getName());

    /**
     * Report for cluster data.
     */
    public static final String REPORT_HEALTH = "reports/visualvm/health-stats.xml";
    }
