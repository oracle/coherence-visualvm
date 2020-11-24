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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import javax.swing.table.AbstractTableModel;

/**
 * An abstract implementation of {@link AbstractTableModel} which is extended
 * to provide the data for the {@link JTable} implementations.
 *
 * @param <K>  The key object for each row - the type of this will determine sorting
 * @param <V>  The value object for each row
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public abstract class AbstractCoherenceTableModel<K, V>
        extends AbstractTableModel
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Creates the mode with the string of column names.
     *
     * @param asColumnNames  the column names
     */
    public AbstractCoherenceTableModel(String[] asColumnNames)
        {
        this.f_asColumnNames = asColumnNames;
        }

    // ----- TableModel methods ---------------------------------------------

    /**
     * Returns the column count for this model.
     *
     * @return the column count for this model
     */
    public int getColumnCount()
        {
        if (f_asColumnNames == null)
            {
            throw new IllegalStateException("No definition of AbstractMeasures for this model. " + this.getClass());
            }

        return f_asColumnNames.length;
        }

    /**
     * {@inheritDoc}
     */
    public int getRowCount()
        {
        return m_dataList == null ? 0 : m_dataList.size();
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col)
        {
        if (f_asColumnNames == null)
            {
            throw new IllegalStateException("No definition of AbstractMeasures for this model. " + this.getClass());
            }

        return f_asColumnNames[col];
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col)
        {
        if (getValueAt(0, col) != null)
            {
            return getValueAt(0, col).getClass();
            }
        else
            {
            return (String.class);
            }
        }

    /**
     * {@inheritDoc}
     */
    public Object getValueAt(int row, int col)
        {
        if (m_dataList == null || m_dataList.size() == 0)
            {
            return null;
            }

        Map.Entry<K, V> entry = m_dataList.get(row);

        if (entry != null)
            {
            Object value = entry.getValue();

            if (value instanceof Data)
                {
                return ((Data) value).getColumn(col);
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

    // ----- AbstractCoherenceTableModel methods ----------------------------

    /**
     * Returns the data list for this model.
     *
     * @param dataList the data list for this model
     */
    public void setDataList(List<Map.Entry<K, V>> dataList)
        {
        this.m_dataList = dataList;
        }

    /**
     * Returns the column names for this model.
     *
     * @return the column names for this model
     */
    public String[] getColumnNames()
        {
        return f_asColumnNames;
        }


    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 8366117699998286805L;

    // ----- data members ---------------------------------------------------

    /**
     * The data list for the model.
     */
    protected List<Map.Entry<K, V>> m_dataList = Collections.emptyList();

    /**
     * The column names for the model.
     */
    protected final String[] f_asColumnNames;
    }
