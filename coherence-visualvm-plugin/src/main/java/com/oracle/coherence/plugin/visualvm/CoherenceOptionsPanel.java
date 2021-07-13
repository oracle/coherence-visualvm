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


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.oracle.coherence.plugin.visualvm.panel.util.AbstractMenuOption;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graalvm.visualvm.core.options.UISupport;
import org.graalvm.visualvm.core.ui.components.SectionSeparator;
import org.graalvm.visualvm.core.ui.components.Spacer;

import org.openide.awt.Mnemonics;

import static com.oracle.coherence.plugin.visualvm.Localization.*;


/**
 * A controller for Coherence options.
 *
 * @author Tim Middleton 2021.02.23
 */
public class CoherenceOptionsPanel
        extends JPanel
    {

    // ----- constructors --------------------------------------------------

    /**
     * Constructs a {@link CoherenceOptionsPanel}.
     *
     * @param controller {@link CoherenceOptionsPanelController}
     */
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

    /**
     * Read all settings.
     */
    void load()
        {
        GlobalPreferences preferences = GlobalPreferences.sharedInstance();
        m_refreshTime.setValue(preferences.getRefreshTime());
        m_logQueryTimes.setSelected(preferences.isLogQueryTimes());
        m_disableMBeanCheck.setSelected(preferences.isMBeanCheckDisabled());
        m_restRequestTimout.setValue(preferences.getRestTimeout());
        m_enableRestDebug.setSelected(preferences.isRestDebugEnabled());
        m_enableZoom.setSelected(preferences.isZoomEnabled());
        m_enablePersistenceList.setSelected(preferences.isPersistenceListEnabled());
        m_enableClusterSnapshot.setSelected(preferences.isClusterSnapshotEnabled());
        m_adminFunctionsEnabled.setSelected(preferences.isAdminFunctionEnabled());
        }

    /**
     * Store all settings.
     */
    void store()
        {
        GlobalPreferences preferences = GlobalPreferences.sharedInstance();
        preferences.setRefreshTime((Integer) m_refreshTime.getValue());
        preferences.setLogQueryTimes(m_logQueryTimes.isSelected());
        preferences.setDisableMbeanCheck(m_disableMBeanCheck.isSelected());
        preferences.setRestDebugEnabled(m_enableRestDebug.isSelected());
        preferences.setRestTimeout((Integer) m_restRequestTimout.getValue());
        preferences.setZoomEnabled(m_enableZoom.isSelected());
        preferences.setPersistenceListEnabled(m_enablePersistenceList.isSelected());
        preferences.setClusterSnapshotEnabled(m_enableClusterSnapshot.isSelected());
        preferences.setAdminFunctionsEnabled(m_adminFunctionsEnabled.isSelected());
        }

    /**
     * Ensure that settings are valid.
     *
     * @return true if settings are valid
     */
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

    /**
     * Initialize UI components.
     */
    private void initComponents()
        {
        GridBagConstraints c;

        setLayout(new GridBagLayout());

        // ---- Header General ----
        addHeader(0, "LBL_general");

        // ---- Refresh Time Label ----
        JLabel plottersLabel = new JLabel();
        Mnemonics.setLocalizedText(plottersLabel, getLocalText("LBL_refresh_time"));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(plottersLabel, c);

        // refresh time
        m_refreshTime = new JSpinner();
        m_refreshTime.setToolTipText(getLocalText("TTIP_refresh_time"));
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
        Mnemonics.setLocalizedText(plottersUnits, getLocalText("LBL_seconds")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 0);
        add(plottersUnits, c);

        m_logQueryTimes = new JCheckBox();
        m_logQueryTimes.setToolTipText(getLocalText("TTIP_log_query_times"));
        addCheckBox(2, "LBL_log_query_times", m_logQueryTimes);

        m_disableMBeanCheck = new JCheckBox();
        m_disableMBeanCheck.setToolTipText(getLocalText("TTIP_disable_mbean_check"));
        addCheckBox(3, "LBL_disable_mbean_check", m_disableMBeanCheck);

        // ---- REST ----
        addHeader(4, "LBL_rest");

        // ---- REST Request Timeout ----
        JLabel lblRest = new JLabel();
        Mnemonics.setLocalizedText(lblRest, getLocalText("LBL_rest_request_timeout"));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(lblRest, c);

        m_restRequestTimout = new JSpinner();
        m_restRequestTimout.setToolTipText(getLocalText("TTIP_rest_request_timeout"));
        lblRest.setLabelFor(m_restRequestTimout);
        m_restRequestTimout.setModel(new SpinnerNumberModel(30000, 1000, 99999999, 1000));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 4);
        add(m_restRequestTimout, c);

        JLabel requestUnits = new JLabel();
        Mnemonics.setLocalizedText(requestUnits, getLocalText("LBL_millis")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 0);
        add(requestUnits, c);

        m_enableRestDebug = new JCheckBox();
        m_enableRestDebug.setToolTipText(getLocalText("TTIP_rest_debug"));
        addCheckBox(6, "LBL_enable_rest_debug", m_enableRestDebug);

        // ---- Other / Experimental ----
        addHeader(7, "LBL_other");

        m_enablePersistenceList = new JCheckBox();
        m_enablePersistenceList.setToolTipText(getLocalText("TTIP_persistence_list"));
        addCheckBox(8, "LBL_enable_persistence_list", m_enablePersistenceList);

        m_enableZoom = new JCheckBox();
        m_enableZoom.setToolTipText(getLocalText("TTIP_zoom_enabled"));
        addCheckBox(9, "LBL_enable_zoom", m_enableZoom);

        m_enableClusterSnapshot = new JCheckBox();
        m_enableClusterSnapshot.setToolTipText(getLocalText("TTIP_enable_cluster_snapshot"));
        addCheckBox(10, "LBL_enable_cluster_snapshot", m_enableClusterSnapshot);

        m_adminFunctionsEnabled = new JCheckBox();
        m_adminFunctionsEnabled.setToolTipText(getLocalText("TTIP_enable_cluster_head_dump"));
        addCheckBox(11, "LBL_enable_admin_functions", m_adminFunctionsEnabled);

        m_btnAnalyzeUnavailableTime = new JButton(Localization.getLocalText("LBL_analyze_log_file"));
        m_btnAnalyzeUnavailableTime.setMnemonic(KeyEvent.VK_A);
        m_btnAnalyzeUnavailableTime.setToolTipText(Localization.getLocalText("TTIP_LBL_analyze_log_file"));
        m_btnAnalyzeUnavailableTime.addActionListener(event ->
            {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                File file = fc.getSelectedFile();

                boolean fVerbose = JOptionPane.showConfirmDialog(null,
                        Localization.getLocalText("LBL_verbose"),
                        Localization.getLocalText("LBL_confirm_operation"),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

                // analyze the file
                UnavailabilityTimeAnalyzer analyzer = new UnavailabilityTimeAnalyzer(file);
                AbstractMenuOption.showMessageDialog(Localization.getLocalText("LBL_result"),
                        analyzer.analyze(fVerbose),
                        JOptionPane.INFORMATION_MESSAGE, 500, 400, true);
                }
            });

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 12;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(m_btnAnalyzeUnavailableTime, c);

        JLabel appsLabel = new JLabel();
        Mnemonics.setLocalizedText(appsLabel, getLocalText("LBL_reconnect")); // NOI18N
        c = new GridBagConstraints();
        c.gridy = 14;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 15, 6, 0);
        add(appsLabel, c);

        // filler
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 15;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(Spacer.create(), c);
        }

    /**
     * Adds a checkbox.
     *
     * @param y        y position
     * @param sLabel   label bundle key
     * @param checkBox the {@link JCheckBox}
     */
    private void addCheckBox(int y, String sLabel, JCheckBox checkBox)
        {
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, getLocalText(sLabel));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 15, 3, 0);
        add(label, c);

        label.setLabelFor(checkBox);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 3, 4);
        add(checkBox, c);
        }

    /**
     * Adds a header.
     *
     * @param y      y position
     * @param sLabel the {@link JCheckBox}
     */
    private void addHeader(int y, String sLabel)
        {
        SectionSeparator sectionSeparator = UISupport.createSectionSeparator(getLocalText(sLabel));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        add(sectionSeparator, c);
        }

    /**
     * Start tracking changes.
     */
    private void startTrackingChanges()
        {
        m_refreshTime.getModel().addChangeListener(changeListener);
        m_logQueryTimes.getModel().addChangeListener(changeListener);
        m_disableMBeanCheck.getModel().addChangeListener(changeListener);
        m_enableRestDebug.getModel().addChangeListener(changeListener);
        m_restRequestTimout.getModel().addChangeListener(changeListener);
        m_enableZoom.getModel().addChangeListener(changeListener);
        m_enablePersistenceList.getModel().addChangeListener(changeListener);
        m_enableClusterSnapshot.getModel().addChangeListener(changeListener);
        m_adminFunctionsEnabled.getModel().addChangeListener(changeListener);
        }

    //----- inner classes ---------------------------------------------------

    /**
     * Class to analyze log files where Partition Events Logging has been enabled
     * and subsequently partition unavailable time is being logged. This feature is experimental and
     * may be removed or changed in the future.
     * The following must be set to enabled this feature in Coherence 21.06 and above.
     * <pre>
     *    -Dcoherence.distributed.partition.events=log
     *    -Dcoherence.log.level=8
     * </pre>
     *
     * See: https://coherence.community/21.06/docs/#/docs/core/07_partition_events_logging.
     */
    public static class UnavailabilityTimeAnalyzer
        {
        /**
         * Constructor.
         * @param fileLogFile log file to analyze
         */
        public UnavailabilityTimeAnalyzer(File fileLogFile)
            {
            this.f_fileLogFile = fileLogFile;
            }

        /**
         * Analyze the results.
         *
         * @param fVerbose indicates if output should be verbose
         *
         * @return the results
         */
        public String analyze(boolean fVerbose)
            {
            StringBuilder sb = new StringBuilder("Analysis of log file: ").append(f_fileLogFile.getAbsolutePath())
                                      .append("\n")
                                      .append("Date: ")
                                      .append(new Date())
                                      .append('\n');

            try
                {
                if (!f_fileLogFile.canRead())
                    {
                    sb.append("Unable to read file.");
                    }
                else
                    {
                    List<String> listLines = Files.readAllLines(f_fileLogFile.toPath());
                    Map<Long, String> mapLines = new HashMap<>();
                    AtomicLong lineId = new AtomicLong(0);

                    Set<UnavailabilityMetrics> setMetrics =
                        listLines.stream()
                                 .filter(s -> (s.contains(PARTITION_ID) || s.contains(PARTITION_SET)) &&
                                              s.contains(UNAVAILABLE_TIME) &&
                                              s.contains(OWNER) &&
                                              s.contains(ACTION))
                                 .map(s ->
                                     {
                                     // save the line for inclusion below
                                     long nLineId = lineId.incrementAndGet();
                                     mapLines.put(nLineId, s);
                                     return  nLineId + " " +
                                             s.replaceAll("^.*thread=","")
                                              .replaceAll(", .*" + PARTITION_ID, "")
                                              .replaceAll(", .*" + PARTITION_SET, " ")
                                              .replaceAll(", " + OWNER, "")
                                              .replaceAll(", " + ACTION, "")
                                              .replaceAll(", " + UNAVAILABLE_TIME, "");
                                     })
                                 .map(s -> s.split(" "))
                                 .filter(a -> a.length == 6)
                                 .map(a -> {
                                     // determine the service name from the thread as it is not always obvious
                                     String sServiceName = a[1].replaceAll("Dedicated.*$", "")
                                            .replaceAll("DistributedCache:", "")
                                            .replaceAll("FederatedCache:", "");

                                     // check for initial PartitionSet ASSIGN and allocate a partition id of -1 meaning all partitions
                                     String sPartitionId = a[2];
                                     if (sPartitionId.startsWith("{")) {
                                         sPartitionId = "-1";
                                     }
                                     return new UnavailabilityMetrics(sServiceName, Integer.parseInt(a[3]), Integer.parseInt(sPartitionId), a[4],
                                             Long.parseLong(a[5]),  Long.parseLong(a[0]));
                                 })
                                 .collect(Collectors.toSet());

                    // get the unique list of services
                    Set<String> setServices = setMetrics.stream()
                                                     .map(UnavailabilityMetrics::getServiceName)
                                                     .collect(Collectors.toSet());
                    if (setServices.size() == 0) {
                        sb.append("No services found. This may not be a Coherence log file.");
                        return sb.toString();
                    }

                    sb.append(String.format("Total lines in file: %,d, total matching lines processed: %,d\n", listLines.size(), lineId.get()));

                    int nMaxLength = Math.max(setServices.stream().mapToInt(String::length).max().getAsInt(), 23);
                    long nTotalMillis = setMetrics.stream().mapToLong(UnavailabilityMetrics::getMillis).sum();

                    Map<String, Long> mapSumByService = setMetrics.stream().collect(
                                Collectors.groupingBy(UnavailabilityMetrics::getServiceName,
                                        Collectors.summingLong(UnavailabilityMetrics::getMillis)));

                    // total unavailability time per service
                    Map<String, LongSummaryStatistics> mapServiceStats = setMetrics.stream().collect(
                                Collectors.groupingBy(UnavailabilityMetrics::getServiceName,
                                        Collectors.summarizingLong(UnavailabilityMetrics::getMillis)));

                    // total unavailability time per action
                    Map<String, LongSummaryStatistics> mapActionStats = setMetrics.stream().collect(
                                Collectors.groupingBy(UnavailabilityMetrics::getAction,
                                        Collectors.summarizingLong(UnavailabilityMetrics::getMillis)));

                    String sLineFormat = "%-" + (nMaxLength + 2) + "s %,10d %,10d %,10d %13.2f %,14d %9.2f%%\n";
                    String sHeaderFormat = "%-" + (nMaxLength + 2) + "s %10s %10s %10s %13s %14s %9s\n";

                    sb.append("\nSummary by Service. Total unavailable millis: ")
                      .append(String.format("%,d", nTotalMillis))
                      .append('\n')
                      .append(String.format(sHeaderFormat, "Service Name", "    Count", "  Min (ms)", "  Max (ms)", " Average (ms)", "    Total (ms)", "   Percent"));

                    mapServiceStats.forEach((k, v) -> sb.append(formatLine(sLineFormat, nTotalMillis, k, v)));

                    sb.append("\nSummary by Action (All Services)\n")
                      .append(String.format(sHeaderFormat, "Action", "    Count", "  Min (ms)", "  Max (ms)", " Average (ms)", "    Total (ms)", "   Percent"));

                    mapActionStats.forEach((k, v) -> sb.append(formatLine(sLineFormat, nTotalMillis, k, v)));

                    // get times per service
                    setServices.forEach(s ->
                        {
                        long nServiceTotal = mapSumByService.get(s);
                        sb.append("\nDetails for Service: ").append(s)
                          .append(". Total unavailable millis: ")
                          .append(String.format("%,d", nServiceTotal))
                          .append('\n');

                        Map<String, LongSummaryStatistics> mapActionStatsPerService = setMetrics.stream()
                                .filter(m -> m.getServiceName().equals(s))
                                .collect(Collectors.groupingBy(UnavailabilityMetrics::getAction,
                                        Collectors.summarizingLong(UnavailabilityMetrics::getMillis)));
                        sb.append("Summary by Action\n")
                          .append(String.format(sHeaderFormat, "Action", "    Count", "  Min (ms)", "  Max (ms)", " Average (ms)", "    Total (ms)", "   Percent"));

                        mapActionStatsPerService.forEach((k, v) -> sb.append(formatLine(sLineFormat, nServiceTotal, k, v)));

                        if (fVerbose)
                            {
                            // top 10 partitions by unavailable time
                            Map<Integer, LongSummaryStatistics> mapTop10Partitions = setMetrics.stream()
                                    .filter(m -> m.getServiceName().equals(s))
                                    .collect(Collectors.groupingBy(UnavailabilityMetrics::getPartitionId,
                                             Collectors.summarizingLong(UnavailabilityMetrics::getMillis)));

                            Map<Integer, Long> mapByTotalMillis = mapTop10Partitions.entrySet().stream().collect(
                                    Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSum()));
                            List<Map.Entry<Integer, Long>> listSorted = new ArrayList<>(mapByTotalMillis.entrySet());
                            listSorted.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

                            sb.append("\nTop 10 partitions by total unavailable time\n");
                            listSorted.stream()
                                  .limit(10)
                                  .forEach((e) ->
                                      {
                                      int nPartitionId = e.getKey();
                                      sb.append(String.format("- Partition: %d, total millis: %,d\n", nPartitionId,
                                              e.getValue()));
                                      setMetrics.stream()
                                                .filter(m->m.getServiceName().equals(s) && m.getPartitionId() == nPartitionId)
                                                .forEach(m->sb.append(String.format("  %s\n", mapLines.get(m.getLineId()))));
                                      });

                            sb.append("\nOutput by partition and event for ").append(s).append('\n');

                            // retrieve a unique set of partitions
                            Set<Integer> setPartitions = setMetrics.stream()
                                       .map(UnavailabilityMetrics::getPartitionId)
                                       .collect(Collectors.toSet());

                            setPartitions.stream().filter(p -> p >= 0).forEach(p ->
                                {
                                sb.append("- Partition ").append(p).append('\n');
                                // retrieve the initial partition assign which has been marked with partition of -1
                                setMetrics.stream().filter(m -> m.getServiceName().equals(s) && m.getPartitionId() == -1)
                                       .forEach(m -> sb.append(String.format("  %s\n", mapLines.get(m.getLineId()))));
                                setMetrics.stream()
                                       .filter(m -> m.getServiceName().equals(s) && m.getPartitionId() == p)
                                       .forEach(m -> sb.append(String.format("  %s\n", mapLines.get(m.getLineId()))));
                                });
                            }
                        });
                    }
                }
            catch (Exception e)
                {
                sb.append("Unable to process file. This may not be a Coherence log file.").append(e.getMessage());
                }

            return sb.toString();
            }

        /**
         * Format a line for output.
         *
         * @param sFormat        printf format
         * @param nTotalMillis   total millis unavailable
         * @param sServiceName   service name
         * @param metrics        {@link LongSummaryStatistics}
         * @return formatted line
         */
        private String formatLine(String sFormat, long nTotalMillis, String sServiceName, LongSummaryStatistics metrics)
            {
            return String.format(sFormat, sServiceName, metrics.getCount(), metrics.getMin(), metrics.getMax(), metrics.getAverage(), metrics.getSum(),
                               (metrics.getSum() * 1.0f / nTotalMillis) * 100);
            }

        // ----- constants -----------------------------------------------

        private static final String PARTITION_ID = "PartitionId:";
        private static final String PARTITION_SET = "PartitionSet";
        private static final String OWNER = "Owner:";
        private static final String ACTION = "Action:";
        private static final String UNAVAILABLE_TIME = "UnavailableTime:";

        // ----- data members -----------------------------------------------

        /**
         * Log file to analyze.
         */
        private final File f_fileLogFile;
        }

    public static class UnavailabilityMetrics
        {
        public UnavailabilityMetrics(String sServiceName, int nMember, int nPartitionId, String sAction, long millis, long nLineId)
            {
            this.f_sServiceName = sServiceName;
            this.f_nMember = nMember;
            this.f_nPartitionId = nPartitionId;
            this.f_sAction = sAction;
            this.f_millis = millis;
            this.f_nLineId = nLineId;
            }

        public String getServiceName()
            {
            return f_sServiceName;
            }

        public int getMember()
            {
            return f_nMember;
            }

        public int getPartitionId()
            {
            return f_nPartitionId;
            }

        public String getAction()
            {
            return f_sAction;
            }

        public long getMillis()
            {
            return f_millis;
            }

        public long getLineId() {
            return f_nLineId;
        }

            @Override
        public String toString()
            {
            return "UnavailabilityMetrics{" +
                   "sServiceName='" + f_sServiceName + '\'' +
                   ", nMember=" + f_nMember +
                   ", nPartitionId=" + f_nPartitionId +
                   ", sAction='" + f_sAction + '\'' +
                   ", millis=" + f_millis +
                   ", lineId=" + f_nLineId +
                   '}';
            }

            private final String f_sServiceName;
            private final int    f_nMember;
            private final int    f_nPartitionId;
            private final String f_sAction;
            private final long   f_millis;
            private final long   f_nLineId;
        }

    //----- data members ----------------------------------------------------

    /**
     * Controller associated with this panel.
     */
    private final CoherenceOptionsPanelController f_controller;

    /**
     * Refresh time spinner.
     */
    private JSpinner m_refreshTime;

    /**
     * Reqest request time spinner.
     */
    private JSpinner m_restRequestTimout;

    /**
     * Log query times checkbox.
     */
    private JCheckBox m_logQueryTimes;

    /**
     * Disable MBean Check checkbox.
     */
    private JCheckBox m_disableMBeanCheck;

    /**
     * Enable REST Debug checkbox.
     */
    private JCheckBox m_enableRestDebug;

    /**
     * Enable HeatMap checkbox.
     */
    private JCheckBox m_enableHeatMap;

    /**
     * Enable Zoom checkbox.
     */
    private JCheckBox m_enableZoom;

    /**
     * Enable Persistence list checkbox.
     */
    private JCheckBox m_enablePersistenceList;

    /**
     * Enable cluster snapshot checkbox.
     */
    private JCheckBox m_enableClusterSnapshot;

    /**
     * Enable admin functions.
     */
    private JCheckBox m_adminFunctionsEnabled;

    /**
     * A button to analyze unavailable time in a log file.
     */
    private JButton m_btnAnalyzeUnavailableTime = null;
    }
