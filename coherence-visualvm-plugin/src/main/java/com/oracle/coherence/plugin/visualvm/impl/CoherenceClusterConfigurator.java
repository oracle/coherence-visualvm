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

package com.oracle.coherence.plugin.visualvm.impl;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * The application configurator window, where a user will configure the parameters
 * to connect to a Coherence cluster.
 *
 * @author sr 12.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
class CoherenceClusterConfigurator extends JPanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create an instance of {@link CoherenceClusterConfigurator}.
     */
    private CoherenceClusterConfigurator()
        {
        initComponents();
        update();
        }

    // ----- CoherenceClusterConfigurator methods -----------------------

    /**
     * Create an instance of CoherenceClusterConfigurator, and show the configuration dialog
     * to the user.
     *
     * @return the instance of {@link CoherenceClusterConfigurator}
     */
    public static CoherenceClusterConfigurator defineApplication()
        {
        CoherenceClusterConfigurator hc = new CoherenceClusterConfigurator();
        final DialogDescriptor dd =
                new DialogDescriptor(hc, Localization.getLocalText("LBL_Add_Coherence_Cluster"), true,
                        new Object[]{hc.m_okButton, DialogDescriptor.CANCEL_OPTION}, hc.m_okButton, 0,
                        null, null);

        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        do
            {
            d.setVisible(true);
            if (dd.getValue() != hc.m_okButton)
                {
                return null;
                }
            }
        while (!isValid(hc.getClusterName(), hc.getAppUrl()));

        return hc;
        }

    /**
     * Checks to see if the URL is valid.
     *
     * @param sClusterName cluster name
     * @param sUrl         uRL
     *
     * @return true if the URL is valid
     */
    private static boolean isValid(String sClusterName, String sUrl)
        {
        if (sClusterName == null || sClusterName.equals(""))
            {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(Localization.getLocalText("ERR_Invalid_Cluster_Name")));
            return false;
            }

        HttpRequestSender sender = new HttpRequestSender(sUrl);
        // Valid URL's are
        // Standard Coherence:  http://host:management-port/management/coherence/cluster
        // Managed Coherence Servers: http://<admin-host>:<admin-port>/management/coherence/<version>/clusters
        if (!sender.isValidUrl() || !sUrl.contains("/management/coherence"))
            {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(Localization.getLocalText("ERR_Invalid_URL", sUrl)));
            return false;
            }

        return true;
        }

    /**
     * The management over REST application URL configured by the user.
     *
     * @return the management over REST application URL
     */
    public String getAppUrl()
        {
        return m_appUrlField.getText().trim();
        }

    /**
     * The Coherence cluster name.
     *
     * @return the name of the Coherence cluster
     */
    public String getClusterName()
        {
        return m_cohClusterField.getText().trim();
        }

    /**
     * Update the OK button to enabled state when a URL is entered.
     */
    private void update()
        {
        SwingUtilities.invokeLater(new Runnable()
            {
            public void run()
                {
                String sAppUrl = getAppUrl();
                m_okButton.setEnabled(sAppUrl != null && sAppUrl.length() != 0);
                }
            });
        }

    /**
     * Initialize the components of the configurator panel.
     */
    private void initComponents()
        {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints;

        JLabel coherenceClusterUrlLabel = new JLabel(Localization.getLocalText("LBL_Coherence_Cluster"));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(15, 10, 0, 0);
        add(coherenceClusterUrlLabel, constraints);

        m_cohClusterField = new JTextField();
        coherenceClusterUrlLabel.setLabelFor(m_cohClusterField);
        m_cohClusterField.setPreferredSize(new Dimension(300, m_cohClusterField.getPreferredSize().height));
        m_cohClusterField.getDocument().addDocumentListener(new DocumentListener()
            {
            public void insertUpdate(DocumentEvent e)
                {
                update();
                }

            public void removeUpdate(DocumentEvent e)
                {
                update();
                }

            public void changedUpdate(DocumentEvent e)
                {
                update();
                }
            });
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 5, 0, 0);
        add(m_cohClusterField, constraints);

        JLabel appUrlLabel = new JLabel(Localization.getLocalText("LBL_Coherence_REST_url"));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(15, 10, 0, 0);
        add(appUrlLabel, constraints);

        m_appUrlField = new JTextField();
        appUrlLabel.setLabelFor(m_appUrlField);
        m_appUrlField.setPreferredSize(new Dimension(220, m_appUrlField.getPreferredSize().height));
        m_appUrlField.getDocument().addDocumentListener(new DocumentListener()
            {
            public void insertUpdate(DocumentEvent e)
                {
                update();
                }

            public void removeUpdate(DocumentEvent e)
                {
                update();
                }

            public void changedUpdate(DocumentEvent e)
                {
                update();
                }
            });
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(15, 5, 0, 0);
        add(m_appUrlField, constraints);

        m_okButton = new JButton(Localization.getLocalText("BTN_OK"));
        }


    // ----- data members ---------------------------------------------------

    /**
     * The text field for entering application URL.
     */
    private JTextField m_appUrlField;

    /**
     * The text field for entering cluster name.
     */
    private JTextField m_cohClusterField;

    /**
     * The OK button in the panel.
     */
    private JButton m_okButton;
    }
