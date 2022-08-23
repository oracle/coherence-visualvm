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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.URI;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * An implementation of a {@link JTable} that allows exporting table data as CSV
 * as well as addition of additional menu options for right click.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class ExportableJTable
        extends JTable
        implements ActionListener, AdditionalMenuOptions
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Create the table.
     *
     * @param model the {@link TableModel} to base this {@link JTable} on
     */
    public ExportableJTable(TableModel model)
        {
        super(model);

        setAutoCreateRowSorter(true);

        // ensure users can only ever select one row at a time
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setRowSelectionAllowed(true);

        ListSelectionModel rowSelectionModel = this.getSelectionModel();

        f_listener = new DefaultRowListSelectionListener(this);
        rowSelectionModel.addListSelectionListener(f_listener);

        // set more pleasant gird lines view
        setShowGrid(true);
        String sOS = System.getProperty("os.name").toLowerCase();
        if (sOS.contains("windows"))
            {
            setGridColor(UIManager.getColor("controlHighlight"));
            }
        else if (sOS.contains("mac"))
            {
            setGridColor(Color.LIGHT_GRAY);
            }
        }

    // ---- JTable methods --------------------------------------------------

    @Override
    public JPopupMenu getComponentPopupMenu()
        {
        if (m_menu == null)
            {
            m_menu = new JPopupMenu("Table Options:");

            m_menuItemSaveAs = new JMenuItem(Localization.getLocalText("LBL_save_data_as"));
            m_menuItemHelp = new JMenuItem(Localization.getLocalText("LBL_show_help"));

            m_menuItemSaveAs.addActionListener(this);
            m_menu.add(m_menuItemSaveAs);
            m_menuItemHelp.addActionListener(this);
            m_menu.add(m_menuItemHelp);

            // add additional menu options
            if (m_menuOption != null)
                {
                m_menu.addSeparator();

                for (MenuOption menuOption : m_menuOption)
                    {
                    if (menuOption instanceof SeparatorMenuOption)
                        {
                        m_menu.addSeparator();
                        }
                    else
                        {
                        if (menuOption != null)
                            {
                            JMenuItem newMenuItem = new JMenuItem(menuOption.getMenuItem());

                            newMenuItem.addActionListener(menuOption);
                            m_menu.add(newMenuItem);
                            }
                        }
                    }
                }
            }

        return m_menu;
        }

    // ---- ActionListener methods ------------------------------------------

    /**
     * Respond to a right-click event for saving data to disk.
     *
     * @param event  the event
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent event)
        {
        JComponent src = (JComponent) event.getSource();

        if (src.equals(m_menuItemSaveAs))
            {
            int result = fileChooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION)
                {
                saveTableDataToFile(fileChooser.getSelectedFile());
                }
            }
        else if (src.equals(m_menuItemHelp))
            {
            String sSimpleName = dataModel.getClass().getSimpleName();

            try
                {
                if (Desktop.isDesktopSupported())
                    {
                    Desktop.getDesktop().browse(new URI(BASE_URL + sSimpleName));
                    }
                }
            catch (Exception ee)
                {
                JOptionPane.showMessageDialog(null, Localization.getLocalText("LBL_unable_to_open"));
                }
            }
        }

    // ----- AdditionalMenuOptions methods ----------------------------------

    /**
     * {@inheritDoc}
     */
    public MenuOption[] getMenuOptions()
        {
        return m_menuOption;
        }

    /**
     * {@inheritDoc}
     */
    public void setMenuOptions(MenuOption[] menuOptions)
        {
        m_menuOption = menuOptions;
        }

    // ----- ExportableJTable methods ---------------------------------------

    /**
     * Return the {link ListSelectionListener} that has been setup for this table.
     *
     * @return the {link ListSelectionListener} that has been setup for this table
     */
    public DefaultRowListSelectionListener getListener()
        {
        return f_listener;
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Save the data for the table to a CSV file.
     *
     * @param file  the {@link File} to save to
     */
    private void saveTableDataToFile(File file)
        {
        PrintStream fileWriter = null;

        try
            {
            fileWriter = new PrintStream(new FileOutputStream(file));

            AbstractTableModel tableModel = (AbstractTableModel) this.getModel();

            // Get the column headers
            TableColumnModel columnModel = this.getTableHeader().getColumnModel();
            int              columnCount = columnModel.getColumnCount();

            for (int i = 0; i < columnCount; i++)
                {
                fileWriter.print("\"" + columnModel.getColumn(i).getHeaderValue()
                                 + (i < columnCount - 1 ? "\"," : "\""));
                }

            fileWriter.print(LF);

            // output the data line by line
            for (int r = 0; r < tableModel.getRowCount(); r++)
                {
                for (int c = 0; c < columnCount; c++)
                    {
                    Object oValue = tableModel.getValueAt(r, c);

                    fileWriter.print((oValue == null ? "" : oValue.toString()) + (c < columnCount - 1 ? "," : ""));
                    }

                fileWriter.print(LF);
                }

            }
        catch (IOException ioe)
            {
            LOGGER.log(Level.WARNING, Localization.getLocalText("LBL_unable_to_save", file.toString(), ioe.getMessage()));
            }
        finally
            {
            if (fileWriter != null)
                {
                fileWriter.close();
                }
            }
        }

    /**
     * An implementation of a {@link JFileChooser} that will confirm overwrite of
     * an existing file.
     *
     */
    public static class CheckExistsFileChooser
            extends JFileChooser
        {
        /**
         * Default constructor.
         */
        public CheckExistsFileChooser()
            {
            super();
            }

        /**
         * Constructor taking a directory name to check for existence.
         *
         * @param sDirectory the directory to check for existence
         */
        public CheckExistsFileChooser(String sDirectory)
            {
            super(sDirectory);
            }

        /**
         * {@inheritDoc}
         */
        @Override
        public void approveSelection()
            {
            if (!validateFileSelection(this.getSelectedFile()))
                {
                }
            else
                {
                super.approveSelection();
                }
            }

        /**
         * If a file exists, then ensure you ask the user if they want to
         * overwrite it.
         *
         * @param file  the {@link File} that was selected
         *
         * @return true if the file does not exist or the user wants to overwrite it
         */
        private boolean validateFileSelection(File file)
            {
            if (file.exists())
                {
                String sQuestion = Localization.getLocalText("LBL_file_already_exists", file.getAbsolutePath());

                if (JOptionPane.showConfirmDialog(null, sQuestion, Localization.getLocalText("LBL_confirm"),
                                                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    {
                    return true;
                    }

                return false;
                }

            return true;
            }
        }

    // ----- inner classes --------------------------------------------------

    /**
     * A {@link ListSelectionListener} that allows us to re-select a row after the
     * table has been redrawn.
     *
     */
    public class DefaultRowListSelectionListener
            implements ListSelectionListener
        {
        /**
         * Create a new listener to allow us to re-select the row if it was selected before
         * the refresh.
         *
         * @param table the {@link JTable} that this listener applies to
         */
        public DefaultRowListSelectionListener(JTable table)
            {
            m_table = table;
            }

        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent)
            {
            if (listSelectionEvent.getValueIsAdjusting())
                {
                return;
                }

            ListSelectionModel selectionModel = (ListSelectionModel) listSelectionEvent.getSource();

            if (!selectionModel.isSelectionEmpty())
                {
                m_nSelectedRow = selectionModel.getMinSelectionIndex();
                }
            else
                {
                m_nSelectedRow = -1;
                }
            }

        /**
         * Re-select the last selected row.
         */
        public void updateRowSelection()
            {
            if (m_nSelectedRow != -1)
                {
                m_table.addRowSelectionInterval(m_nSelectedRow, m_nSelectedRow);
                }
            }

        // ----- accessors --------------------------------------------------

        /**
         * Return the currently selected row.
         *
         * @return the currently selected row
         */
        public int getSelectedRow()
            {
            return m_nSelectedRow;
            }

        /**
         * Set the currently selected row.
         *
         * @param nSelectedRow the row to select
         */
        public void setSelectedRow(int nSelectedRow)
            {
            m_nSelectedRow = nSelectedRow;
            updateRowSelection();
            }

        // ----- data members -----------------------------------------------

        /**
         * The selected row.
         */
        private int m_nSelectedRow = -1;

        /**
         * The {@link JTable} that this applies to.
         */
        private JTable m_table;
        }

    // ----- constants ------------------------------------------------------

    private static final String BASE_URL = "https://github.com/oracle/coherence-visualvm/blob/jdk17-test/help.adoc#";

    private static final long serialVersionUID = 5999795232769091368L;

    /**
     * The line separator for the platform this process is running on.
     */
    private static final String LF = System.getProperty("line.separator");

    /**
     * File chooser to select a file.
     */
    private static JFileChooser fileChooser = null;

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ExportableJTable.class.getName());

    /**
     * Initialize so that we only get one instance.
     */
    static
        {
        fileChooser = new CheckExistsFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(Localization.getLocalText("LBL_csv_file"), "csv"));
        }

    // ----- data members ---------------------------------------------------

    private MenuOption[] m_menuOption = null;

    /**
     * Right-click menu.
     */
    private JPopupMenu m_menu = null;

    /**
     * Menu item for "Save As".
     */
    private JMenuItem m_menuItemSaveAs;

    /**
     * Menu item for "Help".
     */
    private JMenuItem m_menuItemHelp;

    /**
     * The row selection listener.
     */
    private final DefaultRowListSelectionListener f_listener;
    }
