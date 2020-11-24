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

import com.oracle.coherence.plugin.visualvm.helper.GraphHelper;
import com.oracle.coherence.plugin.visualvm.helper.RenderHelper;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;
import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MachineData;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.util.Date;
import java.util.List;

import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.graalvm.visualvm.charts.SimpleXYChartSupport;

/**
 * An implementation of an {@link AbstractCoherencePanel} to
 * view various overview graphs for a Coherence cluster.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class CoherenceClusterOverviewPanel
        extends AbstractCoherencePanel
    {

    // ---- constructors ----------------------------------------------------

    /**
     * Create the layout for the {@link CoherenceClusterOverviewPanel}.
     *
     * @param model {@link VisualVMModel} to use for this panel
     */
    public CoherenceClusterOverviewPanel(VisualVMModel model)
        {
        super(new BorderLayout(), model);

        this.setPreferredSize(new Dimension(500, 300));

        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new FlowLayout());
        pnlHeader.setOpaque(false);

        f_txtClusterName = getTextField(15, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_cluster_name", f_txtClusterName));
        pnlHeader.add(f_txtClusterName);

        f_txtVersion = getTextField(8, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_version", f_txtVersion));
        pnlHeader.add(f_txtVersion);

        f_txtClusterSize = getTextField(3, JTextField.RIGHT);
        pnlHeader.add(getLocalizedLabel("LBL_members", f_txtClusterSize));
        pnlHeader.add(f_txtClusterSize);

        f_txtRefreshDate = getTextField(18, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_refresh_date", f_txtRefreshDate));
        pnlHeader.add(f_txtRefreshDate);

        f_txtClusterStatusHA = getTextField(10, JTextField.LEFT);
        pnlHeader.add(getLocalizedLabel("LBL_cluster_statusha", f_txtClusterStatusHA));
        pnlHeader.add(f_txtClusterStatusHA);

        JPanel pnlData = new JPanel();

        pnlData.setLayout(new GridLayout(2, 2));

        // create a chart for total cluster memory
        f_memoryGraph = GraphHelper.createClusterMemoryGraph();

        JPanel pnlPlotter = new JPanel(new GridLayout(1, 1));

        pnlPlotter.add(f_memoryGraph.getChart());

        pnlData.add(pnlPlotter);

        // create a chart for publisher success rate
        f_publisherGraph = GraphHelper.createPublisherGraph();

        JPanel pnlPlotter2 = new JPanel(new GridLayout(1, 1));

        pnlPlotter2.add(f_publisherGraph.getChart());
        pnlData.add(pnlPlotter2);

        // create a chart for machine load average
        f_loadAverageGraph = GraphHelper.createMachineLoadAverageGraph(model);

        JPanel pnlPlotter4 = new JPanel(new GridLayout(1, 1));

        pnlPlotter4.add(f_loadAverageGraph.getChart());
        pnlData.add(pnlPlotter4);

        // create a chart for receiver success rate
        f_receiverGraph = GraphHelper.createReceiverGraph();

        JPanel pnlPlotter3 = new JPanel(new GridLayout(1, 1));

        pnlPlotter3.add(f_receiverGraph.getChart());
        pnlData.add(pnlPlotter3);

        add(pnlHeader, BorderLayout.PAGE_START);
        add(pnlData, BorderLayout.CENTER);
        }

    // ----- AbstractCoherencePanel methods ---------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGUI()
        {
        int   cTotalMemory        = 0;
        int   cTotalMemoryUsed    = 0;
        float cTotalPublisherRate = 0.0f;
        float cTotalReceiverRate  = 0.0f;
        float cMinPublisherRate   = -1;
        float cMinReceiverRate    = -1;

        // get the min /max values for publisher and receiver success rates
        if (memberData != null)
            {
            int   count = 0;
            float cRate = 0;

            for (Entry<Object, Data> entry : memberData)
                {
                // only include memory is the node is storage enabled
                if (isNodeStorageEnabled((Integer) entry.getValue().getColumn(MemberData.NODE_ID)))
                    {
                    cTotalMemory     += (Integer) entry.getValue().getColumn(MemberData.MAX_MEMORY);
                    cTotalMemoryUsed += (Integer) entry.getValue().getColumn(MemberData.USED_MEMORY);
                    }

                count++;
                cRate               = (Float) entry.getValue().getColumn(MemberData.PUBLISHER_SUCCESS);
                cTotalPublisherRate += cRate;

                if (cMinPublisherRate == -1 || cRate < cMinPublisherRate)
                    {
                    cMinPublisherRate = cRate;
                    }

                cRate              = (Float) entry.getValue().getColumn(MemberData.RECEIVER_SUCCESS);
                cTotalReceiverRate += cRate;

                if (cMinReceiverRate == -1 || cRate < cMinReceiverRate)
                    {
                    cMinReceiverRate = cRate;
                    }
                }

            // update the publisher graph
            GraphHelper.addValuesToPublisherGraph(f_publisherGraph, cMinPublisherRate,
                count == 0 ? 0 : cTotalPublisherRate / count);

            GraphHelper.addValuesToReceiverGraph(f_receiverGraph, cMinReceiverRate,
                count == 0 ? 0 : cTotalReceiverRate / count);
            }

        // update the memory graph
        if (cTotalMemory != 0)
            {
            GraphHelper.addValuesToClusterMemoryGraph(f_memoryGraph, cTotalMemory, cTotalMemoryUsed);
            }

        // update cluster information
        if (m_clusterData != null)
            {
            for (Entry<Object, Data> entry : m_clusterData)
                {
                f_txtClusterName.setText(entry.getValue().getColumn(ClusterData.CLUSTER_NAME).toString());
                f_txtRefreshDate.setText(new Date(f_model.getLastUpdate()).toString());
                f_txtVersion.setText(entry.getValue().getColumn(ClusterData.VERSION).toString().replaceFirst(" .*$", ""));
                f_txtClusterSize.setText(String.format("%d", entry.getValue().getColumn(ClusterData.CLUSTER_SIZE)));

                }
            }

        // update the statusHA value for the cluster
        if (m_serviceData != null)
            {
            // start at best value of SITE-SAFE and get a "cluster statusHA" by working backwards
            int bestStatusHA = STATUSHA_VALUES.length;

            for (Entry<Object, Data> entry : m_serviceData)
                {
                if (!"n/a".equals(entry.getValue().getColumn(ServiceData.STATUS_HA)))
                    {
                    int statusHAIndex = getStatusHAIndex(entry.getValue().getColumn(ServiceData.STATUS_HA).toString());

                    if (statusHAIndex < bestStatusHA)
                        {
                        bestStatusHA = statusHAIndex;
                        }
                    }
                }

            if (bestStatusHA < STATUSHA_VALUES.length)
                {
                // now set the "cluster statusHA"
                String sStatusHA = STATUSHA_VALUES[bestStatusHA];

                if (bestStatusHA == 0)
                    {
                    f_txtClusterStatusHA.setBackground(Color.red);
                    f_txtClusterStatusHA.setForeground(Color.white);
                    f_txtClusterStatusHA.setToolTipText(RenderHelper.ENDANGERED_TOOLTIP);
                    }
                else if (bestStatusHA == 1)
                    {
                    f_txtClusterStatusHA.setBackground(Color.orange);
                    f_txtClusterStatusHA.setForeground(Color.black);
                    f_txtClusterStatusHA.setToolTipText(RenderHelper.NODE_SAFE_TOOLTIP);
                    }
                else
                    {
                    f_txtClusterStatusHA.setBackground(Color.green);
                    f_txtClusterStatusHA.setForeground(Color.black);
                    f_txtClusterStatusHA.setToolTipText(RenderHelper.MACHINE_SAFE_TOOLTIP);
                    }

                f_txtClusterStatusHA.setText(sStatusHA);
                }
            else
                {
                f_txtClusterStatusHA.setText("");
                }
            }

        int    count             = 0;
        double cLoadAverage      = 0;
        double cMax              = -1;
        double cTotalLoadAverage = 0;

        // work out the max and average load averages for the graph
        if (m_machineData != null)
            {
            for (Entry<Object, Data> entry : m_machineData)
                {
                count++;
                cLoadAverage      = (Double) entry.getValue().getColumn(MachineData.SYSTEM_LOAD_AVERAGE);
                cTotalLoadAverage += cLoadAverage;

                if (cMax == -1 || cLoadAverage > cMax)
                    {
                    cMax = cLoadAverage;
                    }
                }

            // update graph
            GraphHelper.addValuesToLoadAverageGraph(f_loadAverageGraph, (float) cMax,
                (float) (cTotalLoadAverage == 0 ? 0 : cTotalLoadAverage / count));
            }
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateData()
        {
        memberData  = f_model.getData(VisualVMModel.DataType.MEMBER);
        m_clusterData = f_model.getData(VisualVMModel.DataType.CLUSTER);
        m_serviceData = f_model.getData(VisualVMModel.DataType.SERVICE);
        m_machineData = f_model.getData(VisualVMModel.DataType.MACHINE);
        }

    // ----- helpers --------------------------------------------------------

    /**
     * Returns the status HA index from 0-4. 0 being ENDANGERED and 4 being SITE-SAFE.
     *
     * @param sStatusHA  the textual version of statusHA
     *
     * @return  the index that the textual version matches
     */
    private int getStatusHAIndex(String sStatusHA)
        {
        for (int i = 0; i < STATUSHA_VALUES.length; i++)
            {
            if (STATUSHA_VALUES[i].equals(sStatusHA))
                {
                return i;
                }
            }

        return -1;

        }

    // ----- constants ------------------------------------------------------

    /**
     * Text value of statusHA.
     */
    private static final String[] STATUSHA_VALUES = new String[] {"ENDANGERED", "NODE-SAFE", "MACHINE-SAFE",
        "RACK-SAFE", "SITE-SAFE"};

    private static final long serialVersionUID = 2602085070795849149L;

    // ----- data members ---------------------------------------------------

    /**
     * The cluster name.
     */
    private final JTextField f_txtClusterName;

    /**
     * The cluster version with anything after the version number stripped out.
     */
    private final JTextField f_txtVersion;

    /**
     * The date when the JConsole tab was refreshed. This is different to the data
     * refresh.
     */
    private final JTextField f_txtRefreshDate;

    /**
     * The cluster statusHA value obtained from all the services that have statusHA.
     */
    private final JTextField f_txtClusterStatusHA;

    /**
     * The cluster size.
     */
    private final JTextField f_txtClusterSize;

    /**
     * The graph of overall cluster memory.
     */
    private final SimpleXYChartSupport f_memoryGraph;

    /**
     * The graph of packet publisher success rates.
     */
    private final SimpleXYChartSupport f_publisherGraph;

    /**
     * The graph of packet receiver success rates.
     */
    private final SimpleXYChartSupport f_receiverGraph;

    /**
     * The graph of primary memory cache size.
     */
    private final SimpleXYChartSupport f_loadAverageGraph;

    /**
     * The member statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> memberData;

    /**
     * The cluster statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_clusterData;

    /**
     * The service statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_serviceData;

    /**
     * The machine statistics data retrieved from the {@link VisualVMModel}.
     */
    private List<Entry<Object, Data>> m_machineData;
    }
