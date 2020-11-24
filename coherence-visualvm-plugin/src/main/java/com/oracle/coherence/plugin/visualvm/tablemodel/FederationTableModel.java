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

package com.oracle.coherence.plugin.visualvm.tablemodel;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationDestinationData;

import java.util.Map;

/**
 * A model for holding federation data.
 *
 * @author cl  2014.02.17
 * @since  12.2.1
 */
public class FederationTableModel
        extends AbstractCoherenceTableModel<Object, Data>
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Creates a table model with the given columns.
     *
     * @param asColumns the columns for this table model
     */
    public FederationTableModel(String[] asColumns)
        {
        super(asColumns);
        }

    /**
     * {@inheritDoc}
     */
    public Object getValueAt(int row, int col)
        {
        Map.Entry<Object, Data> entry = m_dataList.get(row);

        if (entry != null)
            {
            Object value = entry.getValue();

            if (value instanceof Data)
                {
                    Data data = (Data) value;
                switch(col)
                    {
                    case 0 : return data.getColumn(FederationDestinationData.Column.SERVICE.ordinal());
                    case 1 : return data.getColumn(FederationDestinationData.Column.PARTICIPANT.ordinal());
                    case 2 : return data.getColumn(FederationDestinationData.Column.STATUS.ordinal());
                    case 3 : return data.getColumn(FederationDestinationData.Column.TOTAL_BYTES_SENT.ordinal());
                    case 4 : return data.getColumn(FederationDestinationData.Column.TOTAL_MSGS_SENT.ordinal());
                    case 5 : return data.getColumn(FederationDestinationData.Column.TOTAL_BYTES_RECEIVED.ordinal());
                    case 6 : return data.getColumn(FederationDestinationData.Column.TOTAL_MSGS_RECEIVED.ordinal());
                    default : return null;
                    }
                }
            else
                {
                return null;
                }
            }
        else
            {
            return null;
            }
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -8299887270471460520L;
    }
