/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
;

/**
 * A class to hold basic proxy connection data.
 * This class only hold data and doesn't contain any actual logic.
 *
 * @author tam  2023.02.21
 * @since  1.6.0
 */
public class ProxyConnectionData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ProxyData passing in the number of columns.
     */
    public ProxyConnectionData()
        {
        super(CLIENT_ROLE + 1);
        }

    // ----- DataRetriever methods ------------------------------------------

    @Override
    public SortedMap<Object, Data> getAggregatedDataFromHttpQuerying(VisualVMModel     model,
                                                                     HttpRequestSender requestSender)
        throws Exception
        {
        return null;
        }

    @Override
    public String getReporterReport()
        {
        return null;
        }

    @Override
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        return null;
        }

    @Override
    public Data processReporterData(Object[] aoColumns, VisualVMModel model)
        {
        return null;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1789802484301825295L;

    /**
     * Array index for UUID.
     */
    public static final int UUID = 0;

    /**
     * Array index for connection millis.
     */
    public static final int CONN_MILLIS = 1;

    /**
     * Array index for connection time
     */
    public static final int CONN_TIME = 2;

    /**
     * Array index for remove address/port.
     */
    public static final int REMOTE_ADDRESS_PORT = 3;

    /**
     * Array index for data sent.
     */
    public static final int DATA_SENT = 4;

    /**
     * Array index for data received.
     */
    public static final int DATA_REC = 5;

    /**
     * Array index for backlog.
     */
    public static final int BACKLOG = 6;

    /**
     * Array index for client process.
     */
    public static final int CLIENT_PROCESS = 7;

    /**
     * Array index for client role.
     */
    public static final int CLIENT_ROLE = 8;
    }
