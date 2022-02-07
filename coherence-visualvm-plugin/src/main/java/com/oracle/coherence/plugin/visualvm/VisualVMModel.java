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
package com.oracle.coherence.plugin.visualvm;

import com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender;
import com.oracle.coherence.plugin.visualvm.helper.RequestSender;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheFrontDetailData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.CacheStorageManagerData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ClusterData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Data;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.DataRetriever;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ExecutorData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationDestinationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationDestinationDetailsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationOriginData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FederationOriginDetailsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.FlashJournalData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.GrpcProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HotCacheData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HotCachePerCacheData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpProxyMemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.HttpSessionData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.JCacheConfigurationData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.JCacheStatisticsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MachineData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.MemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.NodeStorageData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceNotificationsData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ProxyData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.RamJournalData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.ServiceMemberData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.TopicData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Tuple;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;


/**
 * A class that is used to store and update Coherence cluster
 * JMX statistics. This is used to avoid placing too much stress on
 * the Management service of a cluster.
 *
 * @author tam  2013.11.14
 * @since  12.1.3
 */
public class VisualVMModel
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Create a new VisualVModel for monitoring Coherence clusters in JMX.
     */
    public VisualVMModel()
        {
        }

    // ---- helper methods --------------------------------------------------

    /**
     * Initialize anything for this instance of the model.
     */
    private void init()
        {
        m_nRefreshTime = getRefreshTime();
        m_fLogJMXQueryTimes = isLogQueryTimes();

        String sReporterDisabled = System.getProperty(PROP_REPORTER_DISABLED);
        // if this option is set we are specifically disabling the reporter even if Coherence
        // version >= 12.1.3
        if ("true".equalsIgnoreCase(sReporterDisabled))
            {
            setReporterAvailable(false);
            }

        // force update on first time
        m_ldtLastUpdate = System.currentTimeMillis() - m_nRefreshTime - 1L;

        // populate mapCollectedData which contains an entry for each type
        m_mapCollectedData = new HashMap<DataType, List<Entry<Object, Data>>>();

        for (DataType type : DataType.values())
            {
            m_mapCollectedData.put(type, null);
            }

        // intialize the data retrievers map
        f_mapDataRetrievers.put(CacheData.class, new CacheData());
        f_mapDataRetrievers.put(ClusterData.class, new ClusterData());
        f_mapDataRetrievers.put(MemberData.class, new MemberData());
        f_mapDataRetrievers.put(ServiceData.class, new ServiceData());
        f_mapDataRetrievers.put(ServiceMemberData.class, new ServiceMemberData());
        f_mapDataRetrievers.put(ProxyData.class, new ProxyData());
        f_mapDataRetrievers.put(MachineData.class, new MachineData());
        f_mapDataRetrievers.put(CacheDetailData.class, new CacheDetailData());
        f_mapDataRetrievers.put(CacheFrontDetailData.class, new CacheFrontDetailData());
        f_mapDataRetrievers.put(TopicData.class, new TopicData());
        f_mapDataRetrievers.put(PersistenceData.class, new PersistenceData());
        f_mapDataRetrievers.put(PersistenceNotificationsData.class, new PersistenceNotificationsData());
        f_mapDataRetrievers.put(CacheStorageManagerData.class, new CacheStorageManagerData());
        f_mapDataRetrievers.put(HttpSessionData.class, new HttpSessionData());
        f_mapDataRetrievers.put(FederationDestinationData.class, new FederationDestinationData());
        f_mapDataRetrievers.put(FederationDestinationDetailsData.class, new FederationDestinationDetailsData());
        f_mapDataRetrievers.put(FederationOriginData.class, new FederationOriginData());
        f_mapDataRetrievers.put(FederationOriginDetailsData.class, new FederationOriginDetailsData());
        f_mapDataRetrievers.put(RamJournalData.class, new RamJournalData());
        f_mapDataRetrievers.put(FlashJournalData.class, new FlashJournalData());
        f_mapDataRetrievers.put(JCacheConfigurationData.class, new JCacheConfigurationData());
        f_mapDataRetrievers.put(JCacheStatisticsData.class, new JCacheStatisticsData());
        f_mapDataRetrievers.put(HttpProxyData.class, new HttpProxyData());
        f_mapDataRetrievers.put(HttpProxyMemberData.class, new HttpProxyMemberData());
        f_mapDataRetrievers.put(HotCacheData.class,new HotCacheData());
        f_mapDataRetrievers.put(HotCachePerCacheData.class, new HotCachePerCacheData());
        f_mapDataRetrievers.put(NodeStorageData.class, new NodeStorageData());
        f_mapDataRetrievers.put(ExecutorData.class, new ExecutorData());
        f_mapDataRetrievers.put(GrpcProxyData.class, new GrpcProxyData());

        // Loop through each data retriever and initialize the map of
        // report XML. Doing it this way we load it only once

        Iterator<Map.Entry<Class, DataRetriever>> iter = f_mapDataRetrievers.entrySet().iterator();
        while (iter.hasNext())
            {
            Map.Entry<Class, DataRetriever> entry = iter.next();
            String sReport = entry.getValue().getReporterReport();
            if (sReport != null)
                {
                String sReportXML = getReportXML(sReport);
                if (sReportXML != null)
                    {
                    f_mapReportXML.put(entry.getKey(), sReportXML);
                    }
                }
            }
        }

    /**
     * Returns the current refresh time in millis.
     * 
     * @return the current refresh time in millis
     */
    private long getRefreshTime()
        {
        return GlobalPreferences.sharedInstance().getRefreshTime() * 1000L;
        }

    /**
     * Indicates if we should log query times.
     *
     * @return if we should log query times
     */
    private boolean isLogQueryTimes()
        {
        return GlobalPreferences.sharedInstance().isLogQueryTimes();
        }

    /**
     * Refresh the statistics from the given {@link MBeanServerConnection}
     * connection. This method will only refresh data if at least the REFRESH_TIME
     * has passed since last refresh.
     *
     * @param requestSender  the RequestSender to use
     */
    public void refreshStatistics(RequestSender requestSender)
        {
        if (System.currentTimeMillis() - m_ldtLastUpdate >= m_nRefreshTime)
            {
            long ldtStart = System.currentTimeMillis();
            // refresh every iteration so we can enable and disable on the fly
            m_fLogJMXQueryTimes = isLogQueryTimes();

            // its important that the CACHE data is refreshed first and
            // as such we are relying on the order of types in the enum.
            for (DataType type : DataType.values())
                {
                // optimize the retrieval if this is not the first time and only query
                // specific data types if the functionality is enabled.
                // this can improve performance especially over REST
                if (m_fIsFirstRefresh || shouldRetrieveData(type))
                    {
                    if (m_fLogJMXQueryTimes)
                        {
                        LOGGER.info("Starting querying statistics for " + type.toString());
                        }

                    long ldtCollectionStart = System.currentTimeMillis();
                    m_mapCollectedData.put(type, getData(requestSender, type.getClassName()));
                    long ldtCollectionTime  = System.currentTimeMillis() - ldtCollectionStart;

                    if (m_fLogJMXQueryTimes)
                        {
                        LOGGER.info("Time to query statistics for " + type.toString() + " was " +
                                    ldtCollectionTime + " ms");
                        }
                    }
                else
                    {
                    if (m_fLogJMXQueryTimes)
                        {
                        LOGGER.info("Skipping querying statistics for " + type.toString() + " as it is not configured");
                        }
                    }
                }

            long ldtTotalDuration = System.currentTimeMillis() - ldtStart;

            if (m_fLogJMXQueryTimes)
               {
               LOGGER.info("Time to query all statistics was " + ldtTotalDuration + " ms");
               }

            m_nRefreshTime  = getRefreshTime();
            m_ldtLastUpdate = System.currentTimeMillis();
            }
        }


    /**
     * Returns true if the {@link DataType} should be refreshed. E.g. If after the
     * first refresh, Federation is not enabled then don't refresh data on subsequent calls.
     *
     * @param type the {@link DataType} to check
     *
     * @return true if the {@link DataType} should be refreshed
     */
    private boolean shouldRetrieveData(DataType type)
        {
        Class<?> clazz = type.getClassName();

        if (!isHotcacheConfigured() &&
            (
            clazz.equals(DataType.HOTCACHE.getClassName()) ||
            clazz.equals(DataType.HOTCACHE_PERCACHE.getClassName())
            ))
            {
            return false;
            }

        if (!isFederationCongfigured() &&
             (
             clazz.equals(DataType.FEDERATION_DESTINATION.getClassName()) ||
             clazz.equals(DataType.FEDERATION_DESTINATION_DETAILS.getClassName()) ||
             clazz.equals(DataType.FEDERATION_ORIGIN.getClassName()) ||
             clazz.equals(DataType.FEDERATION_ORIGIN_DETAILS.getClassName())
             ))
            {
            return false;
            }

        if (!isCoherenceExtendConfigured() && clazz.equals(DataType.PROXY.getClassName()))
            {
            return false;
            }

        if (!isPersistenceConfigured() &&
            (
            clazz.equals(DataType.PERSISTENCE.getClassName()) ||
            clazz.equals(DataType.PERSISTENCE_NOTIFICATIONS.getClassName())
            ))
            {
            return false;
            }

        if (!isCoherenceWebConfigured() && clazz.equals(DataType.HTTP_SESSION.getClassName()))
            {
            return false;
            }

        if (!isElasticDataConfigured() &&
            (
            clazz.equals(DataType.RAMJOURNAL.getClassName()) ||
            clazz.equals(DataType.FLASHJOURNAL.getClassName())
            ))
            {
            return false;
            }

        if (!isHotcacheConfigured() &&
            (
            clazz.equals(DataType.HOTCACHE.getClassName()) ||
            clazz.equals(DataType.HOTCACHE_PERCACHE.getClassName())
            ))
            {
            return false;
            }

        if (!isJCacheConfigured() &&
            (
            clazz.equals(DataType.JCACHE_CONFIG.getClassName()) ||
            clazz.equals(DataType.JCACHE_STATS.getClassName())
            ))
            {
            return false;
            }

        if (!isHttpProxyConfigured() &&
            (
            clazz.equals(DataType.HTTP_PROXY.getClassName()) ||
            clazz.equals(DataType.HTTP_PROXY_DETAIL.getClassName())
            ))
            {
            return false;
            }

        return true;
        }

    /**
     * This is a wrapper method which will call the underlying implementation
     * to get statistics. If statistics directly from the reporter are available
     * then run the particular report, otherwise do a JMX query.
     *
     * @param requestSender  the {@link RequestSender} to use to query the report
     * @param clazz          the implementation of {@link DataRetriever} to get data for
     *
     * @return the {@link List} of data obtainer by either method
     */
    public List<Entry<Object, Data>> getData(RequestSender requestSender, Class clazz)
        {
        boolean fFallBack = false;

        // Re Bug 22132359 - When we are connecting to pre 12.2.1.1.0 cluster from 12.2.1.1.0 or above and we
        // are collecting ProxyStats, we need to force to use JMX rather than report
        if (isReporterAvailable() != null && isReporterAvailable() &&
            !(clazz.equals(ProxyData.class) && getClusterVersionAsInt() < 122110))
            {
            // retrieve the report XML for this class
            String sReportXML = f_mapReportXML.get(clazz);

            if (sReportXML == null)
                {
                // this means there is no report for this class
                fFallBack = true;
                }
            if (!fFallBack)
                {
                try
                    {
                    DataRetriever           retriever        = getDataRetrieverInstance(clazz);
                    SortedMap<Object, Data> mapCollectedData = null;
                    if (requestSender instanceof HttpRequestSender)
                        {
                        mapCollectedData = retriever.
                                getAggregatedDataFromHttpQuerying(this, ((HttpRequestSender) requestSender));
                        }
                    else
                        {
                        mapCollectedData = retriever.getAggregatedDataUsingReport(this, requestSender, sReportXML);
                        }

                    if (mapCollectedData != null)
                        {
                        return new ArrayList<Map.Entry<Object, Data>>(mapCollectedData.entrySet());
                        }
                    else
                        {
                        return null;
                        }
                    }
                catch (Exception e)
                    {
                    // we received an error running the report, so mark as
                    // a fall back so it will be immediately run
                    LOGGER.warning(Localization.getLocalText("ERR_Failed_to_run_report", clazz.toString(), e.toString()));
                    e.printStackTrace(); 
                    fFallBack = true;
                    }
                }
            }

        // this code path is for the following scenarios:
        // 1. If we need to fall-back as the reporter is being used but no report yet available
        // 2. The reporter is not available
        // 3. We have not yet decided is the reporter is available
        // 4. Bug 22132359 - We are connecting to pre 12.2.1.1.0 cluster from 12.2.1.1.0 or above and we
        //                   are collecting ProxyStats,  we need to force to use JMX rather than report
        if (fFallBack || isReporterAvailable() == null || !isReporterAvailable() ||
            (clazz.equals(ProxyData.class) && getClusterVersionAsInt() < 122110))
            {
            try
                {
                // get data the old fashioned way via JMX queries
                // ClusterData is a a special case as its used to determine if
                // we may be able to use the reporter
                if (clazz.equals(ClusterData.class))
                    {
                    List<Entry<Object, Data>> clusterData = getDataRetrieverInstance(clazz).getJMXData(requestSender, this);

                    // if we have not yet evaluated if the reporter is available, e.g. value of null,
                    // then do it. Also check for the version as well.
                    if (isReporterAvailable() == null || is1213AndAbove() == null)
                        {
                        // get the Coherence version. Easier to do if we are connected to a cluster,
                        // but we are have JMX connection as we have to look in data we collected.

                        if (clusterData != null)
                            {
                            for (Entry<Object, Data> entry : clusterData)
                                {
                                // there will only be one cluster entry

                                String sCoherenceVersion =
                                    entry.getValue().getColumn(ClusterData.VERSION).toString().replaceFirst(" .*$", "")
                                                    .replaceFirst("[\\.-]SNAPSHOT.*$","").replaceAll("-",".");
                                m_sClusterVersion = sCoherenceVersion;

                                int nVersion = 0;

                                if (sCoherenceVersion.startsWith("3.5"))
                                    {
                                    // manual check as version numbering changed after 35
                                    nVersion = 353;
                                    }
                                else if (sCoherenceVersion.startsWith("2"))
                                    {
                                    // check for versions such as 20.06 or 20.06.01 and convert them to an ever increasing number
                                    // 20.06    -> 2006000
                                    // 20.06.1  -> 2006100
                                    // 20.06.10 -> 2006100
                                    String sStrippedVersion = sCoherenceVersion.replaceAll("\\.", "");
                                    nVersion = Integer.parseInt(sStrippedVersion) * (int) Math.pow(10, 7 - sStrippedVersion.length());
                                    }
                                else
                                    {
                                    nVersion = Integer.parseInt(sCoherenceVersion.replaceAll("\\.", ""));
                                    }

                                if (nVersion >= 121300)
                                    {
                                    // only set if the reporter available is it is not already set as we may have
                                    // got to this code path because is1213AndAbove() is still null
                                    setReporterAvailable(isReporterAvailable() == null ? true : isReporterAvailable());
                                    m_fis1213AndAbove = true;
                                    }
                                else
                                    {
                                    setReporterAvailable(isReporterAvailable() == null ? false : isReporterAvailable());
                                    m_fis1213AndAbove = false;
                                    }
                                m_nClusterVersion = nVersion;
                                }
                            }
                        }

                    return clusterData;
                    }
                else
                    {
                    return getDataRetrieverInstance(clazz).getJMXData(requestSender, this);
                    }
                }
            catch (Exception e)
                {
                LOGGER.log(Level.WARNING, "Unable to get data", e);
                }
            }

        return null;
        }

    /**
     * Retrieve the XML for the report by loading it from the resource.
     *
     * @param sReport  the report to load from - this will be available as part
     *                 of the JVisualVM plugin
     *
     * @return  a String containing the report XML
     */
    private String getReportXML(String sReport) {
        StringBuffer sb = new StringBuffer();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(sReport);

        if (in == null)
            {
            throw new RuntimeException("Unable to load report " + sReport);
            }

        InputStreamReader is = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(is);
        try
            {
            String sLine;

            while ((sLine  = br.readLine()) != null)
                {
                sb.append(sLine);

                }
            }
        catch (IOException ioe)
            {
            throw  new RuntimeException("Unable to read from report " + sReport + " : " +
                                        ioe.getMessage());
            }
        finally
            {
            closeAndIgnore(br);
            closeAndIgnore(is);
            closeAndIgnore(is);
            }

        return sb.toString();
    }

    /**
     * Close a {@link Closeable} or {@link Reader} object.
     *
     * @param obj  the {@link Closeable} or {@link Reader} object to close
     */
    private void closeAndIgnore(Object obj)
        {
        try
            {
            if (obj instanceof Closeable)
                {
                ((Closeable)obj).close();
                }
            else if (obj instanceof Reader)
                {
                ((Reader)obj).close();
                }
            }
        catch (Exception e)
            {
            // ignore
            }
        }

    /**
     * Returns a unique list of addresses for the member data as we only want to
     * get information for each machine. The node Id is stored as the value.
     *
     * @return the {@link SortedMap} of machines
     */
    public SortedMap<String, Integer> getInitialMachineMap()
        {
        SortedMap<String, Integer> initialMachineMap = new TreeMap<>();

        // get a unique list of addresses for the member data as we only want to
        // get information for each machine.
        if (m_mapCollectedData.get(DataType.MEMBER) != null)
            {
            for (Entry<Object, Data> entry : m_mapCollectedData.get(DataType.MEMBER))
                {
                initialMachineMap.putIfAbsent(((String) entry.getValue().getColumn(MemberData.ADDRESS)),
                        (Integer) entry.getValue().getColumn(MemberData.NODE_ID));
                }
            }

        return initialMachineMap;
        }

    /**
     * Erase the current service member data as we have changed the
     * selected service.
     */
    public void eraseServiceMemberData()
        {
        m_mapCollectedData.put(DataType.SERVICE_DETAIL, null);
        }

    /**
     * Erase the current hotcache member data as we have changed the selected member.
     */
    public void eraseHotCachePerCacheData()
        {
        m_mapCollectedData.put(DataType.HOTCACHE_PERCACHE, null);
        }

    /**
     * Erase the current destination details data and origin details data
     * as we have changed the service / participant pair in federation tab.
     */
    public void eraseFederationDetailsData()
        {
        m_mapCollectedData.put(DataType.FEDERATION_DESTINATION_DETAILS, null);
        m_mapCollectedData.put(DataType.FEDERATION_ORIGIN_DETAILS, null);
        }

    // ----- accessors ------------------------------------------------------

    /**
     * Returns the currently selected service.
     *
     * @return the currently selected service
     */
    public String getSelectedService()
        {
        return m_sSelectedService;
        }

    /**
     * Sets the currently selected service.
     *
     * @param sService the currently selected service
     */
    public void setSelectedService(String sService)
        {
        m_sSelectedService = sService;
        m_mapCollectedData.remove(DataType.SERVICE_DETAIL);
        }

    /**
     * Returns the currently selected hotcache PercacheOperation.
     *
     * @return the currently selected hotcache PercacheOperation
     */
    public String getSelectedHotCachePerCacheOperation()
        {
        return m_sSelectedPerCacheOperation;
        }

    /**
     * Sets the currently selected hotcache PercacheOperation.
     *
     * @param sSelectedPerCacheOperation the currently selected hotcache PercacheOperation
     */
    public void setSelectedHotCachePerCacheOperation(String sSelectedPerCacheOperation)
        {
        this.m_sSelectedPerCacheOperation = sSelectedPerCacheOperation;
        }

    /**
     * Returns the currently selected hotcache member.
     *
     * @return the currently selected hotcache member
     */
    public String getSelectedHotCacheMember()
        {
        return m_sSelectedMember;
        }

    /**
     * Sets the currently selected hotcache member.
     *
     * @param sSelectedMember the currently selected hotcache member
     */
    public void setSelectedHotCacheMember(String sSelectedMember)
        {
        this.m_sSelectedMember = sSelectedMember;
        m_mapCollectedData.remove(DataType.HOTCACHE_PERCACHE);
        }


    /**
     * Returns the currently selected service in http proxy tab.
     *
     * @return  the currently selected service in http proxy tab
     */
    public String getSelectedHttpProxyService()
        {
        return m_sSelectedHttpProxyService;
        }

    /**
     * Sets the current selected service in http proxy tab.
     *
     * @param sService the currently selected service
     */
    public void setSelectedHttpProxyService(String sService)
        {
        m_sSelectedHttpProxyService = sService;
        m_mapCollectedData.remove(DataType.HTTP_PROXY_DETAIL);
        }

    /**
     * Returns the currently selected service in federation tab.
     *
     * @return the currently selected service
     */
    public String getSelectedServiceInFed()
        {
        return m_sSelectedServiceInFed;
        }

    /**
     * Sets the currently selected service in federation tab.
     *
     * @param sService the currently selected service
     */
    public void setSelectedServiceInFed(String sService)
        {
        m_sSelectedServiceInFed = sService;
        }

    /**
     * Returns the selected node Id in outbound details in federation tab.
     *
     * @return the currently selected node Id in outbound.
     */
    public String getSelectedNodeOutbound()
        {
        return m_sOutboundNodeId;
        }

    /**
     * Sets the selected node Id in outbound details in federation tab.
     *
     * @param sNodeId  the node Id
     */
    public void setSelectedNodeOutbound(String sNodeId)
        {
        m_sOutboundNodeId = sNodeId;
        }

    /**
     * Returns the selected node Id in inbound details in federation tab.
     *
     * @return the currently selected node Id in inbound.
     */
    public String getSelectedNodeInbound()
        {
        return m_sInboundNodeId;
        }

    /**
     * Sets the selected node Id in inbound details in federation tab.
     *
     * @param sNodeId  the node Id
     */
    public void setSelectedNodeInbound(String sNodeId)
        {
        m_sInboundNodeId = sNodeId;
        }

    /**
     * Returns if the reporter is available for use.
     *
     * @return null if not yet evaluated or true/false if evaluated
     */
    public Boolean isReporterAvailable()
        {
        return m_fReporterAvailable;
        }

    /**
     * Return if we are running Coherence 12.1.3 or above.
     *
     * @return if we are running Coherence 12.1.3 or above
     */
    public Boolean is1213AndAbove()
        {
        return m_fis1213AndAbove;
        }

    /**
     * Sets if the reporter is available.
     *
     * @param value  if the reporter is available
     */
    public void setReporterAvailable(Boolean value)
        {
        m_fReporterAvailable = value;
        GlobalPreferences.sharedInstance().setReporterDisabled(value);
        }

    /**
     * Sets if we want to include the NameService in the list of
     * proxy servers.
     *
     * @param fInclude if we want to include the NameService
     *                 in the list of proxy servers
     */
    public void setIncludeNameService(boolean fInclude)
        {
        m_fIncludeNameService = fInclude;
        }

    /**
     * Returns if we want to include the NameService in the list of
     * proxy servers.
     *
     * @return if we want to include the NameService
     */
    public boolean isIncludeNameService()
        {
        return m_fIncludeNameService;
        }

    /**
     * Sets the currently selected cache.
     *
     * @param selectedCache  the currently selected cache (service/cache name {@link Tuple}
     */
    public void setSelectedCache(Pair<String, String> selectedCache)
        {
        this.m_selectedCache = selectedCache;
        m_mapCollectedData.remove(DataType.CACHE_DETAIL);
        m_mapCollectedData.remove(DataType.CACHE_FRONT_DETAIL);
        m_mapCollectedData.remove(DataType.CACHE_STORAGE_MANAGER);
        }

    /**
     * Sets the value for is first refresh.
     *
     * @param fIsFirstRefresh the value for is first refresh
     */
    public void setIsFirstRefresh(boolean fIsFirstRefresh)
        {
        this.m_fIsFirstRefresh = fIsFirstRefresh;
        }

    /**
     * Returns the currently selected cache.
     *
     * @return the currently selected cache
     */
    public Pair<String, String> getSelectedCache()
        {
        return this.m_selectedCache;
        }

    /**
     * Sets the current selected JCache.
     *
     * @param selectedJCache the elected JCache
     */
    public void setSelectedJCache(Pair<String, String> selectedJCache)
        {
        this.m_selectedJCache = selectedJCache;
        }

    /**
     * Returns the currently selected JCache cache.
     *
     * @return the currently selected JCache cache
     */
    public Pair<String, String> getSelectedJCache()
        {
        return this.m_selectedJCache;
        }

    /**
     * Sets the currently selected service name and participant name in federation tab.
     *
     * @param selectedServiceParticipant  the currently selected service name and participant name {@link Tuple}
     */
    public void setSelectedServiceParticipant(Pair<String, String> selectedServiceParticipant)
        {
        this.m_selectedServiceParticipant = selectedServiceParticipant;
        }

    /**
     * Returns the currently selected service name and participant name in federation tab.
     *
     * @return the currently selected service name and participant name
     */
    public Pair<String, String> getSelectedServiceParticipant()
        {
        return this.m_selectedServiceParticipant;
        }

    /**
     * Sets the flag to indicate whether the federation service is available.
     *
     * @param isAvailable  true if the federation is available
     */
    public void setFederationAvailable(boolean isAvailable)
        {
        m_fIsFederationAvailable = isAvailable;
        }

    /**
     * Returns if proxy servers are configured.
     *
     * @return true if proxy servers are configured.
     */
    public boolean isCoherenceExtendConfigured()
        {
        // if we have never set this flag, do it once only so that
        // the tab will always display and be updated
        if (m_fIsCoherenceExtendConfigured == null)
            {
            m_fIsCoherenceExtendConfigured = m_mapCollectedData.get(DataType.PROXY) != null
                                             && m_mapCollectedData.get(DataType.PROXY).size() != 0;
            }

        return m_fIsCoherenceExtendConfigured;
        }

    /**
     * Returns if Coherence*Web is configured.
     *
     * @return true if Coherence*Web is configured.
     */
    public boolean isCoherenceWebConfigured()
        {
        return m_mapCollectedData.get(DataType.HTTP_SESSION) != null
               && m_mapCollectedData.get(DataType.HTTP_SESSION).size() != 0;
        }

    /**
     * Returns if Hotcache is configured.
     *
     * @return true if Hotcache is configured.
     */
    public boolean isHotcacheConfigured()
        {
        return m_mapCollectedData.get(DataType.HOTCACHE) != null
               && m_mapCollectedData.get(DataType.HOTCACHE).size() != 0;
        }

    /**
     * Returns if Persistence is configured.
     *
     * @return true if Persistence is configured.
     */
    public boolean isPersistenceConfigured()
        {
        return m_mapCollectedData.get(DataType.PERSISTENCE) != null
               && m_mapCollectedData.get(DataType.PERSISTENCE).size() != 0;
        }

    /**
     * Returns if Topics is configured.
     *
     * @return true if Topics is configured.
     */
    public boolean isTopicsConfigured()
        {
        return m_mapCollectedData.get(DataType.TOPICS_DETAIL) != null
               && m_mapCollectedData.get(DataType.TOPICS_DETAIL).size() != 0;
       }

    /**
     * Return if Federation is configured.
     *
     * @return true if Federation is configured.
     */
    public boolean isFederationCongfigured()
        {
        return m_fIsFederationAvailable;
        }

    /**
     * Returns if Elastic Data is configured.
     *
     * @return true if Elastic Data is configured.
     */
    public boolean isElasticDataConfigured()
        {
        return (m_mapCollectedData.get(DataType.RAMJOURNAL) != null
                && m_mapCollectedData.get(DataType.RAMJOURNAL).size() != 0) ||
               (m_mapCollectedData.get(DataType.FLASHJOURNAL) != null
                && m_mapCollectedData.get(DataType.FLASHJOURNAL).size() != 0);
        }

    /**
     * Returns if Executor is configured.
     *
     * @return true if Executor is configured.
     */
    public boolean isExecutorConfigured()
        {
        return (m_mapCollectedData.get(DataType.EXECUTOR) != null
                && m_mapCollectedData.get(DataType.EXECUTOR).size() != 0);
        }

    /**
     * Returns if GrpcProxy is configured.
     *
     * @return true if GrpcProxy is configured.
     */
    public boolean isGrpcProxyConfigured()
        {
        return (m_mapCollectedData.get(DataType.GRPC_PROXY) != null
                && m_mapCollectedData.get(DataType.GRPC_PROXY).size() != 0);
        }

    /**
     * Returns if JCache is configured.
     *
     * @return true if JCache is configured.
     */
    public boolean isJCacheConfigured()
        {
        return (m_mapCollectedData.get(DataType.JCACHE_CONFIG) != null
                && m_mapCollectedData.get(DataType.JCACHE_CONFIG).size() != 0) ||
               (m_mapCollectedData.get(DataType.JCACHE_STATS) != null
                && m_mapCollectedData.get(DataType.JCACHE_STATS).size() != 0);
        }

    /**
     * Return if http proxy servers are configured.
     *
     * @return true if http proxy servers are configured
     */
    public boolean isHttpProxyConfigured()
        {
        return (m_mapCollectedData.get(DataType.HTTP_PROXY) != null
                && m_mapCollectedData.get(DataType.HTTP_PROXY).size() != 0);
        }

    /**
     * Returns the data for a given {@link DataType} enum.
     *
     * @param dataType the type of data to return
     *
     * @return the data for a given {@link DataType} enum.
     */
    public List<Entry<Object, Data>> getData(DataType dataType)
        {
        return m_mapCollectedData.get(dataType);
        }

    /**
     * Returns if load average is available.
     *
     * @return true if load average is available
     */
    public boolean isLoadAverageAvailable()
        {
        return m_fIsLoadAverageAvailable;
        }

    /**
     * Set an indicator to show if load average should be used.
     *
     * @param fLoadAverageAvailable indicates if load average is available
     */
    public void setLoadAverageAvailable(boolean fLoadAverageAvailable)
        {
        m_fIsLoadAverageAvailable = fLoadAverageAvailable;
        }

    /**
     * Returns an instance of the data retriever class for executing JMX calls on.
     *
     * @param clazz the {@link Class} to get the instance for
     *
     * @return an instance of the data retriever class for executing JMX calls on
     */
    public DataRetriever getDataRetrieverInstance(Class clazz)
        {
        DataRetriever retriever = f_mapDataRetrievers.get(clazz);

        if (retriever == null)
            {
            throw new IllegalArgumentException(Localization.getLocalText("ERR_instance",
                new String[] {clazz.getCanonicalName()}));
            }

        return retriever;
        }

    /**
      * Returns the cluster version as a String.
     *
     * @return the cluster version as a String
      */
    public String getClusterVersion()
        {
        return m_sClusterVersion;
        }

    /**
     * Returns the cluster version as an integer for comparison reasons.
     *
     * @return the cluster version as an integer
     */
    public int getClusterVersionAsInt()
        {
        return m_nClusterVersion;
        }

    /**
     * Set the known distributed caches.
     *
     * @param  setCaches the {@link Set} of distributed caches.
     */
    public void setDistributedCaches(Set<String> setCaches)
        {
        this.m_setKnownDistributedCaches = setCaches;
        }

    /**
     * Returns the {@link Set} of distributed caches.
     *
     * @return the {@link Set} of distributed caches
     */
    public Set<String> getDistributedCaches()
        {
        return m_setKnownDistributedCaches;
        }

    /**
     * Returns the {@link Set} of domain partitions that have been discovered.
     *
     * @return the {@link Set} of domain partitions that have been discovered
     */
    public Set<String> getDomainPartitions()
        {
        return f_setDomainPartitions;
        }

    /**
     * Return the last time the statistics were updated.
     *
     * @return the last time the statistics were updated
     */
    public long getLastUpdate()
        {
        return m_ldtLastUpdate;
        }

    /**
     * Indicates if REST cache optimization is available.
     *
     * @return if REST cache optimization is available of null if it has not be
     * determined
     */
    public Boolean isRestCacheOptimizationAvailable()
        {
        return m_fIRestCacheOptimizationAvailable;
        }

    /**
     * Sets if REST cache optimization is available.
     *
     * @param fValue if REST cache optimization is available
     */
    public void setRestCacheOptimizationAvailable(boolean fValue)
        {
        m_fIRestCacheOptimizationAvailable = fValue;
        }

    // ----- constants ------------------------------------------------------

    /**
     * Returns an instance of the VisualVMModel.
     *
     * @return an instance of the initialized VisualVMModel
     */
    public static VisualVMModel getInstance()
        {
        VisualVMModel model = new VisualVMModel();

        model.init();

        return model;
        }

    /**
     * Returns the report XML for a given class.
     *
     * @return the report XML for a given class
     */
    public Map<Class, String> getReportXMLMap()
        {
        return this.f_mapReportXML;
        }

    /**
     * Defines the type of data we can collect.
     * Note: The order of these is important. Please do not change. e.g. cluster
     * need to go first so we can determine the version. Also service needs
     * to go before cache so we could setup the list of distributed caches.
     */
    public enum DataType
        {
        CLUSTER(ClusterData.class, CLUSTER_LABELS),
        SERVICE(ServiceData.class, SERVICE_LABELS),
        SERVICE_DETAIL(ServiceMemberData.class, SERVICE_DETAIL_LABELS),
        CACHE(CacheData.class, CACHE_LABELS),
        CACHE_DETAIL(CacheDetailData.class, CACHE_DETAIL_LABELS),
        CACHE_FRONT_DETAIL(CacheFrontDetailData.class, CACHE_FRONT_DETAIL_LABELS),
        CACHE_STORAGE_MANAGER(CacheStorageManagerData.class, CACHE_STORAGE_MANAGER_LABELS),
        TOPICS_DETAIL(TopicData.class, TOPICS_LABELS),
        MEMBER(MemberData.class, MEMBER_LABELS),
        NODE_STORAGE(NodeStorageData.class, new String[] {}),
        MACHINE(MachineData.class, MACHINE_LABELS),
        PROXY(ProxyData.class, PROXY_LABELS),
        PERSISTENCE(PersistenceData.class, PERSISTENCE_LABELS),
        PERSISTENCE_NOTIFICATIONS(PersistenceNotificationsData.class, PERSISTENCE_NOTIFICATIONS_LABELS),
        HTTP_SESSION(HttpSessionData.class, HTTP_SESSION_LABELS),
        FEDERATION_DESTINATION(FederationDestinationData.class, FEDERATION_OVERALL_LABELS),
        FEDERATION_ORIGIN(FederationOriginData.class, null),
        FEDERATION_DESTINATION_DETAILS(FederationDestinationDetailsData.class, FEDERATION_DESTINATION_DETAILS_LABELS),
        FEDERATION_ORIGIN_DETAILS(FederationOriginDetailsData.class, FEDERATION_ORIGIN_DETAILS_LABELS),
        RAMJOURNAL(RamJournalData.class, ELASTIC_DATA_LABELS),
        FLASHJOURNAL(FlashJournalData.class, ELASTIC_DATA_LABELS),
        JCACHE_CONFIG(JCacheConfigurationData.class, JCACHE_CONFIG_LABELS),
        JCACHE_STATS(JCacheStatisticsData.class, JCACHE_STATS_LABELS),
        HTTP_PROXY(HttpProxyData.class, HTTP_PROXY_LABELS),
        HTTP_PROXY_DETAIL(HttpProxyMemberData.class, HTTP_PROXY_DETAIL_LABELS),
        HOTCACHE(HotCacheData.class, HOTCACHE_LABELS),
        HOTCACHE_PERCACHE(HotCachePerCacheData.class, HOTCACHE_PERCACHE_LABELS),
        EXECUTOR(ExecutorData.class, EXECUTOR_LABELS),
        GRPC_PROXY(GrpcProxyData.class, GRPC_PROXY_LABELS);

        private DataType(Class clz, String[] asMeta)
            {
            clazz      = clz;
            asMetadata = asMeta;
            }

        /**
         * Returns the class for this enum.
         *
         * @return the class for this enum
         */
        public Class getClassName()
            {
            return clazz;
            }

        /**
         * Returns the column metadata for this enum.
         *
         * @return the column metadata for this enum
         */
        public String[] getMetadata()
            {
            return asMetadata;
            }

        /**
         * The {@link Class} associated with this enum.
         */
        private Class clazz;

        /**
         * The column name associated with this enum.
         */
        private String[] asMetadata;
        }

    /**
     * Labels for cluster table. Note: No localization is done for these labels
     * as currently they are not displayed.
     */
    private static final String[] CLUSTER_LABELS = new String[] {"Cluster Name", "License Mode", "Version",
        "Departure Count", "Cluster Size"};

    /**
     * Labels for service table.
     */
    private static final String[] SERVICE_LABELS = new String[]
        {
        Localization.getLocalText("LBL_service_name"), Localization.getLocalText("LBL_status_ha"),
        Localization.getLocalText("LBL_members"), Localization.getLocalText("LBL_storage_enabled"),
        Localization.getLocalText("LBL_partitions"), Localization.getLocalText("LBL_endangered"),
        Localization.getLocalText("LBL_vulnerable"), Localization.getLocalText("LBL_unbalanced"),
        Localization.getLocalText("LBL_pending")
        };

    /**
     * Labels for service detail table.
     */
    private static final String[] SERVICE_DETAIL_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_threads"),
        Localization.getLocalText("LBL_idle_threads"), Localization.getLocalText("LBL_thread_util"),
        Localization.getLocalText("LBL_task_average"), Localization.getLocalText("LBL_task_backlog"),
        Localization.getLocalText("LBL_request_average")
        };

    /**
     * Labels for cache table.
     */
    private static final String[] TOPICS_LABELS = new String[] {Localization.getLocalText("LBL_topic_name"),
        Localization.getLocalText("LBL_topic_size"), Localization.getLocalText("LBL_memory_bytes"),
        Localization.getLocalText("LBL_memory_mb"), Localization.getLocalText("LBL_average_object_size"),
        Localization.getLocalText("LBL_publisher_sends"), Localization.getLocalText("LBL_subscriber_receives")
    };

    /**
     * Labels for topics table.
     */
    private static final String[] CACHE_LABELS = new String[] {Localization.getLocalText("LBL_service_cache_name"),
        Localization.getLocalText("LBL_size"), Localization.getLocalText("LBL_memory_bytes"),
        Localization.getLocalText("LBL_memory_mb"), Localization.getLocalText("LBL_average_object_size"),
        Localization.getLocalText("LBL_unit_calculator")
    };

    /**
     * Labels for cache detail table.
     */
    private static final String[] CACHE_DETAIL_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_size"),
        Localization.getLocalText("LBL_memory_bytes"), Localization.getLocalText("LBL_total_gets"),
        Localization.getLocalText("LBL_total_puts"), Localization.getLocalText("LBL_cache_hits"),
        Localization.getLocalText("LBL_cache_misses"), Localization.getLocalText("LBL_hit_probability")
        };

    /**
     * Labels for front cache detail table.
     */
    private static final String[] CACHE_FRONT_DETAIL_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_size"),
        Localization.getLocalText("LBL_total_gets"), Localization.getLocalText("LBL_total_puts"),
        Localization.getLocalText("LBL_cache_hits"), Localization.getLocalText("LBL_cache_misses"),
        Localization.getLocalText("LBL_hit_probability")
        };

    /**
     * Labels for storage manager table.
     */
    private static final String[] CACHE_STORAGE_MANAGER_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_locks_granted"),
        Localization.getLocalText("LBL_locks_pending"), Localization.getLocalText("LBL_listener_reg"),
        Localization.getLocalText("LBL_max_query_millis"), Localization.getLocalText("LBL_max_query_desc"),
        Localization.getLocalText("LBL_non_opt_avge"), Localization.getLocalText("LBL_opt_avge"),
        Localization.getLocalText("LBL_index_units"), Localization.getLocalText("LBL_indexing_total_millis")
        };

    /**
     * Labels for member table.
     */
    private static final String[] MEMBER_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_unicast_address"),
        Localization.getLocalText("LBL_port"), Localization.getLocalText("LBL_role"),
        Localization.getLocalText("LBL_publisher_rate"), Localization.getLocalText("LBL_receiver_rate"),
        Localization.getLocalText("LBL_send_q"), Localization.getLocalText("LBL_max_memory"),
        Localization.getLocalText("LBL_used_memory"), Localization.getLocalText("LBL_free_memory"),
        Localization.getLocalText("LBL_storage_enabled")
        };

    /**
     * Labels for machine table.
     */
    private static final String[] MACHINE_LABELS = new String[]
        {
        Localization.getLocalText("LBL_machine_name"), Localization.getLocalText("LBL_core_count"),
        Localization.getLocalText("LBL_load_average"), Localization.getLocalText("LBL_total_physical_mem"),
        Localization.getLocalText("LBL_free_physical_mem"), Localization.getLocalText("LBL_percent_free_mem")
        };

    /**
     * Labels for proxy table.
     */
    private static final String[] PROXY_LABELS = new String[]
        {
        Localization.getLocalText("LBL_ip_port"), Localization.getLocalText("LBL_service_name"),
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_connection_count"),
        Localization.getLocalText("LBL_outgoing_msg_backlog"), Localization.getLocalText("LBL_total_bytes_rcv"),
        Localization.getLocalText("LBL_total_bytes_sent"), Localization.getLocalText("LBL_total_msg_rcv"),
        Localization.getLocalText("LBL_total_msg_sent")
        };

    /**
     * Labels for hotcache table.
     */
    private static final String[] HOTCACHE_LABELS = new String[]
        {
        Localization.getLocalText("LBL_hotcacheMember"),
        Localization.getLocalText("LBL_numberOfOperationsProcessed"), Localization.getLocalText("LBL_startTime"),
        Localization.getLocalText("LBL_trailFileName"), Localization.getLocalText("LBL_trailFilePos")
        };

    /**
     * Labels for hotcache_percache table.
     */
    private static final String[] HOTCACHE_PERCACHE_LABELS = new String[]
        {
        Localization.getLocalText("LBL_CacheOperation"), Localization.getLocalText("LBL_Count"),
        Localization.getLocalText("LBL_max"), Localization.getLocalText("LBL_mean"),
        Localization.getLocalText("LBL_min")
        };

    /**
     * Labels for executor table.
     */
    private static final String[] EXECUTOR_LABELS = new String[]
        {
        Localization.getLocalText("LBL_executor"),         Localization.getLocalText("LBL_executor_count"),
        Localization.getLocalText("LBL_tasks_in_progress"), Localization.getLocalText("LBL_tasks_completed"),
        Localization.getLocalText("LBL_tasks_rejected"), Localization.getLocalText("LBL_exec_description")
        };

    /**
     * Labels for gRPC Proxy table.
     */
    private static final String[] GRPC_PROXY_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"),         Localization.getLocalText("LBL_successful_requests"),
        Localization.getLocalText("LBL_error_requests"),  Localization.getLocalText("LBL_responses_sent"),
        Localization.getLocalText("LBL_messages_received"), Localization.getLocalText("LBL_request_duration_mean"),
        Localization.getLocalText("LBL_message_duration_mean")
        };

    /**
     * Labels for persistence table.
     */
    private static final String[] PERSISTENCE_LABELS = new String[]
        {
        Localization.getLocalText("LBL_service_name"), Localization.getLocalText("LBL_persistence_mode"),
        Localization.getLocalText("LBL_active_space_bytes"), Localization.getLocalText("LBL_active_space_mb"),
        Localization.getLocalText("LBL_avge_persistence"), Localization.getLocalText("LBL_max_persistence"),
        Localization.getLocalText("LBL_snapshot_count"), Localization.getLocalText("LBL_status")
        };

    /**
     * Labels for persistence notifications table.
     */
    private static final String[] PERSISTENCE_NOTIFICATIONS_LABELS = new String[]
        {
        Localization.getLocalText("LBL_sequence"), Localization.getLocalText("LBL_service_name"),
        Localization.getLocalText("LBL_operation"), Localization.getLocalText("LBL_start_time"),
        Localization.getLocalText("LBL_end_time"), Localization.getLocalText("LBL_duration"),
        Localization.getLocalText("LBL_message")
        };

    /**
     * Labels for persistence table.
     */
    private static final String[] HTTP_SESSION_LABELS = new String[]
        {
        Localization.getLocalText("LBL_application_id"), Localization.getLocalText("LBL_platform"),
        Localization.getLocalText("LBL_session_timeout"), Localization.getLocalText("LBL_session_cache_name"),
        Localization.getLocalText("LBL_overflow_cache_name"), Localization.getLocalText("LBL_avge_session_size"),
        Localization.getLocalText("LBL_total_reaped_sessions"), Localization.getLocalText("LBL_avge_reaped_sessions"),
        Localization.getLocalText("LBL_avge_reap_duration"), Localization.getLocalText("LBL_last_reap_max"),
        Localization.getLocalText("LBL_session_updates")
        };

    /**
     * Labels for federation table.
     */
    private static final String[] FEDERATION_OVERALL_LABELS = new String[]
        {
        Localization.getLocalText("LBL_service_name"), Localization.getLocalText("LBL_participant"),
        Localization.getLocalText("LBL_status"), Localization.getLocalText("LBL_total_bytes_sent_sec"),
        Localization.getLocalText("LBL_total_msgs_sent_sec"), Localization.getLocalText("LBL_total_bytes_received_sec"),
        Localization.getLocalText("LBL_total_msgs_received_sec")
        };

    /**
     * Labels for federation destination details table.
     */
    private static final String[] FEDERATION_DESTINATION_DETAILS_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_state"),
        Localization.getLocalText("LBL_current_bandwidth"), Localization.getLocalText("LBL_total_bytes_sent"),
        Localization.getLocalText("LBL_total_entries_sent"), Localization.getLocalText("LBL_total_records_sent"),
        Localization.getLocalText("LBL_total_msg_sent"), Localization.getLocalText("LBL_total_msg_unacked")
        };

    /**
     * Labels for federation origin details table.
     */
    private static final String[] FEDERATION_ORIGIN_DETAILS_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_total_bytes_received"),
        Localization.getLocalText("LBL_total_records_received"),Localization.getLocalText("LBL_total_entries_received"),
        Localization.getLocalText("LBL_total_msg_received"), Localization.getLocalText("LBL_total_msg_unacked"),
        };

    /**
     * Labels for ramjournal/ flashjournal table.
     */
    private static final String[] ELASTIC_DATA_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_file_count"),
        Localization.getLocalText("LBL_max_journal_files"), Localization.getLocalText("LBL_max_file_size"),
        Localization.getLocalText("LBL_total_committed_bytes"), Localization.getLocalText("LBL_max_committed_bytes"),
        Localization.getLocalText("LBL_total_data_size"), Localization.getLocalText("LBL_compaction_count"),
        Localization.getLocalText("LBL_exhaustive_compaction_count"), Localization.getLocalText("LBL_current_collector_load_factor")
        };

    /**
     * Labels for JCache Configuration table.
     */
    private static final String[] JCACHE_CONFIG_LABELS = new String[]
        {
        Localization.getLocalText("LBL_config_cache"), Localization.getLocalText("LBL_key_type"),
        Localization.getLocalText("LBL_value_type"),  Localization.getLocalText("LBL_statistics_enabled"),
        Localization.getLocalText("LBL_read_through"), Localization.getLocalText("LBL_write_through"),
        Localization.getLocalText("LBL_store_by_value")
        };

    /**
     * Labels for JCache Statistics table.
     */
    private static final String[] JCACHE_STATS_LABELS = new String[]
        {
        Localization.getLocalText("LBL_config_cache"), Localization.getLocalText("LBL_total_puts"),
        Localization.getLocalText("LBL_total_gets"), Localization.getLocalText("LBL_removals"),
        Localization.getLocalText("LBL_cache_hits"), Localization.getLocalText("LBL_cache_misses"),
        Localization.getLocalText("LBL_evictions"), Localization.getLocalText("GRPH_average_get_time"),
        Localization.getLocalText("GRPH_average_put_time"), Localization.getLocalText("GRPH_average_remove_time"),
        Localization.getLocalText("LBL_hit_percentage"), Localization.getLocalText("LBL_miss_percentage")
        };

    /**
     * Labels for Http Proxy table.
     */
    private static final String[] HTTP_PROXY_LABELS = new String[]
        {
        Localization.getLocalText("LBL_service_name"), Localization.getLocalText("LBL_http_server_type"),
        Localization.getLocalText("LBL_members"),
        Localization.getLocalText("LBL_total_request_count"), Localization.getLocalText("LBL_total_error_count"),
        Localization.getLocalText("LBL_avg_request_per_second"), Localization.getLocalText("LBL_avg_request_time")
        };

    /**
     * Labels for Http Proxy Member table.
     */
    private static final String[] HTTP_PROXY_DETAIL_LABELS = new String[]
        {
        Localization.getLocalText("LBL_node_id"), Localization.getLocalText("LBL_ip_port"),
        Localization.getLocalText("LBL_avg_request_time"), Localization.getLocalText("LBL_avg_request_per_second"),
        Localization.getLocalText("LBL_total_request_count"), Localization.getLocalText("LBL_total_error_count")
        };

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(VisualVMModel.class.getName());

    /**
     * Property to display cluster snapshot.
     */
    public static final String PROP_CLUSTER_SNAPSHOT = "coherence.plugin.visualvm.cluster.snapshot.enabled";

    /**
     * Property to set the time in seconds between refreshing data from the cluster.
     */
    public static final String PROP_REFRESH_TIME = "coherence.plugin.visualvm.refreshtime";

    /**
     * Property to enable logging of query times when retrieving data.
     */
    public static final String PROP_LOG_QUERY_TIMES = "coherence.plugin.visualvm.log.query.times";

    /**
     * Property to disable use of the Coherence Reporter.
     */
    public static final String PROP_REPORTER_DISABLED = "coherence.plugin.visualvm.reporter.disabled";

    /**
     * Property to enable experimental heat map for caches.
     */
    public static final String PROP_HEATMAP_ENABLED = "coherence.plugin.visualvm.heatmap.enabled";

    /**
     * Property to enable dropdown list of snapshots when performing snapshot operations.
     */
    public static final String PROP_PERSISTENCE_LIST_ENABLED = "coherence.plugin.visualvm.persistence.list";

    /**
     * Property to enable additional zoom function for all graphs.
     */
    public static final String PROP_ZOOM_ENABLED = "coherence.plugin.visualvm.zoom.enabled";

    /**
     * Property to set the request timeout (in ms) when using REST to connect to a cluster.
     */
    public static final String PROP_REST_TIMEOUT = "coherence.plugin.visualvm.rest.request.timeout";

    /**
     * Property to enable the HTTP request debugging using REST to connect to a cluster.
     */
    public static final String PROP_REST_DEBUG = "coherence.plugin.visualvm.rest.request.debug";

    /**
     * Property for disabling the MBean check when connecting to WebLogic Server.
     */
    public static final String PROP_DISABLE_MBEAN_CHECK = "coherence.plugin.visualvm.disable.mbean.check";

    // ----- data members ---------------------------------------------------

    /**
     * The time between refresh of JMX data. Defaults to DEFAULT_REFRESH_TIME.
     */
    private long m_nRefreshTime;

    /**
     * Last time statistics were updated.
     */
    private long m_ldtLastUpdate = -1L;

    /**
     * Indicates if we should log detailed JMX query times for troubleshooting.
     */
    private boolean m_fLogJMXQueryTimes = false;

    /**
     * A {@link Map} of {@link List}s to store the retrieved data
     */
    private Map<DataType, List<Entry<Object, Data>>> m_mapCollectedData;

    /**
     * a {@link Map} of report Class and their loaded XML.
     */
    private final Map<Class, String> f_mapReportXML = new HashMap<>();

    /**
     * The selected service for detailed service data.
     */
    private String m_sSelectedService = null;

    /**
     *  The selected hotcache member.
     */
    private String m_sSelectedMember = null;

    /**
     *  The selected hotcache percache operation.
     */
    private String m_sSelectedPerCacheOperation = null;

    /**
     * The selected service for detailed service data in federation tab.
     */
    private String m_sSelectedServiceInFed = null;

    /**
     * The selected service for detail in HTTP proxy tab.
     */
    private String m_sSelectedHttpProxyService = null;

    /**
     * The selected node Id in outbound details in federation tab.
     */
    private String m_sOutboundNodeId = null;

    /**
     * The selected node Id in inbound details in federation tab.
     */
    private String m_sInboundNodeId = null;

    /**
     * The selected cache for detailed cache data.
     */
    private Pair<String, String> m_selectedCache = null;

    /**
     * The selected JCache cache for detailed JCache information.
     */
    private Pair<String, String> m_selectedJCache = null;

    /**
     * The selected service / participants pair in federation tab.
     */
    private Pair<String, String> m_selectedServiceParticipant = null;

    /**
     * Defines if the federation service is used.
     */
    private boolean m_fIsFederationAvailable = false;

    /**
     * Defines if we can get statistics directly from reporter. This is only valid for
     * a coherence version >= 12.1.3. An initial null value indicates that we have
     * not yet determined if we can use the reporter.
     */
    private Boolean m_fReporterAvailable = null;

    /**
     * Defines if we are running Coherence 12.1.3 or above
     */
    private Boolean m_fis1213AndAbove = null;

    /**
     * Defines if we want to include the NameService in the list of proxy servers.
     */
    private boolean m_fIncludeNameService = false;

    /**
     * Defines is proxy servers were present when we first collected stats.
     */
    private Boolean m_fIsCoherenceExtendConfigured = null;

    /**
     * Map of instances of data retrievers for execution of actual JMX queries.
     */
    private final Map<Class, DataRetriever> f_mapDataRetrievers = new HashMap<Class, DataRetriever>();

    /**
     * The set of distributed caches so that we don't double count replicated
     * or optimistic caches.
     */
    private Set<String> m_setKnownDistributedCaches;

    /**
     * The set of domainPartition key values to check for connection
     * to WebLogicServer MT environment.
     */
    private final Set<String> f_setDomainPartitions = new HashSet<>();

    /**
     * The cluster version as a String.
     */
    private String m_sClusterVersion;

    /**
     * The cluster version as an integer for comparison.
     */
    private int m_nClusterVersion;

    /**
     * Indicates if "Load Average" is available for the cluster being sampled.
     * If "SystemLoadAverage" attribute returns -1, then this means we are on
     * Windows (tm) platform and we should use the "SystemCPULoad" instead.
     */
    private boolean m_fIsLoadAverageAvailable = true;

    /**
     * Indicates if this is the first refresh.
     */
    private boolean m_fIsFirstRefresh = true;

    /**
     * Indicates if we can take advantage of REST optimizations from Enh 32530689.
     * If this value is null it means we have not yet determined if the cluster supports this.
     */
    private Boolean m_fIRestCacheOptimizationAvailable = null;
    }
