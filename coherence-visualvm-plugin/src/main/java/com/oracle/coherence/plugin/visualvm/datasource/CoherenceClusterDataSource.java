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

package com.oracle.coherence.plugin.visualvm.datasource;

import java.net.MalformedURLException;
import java.net.URL;

import org.graalvm.visualvm.core.datasource.DataSource;
import org.graalvm.visualvm.core.datasource.Storage;

/**
 * The {@link DataSource} for a single Coherence cluster.
 *
 * @author sr 12.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
public class CoherenceClusterDataSource
        extends DataSource
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create a Coherence cluster data source with the provided URL as the management URL.
     *
     * @param  sManagementUrl  the management service REST URL
     * @param  sClusterName    the name of the Coherence cluster
     * @param  storage         the storage of the data source
     */
    public CoherenceClusterDataSource(String sManagementUrl, String sClusterName, Storage storage)
        {
        this.f_sManagementUrl = sManagementUrl;
        this.f_storage        = storage;
        this.f_sClusterName   = sClusterName;
        }

    // ----- DataSource methods ---------------------------------------------

    @Override
    public boolean supportsUserRemove()
        {
        return true;
        }

    @Override
    protected void remove()
        {
        f_storage.deleteCustomPropertiesStorage();
        }

    // ----- accessors ------------------------------------------------------

    /**
     * The management URL.
     *
     * @return the REST management URL
     */
    public String getUrl()
        {
        return f_sManagementUrl;
        }

    /**
     * Return the name to be used for this particular Data source. The name will be shown
     * in the LHS tree.
     *
     * @return the name for the data source
     */
    public String getName()
        {
        try
            {
            URL url = new URL(f_sManagementUrl);
            return f_sClusterName + "[" + url.getHost() + ":" + url.getPort() + "]";
            }
        catch (MalformedURLException e)
            {
            // ignore the exceptions and return the URL as it is.
            }

        return f_sClusterName + "[" + f_sManagementUrl + "]";
        }

    // ----- data members ---------------------------------------------------

    /**
     * The management REST URL for the Coherence cluster.
     */
    private final String f_sManagementUrl;

    /**
     * The name for Coherence cluster.
     */
    private final String f_sClusterName;

    /**
     * The persistent storage of the data source.
     */
    private final Storage f_storage;
    }
