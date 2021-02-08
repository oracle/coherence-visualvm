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

package com.oracle.coherence.plugin.visualvm.helper;


import com.oracle.coherence.plugin.visualvm.Localization;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Helper to generate a report of the current state of the cluster.
 *
 * @author Tim Middleton 2020.02.02
 * @since 1.0.1
 */
public class ClusterReportGenerator
    {
    // ----- constructors ----------------------------------------------------

    /**
     * Construct a ClusterReportGenerator.
     *
     * @param reportDateTime report date time
     * @param fileOutput     {@link File} to write to
     * @param model          the current {@link VisualVMModel}
     */
    public ClusterReportGenerator(LocalDateTime reportDateTime, File fileOutput, VisualVMModel model)
        {
        f_fileOutput = fileOutput;
        f_model = model;
        f_reportDateTime = reportDateTime;
        f_sReportTitle = "Coherence Cluster Report " + f_reportDateTime;
        }

    /**
     * Generates a report of the current cluster state to a file.
     */
    public void generateReport()
        {

        try
            {
            if (!f_fileOutput.createNewFile())
                {
                throw new IOException("Unable to create file " + f_fileOutput.getAbsolutePath());
                }

            m_fileWriter = new PrintStream(new FileOutputStream(f_fileOutput));

            generateHeader();

            m_fileWriter.println("<h1>Report</h1>");
            m_fileWriter.println("</body></html>");
            }
        catch (IOException ioe)
            {
            String sMessage = Localization.getLocalText("LBL_unable_to_generate_report",
                                                        f_fileOutput.toString(),
                                                        ioe.getMessage());
            LOGGER.log(Level.WARNING, sMessage);
            throw new RuntimeException(ioe);
            }
        finally
            {
            if (m_fileWriter != null)
                {
                m_fileWriter.close();
                }
            }
        }

    // ----- helpers --------------------------------------------------------

    private void generateHeader()
        {
        m_fileWriter.println("<!DOCTYPE html>\n"
                             + "<html lang=\"en\">\n"
                             + "<head>\n"
                             + "    <title>" + f_sReportTitle +  "</title>\n"
                             + "    <meta charset=\"utf-8\">\n"
                             + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                             + "    <link rel=\"stylesheet\"\n"
                             + "          href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css\">\n"
                             + "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>\n"
                             + "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js\"></script>\n"
                             + "</head>\n"
                             + "<body>\n");
        }

    // ----- constants ------------------------------------------------------

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(ClusterReportGenerator.class.getName());

    // ----- data members ---------------------------------------------------

    private final VisualVMModel f_model;

    private final LocalDateTime f_reportDateTime;

    private final File f_fileOutput;

    private PrintStream m_fileWriter;

    private final String f_sReportTitle;
    }
