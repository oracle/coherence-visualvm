/*
 *  Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package com.oracle.coherence.plugin.visualvm.tracer;

import org.graalvm.visualvm.modules.tracer.ItemValueFormatter;
import java.text.NumberFormat;

/**
 * A customer formatter.
 *
 * @author tam 2024.03.04
 */
public class CustomFormatter
        extends ItemValueFormatter {

    // ----- constructors ---------------------------------------------------

    public CustomFormatter(int factor, String units)
        {
        this.m_nFactor = factor;
        this.m_sUnits  = units;
        }

    @Override
    public String formatValue(long value, int format)
        {
        return FORMAT.format(value / (double) this.m_nFactor);
        }

    @Override
    public String getUnits(int format)
        {
        return this.m_sUnits;
        }

    // ----- constants ------------------------------------------------------

    private static final NumberFormat FORMAT = NumberFormat.getInstance();
    
    // ----- data members ---------------------------------------------------

    private final int    m_nFactor;
    private final String m_sUnits;

    }
