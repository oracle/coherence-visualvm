/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A class to hold Federated Destination data.
 *
 * @author bb  2014.01.29
 *
 * @since  12.2.1
 */
public abstract class FederationData
        extends AbstractData
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create ServiceData passing in the number of columns.
     */
    public FederationData()
        {
        super(Column.values().length);
        }

    // ----- DataRetriever methods ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public List<Map.Entry<Object, Data>> getJMXData(RequestSender requestSender, VisualVMModel model)
        {
        return null;
        }


    /**
     * Defines the data collected from destination MBeans, origin MBeans and aggregations.
     */
    public enum Column
        {
        KEY(0),
        SERVICE(1),
        PARTICIPANT(2),
        STATUS(3),
        // column is not sequential because it is coming from origin mbean while the above two
        // are coming from destination mbean.
        TOTAL_BYTES_SENT(4),
        TOTAL_BYTES_RECEIVED(3),
        TOTAL_MSGS_SENT(5),
        TOTAL_MSGS_RECEIVED(4);

        Column(int nCol)
            {
            f_nCol = nCol;
            }

        /**
         * Returns the column number for this enum.
         *
         * @return the column number
         */
        public int getColumn()
            {
            return f_nCol;
            }

        /**
         * The column number associates with thw enum.
         */
        protected final int f_nCol;
        }

    /**
     * Retrieve the {@link Set} of federated services.
     * @param requestSender {@link HttpRequestSender}
     * @return the {@link Set} of federated services
     * @throws Exception in case of errors
     */
    protected Set<String> retrieveFederatedServices(HttpRequestSender requestSender) throws Exception
        {
        Set<String> setServices       = new HashSet<>();
        JsonNode    allStorageMembers = requestSender.getAllStorageMembers();
        JsonNode    serviceItemsNode  = allStorageMembers.get("items");

        if (serviceItemsNode != null && serviceItemsNode.isArray())
            {
            for (int i = 0; i < ((ArrayNode) serviceItemsNode).size(); i++)
                {
                JsonNode details = serviceItemsNode.get(i);
                String sServiceName = details.get("name").asText();
                JsonNode domainPartition = details.get("domainPartition");
                String sDomainPartition = domainPartition == null ? null : domainPartition.asText();
                String sType = details.get("type").asText();

                String sService = sDomainPartition == null ? sServiceName : sDomainPartition + "/" +  sServiceName;

                if (!setServices.contains(sService) && "FederatedCache".equals(sType))
                    {
                    setServices.add(sService);
                    }
                }
            }
         return setServices;
         }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -5166985357635016554L;
    }
