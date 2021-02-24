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

package com.oracle.coherence.plugin.visualvm.panel;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.awt.BorderLayout;

import javax.swing.*;


/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view a cluster snapshot.
 *
 * @author tam  2021.02.23
 * @since  1.0.1
 */
public class CoherenceClusterSnapshotPanel
        extends AbstractCoherencePanel
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceClusterSnapshotPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceClusterSnapshotPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        setOpaque(false);
        f_htmlTextArea = new JEditorPane();
        f_htmlTextArea.setContentType("text/html");
        f_htmlTextArea.setEditable(false);
        f_htmlTextArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

        add(f_htmlTextArea);
        }

    // ----- AbstractCoherencePanel methods ----------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGUI()
        {
        f_htmlTextArea.setText("<h1>Hello</h1>");
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateData()
        {
        }

    // ----- constants ------------------------------------------------------

    private static final long serialVersionUID = -761252043492412546L;

    // ----- data members ---------------------------------------------------

    /**
     * HTML Text area.
     */
    private final JEditorPane f_htmlTextArea;
    }
