package org.wso2.carbon.cassandra.cluster.mgt.query;/*
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


import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.db.compaction.OperationType;
import org.apache.cassandra.utils.EstimatedHistogram;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.mgt.data.*;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.*;

import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.*;

public class ClusterMBeanServiceHandler {
    private static Log log = LogFactory.getLog(ClusterMBeanServiceHandler.class);
    private ClusterStorageMBeanService clusterStorageMBeanService;
    private ClusterCacheMBeanService clusterCacheMBeanService;
    private ClusterColumnFamilyMBeanService clusterColumnFamilyMBeanService;
    private ClusterCompactionManagerMBeanService clusterCompactionManagerMBeanService;
    private ClusterEndpointSnitchMBeanService clusterEndpointSnitchMBeanService;
    private ClusterMemoryMXBeanService clusterMemoryMXBeanService;
    private ClusterMessagingServiceMBeanService clusterMessagingServiceMBeanService;
    private ClusterRuntimeMXBeanService clusterRuntimeMXBeanService;
    private ClusterStreamProxyMBeanService clusterStreamProxyMBeanService;
    private ClusterThreadPoolProxyMBeanService clusterThreadPoolProxyMBeanService;

    public ClusterRingInformation[] getRingInfo(String keyspace) throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        clusterEndpointSnitchMBeanService = ClusterMBeanProxy.getClusterEndpointSnitchMBeanService();
        Map<String, String> tokenToEndpoint = clusterStorageMBeanService.getTokenToEndpointMap();
        List<String> sortedTokens = new ArrayList<String>(tokenToEndpoint.keySet());
        ClusterRingInformation[] clusterRingInformations = new ClusterRingInformation[sortedTokens.size()];
        ClusterRingInformation clusterRingInformation;
        Collection<String> liveNodes = clusterStorageMBeanService.getLiveNodes();
        Collection<String> deadNodes = clusterStorageMBeanService.getUnreachableNodes();
        Collection<String> joiningNodes = clusterStorageMBeanService.getJoiningNodes();
        Collection<String> leavingNodes = clusterStorageMBeanService.getLeavingNodes();
        Collection<String> movingNodes = clusterStorageMBeanService.getMovingNodes();
        Map<String, String> loadMap = clusterStorageMBeanService.getLoadMap();

        // Calculate per-token ownership of the ring
        Map<InetAddress, Float> ownerships;
        try {
            ownerships = clusterStorageMBeanService.effectiveOwnership(keyspace);
        }
        catch (Exception ex) {
            ownerships = clusterStorageMBeanService.getOwnership();
        }

        int count = 0;
        for (String token : sortedTokens) {
            clusterRingInformation = new ClusterRingInformation();
            String primaryEndpoint = tokenToEndpoint.get(token);
            clusterRingInformation.setAddress(primaryEndpoint);
            clusterRingInformation.setToken(token);
            String dataCenter;
            String hostName;
            try {
                hostName = InetAddress.getByName(primaryEndpoint).getHostName();
                dataCenter = clusterEndpointSnitchMBeanService.getDataCenter(primaryEndpoint);
                clusterRingInformation.setDataCenter(dataCenter);
                clusterRingInformation.setEndPoint(hostName);
            }
            catch (UnknownHostException e) {
                dataCenter = "Unknown";
                clusterRingInformation.setEndPoint("Unknown");
                clusterRingInformation.setDataCenter(dataCenter);
            }
            String rack;
            try {
                rack = clusterEndpointSnitchMBeanService.getRack(primaryEndpoint);
                clusterRingInformation.setRack(rack);
            }
            catch (UnknownHostException e) {
                rack = "Unknown";
                clusterRingInformation.setRack(rack);
            }
            String status = liveNodes.contains(primaryEndpoint)
                    ? "Up"
                    : deadNodes.contains(primaryEndpoint)
                    ? "Down"
                    : "?";
            clusterRingInformation.setStatus(status);
            String state = "Normal";

            if (joiningNodes.contains(primaryEndpoint))
                state = "Joining";
            else if (leavingNodes.contains(primaryEndpoint))
                state = "Leaving";
            else if (movingNodes.contains(primaryEndpoint))
                state = "Moving";

            clusterRingInformation.setState(state);
            String load = loadMap.containsKey(primaryEndpoint)
                    ? loadMap.get(primaryEndpoint)
                    : "?";
            clusterRingInformation.setLoad(load);
            String owns = new DecimalFormat("##0.00%").format(ownerships.get(token) == null ? 0.0F : ownerships.get(token));
            clusterRingInformation.setEffectiveOwnership(owns);
            clusterRingInformations[count] = clusterRingInformation;
            count++;
        }
        return clusterRingInformations;
    }

    public NodeInformation getNodeInfo()
            throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        clusterEndpointSnitchMBeanService = ClusterMBeanProxy.getClusterEndpointSnitchMBeanService();
        clusterCacheMBeanService = ClusterMBeanProxy.getClusterCacheMBeanService();
        clusterRuntimeMXBeanService = ClusterMBeanProxy.getRuntimeMXBeanService();
        clusterMemoryMXBeanService = ClusterMBeanProxy.getClusterMemoryMXBeanService();
        NodeInformation nodeInformation = new NodeInformation();
        boolean gossipInitialized = clusterStorageMBeanService.isInitialized();
        nodeInformation.setToken(clusterStorageMBeanService.getToken());
        nodeInformation.setGossipState(gossipInitialized);
        nodeInformation.setThriftState(clusterStorageMBeanService.getRPCServerStatus());
        nodeInformation.setLoad(clusterStorageMBeanService.getLoadString());
        if (gossipInitialized)
            nodeInformation.setGenerationNo(clusterStorageMBeanService.getCurrentGenerationNumber());
        else
            nodeInformation.setGenerationNo(0);

        // Uptime
        long secondsUp = clusterRuntimeMXBeanService.getUptime() / 1000;
        nodeInformation.setUptime(secondsUp);

        // Memory usage
        MemoryUsage heapUsage = clusterMemoryMXBeanService.getHeapMemoryUsage();
        HeapMemory heapMemory = new HeapMemory();
        double memUsed = (double) heapUsage.getUsed() / (1024 * 1024);
        double memMax = (double) heapUsage.getMax() / (1024 * 1024);
        heapMemory.setMaxMemory(memMax);
        heapMemory.setUseMemory(memUsed);
        nodeInformation.setHeapMemory(heapMemory);

        // Data Center/Rack
        try {
            nodeInformation.setDataCenter(clusterEndpointSnitchMBeanService.getDataCenter(clusterStorageMBeanService.getEndpoint()));
            nodeInformation.setRack(clusterEndpointSnitchMBeanService.getRack(clusterStorageMBeanService.getEndpoint()));
        } catch (UnknownHostException e) {
            throw new ClusterDataAdminException("Host is unknown when retrieving node info", e, log);
        }
        // Exceptions
        nodeInformation.setExceptions(clusterStorageMBeanService.getExceptionCount());
        CacheProperties keyCacheProperties = new CacheProperties();
        CacheProperties rowCacheProperties = new CacheProperties();

        // Key Cache: Hits, Requests, RecentHitRate, SavePeriodInSeconds
        keyCacheProperties.setCacheSize(clusterCacheMBeanService.getKeyCacheSize());
        keyCacheProperties.setCacheCapacity(clusterCacheMBeanService.getKeyCacheCapacityInBytes());
        keyCacheProperties.setCacheHits(clusterCacheMBeanService.getKeyCacheHits());
        keyCacheProperties.setCacheRequests(clusterCacheMBeanService.getKeyCacheRequests());
        keyCacheProperties.setCacheRecentHitRate(clusterCacheMBeanService.getKeyCacheRecentHitRate());
        keyCacheProperties.setCacheSavePeriodInSeconds(clusterCacheMBeanService.getKeyCacheSavePeriodInSeconds());

        // Row Cache: Hits, Requests, RecentHitRate, SavePeriodInSeconds

        rowCacheProperties.setCacheSize(clusterCacheMBeanService.getRowCacheSize());
        rowCacheProperties.setCacheCapacity(clusterCacheMBeanService.getRowCacheCapacityInBytes());
        rowCacheProperties.setCacheHits(clusterCacheMBeanService.getRowCacheHits());
        rowCacheProperties.setCacheRequests(clusterCacheMBeanService.getRowCacheRequests());
        rowCacheProperties.setCacheRecentHitRate(clusterCacheMBeanService.getRowCacheRecentHitRate());
        rowCacheProperties.setCacheSavePeriodInSeconds(clusterCacheMBeanService.getRowCacheSavePeriodInSeconds());
        nodeInformation.setRowCacheProperties(rowCacheProperties);
        nodeInformation.setKeyCacheProperties(keyCacheProperties);
        return nodeInformation;
    }

    public KeyspaceInfo[] getColumnFamilyStats() throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService();
        KeyspaceInfo[] keyspaceInfos;
        ColumnFamilyInformation[] columnFamilyInformations;
        Map<String, List<ColumnFamilyStoreMBean>> cfstoreMap = new HashMap<String, List<ColumnFamilyStoreMBean>>();

        // get a list of column family stores
        Iterator<Map.Entry<String, ColumnFamilyStoreMBean>> cfamilies = clusterColumnFamilyMBeanService.getColumnFamilyStoreMBeanProxies();

        while (cfamilies.hasNext()) {
            Map.Entry<String, ColumnFamilyStoreMBean> entry = cfamilies.next();
            String tableName = entry.getKey();
            ColumnFamilyStoreMBean cfsProxy = entry.getValue();

            if (!cfstoreMap.containsKey(tableName)) {
                List<ColumnFamilyStoreMBean> columnFamilies = new ArrayList<ColumnFamilyStoreMBean>();
                columnFamilies.add(cfsProxy);
                cfstoreMap.put(tableName, columnFamilies);
            } else {
                cfstoreMap.get(tableName).add(cfsProxy);
            }
        }
        keyspaceInfos = new KeyspaceInfo[cfstoreMap.size()];
        // print out the table statistics
        int keyspaceCount = 0;

        for (Map.Entry<String, List<ColumnFamilyStoreMBean>> entry : cfstoreMap.entrySet()) {
            KeyspaceInfo keyspaceInfo = new KeyspaceInfo();
            String tableName = entry.getKey();
            List<ColumnFamilyStoreMBean> columnFamilies = entry.getValue();
            long tableReadCount = 0;
            long tableWriteCount = 0;
            int tablePendingTasks = 0;
            double tableTotalReadTime = 0.0f;
            double tableTotalWriteTime = 0.0f;

            keyspaceInfo.setKeyspaceName(tableName);
            for (ColumnFamilyStoreMBean cfstore : columnFamilies) {
                long writeCount = cfstore.getWriteCount();
                long readCount = cfstore.getReadCount();

                if (readCount > 0) {
                    tableReadCount += readCount;
                    tableTotalReadTime += cfstore.getTotalReadLatencyMicros();
                }
                if (writeCount > 0) {
                    tableWriteCount += writeCount;
                    tableTotalWriteTime += cfstore.getTotalWriteLatencyMicros();
                }
                tablePendingTasks += cfstore.getPendingTasks();
            }

            double tableReadLatency = tableReadCount > 0 ? tableTotalReadTime / tableReadCount / 1000 : Double.NaN;
            double tableWriteLatency = tableWriteCount > 0 ? tableTotalWriteTime / tableWriteCount / 1000 : Double.NaN;

            keyspaceInfo.setTableReadCount(tableReadCount);
            keyspaceInfo.setTableReadLatency(tableReadLatency);
            keyspaceInfo.setTableWriteCount(tableWriteCount);
            keyspaceInfo.setTableWriteLatency(tableWriteLatency);
            keyspaceInfo.setTablePendingTasks(tablePendingTasks);
            columnFamilyInformations = new ColumnFamilyInformation[columnFamilies.size()];
            int keyspaceColumnFamilyCount = 0;
            // print out column family statistics for this table
            for (ColumnFamilyStoreMBean cfstore : columnFamilies) {
                columnFamilyInformations[keyspaceColumnFamilyCount] = getColumnFamilyStats(cfstore);
                keyspaceColumnFamilyCount++;
            }
            keyspaceInfo.setColumnFamilyInformations(columnFamilyInformations);
            keyspaceInfos[keyspaceCount] = keyspaceInfo;
            keyspaceCount++;
        }
        return keyspaceInfos;
    }

    public KeyspaceInfo getKeyspaceColumnFamilyStats(String keyspace) throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService();
        KeyspaceInfo keyspaceInfo = new KeyspaceInfo();
        // get a list of column family stores
        Iterator<Map.Entry<String, ColumnFamilyStoreMBean>> cfamilies = clusterColumnFamilyMBeanService.getColumnFamilyStoreMBeanProxies();
        List<ColumnFamilyInformation> columnFamilyInformations = new ArrayList<ColumnFamilyInformation>();
        List<ColumnFamilyStoreMBean> columnFamilyMBeans = new ArrayList<ColumnFamilyStoreMBean>();
        keyspaceInfo.setKeyspaceName(keyspace);
        while (cfamilies.hasNext()) {
            Map.Entry<String, ColumnFamilyStoreMBean> entry = cfamilies.next();
            String tableName = entry.getKey();
            ColumnFamilyStoreMBean cfsProxy = entry.getValue();

            if (keyspace.equals(tableName)) {
                columnFamilyInformations.add(getColumnFamilyStats(cfsProxy));
                columnFamilyMBeans.add(cfsProxy);
            }
        }

        long tableReadCount = 0;
        long tableWriteCount = 0;
        int tablePendingTasks = 0;
        double tableTotalReadTime = 0.0f;
        double tableTotalWriteTime = 0.0f;

        for (ColumnFamilyStoreMBean cfstore : columnFamilyMBeans) {
            long writeCount = cfstore.getWriteCount();
            long readCount = cfstore.getReadCount();

            if (readCount > 0) {
                tableReadCount += readCount;
                tableTotalReadTime += cfstore.getTotalReadLatencyMicros();
            }
            if (writeCount > 0) {
                tableWriteCount += writeCount;
                tableTotalWriteTime += cfstore.getTotalWriteLatencyMicros();
            }
            tablePendingTasks += cfstore.getPendingTasks();
        }

        double tableReadLatency = tableReadCount > 0 ? tableTotalReadTime / tableReadCount / 1000 : Double.NaN;
        double tableWriteLatency = tableWriteCount > 0 ? tableTotalWriteTime / tableWriteCount / 1000 : Double.NaN;

        keyspaceInfo.setTableReadCount(tableReadCount);
        keyspaceInfo.setTableReadLatency(tableReadLatency);
        keyspaceInfo.setTableWriteCount(tableWriteCount);
        keyspaceInfo.setTableWriteLatency(tableWriteLatency);
        keyspaceInfo.setTablePendingTasks(tablePendingTasks);
        keyspaceInfo.setColumnFamilyInformations(columnFamilyInformations.toArray(new ColumnFamilyInformation[columnFamilyInformations.size()]));
        return keyspaceInfo;
    }

    public ColumnFamilyInformation getSingleColumnFamilyStats(String keyspace, String columnFamily) throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService();
        // get a list of column family stores
        Iterator<Map.Entry<String, ColumnFamilyStoreMBean>> cfamilies = clusterColumnFamilyMBeanService.getColumnFamilyStoreMBeanProxies();

        while (cfamilies.hasNext()) {
            Map.Entry<String, ColumnFamilyStoreMBean> entry = cfamilies.next();
            String tableName = entry.getKey();
            ColumnFamilyStoreMBean cfsProxy = entry.getValue();

            if (keyspace.equals(tableName) && columnFamily.equals(cfsProxy.getColumnFamilyName())) {
                return getColumnFamilyStats(cfsProxy);
            }
        }
        return null;
    }


    public String[] getColumnFamiliesForKeyspace(String keyspace) throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService();
        List<String> keyspaceColumnFamilies = new ArrayList<String>();
        // get a list of column family stores
        Iterator<Map.Entry<String, ColumnFamilyStoreMBean>> cfamilies = clusterColumnFamilyMBeanService.getColumnFamilyStoreMBeanProxies();
        while (cfamilies.hasNext()) {
            Map.Entry<String, ColumnFamilyStoreMBean> entry = cfamilies.next();
            String tableName = entry.getKey();
            ColumnFamilyStoreMBean cfsProxy = entry.getValue();
            if (keyspace.equals(tableName)) {
                keyspaceColumnFamilies.add(cfsProxy.getColumnFamilyName());
            }
        }
        return keyspaceColumnFamilies.toArray(new String[keyspaceColumnFamilies.size()]);
    }

    private ColumnFamilyInformation getColumnFamilyStats(ColumnFamilyStoreMBean cfsProxy) {
        ColumnFamilyInformation columnFamilyInformation = new ColumnFamilyInformation();
        columnFamilyInformation.setColumnFamilyName(cfsProxy.getColumnFamilyName());
        columnFamilyInformation.setSSTableCount(cfsProxy.getLiveSSTableCount());
        columnFamilyInformation.setLiveDiskSpaceUsed(cfsProxy.getLiveDiskSpaceUsed());
        columnFamilyInformation.setTotalDiskSpaceUsed(cfsProxy.getTotalDiskSpaceUsed());
        columnFamilyInformation.setNumberOfKeys(cfsProxy.estimateKeys());
        columnFamilyInformation.setMemtableSwitchCount(cfsProxy.getMemtableSwitchCount());
        columnFamilyInformation.setReadCount(cfsProxy.getReadCount());
        columnFamilyInformation.setReadLatency(cfsProxy.getRecentReadLatencyMicros());
        columnFamilyInformation.setWriteCount(cfsProxy.getWriteCount());
        columnFamilyInformation.setWriteLatency(cfsProxy.getRecentWriteLatencyMicros());
        columnFamilyInformation.setPendingTasks(cfsProxy.getPendingTasks());
        columnFamilyInformation.setBloomFilterFalsePostives(cfsProxy.getBloomFilterFalsePositives());
        columnFamilyInformation.setBloomFilterFalseRatio(cfsProxy.getRecentBloomFilterFalseRatio());
        columnFamilyInformation.setBloomFilterSpaceUsed(cfsProxy.getBloomFilterDiskSpaceUsed());
        columnFamilyInformation.setCompactedRowMinimumSize(cfsProxy.getMinRowSize());
        columnFamilyInformation.setCompactedRowMeanSize(cfsProxy.getMaxRowSize());
        columnFamilyInformation.setCompactedRowMeanSize(cfsProxy.getMeanRowSize());
        return columnFamilyInformation;
    }

    public ThreadPoolInfo getThreadPoolStats() throws ClusterDataAdminException {
        clusterThreadPoolProxyMBeanService = ClusterMBeanProxy.getClusterThreadPoolProxyMBeanService();
        clusterMessagingServiceMBeanService = ClusterMBeanProxy.getClusterMessagingServiceMBeanService();
        List<ThreadPoolProperties> threadPoolProperties = new ArrayList<ThreadPoolProperties>();
        List<ThreadPoolDroppedProperties> threadPoolDroppedProperties = new ArrayList<ThreadPoolDroppedProperties>();
        ThreadPoolInfo threadPoolInfo = new ThreadPoolInfo();
        Iterator<Map.Entry<String, JMXEnabledThreadPoolExecutorMBean>> threads = clusterThreadPoolProxyMBeanService.getThreadPoolMBeanProxies();
        while (threads.hasNext()) {
            ThreadPoolProperties threadPoolProperty = new ThreadPoolProperties();
            Map.Entry<String, JMXEnabledThreadPoolExecutorMBean> thread = threads.next();
            String poolName = thread.getKey();
            JMXEnabledThreadPoolExecutorMBean threadPoolProxy = thread.getValue();
            threadPoolProperty.setThreadPoolPropertyName(poolName);
            threadPoolProperty.setActive(threadPoolProxy.getActiveCount());
            threadPoolProperty.setPending(threadPoolProxy.getPendingTasks());
            threadPoolProperty.setCompleted(threadPoolProxy.getCompletedTasks());
            threadPoolProperty.setBlocked(threadPoolProxy.getCurrentlyBlockedTasks());
            threadPoolProperty.setAllTimeBlocked(threadPoolProxy.getTotalBlockedTasks());
            threadPoolProperties.add(threadPoolProperty);
        }

        for (Map.Entry<String, Integer> entry : clusterMessagingServiceMBeanService.getDroppedMessages().entrySet()) {
            ThreadPoolDroppedProperties threadPoolDroppedProperty = new ThreadPoolDroppedProperties();
            threadPoolDroppedProperty.setPropertyName(entry.getKey());
            threadPoolDroppedProperty.setDroppedCount(entry.getValue());
            threadPoolDroppedProperties.add(threadPoolDroppedProperty);
        }
        threadPoolInfo.setThreadPoolProperties(threadPoolProperties.toArray(new ThreadPoolProperties[threadPoolProperties.size()]));
        threadPoolInfo.setThreadPoolDroppedProperties(threadPoolDroppedProperties.toArray(new ThreadPoolDroppedProperties[threadPoolDroppedProperties.size()]));
        return threadPoolInfo;
    }

    public CompactionStats getCompactionStats() throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        clusterCompactionManagerMBeanService = ClusterMBeanProxy.getClusterCompactionManagerMBeanService();
        CompactionProperties[] compactionProperties;
        CompactionStats compactionStats = new CompactionStats();
        int compactionThroughput = clusterStorageMBeanService.getCompactionThroughput();
        compactionStats.setPendingTasks(clusterCompactionManagerMBeanService.getPendingTasks());
        long remainingBytes = 0;
        if (clusterCompactionManagerMBeanService.getCompactions().size() > 0) {
            int count = 0;
            compactionProperties = new CompactionProperties[clusterCompactionManagerMBeanService.getCompactions().size()];
            for (Map<String, String> c : clusterCompactionManagerMBeanService.getCompactions()) {
                CompactionProperties compactionProperty = new CompactionProperties();
                String percentComplete = new Long(c.get("totalBytes")) == 0
                        ? "n/a"
                        : new DecimalFormat("0.00").format((double) new Long(c.get("bytesComplete")) / new Long(c.get("totalBytes")) * 100) + "%";
                compactionProperty.setCompactionType(c.get("taskType"));
                compactionProperty.setKeyspace(c.get("keyspace"));
                compactionProperty.setColumFamily(c.get("columnfamily"));
                compactionProperty.setBytesCompacted(c.get("bytesComplete"));
                compactionProperty.setBytesTotal(c.get("totalBytes"));
                compactionProperty.setProgress(percentComplete);
                compactionProperties[count] = compactionProperty;
                count++;
                if (c.get("taskType").equals(OperationType.COMPACTION.toString()))
                    remainingBytes += (new Long(c.get("totalBytes")) - new Long(c.get("bytesComplete")));
            }
        } else {
            compactionProperties = null;
        }

        long remainingTimeInSecs = compactionThroughput == 0 || remainingBytes == 0
                ? -1
                : (remainingBytes) / (1024L * 1024L * compactionThroughput);
        String remainingTime = remainingTimeInSecs < 0
                ? "n/a"
                : String.format("%dh%02dm%02ds", remainingTimeInSecs / 3600, (remainingTimeInSecs % 3600) / 60, (remainingTimeInSecs % 60));
        compactionStats.setActiveCompactionRemainingTime(remainingTime);
        compactionStats.setCompactionProperties(compactionProperties);
        return compactionStats;
    }

    public ClusterNetstat getNetworkStats(String hostName) throws ClusterDataAdminException {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            throw new ClusterDataAdminException("Unknown host", e, log);
        }
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        clusterStreamProxyMBeanService = ClusterMBeanProxy.getClusterStreamProxyMBeanService();
        clusterMessagingServiceMBeanService = ClusterMBeanProxy.getClusterMessagingServiceMBeanService();
        ClusterNetstat clusterNetstat = new ClusterNetstat();
        clusterNetstat.setOperationMode(clusterStorageMBeanService.getOperationMode());

        NetstatStreamingProperties[] netstatReceivingStreamingProperties;
        NetstatStreamingProperties[] netstatResponseStreamingProperties;

        Set<InetAddress> hosts = addr == null ? clusterStreamProxyMBeanService.getStreamDestinations() : new HashSet<InetAddress>() {{
            add(addr);
        }};
        if (hosts.size() == 0)
            netstatReceivingStreamingProperties = null;
        else
            netstatReceivingStreamingProperties = new NetstatStreamingProperties[hosts.size()];
        int count = 0;
        for (InetAddress host : hosts) {
            NetstatStreamingProperties netstatStreamingProperty = new NetstatStreamingProperties();
            netstatStreamingProperty.setStreamType("Receiving");
            netstatStreamingProperty.setHost(host.getHostAddress());
            List<String> files = clusterStreamProxyMBeanService.getFilesDestinedFor(host);
            String[] fileList = new String[files.size()];
            if (files.size() > 0) {
                int fileCount = 0;
                for (String file : files) {
                    fileList[fileCount] = file;
                    fileCount++;
                }
                netstatStreamingProperty.setFileName(fileList);
            } else {
                netstatStreamingProperty.setFileName(null);
            }
            assert netstatReceivingStreamingProperties != null;
            netstatReceivingStreamingProperties[count] = netstatStreamingProperty;
            count++;
        }
        clusterNetstat.setNetstatReceivingStreamingProperties(netstatReceivingStreamingProperties);

        count = 0;

        hosts = addr == null ? clusterStreamProxyMBeanService.getStreamSources() : new HashSet<InetAddress>() {{
            add(addr);
        }};
        if (hosts.size() == 0)
            netstatResponseStreamingProperties = null;
        else
            netstatResponseStreamingProperties = new NetstatStreamingProperties[hosts.size()];

        for (InetAddress host : hosts) {
            NetstatStreamingProperties netstatStreamingProperty = new NetstatStreamingProperties();
            netstatStreamingProperty.setStreamType("Response");
            netstatStreamingProperty.setHost(host.getHostAddress());
            List<String> files = clusterStreamProxyMBeanService.getIncomingFiles(host);
            String[] fileList = new String[files.size()];
            if (files.size() > 0) {
                int fileCount = 0;
                for (String file : files) {
                    fileList[fileCount] = file;
                    fileCount++;
                }
                netstatStreamingProperty.setFileName(fileList);
            } else {
                netstatStreamingProperty.setFileName(null);
            }

            assert netstatResponseStreamingProperties != null;
            netstatResponseStreamingProperties[count] = netstatStreamingProperty;
            count++;
        }
        clusterNetstat.setNetstatResponseStreamingProperties(netstatResponseStreamingProperties);

        NetstatProperties[] netstatProperties = new NetstatProperties[2];
        int pending;
        int completed;
        NetstatProperties netstatCommandProperty = new NetstatProperties();
        netstatCommandProperty.setPoolName("Command");
        pending = 0;
        for (int n : clusterMessagingServiceMBeanService.getCommandPendingTasks().values())
            pending += n;
        completed = 0;
        for (long n : clusterMessagingServiceMBeanService.getCommandCompletedTasks().values())
            completed += n;

        netstatCommandProperty.setPending(pending);
        netstatCommandProperty.setCompleted(completed);
        netstatCommandProperty.setActive("n/a");
        NetstatProperties netstatResponseProperty = new NetstatProperties();
        netstatResponseProperty.setPoolName("Response");
        pending = 0;
        for (int n : clusterMessagingServiceMBeanService.getResponsePendingTasks().values())
            pending += n;
        completed = 0;
        for (long n : clusterMessagingServiceMBeanService.getResponseCompletedTasks().values())
            completed += n;
        netstatResponseProperty.setPending(pending);
        netstatResponseProperty.setCompleted(completed);
        netstatResponseProperty.setActive("n/a");

        netstatProperties[0] = netstatCommandProperty;
        netstatProperties[1] = netstatResponseProperty;

        clusterNetstat.setNetstatProperties(netstatProperties);
        return clusterNetstat;
    }

    public ColumnFamilyHistograms[] getCfHistograms(String keySpace, String columnFamily)
            throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService(keySpace, columnFamily);

        // default is 90 offsets
        long[] offsets = new EstimatedHistogram().getBucketOffsets();

        long[] rrlh = clusterColumnFamilyMBeanService.getRecentReadLatencyHistogramMicros();
        long[] rwlh = clusterColumnFamilyMBeanService.getRecentWriteLatencyHistogramMicros();
        long[] sprh = clusterColumnFamilyMBeanService.getRecentSSTablesPerReadHistogram();
        long[] ersh = clusterColumnFamilyMBeanService.getEstimatedRowSizeHistogram();
        long[] ecch = clusterColumnFamilyMBeanService.getEstimatedColumnCountHistogram();
        ColumnFamilyHistograms[] columnFamilyHistograms = new ColumnFamilyHistograms[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            ColumnFamilyHistograms columnFamilyHistogram = new ColumnFamilyHistograms();
            columnFamilyHistogram.setOffset(offsets[i]);
            if (i < sprh.length)
                columnFamilyHistogram.setSSTables(sprh[i]);
            else
                columnFamilyHistogram.setSSTables(-1);

            if (i < rwlh.length)
                columnFamilyHistogram.setWriteLatency(rwlh[i]);
            else
                columnFamilyHistogram.setWriteLatency(-1);

            if (i < rrlh.length)
                columnFamilyHistogram.setReadLatency(rrlh[i]);
            else
                columnFamilyHistogram.setReadLatency(-1);

            if (i < ersh.length)
                columnFamilyHistogram.setRowSize(ersh[i]);
            else
                columnFamilyHistogram.setRowSize(-1);

            if (i < ecch.length)
                columnFamilyHistogram.setColumnCount(ecch[i]);
            else
                columnFamilyHistogram.setColumnCount(-1);
            columnFamilyHistograms[i] = columnFamilyHistogram;
        }
        return columnFamilyHistograms;
    }

    public String[] getEndPoints(String keySpace, String cf, String key)
            throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        List<InetAddress> endpoints = clusterStorageMBeanService.getEndpoints(keySpace, cf, key);
        String[] hostAddresses = new String[endpoints.size()];
        for (int i = 0; i < endpoints.size(); i++) {
            hostAddresses[i] = endpoints.get(i).getHostAddress();
        }
        return hostAddresses;
    }

    public String[] getSSTables(String keyspace, String cf, String key)
            throws ClusterDataAdminException {
        clusterColumnFamilyMBeanService = ClusterMBeanProxy.getClusterColumnFamilyMBeanService(keyspace, cf);
        List<String> sstables = clusterColumnFamilyMBeanService.getSSTables(key);
        return sstables.toArray(new String[sstables.size()]);
    }

    public String[] getRangeKeySample() throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        List<String> tokenStrings = clusterStorageMBeanService.sampleKeyRange();
        return tokenStrings.toArray(new String[tokenStrings.size()]);
    }

    public DescribeRingProperties getDescribeRing(String keyspaceName) throws ClusterDataAdminException {
        clusterStorageMBeanService = ClusterMBeanProxy.getClusterStorageMBeanService();
        DescribeRingProperties describeRingProperties = new DescribeRingProperties();
        describeRingProperties.setSchemaVersion(clusterStorageMBeanService.getSchemaVersion());
        try {
            List<String> properties = clusterStorageMBeanService.describeRing(keyspaceName);
            describeRingProperties.setTokenRange(properties.toArray(new String[properties.size()]));
        }
        catch (IOException e) {
            throw new ClusterDataAdminException("Error while query describe ring data", e, log);
        }
        return describeRingProperties;
    }

    public String[] getClusterBasicInfo() throws ClusterDataAdminException {
        String[] clusterBasicInfo = new String[3];
        String hostAddress = ClusterMBeanProxy.getClusterStorageMBeanService().getEndpoint();
        clusterBasicInfo[0] = hostAddress;
        try {
            clusterBasicInfo[1] = InetAddress.getByName(hostAddress).getHostName();
        } catch (UnknownHostException e) {
            clusterBasicInfo[1] = null;
        }
        clusterBasicInfo[2] = ClusterMBeanProxy.getClusterStorageMBeanService().getToken();
        return clusterBasicInfo;
    }
}
