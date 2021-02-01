/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm;

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.application.jvm.Jvm;
import org.graalvm.visualvm.application.type.ApplicationType;
import org.graalvm.visualvm.application.type.ApplicationTypeFactory;
import org.graalvm.visualvm.application.type.MainClassApplicationTypeFactory;

/**
 * An implementation of a {@link MainClassApplicationTypeFactory} to set the
 * icon and description for a Coherence application.
 */
public class CoherenceApplicationTypeFactory
        extends MainClassApplicationTypeFactory
    {

    // ----- CoherenceApplicationTypeFactory methods  -----------------------

    /**
     * Initialize.
     */
    public static void initialize()
        {
        ApplicationTypeFactory.getDefault().registerProvider(INSTANCE);
        }

    /**
     * Shutdown.
     */
    public static void shutdown()
        {
        ApplicationTypeFactory.getDefault().unregisterProvider(INSTANCE);
        }

    // ----- MainClassApplicationTypeFactory methods  -----------------------

    @Override
    public ApplicationType createApplicationTypeFor(Application app, Jvm jvm, String sMainClass)
        {
        if (CoherenceApplicationType.f_mapClasses.containsKey(sMainClass))
            {
            return new CoherenceApplicationType(app, jvm, sMainClass);
            }

        return null;
        }

    // ----- constants ------------------------------------------------------

    /**
     * Factory instance.
     */
    private final static CoherenceApplicationTypeFactory INSTANCE = new CoherenceApplicationTypeFactory();
    }
