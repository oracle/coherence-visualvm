/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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


import org.junit.Assert;
import org.junit.Test;;import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the PartitionHelper.
 *
 * @author tam 2024.09.03
 */
public class PartitionHelperTest
    {

    @Test(expected = PartitionHelper.PartitionParsingException.class)
    public void testInvalidOwnershipText1()
            throws PartitionHelper.PartitionParsingException
        {
        PartitionHelper.parsePartitionOwnership("");
        }

    @Test(expected = PartitionHelper.PartitionParsingException.class)
    public void testInvalidOwnershipText2()
            throws PartitionHelper.PartitionParsingException
        {
        PartitionHelper.parsePartitionOwnership("{\"nothing\":");
        }

    @Test(expected = PartitionHelper.PartitionParsingException.class)
    public void testInvalidOwnershipText3()
            throws PartitionHelper.PartitionParsingException
        {
        PartitionHelper.parsePartitionOwnership("{\"ownership\":\"\"}");
        }

    @Test
    public void test7Partitions1Backup()
            throws PartitionHelper.PartitionParsingException
        {
        Map<Integer, PartitionOwnership> mapOwnerShip  =
                PartitionHelper.parsePartitionOwnership(encodeOwnership(OWNERSHIP_7_1));
        mapOwnerShip.forEach((k,v) -> System.out.println("k=" + k + ", v=" + v));
        assertEquals(4, mapOwnerShip.size());
        }

    @Test
    public void test19Partitions2Backup()
            throws PartitionHelper.PartitionParsingException
        {
        Map<Integer, PartitionOwnership> mapOwnerShip  =
                PartitionHelper.parsePartitionOwnership(encodeOwnership(OWNERSHIP_19_2));
        mapOwnerShip.forEach((k,v) -> System.out.println("k=" + k + ", v=" + v));
        assertEquals(7, mapOwnerShip.size());
        }

    @Test
    public void testExtractBackup()
        {
        assertEquals(-1, PartitionHelper.extractBackup("Rubbish"));
        assertEquals(1, PartitionHelper.extractBackup("Backup[1]#008:"));
        assertEquals(1, PartitionHelper.extractBackup("Backup[1]#008: 333, 444, 5555"));
        assertEquals(2, PartitionHelper.extractBackup("Backup[2]#008: 333, 444, 5555"));
        }

    @Test
    public void testRemovePrefix()
        {
        assertEquals("", PartitionHelper.removePrefix("Rubbish"));
        assertEquals("", PartitionHelper.removePrefix("Backup[1]#008: "));
        assertEquals("", PartitionHelper.removePrefix("Backup[1]#000"));
        assertEquals("333, 444, 5555", PartitionHelper.removePrefix("Backup[1]#008: 333, 444, 5555"));
        assertEquals("333, 444", PartitionHelper.removePrefix("Backup[2]#008: 333, 444"));
        assertEquals("031, 032, 033, 034, 035, 036", PartitionHelper.removePrefix("Primary[]#006: 031, 032, 033, 034, 035, 036"));
        }

    @Test
    public void testExtractPartitions()
        {
        Integer[] aValues = PartitionHelper.extractPartitions("Backup[1]#008: ");
        assertEquals(0, aValues.length);

        aValues = PartitionHelper.extractPartitions("Backup[1]#000");
        assertEquals(0, aValues.length);

        aValues = PartitionHelper.extractPartitions("Backup[1]#008: 333, 444, 5555");
        assertEquals(3, aValues.length);

        aValues = PartitionHelper.extractPartitions("Primary[]#006: 031, 032, 033, 034, 035, 036");
        assertEquals(6, aValues.length);
        }

    // ----- helpers --------------------------------------------------------

    private static String encodeOwnership(String sText)
        {
        return  "{\"ownership\":\"" + sText + "\"}";
        }

    // ----- constants ------------------------------------------------------

    private static final String OWNERSHIP_7_1 = "There are currently no pending or scheduled distributions for this service.<br/>*** Member:  1 total=5 (primary=3, backup=2)<br/>Primary[]#003: 000, 001, 002<br/>Backup[1]#002: 003, 004<br/><br/>*** Member:  2 total=5 (primary=2, backup=3)<br/>Primary[]#002: 005, 006<br/>Backup[1]#003: 000, 001, 002<br/><br/>*** Member:  3 total=4 (primary=2, backup=2)<br/>Primary[]#002: 003, 004<br/>Backup[1]#002: 005, 006<br/><br/>*** Orphans:<br/>Primary[]#000<br/>Backup[1]#000<br/>";
    private static final String OWNERSHIP_19_2 = "There are currently no pending or scheduled distributions for this service.<br/>*** Member:  1 total=9 (primary=3, backup=6)<br/>Primary[]#003: 000, 008, 012<br/>Backup[1]#003: 013, 015, 017<br/>Backup[2]#003: 002, 004, 007<br/><br/>*** Member:  2 total=9 (primary=3, backup=6)<br/>Primary[]#003: 005, 009, 013<br/>Backup[1]#002: 006, 008<br/>Backup[2]#004: 010, 012, 015, 017<br/><br/>*** Member:  3 total=9 (primary=3, backup=6)<br/>Primary[]#003: 001, 002, 004<br/>Backup[1]#006: 000, 003, 005, 010, 011, 016<br/>Backup[2]#000<br/><br/>*** Member:  4 total=9 (primary=3, backup=6)<br/>Primary[]#003: 006, 010, 014<br/>Backup[1]#001: 018<br/>Backup[2]#005: 000, 003, 005, 008, 013<br/><br/>*** Member:  5 total=10 (primary=3, backup=7)<br/>Primary[]#003: 003, 007, 011<br/>Backup[1]#003: 009, 012, 014<br/>Backup[2]#004: 001, 006, 016, 018<br/><br/>*** Member:  6 total=11 (primary=4, backup=7)<br/>Primary[]#004: 015, 016, 017, 018<br/>Backup[1]#004: 001, 002, 004, 007<br/>Backup[2]#003: 009, 011, 014<br/><br/>*** Orphans:<br/>Primary[]#000<br/>Backup[1]#000<br/>Backup[2]#000<br/>";
    }
