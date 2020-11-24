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

import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import javax.management.AttributeList;
import javax.management.ObjectName;

import static com.oracle.coherence.plugin.visualvm.helper.JMXUtils.getAttributeValueAsString;

/**
 * A class to hold detailed front cache data.
 *
 * @author cl  2015.02.10
 * @since  12.2.1
 */
public class CacheFrontDetailData
    extends CacheDetailData
    {

    /**
     * Create CacheFrontDetailData passing in the cache tier type.
     */
    public CacheFrontDetailData()
        {
        super(CacheType.FRONT_TIER, CacheFrontDetailData.HIT_PROBABILITY + 1);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data populateData(RequestSender sender, ObjectName objName)
        throws Exception
        {
        Data data = new CacheFrontDetailData();

        AttributeList listAttr = sender.getAttributes(objName,
            new String[]{ ATTR_SIZE, ATTR_UNITS, ATTR_UNIT_FACTOR, ATTR_CACHE_HITS,
                          ATTR_CACHE_MISSES, ATTR_TOTAL_GETS, ATTR_TOTAL_PUTS,
                          ATTR_HIT_PROBABILITY });

        data.setColumn(CacheFrontDetailData.NODE_ID, new Integer(objName.getKeyProperty("nodeId")));

        data.setColumn(CacheFrontDetailData.SIZE, Integer.parseInt(getAttributeValueAsString(listAttr, ATTR_SIZE)));
        data.setColumn(CacheFrontDetailData.CACHE_HITS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_CACHE_HITS)));
        data.setColumn(CacheFrontDetailData.CACHE_MISSES, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_CACHE_MISSES)));
        data.setColumn(CacheFrontDetailData.TOTAL_GETS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_GETS)));
        data.setColumn(CacheFrontDetailData.TOTAL_PUTS, Long.parseLong(getAttributeValueAsString(listAttr, ATTR_TOTAL_PUTS)));
        data.setColumn(CacheFrontDetailData.HIT_PROBABILITY, Double.parseDouble(getAttributeValueAsString(listAttr, ATTR_HIT_PROBABILITY)));

        return data;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data populateData(Object[] aoColumns)
        {
        Data data = new CacheFrontDetailData();

        data.setColumn(CacheFrontDetailData.NODE_ID, new Integer(getNumberValue(aoColumns[4].toString())));
        data.setColumn(CacheFrontDetailData.SIZE, new Integer(getNumberValue(aoColumns[5].toString())));
        data.setColumn(CacheFrontDetailData.TOTAL_GETS, new Long(getNumberValue(aoColumns[7].toString())));
        data.setColumn(CacheFrontDetailData.TOTAL_PUTS, new Long(getNumberValue(aoColumns[8].toString())));
        data.setColumn(CacheFrontDetailData.CACHE_HITS, new Long(getNumberValue(aoColumns[9].toString())));
        data.setColumn(CacheFrontDetailData.CACHE_MISSES, new Integer(getNumberValue(aoColumns[10].toString())));
        data.setColumn(CacheFrontDetailData.HIT_PROBABILITY, new Float(aoColumns[11].toString()));

        return data;
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -4275691531231514047L;

    /**
     * Array index for node id.
     */
    public static final int NODE_ID = 0;

    /**
     * Array index for size.
     */
    public static final int SIZE = 1;

    /**
     * Array index for total gets.
     */
    public static final int TOTAL_GETS = 2;

    /**
     * Array index for total puts.
     */
    public static final int TOTAL_PUTS = 3;

    /**
     * Array index for cache hits.
     */
    public static final int CACHE_HITS = 4;

    /**
     * Array index for cache misses.
     */
    public static final int CACHE_MISSES = 5;

    /**
     * Array index for hit probability.
     */
    public static final int HIT_PROBABILITY = 6;
    }