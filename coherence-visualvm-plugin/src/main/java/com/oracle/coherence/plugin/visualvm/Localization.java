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
package com.oracle.coherence.plugin.visualvm;

import org.openide.util.NbBundle;

/**
 * Holds methods for localization of messages.  The default English/US
 * messages are store in Bundle.properties.  The localization of this
 * is done using by specifying properties as such:
 * <br>
 * <ul>
 *   <li>Bundle_[language]_[country].properties o</li>
 *   <li>Bundle_[language].properties<li>
 * </li>
 * </ul>
 * For example:<br>
 * <ul>
 *   <li>Bundle_fr.properties - French</li>
 *   <li>Bundle_fr_CA.properties - French Canadian</li>
 *   <li>Bundle_ja.properties - Japanese</li>
 * </ul>
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class Localization
    {

    // ----- helpers --------------------------------------------------------

    /**
     * Return a localized version of text obtained from Bundle.properties by
     * default or localized bundle as described above.
     * <br>
     * Example:
     * <pre>
     * String sLabel = Localization.getLocalText("LBL_cluster_name");
     * </pre>
     * Bundle.properties should contain a line with the text:<br>
     * <pre>
     * LBL_cluster_name=Cluster Name:
     * </pre>
     *
     * @param sKey the key to obtain the localization for
     *
     * @return the localized message
     */
    public static String getLocalText(String sKey)
        {
        return NbBundle.getMessage(Localization.class, sKey);
        }

    /**
     * Return a localized version of text obtained from Bundle.properties by
     * default or localized bundle as described above.
     *
     * Example:
     * <pre>
     * String sLabel = Localization.getLocalText("MSG_file_not_found", new String[] {"tim.txt"});
     * </pre>
     * Bundle.properties should contain a line with the text:<br>
     * <pre>
     * MSG_file_not_found=The file {0} was not found.
     * </pre>
     *
     * @param sKey     the key to obtain the localization for
     * @param asParams the array of parameters to substitue
     *
     * @return the localized message
     */
    public static String getLocalText(String sKey, String asParams[])
        {
        return NbBundle.getMessage(Localization.class, sKey, asParams);
        }
    }
