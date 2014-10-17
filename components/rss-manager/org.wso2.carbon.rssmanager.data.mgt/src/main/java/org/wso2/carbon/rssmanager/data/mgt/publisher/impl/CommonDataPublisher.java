/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.data.mgt.publisher.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.publisher.DataPublishable;
import org.wso2.carbon.rssmanager.data.mgt.publisher.exception.RSSDataMgtException;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.StreamsDefinitions;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfig;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfigManager;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.PublisherManager;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.PublisherServiceLocator;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;

public class CommonDataPublisher implements DataPublishable {

	// Per each definition there is a lock, to make each definition creation
	// atomic
	private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private static final Lock readLock = readWriteLock.readLock();
	private static final Lock writeLock = readWriteLock.writeLock();

	// defaults
	private String streamName = StreamsDefinitions.RSS_STATS_TABLE;
	private String streamVersion = StreamsDefinitions.VERSION;
	private String streamDefinition = StreamsDefinitions.RSS_STATS_TABLE_STREAM_DEF;
	private String configFileName = RSSPublisherConstants.CONFIGURATION_FILE_NAME;

	public CommonDataPublisher() {
	}

	public CommonDataPublisher(String streamName, String streamVersion, String streamDefinition,
	                           String configFileName) {
		super();
		if (StringUtils.isNotEmpty(streamName)) {
			this.streamName = streamName;
		}

		if (StringUtils.isNotEmpty(streamVersion)) {
			this.streamVersion = streamVersion;
		}

		if (StringUtils.isNotEmpty(streamDefinition)) {
			this.streamDefinition = streamDefinition;
		}

		if (StringUtils.isNotEmpty(configFileName)) {
			this.configFileName = configFileName;
		}

	}

	public DataPublisher getDataPublisher() throws RSSDataMgtException {

		DataPublisher publisher = null;
		// Separate ClusterMonitorConfig for each config file
		ClusterMonitorConfig config = ClusterMonitorConfigManager.getClusterMonitorConfig(configFileName);
		try {
			if (config != null) {

				PublisherManager.Key key = new PublisherManager.Key(streamName,
						streamVersion);
				// each stream definition has separate publisher
				// manager/publisher
				if (PublisherServiceLocator.hasPublisherManager(key)) {
					PublisherManager pubManager = PublisherServiceLocator.getPublisherManager(key);
					publisher = pubManager.getPublisher();
				} else {

					publisher = new DataPublisher(config.getSecureUrl(),config.getReceiverUrl(), config.getUsername(),
							config.getPassword());

					PublisherManager pubManager = new PublisherManager(publisher, key);
					PublisherManager previousValue = PublisherServiceLocator.addPublisherManager(pubManager);
					// ensure use same manager/publisher
					if (previousValue != null) {
						publisher = previousValue.getPublisher();
					}

				}

			}

		} catch (Exception e) {
			throw new RSSDataMgtException(" Error while getting data publisher", e);
		}

		return publisher;
	}

	public String findStreamId(DataPublisher dataPublisher) throws RSSDataMgtException {
		try {
			return dataPublisher.findStreamId(streamName, streamVersion);
		} catch (AgentException e) {
			throw new RSSDataMgtException(" Error while finding Stream Id", e);
		}
	}

	public String getStreamId(final DataPublisher dataPublisher) throws RSSDataMgtException {

		String streamId = null;
		
		try{
			readLock.lock();
			boolean streamDefExist = false;
			
			try {
				streamId = findStreamId(dataPublisher);
				if (StringUtils.isNotEmpty(streamId)) {
					streamDefExist = true;
				}
			} finally {
				readLock.unlock();
			}

			if (!streamDefExist) {
				try {

					writeLock.lock();
					// double check - this happens only once for entire server life
					// cycle
					streamId = findStreamId(dataPublisher);
					if (StringUtils.isEmpty(streamId)) {
						streamId = dataPublisher.defineStream(streamDefinition);
					}

				} finally {

					writeLock.unlock();

				}
			}
		} catch (Exception e) {
			throw new RSSDataMgtException(" Error while getting Stream Id", e);
		}		

		return streamId;
	}

	public void publishStats(DataPublisher dataPublisher, String streamId,
	                         final PublishEventData data) throws RSSDataMgtException {

		Event event = new Event(streamId, System.currentTimeMillis(), null, null, null);
		if (data != null) {
			event.setCorrelationData(data.getCorrelationDataArray());
			event.setMetaData(data.getMetaDataArray());
			event.setPayloadData(data.getPayloadDataArray());
		}

		try {
			dataPublisher.publish(event);
		} catch (AgentException e) {
			throw new RSSDataMgtException(" Error while publishing events", e);
		}
	}

	public void deleteStreamDefinition(DataPublisher dataPublisher) throws RSSDataMgtException {

		writeLock.lock();
		try {
			dataPublisher.deleteStream(streamName, streamVersion);
		} catch (AgentException e) {
			throw new RSSDataMgtException(" Error while delete Stream definition", e);
		} finally {
			writeLock.unlock();
		}

	}
	
	public static PublishEventData populateEventData(final UsageStatistic stats, final long time){
		Object [] meta = new Object[] { RSSPublisherConstants.META_DATA_PREFIX };
		PublishEventData eventData = new PublishEventData(meta, null, createStatsArray(stats, ""+time));
		return eventData;
	}
	
	private static Object [] createStatsArray(final UsageStatistic stats, String timeStamp){
		Object [] data = new Object[]{stats.getHostAddress(),stats.getHostName(),stats.getTenantId(),stats.getDiskUsage(),stats.getDatabaseName(),timeStamp, stats.getAbortedClients(),stats.getAbortedConnections(),stats.getBytesReceived(),stats.getBytesSent(),stats.getConnections(),stats.getCreatedTmpDiskTables()
		                              ,stats.getCreatedTmpFiles(),stats.getCreatedTmpTables(),stats.getOpenedTables(),stats.getOpenFiles(),stats.getOpenStreams(),stats.getOpenTables()
		                              ,stats.getQuestions(),stats.getReadCount(),stats.getReadLatency(),stats.getTableLocksImmediate(),stats.getTableLocksWaited(), stats.getThreadsCached(),stats.getThreadsConnected()
		                              ,stats.getThreadsCreated(),stats.getThreadsRunning(),stats.getUptime(),stats.getWriteCount(),stats.getWriteLatency()};
		
		
		return data;
	}

}
