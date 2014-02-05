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
package org.wso2.carbon.cassandra.cluster.mgt.ui.operation;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.cluster.mgt.ui.exception.ClusterAdminClientException;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyKeyspaceInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNodeInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.stub.operation.ClusterOperationProxyAdminClusterProxyAdminException;
import org.wso2.carbon.cassandra.cluster.proxy.stub.operation.ClusterOperationProxyAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.rmi.RemoteException;


/**
 * Class for performing node level operations
 */
public class ClusterNodeOperationsAdminClient {
    private static final Log log = LogFactory.getLog(ClusterNodeOperationsAdminClient.class);
    private ClusterOperationProxyAdminStub cassandraClusterToolsAdminStub;

    public ClusterNodeOperationsAdminClient(javax.servlet.ServletContext servletContext,
                                            javax.servlet.http.HttpSession httpSession) throws AxisFault{
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);

    }
    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "ClusterOperationProxyAdmin";
        cassandraClusterToolsAdminStub = new ClusterOperationProxyAdminStub(ctx,serviceURL);

        ServiceClient client = cassandraClusterToolsAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     * Drain the node
     * @return Return true if the operation successfully perform and else false
     */
    public boolean drainNode(String host){
        try {
            return cassandraClusterToolsAdminStub.drainNode(host);
        }catch (Exception e)
        {
            throw new ClusterAdminClientException("Error while draining the node",e, log);
        }
    }

    /**
     * Decommission the node
     * @return Return true if the operation successfully perform and else false
     */
    public boolean decommissionNode(String host) {

        try {
            return cassandraClusterToolsAdminStub.decommissionNode(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while decommission the node",e, log);
        }
    }

    /**
     * Perform garbage collector
     */
    public void performGC(String host){
        try {
            cassandraClusterToolsAdminStub.performGC(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while perform GC",e, log);
        }
    }

    /**
     * Move node to new token
     * @param newToken  Name of the token
     * @return Return true if the operation successfully perform and else false
     */
    public void moveNode(String host,String newToken) {
        try {
             cassandraClusterToolsAdminStub.moveNode(host,newToken);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while moving node",e, log);
        }
    }

    /**
     * Take a backup of entire node
     * @param tag Name of the backuo
     * @return Return true if the operation successfully perform and else false
     */
    public void takeNodeSnapShot(String host,String tag){
        try {
            cassandraClusterToolsAdminStub.takeSnapshotOfNode(host,tag);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while taking snapshot of node",e, log);
        }
    }

    /**
     * Clear a snapshot of node
     * @param tag Name of the snapshot which need to be clear
     * @return Return true if the operation successfully perform and else false
     * @throws ClusterAdminClientException for unable to perform operation due to exception
     */
    public void clearNodeSnapShot(String host,String tag) {
        try {
             cassandraClusterToolsAdminStub.clearSnapshotOfNode(host,tag);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while clearing snapshot of node",e, log);
        }
    }

    /**
     * Start RPC server
     */
    public void startRPCServer(String host) {
        try {
            cassandraClusterToolsAdminStub.startRPCServer(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while starting RPC server",e, log);
        }
    }

    /**
     * Stop RPC server
     */
    public void stopRPCServer(String host) {
        try {
            cassandraClusterToolsAdminStub.stopRPCServer(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while stopping RPC server",e, log);
        }
    }

    /**
     * Start Gossip server
     */
    public void startGossipServer(String host) {
        try {
            cassandraClusterToolsAdminStub.startGossipServer(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while starting Gossip server",e, log);
        }
    }

    /**
     * Stop Gossip server
     */
    public void stopGossipServer(String host) {
        try {
            cassandraClusterToolsAdminStub.stopGossipServer(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while stopping Gossip server",e, log);
        }
    }

    /**
     * Enable or Disable incremental backup of cassandra node
     * @param status boolean value to set backup status
     */
    public void setIncrementalBackUpStatus(String host,boolean status) {
        try {
            cassandraClusterToolsAdminStub.setIncrementalBackUpStatus(host,status);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error setting incremental backup status",e, log);
        }
    }

    /**
     * Join to cassandra ring
     */
    public boolean joinRing(String host) {
        try {
            return cassandraClusterToolsAdminStub.joinCluster(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error joining to the cluster",e, log);
        }
    }

    /**
     * Get Gossip server status
     * @return boolean
     */
    public boolean getGossipServerStatus(String host){
        try {
            return cassandraClusterToolsAdminStub.isGossipServerEnable(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error getting gossip server status",e, log);
        }
    }

    /**
     * Get RPC server status
     * @return boolean
     */
    public boolean getRPCServerStatus(String host){
        try {
            return cassandraClusterToolsAdminStub.isRPCRunning(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error getting RPC server status",e, log);
        }
    }

    /**
     * Get Incremental Backup status
     * @return boolean
     */
    public boolean getIncrementalBackUpStatus(String host){
        try {
            return cassandraClusterToolsAdminStub.getIncrementalBackUpStatus(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error getting RPC server status",e, log);
        }
    }

    /**
     * Check whether node is join the ring
     * @return boolean
     */
    public boolean isJoinedRing(String host){
        try {
            return cassandraClusterToolsAdminStub.isJoined(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error getting node join status",e, log);
        }
    }

    public boolean invalidateKeyCache(String host)
    {
        try {
            return cassandraClusterToolsAdminStub.invalidateKeyCache(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while invalidate key cache",e, log);
        }
    }

    public boolean invalidateRowCache(String host)
    {
        try {
            return cassandraClusterToolsAdminStub.invalidateKeyCache(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while invalidate row cache",e, log);
        }
    }

    public void setRowCacheCapacity(String host, int capacity)
    {
        try {
             cassandraClusterToolsAdminStub.setRowCacheCapacity(host,capacity);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting row cache",e, log);
        }
    }

    public void setKeyCacheCapacity(String host, int capacity)
    {
        try {
            cassandraClusterToolsAdminStub.setRowCacheCapacity(host,capacity);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting key cache",e, log);
        }
    }

    public void setStreamThroughput(String host,int capacity)
    {
        try {
            cassandraClusterToolsAdminStub.setStreamThroughputMbPerSec(host, capacity);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  stream throughput",e, log);
        }
    }

    public void setCompactionThroughput(String host,int capacity)
    {
        try {
            cassandraClusterToolsAdminStub.setCompactionThroughputMbPerSec(host, capacity);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public void rebuild(String host,String datacenter)
    {
        try {
            cassandraClusterToolsAdminStub.rebuild(host, datacenter);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public void stopCompaction(String host,String type)
    {
        try {
            cassandraClusterToolsAdminStub.stopCompaction(host,type);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public void removeToken(String host,String token)
    {
        try {
            cassandraClusterToolsAdminStub.removeToken(host,token);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public void forceRemoveToken(String host)
    {
        try {
            cassandraClusterToolsAdminStub.forceRemoveCompletion(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public String getTokenRemovalStatus(String host)
    {
        try {
        return     cassandraClusterToolsAdminStub.getRemovalStatus(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public String[] getSnapshotTags(String host)
    {
        try {
            return     cassandraClusterToolsAdminStub.getSnapshotTags(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting  compaction throughput",e, log);
        }
    }

    public ProxyNodeInitialInfo getNodeInitialInfo(String host)
    {
        try {
            return cassandraClusterToolsAdminStub.getNodeInitialInfo(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while getting node initial info",e, log);
        }
    }

    public ProxyKeyspaceInitialInfo getKeyspaceInitialInfo(String host)
    {
        try {
            return cassandraClusterToolsAdminStub.getKeyspaceInitialInfo(host);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while getting keyspace initial info",e, log);
        }
    }
}
