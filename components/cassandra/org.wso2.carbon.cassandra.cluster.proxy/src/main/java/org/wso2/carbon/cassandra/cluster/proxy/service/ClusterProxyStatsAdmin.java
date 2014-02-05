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
import org.wso2.carbon.cassandra.cluster.proxy.exception.ClusterProxyAdminException;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyClusterNetstat;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyCompactionStats;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyDescribeRingProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyKeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNodeInformation;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.proxy.internal.AuthenticateStub;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ClusterNetstat;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.CompactionStats;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.DescribeRingProperties;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.KeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.proxy.mapper.DataMapper;

public class ClusterProxyStatsAdmin {
    private static final Log log = LogFactory.getLog(ClusterProxyStatsAdmin.class);
    private DataMapper dataMapper;

    public ClusterProxyStatsAdmin() {
        this.dataMapper = new DataMapper();
    }

    public ProxyClusterRingInformation[] getRing(String host, String keyspace)
            throws ClusterProxyAdminException {
        try{
            ClusterRingInformation[] clusterRingInformations= AuthenticateStub.getAuthenticatedStatsStub(host).getRing(keyspace);
            return dataMapper.getRing(clusterRingInformations);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting cluster ring information",e,log);
        }
    }

    public ProxyNodeInformation getInfo(String host)
            throws ClusterProxyAdminException {
        try{
            return dataMapper.getNodeInfo(AuthenticateStub.getAuthenticatedStatsStub(host).getInfo());
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting cluster node ring information",e,log);
        }
    }

    public ProxyKeyspaceInfo[] getCfstats(String host) throws ClusterProxyAdminException {
        try{
            KeyspaceInfo[] keyspaceInfos= AuthenticateStub.getAuthenticatedStatsStub(host).getColumnFamilyStats();
            return dataMapper.getCfStats(keyspaceInfos);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting column family stats",e,log);
        }
    }

    public String getVersion(String host)
            throws
            ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getVersion();
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting the version",e,log);
        }
    }

    public ProxyThreadPoolInfo getTpstats(String host)
            throws ClusterProxyAdminException {
        try{
            ThreadPoolInfo threadPoolInfo= AuthenticateStub.getAuthenticatedStatsStub(host).getTpstats();
            return dataMapper.getTpstats(threadPoolInfo);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting thread pool information",e,log);
        }
    }

    public ProxyCompactionStats getCompactionStats(String host)
            throws ClusterProxyAdminException {
        try{
            CompactionStats compactionStats= AuthenticateStub.getAuthenticatedStatsStub(host).getCompactionStats();
            return dataMapper.getCompactionStats(compactionStats);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting compaction stats",e,log);
        }
    }

    public String getGossipInfo(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getGossipInfo();
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting gossip information",e,log);
        }
    }

    public ProxyClusterNetstat getNetstat(String connectedHost, String host)
            throws ClusterProxyAdminException {
        try{
            ClusterNetstat clusterNetstat= AuthenticateStub.getAuthenticatedStatsStub(connectedHost).getNetstat(host);
            return dataMapper.getNetstat(clusterNetstat);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting network information",e,log);
        }
    }

    public String getTokenRemovalStatus(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getTokenRemovalStatus();
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting token removal status",e,log);
        }
    }

    public ProxyDescribeRingProperties getDescribeRing(String host, String keyspace)
            throws ClusterProxyAdminException {
        try{
            DescribeRingProperties describeRingProperties= AuthenticateStub.getAuthenticatedStatsStub(host).getDescribeRing(keyspace);
            return dataMapper.getDescribeRing(describeRingProperties);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting describe ring properties",e,log);
        }
    }

    public String[] getRangekeysample(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getRangekeysample();
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting range key sample",e,log);
        }
    }

    public int[] getCompactionThresholds(String host,String keyspace,String columnFamily)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getCompactionThresholds(keyspace,columnFamily);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting cluster ring information",e,log);
        }
    }

    public ProxyColumnFamilyHistograms[] getColumnFamilyHistograms(String host, String keyspace,
                                                                   String columnFamily)
            throws ClusterProxyAdminException {
        try{
            ColumnFamilyHistograms[] columnFamilyHistogramses= AuthenticateStub.getAuthenticatedStatsStub(host).getColumnFamilyHistograms(keyspace, columnFamily);
            return dataMapper.getCFHistograms(columnFamilyHistogramses);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting column family histograms",e,log);
        }
    }

    public String[] getEndpoints(String host,String keyspace,String columnFamily,String key)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getEndpoints(keyspace, columnFamily, key);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting endpoints information",e,log);
        }
    }

    public String[] getSSTables(String host,String keyspace, String columnFamily, String key)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getSSTables(keyspace, columnFamily, key);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting SS Tables",e,log);
        }
    }

    public String[] getColumnFamiliesForKeyspace(String host,String keyspace)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getColumnFamiliesForKeyspace(keyspace);
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting column families for keyspace",e,log);
        }
    }

    public String[] getKeyspaces(String host)
            throws ClusterProxyAdminException {
        try{
            return AuthenticateStub.getAuthenticatedStatsStub(host).getKeyspaces();
        }catch (Exception e){
            throw new ClusterProxyAdminException("Error while getting keyspaces",e,log);
        }
    }
}
