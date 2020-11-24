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
import com.oracle.coherence.plugin.visualvm.panel.CoherencePersistencePanel;

import java.util.List;
import java.util.Map;

import java.util.SortedMap;

/**
 * A class to hold persistence notifications data.<br>
 * Note: this class is not populated via normal JMX queries in {@link VisualVMModel}
 * but is populated via JMX Subscriptions in {@link CoherencePersistencePanel}.
 *
 * @author tam  2015.03.01
 * @since  12.2.1
 */
public class PersistenceNotificationsData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create PersistenceData passing in the number of columns.
     */
    public PersistenceNotificationsData()
        {
        super(MESSAGE + 1);
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
        return null;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 7766559773272105147L;

    /**
     * Array index for service name.
     */
    public static final int SEQUENCE = 0;

    /**
     * Array index for service name.
     */
    public static final int SERVICE = 1;

    /**
     * Array index for operation.
     */
    public static final int OPERATION = 2;

    /**
     * Array index for start time.
     */
    public static final int START_TIME = 3;

    /**
     * Array index for end time.
     */
    public static final int END_TIME = 4;

    /**
     * Array index for duration.
     */
    public static final int DURATION = 5;

    /**
     * Array index for message.
     */
    public static final int MESSAGE = 6;
    }