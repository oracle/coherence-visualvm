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


import javax.swing.JComponent;
import org.graalvm.visualvm.core.options.UISupport;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * A controller for Coherence options.
 *
 * @author Tim Middleton 2020.02.23
 */
@OptionsPanelController.TopLevelRegistration(
        id = "CoherenceOptions",
        categoryName = "#OptionsCategory_Name_Coherence",
        iconBase = "com/oracle/coherence/plugin/visualvm/coherence_grid_icon32.png",
        position = 5000
)
public final class CoherenceOptionsPanelController
        extends OptionsPanelController
    {

    //----- accessors -------------------------------------------------------

    private CoherenceOptionsPanel getPanel()
        {
        if (m_panel == null)
            {
            m_panel = new CoherenceOptionsPanel(this);
            }
        return m_panel;
        }

    void changed()
        {
        if (!m_fChanged)
            {
            m_fChanged = true;
            m_pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
            }
        m_pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }

    //----- OptionsPanelController methods ----------------------------------

    @Override
    public void update()
        {
        getPanel().load();
        m_fChanged = false;
        }

    @Override
    public void applyChanges()
        {
        getPanel().store();
        m_fChanged = false;
        }

    @Override
    public void cancel()
        {
        // need not do anything special, if no changes have been persisted yet
        }

    @Override
    public boolean isValid()
        {
        return getPanel().valid();
        }

    @Override
    public boolean isChanged()
        {
        return m_fChanged;
        }

    @Override
    public JComponent getComponent(Lookup lookup)
        {
        if (m_component == null)
            {
            m_component = UISupport.createScrollableContainer(getPanel());
            }
        return m_component;
        }

    @Override
    public HelpCtx getHelpCtx()
        {
        return null;
        }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener)
        {
        m_pcs.addPropertyChangeListener(propertyChangeListener);
        }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener)
        {
        m_pcs.removePropertyChangeListener(propertyChangeListener);
        }

    // ----- data members ---------------------------------------------------

    private CoherenceOptionsPanel m_panel;

    private JComponent m_component;

    private final PropertyChangeSupport m_pcs = new PropertyChangeSupport(this);

    private boolean m_fChanged;
    }
