package org.wso2.carbon.cassandra.cluster.proxy.service;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyKeyspaceInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNodeInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.exception.ClusterProxyAdminException;
import org.wso2.carbon.cassandra.cluster.proxy.internal.AuthenticateStub;
import org.wso2.carbon.cassandra.cluster.proxy.mapper.DataMapper;
import org.wso2.carbon.core.AbstractAdmin;

import static org.wso2.carbon.cassandra.cluster.proxy.internal.AuthenticateStub.getAuthenticatedOperationStub;
public class ClusterProxyOperationAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ClusterProxyOperationAdmin.class);
    private DataMapper dataMapper;
    public ClusterProxyOperationAdmin()
    {
        this.dataMapper = new DataMapper();
    }
    public boolean drainNode(String host)
            throws ClusterProxyAdminException {

        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).drainNode();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while drain the node",e,log);
        }
    }

    public  boolean decommissionNode(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).decommissionNode();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while decommission the node",e,log);
        }
    }

    public void moveNode(String host,String newToken)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).moveNode(newToken);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while move node",e,log);
        }
    }

    public boolean flushColumnFamilies(String host,String keyspace,String[] columnFamilies) throws
                                                                                            ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).flushColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while flush column families",e,log);
        }
    }

    public boolean repairColumnFamilies(String host,String keyspace,String[] columnFamilies) throws
                                                                                             ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).repairColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while repair column families",e,log);
        }
    }

    public boolean compactColumnFamilies(String host,String keyspace,String[] columnFamilies) throws
                                                                                              ClusterProxyAdminException{
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).compactColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while compact column families",e,log);
        }
    }
    public boolean cleanUpColumnFamilies(String host,String keyspace, String[] columnFamilies)
            throws
            ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).cleanUpColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while cleanup column families",e,log);
        }
    }

    /**
     *Perform garbage collection of the node
     */
    public void performGC(String host) throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).performGC();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while performGC",e,log);
        }
    }

    public boolean cleanUpKeyspace(String host,String keyspace)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).cleanUpKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while cleanup keyspace",e,log);
        }
    }

    public boolean flushKeyspace(String host,String keyspace) throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).flushKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while flush keyspace",e,log);
        }
    }

    public boolean compactKeyspace(String host,String keyspace)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).compactKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while compact keyspace",e,log);
        }
    }

    public boolean repairKeyspace(String host,String keyspace)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).repairKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while repair keyspace",e,log);
        }
    }

    public boolean scrubKeyspace(String host,String keyspace) throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).scrubKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while scrub keyspace",e,log);
        }
    }

    public boolean scrubColumnFamilies(String host,String keyspace,String[] columnFamilies) throws
                                                                                            ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).scrubColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while scrub column families",e,log);
        }
    }

    public boolean upgradeSSTablesInKeyspace(String host,String keyspace) throws
                                                                          ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).upgradeSSTablesInKeyspace(keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while upgrade SSTables of keyspace",e,log);
        }
    }

    public boolean upgradeSSTablesColumnFamilies(String host,String keyspace,String[] columnFamilies)
            throws
            ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).upgradeSSTablesColumnFamilies(keyspace, columnFamilies);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while upgrade SSTables of column families",e,log);
        }
    }

    public void takeSnapshotOfNode(String host,String snapShotName) throws
                                                                       ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).takeSnapshotOfNode(snapShotName);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while take snapshot of node",e,log);
        }
    }

    public void takeSnapshotOfKeyspace(String host,String snapShotName,String keyspace) throws
                                                                                           ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).takeSnapshotOfKeyspace(snapShotName, keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while take snapshot of keyspace",e,log);
        }
    }

    public void takeSnapshotOfColumnFamily(String host,String snapShotName,String keyspace,String columnFamily)
            throws ClusterProxyAdminException {
        try{
             AuthenticateStub.getAuthenticatedOperationStub(host).takeSnapshotOfColumnFamily(snapShotName, keyspace, columnFamily);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while take snapshot of column family",e,log);
        }
    }

    public void clearSnapshotOfNode(String host,String snapShotName) throws
                                                                        ClusterProxyAdminException {
        try{
             AuthenticateStub.getAuthenticatedOperationStub(host).clearSnapshotOfNode(snapShotName);

        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while clear the snapshot of node",e,log);
        }
    }

    public void clearSnapshotOfKeyspace(String host,String snapShotName,String keyspace) throws
                                                                                            ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).clearSnapshotOfKeyspace(snapShotName, keyspace);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while clear the snapshot of keyspace",e,log);
        }
    }

    /**
     * Check whether node is join in the ring or not
     * @return return true if node is join in the ring else false
     */
    public boolean isJoined(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).isJoined();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while getting join status",e,log);
        }
    }

    /**
     * Check whether RPC server is running
     * @return return true if RPC is running and else return false
     *
     */
    public boolean isRPCRunning(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).isRPCRunning();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while geting RPC server status",e,log);
        }
    }

    /**
     * Check whether Gossip server is running
     * @return return true if Gossip is running and else return false
     */
    public boolean isGossipServerEnable(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).isGossipServerEnable();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while getting gossip server status",e,log);
        }
    }

    /**
     * Stop the RPC server
     */
    public void stopRPCServer(String host)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).stopRPCServer();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while stop RPC server",e,log);
        }
    }

    /**
     * Start the RPC server of the node
     */
    public void startRPCServer(String host)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).startRPCServer();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while start rpc server",e,log);
        }
    }

    /**
     * Start the gossip server of the node
     */
    public void startGossipServer(String host)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).startGossipServer();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while start gossip server",e,log);
        }
    }

    /**
     * Stop the gossip server of the node
     */
    public void stopGossipServer(String host)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).stopGossipServer();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while stop gossip server",e,log);
        }
    }


    public void setIncrementalBackUpStatus(String host,boolean status)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setIncrementalBackUpStatus(status);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while setting incremental backup status",e,log);
        }
    }

    public boolean getIncrementalBackUpStatus(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).getIncrementalBackUpStatus();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while getting incremental backup status",e,log);
        }
    }

    public boolean joinCluster(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).joinCluster();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while join cluster",e,log);
        }
    }

    public boolean invalidateRowCache(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).invalidateRowCache();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while invalidate row cache",e,log);
        }
    }

    public boolean invalidateKeyCache(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).invalidateKeyCache();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while invalidate key cache",e,log);
        }
    }

    public boolean resetLocalSchema(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedOperationStub(host).resetLocalSchema();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while reset local schema",e,log);
        }
    }

    public void removeToken(String host,String token)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).removeToken(token);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while remove token",e,log);
        }
    }

    public void forceRemoveCompletion(String host)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).forceRemoveCompletion();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while force remove completion",e,log);
        }
    }

    public void setStreamThroughputMbPerSec(String host,int value)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setStreamThroughputMbPerSec(value);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while setting stream throughput",e,log);
        }
    }

    public void setCompactionThroughputMbPerSec(String host,int value)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setCompactionThroughputMbPerSec(value);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while set compaction throughput",e,log);
        }
    }

    public void rebuild(String host,String dataCenter)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).rebuild(dataCenter);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while refresh the node",e,log);
        }
    }

    public void refresh(String host,String keyspace,String columnFamily)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).refresh(keyspace, columnFamily);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while refresh",e,log);
        }
    }

    public void rebuildColumnFamilyWithIndex(String host,String keyspace,String columnFamily,String[] index)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).rebuildColumnFamilyWithIndex(keyspace, columnFamily, index);

        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while rebuild the column family with index",e,log);
        }
    }

    public void rebuildColumnFamily(String host,String keyspace,String columnFamily)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).rebuildColumnFamily(keyspace, columnFamily);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error while rebuild the column family",e,log);
        }
    }

    public void setKeyCacheCapacity(String host,int keyCacheCapacity)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setKeyCacheCapacity(keyCacheCapacity);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to set key cache capacity",e,log);
        }
    }

    public void setRowCacheCapacity(String host,int rowCacheCapacity)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setRowCacheCapacity(rowCacheCapacity);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to set row cache capacity",e,log);
        }
    }

    public String getRemovalStatus(String host)
            throws ClusterProxyAdminException {
        try{
         return   AuthenticateStub.getAuthenticatedOperationStub(host).getRemovalStatus();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to get removal status",e,log);
        }
    }

    public void setCompactionThresholds(String host,String keyspace,String columnFamily,int minThresholds,int maxThresholds)
            throws ClusterProxyAdminException {
        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).setCompactionThresholds(keyspace, columnFamily, minThresholds, maxThresholds);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Error setting compaction thresholds",e,log);
        }
    }
    public String[] getSnapshotTags(String host) throws ClusterProxyAdminException {
        try{
        return AuthenticateStub.getAuthenticatedOperationStub(host).getSnapshots();
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to get snapshot tags",e,log);
        }
    }
    public void stopCompaction(String host,String type)
            throws ClusterProxyAdminException {

        try{
            AuthenticateStub.getAuthenticatedOperationStub(host).stopCompaction(type);
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to complete compaction stop",e,log);
        }
    }

    public ProxyNodeInitialInfo getNodeInitialInfo(String host)
            throws ClusterProxyAdminException {

        try{
            return dataMapper.getNodeInitialInfo(AuthenticateStub.getAuthenticatedOperationStub(host).getNodeInitialInfo());
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to get node initial info",e,log);
        }
    }

    public ProxyKeyspaceInitialInfo getKeyspaceInitialInfo(String host)
            throws ClusterProxyAdminException {

        try{
            return dataMapper.getKeyspaceInitialInfo(AuthenticateStub.getAuthenticatedOperationStub(host).getKeyspaceInitialInfo());
        }catch (Exception e)
        {
            throw new ClusterProxyAdminException("Unable to get node keyspace initial info",e,log);
        }
    }

}
