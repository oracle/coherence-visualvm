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

package com.oracle.coherence.plugin.visualvm.helper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * Utility class for querying JMX.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class JMXUtils
    {

    // ----- helpers --------------------------------------------------------

    /**
     * Retrieves a number of attributes from an MBean server query
     *
     * @param server       the {@link MBeanServerConnection} to query
     * @param sQuery       the query to run
     * @param aFields      the array of attributes to retrieve
     *
     * @return a {@link Set} of Object[] of results
     */
    public static Set<Object[]> runJMXQuery(MBeanServerConnection server, String sQuery, JMXField[] aFields)
        {
        if (aFields == null || aFields.length == 0)
            {
            throw new IllegalArgumentException("Please supply at least one JMXField");
            }

        Set<Object[]> setResults = new HashSet<Object[]>();

        try
            {
            Set<ObjectName> resultSet = server.queryNames(new ObjectName(sQuery), null);

            // loop through each result
            for (Iterator<ObjectName> restulsIter = resultSet.iterator(); restulsIter.hasNext(); )
                {
                ObjectName objectName    = restulsIter.next();
                Object[]   aoQueryResult = new Object[aFields.length];
                int        i             = 0;

                // go through each attribute and retrieve
                for (JMXField jmxField : aFields)
                    {
                    if (jmxField instanceof Attribute)
                        {
                        aoQueryResult[i++] = server.getAttribute(objectName, jmxField.getName());
                        }
                    else
                        {
                        aoQueryResult[i++] = objectName.getKeyProperty(jmxField.getName());
                        }

                    }

                setResults.add(aoQueryResult);

                }
            }
        catch (Exception e)
            {
            throw new RuntimeException("Unable to run query " + sQuery + " for fields " + aFields + " : "
                    + e.getMessage());
            }

        return setResults;
        }

    /**
     * Run a JMX query expecting a single attribute
     *
     * @param server       the {@link MBeanServerConnection} to query
     * @param sQuery       the query to run
     * @param jmxField     the {@link JMXField} to retrieve
     *
     * @return a single {@link Object} result
     */
    public static Object runJMXQuerySingleResult(MBeanServerConnection server, String sQuery, JMXField jmxField)
        {
        Set<Object[]> setResults = runJMXQuery(server, sQuery, new JMXField[] {jmxField});

        if (setResults.size() != 1)
            {
            throw new RuntimeException("Returned " + setResults.size() + " results instead of 1 for query " + sQuery);
            }

        Object[] aoResult = setResults.iterator().next();

        return (aoResult == null ? null : aoResult[0]);
        }

    /**
     * Return the value for the attribute name in the given AttributeList.
     *
     * @param listAttr  an AttributeList
     * @param sName     the attribute name
     *
     * @return  the value of the attribute
     */
    public static String getAttributeValueAsString(AttributeList listAttr, String sName)
        {
        for (int i = 0; i < listAttr.size() ; i++)
            {
            javax.management.Attribute attr = (javax.management.Attribute) listAttr.get(i);
            if (sName.equals(attr.getName()))
                {
                return (attr.getValue() == null ? "" : attr.getValue().toString());
                }
            }

        return null;
        }

    /**
     * Return the value for the attribute name in the given AttributeList.
     *
     * @param listAttr  an AttributeList
     * @param sName     the attribute name
     *
     * @return  the value of the attribute
     */
    public static Object getAttributeValue(AttributeList listAttr, String sName)
        {
        for (int i = 0; i < listAttr.size() ; i++)
            {
            javax.management.Attribute attr = (javax.management.Attribute) listAttr.get(i);
            if (sName.equals(attr.getName()))
                {
                return attr.getValue();
                }
            }

        return null;
        }

    /**
     * A request for an attribute value.
     */
    public static class Attribute
            extends JMXField
        {
        /**
         * Construct a new attribute request for a given name.
         *
         * @param sName the name of the attribute
         */
        public Attribute(String sName)
            {
            super(sName);
            }
        }

    /**
     * A representation of a JMX Field request which can be an Attribute or Key.
     */
    public static abstract class JMXField
        {
        /**
         * Construct a new field request for a given name.
         *
         * @param sName the name of the field
         */
        public JMXField(String sName)
            {
            this.sName = sName;
            }

        /**
         * Return the name of the field requested.
         *
         * @return the name of the field requested
         */
        public String getName()
            {
            return this.sName;
            }

        private String sName;
        }

    /**
     * A request for a key value.
     */
    public static class Key
            extends JMXField
        {
        /**
         * Construct a new Key request for a given name.
         *
         * @param sName the name of the key
         */
        public Key(String sName)
            {
            super(sName);
            }
        }
    }
