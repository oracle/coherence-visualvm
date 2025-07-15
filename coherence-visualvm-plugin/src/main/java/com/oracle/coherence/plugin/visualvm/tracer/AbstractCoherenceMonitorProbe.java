/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.coherence.plugin.visualvm.tracer;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceMemberData;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.graalvm.visualvm.application.Application;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import org.graalvm.visualvm.modules.tracer.ProbeItemDescriptor;
import org.graalvm.visualvm.modules.tracer.TracerProbe;

import org.openide.util.ImageUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.oracle.coherence.plugin.visualvm.VisualVMView.IMAGE_PATH;

/**
 * Abstract implementations of a Coherence {@link TracerProbe}.
 * @author tam 2024.03.03
 */
public abstract class AbstractCoherenceMonitorProbe
        extends TracerProbe<Application>
    {

    // ----- constructors ---------------------------------------------------

        /**
         * An abstract implementation of a {link @TracerProbe}.
         *
         * @param valuesCount      the number of values to return
         * @param itemDescriptors  the item description
         * @param resolver         the {@link MonitoredDataResolver}
         */
    protected AbstractCoherenceMonitorProbe(int valuesCount, ProbeItemDescriptor[] itemDescriptors,
                                  MonitoredDataResolver resolver)
        {
        super(itemDescriptors);
        this.f_nValuesCount = valuesCount;
        this.f_resolver     = resolver;
        }

    /**
     * Return the values for the given timestamp.
     * @param timestamp timestamp to return values for
     * @return values for the given timestamp
     */
    public final synchronized long[] getItemValues(long timestamp)
        {
        VisualVMModel model = f_resolver.getMonitoredData();
        if (model != null)
            {
            return getValues(model);
            }
        
        long[] noData = new long[f_nValuesCount];
        Arrays.fill(noData, ProbeItemDescriptor.VALUE_UNDEFINED);
        return noData;
        }

        /**
         * Returns the monitors string.
         * @param sKey key for the label
         * @return the monitors string
         */
    protected static String getMonitorsString(String sKey)
        {
        return Localization.getLocalText("LBL_monitors") + " " + Localization.getLocalText(sKey);
        }

    /**
     * Returns the values for the {@link VisualVMModel}.
     * @param model {@link VisualVMModel}
     * @return the values
     */
    protected abstract long[] getValues(VisualVMModel model);

    /**
     * An interface defining monitored data resolvers.
     */
    public interface MonitoredDataResolver
        {
        /**
         * return the monitored data.
         * @return the monitored data.
         */
        VisualVMModel getMonitoredData();
        }

        /**
         * Returns a value as a long.
         * @param oValue value to parse
         * @return value as a long
         */
    protected long getValueAsLong(Object oValue)
        {
        if (oValue instanceof Long)
            {
            return (Long) oValue;
            }
        if (oValue instanceof Integer)
            {
            return ((Integer) oValue);
            }
        if (oValue instanceof String)
            {
            return Long.parseLong((String) oValue);
            }
        if (oValue instanceof Float)
            {
            return ((Float) oValue).longValue();
            }
        return 0L;
        }

    /**
     * Returns a single value from a data type.
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to query
     * @param nColumn   the column to extract
     * @param aDefault  default value
     *
     * @return the tracer result
     */
    protected long[] getSingValue(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn, long[] aDefault)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);
       if (data == null || data.isEmpty())
            {
            return aDefault;
            }

       return new long[] {getValueAsLong(data.get(0).getValue().getColumn(nColumn))};
       }

    /**
     * Returns the sum of a single value from all entries for a datatype.
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to query
     * @param nColumn   the column to extract
     * @param aDefault  default value
     *
     * @return the tracer result
     */
    protected long[] getSingValueSum(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn, long[] aDefault)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);
       long nSum = 0L;

       if (data != null && !data.isEmpty())
            {
            for (Map.Entry<Object, Data> entry : data)
                {
                nSum += getValueAsLong(entry.getValue().getColumn(nColumn));
                }
            }

       return new long[] {nSum};
       }

     /**
     * Returns the max of a single value from all entries for a datatype.
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to query
     * @param nColumn   the column to extract
     * @param aDefault  default value
     *
     * @return the tracer result
     */
    protected long[] getSingValueMax(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn, long[] aDefault)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);
       long nMax   = 0L;
       long nValue = 0L;

       if (data != null && !data.isEmpty())
            {
            for (Map.Entry<Object, Data> entry : data)
                {
                nValue = getValueAsLong(entry.getValue().getColumn(nColumn));

                if (nValue > nMax)
                    {
                    nMax = nValue;
                    }
                }
            }

       return new long[] {nMax};
       }

     /**
     * Returns the total and idle threads for the selected service.
     * @param model     the {@link VisualVMModel} to use
     *
     * @return the tracer result
     *                0: Integer - total thread count
     *                1: Integer - total idle threads
     */
    protected Object[] getSelectedServiceThreadValues(VisualVMModel model)
       {
       List<Map.Entry<Object, Data>> data = model.getData(VisualVMModel.DataType.SERVICE_DETAIL);
       int nTotalThreadCount = 0;
       int nTotalIdleThreads = 0;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               nTotalThreadCount += (Integer) entry.getValue().getColumn(ServiceMemberData.THREAD_COUNT);
               nTotalIdleThreads += (Integer) entry.getValue().getColumn(ServiceMemberData.THREAD_IDLE_COUNT);
               }
           }

       return new Object[] {nTotalThreadCount, nTotalIdleThreads};
       }

     /**
     * Returns the max and average value for a selected service.
     *
     * @param model     the {@link VisualVMModel} to use
     * @param nColumn   the column to extract
     *
     * @return the tracer result (multiplied byt 1000)
     *                0: Long - Max
     *                1: Long - Average
     */
    protected Long[] getSelectedServiceMaxAndAverage(VisualVMModel model, int nColumn)
       {
       List<Map.Entry<Object, Data>> data = model.getData(VisualVMModel.DataType.SERVICE_DETAIL);

       long nTotal   = 0L;
       long nMax     = 0L;
       long nCount   = 0L;
       long nCurrent = 0L;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               nCurrent = (long) ((Float) entry.getValue().getColumn(nColumn) * 1000L);
               if (nCurrent <= 0)
                   {
                   // exclude negative values
                   continue;
                   }
               nCount++;
               nTotal  += nCurrent;
               if (nCurrent > nMax)
                   {
                   nMax = nCurrent;
                   }
               }
           }

       return nCount == 0L ? new Long[] {0L,0L} : new Long[] {nMax, nTotal / nCount};
       }

    /**
     * Returns the total value for a selected service.
     *
     * @param model     the {@link VisualVMModel} to use
     * @param nColumn   the column to extract
     *
     * @return the tracer result
     */
    protected long getSelectedServiceSumInteger(VisualVMModel model, int nColumn)
       {
       List<Map.Entry<Object, Data>> data = model.getData(VisualVMModel.DataType.SERVICE_DETAIL);

       int nTotal = 0;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               nTotal += (Integer) entry.getValue().getColumn(nColumn);
               }
           }

       return nTotal;
       }

    /**
     * Returns the total value for a selected cache and datatype.
     *
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to extract from
     * @param nColumn   the column to extract
     *
     * @return the tracer result
     */
    protected long getSelectedCacheSum(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);

       long nTotal = 0L;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               nTotal += getValueAsLong(entry.getValue().getColumn(nColumn));
               }
           }

       return nTotal;
       }

    /**
     * Returns the max value for a selected cache and datatype.
     *
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to extract from
     * @param nColumn   the column to extract
     *
     * @return the tracer result
     */
    protected long getSelectedCacheMax(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);

       long nMax = 0L;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               long nValue = getValueAsLong(entry.getValue().getColumn(nColumn));
               if (nValue > nMax)
                   {
                   nMax = nValue;
                   }
               }
           }

       return nMax;
       }

    /**
     * Returns the max value for a selected cache and datatype.
     *
     * @param model     the {@link VisualVMModel} to use
     * @param dataType  the {@link VisualVMModel.DataType} to extract from
     * @param nColumn   the column to extract
     *
     * @return the tracer result multiplied by 1000L
     */
    protected long getSelectedCacheAverage(VisualVMModel model, VisualVMModel.DataType dataType, int nColumn)
       {
       List<Map.Entry<Object, Data>> data = model.getData(dataType);

       long nTotal = 0L;
       long nCount = 0L;

       if (data != null && !data.isEmpty())
           {
           for (Map.Entry<Object, Data> entry : data)
               {
               nTotal += getValueAsLong(entry.getValue().getColumn(nColumn)) * 1000L;
               nCount++;
               }
           }

       return nCount == 0 ? 0L : nTotal / nCount;
       }

    // ----- data members ---------------------------------------------------

    /**
     * The resolver.
     */
    private final MonitoredDataResolver f_resolver;

    /**
     * The numer of values for this probe.
     */
    private final int f_nValuesCount;

    /**
     * An array of two zero values.
     */
    protected static final long[] ZERO_VALUES2 = new long[]{0L,0L};

    /**
     * An array of one zero values.
     */
    protected static final long[] ZERO_VALUES1 = new long[]{0L};

    // ----- constants ------------------------------------------------------

    /**
     * Coherence icon.
     */
    public static final Icon ICON = new ImageIcon(ImageUtilities.loadImage(IMAGE_PATH, true)); // NOI18N

    /**
     * Custom formatter.
     */
    public static final ItemValueFormatter CUSTOM_FORMATTER = new CustomFormatter(1000, "");
    }
