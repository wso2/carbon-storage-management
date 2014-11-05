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

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.AbstractScheduleDataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.DataPublishable;
import org.wso2.carbon.rssmanager.data.mgt.publisher.exception.RSSDataMgtException;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.service.StorageUsageManagerService;

public class RSSScheduleDataPublisher extends AbstractScheduleDataPublisher {

	private DataPublishable commonPublisher = new CommonDataPublisher();
	private StorageUsageManagerService storageUsageManagerService =
	                                                                new StorageUsageManagerService();

	public DataPublisher getDataPublisher() throws RSSDataMgtException {
		return commonPublisher.getDataPublisher();
	}

	public String findStreamId(DataPublisher dataPublisher) throws RSSDataMgtException {
		return commonPublisher.findStreamId(dataPublisher);
	}

	public String getStreamId(DataPublisher dataPublisher) throws RSSDataMgtException {
		return commonPublisher.getStreamId(dataPublisher);
	}

	public void publishStats(DataPublisher dataPublisher, String streamId, PublishEventData data)
	                                                                                             throws RSSDataMgtException {
		commonPublisher.publishStats(dataPublisher, streamId, data);
	}

	public void deleteStreamDefinition(DataPublisher dataPublisher) throws RSSDataMgtException {
		commonPublisher.deleteStreamDefinition(dataPublisher);
	}

	@Override
	protected List<PublishEventData> getStatsInfo() throws RSSDataMgtException {
		long time = System.currentTimeMillis();

		List<PublishEventData> events = new ArrayList<PublishEventData>();
		try{
			List<UsageStatistic> statsList = storageUsageManagerService.getGlobalStatisticsForAllDBs();
			for (UsageStatistic stats : statsList) {
				if(stats == null){
					continue;
				}
				events.add(CommonDataPublisher.populateEventData(stats, time));
			}
		}catch(Exception ex){
			throw new RSSDataMgtException(" Error while getting Stats Info ", ex);
		}		

		return events;
	}

}
