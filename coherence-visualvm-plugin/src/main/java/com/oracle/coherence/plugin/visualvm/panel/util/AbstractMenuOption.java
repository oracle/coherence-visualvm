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

package com.oracle.coherence.plugin.visualvm.panel.util;

import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dimension;

import java.awt.datatransfer.StringSelection;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import static com.oracle.coherence.plugin.visualvm.Localization.getLocalText;

/**
 * Abstract implementation of a {@link MenuOption} providing default functionality.
 *
 * @author tam  2014.02.27
 * @since  12.2.1
 */
public abstract class AbstractMenuOption
        implements MenuOption
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create a new AbstractMenuOption with default values.
     *
     * @param model         the {@link VisualVMModel} to get collected data from
     * @param requestSender the {@link RequestSender} to perform additional queries
     * @param jtable        the {@link ExportableJTable} that this applies to
     */
    public AbstractMenuOption(VisualVMModel model, RequestSender requestSender, ExportableJTable jtable)
        {
        f_jtable = jtable;
        f_model  = model;
        f_requestSender = requestSender;
        }

    // ----- AbstractMenuOption methods -------------------------------------

    /**
     * Show a message dialog with a scrollable text area for the message with a default size.
     *
     * @param sTitle       the title of the dialog box
     * @param sMessage     the message to display
     * @param nDialogType  the type of dialog, e.g. JOptionPane.INFORMATION_MESSAGE
     */
    protected static void showMessageDialog(String sTitle, String sMessage, int nDialogType)
        {
        showMessageDialog(sTitle, sMessage, nDialogType, 500, 400);
        }

    /**
     * Show a message dialog with a scrollable text area for the message.
     *
     * @param sTitle       the title of the dialog box
     * @param sMessage     the message to display
     * @param nDialogType  the type of dialog, e.g. JOptionPane.INFORMATION_MESSAGE
     * @param nLength      the length of the dialog window
     * @param nWidth       the width of the dialog window
     * @param fCopySave    true if copy and save buttons should be displayed
     */
    public static void showMessageDialog(String sTitle, String sMessage, int nDialogType, int nLength, int nWidth, boolean fCopySave)
        {
        JTextArea         txtArea    = new JTextArea(sMessage);
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        final JScrollPane pneMessage = new JScrollPane(txtArea);

        txtArea.setEditable(false);
        txtArea.setLineWrap(false);
        txtArea.setWrapStyleWord(true);
        txtArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        pneMessage.setPreferredSize(new Dimension(nLength, nWidth));

        setResizable(pneMessage);
        panel.add(pneMessage, BorderLayout.CENTER);

        if (fCopySave)
             {
             JButton btnCopy = new JButton();
             JButton btnSaveAs = new JButton();
             btnCopy.setText(getLocalText("LBL_copy_to_clipboard"));
             btnSaveAs.setText(getLocalText("LBL_save_data_as"));
             
             btnCopy.addActionListener((a) ->
                 {
                 Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(sMessage), null);
                 JOptionPane.showMessageDialog(panel, getLocalText("LBL_copied"), getLocalText("LBL_result"), JOptionPane.INFORMATION_MESSAGE);
                 });

             btnSaveAs.addActionListener((a) ->
                 {
                 JFileChooser fileChooser = new ExportableJTable.CheckExistsFileChooser();
                 fileChooser.setFileFilter(new FileNameExtensionFilter("*.*", "txt", "log"));

                 int result = fileChooser.showSaveDialog(btnSaveAs);

                 if (result == JFileChooser.APPROVE_OPTION)
                     {
                     File selectedFile = fileChooser.getSelectedFile();
                     if (saveContentsToFile(selectedFile, sMessage))
                         {
                         JOptionPane.showMessageDialog(panel,
                                 getLocalText("LBL_data_saved",selectedFile.getAbsolutePath()),
                                 getLocalText("LBL_result"), JOptionPane.INFORMATION_MESSAGE);
                         }
                     }
                 });

             JPanel flowLayout = new JPanel();
             flowLayout.add(getFiller());
             flowLayout.add(btnCopy);
             flowLayout.add(getFiller());
             flowLayout.add(btnSaveAs);
             flowLayout.add(getFiller());
             panel.add(flowLayout, BorderLayout.SOUTH);
             }

        JOptionPane.showMessageDialog(null, panel, sTitle, nDialogType);
        }

    /**
     * Returns a filler {@link JLabel}.
     *
     * @return a filler {@link JLabel}
     */
    private static JLabel getFiller()
        {
        JLabel label = new JLabel();
        label.setText("     ");
        return label;
        }

    /**
     * Save contents to a file.
     *
     * @param file  the {@link File} to save to
     *
     * @return true if the file was saved
     */
    private static boolean saveContentsToFile(File file, String sContents)
        {
        PrintStream fileWriter = null;

        try
            {
            fileWriter = new PrintStream(new FileOutputStream(file));
            fileWriter.write(sContents.getBytes());
            }
        catch (IOException ioe)
            {
            JOptionPane.showMessageDialog(null, getLocalText("LBL_unable_to_save", file.getAbsolutePath(), ioe.getMessage()),
                    getLocalText("LBL_result"), JOptionPane.ERROR_MESSAGE);
            return false;
            }
        finally
            {
            if (fileWriter != null)
                {
                fileWriter.close();
                }
            }
        return true;
        }


    /**
     * Show a message dialog with a scrollable text area for the message.
     *
     * @param sTitle       the title of the dialog box
     * @param sMessage     the message to display
     * @param nDialogType  the type of dialog, e.g. JOptionPane.INFORMATION_MESSAGE
     * @param nLength      the length of the dialog window
     * @param nWidth       the width of the dialog window
     */
    protected static void showMessageDialog(String sTitle, String sMessage, int nDialogType, int nLength, int nWidth)
        {
        showMessageDialog(sTitle, sMessage, nDialogType, nLength, nWidth,false);
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Ensure we can resize the window that this dialog belongs to.
     * refer: https://blogs.oracle.com/scblog/entry/tip_making_joptionpane_dialog_resizable
     *
     * @param component the {@link javax.swing.JComponent} to resize
     */
    public static void setResizable(final JComponent component)
        {
        component.addHierarchyListener(new HierarchyListener()
            {
            public void hierarchyChanged(HierarchyEvent e)
                {
                Window window = SwingUtilities.getWindowAncestor(component);

                if (window instanceof Dialog)
                    {
                    Dialog dialog = (Dialog) window;

                    if (!dialog.isResizable())
                        {
                        dialog.setResizable(true);
                        }
                    }
                }
            });
        }

    // ----- MenuOptions methods --------------------------------------------

    /**
     * Return the {@link ExportableJTable} that this menu option applies to
     *
     * @return the {@link ExportableJTable} that this menu option applies to
     */
    protected ExportableJTable getJTable()
        {
        return f_jtable;
        }

    /**
     * Return the {@link RequestSender} that can be used to run operations
     * or queries.
     *
     * @return the {@link RequestSender}
     */
    protected RequestSender getServer()
        {
        return f_requestSender;
        }

    /**
     * Return the {@link VisualVMModel} model that was used to collect stats.
     *
     * @return the {@link VisualVMModel} model that was used to collect stats
     */
    protected VisualVMModel getMode()
        {
        return f_model;
        }

    /**
     * Return the selected row or -1 if none selected.
     *
     * @return the selected row or -1 if none selected
     */
    protected int getSelectedRow()
        {
        return f_jtable != null ? f_jtable.getSelectedRow() : -1;
        }

    /**
     * Return the selected column or -1 if none selected.
     *
     * @return the selected column or -1 if none selected
     */
    protected int getSelectedColumn()
        {
        return f_jtable != null ? f_jtable.getSelectedColumn() : -1;
        }

    /**
     * {@inheritDoc}
      */
    public void setMenuLabel(String sMenuLabel)
        {
        m_sMenuLabel = sMenuLabel;
        }

    // ----- data members ---------------------------------------------------

    /**
     * The {@link ExportableJTable} that this menu option applies to.
     */
    protected final ExportableJTable f_jtable;

    /**
     * The {@link VisualVMModel} to get collected data from.
     */
    protected final VisualVMModel f_model;

    /**
     * The {@link RequestSender} to perform additional queries on.
     */
    protected final RequestSender f_requestSender;

    /**
     * The menu label for the right click option.
     */
    protected String m_sMenuLabel = getLocalText("LBL_show_details");
    }
