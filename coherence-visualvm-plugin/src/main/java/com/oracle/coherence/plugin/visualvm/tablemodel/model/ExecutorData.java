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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import javax.management.AttributeList;
import javax.management.ObjectName;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * A class to hold basic Executor data.
 *
 * @author tam  2021.08.11
 * @since  1.2.0
 */
public class ExecutorData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ExecutorData passing in the number of columns.
     */
    public ExecutorData()
        {
        super(DESCRIPTION + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<>();
        Data                    data;
        Map<String, Integer> mapExecutorCount = new HashMap<>();

        try
            {
            // force to use more efficient http
            if (requestSender instanceof HttpRequestSender)
                {
                return new ArrayList<>(getAggregatedDataFromHttpQuerying(model, (HttpRequestSender) requestSender).entrySet());
                }

            Set<ObjectName> setNodeNames = requestSender.getAllExecutorMembers();

            for (Iterator<ObjectName> iter = setNodeNames.iterator(); iter.hasNext(); )
                {
                ObjectName nodeNameObjName = iter.next();

                String        sName    = nodeNameObjName.getKeyProperty("name");
                AttributeList listAttr = requestSender.getAttributes(nodeNameObjName,
                  new String[] { ATTR_MEMBER_ID, ATTR_TASKS_COMPLETED, ATTR_TASKS_REJECTED,
                                 ATTR_TASKS_IN_PROGRESS, ATTR_DESCRIPTION });

                data = new ExecutorData();
                data.setColumn(NAME, sName);
                data.setColumn(EXECUTOR_COUNT, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_MEMBER_ID)));
                data.setColumn(TASKS_IN_PROGRESS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TASKS_IN_PROGRESS)));
                data.setColumn(TASKS_COMPLETED, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TASKS_COMPLETED)));
                data.setColumn(TASKS_REJECTED, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TASKS_REJECTED)));
                data.setColumn(DESCRIPTION, getAttributeValueAsString(listAttr, ATTR_DESCRIPTION));
                mapData.put(sName, data);

                if (!mapExecutorCount.containsKey(sName))
                    {
                    mapExecutorCount.put(sName, 1);
                    }
                else
                    {
                    mapExecutorCount.put(sName, mapExecutorCount.get(sName) + 1);
                    }
                }

            // process the final data to update the executor count with the proper count
            mapData.forEach((k,v) -> v.setColumn(EXECUTOR_COUNT, mapExecutorCount.get(k)));

            return new ArrayList<>(mapData.entrySet());
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, "Error getting member statistics", e);

            return null;
            }
        }

    @Override
    public String getReporterReport()
        {
        // force to use JMX
        return null;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        return null;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        JsonNode rootNode = requestSender.getExecutors();
        SortedMap<Object, Data> mapData                 = new TreeMap<>();
        JsonNode                nodeProxyExecutorsItems = rootNode.get("items");
        Map<String, Integer>    mapExecutorCount        = new HashMap<>();

        if (nodeProxyExecutorsItems != null && nodeProxyExecutorsItems.isArray())
            {
            for (int k = 0; k < (nodeProxyExecutorsItems).size(); k++)
                {
                JsonNode     executor = nodeProxyExecutorsItems.get(k);
                ExecutorData data     = new ExecutorData();
                String       sName    = executor.get("name").asText();
                
                data.setColumn(NAME, sName);
                data.setColumn(TASKS_IN_PROGRESS, executor.get("tasksInProgressCount").asLong());
                data.setColumn(TASKS_COMPLETED, executor.get("tasksCompletedCount").asLong());
                data.setColumn(TASKS_REJECTED, executor.get("tasksRejectedCount").asLong());
                data.setColumn(DESCRIPTION, executor.get("description").asText());

                mapData.put(sName, data);

                if (!mapExecutorCount.containsKey(sName))
                    {
                    mapExecutorCount.put(sName, 1);
                    }
                else
                    {
                    mapExecutorCount.put(sName, mapExecutorCount.get(sName) + 1);
                    }
                }
            }

            // process the final data to update the executor count with the proper count
            mapData.forEach((k,v) -> v.setColumn(EXECUTOR_COUNT, mapExecutorCount.get(k)));

            return mapData;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802484301825295L;

    /**
     * Array index for name.
     */
    public static final int NAME = 0;

    /**
     * Array index for executor count.
     */
    public static final int EXECUTOR_COUNT = 1;

    /**
     * Array index for tasks in progress.
     */
    public static final int TASKS_IN_PROGRESS = 2;

    /**
     * Array index for tasks completed.
     */
    public static final int TASKS_COMPLETED = 3;

    /**
     * Array index for tasks rejected.
     */
    public static final int TASKS_REJECTED = 4;

    /**
     * Array index for description.
     */
    public static final int DESCRIPTION = 5;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ExecutorData.class.getName());

    private static final String ATTR_MEMBER_ID = "MemberId";
    private static final String ATTR_TASKS_COMPLETED = "TasksCompletedCount";
    private static final String ATTR_TASKS_REJECTED = "TasksRejectedCount";
    private static final String ATTR_TASKS_IN_PROGRESS = "TasksInProgressCount";
    private static final String ATTR_DESCRIPTION = "Description";
    }