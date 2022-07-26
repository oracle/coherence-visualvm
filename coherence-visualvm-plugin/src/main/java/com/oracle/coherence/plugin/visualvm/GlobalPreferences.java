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


import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.openide.util.NbPreferences;

import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_CLUSTER_SNAPSHOT;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_DISABLE_MBEAN_CHECK;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_LOG_QUERY_TIMES;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_PERSISTENCE_LIST_ENABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REFRESH_TIME;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REPORTER_DISABLED;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REST_DEBUG;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_REST_TIMEOUT;
import static com.oracle.coherence.plugin.visualvm.VisualVMModel.PROP_ZOOM_ENABLED;


/**
 * Global preferences for Coherence. Preferences without set methods are read
 * only.
 *
 * @author Tim Middleton 2021.02.23
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

        // If any system properties are present then set the preferences to this value
        // otherwise just issue a get() for the current preference and it will load a default if it doesn't exist

        String sValue = System.getProperty(PROP_REFRESH_TIME);
        if (sValue != null)
            {
            setRefreshTime(Integer.parseInt(sValue));
            }
        else
            {
            getRefreshTime();
            }

        sValue = System.getProperty(PROP_LOG_QUERY_TIMES);
        if (sValue != null)
            {
            setLogQueryTimes(Boolean.parseBoolean(sValue));
            }
        else
            {
            isLogQueryTimes();
            }

        sValue = System.getProperty(PROP_REPORTER_DISABLED);
        if (sValue != null)
            {
            setReporterDisabled(Boolean.parseBoolean(sValue));
            }
        else
            {
            isReporterDisabled();
            }

        // always set heat map enabled
        setHeatMapEnabled(true);

        sValue = System.getProperty(PROP_ZOOM_ENABLED);
        if (sValue != null)
            {
            setZoomEnabled(Boolean.parseBoolean(sValue));
            }
        else
            {
            isZoomEnabled();
            }

        sValue = System.getProperty(PROP_PERSISTENCE_LIST_ENABLED);
        if (sValue != null)
            {
            setPersistenceListEnabled(Boolean.parseBoolean(sValue));
            }
        else
            {
            isPersistenceListEnabled();
            }

        sValue = System.getProperty(PROP_REST_TIMEOUT);
        if (sValue != null)
            {
            setRestTimeout(Integer.parseInt(sValue));
            }
        else
            {
            getRestTimeout();
            }

        sValue = System.getProperty(PROP_REST_DEBUG);
        if (sValue != null)
            {
            setRestDebugEnabled(Boolean.parseBoolean(sValue));
            }
        else
            {
            isRestDebugEnabled();
            }

        sValue = System.getProperty(PROP_DISABLE_MBEAN_CHECK);
        if (sValue != null)
            {
            setDisableMbeanCheck(Boolean.parseBoolean(sValue));
            }
        else
            {
            isMBeanCheckDisabled();
            }

        sValue = System.getProperty(PROP_CLUSTER_SNAPSHOT);
        if (sValue != null)
            {
            setClusterSnapshotEnabled(Boolean.parseBoolean(sValue));
            }
        else
            {
            isClusterSnapshotEnabled();
            }
        }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
        {
        // no-op as no preferences are added in realtime
        }

    // ----- accessors ------------------------------------------------------

    /**
     * Return the singleton {@link GlobalPreferences}.
     *
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
        return getIntegerProperty(REFRESH_TIME, Integer.parseInt(REFRESH_TIME_DEFAULT));
        }

    /**
     * Set the value for refresh time.
     *
     * @param refreshTime the value for refresh time
     */
    public void setRefreshTime(int refreshTime)
        {
        setIntegerProperty(REFRESH_TIME, refreshTime);
        }

    /**
     * Indicates if log query times is set.
     *
     * @return true if log query times is set
     */
    public boolean isLogQueryTimes()
        {
        return getBooleanProperty(LOG_QUERY_TIMES, LOG_QUERY_TIMES_DEFAULT);
        }

    /**
     * Sets log query times.
     *
     * @param fValue if log query times enabled
     */
    public void setLogQueryTimes(boolean fValue)
        {
        setBooleanProperty(LOG_QUERY_TIMES, fValue);
        }

    /**
     * Indicates if the reporter is disabled.
     *
     * @return true if the reporter is disabled
     */
    public boolean isReporterDisabled()
        {
        return getBooleanProperty(REPORTER_DISABLED, REPORTER_DISABLED_DEFAULT);
        }

    /**
     * Set if the reporter is disabled.
     *
     * @param fValue if the reporter is disabled.
     */
    public void setReporterDisabled(boolean fValue)
        {
        setBooleanProperty(REPORTER_DISABLED, fValue);
        }

    /**
     * Indicates if the heat map is enabled.
     *
     * @return true if the heat map is enabled
     */
    public boolean isHeatMapEnabled()
        {
        return getBooleanProperty(HEAT_MAP_ENABLED, HEAT_MAP_ENABLED_DEFAULT);
        }

    /**
     * Sets if the heat map is enabled.
     *
     * @param fValue if the heat map is enabled
     */
    public void setHeatMapEnabled(boolean fValue)
        {
        setBooleanProperty(HEAT_MAP_ENABLED, fValue);
        }

    /**
     * Indicates if zoom is enabled.
     *
     * @return true if zoom is enabled
     */
    public boolean isZoomEnabled()
        {
        return getBooleanProperty(ZOOM_ENABLED, ZOOM_ENABLED_DEFAULT);
        }

    /**
     * Sets if zoom is enabled.
     *
     * @param fValue if zoom is enabled
     */
    public void setZoomEnabled(boolean fValue)
        {
        setBooleanProperty(ZOOM_ENABLED, fValue);
        }

    /**
     * Indicates if persistence list is enabled.
     *
     * @return true if persistence list is enabled.
     */
    public boolean isPersistenceListEnabled()
        {
        return getBooleanProperty(PERSISTENCE_LIST_ENABLED, PERSISTENCE_LIST_ENABLED_DEFAULT);
        }

    /**
     * Sets if persistence list is enabled.
     *
     * @param fValue if persistence list is enabled
     */
    public void setPersistenceListEnabled(boolean fValue)
        {
        setBooleanProperty(PERSISTENCE_LIST_ENABLED, fValue);
        }

    /**
     * Returns the REST timeout.
     *
     * @return the REST timeout
     */
    public int getRestTimeout()
        {
        return getIntegerProperty(REST_TIMEOUT, Integer.parseInt(REST_TIMEOUT_DEFAULT));
        }

    /**
     * Sets the REST timeout.
     *
     * @param nTimeout the REST timeout
     */
    public void setRestTimeout(int nTimeout)
        {
        setIntegerProperty(REST_TIMEOUT, nTimeout);
        }

    /**
     * Indicates if REST debug is enabled.
     *
     * @return true if REST debug is enabled
     */
    public boolean isRestDebugEnabled()
        {
        return getBooleanProperty(REST_DEBUG, REST_DEBUG_DEFAULT);
        }

    /**
     * Indicates if SSL Cert validation is disabled.
     *
     * @return true ff SSL Cert validation is disabled
     */
    public boolean isSSLCertValidationDisabled()
        {
        return getBooleanProperty(SSL_CERT_DISABLED, SSL_CERT_DISABLED_DEFAULT);
        }

    /**
     * Sets if REST debug is enabled.
     *
     * @param fValue if REST debug is enabled
     */
    public void setRestDebugEnabled(boolean fValue)
        {
        setBooleanProperty(REST_DEBUG, fValue);
        }

    /**
     * Sets if SSL Cert validation is disabled.
     *
     * @param fValue if SSL Cert validation is disabled
     */
    public void setSSLCertValidationDisabled(boolean fValue)
        {
        setBooleanProperty(SSL_CERT_DISABLED, fValue);
        }

    /**
     * Indicates if admin functions are enabled.
     *
     * @return if admin functions are enabled
     */
    public boolean isAdminFunctionEnabled()
        {
        return getBooleanProperty(ENABLE_ADMIN_FUNCTIONS, ENABLE_ADMIN_FUNCTIONS_DEFAULT);
        }

    /**
     * Sets if admin functions are enabled.
     *
     * @param fValue if admin functions are enabled
     */
    public void setAdminFunctionsEnabled(boolean fValue)
        {
        setBooleanProperty(ENABLE_ADMIN_FUNCTIONS, fValue);
        }

    /**
     * Indicates if MBean Check is disabled.
     *
     * @return true if MBean Check is disabled
     */
    public boolean isMBeanCheckDisabled()
        {
        return getBooleanProperty(DISABLE_MBEAN_CHECK, DISABLE_MBEAN_CHECK_DEFAULT);
        }

    /**
     * Sets if MBean Check is disabled.
     *
     * @param fValue if MBean Check is disabled
     */
    public void setDisableMbeanCheck(boolean fValue)
        {
        setBooleanProperty(DISABLE_MBEAN_CHECK, fValue);
        }

    /**
     * Indicates if the cluster snapshot tab is dispalyed.
     *
     * @return true if the cluster snapshot tab is displayed
     */
    public boolean isClusterSnapshotEnabled()
        {
        return getBooleanProperty(CLUSTER_SNAPSHOT_ENABLED, CLUSTER_SNAPSHOT_DEFAULT);
        }

    /**
     * Set if the cluster snapshot tab is displayed.
     *
     * @param fValue if the cluster snapshot tab is displayed
     */
    public void setClusterSnapshotEnabled(boolean fValue)
        {
        setBooleanProperty(CLUSTER_SNAPSHOT_ENABLED, fValue);
        }

    /**
     * Returns a boolean property value.
     *
     * @param sProperty property key
     * @param sDefault  default
     *
     * @return a boolean value
     */
    private boolean getBooleanProperty(String sProperty, String sDefault)
        {
        String sValue;
        synchronized (f_prefs)
            {
            sValue = f_prefs.get(sProperty, "NONE");
            if (sValue.equals("NONE"))
                {
                sValue = sDefault;
                f_prefs.put(sProperty, sDefault);
                }

            }
        return Boolean.parseBoolean(sValue);
        }

    /**
     * Returns an integer property value.
     *
     * @param sProperty property key
     * @param nDefault  default
     *
     * @return an integer value
     */
    private int getIntegerProperty(String sProperty, int nDefault)
        {
        int value;
        synchronized (f_prefs)
            {
            value = f_prefs.getInt(sProperty, -1);
            if (value == -1)
                {
                value = nDefault;
                f_prefs.putInt(sProperty, value);
                }
            }
        return value;
        }

    /**
     * Sets an integer property value.
     *
     * @param sProperty property key
     * @param nValue    value
     */
    private void setIntegerProperty(String sProperty, int nValue)
        {
        synchronized (f_prefs)
            {
            f_prefs.putInt(sProperty, nValue);
            }
        }

    /**
     * Sets a boolean property value.
     *
     * @param sProperty property key
     * @param fValue    value
     */
    private void setBooleanProperty(String sProperty, boolean fValue)
        {
        synchronized (f_prefs)
            {
            f_prefs.put(sProperty, Boolean.toString(fValue));
            }
        }

    // ----- constants ------------------------------------------------------

    /**
     * Singleton.
     */
    private static final GlobalPreferences INSTANCE = new GlobalPreferences();

    /**
     * Preference key for refresh time.
     */
    private static final String REFRESH_TIME = "refreshTime";

    /**
     * Default for refresh time.
     */
    private static final String REFRESH_TIME_DEFAULT = "30";

    /**
     * Preference key for log query times.
     */
    private static final String LOG_QUERY_TIMES = "logQueryTimes";

    /**
     * Default for log query times.
     */
    private static final String LOG_QUERY_TIMES_DEFAULT = "false";

    /**
     * Preference key for reporter disable.
     */
    private static final String REPORTER_DISABLED = "reporterDisabled";

    /**
     * Default for reporter disabled.
     */
    private static final String REPORTER_DISABLED_DEFAULT = "false";

    /**
     * Preference key for heat map enabled.
     */
    private static final String HEAT_MAP_ENABLED = "heatMapEnabled";

    /**
     * default for heat map enabled.
     */
    private static final String HEAT_MAP_ENABLED_DEFAULT = "false";

    /**
     * Preference key for zoom enabled.
     */
    private static final String ZOOM_ENABLED = "zoomEnabled";

    /**
     * Default for zoom enabled.
     */
    private static final String ZOOM_ENABLED_DEFAULT = "false";

    /**
     * Preference key for cluster snapshot enabled.
     */
    private static final String CLUSTER_SNAPSHOT_ENABLED = "clusterSnapshot";

    /**
     * Default for cluster snapshot enabled.
     */
    private static final String CLUSTER_SNAPSHOT_DEFAULT = "false";

    /**
     * Preference key for persistence list enabled.
     */
    private static final String PERSISTENCE_LIST_ENABLED = "persistenceListEnabled";

    /**
     * Default for persistence list enabled.
     */
    private static final String PERSISTENCE_LIST_ENABLED_DEFAULT = "true";

    /**
     * Preference key for REST timeout.
     */
    private static final String REST_TIMEOUT = "restTimeout";

    /**
     * Default for REST timeout.
     */
    private static final String REST_TIMEOUT_DEFAULT = "30000";

    /**
     * Preference key for REST Debug
     */
    private static final String REST_DEBUG = "restDebug";

    /**
     * Default for REST debug.
     */
    private static final String REST_DEBUG_DEFAULT = "false";

    /**
     * Preference key for Disable SSL Cert Validation.
     */
    private static final String SSL_CERT_DISABLED = "restSSLCertDisabled";

    /**
     * Default for for Disable SSL Cert Validation.
     */
    private static final String SSL_CERT_DISABLED_DEFAULT = "false";

    /**
     * Preference key for Admin functions.
     */
    private static final String ENABLE_ADMIN_FUNCTIONS = "adminFunctions";

    /**
     * Default for enable admin functions.
     */
    private static final String ENABLE_ADMIN_FUNCTIONS_DEFAULT = "false";

    /**
     * Preference key for disable MBean check.
     */
    private static final String DISABLE_MBEAN_CHECK = "disableMBeanCheck";

    /**
     * Default for disable MBean check.
     */
    private static final String DISABLE_MBEAN_CHECK_DEFAULT = "false";

    // ----- data members ------------------------------------------------------

    /**
     * Preferences.
     */
    private final Preferences f_prefs;
    }
