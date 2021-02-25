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

import java.util.Map;

import java.util.Map.Entry;
import java.util.Objects;


/**
 * The representation of a two value {@link Tuple}.
 *
 * @author tam/bko  2013.11.14
 * @since  12.1.3
 *
 * @param <X>  type of X value
 * @param <Y>  type of Y value
 */
public class Pair<X, Y>
        implements Tuple, Comparable<Pair<X, Y>>
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Default constructor.
     */
    public Pair()
        {
        }

    /**
     * Create a Pair given a {@link Entry}
     *
     * @param entry {@link Entry} of X,Y
     */
    public Pair(Map.Entry<X, Y> entry)
        {
        this.x = entry.getKey();
        this.y = entry.getValue();
        }

    /**
     * Create a Pair given a X,Y value.
     *
     * @param x X value
     * @param y Y value
     */
    public Pair(X x, Y y)
        {
        this.x = x;
        this.y = y;
        }

    // ----- Tuple methods --------------------------------------------------

    /**
     * Get the entry given an index
     *
     * @param index the index to return
     *
     * @return the entry given the index.
     *
     * @throws IndexOutOfBoundsException if the entry is outside the max value
     */
    public Object get(int index)
            throws IndexOutOfBoundsException
        {
        if (index == 0)
            {
            return x;
            }
        else if (index == 1)
            {
            return y;
            }
        else
            {
            throw new IndexOutOfBoundsException(String.format("%d is an illegal index for a Pair", index));
            }
        }

    /**
     * Returns the size of the Pair.
     */
    public int size()
        {
        return 2;
        }

    // ----- accessors ------------------------------------------------------

    /**
     * Returns the X value of the Pair.
     *
     * @return the X value of the Pair
     */
    public X getX()
        {
        return x;
        }

    /**
     * Returns the Y value of the Pair.
     *
     * @return the Y value of the Pair
     */
    public Y getY()
        {
        return y;
        }

    // ----- Comparable methods ---------------------------------------------

    /**
     * {@inheritDoc}
     */
    public int compareTo(Pair<X, Y> pair)
        {
        if (pair.getX().equals(this.getX()))
            {
            return ((Comparable) this.getY()).compareTo(pair.getY());
            }
        else
            {
            return ((Comparable) this.getX()).compareTo(pair.getX());
            }

        }

    // ----- Object methods --------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
        {
        return String.format("%s / %s", x == null ? "null" : x.toString(), y == null ? "null" : y.toString());
        }

    @Override
    public boolean equals(Object o)
        {
        if (this == o)
            {
            return true;
            }
        if (o == null || getClass() != o.getClass())
            {
            return false;
            }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(x, pair.x) && Objects.equals(y, pair.y);
        }

    @Override
    public int hashCode()
        {
        return Objects.hash(x, y);
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = 2463266056607221797L;

    // ----- data members ---------------------------------------------------

    /**
     * Value for X.
     */
    private X x;

    /**
     * Value for Y.
     */
    private Y y;
    }
