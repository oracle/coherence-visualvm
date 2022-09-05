/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.coherence.plugin.visualvm.tests;

import java.io.File;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceCacheServer;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.JMXManagementMode;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.Logging;
import com.oracle.bedrock.runtime.coherence.options.Multicast;
import com.oracle.bedrock.runtime.coherence.options.OperationalOverride;
import com.oracle.bedrock.runtime.coherence.options.RoleName;
import com.oracle.bedrock.runtime.coherence.options.WellKnownAddress;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.java.profiles.JmxProfile;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.testsupport.junit.TestLogs;

import com.oracle.coherence.plugin.visualvm.VisualVMModel;
import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.JMXRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.AbstractData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;

import com.tangosol.io.FileHelper;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import org.junit.ClassRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Base class for VisualVM tests.
 */
public abstract class AbstractVisualVMTest
    {
    // ----- constructors --------------------------------------------------

    /**
     * Default constructor.
     */
    public AbstractVisualVMTest()
        {
        }

    // ----- test methods ---------------------------------------------------

    /**
     * Start the required cache servers using oracle-tools
     *
     * @param fRestRequestSender  if a REST request sender needs to be used
     */
    public static void startupCacheServers(boolean fRestRequestSender)
        {
        try
            {
            s_sEdition = CacheFactory.getEdition();

            // setup temporary directories for persistence
            s_fileActiveDirA = FileHelper.createTempDir();
            s_fileSnapshotDirA = FileHelper.createTempDir();
            s_fileTrashDirA = FileHelper.createTempDir();

            s_availablePortIteratorWKA = LocalPlatform.get().getAvailablePorts();

            // Iterate to next port and waste this as sometimes the second port will
            // already be used up before the realize()
            s_availablePortIteratorWKA.next();

            int nClusterAPort = s_availablePortIteratorWKA.next();
            int nClusterBPort = s_availablePortIteratorWKA.next();

            LocalPlatform platform        = LocalPlatform.get();
            OptionsByType optionsByTypeA1 = createCacheServerOptions(CLUSTER_NAME, s_fileActiveDirA,
                    s_fileSnapshotDirA, s_fileTrashDirA);
            OptionsByType optionsByTypeA2 = createCacheServerOptions(CLUSTER_NAME, s_fileActiveDirA,
                    s_fileSnapshotDirA, s_fileTrashDirA);

            OptionsByType optionsByTypeMemberA1 = OptionsByType.of(optionsByTypeA1).add(DisplayName.of("memberA1"));
            OptionsByType optionsByTypeMemberA2 = OptionsByType.of(optionsByTypeA2).add(DisplayName.of("memberA2"));

            if (fRestRequestSender)
                {
                optionsByTypeMemberA1.add(SystemProperty.of("coherence.management", "dynamic"));
                optionsByTypeMemberA1.add(SystemProperty.of("coherence.management.http", "inherit"));
                optionsByTypeMemberA1.add(SystemProperty.of("coherence.management.http.host", REST_MGMT_HOST));
                optionsByTypeMemberA1.add(SystemProperty.of("coherence.management.http.port", REST_MGMT_PORT));
                }

            s_memberA1 = platform.launch(CoherenceCacheServer.class, optionsByTypeMemberA1.asArray());
            s_memberA2 = platform.launch(CoherenceCacheServer.class, optionsByTypeMemberA2.asArray());

            s_requestSender = fRestRequestSender
                    ? new HttpRequestSender("http://" + REST_MGMT_HOST + ":" + "8080")
                    : new JMXRequestSender(s_memberA1.get(JmxFeature.class)
                                                     .getDeferredJMXConnector().get().getMBeanServerConnection());

            Eventually.assertThat(invoking(s_memberA1).getClusterSize(), is(2));

            // only start second cluster if we are running Commercial version
            if (isCommercial())
                {
                // setup temporary directories for persistence
                s_fileActiveDirB = FileHelper.createTempDir();
                s_fileSnapshotDirB = FileHelper.createTempDir();
                s_fileTrashDirB = FileHelper.createTempDir();

                OptionsByType optionsByTypeB1 = createCacheServerOptions(CLUSTER_B_NAME, s_fileActiveDirB,
                        s_fileSnapshotDirB, s_fileTrashDirB);
                OptionsByType optionsByTypeB2 = createCacheServerOptions(CLUSTER_B_NAME, s_fileActiveDirB,
                        s_fileSnapshotDirB, s_fileTrashDirB);

                OptionsByType optionsByTypeMemberB1 = OptionsByType.of(optionsByTypeB1).add(DisplayName.of("memberB1"));
                OptionsByType optionsByTypeMemberB2 = OptionsByType.of(optionsByTypeB2).add(DisplayName.of("memberB2"));

                if (fRestRequestSender)
                    {
                    optionsByTypeMemberB1.add(SystemProperty.of("coherence.management", "dynamic"));
                    optionsByTypeMemberB1.add(SystemProperty.of("coherence.management.http", "inherit"));
                    optionsByTypeMemberB1.add(SystemProperty.of("coherence.management.http.host", REST_MGMT_HOST));
                    optionsByTypeMemberB1.add(SystemProperty.of("coherence.management.http.port", REST_MGMT_PORT2));
                    }

                s_memberB1 = platform.launch(CoherenceCacheServer.class, optionsByTypeMemberB1.asArray());
                s_memberB2 = platform.launch(CoherenceCacheServer.class, optionsByTypeMemberB2.asArray());

                Eventually.assertThat(invoking(s_memberB1).getClusterSize(), is(2));
                }
            }
        catch (Exception e)
            {
            System.err.println("Error starting cache servers. " + e.getMessage());
            e.printStackTrace();

            throw new RuntimeException(e);
            }

        }

    /**
     * Shutdown all cache servers after the test.
     */
    public static void shutdownCacheServers()
        {
        CacheFactory.shutdown();

        destroyMember(s_memberA1);
        destroyMember(s_memberA2);

        safeDelete(s_fileActiveDirA);
        safeDelete(s_fileSnapshotDirA);
        safeDelete(s_fileTrashDirA);

        if (isCommercial())
            {
            destroyMember(s_memberB1);
            destroyMember(s_memberB2);
            safeDelete(s_fileActiveDirB);
            safeDelete(s_fileSnapshotDirB);
            safeDelete(s_fileTrashDirB);
            }
        }

    // ----- helpers --------------------------------------------------------

    private static void safeDelete(File file)
        {
        try
            {
            FileHelper.deleteDir(file);
            }
        catch (Throwable thrown)
            {
            // ignored
            }
        }

    /**
     * Destroy a member and ignore any errors.
     *
     * @param member the {@link CoherenceClusterMember} to destroy
     */
    private static void destroyMember(CoherenceClusterMember member)
        {
        try
            {
            if (member != null)
                {
                member.close();
                }
            }
        catch (Throwable thrown)
            {
            // ignored
            }
        }

    /**
     * Establish {@link OptionsByType} to use launching cache servers.
     *
     * @param sClusterName      the cluster name
     * @param fileActiveDir     active persistence directory
     * @param fileSnapshotDir   snapshot persistence directory
     * @param fileTrashDir      trash persistence directory
     *
     * @return an {@link OptionsByType}
     */
    protected static OptionsByType createCacheServerOptions(String sClusterName, File fileActiveDir,
                                                            File fileSnapshotDir, File fileTrashDir)
        {
        String        hostName      = "127.0.0.1";
        OptionsByType optionsByType = OptionsByType.empty();

        optionsByType.addAll(JMXManagementMode.ALL,
                             JmxProfile.enabled(),
                             LocalStorage.enabled(),
                             LocalHost.of(hostName),
                             WellKnownAddress.of(hostName),
                             Multicast.ttl(0),
                             CacheConfig.of(System.getProperty("tangosol.coherence.cacheconfig")),
                             Logging.at(9),
                             ClusterName.of(sClusterName),
                             SystemProperty.of("partition.count", Integer.toString(PARTITION_COUNT)),
                             SystemProperty.of("tangosol.coherence.management.refresh.expiry", "1s"),
                             SystemProperty.of("active.dir", fileActiveDir.getAbsolutePath()),
                             SystemProperty.of("snapshot.dir", fileSnapshotDir.getAbsolutePath()),
                             SystemProperty.of("trash.dir", fileTrashDir.getAbsolutePath()),
                             OperationalOverride.of(System.getProperty("tangosol.coherence.override")),
                             SystemProperty.of("java.rmi.server.hostname", hostName),
                             SystemProperty.of("coherence.distribution.2server", "false"),
                             SystemProperty.of("loopbackhost", hostName),
                             RoleName.of(ROLE_NAME),
                             s_logs.builder());

        // check if HealthCheck class is available, then use Coherence to start
        if (isHealthCheckAvailable())
            {
            try
                {
                optionsByType.add(ClassName.of(Class.forName("com.tangosol.net.Coherence")));
                }
            catch (ClassNotFoundException e)
                {
                // ignore as it should not fail as we have already done the check
                }
            }

        return optionsByType;
        }

    /**
     * Returns true if health check is available.
     *
     * @return true if health check is available
     */
    protected static boolean isHealthCheckAvailable()
        {
        try
            {
            Class.forName(HEALTH_CLASS);
            // health check available
            return true;
            }
        catch (ClassNotFoundException e)
            {
            return false;
            }
        }

    /**
     * Validate the generated data for basic completeness including the number of rows
     * returned and column count for each row.
     *
     * @param type   The type of the data
     * @param data   the generated data
     * @param nExpectedSize the expected size
     */
    protected void validateData(VisualVMModel.DataType type, List<Map.Entry<Object, Data>> data, int nExpectedSize)
        {
        String sClass = type.getClassName().toString();

        // if the type is Member then add one as the number of columns displayed is
        // different to the number of columns in the model. This is the only type that has this difference.
        int cColumns = type.getMetadata().length + (type.equals(VisualVMModel.DataType.MEMBER) ? 1 : 0);

        assertThat("Data for " + sClass + " should not be null", data, is(notNullValue()));

        // now check that each row has the expected number of columns
        for (Map.Entry<Object, Data> entry : data)
            {
            AbstractData dataItem = (AbstractData) entry.getValue();

            assertEquals("Number of elements for " + sClass + " should be " + cColumns + " but is "
                                        + dataItem.getColumnCount() + ". Entry is " + entry.getValue(), dataItem.getColumnCount(), cColumns);
            }

        }

    /**
     * Assert that the cluster is ready for the tests.
     */
    protected void assertClusterReady()
        {
        assertThat(s_requestSender, is(notNullValue()));
        assertThat("Cluster size should be 2 but is " + s_memberA1.getClusterSize(), s_memberA1.getClusterSize(), is(2));

        if (isCommercial())
            {
            assertThat("Cluster size should be 2 but is " + s_memberB1.getClusterSize(), s_memberB1.getClusterSize(), is(2));
            }
        }

    /**
     * Populate a given {@link NamedCache} with data.
     *
     * @param nc        {@link NamedCache} to populate
     * @param cEntries  the number of entries to add
     * @param nSize     the size of each entry
     */
    protected void populateRandomData(NamedCache nc, int cEntries, int nSize)
        {
        char[]               bData  = new char[nSize];
        Map<Integer, String> buffer = new HashMap<>();

        for (int i = 0; i < nSize; i++)
            {
            bData[i] = ((i % 2 == 0) ? 'X' : 'Y');
            }

        String sData = Arrays.toString(bData);

        for (int j = 0; j < cEntries; j++)
            {
            buffer.put(j, sData);
            }

        nc.putAll(buffer);
        buffer.clear();

        assertThat("NamedCache " + nc.getCacheName() + " should contain " + cEntries + " but contains "
                     + nc.size(), nc.size(), is(cEntries));
        }

    /**
     * Set the current data type for ease of use for the validateColumn() method
     *
     * @param dataType the dataType to set
     */
    protected void setCurrentDataType(VisualVMModel.DataType dataType)
        {
        this.dataType = dataType;
        }

    /**
     * Get the given column for the current dataType. This is a helper method and
     * does some sanity checking to ensure that the returned column is correct
     * and not null.
     *
     * @param nColumn         the column index to validate
     * @param entry           the entry which contains the retrieved value
     */
    protected Object getColumn(int nColumn, Map.Entry<Object, Data> entry)
        {
        assertThat("You must call setCurrentDataType before this call", dataType, is(notNullValue()));

        Data data = entry.getValue();

        assertThat(data, is(notNullValue()));

        return entry.getValue().getColumn(nColumn); // actual value
        }

    /**
     * Validate a given column for the current dataType. This is a helper method and
     * does some sanity checking to ensure that the returned column is not null only.
     *
     * @param nColumn         the column index to validate
     * @param entry           the entry which contains the retrieved value
     */
    protected void validateColumnNotNull(int nColumn, Map.Entry<Object, Data> entry)
        {
        assertThat(getColumn(nColumn, entry), is(notNullValue()));
        }

    /**
     * Validate a given column for the current dataType. This is a helper method and
     * does some sanity checking to ensure that the returned column is correct
     * and not null.
     *
     * @param nColumn         the column index to validate
     * @param entry           the entry which contains the retrieved value
     * @param oExpectedValue  the expected value
     */
    protected void validateColumn(int nColumn, Map.Entry<Object, Data> entry, Object oExpectedValue)
        {
        Object oActualValue = getColumn(nColumn, entry);

        String sColumn = dataType.getMetadata()[nColumn];

        String sError  = "The value of column \"" + sColumn + "\" with index " + nColumn + " was expected to be \""
                         + oExpectedValue + "\" but was \"" + oActualValue + "\"";

        if (oExpectedValue instanceof String)
            {
            assertThat(sError, oActualValue.toString(), is(oExpectedValue));
            }
        else if (oExpectedValue instanceof Float)
            {
            assertEquals(sError, ((Float) oExpectedValue).floatValue(), ((Float) oActualValue).floatValue(), 0.0);
            }
        else if (oExpectedValue instanceof Integer)
            {
            assertThat(sError, ((Integer) oExpectedValue).intValue(), is(((Integer) oActualValue).intValue()));
            }
        else if (oExpectedValue instanceof Long)
            {
            assertThat(sError, ((Long) oExpectedValue).longValue(), is(((Long) oActualValue).longValue()));
            }
        else
            {
            throw new RuntimeException("Unable to validate type " + oExpectedValue.getClass().toString() + " for \""
                                       + sColumn + "\"");
            }
        }

    protected static void wait(String sMessage, long nMillis)
        {
        try
            {
            Thread.sleep(nMillis);
            }
        catch (InterruptedException e)
            {
            e.printStackTrace();
            }
        }

    /**
     * Returns true if this is being run using a commercial edition of Coherence.
     * @return true if this is being run using a commercial edition of Coherence.
     */
    protected static boolean isCommercial()
        {
        return !s_sEdition.equals("CE");
        }

    /**
     * Returns the {@link VisualVMModel}.
     * @return the {@link VisualVMModel}
     */
    protected VisualVMModel getModel()
        {
        return m_model;
        }

    /**
     * Sets the {@link VisualVMModel}.
     * @param model the {@link VisualVMModel}
     */
    protected void setModel(VisualVMModel model)
        {
        m_model = model;
        }

    /**
     * Returns the {@link RequestSender} to be used for this test.
     * @return the {@link RequestSender} to be used for this test
     */
    protected static RequestSender getRequestSender()
        {
        return s_requestSender;
        }

    // ----- constants ------------------------------------------------------

    /**
     * Field description
     */
    public static final String ROLE_NAME = "CacheServer";

    /**
     * Active persistence directory for ClusterA.
     */
    public static File s_fileActiveDirA;

    /**
     * Snapshot persistence directory for ClusterA.
     */
    public static File s_fileSnapshotDirA;

    /**
     * Trash persistence directory for ClusterA.
     */
    public static File s_fileTrashDirA;

    /**
     * Active persistence directory for ClusterB.
     */
    public static File s_fileActiveDirB;

    /**
     * Snapshot persistence directory for ClusterB.
     */
    public static File s_fileSnapshotDirB;

    /**
     * Trash persistence directory for ClusterB.
     */
    public static File s_fileTrashDirB;

    /**
     * Partition count.
     */
    protected static int PARTITION_COUNT = 277;

    /**
     * Cluster A Name.
     */
    protected static String CLUSTER_NAME   = "ClusterA";

    /**
     * Cluster B Name.
     */
    protected static String CLUSTER_B_NAME = "ClusterB";

    /**
     * Rest Management Host.
     */
    protected static String REST_MGMT_HOST = "127.0.0.1";

    /**
     * Rest Management Port
     */
    protected static String REST_MGMT_PORT = "8080";

    /**
     * Rest Management Port2
     */
    protected static String REST_MGMT_PORT2 = "8081";

    /**
     * port iterator for ephemeral ports.
     */
    protected static AvailablePortIterator s_availablePortIteratorWKA;

    /**
     * First cluster member of ClusterA.
     */
    protected static CoherenceCacheServer s_memberA1 = null;

    /**
     * Second cluster member of ClusterA.
     */
    protected static CoherenceCacheServer s_memberA2 = null;

    /**
     * First cluster member of ClusterB.
     */
    protected static CoherenceCacheServer s_memberB1 = null;

    /**
     * Second cluster member of ClusterB.
     */
    protected static CoherenceCacheServer s_memberB2 = null;

    /**
     * Connection to MBean server.
     */
    private static RequestSender s_requestSender = null;

    /**
     * The edition we are running under.
     */
    private static String s_sEdition;

    /**
     * HealthCheck class.
     */
    private static final String HEALTH_CLASS = "com.tangosol.util.HealthCheck";

    // ----- data members -----------------------------------------------

    @ClassRule
    public static final TestLogs s_logs = new TestLogs(AbstractVisualVMTest.class);

    /**
     * Currently selected dataType for use by helper methods.
     */
    protected VisualVMModel.DataType dataType;

    /**
     * The model used to store the collected stats.
     */
    private VisualVMModel m_model = null;
    }
