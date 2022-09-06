/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the UnavailabilityTimeAnalyzer.
 *
 * @author Tim Middleton 2021.07.06
 */
public class UnavailabilityTimeAnalyzerTest
    {
    @Test
    public void testAnalyzeLogFile()
        {
        File fileLogFile = getTestResource("/test-logfile-1.log");

        assertNotNull(fileLogFile);
        CoherenceOptionsPanel.UnavailabilityTimeAnalyzer analyzer =
                new CoherenceOptionsPanel.UnavailabilityTimeAnalyzer(fileLogFile);

        String sResults = analyzer.analyze(true);
        assertNotNull(sResults);
        }

    @Test
    public void testInvalidFile()
        {
        File fileLogFile = getTestResource("/test-logfile-2.log");

        assertNotNull(fileLogFile);
        CoherenceOptionsPanel.UnavailabilityTimeAnalyzer analyzer =
                new CoherenceOptionsPanel.UnavailabilityTimeAnalyzer(fileLogFile);

        String sResults = analyzer.analyze(true);
        assertTrue(sResults.contains("No services found. This may not be a Coherence log file."));
        }

    // ----- helpers --------------------------------------------------------

    private File getTestResource(String sResource)
        {
        URL resource = this.getClass().getResource(sResource);
        assertNotNull(resource);
        return new File(resource.getFile());
        }

    // ----- data members ---------------------------------------------------

    private File testLogfile;
    }
