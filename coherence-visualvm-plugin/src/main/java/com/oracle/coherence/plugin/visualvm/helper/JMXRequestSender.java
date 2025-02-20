/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.coherence.plugin.visualvm.tablemodel.model.AbstractData;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import com.oracle.coherence.plugin.visualvm.tablemodel.model.PersistenceData;
import com.oracle.coherence.plugin.visualvm.Localization;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import static com.oracle.coherence.plugin.visualvm.helper.HttpRequestSender.PART_STATS;
import static com.oracle.coherence.plugin.visualvm.panel.CoherenceTopicPanel.NOTIFY_POPULATED;

/**
 * The {@link RequestSender} based on JMX.
 *
 * @author shyaradh 11.10.2017
 *
 * @since Coherence 12.2.1.4.0
 */
public class JMXRequestSender
        implements RequestSender
    {
    // ------ constructors --------------------------------------------------

    /**
     * Create a {@link JMXRequestSender} object.
     *
     * @param connection  the {@link MBeanServerConnection} to be used by by sender
     */
    public JMXRequestSender(MBeanServerConnection connection)
        {
        this.f_connection = connection;
        }

    @Override
    public List<Attribute> getAllAttributes(ObjectName objName)
            throws Exception
        {
        MBeanInfo            info         = f_connection.getMBeanInfo(objName);
        MBeanAttributeInfo[] attrInfo     = info.getAttributes();
        String[]             asAttributes = new String[attrInfo.length];
        int                  i            = 0;

        // add the attributes
        for (MBeanAttributeInfo attributeInfo : attrInfo)
            {
            asAttributes[i++] = attributeInfo.getName();
            }

        Arrays.sort(asAttributes);

        return f_connection.getAttributes(objName, asAttributes).asList();
        }

    // ------ RequestSender interface ---------------------------------------

    @Override
    public String getAttribute(ObjectName objectName, String attribute)
            throws Exception
        {
        return f_connection.getAttribute(objectName, attribute) + "";
        }

    @Override
    public AttributeList getAttributes(ObjectName objectName, String[] asAttribute)
            throws Exception
        {
        return f_connection.getAttributes(objectName, asAttribute);
        }

    @Override
    public Set<ObjectName> getAllCacheMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Cache,*"), null);
        }

    @Override
    public void invokeStorageManagerOperation(String sService, String sCacheName, String sOperation)
            throws Exception
        {
        ObjectName objectName = new ObjectName(STORAGE_MANAGER_EQUALS + sService + CACHE_EQUALS + sCacheName + ",*");

        Set<ObjectName> setResult = getCompleteObjectName(objectName);
        String sFQN = getFirstResult(setResult);

        invoke(new ObjectName(sFQN), sOperation,  new Object[]{}, new String[]{});
        }

    @Override
    public String invokeReportPartitionsStatsOperation(String sService, String sCacheName, String sOutputFormat)
            throws Exception
        {
        ObjectName objectName = new ObjectName(STORAGE_MANAGER_EQUALS + sService + CACHE_EQUALS + sCacheName + ",*");

        Set<ObjectName> setResult = getCompleteObjectName(objectName);
        String sFQN = getFirstResult(setResult);

        return (String) invoke(new ObjectName(sFQN), PART_STATS, new Object[]{sOutputFormat}, new String[]{String.class.getName()});
        }


    @Override
    public Set<ObjectName> getAllJournalMembers(String sJournalType)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Journal,name="
                + sJournalType + ",*"), null);
        }

    @Override
    public Set getCacheMembers(String sServiceName, String sCacheName, String sDomainPartition)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Cache,service=" + sServiceName
                + (sDomainPartition != null ? DOMAIN_PARTITION + sDomainPartition : "")
                + NAME + sCacheName + ",*"), null);
        }

    @Override
    public Set<ObjectName> getCacheStorageMembers(String sServiceName, String sCacheName, String sDomainPartition)
            throws Exception
        {

        return f_connection.queryNames(new ObjectName(STORAGE_MANAGER_EQUALS
                + sServiceName + (sDomainPartition != null ? DOMAIN_PARTITION + sDomainPartition : "")
                + CACHE_EQUALS + sCacheName + ",*"), null);
        }

    @Override
    public Set<ObjectName> getAllClusters()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName(CLUSTER), null);
        }

    @Override
    public Set<ObjectName> getHotCacheMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=CoherenceAdapter,*"), null);
        }

    @Override
    public Set<ObjectName> getHotCachePerCacheAdapters(String sMember)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName(
                "Coherence:type=CoherenceAdapter,name=hotcache,member="+sMember+",*"), null);
        }

    @Override
    public Set<ObjectName> getAllCoherenceWebMembers(String sSessionManager)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=" +sSessionManager + ",*"), null);
        }

    @Override
    public Set<ObjectName> getCoherenceWebMembersForApplication(String sSessionManager, String sAppId)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=" + sSessionManager + ",appId=" + sAppId
                + ",*"), null);
        }

    @Override
    public Set<ObjectName> getClusterMemberOS(int nodeId)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName(
                "Coherence:type=Platform,Domain=java.lang,subType=OperatingSystem,nodeId="
                + nodeId + ",*"), null);
        }

    @Override
    public Set<ObjectName> getAllClusterMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Node,*"), null);
        }

    @Override
    public Set<ObjectName> getAllExecutorMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Executor,*"), null);
        }

    @Override
    public Set<ObjectName> getAllGrpcProxyMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=GrpcNamedCacheProxy,*"), null);
        }


    @Override
    public Set<ObjectName> getAllServiceMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Service,*"), null);
        }

    @Override
    public Set<ObjectName> getAllPersistenceServices()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Persistence,*"), null);
        }

    @Override
    public Set<ObjectName> getMembersOfService(String sServiceName, String sDomainPartition)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=Service,name=" + sServiceName +
                (sDomainPartition != null ? DOMAIN_PARTITION + sDomainPartition : "") + ",*"), null);
        }

    @Override
    public Set<ObjectName> getAllProxyServerMembers()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=ConnectionManager,*"), null);
        }

    @Override
    public Set<ObjectName> getViewMembers(String sServiceName, String sViewName)
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=View,service=" + sServiceName + NAME + sViewName + ",*"), null);
        }

    @Override
    public Set<ObjectName> getProxyConnections(String sServiceName, int nNodeId)
            throws Exception
       {
       return f_connection.queryNames(new ObjectName("Coherence:type=Connection,name=" + sServiceName + ",nodeId=" + nNodeId + ",*"), null);
       }

    @Override
    public Set<ObjectName> getCompleteObjectName(ObjectName objectName)
            throws Exception
        {
        return f_connection.queryNames(objectName, null);
        }

    @Override
    public Set<ObjectName> getPartitionAssignmentObjectName(String sService, String sDomainPartition)
            throws Exception
        {
        String sQuery = "Coherence:type=PartitionAssignment,service="
                + sService + (sDomainPartition != null ? DOMAIN_PARTITION + sDomainPartition : "")
                + ",responsibility=DistributionCoordinator,*";
        return f_connection.queryNames(new ObjectName(sQuery), null);
        }

    @Override
    public String getScheduledDistributions(String sService, String sDomainPartition)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getPartitionAssignmentObjectName(sService, sDomainPartition);

        String sFQN = getFirstResult(setResult);

        return (String) invoke(new ObjectName(sFQN), "reportScheduledDistributions",
                new Object[]{true}, new String[]{boolean.class.getName()});
        }

    @Override
    public String getServiceDescription(String sService, String sDomainPartition)
            throws Exception
       {
       Set<ObjectName> setResult = getServiceObjectName(sService, sDomainPartition, getLocalMemberId());

       String sFQN = getFirstResult(setResult);

       return (String) invoke(new ObjectName(sFQN), "getServiceDescription", new Object[0], new String[0]);
       }

    @Override
    public String getServiceOwnership(String sService,  int nNodeID)
            throws Exception
       {
       Set<ObjectName> setResult = getServiceObjectName(sService, null, getLocalMemberId());

       String sFQN = getFirstResult(setResult);

       String sResult = (String) invoke(new ObjectName(sFQN), "reportOwnership",
                new Object[]{true}, new String[]{boolean.class.getName()});

       return String.format("{\"ownership\":\"%s\"}", sResult);
       }

    @Override
    public Set<Object[]> getPartitionAssignmentAttributes(String sService, String sDomainPartition)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getPartitionAssignmentObjectName(sService, sDomainPartition);

        String sFQN = getFirstResult(setResult);

        return JMXUtils.runJMXQuery(f_connection, sFQN, new JMXUtils.JMXField[]{
                new JMXUtils.Attribute("AveragePartitionSizeKB"),
                new JMXUtils.Attribute("MaxPartitionSizeKB"),
                new JMXUtils.Attribute("AverageStorageSizeKB"),
                new JMXUtils.Attribute("MaxStorageSizeKB"),
                new JMXUtils.Attribute("MaxLoadNodeId")});
        }

    @Override
    public void invokeDisconnectAll(String sService, String sTopic, String sSubscriberGroup)
        throws Exception
        {
        ObjectName objectName;
        if (sSubscriberGroup == null)
            {
            // topic only
            objectName = new ObjectName("Coherence:type=PagedTopic,service=" + sService + NAME + sTopic + ",*");
            }
        else
           {
           // subscriber group
           objectName = new ObjectName("Coherence:type=PagedTopicSubscriberGroup,service=" + sService + ",topic=" + sTopic +
                                       NAME + sSubscriberGroup + ",*");
           }


        Set<ObjectName> setResult = getCompleteObjectName(objectName);
        String sFQN = getFirstResult(setResult);

        invoke(new ObjectName(sFQN), "disconnectAll",  new Object[]{}, new String[]{});
        }

    @Override
    public void invokeFederationOperation(String sService, String sOperation, String sParticipant)
            throws Exception
        {
        String sObjName = getFederationManagerObjectName(sService);
        invoke(new ObjectName(sObjName), sOperation, new Object[]{sParticipant}, new String[]{String.class.getName()});
        }

    @Override
    public Integer retrievePendingIncomingMessages(String sService)
            throws Exception
        {
        return (Integer) invoke(new ObjectName(getFederationManagerObjectName(sService)),
                "retrievePendingIncomingMessages", new Object[]{}, new String[]{});
        }

    @Override
    public Integer retrievePendingOutgoingMessages(String sService)
            throws Exception
        {
        return (Integer) invoke(new ObjectName(getFederationManagerObjectName(sService)),
                "retrievePendingOutgoingMessages", new Object[]{}, new String[]{});
        }

    @Override
    public String getNodeState(Integer nNodeId)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getCompleteObjectName(
                new ObjectName(COHERENCE_TYPE_NODE + nNodeId + ",*"));

        String sFQN = getFirstResult(setResult);

        return (String) invoke(new ObjectName(sFQN), "reportNodeState", new Object[0], new String[0]);
        }

    @Override
    public String reportEnvironment(Integer nNodeId)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getCompleteObjectName(
                new ObjectName(COHERENCE_TYPE_NODE + nNodeId + ",*"));

        String sFQN = getFirstResult(setResult);

        return (String) invoke(new ObjectName(sFQN), "reportEnvironment", new Object[0], new String[0]);
        }

    @Override
    public String getNodeDescription(Integer nNodeId)
            throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getCompleteObjectName(
                new ObjectName(COHERENCE_TYPE_NODE + nNodeId + ",*"));

        String sFQN = getFirstResult(setResult);

        return (String) invoke(new ObjectName(sFQN), "getNodeDescription", new Object[0], new String[0]);
        }

    /**
     * Issue a dump cluster heap request.
     *
     * @param sRole the role to dump for or null for all roles
     * @throws Exception if any errors
     */
    public void dumpClusterHeap(String sRole) throws Exception
        {
        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getCompleteObjectName(
                new ObjectName(CLUSTER));

        String sFQN = getFirstResult(setResult);

        invoke(new ObjectName(sFQN), "dumpClusterHeap", new Object[]{sRole},
                    new String[] {String.class.getName()});
        }

    public Set<ObjectName> getV1GrpcProxyMBean()
            throws Exception
        {
        return f_connection.queryNames(new ObjectName("Coherence:type=GrpcProxy,*"), null);
        }


    @Override
    public String[] getSnapshots(String sService, String sDomainPartition)
            throws Exception
        {
        String sServiceName = sDomainPartition == null
                ? sService
                : AbstractData.getFullServiceName(sDomainPartition, sService);
        Set<ObjectName> setResult = getCompleteObjectName(new ObjectName(PersistenceData.getMBeanName(sServiceName)));

        String sFQN = getFirstResult(setResult);

        return (String[]) f_connection.getAttribute(new ObjectName(sFQN), "Snapshots");
        }

    @Override
    public String[] getArchivedSnapshots(String sService, String sDomainPartition)
            throws Exception
        {
        Set<ObjectName> setResult = getCompleteObjectName(new ObjectName(PersistenceData.getMBeanName(
                AbstractData.getFullServiceName(sDomainPartition, sService))));

        String sFQN = getFirstResult(setResult);

        return (String[]) invoke(new ObjectName(sFQN), "listArchivedSnapshots", null, null);
        }

    @Override
    public void executePersistenceOperation(String sService,
                                            String sDomainPartition,
                                            String sOperationName,
                                            String sSnapshotName)
            throws Exception
        {
        ObjectName      objectName = new ObjectName(PersistenceData.getMBeanName(AbstractData.getFullServiceName(sDomainPartition, sService)));
        Set<ObjectName> setResult  = getCompleteObjectName(objectName);

        String sFQN = getFirstResult(setResult);

        invoke(new ObjectName(sFQN), sOperationName, new Object[]{sSnapshotName},
                new String[]{String.class.getName()});
        }


    @Override
    public Object executeSubscriberOperation(Pair<String, String> topic, long sSubscriber, String sOperationName, String sType, int nChannel)
            throws Exception
       {
       ObjectName objectName = new ObjectName("Coherence:type=PagedTopicSubscriber,service=" + topic.getX() + ",topic=" + topic.getY() +
                                              ",subType=" + sType + ",id=" + sSubscriber + ",*");
       Set<ObjectName> setResult = getCompleteObjectName(objectName);
       String sFQN = getFirstResult(setResult);

       Object[] aoArgs      = new Object[0];
       String[] asSignature = new String[0];
       if (sOperationName.equals(NOTIFY_POPULATED))
           {
           aoArgs = new Object[]{nChannel};
           asSignature = new String[]{Integer.class.getName()};
           }

       return invoke(new ObjectName(sFQN), sOperationName, aoArgs, asSignature);
       }


    // ------ JMXRequestSender methods --------------------------------------

    public Set<ObjectName> getServiceObjectName(String sService, String sDomainPartition, int nMemberId)
            throws Exception
        {
        String sQuery = "Coherence:type=Service,name="
                        + sService + (sDomainPartition != null ? DOMAIN_PARTITION + sDomainPartition : "")
                        + ",nodeId=" + nMemberId + ",*";
        return f_connection.queryNames(new ObjectName(sQuery), null);
        }

    /**
     * Retrieve the Reporter MBean for the local member Id. We do a query to get the object
     * as it may have additional key values due to a container environment.
     *
     * @param server {@link MBeanServerConnection} to use to query
     * @param nLocalMemberId local member id
     *
     * @return the reporter for the local member Id
     */
    public String getReporterObjectName(MBeanServerConnection server, int nLocalMemberId)
        {
        String sQuery  = "Coherence:type=Reporter,nodeId=" + nLocalMemberId + ",*";
        try
            {
            Set<ObjectName> setResult = server.queryNames(new ObjectName(sQuery), null);

            return getFirstResult(setResult);
            }
        catch (Exception e)
            {
            throw new RuntimeException("Unable to obtain reporter for nodeId=" + nLocalMemberId +
                    ": " + e.getMessage());
            }
        }

    /**
     * Retrieve the local member id from the Coherence Cluster MBean known as
     * Coherence:type=Cluster.
     *
     * @return the local member id or 0 it no Coherence
     */
    public int getLocalMemberId()
        {
        int memberId = 0;

        try
            {
            memberId = (Integer) JMXUtils.runJMXQuerySingleResult(f_connection, CLUSTER,
                    new JMXUtils.Attribute("LocalMemberId"));
            }
        catch (Exception e)
            {
            LOGGER.log(Level.WARNING, Localization.getLocalText("ERR_local_member", e.getMessage()));
            }

        return memberId;
        }

    /**
     * Remove a notification listener.
     *
     * @param objName   the MBean ObjectName
     * @param listener  the JMX listener
     *
     * @throws Exception thrown in case of errors
     */
    public void removeNotificationListener(ObjectName objName, NotificationListener listener)
            throws Exception
        {
        f_connection.removeNotificationListener(objName, listener);
        }


    /**
     * Add a JMX notification for the operations which are triggered on the provided MBean.
     *
     * @param objName   the MBean ObjectName
     * @param listener  the JMX listener
     * @param filter    the filter
     * @param handback  the handbacl
     *
     * @throws Exception thrown in case of errors
     */
    public void addNotificationListener(ObjectName           objName,
                                        NotificationListener listener,
                                        NotificationFilter   filter,
                                        Object               handback)
            throws Exception
        {
        f_connection.addNotificationListener(objName, listener, filter, handback);
        }

    /**
     * Return the fully qualified ObjectName for the ReporterMBean.
     *
     * @param nLocalMemberId  the member id on which the reporter is running
     *
     * @return the fully qualified ObjectName
     */
    public String getReporterObjectName(int nLocalMemberId)
        {
        return getReporterObjectName(f_connection, nLocalMemberId);
        }

    /**
     * Invoke an operation on an MBean.
     *
     * @param objectName  the ObjectName of the MBean
     * @param opName      the operation name
     * @param arguments   the arguments to the operation
     * @param signature   the signature of the operation
     *
     * @return the result of the MBean operation
     *
     * @throws Exception thrown in case of errors
     */
    public Object invoke(ObjectName objectName, String opName, Object[] arguments, String[] signature)
            throws Exception
        {
        return f_connection.invoke(objectName, opName, arguments, signature);
        }

    /**
     * Helper method to get MBean's object name
     *
     * @param  sService   service name
     *
     * @return the object name of the MBean
     *
     * @throws Exception in case of errors
     */
    protected String getFederationManagerObjectName(String sService)
            throws Exception
        {
        String sQuery = "Coherence:type=Federation,service="
                + sService + ",responsibility=Coordinator,*";

        // look up the full name of the MBean in case we are in container
        Set<ObjectName> setResult = getCompleteObjectName(new ObjectName(sQuery));

        return setResult.isEmpty() ? null : setResult.iterator().next().toString();
        }



    /**
     * Returns the first result from a {@link Set} of {@link ObjectName}s.
     *
     * @param setResult {@link Set} of {@link ObjectName}s
     *
     * @return result
     */
    private String getFirstResult(Set<ObjectName> setResult)
        {
        return setResult.isEmpty() ? null : setResult.iterator().next().toString();
        }

    // ------ constants -----------------------------------------------------

    /**
     * The logger object to use.
     */
    private static final Logger LOGGER = Logger.getLogger(JMXRequestSender.class.getName());

    private static final String DOMAIN_PARTITION       = ",domainPartition=";
    private static final String NAME                   = ",name=";
    private static final String CLUSTER                = "Coherence:type=Cluster,*";
    private static final String COHERENCE_TYPE_NODE    = "Coherence:type=Node,nodeId=";
    private static final String CACHE_EQUALS           = ",cache=";
    private static final String STORAGE_MANAGER_EQUALS = "Coherence:type=StorageManager,service=";

    // ------ data members --------------------------------------------------

    /**
     * The {@link MBeanServerConnection} to use.
     */
    private final MBeanServerConnection f_connection;
    }
