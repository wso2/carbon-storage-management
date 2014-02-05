package org.wso2.carbon.cassandra.cluster.mgt.service;/*
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
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.query.ClusterMBeanServiceHandler;
import org.wso2.carbon.cassandra.cluster.mgt.data.ClusterNetstat;
import org.wso2.carbon.cassandra.cluster.mgt.data.ClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.mgt.data.ColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.mgt.data.ColumnFamilyInformation;
import org.wso2.carbon.cassandra.cluster.mgt.data.CompactionStats;
import org.wso2.carbon.cassandra.cluster.mgt.data.DescribeRingProperties;
import org.wso2.carbon.cassandra.cluster.mgt.data.KeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.mgt.data.NodeInformation;
import org.wso2.carbon.cassandra.cluster.mgt.data.ThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.ClusterColumnFamilyMBeanService;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.ClusterMBeanProxy;
import org.wso2.carbon.core.AbstractAdmin;
import java.util.List;

public class ClusterStatsAdminClient extends AbstractAdmin{
    private static Log log = LogFactory.getLog(ClusterStatsAdminClient.class);

    private ClusterMBeanServiceHandler clusterMBeanServiceHandler;
    public ClusterStatsAdminClient() {
        clusterMBeanServiceHandler=new ClusterMBeanServiceHandler();
    }

    public ClusterRingInformation[] getRing(String keyspace) throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getRingInfo(keyspace);
    }

    public NodeInformation getInfo() throws  ClusterDataAdminException {
        return clusterMBeanServiceHandler.getNodeInfo();
    }

    public KeyspaceInfo[] getColumnFamilyStats() throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getColumnFamilyStats();
    }

    public KeyspaceInfo getColumnFamilyStatsForKeyspace(String keyspace)
            throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getKeyspaceColumnFamilyStats(keyspace);
    }

    public ColumnFamilyInformation getSingleColumnFamilyStats(String keyspace,String columnFamily)
            throws ClusterDataAdminException {
         return clusterMBeanServiceHandler.getSingleColumnFamilyStats(keyspace,columnFamily);
    }
    public String getVersion() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().getReleaseVersion();
    }

    public ThreadPoolInfo getTpstats() throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getThreadPoolStats();
    }

    public CompactionStats getCompactionStats() throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getCompactionStats();
    }

    public String getGossipInfo() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterFailureDetectorMBeanService().getGossipInfo();
    }

    public ClusterNetstat getNetstat(String host) throws ClusterDataAdminException {

            return clusterMBeanServiceHandler.getNetworkStats(host);
    }

    public String getTokenRemovalStatus() throws ClusterDataAdminException {
        return ClusterMBeanProxy.getClusterStorageMBeanService().getRemovalStatus();
    }

    public DescribeRingProperties getDescribeRing(String keyspace)
            throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getDescribeRing(keyspace);
    }

    public String[] getRangekeysample() throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getRangeKeySample();
    }

    public int[] getCompactionThresholds(String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        int [] temp=new int[2];
        ClusterColumnFamilyMBeanService clusterColumnFamilyMBeanService=ClusterMBeanProxy.getClusterColumnFamilyMBeanService(keyspace,columnFamily);
        temp[0]=clusterColumnFamilyMBeanService.getMinimumCompactionThreshold();
        temp[1]=clusterColumnFamilyMBeanService.getMaximumCompactionThreshold();
        return temp;
    }

    public ColumnFamilyHistograms[] getColumnFamilyHistograms(String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getCfHistograms(keyspace,columnFamily);
    }

    public String[] getEndpoints(String keyspace,String columnFamily,String key)
            throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getEndPoints(keyspace,columnFamily,key);
    }

    public String[] getSSTables(String keyspace, String columnFamily, String key)
            throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getSSTables(keyspace,columnFamily,key);
    }

    public String[] getColumnFamiliesForKeyspace(String keyspace) throws ClusterDataAdminException {
        return clusterMBeanServiceHandler.getColumnFamiliesForKeyspace(keyspace);
    }

    public String[] getKeyspaces() throws ClusterDataAdminException {
        List<String> keyspaces=ClusterMBeanProxy.getClusterStorageMBeanService().getKeyspaces();
        return keyspaces.toArray(new String[keyspaces.size()]);
    }
}



