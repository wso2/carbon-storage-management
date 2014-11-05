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
package org.wso2.carbon.cassandra.cluster.mgt.publisher;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.mgt.Util.ClusterMonitorConfig;
import org.wso2.carbon.cassandra.cluster.mgt.Util.StreamsDefinitions;
import org.wso2.carbon.cassandra.cluster.mgt.data.ColumnFamilyInformation;
import org.wso2.carbon.cassandra.cluster.mgt.data.KeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.mgt.data.NodeInformation;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.query.ClusterMBeanServiceHandler;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.ntask.core.AbstractTask;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class ClusterDataPublisher extends AbstractTask {
    private static Log log = LogFactory.getLog(ClusterDataPublisher.class);

    private static DataPublisher dataPublisher;

    @Override
    public void execute() {
        String columnFamilyStatsStreamId = null;
        String nodeStatsStreamId = null;
        String keyspaceStatsStreamId = null;
        DataPublisher dataPublisher = null;
	    try {
		    dataPublisher = getDataPublisher(); //Get data publisher
		    //Define stream if not exists for the publish column family statistics
		    try {
			    columnFamilyStatsStreamId = dataPublisher.findStreamId(StreamsDefinitions.COLUMN_FAMILY_STATS, StreamsDefinitions.VERSION);
			    if (StringUtils.isEmpty(columnFamilyStatsStreamId)) {
				    columnFamilyStatsStreamId = dataPublisher.defineStream(StreamsDefinitions.COLUMN_FAMILY_STATS_STREAM_DEF);
			    }
		    } catch (AgentException e) {
			    log.error("Error while getting stream id of" + StreamsDefinitions.COLUMN_FAMILY_STATS + "version+" + StreamsDefinitions.VERSION,
			              e);
		    } catch (Exception e) {
			    log.error("Error while defining the stream" + StreamsDefinitions.COLUMN_FAMILY_STATS, e);
		    }

		    //Define stream if not exists for the publish node statistics
		    try {
			    nodeStatsStreamId = dataPublisher.findStreamId(StreamsDefinitions.NODE_STATS, StreamsDefinitions.VERSION);
			    if (StringUtils.isEmpty(nodeStatsStreamId)) {
				    nodeStatsStreamId = dataPublisher.defineStream(StreamsDefinitions.NODE_STATS_STREAM_DEF);
			    }
		    } catch (AgentException e) {
			    log.error("Error while getting stream id of" + StreamsDefinitions.NODE_STATS + "version+" + StreamsDefinitions.VERSION,
			              e);
		    } catch (Exception e) {
			    log.error("Error while defining the stream" + StreamsDefinitions.NODE_STATS_STREAM_DEF, e);
		    }

		    //Define stream if not exists for the publish keyspace statistics
		    try {
			    keyspaceStatsStreamId = dataPublisher.findStreamId(StreamsDefinitions.KS_STATS, StreamsDefinitions.VERSION);
			    if (StringUtils.isEmpty(nodeStatsStreamId)) {
				    keyspaceStatsStreamId = dataPublisher.defineStream(StreamsDefinitions.KS_STATS_STREAM_DEF);
			    }
		    } catch (AgentException e) {
			    log.error("Error while getting stream id of" + StreamsDefinitions.KS_STATS + "version+" + StreamsDefinitions.VERSION,
			              e);
		    } catch (Exception e) {
			    log.error("Error while defining the stream" + StreamsDefinitions.KS_STATS_STREAM_DEF, e);
		    }
	    } catch (Exception e) {
		    log.error("Error while getting data publisher", e);
	    }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        if (columnFamilyStatsStreamId != null && dataPublisher != null) {
            try {
                publishCFSTats(dataPublisher, columnFamilyStatsStreamId, timeStamp);
            } catch (ClusterDataAdminException e) {
                if (log.isDebugEnabled()) {
                    log.error("Error while publishing the column family stats", e);
                }
            }
        }

        if (nodeStatsStreamId != null && dataPublisher != null) {
            try {
                publishNodeInfoStats(dataPublisher, nodeStatsStreamId, timeStamp);
            } catch (ClusterDataAdminException e) {
                if (log.isDebugEnabled()) {
                    log.error("Error while publishing the data node info stats", e);
                }
            }
        }

        if (keyspaceStatsStreamId != null && dataPublisher != null) {
            try {
                publishKeyspaceStats(dataPublisher, keyspaceStatsStreamId, timeStamp);
            } catch (ClusterDataAdminException e) {
                if (log.isDebugEnabled()) {
                    log.error("Error while publishing keyspace stats", e);
                }
            }
        }
    }

    private static DataPublisher getDataPublisher()
            throws AgentException, MalformedURLException,
            AuthenticationException, TransportException {
        if (null == dataPublisher) {
            dataPublisher = new DataPublisher(ClusterMonitorConfig.getSecureUrl(),
                    ClusterMonitorConfig.getReceiverUrl(),
                    ClusterMonitorConfig.getUsername(), ClusterMonitorConfig.getPassword());
        }
        return dataPublisher;
    }

    private void publishCFSTats(DataPublisher dataPublisher, String streamId, String timeStamp)
            throws ClusterDataAdminException {
        ArrayList<String> cfstats;
        ClusterMBeanServiceHandler clusterMBeanServiceHandler = new ClusterMBeanServiceHandler();
        String[] nodeBasicInfo = clusterMBeanServiceHandler.getClusterBasicInfo();
        for (KeyspaceInfo keyspaceInfo : clusterMBeanServiceHandler.getColumnFamilyStats()) {
            for (ColumnFamilyInformation columnFamilyInformation : keyspaceInfo.getColumnFamilyInformations()) {
                cfstats = new ArrayList<String>();
                cfstats.add(timeStamp);
                cfstats.add(ClusterMonitorConfig.getNodeId());
                cfstats.add(nodeBasicInfo[0]);
                cfstats.add(nodeBasicInfo[1]);
                cfstats.add(nodeBasicInfo[2]);
                cfstats.add(keyspaceInfo.getKeyspaceName());
                cfstats.add(columnFamilyInformation.getColumnFamilyName());
                cfstats.add(String.valueOf(columnFamilyInformation.getSSTableCount()));
                cfstats.add(String.valueOf(columnFamilyInformation.getLiveDiskSpaceUsed()));
                cfstats.add(String.valueOf(columnFamilyInformation.getTotalDiskSpaceUsed()));
                cfstats.add(String.valueOf(columnFamilyInformation.getMemtableColumnsCount()));
                cfstats.add(String.valueOf(columnFamilyInformation.getMemtableDataSize()));
                cfstats.add(String.valueOf(columnFamilyInformation.getMemtableSwitchCount()));
                cfstats.add(String.valueOf(columnFamilyInformation.getReadCount()));
                cfstats.add(String.valueOf(columnFamilyInformation.getReadLatency()));
                cfstats.add(String.valueOf(columnFamilyInformation.getWriteCount()));
                cfstats.add(String.valueOf(columnFamilyInformation.getWriteLatency()));
                cfstats.add(String.valueOf(columnFamilyInformation.getPendingTasks()));
                cfstats.add(String.valueOf(columnFamilyInformation.getNumberOfKeys()));
                cfstats.add(String.valueOf(columnFamilyInformation.getBloomFilterFalsePostives()));
                cfstats.add(String.valueOf(columnFamilyInformation.getBloomFilterFalseRatio()));
                cfstats.add(String.valueOf(columnFamilyInformation.getBloomFilterSpaceUsed()));
                cfstats.add(String.valueOf(columnFamilyInformation.getCompactedRowMinimumSize()));
                cfstats.add(String.valueOf(columnFamilyInformation.getCompactedRowMaximumSize()));
                cfstats.add(String.valueOf(columnFamilyInformation.getCompactedRowMeanSize()));
                Event cfstatsEvent = new Event(streamId, Long.valueOf(timeStamp), new Object[]{"external"}, null, cfstats.toArray(new String[cfstats.size()]));
                try {
                    dataPublisher.publish(cfstatsEvent);
                } catch (AgentException e) {
                    if (log.isDebugEnabled()) {
                        log.error("Error while publishing the data column family stats", e);
                    }
                }
            }
        }

    }

    private void publishNodeInfoStats(DataPublisher dataPublisher, String streamId, String timeStamp)
            throws ClusterDataAdminException {
        ArrayList<String> nodeInfo;
        ClusterMBeanServiceHandler clusterMBeanServiceHandler = new ClusterMBeanServiceHandler();
        String[] nodeBasicInfo = clusterMBeanServiceHandler.getClusterBasicInfo();
        nodeInfo = new ArrayList<String>();
        nodeInfo.add(timeStamp);
        nodeInfo.add(ClusterMonitorConfig.getNodeId());
        nodeInfo.add(nodeBasicInfo[0]);
        nodeInfo.add(nodeBasicInfo[1]);
        nodeInfo.add(nodeBasicInfo[2]);
        NodeInformation nodeInformation = clusterMBeanServiceHandler.getNodeInfo();
        nodeInfo.add(nodeInformation.getLoad().split(" ")[1]);
        nodeInfo.add(nodeInformation.getLoad().split(" ")[0]);

        nodeInfo.add(String.valueOf(nodeInformation.getUptime()));
        nodeInfo.add(String.valueOf(nodeInformation.getExceptions()));
        nodeInfo.add(String.valueOf(nodeInformation.getHeapMemory().getUseMemory()));
        nodeInfo.add(String.valueOf(nodeInformation.getHeapMemory().getMaxMemory()));
        nodeInfo.add(nodeInformation.getDataCenter());

        nodeInfo.add(nodeInformation.getRack());
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheCapacity()));
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheSize()));
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheRequests()));
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheHits()));
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheSavePeriodInSeconds()));
        nodeInfo.add(String.valueOf(nodeInformation.getKeyCacheProperties().getCacheRecentHitRate()));

        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheCapacity()));
        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheSize()));
        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheRequests()));
        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheHits()));
        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheSavePeriodInSeconds()));
        nodeInfo.add(String.valueOf(nodeInformation.getRowCacheProperties().getCacheRecentHitRate()));

        Event nodeInfoStats = new Event(streamId, Long.valueOf(timeStamp), new Object[]{"external"}, null, nodeInfo.toArray(new String[nodeInfo.size()]));
        try {
            dataPublisher.publish(nodeInfoStats);
        } catch (AgentException e) {
            if (log.isDebugEnabled()) {
                log.error("Error while publishing the data column family stats", e);
            }
        }
    }

    private void publishKeyspaceStats(DataPublisher dataPublisher, String streamId, String timeStamp)
            throws ClusterDataAdminException {
        ArrayList<String> ksStats;
        ClusterMBeanServiceHandler clusterMBeanServiceHandler = new ClusterMBeanServiceHandler();
        for (KeyspaceInfo keyspaceInfo : clusterMBeanServiceHandler.getColumnFamilyStats()) {
            ksStats = new ArrayList<String>();
            ksStats.add(timeStamp);
            ksStats.add(ClusterMonitorConfig.getNodeId());
            ksStats.add(keyspaceInfo.getKeyspaceName());
            ksStats.add(String.valueOf(keyspaceInfo.getTableReadCount()));
            ksStats.add(String.valueOf(keyspaceInfo.getTableReadLatency()));
            ksStats.add(String.valueOf(keyspaceInfo.getTableReadCount()*keyspaceInfo.getTableReadLatency()));
            ksStats.add(String.valueOf(keyspaceInfo.getTableWriteCount()));
            ksStats.add(String.valueOf(keyspaceInfo.getTableWriteLatency()));
            ksStats.add(String.valueOf(keyspaceInfo.getTableWriteCount()*keyspaceInfo.getTableWriteLatency()));
            ksStats.add(String.valueOf(keyspaceInfo.getTablePendingTasks()));
            Event ksStatsEvent = new Event(streamId, Long.valueOf(timeStamp), new Object[]{"external"}, null, ksStats.toArray(new String[ksStats.size()]));
            try {
                dataPublisher.publish(ksStatsEvent);
            } catch (AgentException e) {
                if (log.isDebugEnabled()) {
                    log.error("Error while publishing keyspace stats", e);
                }
            }

        }
    }

}
