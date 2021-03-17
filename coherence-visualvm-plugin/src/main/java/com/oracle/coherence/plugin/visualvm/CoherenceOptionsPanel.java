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


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graalvm.visualvm.core.options.UISupport;
import org.graalvm.visualvm.core.ui.components.SectionSeparator;
import org.graalvm.visualvm.core.ui.components.Spacer;

import org.openide.awt.Mnemonics;

import static com.oracle.coherence.plugin.visualvm.Localization.*;


/**
 * A controller for Coherence options.
 *
 * @author Tim Middleton 2021.02.23
 */
public class CoherenceOptionsPanel
        extends JPanel
    {

    // ----- constructors --------------------------------------------------

    /**
     * Constructs a {@link CoherenceOptionsPanel}.
     *
     * @param controller {@link CoherenceOptionsPanelController}
     */
    CoherenceOptionsPanel(CoherenceOptionsPanelController controller)
        {
        f_controller = controller;
        initComponents();
        startTrackingChanges();
        }

    // ----- CoherenceOptionsPanel methods ---------------------------------

    private final ChangeListener changeListener = new ChangeListener()
        {
        public void stateChanged(ChangeEvent e)
            {
            f_controller.changed();
            }
        };

    /**
     * Read all settings.
     */
    void load()
        {
        GlobalPreferences preferences = GlobalPreferences.sharedInstance();
        m_refreshTime.setValue(preferences.getRefreshTime());
        m_logQueryTimes.setSelected(preferences.isLogQueryTimes());
        m_disableMBeanCheck.setSelected(preferences.isMBeanCheckDisabled());
        m_restRequestTimout.setValue(preferences.getRestTimeout());
        m_enableRestDebug.setSelected(preferences.isRestDebugEnabled());
        m_enableZoom.setSelected(preferences.isZoomEnabled());
        m_enablePersistenceList.setSelected(preferences.isPersistenceListEnabled());
        m_enableClusterSnapshot.setSelected(preferences.isClusterSnapshotEnabled());
        m_adminFunctionsEnabled.setSelected(preferences.isAdminFunctionEnabled());
        }

    /**
     * Store all settings.
     */
    void store()
        {
        GlobalPreferences preferences = GlobalPreferences.sharedInstance();
        preferences.setRefreshTime((Integer) m_refreshTime.getValue());
        preferences.setLogQueryTimes(m_logQueryTimes.isSelected());
        preferences.setDisableMbeanCheck(m_disableMBeanCheck.isSelected());
        preferences.setRestDebugEnabled(m_enableRestDebug.isSelected());
        preferences.setRestTimeout((Integer) m_restRequestTimout.getValue());
        preferences.setZoomEnabled(m_enableZoom.isSelected());
        preferences.setPersistenceListEnabled(m_enablePersistenceList.isSelected());
        preferences.setClusterSnapshotEnabled(m_enableClusterSnapshot.isSelected());
        preferences.setAdminFunctionsEnabled(m_adminFunctionsEnabled.isSelected());
        }

    /**
     * Ensure that settings are valid.
     *
     * @return true if settings are valid
     */
    boolean valid()
        {
        try
            {
            return (Integer) m_refreshTime.getValue() > 0;
            }
        catch (Exception e)
            {
            }
        return false;
        }

    /**
     * Initialize UI components.
     */
    private void initComponents()
        {
        GridBagConstraints c;

        setLayout(new GridBagLayout());

        // ---- Header General ----
        addHeader(0, "LBL_general");

        // ---- Refresh Time Label ----
        JLabel plottersLabel = new JLabel();
        Mnemonics.setLocalizedText(plottersLabel, getLocalText("LBL_refresh_time"));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(plottersLabel, c);

        // refresh time
        m_refreshTime = new JSpinner();
        m_refreshTime.setToolTipText(getLocalText("TTIP_refresh_time"));
        plottersLabel.setLabelFor(m_refreshTime);
        m_refreshTime.setModel(new SpinnerNumberModel(30, 5, 99999, 1));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 4);
        add(m_refreshTime, c);

        // plottersUnits
        JLabel plottersUnits = new JLabel();
        Mnemonics.setLocalizedText(plottersUnits, getLocalText("LBL_seconds")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 0);
        add(plottersUnits, c);

        m_logQueryTimes = new JCheckBox();
        m_logQueryTimes.setToolTipText(getLocalText("TTIP_log_query_times"));
        addCheckBox(2, "LBL_log_query_times", m_logQueryTimes);

        m_disableMBeanCheck = new JCheckBox();
        m_disableMBeanCheck.setToolTipText(getLocalText("TTIP_disable_mbean_check"));
        addCheckBox(3, "LBL_disable_mbean_check", m_disableMBeanCheck);

        // ---- REST ----
        addHeader(4, "LBL_rest");

        // ---- REST Request Timeout ----
        JLabel lblRest = new JLabel();
        Mnemonics.setLocalizedText(lblRest, getLocalText("LBL_rest_request_timeout"));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(lblRest, c);

        m_restRequestTimout = new JSpinner();
        m_restRequestTimout.setToolTipText(getLocalText("TTIP_rest_request_timeout"));
        lblRest.setLabelFor(m_restRequestTimout);
        m_restRequestTimout.setModel(new SpinnerNumberModel(30000, 1000, 99999999, 1000));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 4);
        add(m_restRequestTimout, c);

        JLabel requestUnits = new JLabel();
        Mnemonics.setLocalizedText(requestUnits, getLocalText("LBL_millis")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 0);
        add(requestUnits, c);

        m_enableRestDebug = new JCheckBox();
        m_enableRestDebug.setToolTipText(getLocalText("TTIP_rest_debug"));
        addCheckBox(6, "LBL_enable_rest_debug", m_enableRestDebug);

        // ---- Other / Experimental ----
        addHeader(7, "LBL_other");

        m_enablePersistenceList = new JCheckBox();
        m_enablePersistenceList.setToolTipText(getLocalText("TTIP_persistence_list"));
        addCheckBox(8, "LBL_enable_persistence_list", m_enablePersistenceList);

        m_enableZoom = new JCheckBox();
        m_enableZoom.setToolTipText(getLocalText("TTIP_zoom_enabled"));
        addCheckBox(9, "LBL_enable_zoom", m_enableZoom);

        m_enableClusterSnapshot = new JCheckBox();
        m_enableClusterSnapshot.setToolTipText(getLocalText("TTIP_enable_cluster_snapshot"));
        addCheckBox(10, "LBL_enable_cluster_snapshot", m_enableClusterSnapshot);

        m_adminFunctionsEnabled = new JCheckBox();
        m_adminFunctionsEnabled.setToolTipText(getLocalText("TTIP_enable_cluster_head_dump"));
        addCheckBox(11, "LBL_enable_admin_functions", m_adminFunctionsEnabled);

        JLabel appsLabel = new JLabel();
        Mnemonics.setLocalizedText(appsLabel, getLocalText("LBL_reconnect")); // NOI18N
        c = new GridBagConstraints();
        c.gridy = 12;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 15, 6, 0);
        add(appsLabel, c);

        // filler
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 12;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(Spacer.create(), c);
        }

    /**
     * Adds a checkbox.
     *
     * @param y        y position
     * @param sLabel   label bundle key
     * @param checkBox the {@link JCheckBox}
     */
    private void addCheckBox(int y, String sLabel, JCheckBox checkBox)
        {
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, getLocalText(sLabel));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(label, c);

        label.setLabelFor(checkBox);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 4);
        add(checkBox, c);

        }

    /**
     * Adds a header.
     *
     * @param y      y position
     * @param sLabel the {@link JCheckBox}
     */
    private void addHeader(int y, String sLabel)
        {
        SectionSeparator sectionSeparator = UISupport.createSectionSeparator(getLocalText(sLabel));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        add(sectionSeparator, c);
        }

    /**
     * Start tracking changes.
     */
    private void startTrackingChanges()
        {
        m_refreshTime.getModel().addChangeListener(changeListener);
        m_logQueryTimes.getModel().addChangeListener(changeListener);
        m_disableMBeanCheck.getModel().addChangeListener(changeListener);
        m_enableRestDebug.getModel().addChangeListener(changeListener);
        m_restRequestTimout.getModel().addChangeListener(changeListener);
        m_enableZoom.getModel().addChangeListener(changeListener);
        m_enablePersistenceList.getModel().addChangeListener(changeListener);
        m_enableClusterSnapshot.getModel().addChangeListener(changeListener);
        m_adminFunctionsEnabled.getModel().addChangeListener(changeListener);
        }

    //----- data members ----------------------------------------------------

    /**
     * Controller associated with this panel.
     */
    private final CoherenceOptionsPanelController f_controller;

    /**
     * Refresh time spinner.
     */
    private JSpinner m_refreshTime;

    /**
     * Reqest request time spinner.
     */
    private JSpinner m_restRequestTimout;

    /**
     * Log query times checkbox.
     */
    private JCheckBox m_logQueryTimes;

    /**
     * Disable MBean Check checkbox.
     */
    private JCheckBox m_disableMBeanCheck;

    /**
     * Enable REST Debug checkbox.
     */
    private JCheckBox m_enableRestDebug;

    /**
     * Enable HeatMap checkbox.
     */
    private JCheckBox m_enableHeatMap;

    /**
     * Enable Zoom checkbox.
     */
    private JCheckBox m_enableZoom;

    /**
     * Enable Persistence list checkbox.
     */
    private JCheckBox m_enablePersistenceList;

    /**
     * Enable cluster snapshot checkbox.
     */
    private JCheckBox m_enableClusterSnapshot;

    /**
     * Enable admin functions.
     */
    private JCheckBox m_adminFunctionsEnabled;
    }
