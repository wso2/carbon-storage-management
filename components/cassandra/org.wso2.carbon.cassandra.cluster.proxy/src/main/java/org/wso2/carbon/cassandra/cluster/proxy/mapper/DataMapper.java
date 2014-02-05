/*
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
package org.wso2.carbon.cassandra.cluster.proxy.mapper;

import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ClusterNetstat;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ColumnFamilyInformation;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.CompactionStats;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.DescribeRingProperties;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.KeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.KeyspaceInitialInfo;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.NodeInformation;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.NodeInitialInfo;
import org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyCacheProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyClusterNetstat;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyColumnFamilyInformation;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyCompactionProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyCompactionStats;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyDescribeRingProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyHeapMemory;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyKeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyKeyspaceInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNetstatProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNetstatStreamingProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNodeInformation;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyNodeInitialInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyThreadPoolDroppedProperties;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.proxy.data.ProxyThreadPoolProperties;

public class DataMapper {
    public ProxyNodeInformation getNodeInfo(NodeInformation info)
    {
        ProxyNodeInformation proxyNodeInformation=new ProxyNodeInformation();
        proxyNodeInformation.setDataCenter(info.getDataCenter());
        proxyNodeInformation.setExceptions(info.getExceptions());
        proxyNodeInformation.setGossipState(info.getGossipState());
        proxyNodeInformation.setGenerationNo(info.getGenerationNo());
        proxyNodeInformation.setRack(info.getRack());
        proxyNodeInformation.setLoad(info.getLoad());
        proxyNodeInformation.setToken(info.getToken());
        proxyNodeInformation.setUptime(info.getUptime());
        ProxyHeapMemory proxyHeapMemory=new ProxyHeapMemory();
        proxyHeapMemory.setMaxMemory(info.getHeapMemory().getMaxMemory());
        proxyHeapMemory.setUseMemory(info.getHeapMemory().getUseMemory());
        proxyNodeInformation.setProxyHeapMemory(proxyHeapMemory);
        ProxyCacheProperties keyCacheProperties=new ProxyCacheProperties();
        keyCacheProperties.setCacheCapacity(info.getKeyCacheProperties().getCacheCapacity());
        keyCacheProperties.setCacheSize(info.getKeyCacheProperties().getCacheSize());
        keyCacheProperties.setCacheHits(info.getKeyCacheProperties().getCacheHits());
        keyCacheProperties.setCacheRecentHitRate(info.getKeyCacheProperties().getCacheRecentHitRate());
        keyCacheProperties.setCacheRequests(info.getKeyCacheProperties().getCacheRequests());
        keyCacheProperties.setCacheSavePeriodInSeconds(info.getKeyCacheProperties().getCacheSavePeriodInSeconds());
        proxyNodeInformation.setKeyProxyCacheProperties(keyCacheProperties);
        ProxyCacheProperties rowCacheProperties=new ProxyCacheProperties();
        rowCacheProperties.setCacheCapacity(info.getRowCacheProperties().getCacheCapacity());
        rowCacheProperties.setCacheSize(info.getRowCacheProperties().getCacheCapacity());
        rowCacheProperties.setCacheHits(info.getRowCacheProperties().getCacheCapacity());
        rowCacheProperties.setCacheRecentHitRate(info.getRowCacheProperties().getCacheCapacity());
        rowCacheProperties.setCacheRequests(info.getRowCacheProperties().getCacheCapacity());
        rowCacheProperties.setCacheSavePeriodInSeconds(info.getRowCacheProperties().getCacheCapacity());
        proxyNodeInformation.setRowProxyCacheProperties(rowCacheProperties);
        return proxyNodeInformation;
    }

    public ProxyKeyspaceInfo[] getCfStats(KeyspaceInfo[] keyspaceInfos)
    {
        ProxyKeyspaceInfo[] proxyKeyspaceInfos=new ProxyKeyspaceInfo[keyspaceInfos.length];
        int j=0;
        for(ProxyKeyspaceInfo p:proxyKeyspaceInfos)
        {
            p=new ProxyKeyspaceInfo();
            p.setKeyspaceName(keyspaceInfos[j].getKeyspaceName());
            p.setTablePendingTasks(keyspaceInfos[j].getTablePendingTasks());
            p.setTableReadCount(keyspaceInfos[j].getTableReadCount());
            p.setTableReadLatency(keyspaceInfos[j].getTableReadLatency());
            p.setTableWriteCount(keyspaceInfos[j].getTableWriteCount());
            p.setTableWriteLatency(keyspaceInfos[j].getTableWriteLatency());
            int i=0;
            ColumnFamilyInformation[] columnFamilyInformations=keyspaceInfos[j].getColumnFamilyInformations();
            ProxyColumnFamilyInformation[] proxyColumnFamilyInformations=new ProxyColumnFamilyInformation[columnFamilyInformations.length];
            for(ProxyColumnFamilyInformation c:proxyColumnFamilyInformations)
            {
                c=new ProxyColumnFamilyInformation();
                c.setBloomFilterFalsePostives(columnFamilyInformations[i].getBloomFilterFalsePostives());
                c.setBloomFilterFalseRatio(columnFamilyInformations[i].getBloomFilterFalseRatio());
                c.setBloomFilterSpaceUsed(columnFamilyInformations[i].getBloomFilterSpaceUsed());
                c.setColumnFamilyName(columnFamilyInformations[i].getColumnFamilyName());
                c.setReadCount(columnFamilyInformations[i].getReadCount());
                c.setCompactedRowMaximumSize(columnFamilyInformations[i].getCompactedRowMaximumSize());
                c.setCompactedRowMeanSize(columnFamilyInformations[i].getCompactedRowMeanSize());
                c.setCompactedRowMinimumSize(columnFamilyInformations[i].getCompactedRowMinimumSize());
                c.setReadLatency(columnFamilyInformations[i].getReadLatency());
                c.setWriteCount(columnFamilyInformations[i].getWriteCount());
                c.setWriteLatency(columnFamilyInformations[i].getWriteLatency());
                c.setSSTableCount(columnFamilyInformations[i].getSSTableCount());
                c.setNumberOfKeys(columnFamilyInformations[i].getNumberOfKeys());
                c.setMemtableColumnsCount(columnFamilyInformations[i].getMemtableColumnsCount());
                c.setMemtableDataSize(columnFamilyInformations[i].getMemtableDataSize());
                c.setMemtableSwitchCount(columnFamilyInformations[i].getMemtableSwitchCount());
                c.setLiveDiskSpaceUsed(columnFamilyInformations[i].getLiveDiskSpaceUsed());
                c.setTotalDiskSpaceUsed(columnFamilyInformations[i].getTotalDiskSpaceUsed());
                proxyColumnFamilyInformations[i]=c;
                i++;
            }
            p.setProxyColumnFamilyInformations(proxyColumnFamilyInformations);
            proxyKeyspaceInfos[j]=p;
            j++;
        }
        return proxyKeyspaceInfos;
    }

    public ProxyThreadPoolInfo getTpstats(ThreadPoolInfo threadPoolInfo)
    {
        ProxyThreadPoolInfo proxyThreadPoolInfo=new ProxyThreadPoolInfo();
        ProxyThreadPoolProperties[] proxyThreadPoolProperties=new ProxyThreadPoolProperties[threadPoolInfo.getThreadPoolProperties().length];
        int i=0;
        for(ProxyThreadPoolProperties p:proxyThreadPoolProperties)
        {
            p=new ProxyThreadPoolProperties();
            p.setActive(threadPoolInfo.getThreadPoolProperties()[i].getActive());
            p.setAllTimeBlocked(threadPoolInfo.getThreadPoolProperties()[i].getAllTimeBlocked());
            p.setBlocked(threadPoolInfo.getThreadPoolProperties()[i].getBlocked());
            p.setCompleted(threadPoolInfo.getThreadPoolProperties()[i].getCompleted());
            p.setPending(threadPoolInfo.getThreadPoolProperties()[i].getPending());
            p.setThreadPoolPropertyName(threadPoolInfo.getThreadPoolProperties()[i].getThreadPoolPropertyName());
            proxyThreadPoolProperties[i]=p;
            i++;
        }
        ProxyThreadPoolDroppedProperties[] proxyThreadPoolDroppedProperties=new ProxyThreadPoolDroppedProperties[threadPoolInfo.getThreadPoolDroppedProperties().length];
        int j=0;
        for(ProxyThreadPoolDroppedProperties c:proxyThreadPoolDroppedProperties)
        {
            c=new ProxyThreadPoolDroppedProperties();
            c.setDroppedCount(threadPoolInfo.getThreadPoolDroppedProperties()[j].getDroppedCount());
            c.setPropertyName(threadPoolInfo.getThreadPoolDroppedProperties()[j].getPropertyName());
            proxyThreadPoolDroppedProperties[j]=c;
            j++;
        }
        proxyThreadPoolInfo.setProxyThreadPoolDroppedProperties(proxyThreadPoolDroppedProperties);
        proxyThreadPoolInfo.setProxyThreadPoolProperties(proxyThreadPoolProperties);
        return proxyThreadPoolInfo;
    }

    public ProxyCompactionStats getCompactionStats(CompactionStats compactionStats)
    {
        ProxyCompactionStats proxyCompactionStats=new ProxyCompactionStats();
        proxyCompactionStats.setActiveCompactionRemainingTime(compactionStats.getActiveCompactionRemainingTime());
        proxyCompactionStats.setPendingTasks(compactionStats.getPendingTasks());
        if(compactionStats.getCompactionProperties()!=null)
        {
            int i=0;
            ProxyCompactionProperties[] proxyCompactionPropertieses=new ProxyCompactionProperties[compactionStats.getCompactionProperties().length];
            for(ProxyCompactionProperties p:proxyCompactionPropertieses)
            {
                if(compactionStats.getCompactionProperties()[i]!=null)
                {
                    p=new ProxyCompactionProperties();
                    p.setBytesCompacted(compactionStats.getCompactionProperties()[i].getBytesCompacted());
                    p.setBytesTotal(compactionStats.getCompactionProperties()[i].getBytesTotal());
                    p.setKeyspace(compactionStats.getCompactionProperties()[i].getKeyspace());
                    p.setColumFamily(compactionStats.getCompactionProperties()[i].getColumFamily());
                    p.setCompactionType(compactionStats.getCompactionProperties()[i].getCompactionType());
                    p.setProgress(compactionStats.getCompactionProperties()[i].getProgress());
                    proxyCompactionPropertieses[i]=p;
                }
                i++;
            }
            proxyCompactionStats.setProxyCompactionProperties(proxyCompactionPropertieses);
        }
        return proxyCompactionStats;
    }

    public ProxyClusterNetstat getNetstat(ClusterNetstat clusterNetstat)
    {
        ProxyClusterNetstat proxyClusterNetstat=new ProxyClusterNetstat();
        proxyClusterNetstat.setOperationMode(clusterNetstat.getOperationMode());
        ProxyNetstatProperties[] proxyNetstatProperties=new ProxyNetstatProperties[clusterNetstat.getNetstatProperties().length];
        int i=0;
        for(ProxyNetstatProperties p:proxyNetstatProperties)
        {
            p=new ProxyNetstatProperties();
            p.setActive(clusterNetstat.getNetstatProperties()[i].getActive());
            p.setCompleted(clusterNetstat.getNetstatProperties()[i].getCompleted());
            p.setPending(clusterNetstat.getNetstatProperties()[i].getPending());
            p.setPoolName(clusterNetstat.getNetstatProperties()[i].getPoolName());
            proxyNetstatProperties[i]=p;
            i++;
        }

        proxyClusterNetstat.setProxyNetstatProperties(proxyNetstatProperties);
        ProxyNetstatStreamingProperties[] proxyNetstatReceivingStreamingProperties;
        ProxyNetstatStreamingProperties[] proxyNetstatResponseStreamingPropertieses;
        if(clusterNetstat.getNetstatReceivingStreamingProperties()!=null)
        {
            proxyNetstatReceivingStreamingProperties=new ProxyNetstatStreamingProperties[clusterNetstat.getNetstatReceivingStreamingProperties().length];
            int j=0;
            for(ProxyNetstatStreamingProperties s:proxyNetstatReceivingStreamingProperties)
            {
                if(clusterNetstat.getNetstatReceivingStreamingProperties()[j]!=null)
                {
                    s=new ProxyNetstatStreamingProperties();
                    s.setHost(clusterNetstat.getNetstatReceivingStreamingProperties()[j].getHost());
                    s.setStreamType(clusterNetstat.getNetstatReceivingStreamingProperties()[j].getStreamType());
                    s.setFileName(clusterNetstat.getNetstatReceivingStreamingProperties()[j].getFileName());
                    proxyNetstatReceivingStreamingProperties[j]=s;
                    j++;
                }
            }
            proxyClusterNetstat.setProxyNetstatReceivingStreamingProperties(proxyNetstatReceivingStreamingProperties);
        }

        if(clusterNetstat.getNetstatResponseStreamingProperties()!=null)
        {
            proxyNetstatResponseStreamingPropertieses=new ProxyNetstatStreamingProperties[clusterNetstat.getNetstatResponseStreamingProperties().length];
            int k=0;
            for(ProxyNetstatStreamingProperties r:proxyNetstatResponseStreamingPropertieses)
            {
                if(clusterNetstat.getNetstatResponseStreamingProperties()[k]!=null)
                {
                    r=new ProxyNetstatStreamingProperties();
                    r.setHost(clusterNetstat.getNetstatResponseStreamingProperties()[k].getHost());
                    r.setStreamType(clusterNetstat.getNetstatResponseStreamingProperties()[k].getStreamType());
                    r.setFileName(clusterNetstat.getNetstatResponseStreamingProperties()[k].getFileName());
                    proxyNetstatResponseStreamingPropertieses[k]=r;
                    k++;
                }

            }
            proxyClusterNetstat.setProxyNetstatResponseStreamingProperties(proxyNetstatResponseStreamingPropertieses);
        }
        return proxyClusterNetstat;
    }

    public ProxyClusterRingInformation[] getRing(ClusterRingInformation[] clusterRingInformation)
    {
        ProxyClusterRingInformation[] proxyClusterRingInformation=new ProxyClusterRingInformation[clusterRingInformation.length];
        int j=0;
        for(ProxyClusterRingInformation p:proxyClusterRingInformation)
        {
            p=new ProxyClusterRingInformation();
            p.setAddress(clusterRingInformation[j].getAddress());
            p.setEndPoint(clusterRingInformation[j].getEndPoint());
            p.setToken(clusterRingInformation[j].getToken());
            p.setDataCenter(clusterRingInformation[j].getDataCenter());
            p.setDataCenter(clusterRingInformation[j].getDataCenter());
            p.setEffectiveOwnership(clusterRingInformation[j].getEffectiveOwnership());
            p.setRack(clusterRingInformation[j].getRack());
            p.setLoad(clusterRingInformation[j].getLoad());
            p.setState(clusterRingInformation[j].getState());
            p.setStatus(clusterRingInformation[j].getStatus());
            proxyClusterRingInformation[j]=p;
            j++;
        }
        return proxyClusterRingInformation;
    }

    public ProxyDescribeRingProperties getDescribeRing(DescribeRingProperties describeRingProperties)
    {
        ProxyDescribeRingProperties proxyDescribeRingProperties=new ProxyDescribeRingProperties();
        proxyDescribeRingProperties.setSchemaVersion(describeRingProperties.getSchemaVersion());
        proxyDescribeRingProperties.setTokenRange(describeRingProperties.getTokenRange());
        return proxyDescribeRingProperties;
    }

    public ProxyColumnFamilyHistograms[] getCFHistograms(ColumnFamilyHistograms[] columnFamilyHistogramses)
    {
        ProxyColumnFamilyHistograms[] proxyColumnFamilyHistogramses=new ProxyColumnFamilyHistograms[columnFamilyHistogramses.length];
        int i=0;
        for(ProxyColumnFamilyHistograms p:proxyColumnFamilyHistogramses)
        {
            p=new ProxyColumnFamilyHistograms();
            p.setOffset(columnFamilyHistogramses[i].getOffset());
            p.setColumnCount(columnFamilyHistogramses[i].getColumnCount());
            p.setReadLatency(columnFamilyHistogramses[i].getReadLatency());
            p.setRowSize(columnFamilyHistogramses[i].getRowSize());
            p.setSSTables(columnFamilyHistogramses[i].getSSTables());
            p.setWriteLatency(columnFamilyHistogramses[i].getWriteLatency());
            proxyColumnFamilyHistogramses[i]=p;
            i++;
        }
        return proxyColumnFamilyHistogramses;
    }

    public ProxyNodeInitialInfo getNodeInitialInfo(NodeInitialInfo nodeInitialInfo)
    {
        ProxyNodeInitialInfo proxyNodeInitialInfo=new ProxyNodeInitialInfo();
        proxyNodeInitialInfo.setGossipEnable(nodeInitialInfo.getGossipEnable());
        proxyNodeInitialInfo.setRPCEnable(nodeInitialInfo.getRPCEnable());
        proxyNodeInitialInfo.setIncrementalBackupEnable(nodeInitialInfo.getIncrementalBackupEnable());
        proxyNodeInitialInfo.setJoin(nodeInitialInfo.getJoin());
        proxyNodeInitialInfo.setSnapshotNames(nodeInitialInfo.getSnapshotNames());
        proxyNodeInitialInfo.setToken(nodeInitialInfo.getToken());
        return proxyNodeInitialInfo;
    }

    public ProxyKeyspaceInitialInfo getKeyspaceInitialInfo(KeyspaceInitialInfo keyspaceInitialInfo)
    {
        ProxyKeyspaceInitialInfo proxyKeyspaceInitialInfo=new ProxyKeyspaceInitialInfo();
        proxyKeyspaceInitialInfo.setKeyspaces(keyspaceInitialInfo.getKeyspaces());
        proxyKeyspaceInitialInfo.setSnapshotNames(keyspaceInitialInfo.getSnapshotNames());
        return proxyKeyspaceInitialInfo;
    }
}

