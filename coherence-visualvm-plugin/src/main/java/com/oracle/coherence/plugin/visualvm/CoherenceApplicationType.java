/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.application.jvm.Jvm;
import org.graalvm.visualvm.application.type.ApplicationType;
import java.awt.Image;
import org.openide.util.ImageUtilities;

import static com.oracle.coherence.plugin.visualvm.VisualVMView.IMAGE_PATH;


/**
 * Defines an {@link ApplicationType} for Coherence applications.
 *
 * @author Tim Middleon 2021.02.01
 */
public class CoherenceApplicationType
        extends ApplicationType
    {
    // ----- constructors ----------------------------------------------------

    /**
     * Construct a {@link CoherenceApplicationType}
     * @param app         {@link Application}
     * @param jvm         {@link Jvm}
     * @param sMainClass  main classpath
     */
    public CoherenceApplicationType(Application app, Jvm jvm, String sMainClass)
        {
        String   sDescription = f_mapClasses.get(sMainClass);
        String[] asParts      = sMainClass.split("\\.");

        this.f_sName       = "Coherence " + asParts[asParts.length - 1];
        this.f_description = sDescription
                             + " (" + sMainClass + ")"
                             + "\nCommand Line:"  + jvm.getJvmArgs();
        this.f_sIconPath = IMAGE_PATH;
        this.f_app = app;
        }

    // ----- accessors ------------------------------------------------------
    
    @Override
    public String getName()
        {
        return f_sName;
        }

    @Override
    public String getVersion()
        {
        return null;
        }

    @Override
    public String getDescription()
        {
        return f_description;
        }

    @Override
    public Image getIcon()
        {
        return ImageUtilities.loadImage(f_sIconPath, true);
        }

    // ----- data members ----------------------------------------------------

    /**
     * Name of the application.
     */
    private final String f_sName;

    /**
     * Description.
     */
    private final String f_description;

    /**
     * Icon path.
     */
    private final String f_sIconPath;

    /**
     * Application.
     */
    private final Application f_app;

    /**
     * {@link Map} of possible classes to detect.
     */
    static final Map<String, String> f_mapClasses = new HashMap<>();

    static
        {
        f_mapClasses.put("com.tangosol.net.DefaultCacheServer", "Default Cache Server");
        f_mapClasses.put("com.tangosol.net.CacheFactory", "CacheFactory");
        f_mapClasses.put("com.tangosol.net.Coherence", "Coherence Bootstrap");
        f_mapClasses.put("com.tangosol.coherence.dslquery", "CohQL Client");
        }
    }
