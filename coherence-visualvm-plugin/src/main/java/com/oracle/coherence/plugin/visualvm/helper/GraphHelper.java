/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.helper;

import com.oracle.coherence.plugin.visualvm.GlobalPreferences;
import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import org.graalvm.visualvm.charts.ChartFactory;
import org.graalvm.visualvm.charts.SimpleXYChartDescriptor;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * Various helper methods for creating graphs based on JVisualVM graphs.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 *
 */
public class GraphHelper
    {
    // ----- helpers --------------------------------------------------------

    /**
     * Create a graph representing the cluster memory.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createClusterMemoryGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.bytes(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_cluster_memory_details"));
        sxycd.addLineFillItems(getLocalText("GRPH_total_cluster_memory"), getLocalText("GRPH_used_cluster_memory"));

        return createChart(sxycd);
        }

    /**
     * Add values to the cluster memory graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cTotalMemory     total memory available in cluster
     * @param cTotalMemoryUsed used memory available in cluster
     */
    public static void addValuesToClusterMemoryGraph(SimpleXYChartSupport graph, int cTotalMemory, int cTotalMemoryUsed)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {1L * cTotalMemory * MB, 1L * cTotalMemoryUsed * MB});
        }

    /**
     * Create a graph representing the publisher success rate.<br>
     * <strong>Note:</strong> Currently the JVisualVM tool does not allow for
     * more that 2 decimal places to be displayed.  Because of this the
     * publisher and receiver success rates will be shown in the tables as
     * 4 decimal places but only 2 in the graphs.  An enhancement request
     * has been logged for this.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createPublisherGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_packet_publisher"));
        sxycd.addLineFillItems(getLocalText("GRPH_current_average"), getLocalText("GRPH_current_minimum"));

        return createChart(sxycd);
        }

    /**
     * Add values to the publisher success rate graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cMinValue        current minimum value
     * @param cAverageValue    current average value
     */
    public static void addValuesToPublisherGraph(SimpleXYChartSupport graph, float cMinValue, float cAverageValue)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
            (long) (cMinValue * 10000)});
        }

    /**
     * Create a graph representing the receiver success rate.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createReceiverGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1, 0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_packet_receiver"));
        sxycd.addLineFillItems(getLocalText("GRPH_current_average"),
                getLocalText("GRPH_current_minimum"));

        return createChart(sxycd);
        }

    /**
     * Add values to the receiver success rate graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cMinValue        current minimum value
     * @param cAverageValue    current average value
     */
    public static void addValuesToReceiverGraph(SimpleXYChartSupport graph, float cMinValue, float cAverageValue)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
            (long) (cMinValue * 10000)});
        }

    /**
     * Create a graph representing the total cache size.
     *
     * @return        a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTotalCacheSizeGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_total_cache"));
        sxycd.addLineItems(getLocalText("GRPH_total_memory"));

        return createChart(sxycd);
        }

    /**
     * Add values to the total cache size graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cTotalCacheSize  total size of all caches
     */
    public static void addValuesToTotalCacheSizeGraph(SimpleXYChartSupport graph, float cTotalCacheSize)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) cTotalCacheSize});
        }

    /**
     * Create a graph representing the total proxy connections.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTotalProxyConnectionsGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_total_connections"));
        sxycd.addLineItems(getLocalText("GRPH_connection_count"));

        return createChart(sxycd);
        }

    /**
     * Create a graph representing the messages received and responses sent.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createGrpcMessagesGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_grpc_messages"));
        sxycd.addLineItems(getLocalText("LBL_total_grpc_resp_sent"));
        sxycd.addLineItems(getLocalText("LBL_total_grpc_msg_rec"));

        return createChart(sxycd);
        }

    /**
     * Create a graph representing the mean message and response times.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createMeanGrpcStatsGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_mean_values"));
        sxycd.addLineFillItems(getLocalText("LBL_request_duration_mean"), getLocalText("LBL_message_duration_mean"));

        return createChart(sxycd);
        }

    /**
     * Add values to the mean message and response graph.
     *
     * @param graph      {@link SimpleXYChartSupport} to add values to
     * @param cReqMean   current request mean
     * @param cMsgMean   current message mean
     */
    public static void addValuesToMeanGrpcStatsGraph(SimpleXYChartSupport graph, float cReqMean, float cMsgMean)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cReqMean * 10000),
            (long) (cMsgMean * 10000)});
        }


    /**
     * Create a graph representing the currently executing tasks.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createInProgressExecutorTasksGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_tasks_in_progress"));
        sxycd.addLineFillItems(getLocalText("GRPH_task_count"));

        return createChart(sxycd);
        }

    /**
     * Create a graph representing the completed tasks.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createCompletedTasksGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_completed_tasks"));
        sxycd.addLineItems(getLocalText("GRPH_completed_tasks_count"));
        sxycd.addLineItems(getLocalText("GRPH_rejected_tasks_count"));

        return createChart(sxycd);
        }

    /**
     * Create a graph representing the total unconsumed topic messages.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTotalUnconsumedMessagesGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_total_unconsumed"));
        sxycd.addLineItems(getLocalText("GRPH_message_count"));

        return createChart(sxycd);
        }

    /**
     * Add values to the currently executing tasks graph.
     *
     * @param graph             {@link SimpleXYChartSupport} to add values to
     * @param cInProgressTasks total proxy server connections
     */
    public static void addValuesToInProgressExecutorTasksGraph(SimpleXYChartSupport graph, long cInProgressTasks)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cInProgressTasks});
        }

    /**
     * Add values to the completed tasks.
     *
     * @param graph           {@link SimpleXYChartSupport} to add values to
     * @param cCompletedTasks number of completed tasks
     * @param cRejectedTasks  number of rejected tasks
     */
    public static void addValuesToCompletedTasksGraph(SimpleXYChartSupport graph, long cCompletedTasks, long cRejectedTasks)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cCompletedTasks, cRejectedTasks});
        }

    /**
     * Add values to the total proxy connections graph.
     *
     * @param graph             {@link SimpleXYChartSupport} to add values to
     * @param cTotalConnections total proxy server connections
     */
    public static void addValuesToTotalProxyConnectionsGraph(SimpleXYChartSupport graph, int cTotalConnections)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) cTotalConnections});
        }

    /**
     * Add values to the gRPC proxy messages graph.
     *
     * @param graph      {@link SimpleXYChartSupport} to add values to
     * @param cTotalSent total grpc responses sent
     * @param cTotalRec  total grpc messages rec
     */
    public static void addValuesToGrpcMessagesGraph(SimpleXYChartSupport graph, long cTotalSent, long cTotalRec)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cTotalSent, cTotalRec});
        }

    /**
     * Add values to the total topics unconsumed graph.
     *
     * @param graph             {@link SimpleXYChartSupport} to add values to
     * @param cTotalConnections total unconsumed topics messages
     */
    public static void addValuesToTotalUnconsumedTopicsGraph(SimpleXYChartSupport graph, long cTotalConnections)
        {
        graph.addValues(System.currentTimeMillis(), new long[] { cTotalConnections});
        }

    /**
     * Create a graph representing the load average for cluster machines.
     *
     * @param model the {@link VisualVMModel} to check for load average.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createMachineLoadAverageGraph(VisualVMModel model)
        {
        SimpleXYChartDescriptor sxycd = null;

        if (model.isLoadAverageAvailable())
            {
            sxycd = SimpleXYChartDescriptor.decimal(0, 0.01, true, VALUES_LIMIT);

            sxycd.setChartTitle(getLocalText("GRPH_load_average"));
            sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));
            }
        else
            {
            sxycd = SimpleXYChartDescriptor.percent(true, VALUES_LIMIT);

            sxycd.setChartTitle(getLocalText("GRPH_cpu_load"));
            sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));
            }

        return createChart(sxycd);
        }

    /**
     * Add values to the load average graph.
     *
     * @param graph    {@link SimpleXYChartSupport} to add values to
     * @param cMax     current maximum load average
     * @param cAverage current average load average
     */
    public static void addValuesToLoadAverageGraph(SimpleXYChartSupport graph, float cMax, float cAverage)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cMax * 100), (long) (cAverage * 100)});
        }

    /**
     * Create a graph representing the hotcache statistics.
     *
     * @param sSelectedMember the selected member
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createHotcacheValueGraph(String sSelectedMember)
        {
        SimpleXYChartDescriptor sxycd = null;
        sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_hotcache_values", sSelectedMember));

        sxycd.addLineFillItems(getLocalText("GRPH_average"), getLocalText("GRPH_maximum"), getLocalText("GRPH_minimum"));
        return createChart(sxycd);
        }

    /**
     * Create a graph representing the hotcache statistics in milliseconds.
     *
     * @param sSelectedMember the selected member
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createHotcacheValueinMillisGraph(String sSelectedMember)
        {
        SimpleXYChartDescriptor sxycd = null;
        sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_hotcache_millis", sSelectedMember));

        sxycd.addLineFillItems(getLocalText("GRPH_average"), getLocalText("GRPH_maximum"), getLocalText("GRPH_minimum"));
        return createChart(sxycd);
        }

    /**
     * Create a graph representing the hotcache statistics in nanoseconds.
     *
     * @param sSelectedMember the selected member
     *                        
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createHotcacheValueinNanosGraph(String sSelectedMember)
        {
        SimpleXYChartDescriptor sxycd = null;
        sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);
        sxycd.setChartTitle(Localization.getLocalText("GRPH_hotcache_nanos", sSelectedMember));

        sxycd.addLineFillItems(getLocalText("GRPH_average"), getLocalText("GRPH_maximum"), getLocalText("GRPH_minimum"));
        return createChart(sxycd);
        }

    /**
     * Add values to the load hotcache statistics graphs.
     *
     * @param graph    {@link SimpleXYChartSupport} to add values to
     * @param cMax     maximum statistical value
     * @param cAverage average statistical value
     * @param cMin     minimum statistical value
     */
    public static void addValuesToHotcacheGraph(SimpleXYChartSupport graph, double cAverage, long cMax, long cMin)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) cAverage, cMax, cMin});
        }

    /**
     * Create a graph representing the thread utilization percent for a given
     * service.
     *
     * @param sServiceName  service name
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createThreadUtilizationGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.percent(true, VALUES_LIMIT);

        sxycd.addLineFillItems(getLocalText("GRPH_thread_util"));
        sxycd.setChartTitle(Localization.getLocalText("GRPH_thread_util_percent", sServiceName));

        return createChart(sxycd);
        }

    /**
     * Add values to the thread utilization graph.
     *
     * @param graph    {@link SimpleXYChartSupport} to add values to
     * @param lPercent current thread utilization %
     */
    public static void addValuesToThreadUtilizationGraph(SimpleXYChartSupport graph, long lPercent)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {lPercent});
        }

    /**
     * Create a graph representing the Task average duration.
     * 
     * @param sServiceName service name
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTaskDurationGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_task_average", sServiceName));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the task duration graph.
     *
     * @param graph         {@link SimpleXYChartSupport} to add values to
     * @param cAverageValue current average value
     * @param cMaxValue     current maximum value
     */
    public static void addValuesToTaskDurationGraph(SimpleXYChartSupport graph, float cAverageValue, float cMaxValue)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
            (long) (cMaxValue * 10000)});
        }

    /**
     * Create a graph representing the Request average duration.
     *
     *  @param sServiceName service name
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createRequestDurationGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1, 0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_request_average", sServiceName));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the task duration graph.
     *
     * @param graph         {@link SimpleXYChartSupport} to add values to
     * @param cAverageValue current average value
     * @param cMaxValue     current maximum value
     */
    public static void addValuesToRequestDurationGraph(SimpleXYChartSupport graph, float cAverageValue, float cMaxValue)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
            (long) (cMaxValue * 10000)});
        }

    /**
     * Create a graph representing the task backlog for a selected service.
     *
     * @param sServiceName service name
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTaskBacklogGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, 0.01, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_task_backlog", sServiceName));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the task backlog graph
     *
     * @param graph    {@link SimpleXYChartSupport} to add values to
     * @param cMax     current maximum task backlog
     * @param cAverage current average task backlog
     */
    public static void addValuesToTaskBacklogGraph(SimpleXYChartSupport graph, float cMax, float cAverage)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cMax * 100), (long) (cAverage * 100)});
        }

    /**
     * Create a graph representing the average and max persistence latency values.<br>
     * <strong>Note:</strong> Only experimental if displayed.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createPersistenceLatencyGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_persistence_latency"));
        sxycd.addLineFillItems(getLocalText("GRPH_current_average_persistence"));

        return createChart(sxycd);
        }

    /**
     * Add values to the persistence latency graph.
     *
     * @param graph    {@link SimpleXYChartSupport} to add values to
     * @param cAverage current average latency average
     */
    public static void addValuesToPersistenceLatencyGraph(SimpleXYChartSupport graph, float cAverage)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverage * 10000)});
        }

    /**
     *  Create a graph representing the total active persistence space used.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createPersistenceActiveTotalGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.bytes(0, true,
                VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_total_active_space"));
        sxycd.addLineFillItems(getLocalText("GRPH_total_space"));

        return createChart(sxycd);
        }

    /**
     * Add values to the total active persistence space used.
     *
     * @param graph                   {@link SimpleXYChartSupport} to add values to
     * @param cTotalPersistenceSpace  total persistence size of all caches
     */
    public static void addValuesToPersistenceActiveTotalGraph(SimpleXYChartSupport graph, long cTotalPersistenceSpace)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cTotalPersistenceSpace});
        }

    /**
     * Helper method to return localized text.
     *
     * @param sKey the key of text to return
     *
     * @return the localized text
     */
    private static String getLocalText(String sKey)
        {
        return Localization.getLocalText(sKey);
        }

    /**
     * Create a graph representing the http session count over time.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createSessionCountGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_session_sizes"));
        sxycd.addLineItems(getLocalText("GRPH_total_session_count"),
                getLocalText("GRPH_total_overflow_count"));

        return createChart(sxycd);
        }

    /**
     * Add values to the HTTP session count graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cTotalSession    total number of sessions
     * @param cTotalOverflow   total number of sessions in overflow cache
     */
    public static void addValuesToSessionCountGraph(SimpleXYChartSupport graph, int cTotalSession, int cTotalOverflow)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cTotalSession, cTotalOverflow});
        }

    /**
     * Create a graph representing the reap duration.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createReapDurationGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_reap_druation"));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"),
                getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the reap duration graph.
     *
     * @param graph          {@link SimpleXYChartSupport} to add values to
     * @param nCurrentMax     Current Max
     * @param nCurrentAverage Current Averahe
     */
    public static void addValuesToReapDurationGraph(SimpleXYChartSupport graph, long nCurrentMax, long nCurrentAverage)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {nCurrentMax, nCurrentAverage});

        }

    /**
     * Create a graph representing the outbound percentiles in federation tab.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createOutboundPercentileGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(700, 1, true, VALUES_LIMIT);

        sxycd.addLineItems(getLocalText("GRPH_msg_apply_time_outbound"),
                getLocalText("GRPH_record_backlog_delay_outbound"),
                getLocalText("GRPH_msg_network_roundtrip_outbound"));
        sxycd.setChartTitle(getLocalText("GRPH_replication_percentiles_millis"));

        return createChart(sxycd);
        }

    /**
     * Add values to the outbound percentile graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param backlogDelay     the record backlog delay percentile
     * @param networkRoundtrip the network roundtrip percentile
     * @param applyTime        the message apply time percentile
     */
    public static void addValuesToOutboundPercentileDelayGraph(SimpleXYChartSupport graph, long backlogDelay,
        long networkRoundtrip, long applyTime)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {applyTime, backlogDelay, networkRoundtrip});
        }

    /**
     * Create a graph representing the bandwidth utilization in federation tab.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createBandwidthUtilGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, 0.01, true, VALUES_LIMIT);

        sxycd.addLineFillItems(getLocalText("GRPH_max_bandwidth"),
                getLocalText("GRPH_current_bandwidth"));
        sxycd.setChartTitle(getLocalText("GRPH_bandwidth_util"));

        return createChart(sxycd);
        }

    /**
     * Add values to the bandwith utilization graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param maxBandwidth     the max bandwidth
     * @param currentBandwidth the current bandwidht
     */
    public static void addValuesToBandwidthUtilGraph(SimpleXYChartSupport graph, float maxBandwidth,
        float currentBandwidth)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (maxBandwidth * 100),
            (long) (currentBandwidth * 100)});
        }

    /**
     * Create a graph representing the inbound percentiles in federation tab.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createInboundPercentileGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(700, 1,
                true, VALUES_LIMIT);

        sxycd.addLineItems(getLocalText("GRPH_msg_apply_time_inbound"),
                getLocalText("GRPH_record_backlog_delay_inbound"));
        sxycd.setChartTitle(getLocalText("GRPH_replication_percentiles_millis"));

        return createChart(sxycd);
        }

    /**
     * Add values to the inbound percentile graph.
     *
     * @param graph        {@link SimpleXYChartSupport} to add values to
     * @param backlogDelay the record backlog delay percentile
     * @param applyTime    the message apply time percentile
     */
    public static void addValuesToInboundPercentileGraph(SimpleXYChartSupport graph, long backlogDelay, long applyTime)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {applyTime, backlogDelay});
        }

    /**
     * Create a graph representing the ramjournal memory.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createRamJournalMemoryGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.bytes(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_ramjournal_memory_details"));
        sxycd.addLineFillItems(getLocalText("GRPH_total_ramjournal_memory"),
                getLocalText("GRPH_used_ramjournal_memory"));

        return createChart(sxycd);
        }

    /**
     * Add values to the ramjounral memory graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cTotalMemory     total memory available in cluster
     * @param cTotalMemoryUsed used memory available in cluster
     */
    public static void addValuesToRamJournalMemoryGraph(SimpleXYChartSupport graph, long cTotalMemory,
        long cTotalMemoryUsed)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cTotalMemory, cTotalMemoryUsed});
        }

    /**
     * Create a graph representing the flashjournal usage.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createFlashJournalMemoryGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.bytes(0, true,
                VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_flashjournal_usage_details"));
        sxycd.addLineFillItems(getLocalText("GRPH_total_flashjournal_space"),
                               getLocalText("GRPH_used_flashjournal_space"));

        return createChart(sxycd);
        }

    /**
     * Add values to the flashjournal graph.
     *
     * @param graph            {@link SimpleXYChartSupport} to add values to
     * @param cTotalFlash      total flash available in cluster
     * @param cTotalFlashUsed  used flash available in cluster
     */
    public static void addValuesToFlashJournalMemoryGraph(SimpleXYChartSupport graph, long cTotalFlash,
        long cTotalFlashUsed)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cTotalFlash, cTotalFlashUsed});
        }

    /**
     * Create a graph representing the ramjournal compactions.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createRamJournalCompactionGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_ramjournal_compactions"));
        sxycd.addLineFillItems(getLocalText("GRPH_compactions"),
                               getLocalText("GRPH_exhaustive_compactions"));

        return createChart(sxycd);
        }

    /**
     * Add values to the ramjournal graph.
     *
     * @param graph                 {@link SimpleXYChartSupport} to add values to
     * @param cCompaction           compactions in last time period (delta)
     * @param cExhaustiveCompaction exhaustive compactions in last time period (delta)
     */
    public static void addValuesToRamJournalCompactionGraph(SimpleXYChartSupport graph, int cCompaction,
        int cExhaustiveCompaction)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cCompaction, cExhaustiveCompaction});
        }

    /**
     * Create a graph representing the flashjournal compactions.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createFlashJournalCompactionGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(getLocalText("GRPH_flashjournal_compactions"));
        sxycd.addLineFillItems(getLocalText("GRPH_compactions"), getLocalText("GRPH_exhaustive_compactions"));

        return createChart(sxycd);
        }

    /**
     * Add values to the flashjournal graph.
     *
     * @param graph                 {@link SimpleXYChartSupport} to add values to
     * @param cCompaction           compactions in last time period (delta)
     * @param cExhaustiveCompaction exhaustive compactions in last time period (delta)
     */
    public static void addValuesToFlashJournalCompactionGraph(SimpleXYChartSupport graph, int cCompaction,
        int cExhaustiveCompaction)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cCompaction, cExhaustiveCompaction});
        }

    /**
     * Create a graph representing the service partition values.
     *
     * @param sServiceName the service to create the graph for
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createServicePartitionGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_service_partitions", sServiceName));
        sxycd.addLineItems(getLocalText("LBL_pending"),
                getLocalText("LBL_unbalanced"),
                getLocalText("LBL_vulnerable"),
                getLocalText("LBL_endangered"));

        return createChart(sxycd);
        }

    /**
     * Add values to the service partitions graph.
     *
     * @param graph        {@link SimpleXYChartSupport} to add values to
     * @param cEndangered  Endangered partition count
     * @param cVulnerable   Vulnerable partition count
     * @param cUnbalanced   Unbalanced partition count
     * @param cPending     Pending partition count
     */
    public static void addValuesToServicePartitionGraph(SimpleXYChartSupport graph, int cEndangered ,
                                                        int cVulnerable, int cUnbalanced, int cPending )
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cPending, cUnbalanced, cVulnerable, cEndangered});
        }

    /**
     * Create a graph representing the JCache average get/put/remove rates in micros.<br>
     * <strong>Note:</strong> Currently the JVisualVM tool does not allow for
     * more that 2 decimal places to be displayed.  Because of this the
     * rates will be shown in the tables as only 2 dec places.
     * An enhancement request has been logged for this for JVisualVM.
     *
     * @param sSelectedCache  the selected cache to display in header
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createJCacheAverageGraph(String sSelectedCache)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(
                Localization.getLocalText("GRPH_average_operation_rates", sSelectedCache));
        sxycd.addLineItems(getLocalText("GRPH_average_put_time"),
                getLocalText("GRPH_average_get_time"),
                getLocalText("GRPH_average_remove_time"));

        return createChart(sxycd);
        }

    /**
     * Add values to the JCache get/put/remove rates chart.
     *
     * @param graph           {@link SimpleXYChartSupport} to add values to
     * @param cAveragePut     average put time in ms
     * @param cAverageGet     average get time in ms
     * @param cAverageRemove  average remove time in ms
     */
    public static void addValuesToJCacheAverageGraph(SimpleXYChartSupport graph, float cAveragePut, float cAverageGet,
                                                         float cAverageRemove)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAveragePut * 10000),
                (long) (cAverageGet * 10000),(long) (cAverageRemove * 10000)});
        }

    /**
     * Create a graph representing the JCache hit rate for a given
     * cache.
     *
     * @param sCacheName  the cache name to display for
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createJCacheHitPercentageGraph(String sCacheName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.percent(true, VALUES_LIMIT);

        sxycd.addLineFillItems(getLocalText("LBL_hit_percentage"));
        sxycd.setChartTitle(Localization.getLocalText("GRPH_cache_hit_percentage", sCacheName));

        return createChart(sxycd);
        }

    /**
      * Add values to the JCache hit percentage graph.
      *
      * @param graph    {@link SimpleXYChartSupport} to add values to
      * @param lPercent current hit rate %
      */
    public static void addValuesToJCacheHitPercentagGraph(SimpleXYChartSupport graph, long lPercent)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {lPercent});
        }

    /**
     * Create a graph representing the average request time for http requests.
     *
     * @param sServiceName  service name to display
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createAverageRequestTimeGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1,
                0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(
                Localization.getLocalText("GRPH_average_request_time", sServiceName));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the task duration graph.
     *
     * @param graph         {@link SimpleXYChartSupport} to add values to
     * @param cAverageValue current average value
     * @param cMaxValue     current maximum value
     */
    public static void addValuesToAverageRequestTimeGraph(SimpleXYChartSupport graph, float cAverageValue, float cMaxValue)
      {
      graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
                                                              (long) (cMaxValue * 10000)});
      }

    /**
     * Create a graph representing the average requests per second for http requests.
     *
     * @param sServiceName  service name to display
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createAverageRequestsPerSecondGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(1, 0.0001, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_average_request_per_second", sServiceName));
        sxycd.addLineFillItems(getLocalText("GRPH_current_maximum"), getLocalText("GRPH_current_average"));

        return createChart(sxycd);
        }

    /**
     * Add values to the average requests per second graph.
     *
     * @param graph         {@link SimpleXYChartSupport} to add values to
     * @param cAverageValue current average value
     * @param cMaxValue     current maximum value
     */
    public static void addValuesToAverageRequestsPerSecondGraph(SimpleXYChartSupport graph, float cAverageValue, float cMaxValue)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {(long) (cAverageValue * 10000),
                                                                (long) (cMaxValue * 10000)});
        }

    /**
     * Create a graph representing the Http requests and error counts.
     *
     * @param sServiceName  service name to display
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createHttpRequestGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_requests_over_time", sServiceName));
        sxycd.addLineItems(getLocalText("LBL_total_request_count"),
                           getLocalText("LBL_total_error_count"));

        return createChart(sxycd);
        }

    /**
     * Add values to the Http requests and error count graph.
     *
     * @param graph      {@link SimpleXYChartSupport} to add values to
     * @param cRequests  requests in the last time period (delta)
     * @param cErrors    errors in the last time period (delta)
     */
    public static void addValuesToHttpRequestGraph(SimpleXYChartSupport graph, long cRequests,
                                                            long cErrors)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cRequests, cErrors});
        }

    /**
     * Create a graph representing the Http requests and error counts.
     *
     * @param sServiceName  service name to display
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createHttpResponseGraph(String sServiceName)
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_response_over_time", sServiceName));
        sxycd.addLineItems(getLocalText("LBL_status1xx"),
                           getLocalText("LBL_status2xx"),
                           getLocalText("LBL_status3xx"),
                           getLocalText("LBL_status4xx"),
                           getLocalText("LBL_status5xx")
        );

        return createChart(sxycd);
        }

    /**
     * Add values to the Http requests and error count graph.
     *
     * @param graph         {@link SimpleXYChartSupport} to add values to
     * @param cResponse1xx  100-199 response codes in the last time period (delta)
     * @param cResponse2xx  200-299 response codes in the last time period (delta)
     * @param cResponse3xx  300-399 response codes in the last time period (delta)
     * @param cResponse4xx  400-499 response codes in the last time period (delta)
     * @param cResponse5xx  500-599 response codes in the last time period (delta)
     */
    public static void addValuesToHttpResponseGraph(SimpleXYChartSupport graph, long cResponse1xx,
                                                    long cResponse2xx, long cResponse3xx,
                                                    long cResponse4xx, long cResponse5xx)
        {
        graph.addValues(System.currentTimeMillis(), new long[] {cResponse1xx, cResponse2xx,
                cResponse3xx, cResponse4xx, cResponse5xx});
        }

    /**
     * Create a graph representing the bytes sent/received for proxy servers
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createProxyServerStatsGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.bytes(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_proxy_server_stats"));
        sxycd.addLineItems(getLocalText("LBL_total_bytes_sent"),
                           getLocalText("LBL_total_bytes_rcv"));

        return createChart(sxycd);
        }

    /**
     * Create a graph representing the send/ receive topic rates.
     *
     * @return a {@link SimpleXYChartSupport} representing the graph
     */
    public static SimpleXYChartSupport createTopicsRateGraph()
        {
        SimpleXYChartDescriptor sxycd = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        sxycd.setChartTitle(Localization.getLocalText("GRPH_topics_stats"));
        sxycd.addLineItems(getLocalText("LBL_publisher_sends"),
                           getLocalText("LBL_subscriber_receives"));
        return createChart(sxycd);
        }

    /**
     * Add values to the proxy server stats graph.
     *
     * @param graph       {@link SimpleXYChartSupport} to add values to
     * @param cBytesSent  number of bytes sent for time period
     * @param cBytesRec   number of bytes received for time period
     */
    public static void addValuesToProxyServerStatsGraph(SimpleXYChartSupport graph, long cBytesSent, long cBytesRec)
        {
        graph.addValues(System.currentTimeMillis(), new long[] { cBytesSent, cBytesRec });
        }

    /**
     * Add values to the topics rate graph.
     *
     * @param graph       {@link SimpleXYChartSupport} to add values to
     * @param cBytesSent  number of publisher sends for time period
     * @param cBytesRec   number of subscriber receives for time period
     */
    public static void addValuesToTopicsRateGraph(SimpleXYChartSupport graph, long cBytesSent, long cBytesRec)
        {
        graph.addValues(System.currentTimeMillis(), new long[] { cBytesSent, cBytesRec });
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Create a chart and set any defaults.
     *
     * @param sxycd  {@link SimpleXYChartDescriptor} to base chart upon
     *
     * @return the created chart
     */
    private static SimpleXYChartSupport createChart(SimpleXYChartDescriptor sxycd)
        {
        SimpleXYChartSupport chartSupport = ChartFactory.createSimpleXYChart(sxycd);
        chartSupport.setZoomingEnabled(GlobalPreferences.sharedInstance().isZoomEnabled());

        return chartSupport;
        }

    // ----- constants ------------------------------------------------------

    /**
     * static KB value.
     */
    public static final int KB = 1024;

    /**
     * Static MB value.
     */
    public static final int MB = KB * KB;

    /**
     * Static GB value.
     */
    public static final int GB = KB * KB;

    /**
     * Static TB value.
     */
    public static final long TB = (long) MB * MB;

    /**
     * The number of values to hold for an individual graph. The default value of
     * 50000 represents approximately 12 hours of data (but may vary). Setting this
     * to a higher value will ultimately consume more memory for each graph.  <br>
     * To change the value use: -J-Dcoherence.plugin.visualvm.values.limit=200000 on
     * jvisualvm command line.
     */
    public static final int VALUES_LIMIT = Integer.getInteger("coherence.plugin.visualvm.values.limit", 50000);
    }