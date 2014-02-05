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
package org.wso2.carbon.cassandra.cluster.mgt.mbean;

import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.cassandra.cluster.mgt.registry.RegistryStore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class ClusterStorageMBeanService {
    private static Log log = LogFactory.getLog(ClusterStorageMBeanService.class);
    private StorageServiceMBean storageServiceMBean;
    private static boolean isGossipEnable = true;
    private static boolean isIncrementalBackUpEnable = false;

    public ClusterStorageMBeanService() throws
            ClusterDataAdminException {
        createProxyConnection();
    }

    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try {
            storageServiceMBean = clusterMBeanDataAccess.locateStorageServiceMBean();
        }
        catch (Exception e) {
            throw new ClusterDataAdminException("Unable to locate storage service MBean connection", e, log);
        }
    }

    /**
     * Decommission node
     *
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean decommissionNode() throws ClusterDataAdminException {
        try {
            storageServiceMBean.decommission();
            return true;
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot drain the node.Cause due to interrupted exception", e, log);
        }
    }

    /**
     * Drain node
     *
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean drainNode() throws ClusterDataAdminException {
        try {
            storageServiceMBean.drain();
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot drain the node.Cause due to IOException", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot drain the node.Cause due to interrupted exception", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot drain the node.Cause due to execution exception", e, log);
        }
    }

    /**
     * Join ring
     *
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean joinRing() throws ClusterDataAdminException {
        try {
            storageServiceMBean.joinRing();
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot join the ring.Cause due to IOException", e, log);
        } 
    }

    /**
     * Clear snapshot
     *
     * @param tag      snapshot name
     * @param keyspace keyspace name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean clearSnapShot(String tag, String... keyspace) throws
            ClusterDataAdminException {
        RegistryStore registryStore = new RegistryStore();
        try {

            if (keyspace.length == 0 || keyspace[0] == null) {
                storageServiceMBean.clearSnapshot(tag);
                registryStore.clearNodeSnapshot(tag);
            } else {
                storageServiceMBean.clearSnapshot(tag, keyspace);
                registryStore.clearKeyspaceSnapshot(tag, keyspace[0]);
            }
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot clear snapshot.Cause due to IOException", e, log);
        }
    }

    /**
     * Stop RPC server
     */
    public void shutDownNodeRPCServer() {
        storageServiceMBean.stopRPCServer();

    }

    /**
     * Start node RPC server
     */
    public void startNodeRPCServer() {
        storageServiceMBean.startRPCServer();
    }

    /**
     * Get RPC server status
     *
     * @return boolean
     */
    public boolean getRPCServerStatus() {
        return storageServiceMBean.isRPCServerRunning();
    }

    /**
     * Stop gossip server
     */
    public void stopGossipServer() {
        isGossipEnable = false;
        storageServiceMBean.stopGossiping();
    }

    /**
     * Start gossip server
     */
    public void startGossipServer() {
        isGossipEnable = true;
        storageServiceMBean.startGossiping();
    }

    /**
     * Get gossip server status
     *
     * @return boolean
     */
    public boolean isGossipEnable() {
        return isGossipEnable;
    }

    /**
     * Get node  join status
     *
     * @return boolean
     */
    public boolean isJoined() {
        return storageServiceMBean.isJoined();
    }

    /**
     * Flush
     *
     * @param keyspace     keyspace name
     * @param columnFamily column family name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean flush(String keyspace, String... columnFamily) throws
            ClusterDataAdminException {
        try {
            storageServiceMBean.forceTableFlush(keyspace, columnFamily);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot flush the column family.Cause due to IOException", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot flush the column family.Cause due to execution exception", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot flush the column family.Cause due to interrupted exception", e, log);
        }
    }


    /**
     * Cleanup
     *
     * @param keyspace     keyspace name
     * @param columnFamily column family name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean cleanUp(String keyspace, String... columnFamily) throws
            ClusterDataAdminException {
        try {
            storageServiceMBean.forceTableCleanup(keyspace, columnFamily);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot cleanUp the column family.Cause due to IOException", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot cleanUP the column family.Cause due to execution exception", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot cleanUP the column family.Cause due to interrupted exception", e, log);
        }
    }

    /**
     * Repair
     *
     * @param keyspace     keyspace name
     * @param columnFamily column family name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean repair(String keyspace, String... columnFamily) throws
            ClusterDataAdminException {
        try {
            storageServiceMBean.forceTableRepair(keyspace, false, false, columnFamily);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot repair the column family.Cause due to interrupted exception", e, log);
        }

    }

    /**
     * Compact
     *
     * @param keyspace     keyspace name
     * @param columnFamily column family name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean compact(String keyspace, String... columnFamily) throws
            ClusterDataAdminException {
        try {
            storageServiceMBean.forceTableCompaction(keyspace, columnFamily);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot compact the column family.Cause due to IOException", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot compact the column family.Cause due to execution exception", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot compact the column family.Cause due to interrupted exception", e, log);
        }
    }

    /**
     * Move node to new token
     *
     * @param newToken new token
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean moveNode(String newToken) throws ClusterDataAdminException {
        try {
            storageServiceMBean.move(newToken);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot move the node.Cause due to IOException", e, log);
        } 
    }

    /**
     * Take a snapshot of all the tables, optionally specifying only a specific column family.
     *
     * @param tag          the name of the snapshot.
     * @param columnFamily the column family to snapshot or all on null
     * @param keyspaces    the keyspaces to snapshot
     */
    public boolean takeSnapShot(String tag, String columnFamily, String... keyspaces) throws
            ClusterDataAdminException {
        RegistryStore registryStore = new RegistryStore();
        try {
            if (columnFamily != null) {
                if (keyspaces.length != 1) {
                    throw new IOException("When specifying the column family for a snapshot, you must specify one and only one keyspace");
                }
                storageServiceMBean.takeColumnFamilySnapshot(keyspaces[0], columnFamily, tag);
                registryStore.saveColumnFamilySnapshot(tag, keyspaces[0], columnFamily);
                return true;
            } else {
                if (keyspaces.length == 1 && keyspaces[0] != null) {
                    storageServiceMBean.takeSnapshot(tag, keyspaces);
                    registryStore.saveKeyspaceSnapshot(tag, keyspaces[0]);
                } else {
                    storageServiceMBean.takeSnapshot(tag);
                    registryStore.saveNodeSnapshot(tag);
                }
                return true;
            }
        } catch (IOException ex) {
            throw new ClusterDataAdminException("Unable to take the snapshot", ex, log);
        }

    }

    /**
     * Scrub
     *
     * @param keyspace       keyspace name
     * @param columnFamilies column families
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean scrub(boolean disableSnapshot, String keyspace,
                         String... columnFamilies) throws ClusterDataAdminException {
        try {
            storageServiceMBean.scrub(disableSnapshot, keyspace, columnFamilies);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot perform the scrub.Cause due to IOException", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot scrub the column family.Cause due to execution exception", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot scrub the column family.Cause due to interrupted exception", e, log);
        }
    }

    /**
     * Upgrade SS Tables
     *
     * @param keyspace       keyspace name
     * @param columnFamilies column family name
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean upgradeSSTables(String keyspace, boolean excludeCurrentVersion,
                                   String... columnFamilies) throws ClusterDataAdminException {
        try {
            storageServiceMBean.upgradeSSTables(keyspace, excludeCurrentVersion, columnFamilies);
            return true;
        } catch (IOException e) {
            throw new ClusterDataAdminException("Cannot perform the upgradeSSTables.Cause due to IOException", e, log);
        } catch (ExecutionException e) {
            throw new ClusterDataAdminException("Cannot upgradeSSTables for the column family.Cause due to execution exception", e, log);
        } catch (InterruptedException e) {
            throw new ClusterDataAdminException("Cannot upgradeSSTables for the column family.Cause due to interrupted exception", e, log);
        }
    }

    /**
     * Remove all the existing snapshots.
     */
    /*public void clearSnapshot(String tag, String... keyspaces) throws IOException
    {
        storageServiceMBean.clearSnapshot(tag, keyspaces);
    }*/

    /**
     * Set incremental backup status
     *
     * @param status state
     */
    public void setIncrementalBackUpStatus(boolean status) {
        storageServiceMBean.setIncrementalBackupsEnabled(status);
        isIncrementalBackUpEnable = status;
    }

    /**
     * Get incremental backup status
     *
     * @return boolean
     */
    public boolean isIncrementalBackUpEnable() {
        return isIncrementalBackUpEnable;
    }

    /*public void forceTableRepair(String tableName, boolean isSequential, String... columnFamilies) throws IOException
    {
        storageServiceMBean.forceTableRepair(tableName, isSequential, columnFamilies);
    }

    public void forceTableRepairPrimaryRange(String tableName, boolean isSequential, String... columnFamilies) throws IOException
    {
        storageServiceMBean.forceTableRepairPrimaryRange(tableName, isSequential, columnFamilies);
    }

    public void forceTableRepairRange(String beginToken, String endToken, String tableName, boolean isSequential, String... columnFamilies) throws IOException
    {
        storageServiceMBean.forceTableRepairRange(beginToken, endToken, tableName, isSequential, columnFamilies);
    }*/

    /**
     * Get token to endpoint map
     *
     * @return map Map<String, String>
     */
    public Map<String, String> getTokenToEndpointMap() {
        return storageServiceMBean.getTokenToEndpointMap();
    }

    /**
     * Get live nodes
     *
     * @return list List<String>
     */
    public List<String> getLiveNodes() {
        return storageServiceMBean.getLiveNodes();
    }

    /**
     * Get joining nodes
     *
     * @return list List<String>
     */
    public List<String> getJoiningNodes() {
        return storageServiceMBean.getJoiningNodes();
    }

    /**
     * Get leaving nodes
     *
     * @return list List<String>
     */
    public List<String> getLeavingNodes() {
        return storageServiceMBean.getLeavingNodes();
    }

    /**
     * Get moving nodes
     *
     * @return list  List<String>
     */
    public List<String> getMovingNodes() {
        return storageServiceMBean.getMovingNodes();
    }

    /**
     * Get unreachable nodes
     *
     * @return list List<String>
     */
    public List<String> getUnreachableNodes() {
        return storageServiceMBean.getUnreachableNodes();
    }

    /**
     * Get load map
     *
     * @return map Map<String, String>
     */
    public Map<String, String> getLoadMap() {
        return storageServiceMBean.getLoadMap();
    }

    /**
     * Get ownership
     *
     * @return
     */
    public Map<InetAddress, Float> getOwnership() {
        return storageServiceMBean.getOwnership();

    }

    /**
     * Get effective ownership
     *
     * @param keyspace keyspace name
     * @return map Map<String, Float>
     */
    public Map<InetAddress, Float> effectiveOwnership(String keyspace) {
        return storageServiceMBean.effectiveOwnership(keyspace);
    }

    /**
     * Loca new ss tables
     *
     * @param ksName keyspace name
     * @param cfName column family name
     */
    public void loadNewSSTables(String ksName, String cfName) {
        storageServiceMBean.loadNewSSTables(ksName, cfName);
    }

    /**
     * Rebuild index
     *
     * @param ksName   keyspace name
     * @param cfName   column family name
     * @param idxNames indexes
     */
    public void rebuildIndex(String ksName, String cfName, String... idxNames) {
        storageServiceMBean.rebuildSecondaryIndex(ksName, cfName, idxNames);
    }

    /**
     * Set stream throughput
     *
     * @param value value
     */
    public void setStreamThroughput(int value) {
        storageServiceMBean.setStreamThroughputMbPerSec(value);
    }

    /**
     * Get schema version
     *
     * @return String
     */
    public String getSchemaVersion() {
        return storageServiceMBean.getSchemaVersion();
    }

    /**
     * Describe ring JMX
     *
     * @param keyspaceName keyspace name
     * @return list List<String>
     * @throws InvalidRequestException
     */
    public List<String> describeRing(String keyspaceName) throws IOException {
        return storageServiceMBean.describeRingJMX(keyspaceName);
    }

    /**
     * Get release version
     *
     * @return String
     */
    public String getReleaseVersion() {
        return storageServiceMBean.getReleaseVersion();
    }

    /**
     * Rebuild
     *
     * @param sourceDc source data center
     */
    public void rebuild(String sourceDc) {
        storageServiceMBean.rebuild(sourceDc);
    }

    /**
     * Sample key range
     *
     * @return list List<String>
     */
    public List<String> sampleKeyRange() {
        return storageServiceMBean.sampleKeyRange();
    }

    /**
     * Reset local schema
     *
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean resetLocalSchema() throws ClusterDataAdminException {
        try {
            storageServiceMBean.resetLocalSchema();
            return true;
        } catch (Exception e) {
            throw new ClusterDataAdminException("Can't reset the local schema", e, log);
        }
    }

    /**
     * get is initialized
     *
     * @return boolean
     */
    public boolean isInitialized() {
        return storageServiceMBean.isInitialized();
    }

    /**
     * Set compaction throughput
     *
     * @param value value
     */
    public void setCompactionThroughput(int value) {
        storageServiceMBean.setCompactionThroughputMbPerSec(value);
    }

    /**
     * Get compaction throughput
     *
     * @return int
     */
    public int getCompactionThroughput() {
        return storageServiceMBean.getCompactionThroughputMbPerSec();
    }

    /**
     * Get exception count
     *
     * @return int
     */
    public int getExceptionCount() {
        return storageServiceMBean.getExceptionCount();
    }

    /**
     * Get keyspaces
     *
     * @return String array
     */
    public List<String> getKeyspaces() {
        return storageServiceMBean.getKeyspaces();
    }

    /*public void truncate(String tableName, String cfName)
    {
        try
        {
            storageServiceMBean.truncate(tableName, cfName);
        }
        catch (UnavailableException e)
        {
            throw new RuntimeException("Error while executing truncate", e);
        }
        catch (TimeoutException e)
        {
            throw new RuntimeException("Error while executing truncate", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while executing truncate", e);
        }
    }*/

    /**
     * Remove token
     *
     * @param token token name
     */
    public void removeNode(String token) {
        storageServiceMBean.removeNode(token);
    }

    /**
     * Get token removal state
     *
     * @return String
     */
    public String getRemovalStatus() {
        return storageServiceMBean.getRemovalStatus();
    }

    /**
     * Force remove completion
     */
    public void forceRemoveCompletion() {
        storageServiceMBean.forceRemoveCompletion();
    }

    /**
     * Get endpoints
     *
     * @param keyspace keyspace name
     * @param cf       column family name
     * @param key      key
     * @return list List<InetAddress>
     */
    public List<InetAddress> getEndpoints(String keyspace, String cf, String key) {
        return storageServiceMBean.getNaturalEndpoints(keyspace, cf, key);
    }

    /**
     * Get operation mode
     *
     * @return String
     */
    public String getOperationMode() {
        return storageServiceMBean.getOperationMode();
    }

    /**
     * Get endpoint
     *
     * @return String
     */
    public String getEndpoint() throws ClusterDataAdminException {
        String hostId = storageServiceMBean.getLocalHostId();
        Map<String, String> map = storageServiceMBean.getHostIdMap();
        for(Map.Entry<String, String> item : map.entrySet()){
            if(item.getValue().equals(hostId)){
                return item.getKey();
            }
        }
        throw new ClusterDataAdminException("Host ID not found.", log);
    }

    /**
     * Get token
     *
     * @return String
     */
    public String getToken() throws ClusterDataAdminException {
        try {
            return getListAsCommaSeparatedString(storageServiceMBean.getTokens(getEndpoint()));
        } catch (UnknownHostException e) {
            throw new ClusterDataAdminException("Unknown Host Exception Occurred.", e, log);
        }
    }

    /**
     * Get load string
     *
     * @return String
     */
    public String getLoadString() {
        return storageServiceMBean.getLoadString();
    }

    /**
     * Get currant generation number
     *
     * @return int
     */
    public int getCurrentGenerationNumber() {
        return storageServiceMBean.getCurrentGenerationNumber();
    }

    private String getListAsCommaSeparatedString(List<String> list){
        String string = "";
        for(String item : list){
            string = string.concat(item).concat(",");
        }
        return string.substring(0, string.length() - 1);
    }


}
