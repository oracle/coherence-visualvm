/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Logger;

import com.oracle.coherence.plugin.visualvm.GlobalPreferences;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;

import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

/**
 * A class to hold basic gRPC Proxy data.
 *
 * @author tam  2022.02.04
 * @since  1.3.0
 */
public class GrpcProxyData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ExecutorData passing in the number of columns.
     */
    public GrpcProxyData()
        {
        super(TASK_BACKLOG + 1);
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
        return "reports/visualvm/grpc-proxy-stats.xml";
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        Data data = new GrpcProxyData();
        int  nStart = 1;

        data.setColumn(NODE_ID, Integer.valueOf(getNumberValue(aoColumns[nStart++].toString())));
        data.setColumn(RESPONSES_SENT_COUNT, Long.valueOf(getNumberValue(aoColumns[nStart++].toString())));
        data.setColumn(MESSAGES_RECEIVED_COUNT, Long.valueOf(getNumberValue(aoColumns[nStart++].toString())));
        data.setColumn(ERROR_REQUEST_COUNT, Long.valueOf(getNumberValue(aoColumns[nStart++].toString())));
        data.setColumn(REQUEST_DURATION_MEAN, Float.parseFloat(aoColumns[nStart++].toString()));
        data.setColumn(MESSAGE_DURATION_MEAN, Float.parseFloat(aoColumns[nStart++].toString()));
        data.setColumn(MESSAGE_DURATION_MAX, Float.parseFloat(aoColumns[nStart++].toString()));
        data.setColumn(TASK_ACTIVE_MILLIS, Long.valueOf(aoColumns[nStart++].toString()));
        data.setColumn(TASK_BACKLOG, Long.valueOf(aoColumns[nStart].toString()));

        return data;
        }

    @Override
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // get the gRPC version from the preferences
        int nVersion = GlobalPreferences.sharedInstance().getGrpcVersion();

        String sMBeanName = nVersion == 0 ? "GrpcNamedCacheProxy" : "GrpcProxy";

        return sReporterXML.replaceAll("%MBEAN%", escape(sMBeanName));
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
            throws Exception
        {
        return null;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802485401825295L;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for ResponsesSentCount.
     */
    public static final int RESPONSES_SENT_COUNT = 1;

    /**
     * Array index for MessagesReceivedCount.
     */
    public static final int MESSAGES_RECEIVED_COUNT = 2;

    /**
     * Array index for ErrorRequestCount.
     */
    public static final int ERROR_REQUEST_COUNT = 3;

    /**
     * Array index for RequestDurationMean.
     */
    public static final int REQUEST_DURATION_MEAN = 4;

    /**
     * Array index for MessageDurationMean.
     */
    public static final int MESSAGE_DURATION_MEAN = 5;

    /**
     * Array index for MessageDurationMax.
     */
    public static final int MESSAGE_DURATION_MAX = 6;

    /**
     * Array index for TaskActiveMillis.
     */
    public static final int TASK_ACTIVE_MILLIS = 7;

    /**
     * Array index for TaskBacklog.
     */
    public static final int TASK_BACKLOG = 8;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(GrpcProxyData.class.getName());
    }
