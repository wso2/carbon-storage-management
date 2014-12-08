/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.cluster.mgt.service;

import org.wso2.carbon.cassandra.cluster.mgt.data.KeyspaceInitialInfo;
import org.wso2.carbon.cassandra.cluster.mgt.data.NodeInitialInfo;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.ClusterColumnFamilyMBeanService;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.ClusterMBeanProxy;
import org.wso2.carbon.cassandra.cluster.mgt.operation.NodeMover;
import org.wso2.carbon.cassandra.cluster.mgt.operation.OperationsThreadPool;
import org.wso2.carbon.cassandra.cluster.mgt.operation.SnapshotCleaner;
import org.wso2.carbon.cassandra.cluster.mgt.operation.SnapshotHandler;
import org.wso2.carbon.cassandra.cluster.mgt.registry.RegistryStore;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.List;

public class ClusterOperationAdmin extends AbstractAdmin{

    /**
     * Drain the cassandra node(Cause to stop listening to the write requests)
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to drain node due to exception
     */
    public boolean drainNode() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().drainNode();
    }

    /**
     *  Decommission a cassandra node(Cause to remove node from the ring)
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to decommission node due to exception
     */
    public  boolean decommissionNode() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().decommissionNode();
    }

    /**
     * Move cassandra node to a new token
     * @param newToken Value of the new token
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to move node due to exception
     */
    public void moveNode(String newToken) throws ClusterDataAdminException {
        NodeMover nodeMover=new NodeMover(newToken);
        OperationsThreadPool.getInstance().runOperation(nodeMover);
    }

    /**
     * Flush a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column family
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to flush column family due to exception
     */
    public boolean flushColumnFamilies(String keyspace,String[] columnFamilies) throws
                                                                                ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().flush(keyspace, columnFamilies);
    }

    /**
     * Repair a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column families
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to repair column family due to exception
     */
    public boolean repairColumnFamilies(String keyspace,String[] columnFamilies) throws
                                                                                 ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().repair(keyspace, columnFamilies);
    }

    /**
     * Compact a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column families
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to compact column family due to exception
     */
    public boolean compactColumnFamilies(String keyspace,String[] columnFamilies) throws
                                                                                  ClusterDataAdminException
    {
        return ClusterMBeanProxy.getClusterStorageMBeanService().compact(keyspace, columnFamilies);
    }

    /**
     * Clean up a column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column families
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to compact column family due to exception
     */
    public boolean cleanUpColumnFamilies(String keyspace, String[] columnFamilies) throws
                                                                                   ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().cleanUp(keyspace, columnFamilies);
    }

    /**
     *Perform garbage collection of the node
     */
    public void performGC()
    {
        System.gc();
    }

    /**
     * Cleanup a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to cleanup keyspace due to exception
     */
    public boolean cleanUpKeyspace(String keyspace) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().cleanUp(keyspace);
    }

    /**
     * Flush a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to flush keyspace due to exception
     */
    public boolean flushKeyspace(String keyspace) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().flush(keyspace);
    }

    /**
     * Compact a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to compact keyspace due to exception
     */
    public boolean compactKeyspace(String keyspace) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().compact(keyspace);
    }

    /**
     * Repair a keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to repair keyspace due to exception
     */
    public boolean repairKeyspace(String keyspace) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().repair(keyspace);
    }

    /**
     * Scrub a keyspace(This cause to create a backup inside the all column families in the keyspace)
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to scrub keyspace due to exception
     */
    public boolean scrubKeyspace(boolean disableSnapshot, String keyspace) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().scrub(disableSnapshot, keyspace);
    }

    /**
     * Scrub up a column family(This cause to create a backup inside the column family)
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column family
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to scrub column family due to exception
     */
    public boolean scrubColumnFamilies(boolean disableSnapshot, String keyspace,
                                       String[] columnFamilies) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().scrub(disableSnapshot, keyspace,
                columnFamilies);
    }

    /**
     * Upgrade SSTables in keyspace
     * @param keyspace Name of the keyspace
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException  for unable to Upgrade SSTables in keyspace due to exception
     */
    public boolean upgradeSSTablesInKeyspace(String keyspace,
                                             boolean excludeCurrentVersion) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().upgradeSSTables(keyspace,
                excludeCurrentVersion);
    }

    /**
     * Upgrade SSTables in column family
     * @param keyspace Name of the keyspace where column family located
     * @param columnFamilies Name of the column families
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to Upgrade SSTables in column family due to exception
     */
    public boolean upgradeSSTablesColumnFamilies(String keyspace, boolean excludeCurrentVersion,
                                                 String[] columnFamilies) throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().upgradeSSTables(keyspace,
                excludeCurrentVersion, columnFamilies);
    }

    /**
     * This create a backup of entire node
     * @param snapShotName Name of the snapshot directory
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to take snapshot of the node due to exception
     */
    public void takeSnapshotOfNode(String snapShotName) throws
                                                           ClusterDataAdminException {
        SnapshotHandler snapshot=new SnapshotHandler(snapShotName);
        OperationsThreadPool.getInstance().runOperation(snapshot);
    }

    /**
     * This create a backup of given keyspace
     * @param snapShotName Name of the snapshot directory
     * @param keyspace Name of the keyspace need to be taken a snapshot
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to take snapshot of the keyspace due to exception
     */
    public void takeSnapshotOfKeyspace(String snapShotName,String keyspace) throws
                                                                               ClusterDataAdminException {
        SnapshotHandler snapshot=new SnapshotHandler(snapShotName,keyspace);
        OperationsThreadPool.getInstance().runOperation(snapshot);
    }

    /**
     * This create a backup of given column family
     * @param snapShotName Name of the snapshot directory
     * @param keyspace Name of the keyspace need to be taken a snapshot
     * @param columnFamily column family name
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to take snapshot of the keyspace due to exception
     */
    public void takeSnapshotOfColumnFamily(String snapShotName,String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        SnapshotHandler snapshot=new SnapshotHandler(snapShotName,keyspace,columnFamily);
        OperationsThreadPool.getInstance().runOperation(snapshot);
    }

    /**
     * This clear the backup of node specify in the snapshot name
     * @param snapShotName Name of the snapshot need to be cleared
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to clear the snapshot of the Node due to exception
     */
    public void clearSnapshotOfNode(String snapShotName) throws
                                                            ClusterDataAdminException {
        SnapshotCleaner snapshotCleaner=new SnapshotCleaner(snapShotName);
        OperationsThreadPool.getInstance().runOperation(snapshotCleaner);
    }

    /**
     * This clear the backup of the keyspace specify in the snapshot name
     * @param snapShotName Name of the snapshot need to be cleared
     * @param keyspace Name of the keyspace need to be clear the snapshot
     * @return  return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to clear the snapshot of the keyspace due to exception
     */
    public void clearSnapshotOfKeyspace(String snapShotName,String keyspace) throws
                                                                                ClusterDataAdminException {
        SnapshotCleaner snapshotCleaner=new SnapshotCleaner(snapShotName,keyspace);
        OperationsThreadPool.getInstance().runOperation(snapshotCleaner);
    }

    /**
     * Check whether node is join in the ring or not
     * @return return true if node is join in the ring else false
     */
    public boolean isJoined() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().isJoined();
    }

    /**
     * Check whether RPC server is running
     * @return return true if RPC is running and else return false
     *
     */
    public boolean isRPCRunning() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().getRPCServerStatus();
    }

    /**
     * Check whether Gossip server is running
     * @return return true if Gossip is running and else return false
     */
    public boolean isGossipServerEnable() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().isGossipEnable();
    }

    /**
     * Stop the RPC server
     */
    public void stopRPCServer() throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().shutDownNodeRPCServer();
    }

    /**
     * Start the RPC server of the node
     */
    public void startRPCServer() throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().startNodeRPCServer();
    }

    /**
     * Start the gossip server of the node
     */
    public void startGossipServer() throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().startGossipServer();
    }

    /**
     * Stop the gossip server of the node
     */
    public void stopGossipServer() throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().stopGossipServer();
    }

    /**
     * Set the incremental backup status of the node
     * @param status Pass true to set the incremental backup and false to stop
     */
    public void setIncrementalBackUpStatus(boolean status) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().setIncrementalBackUpStatus(status);
    }

    /**
     * Return whether incremental backup is enable or disable
     * @return true if it enable else false
     */
    public boolean getIncrementalBackUpStatus() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().isIncrementalBackUpEnable();
    }

    /**
     * Join the node to the cluster
     * @return return true if operation success and else false
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException for unable to join the ring due to exception
     */
    public boolean joinCluster() throws ClusterDataAdminException {

        return ClusterMBeanProxy.getClusterStorageMBeanService().joinRing();
    }

    /**
     * Set key cache capacity
     * @param keyCacheCapacity value
     * @throws ClusterDataAdminException
     */
    public void setKeyCacheCapacity(int keyCacheCapacity) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterCacheMBeanService().setKeyCacheCapacity(keyCacheCapacity);
    }

    /**
     * Seet row cache capacity
     * @param rowCacheCapacity value
     * @throws ClusterDataAdminException
     */
    public void setRowCacheCapacity(int rowCacheCapacity) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterCacheMBeanService().setRowCacheCapacity(rowCacheCapacity);
    }

    /**
     * Invalidate row cache
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean invalidateRowCache() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterCacheMBeanService().invalidateRowCache();
    }

    /**
     * Invalidate key cache
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean invalidateKeyCache() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterCacheMBeanService().invalidateKeyCache();
    }

    /**
     * Reset local schema
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean resetLocalSchema() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().resetLocalSchema();
    }

    /**
     * Remove token
     * @param token token value
     * @throws ClusterDataAdminException
     */
    public void removeNode(String token) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().removeNode(token);
    }

    /**
     * Force remove completion
     * @throws ClusterDataAdminException
     */
    public void forceRemoveCompletion() throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().forceRemoveCompletion();
    }

    /**
     * Get removal status
     * @return String value
     * @throws ClusterDataAdminException
     */
    public String getRemovalStatus() throws ClusterDataAdminException{
        return ClusterMBeanProxy.getClusterStorageMBeanService().getRemovalStatus();
    }

    /**
     * Set stream throughput
     * @param value value
     * @throws ClusterDataAdminException
     */
    public void setStreamThroughputMbPerSec(int value) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().setStreamThroughput(value);
    }

    /**
     * Set compaction throughput
     * @param value value
     * @throws ClusterDataAdminException
     */
    public void setCompactionThroughputMbPerSec(int value) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().setCompactionThroughput(value);
    }

    /**
     * Rebuild
     * @param dataCenter data center name
     * @throws ClusterDataAdminException
     */
    public void rebuild(String dataCenter) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().rebuild(dataCenter);
    }

    /**
     * Refresh
     * @param keyspace  keyspace name
     * @param columnFamily column family name
     * @throws ClusterDataAdminException
     */
    public void refresh(String keyspace,String columnFamily) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().loadNewSSTables(keyspace,columnFamily);
    }

    /**
     * Rebuild column family with index
     * @param keyspace keyspace name
     * @param columnFamily column family name
     * @param index index value
     * @throws ClusterDataAdminException
     */
    public void rebuildColumnFamilyWithIndex(String keyspace,String columnFamily,String[] index)
            throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().rebuildIndex(keyspace,columnFamily,index);
    }

    /**
     * Rebuild column family
     * @param keyspace keyspace name
     * @param columnFamily column family name
     * @throws ClusterDataAdminException
     */
    public void rebuildColumnFamily(String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterStorageMBeanService().rebuildIndex(keyspace,columnFamily);
    }

    /**
     * Srt compaction threshold
     * @param keyspace keyspace name
     * @param columnFamily column family name
     * @param minThresholds minimum threshold
     * @param maxThresholds maximum threshold
     * @throws ClusterDataAdminException
     */
    public void setCompactionThresholds(String keyspace,String columnFamily,int minThresholds,int maxThresholds)
            throws ClusterDataAdminException {
        ClusterColumnFamilyMBeanService clusterColumnFamilyMBeanService=ClusterMBeanProxy.getClusterColumnFamilyMBeanService(keyspace,columnFamily);
        clusterColumnFamilyMBeanService.setCompactionThreshold(minThresholds,maxThresholds);
    }

    /**
     * Stop compaction
     * @param type  compaction type
     * @throws ClusterDataAdminException
     */
    public void stopCompaction(String type) throws ClusterDataAdminException {
        ClusterMBeanProxy.getClusterCompactionManagerMBeanService().stop(type);
    }

    /**
     * Get snapshots
     * @return String array
     * @throws ClusterDataAdminException
     */
    public String[] getSnapshots() throws ClusterDataAdminException {
        RegistryStore registryStore=new RegistryStore();
        return registryStore.takeSnapshotTags();
    }

    /**
     * Get node initial stats
     * @return object contains the information
     * @throws ClusterDataAdminException
     */
    public NodeInitialInfo getNodeInitialInfo() throws ClusterDataAdminException {
        RegistryStore registryStore=new RegistryStore();
        NodeInitialInfo nodeInitialInfo=new NodeInitialInfo();
        nodeInitialInfo.setGossipEnable(ClusterMBeanProxy.getClusterStorageMBeanService().isGossipEnable());
        nodeInitialInfo.setRPCEnable(ClusterMBeanProxy.getClusterStorageMBeanService().getRPCServerStatus());
        nodeInitialInfo.setJoin(ClusterMBeanProxy.getClusterStorageMBeanService().isJoined());
        nodeInitialInfo.setIncrementalBackupEnable(ClusterMBeanProxy.getClusterStorageMBeanService().isIncrementalBackUpEnable());
        nodeInitialInfo.setSnapshotNames(registryStore.takeSnapshotTags());
        nodeInitialInfo.setToken(ClusterMBeanProxy.getClusterStorageMBeanService().getToken());
        return nodeInitialInfo;
    }

    /**
     * Get keyspace initial stats
     * @return object contains keyspace info
     * @throws ClusterDataAdminException
     */
    public KeyspaceInitialInfo getKeyspaceInitialInfo() throws ClusterDataAdminException {
        RegistryStore registryStore=new RegistryStore();
        KeyspaceInitialInfo keyspaceInitialInfo=new KeyspaceInitialInfo();
        List<String> keyspaces=ClusterMBeanProxy.getClusterStorageMBeanService().getKeyspaces();
        keyspaceInitialInfo.setKeyspaces(keyspaces.toArray(new String[keyspaces.size()]));
        keyspaceInitialInfo.setSnapshotNames(registryStore.takeSnapshotTags());
        return keyspaceInitialInfo;
    }
}