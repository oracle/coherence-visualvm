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

import java.util.List;
import java.util.Set;

import com.oracle.coherence.plugin.visualvm.tablemodel.model.Pair;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * The sender to be used for getting Management data.
 *
 * @author sr 10.11.2017
 * 
 * @since Coherence 12.2.1.4.0
 */
public interface RequestSender
    {
    /**
     * Get all attributes of an MBean.
     *
     * @param objName  the MBean ObjectName
     *
     * @return the list of attributes
     *
     * @throws Exception in case of errors
     */
    List<Attribute> getAllAttributes(ObjectName objName)
            throws Exception;

    /**
     * Get an attribute of an MBean.
     *
     * @param objectName  the MBean ObjectName
     * @param attribute   the name of the attribute
     *
     * @return the value of the attribute
     *
     * @throws Exception in case of errors
     */
    String getAttribute(ObjectName objectName, String attribute)
            throws Exception;

    /**
     * Get a list of attributes of an MBean.
     *
     * @param objectName   the MBean ObjectName
     * @param asAttribute  the attributes which needs to be fetched
     *
     * @return the attributes
     *
     * @throws Exception in case of errors
     */
     AttributeList getAttributes(ObjectName objectName, String[] asAttribute)
             throws Exception;

    /**
     * Return the list of CacheMBean ObjectNames in the cluster.
     *
     * @return the list of CacheMBean ObjectNames
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllCacheMembers()
            throws Exception;

    /**
     * Invoke a Storage Manager operation.
     *
     * @param sService      the name of the service
     * @param sCacheName    the name of the cache
     * @param sOperation    the name of the operation
     *
     * @throws Exception in case of errors
     */
    void invokeStorageManagerOperation(String sService, String sCacheName, String sOperation)
            throws Exception;

    /**
     * Invoke the reportPartitionStats Storage Manager operation.
     *
     * @param sService      the name of the service
     * @param sCacheName    the name of the cache
     * @param sOutputFormat the output format
     *
     * @return the result of the operation
     *
     * @throws Exception in case of errors
     */
    String invokeReportPartitionsStatsOperation(String sService, String sCacheName, String sOutputFormat)
            throws Exception;

    /**
     * Return the list of all journal member ObjectNames in the cluster.
     *
     * @param sJournalType  the journal type(FlashJournalRM/RamJournalRM)
     *
     * @return the list journal MBean ObjectNames
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllJournalMembers(String sJournalType)
            throws Exception;

    /**
     * Get a list of the cache MBeans.
     *
     * @param sServiceName      the service to which the caches belong
     * @param sCacheName        the cache name
     * @param sDomainPartition  the domain partition to which the caches belong
     *
     * @return the list if cache MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getCacheMembers(String sServiceName, String sCacheName, String sDomainPartition)
            throws Exception;

    /**
     * Get a list of the StorageManager MBeans.
     *
     * @param sServiceName      the service to which the caches belong
     * @param sCacheName        the cache name
     * @param sDomainPartition  the domain partition to which the caches belong
     *
     * @return the list if StorageManager MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getCacheStorageMembers(String sServiceName, String sCacheName, String sDomainPartition)
            throws Exception;

    /**
     * Get the list of cluster MBeans.
     *
     * @return list of cluster MBean ObjectNames
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllClusters()
            throws Exception;

    /**
     * Get the list of all HotCache Adapter MBeans.
     *
     * @return the list of Coherence Adapter MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getHotCacheMembers()
            throws Exception;

    /**
     * Get the list of HotCache Adapter MBeans in a member.
     *
     * @param sMember  the member name/Id
     *
     * @return  the list of Coherence Adapter MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getHotCachePerCacheAdapters(String sMember)
            throws Exception;

    /**
     * Get the list of Session Manager MBeans in the cluster.
     *
     * @param sSessionManager  the session manager type ("WebLogicHttpSessionManager" or "HttpSessionManager")
     *
     * @return list of Session Manager MBeans in the cluster
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllCoherenceWebMembers(String sSessionManager)
            throws Exception;

    /**
     * Get the list of Session Manager MBeans for an application.
     *
     * @param sSessionManager  the session manager type ("WebLogicHttpSessionManager" or "HttpSessionManager")
     * @param sAppId           the application identifier
     *
     * @return list of Session Manager MBeans in the cluster.
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getCoherenceWebMembersForApplication(String sSessionManager, String sAppId)
            throws Exception;

    /**
     * Get the set of OperatingSystem MBeans on which the cluster member is running.
     *
     * @param nodeId  the node id
     *
     * @return the set of OperatingSystem MBeans of the cluster member
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getClusterMemberOS(int nodeId)
            throws Exception;

    /**
     * Get all the Cluster NodeMBean members.
     *
     * @return the list of cluster members
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllClusterMembers()
            throws Exception;

    /**
     * Get all the Executor members.
     *
     * @return the list of executor members
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllExecutorMembers()
            throws Exception;

    /**
     * Get all the gRPCProxy members.
     *
     * @return the list of gRPCProxy members
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllGrpcProxyMembers()
            throws Exception;

    /**
     * Get the list of ServiceMBeans in the cluster.
     *
     * @return list of service MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllServiceMembers()
            throws Exception;

    /**
     * Get the list of Persistence Services in the cluster.
     *
     * @return list of persistence MBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllPersistenceServices()
            throws Exception;

    /***
     * Get the list of ServiceMBean of a service.
     *
     * @param sServiceName      the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return list of ServiceMBeans
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getMembersOfService(String sServiceName, String sDomainPartition)
            throws Exception;

    /**
     * Get the list of ConnectionManager MBeans in the cluster.
     *
     * @return list of proxy server MBeans
     * @throws Exception in case of errors
     */
    Set<ObjectName> getAllProxyServerMembers()
            throws Exception;

    /**
     * Get the list of View MBeans
     *
     * @param sServiceName the name of the service
     * @param sViewName the name of the view
     *
     * @return list of view MBeans
     * @throws Exception in case of errors
     */
    Set<ObjectName> getViewMembers(String sServiceName, String sViewName)
            throws Exception;

    /**
     * Get the list of Connection MBeans in the cluster for a service and node.
     * @param sServiceName service name
     * @param nNodeId node id
     *
     * @return list of proxy server MBeans
     * @throws Exception in case of errors
     */
     Set<ObjectName> getProxyConnections(String sServiceName, int nNodeId)
            throws Exception;

    /**
     * Get the complete ObjectName of the provided MBean.
     *
     * @param objectName  the ObjectName of an MBean
     *
     * @return the complete ObjectName
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getCompleteObjectName(ObjectName objectName)
            throws Exception;

    /**
     * Get the ObjectName of the SimpleAssignmentStrategy MBean.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return the ObjectName of the SimpleAssignmentStrategy MBean.
     *
     * @throws Exception in case of errors
     */
    Set<ObjectName> getPartitionAssignmentObjectName(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Get the scheduled distributions of a service.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return the scheduled distributions
     *
     * @throws Exception in case of errors
     */
    String getScheduledDistributions(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Get the description of a service.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return the service description
     *
     * @throws Exception in case of errors
     */
    String getServiceDescription(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Get the partition ownership of a service.
     *
     * @param sService  the name of the service
     * @param nNodeID   node Id
     *
     * @return the service partition ownership
     *
     * @throws Exception in case of errors
     */
    String getServiceOwnership(String sService, int nNodeID)
            throws Exception;

    /**
     * Get the attributes of the SimpleAssignmentStrategyMBean.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     * @return the attributes of SimpleAssignmentStrategyMBean
     * @throws Exception in case of errors
     */
    Set<Object[]> getPartitionAssignmentAttributes(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Invoke a federation operation.
     *
     * @param sService      the name of the service
     * @param sOperation    the name of the operation
     * @param sParticipant  the name of the participant
     *
     * @throws Exception in case of errors
     */
    void invokeFederationOperation(String sService, String sOperation, String sParticipant)
            throws Exception;

    /**
     * Invoke disconnect all topic operation for a topic and optionally a subscriber.
     *
     * @param sService   topic service
     * @param sTopic     topic name
     * @param sSubscriberGroup subscriber name, can be null
     * @throws Exception in case of errors
     */
    void invokeDisconnectAll(String sService, String sTopic, String sSubscriberGroup)
        throws Exception;

    /**
     * Get the number of pending incoming messages for this federated service.
     *
     * @param sService  the name of the service
     *
     * @return the the number of pending incoming messages for this federated service
     *
     * @throws Exception in case of errors
     */
    Integer retrievePendingIncomingMessages(String sService)
            throws Exception;

    /**
     * Get the number of pending outgoing messages for this federated service.
     *
     * @param sService  the name of the service
     *
     * @return the number of pending outgoing messages for this federated service.
     *
     * @throws Exception in case of errors
     */
    Integer retrievePendingOutgoingMessages(String sService)
            throws Exception;

    /**
     * Get the state of a Cluster member.
     *
     * @param nNodeId  the ID of the cluster member
     *
     * @return the state of a cluster member
     *
     * @throws Exception in case of errors
     */
    String getNodeState(Integer nNodeId)
            throws Exception;

    /**
     * Get the environment of a Cluster member.
     *
     * @param nNodeId  the ID of the cluster member
     *
     * @return the environment of a cluster member
     *
     * @throws Exception in case of errors
     */
    String reportEnvironment(Integer nNodeId)
            throws Exception;

    /**
     * Get the description of a Cluster member.
     *
     * @param nNodeId  the ID of the cluster member
     *
     * @return the description of a cluster member
     *
     * @throws Exception in case of errors
     */
    String getNodeDescription(Integer nNodeId)
            throws Exception;

    /**
     * Issue a dump cluster heap request.
     *
     * @param sRole the role to dump for or null for all roles
     *
     * @throws Exception if any errors
     */
    void dumpClusterHeap(String sRole)
        throws Exception;

    /**
     * Get the list of snapshots of a service.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return the list of snapshots of a service
     * @throws Exception in case of errors
     */
    String[] getSnapshots(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Get the list of archived snapshots of a service.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     *
     * @return list of archived snapshots
     *
     * @throws Exception in case of errors
     */
    String[] getArchivedSnapshots(String sService, String sDomainPartition)
            throws Exception;

    /**
     * Execute a persistence operation.
     *
     * @param sService          the name of the service
     * @param sDomainPartition  the domain partition to which the service belongs
     * @param sOperationName    the name of the operation
     * @param sSnapshotName     the snapshot name to be used for the operation
     *
     * @throws Exception in case of errors
     */
    void executePersistenceOperation(String sService,
                                     String sDomainPartition,
                                     String sOperationName,
                                     String sSnapshotName)
            throws Exception;

    /**
     * Execute operation against subscriber.
     *
     * @param topic           the topic to execute against
     * @param nSubscriber     the subscriber
     * @param sOperationName  the name of the operation
     * @param sType           the type of the subscriber
     * @param nChannel        the channel if required
     *
     * @throws Exception in case of errors
     *
     * @return any results if required by the operation
     */
    Object executeSubscriberOperation(Pair<String, String> topic, long nSubscriber, String sOperationName, String sType, int nChannel)
            throws Exception;
    }
