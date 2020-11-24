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

import javax.management.AttributeList;
import javax.management.ObjectName;

import javax.management.openmbean.CompositeDataSupport;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValue;
import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * A class to hold hotcache data.
 *
 * @author nagaraju  2016.12.24
 *
 */
public class HotCacheData
        extends AbstractData
    {
    //------ constructors ----------------------------------------------------------------------------------------
    /**
     * Create HotCacheData passing in the number of columns.
     */
    public HotCacheData()
        {
        super(26);
        }

    // ----- DataRetriever methods ------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Data                    data;

        try
            {
            Set<ObjectName> hotCacheNamesSet = requestSender.getHotCacheMembers();
            for (Iterator<ObjectName> nodIter = hotCacheNamesSet.iterator(); nodIter.hasNext(); )
                {
                ObjectName hotCacheNameObjName = (ObjectName) nodIter.next();
                Integer nodeId = Integer.valueOf(hotCacheNameObjName.getKeyProperty("nodeId"));
                String member       = hotCacheNameObjName.getKeyProperty("member");
                data = new HotCacheData();
                AttributeList  listAttr = requestSender.getAttributes(hotCacheNameObjName,
                        new String[]{ATTR_MEMBER, ATTR_NUMBER_OF_OPERATIONS_PROCESSED, ATTR_START_TIME, ATTR_TRAIL_FILENAME, ATTR_TRAIL_FILEPOS, ATTR_ExecTime_PerOpr, ATTR_ExecTime_PerTr,
                        ATTR_Invocations_PerOp, ATTR_LastExecTime_PerOp, ATTR_LastOpRepLag, ATTR_OpRepLag, ATTR_OpPerTr});
                data.setColumn(HotCacheData.MEMBER, member);
                data.setColumn(HotCacheData.NUMBER_OF_OPERATIONS_PROCESSED, getAttributeValueAsString(listAttr,ATTR_NUMBER_OF_OPERATIONS_PROCESSED));
                data.setColumn(HotCacheData.START_TIME, getAttributeValueAsString(listAttr,ATTR_START_TIME));
                data.setColumn(HotCacheData.TRAIL_FILENAME, getAttributeValueAsString(listAttr,ATTR_TRAIL_FILENAME));
                data.setColumn(HotCacheData.TRAIL_FILEPOS, getAttributeValueAsString(listAttr,ATTR_TRAIL_FILEPOS));

                data.setColumn(HotCacheData.Max1,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerOpr)).get("max"));
                data.setColumn(HotCacheData.Mean1,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerOpr)).get("average"));
                data.setColumn(HotCacheData.Min1,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerOpr)).get("min"));

                data.setColumn(HotCacheData.Max2,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerTr)).get("max"));
                data.setColumn(HotCacheData.Mean2,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerTr)).get("average"));
                data.setColumn(HotCacheData.Min2,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_ExecTime_PerTr)).get("min"));

                data.setColumn(HotCacheData.Max3,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_Invocations_PerOp)).get("max"));
                data.setColumn(HotCacheData.Mean3,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_Invocations_PerOp)).get("average"));
                data.setColumn(HotCacheData.Min3,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_Invocations_PerOp)).get("min"));

                data.setColumn(HotCacheData.Max4,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastExecTime_PerOp)).get("max"));
                data.setColumn(HotCacheData.Mean4,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastExecTime_PerOp)).get("average"));
                data.setColumn(HotCacheData.Min4,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastExecTime_PerOp)).get("min"));

                data.setColumn(HotCacheData.Max5,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastOpRepLag)).get("max"));
                data.setColumn(HotCacheData.Mean5,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastOpRepLag)).get("average"));
                data.setColumn(HotCacheData.Min5,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_LastOpRepLag)).get("min"));

                data.setColumn(HotCacheData.Max6,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpRepLag)).get("max"));
                data.setColumn(HotCacheData.Mean6,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpRepLag)).get("average"));
                data.setColumn(HotCacheData.Min6,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpRepLag)).get("min"));

                data.setColumn(HotCacheData.Max7,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpPerTr)).get("max"));
                data.setColumn(HotCacheData.Mean7,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpPerTr)).get("average"));
                data.setColumn(HotCacheData.Min7,((CompositeDataSupport)getAttributeValue(listAttr,ATTR_OpPerTr)).get("min"));

                mapData.put(member, data);
                }

            return new ArrayList<>(mapData.entrySet());
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, "Error getting hotcache statistics", e);

            return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getReporterReport() {
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
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        // no reports being used, hence using default functionality provided in getJMXData
        return null;
        }

    // ----- constants ------------------------------------------------------
        /**
         * Array index for node id.
         */
        public static final int MEMBER = 0;

        /**
         * Array index for total number of operations processed.
         */
        public static final int NUMBER_OF_OPERATIONS_PROCESSED = 1;

        /**
         * Array index for start time.
         */
        public static final int START_TIME = 2;

        /**
         * Array index for trail file name.
         */
        public static final int TRAIL_FILENAME = 3;

        /**
         * Array index for trail file position.
         */
        public static final int TRAIL_FILEPOS = 4;

        /**
         * Array index for maximum execution time per operation.
         */
        public static final int Max1 = 5;

        /**
         * Array index for maximum execution time per transaction.
         */
        public static final int Max2 = 6;

        /**
         * Array index for maximum number of Invocations Per Operation.
         */
        public static final int Max3 = 7;

        /**
         * Array index for maximum Last execution time Per Operation.
         */
        public static final int Max4 = 8;

        /**
         * Array index for maximum Last Operation Replication Lag.
         */
        public static final int Max5 = 9;

        /**
         * Array index for maximum Operation Replication Lag.
         */
        public static final int Max6 = 10;

        /**
         * Array index for maximum number of Operations Per Transaction.
         */
        public static final int Max7 = 11;

        /**
         * Array index for mean execution time per operation.
         */
        public static final int Mean1 = 12;

        /**
         * Array index for mean execution time per transaction.
         */
        public static final int Mean2 = 13;

        /**
         * Array index for mean number of Invocations Per Operation.
         */
        public static final int Mean3 = 14;

        /**
         * Array index for mean Last execution time Per Operation.
         */
        public static final int Mean4 = 15;

        /**
         * Array index for mean Last Operation Replication Lag.
         */
        public static final int Mean5 = 16;

        /**
         * Array index for mean Operation Replication Lag.
         */
        public static final int Mean6 = 17;

        /**
         * Array index for mean number of Operations Per Transaction.
         */
        public static final int Mean7 = 18;

        /**
         * Array index for minimum execution time per operation.
         */
        public static final int Min1 = 19;

        /**
         * Array index for minimum execution time per transaction.
         */
        public static final int Min2 = 20;

        /**
         * Array index for minimum number of Invocations Per Operation.
         */
        public static final int Min3 = 21;

        /**
         * Array index for minimum Last execution time Per Operation.
         */
        public static final int Min4 = 22;

        /**
         * Array index for minimum Last Operation Replication Lag.
         */
        public static final int Min5 = 23;

        /**
         * Array index for minimum Operation Replication Lag.
         */
        public static final int Min6 = 24;

        /**
         * Array index for minimum number of Operations Per Transaction.
         */
        public static final int Min7 = 25;

        /**
         * The logger object to use.
         */
        private static final Logger LOGGER = Logger.getLogger(HotCacheData.class.getName());

        /**
         * JMX attribute name for Hotcache member.
         */
        private static final String ATTR_MEMBER = "MEMBER";

        /**
         * JMX attribute name for Number Of Operations Processed.
         */
        private static final String ATTR_NUMBER_OF_OPERATIONS_PROCESSED = "NumberOfOperationsProcessed";

        /**
         * JMX attribute name for start time.
         */
        private static final String ATTR_START_TIME = "StartTime";

        /**
         * JMX attribute name for trail filename.
         */
        private static final String ATTR_TRAIL_FILENAME = "TrailFileName";

        /**
         * JMX attribute name for trailfile position.
         */
        private static final String ATTR_TRAIL_FILEPOS = "TrailFilePosition";

        /**
         * JMX attribute name for Execution Time Per Operation Statistics.
         */
        private static final String ATTR_ExecTime_PerOpr = "ExecutionTimePerOperationStatistics";

        /**
         * JMX attribute name for Execution Time Per Transaction Statistics.
         */
        private static final String ATTR_ExecTime_PerTr = "ExecutionTimePerTransactionStatistics";

        /**
         * JMX attribute name for Number Of Invocations Per Operation Statistics.
         */
        private static final String ATTR_Invocations_PerOp = "InvocationsPerOperationStatistics";

        /**
         * JMX attribute name for Last ExecutionTime Per Operation Statistics.
         */
        private static final String ATTR_LastExecTime_PerOp = "LastExecutionTimePerOperationStatistics";

        /**
         * JMX attribute name for Last Operation ReplicationLag Statistics.
         */
        private static final String ATTR_LastOpRepLag = "LastOperationReplicationLagStatistics";

        /**
         * JMX attribute name for Operation ReplicationLag Statistics.
         */
        private static final String ATTR_OpRepLag = "OperationReplicationLagStatistics";

        /**
         * JMX attribute name for Number Of Operations Per Transaction Statistics.
         */
        private static final String ATTR_OpPerTr = "OperationsPerTransactionStatistics";

    }
