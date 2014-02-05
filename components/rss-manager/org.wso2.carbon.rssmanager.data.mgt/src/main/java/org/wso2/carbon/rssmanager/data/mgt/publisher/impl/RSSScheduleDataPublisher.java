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
