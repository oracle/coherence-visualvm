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

import org.openide.util.NbPreferences;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_DISABLE_MBEAN_CHECK;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_HEATMAP_ENABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_LOG_QUERY_TIMES;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_PERSISTENCE_LIST_ENABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REFRESH_TIME;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REPORTER_DISABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REST_DEBUG;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REST_TIMEOUT;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_SORTING_ENABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_ZOOM_ENABLED;

/**
 * Global preferences for Coherence. Preferences without set methods are read only.
 *
 * @author Tim Middleton 2020.02.23
 */
public class GlobalPreferences
        implements PreferenceChangeListener
    {

    // ----- constructors ---------------------------------------------------

    /**
     * Private constructor.
     */
    private GlobalPreferences()
        {
        f_prefs = NbPreferences.forModule(GlobalPreferences.class);
        f_prefs.addPreferenceChangeListener(this);
        }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
        {
        }

    // ----- accessors ------------------------------------------------------

    /**
     * Return the singleton {@link GlobalPreferences}.
     * @return singleton {@link GlobalPreferences}
     */
    public static GlobalPreferences sharedInstance()
        {
        return INSTANCE;
        }

    /**
     * Returns the value for refresh time or the default if it doesn't exist.
     *
     * @return the value for refresh time
     */
    public int getRefreshTime()
        {
        return getIntegerProperty(REFRESH_TIME, Integer.parseInt(System.getProperty(PROP_REFRESH_TIME, REFRESH_TIME_DEFAULT)));
        }

    /**
     * Set the value for refresh time.
     * @param refreshTime the value for refresh time
     */
    public void setRefreshTime(int refreshTime)
        {
        setIntegerProperty(REFRESH_TIME, refreshTime);
        }

    public boolean isLogQueryTimes()
        {
        return getBooleanProperty(LOG_QUERY_TIMES, Boolean.parseBoolean(System.getProperty(PROP_LOG_QUERY_TIMES, LOG_QUERY_TIMES_DEFAULT)));
        }

    public void setLogQueryTimes(boolean fValue)
        {
        setBooleanProperty(LOG_QUERY_TIMES, fValue);
        }

    public boolean isReporterDisabled()
        {
        return getBooleanProperty(REPORTER_DISABLED, Boolean.parseBoolean(System.getProperty(PROP_REPORTER_DISABLED, REPORTER_DISABLED_DEFAULT)));
        }

    public void setReporterDisabled(boolean fValue)
        {
        setBooleanProperty(REPORTER_DISABLED, fValue);
        }

    public boolean isHeatMapEnabled()
        {
        return getBooleanProperty(HEAT_MAP_ENABLED, Boolean.parseBoolean(System.getProperty(PROP_HEATMAP_ENABLED, HEAT_MAP_ENABLED_DEFAULT)));
        }

    public boolean isSortingAvailable()
        {
        return getBooleanProperty(SORTING_ENABLED, Boolean.parseBoolean(System.getProperty(PROP_SORTING_ENABLED, SORTING_ENABLED_DEFAULT)));
        }

    public boolean isZoomEnabled()
        {
        return getBooleanProperty(ZOOM_ENABLED, Boolean.parseBoolean(System.getProperty(PROP_ZOOM_ENABLED, ZOOM_ENABLED_DEFAULT)));
        }

    public boolean isPersistenceListEnabled()
        {
        return getBooleanProperty(PERSISTENCE_LIST_ENABLED, Boolean.parseBoolean(System.getProperty(PROP_PERSISTENCE_LIST_ENABLED, PERSISTENCE_LIST_ENABLED_DEFAULT)));
        }

    public int getRestTimeout()
        {
        return getIntegerProperty(REST_TIMEOUT, Integer.parseInt(System.getProperty(PROP_REST_TIMEOUT, REST_TIMEOUT_DEFAULT)));
        }

    public void setRestTimeout(int nTimeout)
        {
        setIntegerProperty(REST_TIMEOUT, nTimeout);
        }

    public boolean isRestDebugEnabled()
        {
        return getBooleanProperty(REST_DEBUG, Boolean.parseBoolean(System.getProperty(PROP_REST_DEBUG, REST_DEBUG_DEFAULT)));
        }

    public boolean isMBeanCheckDisabled()
        {
        return getBooleanProperty(DISABLE_MBEAN_CHECK, Boolean.parseBoolean(System.getProperty(PROP_DISABLE_MBEAN_CHECK, DISABLE_MBEAN_CHECK_DEFAULT)));
        }

    /**
     * Returns a boolean property value.
     * @param sProperty  property key
     * @param fDefault  default
     * @return a boolean value
     */
    private boolean getBooleanProperty(String sProperty, boolean fDefault)
        {
        boolean value;
        synchronized (f_prefs)
            {
            value = f_prefs.getBoolean(sProperty, fDefault);

            // we always store the value that was returned as we don't have the
            // concept if a missing value.
            f_prefs.putBoolean(sProperty, value);
            }
        return value;
        }

    /**
     * Returns an integer property value.
     * @param sProperty  property key
     * @param nDefault  default
     * @return an integer value
     */
    private int getIntegerProperty(String sProperty, int nDefault) {
        int value;
        synchronized (f_prefs) {
            value = f_prefs.getInt(sProperty, -1);
            if (value == -1) {
                value = nDefault;
                f_prefs.putInt(sProperty, value);
            }
        }
        return value;
    }

    /**
     * Sets an integer property value.
     * @param sProperty  property key
     * @param nValue  value
     */
    private void setIntegerProperty(String sProperty, int nValue) {
        synchronized (f_prefs) {
            f_prefs.putInt(sProperty, nValue);
        }
    }

    /**
     * Sets a boolean property value.
     * @param sProperty  property key
     * @param fValue  value
     */
    private void setBooleanProperty(String sProperty, boolean fValue) {
        synchronized (f_prefs) {
            f_prefs.putBoolean(sProperty, fValue);
        }
    }

    // ----- constants ------------------------------------------------------

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(GlobalPreferences.class.getName());

    private final static GlobalPreferences INSTANCE = new GlobalPreferences();

    private static final String REFRESH_TIME         = "refreshTime";

    private static final String REFRESH_TIME_DEFAULT = "30";

    private static final String LOG_QUERY_TIMES         = "logQueryTimes";

    private static final String LOG_QUERY_TIMES_DEFAULT = "false";

    private static final String REPORTER_DISABLED         = "reporterDisabled";

    private static final String REPORTER_DISABLED_DEFAULT = "false";

    private static final String SORTING_ENABLED         = "sortingEnabled";

    private static final String SORTING_ENABLED_DEFAULT = "true";

    private static final String HEAT_MAP_ENABLED         = "heatMapEnabled";

    private static final String HEAT_MAP_ENABLED_DEFAULT = "false";

    private static final String ZOOM_ENABLED         = "zoomEnabled";

    private static final String ZOOM_ENABLED_DEFAULT = "false";

    private static final String PERSISTENCE_LIST_ENABLED         = "persistenceListEnabled";

    private static final String PERSISTENCE_LIST_ENABLED_DEFAULT = "true";

    private static final String REST_TIMEOUT = "restTimeout";

    private static final String REST_TIMEOUT_DEFAULT = "30000";

    private static final String REST_DEBUG         = "restDebug";

    private static final String REST_DEBUG_DEFAULT = "false";

    private static final String DISABLE_MBEAN_CHECK        = "disableMBeanCheck";

    private static final String DISABLE_MBEAN_CHECK_DEFAULT = "false";

    // ----- data members ------------------------------------------------------

    private final Preferences f_prefs;
}
