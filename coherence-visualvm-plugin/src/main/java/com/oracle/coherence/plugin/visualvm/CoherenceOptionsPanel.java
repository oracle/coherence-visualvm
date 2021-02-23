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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A controller for Coherence options.
 *
 * @author Tim Middleton 2020.02.23
 */
public class CoherenceOptionsPanel
        extends JPanel
    {

    // ----- constructors --------------------------------------------------

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

    void load()
        {
        // TODO read settings and initialize GUI
        // Example:
        // someCheckBox.setSelected(Preferences.userNodeForPackage(CorePanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(CorePanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        m_refreshTime.setValue(GlobalPreferences.sharedInstance().getRefreshTime());
        //        plottersSpinner.setValue(GlobalPreferences.sharedInstance().getPlottersPoll());
        //        propertyListField.setText(GlobalPreferences.sharedInstance().getOrderedKeyPropertyList());
        }

    void store()
        {
        GlobalPreferences.sharedInstance().setRefreshTime((Integer) m_refreshTime.getValue());

        //        GlobalPreferences.sharedInstance().setPlottersPoll((Integer) plottersSpinner.getValue());
        //        GlobalPreferences.sharedInstance().setOrderedKeyPropertyList(propertyListField.getText());
        //        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(CorePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(CorePanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
        //        GlobalPreferences.sharedInstance().store();
        }

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

    private void initComponents()
        {
        GridBagConstraints c;

        setLayout(new GridBagLayout());

        // pollingSeparator
        SectionSeparator pollingSeparator = UISupport.createSectionSeparator(Localization.getLocalText("LBL_oracle"));
        c = new GridBagConstraints();
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);
        add(pollingSeparator, c);

        // plottersLabel
        JLabel plottersLabel = new JLabel();
        Mnemonics.setLocalizedText(plottersLabel, Localization.getLocalText("LBL_refresh_time"));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(plottersLabel, c);

        // refresh time
        m_refreshTime = new JSpinner();
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
        Mnemonics.setLocalizedText(plottersUnits, Localization.getLocalText("LBL_seconds")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 0);
        add(plottersUnits, c);

        // filler
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(Spacer.create(), c);
    }

    private void startTrackingChanges()
        {
        // plottersSpinner.getModel().addChangeListener(changeListener);
        }

    //----- data members ----------------------------------------------------

    private final CoherenceOptionsPanelController f_controller;

    private JSpinner m_refreshTime;
}
