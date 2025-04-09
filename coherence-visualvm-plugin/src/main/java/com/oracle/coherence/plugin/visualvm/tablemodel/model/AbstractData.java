/*
 * Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.JMXRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.io.Serializable;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

/**
 * An abstract representation of data to be shown on tables and graphs.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public abstract class AbstractData
        implements Data, DataRetriever, Serializable
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Creates a new instance and initializes the required values.
     *
     * @param nColumnCount number of columns
     */
    public AbstractData(int nColumnCount)
        {
        f_nColumnCount  = nColumnCount;
        m_oColumnValues = new Object[nColumnCount];
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public SortedMap<Object, Data> getReporterData(TabularData reportData, VisualVMModel model)
        {
        SortedMap<Object, Data> mapData = new TreeMap<Object, Data>();
        Set<?>                  setKeys = reportData.keySet();
        Data                    data    = null;

        preProcessReporterData(model);

        if (setKeys.size() > 0)
            {
            // loop through each key, which are the rows of data
            for (Object oKey : setKeys)
                {
                // get the columns as an array
                Object[] aoColumns = ((Collection<Object>) oKey).toArray();

                data = processReporterData(aoColumns, model);

                // save the newly created entry if it exists
                if (data != null)
                    {
                    mapData.put(data.getColumn(0), data);
                    }
                }
            }

        return postProcessReporterData(mapData, model);
        }

    /**
     * Perform any pre-processing before reporter is called.
     *
     * @param  model   the {@link VisualVMModel} to use
     */
    protected void preProcessReporterData(VisualVMModel model)
        {
        }

    /**
     * Returns the string representation of a {@link JsonNode} and key
     * @param node {@link JsonNode} to inspect
     * @param sKey key to retrieve
     * @return the string representation
     */
    protected String getSafeValue(JsonNode node, String sKey)
        {
        return getSafeValue(node, sKey, null);
        }

    /**
     * Returns the string representation of a {@link JsonNode} and key
     * @param node {@link JsonNode} to inspect
     * @param sKey key to retrieve
     * @param sDefault default value
     *
     * @return the string representation
     */
    protected String getSafeValue(JsonNode node, String sKey, String sDefault)
        {
        JsonNode jsonNode = node.get(sKey);
        return jsonNode != null ? jsonNode.asText() : sDefault;
        }

    /**
     * {@inheritDoc}
     */
    public String preProcessReporterXML(VisualVMModel model, String sReporterXML)
        {
        // default is to leave as is
        return sReporterXML;
        }

    /**
     * Perform any post-processing of the generated data in case some manipulation
     * cannot be carried out by the reporter. This method should be overridden
     * in the specific implementation of AbstractData.
     *
     * @param  mapData generated {@link SortedMap} of the data from the reporter
     * @param  model   the {@link VisualVMModel} to use
     *
     * @return modified data
     */
    protected SortedMap<Object, Data> postProcessReporterData(SortedMap<Object, Data> mapData, VisualVMModel model)
        {
        // default is to return the data as is
        return mapData;
        }

    @Override
    public SortedMap<Object, Data> getAggregatedDataUsingReport(VisualVMModel model,
                                                                RequestSender requestSender,
                                                                String        sReportXML)
        {
        SortedMap<Object, Data> mapCollectedData = null;

        JMXRequestSender jmxRequestSender = (JMXRequestSender) requestSender;
        // carry out any parameter substitution or pre-processing of reporter XML
        sReportXML = preProcessReporterXML(model, sReportXML);

        if (m_sReporterLocation == null)
            {
            // reporter location has not been defined, so lets find it
            m_sReporterLocation = jmxRequestSender.getReporterObjectName(jmxRequestSender.getLocalMemberId());
            }

        if (m_sReporterLocation != null)
            {
            try
                {
                // run the given report
                TabularData reportData =
                        (TabularData) jmxRequestSender.invoke(new ObjectName(m_sReporterLocation),
                                "runTabularReport", new Object[]{sReportXML}, new String[]{"java.lang.String"});

                if (reportData != null)
                    {
                    // now that we have output from the reporter, call the
                    // appropriate method in the class to populate
                    mapCollectedData = getReporterData(reportData, model);
                    }
                else
                    {
                    // report data is null - this can occur when the reporter has not been correctly started
                    // due to a configuration error. Check that the reporter state == "Error" and
                    // ensure we raise and exception to fall back tp JMX if it is
                    String sState = jmxRequestSender.getAttribute(new ObjectName(m_sReporterLocation), "State");
                    if ("Error".equalsIgnoreCase(sState))
                        {
                        throw new ReporterException("Reporter returned null and may not have been started correctly. Class=" +
                                                    this.getClass());
                        }
                    }
                    // reporter state != "Error", so this may be a valid null result
                }
            catch (Exception e)
                {
                String sError = Localization.getLocalText("ERR_error_running_report",
                        sReportXML, this.getClass().getCanonicalName(), e.getMessage());

                LOGGER.log(Level.WARNING, sError, e);

                model.setReporterAvailable(false);

                // this exception is thrown, so we can catch above and re-run the report
                // using the standard way
                throw new ReporterException("Error running report", e);
                }
            }
        return mapCollectedData;
        }

    // ----- Data methods ---------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public Object getColumn(int nColumn)
        {
        if (nColumn > f_nColumnCount - 1)
            {
            throw new IllegalArgumentException("Invalid column index " + nColumn);
            }

        return m_oColumnValues[nColumn];
        }

    /**
     * {@inheritDoc}
     */
    public void setColumn(int nColumn, Object oValue)
        {
        if (nColumn > f_nColumnCount - 1)
            {
            throw new IllegalArgumentException("Invalid column index nColumn=" + nColumn + " , nColumnCount="
                                               + f_nColumnCount + ", class=" + this.getClass().getName() + "\n"
                                               + this.toString());
            }

        m_oColumnValues[nColumn] = oValue;
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Turn a String number value that may have decimal points to one without.
     *
     * @param sValue  the String value which may actually have decimal placed
     *                or even be in exponential notation.
     *
     * @return the stripped String value
     */
    public static String getNumberValue(String sValue)
        {
        if (sValue != null)
            {
            String s = String.format("%d", new BigDecimal(sValue).longValue());

            return s;
            }

        return null;
        }

    /**
     * Return the column count.
     *
     * @return the column count
     */
    public int getColumnCount()
        {
        return f_nColumnCount;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
        {
        StringBuffer sb = new StringBuffer("AbstractData: Class = " + this.getClass().getName());

        for (int i = 0; i < f_nColumnCount; i++)
            {
            sb.append(", Column ").append(i).append("=").append(m_oColumnValues[i] == null
                      ? "null" : m_oColumnValues[i].toString());
            }

        return sb.toString();
        }

    /**
     * Return the full service name with a domain partition prefix.
     *
     * @param sDomainPartition  domain partition for the service
     * @param sServiceName      name of the service
     *
     * @return full service name with a domain partition prefix
     */
    public static String getFullServiceName(String sDomainPartition, String sServiceName)
        {
        return sDomainPartition + SERVICE_SEP + sServiceName;
        }

    /**
     * Return the parts for a service name split by "/"
     *
     * @param sServiceName  name of the service
     *
     * @return a String[] of the parts. Only one element if no domain partition
     */
    public static String[] getServiceParts(String sServiceName)
        {
        return sServiceName.split(SERVICE_SEP);
        }

    /**
     * Return the domainPartition and service name if the raw
     * service contains it.
     *
     * @param sRawServiceName  the raw service name
     *
     * @return a String array with the domainPartition and service
     */
    public static String[] getDomainAndService(String sRawServiceName)
        {
        return new String[]{null, sRawServiceName}; // Domain partition no longer supported so will always be null
        }

    /**
     * Get the first member of an array, if the provided field is an array. The default value in case
     * if not an array or null element is zero.
     *
     * @param nodeJson        the parent JSON node
     * @param sAttributeName  the attribute name
     *
     * @return the first member of the array
     */
    protected String getFirstMemberOfArray(JsonNode nodeJson, String sAttributeName)
        {
        JsonNode jsonNode = nodeJson.get(sAttributeName);
        return jsonNode == null || !jsonNode.isArray()
                ? (0 + "")
                : jsonNode.get(0).asText();
        }

    /**
     * Return a child valid from a {@link JsonNode}.
     * @param sChildFieldName child field name
     * @param sFieldName      field name
     * @param rootNode        {@link JsonNode}
     * @return  a child valid
     */
    protected String getChildValue(String sChildFieldName, String sFieldName, JsonNode rootNode)
        {
        JsonNode node = rootNode.get(sFieldName);
        if (node != null && node.isContainerNode())
            {
            JsonNode jsonNode = node.get(sChildFieldName);
            return jsonNode != null ? jsonNode.asText(null) : null;
            }
        return null;
        }

    /**
     * Escape a string, by replacing $ with \$, so it can be used in replaceAll without
     * causing an Illegal group reference.
     *
     * @param sValue value to escape
     * @return escaped value
     */
    protected static String escape(String sValue)
        {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sValue.length() ; i++)
            {
            sb.append(sValue.charAt(i) == '$' ? "\\$" : sValue.charAt(i));
             }
        return sb.toString();
        }

    // ----- inner classes --------------------------------------------------

    /**
     * Indicates there was a reporter exception.
     */
    public static class ReporterException extends RuntimeException
        {
        /**
         * Construct a reporter exception with a message.
         * @param sMessage exception message.
         */
        public ReporterException(String sMessage)
            {
            super(sMessage);
            }

        /**
         * Construct a reporter exception with a message and {@link Throwable}.
         * @param sMessage exception message.
         * @param t {@link Throwable}.
         */
        public ReporterException(String sMessage, Throwable t)
            {
            super(sMessage, t);
            }
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 1803170898872716122L;

    /**
     * Separator for the domain partition and service name.
     */
    public static final String SERVICE_SEP = "/";

    // ----- data members ---------------------------------------------------

    /**
     * The array of objects (statistics) for this instance.
     */
    protected Object[] m_oColumnValues = null;

    /**
     * The column count.
     */
    protected final int f_nColumnCount;


    /**
     * The report object location to use to run reports.
     */
    private static String m_sReporterLocation = null;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractData.class.getName());
    }
