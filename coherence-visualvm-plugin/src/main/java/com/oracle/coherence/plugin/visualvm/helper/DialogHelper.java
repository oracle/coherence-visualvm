/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.helper;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Various helper methods for creating dialogs.
 *
 * @author tam  2022.10.13
 * @since  1.5.1
 */
public class DialogHelper
    {

    // ----- constructors ---------------------------------------------------

    private DialogHelper()
        {
        // cannot instantiate
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Show an information message.
     * @param sMessage message to display
     */
    public static void showInfoDialog(String sMessage)
        {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(sMessage));
        }

    /**
     * Show a warning message.
     * @param sMessage message to display
     */
    public static void showWarningDialog(String sMessage)
        {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(sMessage, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
        }

    /**
     * Show a confirmation dialog and return true if the user answers YES.
     * @param sMessage message to display
     * @return ture if user answered YES
     */
    public static boolean showConfirmDialog(String sMessage)
        {
        NotifyDescriptor.Confirmation message =
                new NotifyDescriptor.Confirmation(sMessage, NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE);

        Object answer = DialogDisplayer.getDefault().notify(message);

        return NotifyDescriptor.YES_OPTION.equals(answer);
        }
    }