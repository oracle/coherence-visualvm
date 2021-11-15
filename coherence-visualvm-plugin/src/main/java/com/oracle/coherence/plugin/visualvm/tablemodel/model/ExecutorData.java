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

import com.fasterxml.jackson.databind.JsonNode;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

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
        super(HEAP_FREE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
    * {@inheritDoc}
    */
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        // never used as report will provide data
        return null;
        }

    /**
     * {@inheritDoc}
     */
    public String getReporterReport()
        {
        return REPORT_EXECUTOR;
        }

    /**
     * {@inheritDoc}
     */
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new ExecutorData();

        int nNodeId = Integer.parseInt(getNumberValue(aoColumns[2].toString()));
        data.setColumn(ExecutorData.NAME, aoColumns[1].toString());
        data.setColumn(ExecutorData.NODE_ID, nNodeId);
        data.setColumn(ExecutorData.TASKS_COMPLETED, Long.valueOf(getNumberValue(aoColumns[3].toString())));
        data.setColumn(ExecutorData.TASKS_REJECTED, Long.valueOf(getNumberValue(aoColumns[4].toString())));
        data.setColumn(ExecutorData.TASKS_IN_PROGRESS, Long.valueOf(getNumberValue(aoColumns[5].toString())));
        data.setColumn(ExecutorData.STATE, aoColumns[6].toString());

        // retrieve the member to populate the memory attributes
        setMemberMemory(data, model, nNodeId);

        return data;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        JsonNode rootNode = requestSender.getExecutors();
        SortedMap<Object, Data> mapData              = new TreeMap<>();
        JsonNode                nodeProxyExecutorsItems = rootNode.get("items");
        if (nodeProxyExecutorsItems != null && nodeProxyExecutorsItems.isArray())
            {
            for (int k = 0; k < (nodeProxyExecutorsItems).size(); k++)
                {
                JsonNode executor = nodeProxyExecutorsItems.get(k);

                ExecutorData data = new ExecutorData();
                data.setColumn(NODE_ID, executor.get("nodeId").asInt());
                data.setColumn(NAME, executor.get("name").asText());
                data.setColumn(STATE, executor.get("state").asText());
                data.setColumn(TASKS_IN_PROGRESS, executor.get("tasksInProgressCount").asLong());
                data.setColumn(TASKS_COMPLETED, executor.get("tasksCompletedCount").asLong());
                data.setColumn(TASKS_REJECTED, executor.get("tasksRejectedCount").asLong());

                // retrieve the member to populate the memory attributes
                setMemberMemory(data, model, executor.get("memberId").asInt());

                mapData.put(data.getColumn(0), data);
                }
            }

            return mapData;
        }

    /**
     * Sets the member memory given the node Id
     * @param data   {@link Data}to set for
     * @param model    the {@link VisualVMModel} to ask for data from
     * @param nNodeId  the node id to look for
     */
    private void setMemberMemory(Data data, VisualVMModel model, int nNodeId)
        {
        MemberData member = getMember(model, nNodeId);
        if (member == null)
            {
            LOGGER.warning("Unable to find member for node id " + nNodeId);
            }
        else
            {
            int nHeapMax = Integer.parseInt(member.getColumn(MemberData.MAX_MEMORY).toString());
            int nHeapUsed = Integer.parseInt(member.getColumn(MemberData.USED_MEMORY).toString());
            data.setColumn(ExecutorData.HEAP_MAX, nHeapMax);
            data.setColumn(ExecutorData.HEAP_USED, nHeapUsed);
            data.setColumn(ExecutorData.HEAP_FREE, nHeapMax - nHeapUsed);
            }
        }

    /**
     * Returns the {@link MemberData} for the node id.
     *
     * @param model    the {@link VisualVMModel} to ask for data from
     * @param nNodeId  the node id to look for
     * @return the {@link MemberData} or null if not found
     */
    private MemberData getMember(VisualVMModel model, int nNodeId)
        {
        List<Map.Entry<Object, Data>> memberData = model.getData(VisualVMModel.DataType.MEMBER);
        for (Map.Entry<Object, Data> entry : memberData)
            {
            if (((Integer) entry.getValue().getColumn(MemberData.NODE_ID)) == nNodeId)
                {
                return (MemberData) entry.getValue();
                }
            }
        return null;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802484301825295L;

    /**
     * Array index for name.
     */
    public static final int NAME = 0;

    /**
     * Array index for Node id.
     */
    public static final int NODE_ID = 1;

    /**
     * Array index for state
     */
    public static final int STATE = 2;

    /**
     * Array index for tasks in progress.
     */
    public static final int TASKS_IN_PROGRESS = 3;

    /**
     * Array index for tasks completed.
     */
    public static final int TASKS_COMPLETED = 4;

    /**
     * Array index for tasks rejected.
     */
    public static final int TASKS_REJECTED = 5;

    /**
     * Array index for heap max.
     */
    public static final int HEAP_MAX = 6;

    /**
     * Array index for heap used
     */
    public static final int HEAP_USED = 7;

    /**
     * Array index for free used
     */
    public static final int HEAP_FREE = 8;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ExecutorData.class.getName());

    /**
     * Report for proxy server data.
     */
    public static final String REPORT_EXECUTOR = "reports/visualvm/executor-stats.xml";
    }