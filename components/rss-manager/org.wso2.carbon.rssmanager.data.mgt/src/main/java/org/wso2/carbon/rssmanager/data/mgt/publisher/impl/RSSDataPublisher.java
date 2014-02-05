package org.wso2.carbon.rssmanager.data.mgt.publisher.impl;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.AbstractOnDemandDataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.DataPublishable;
import org.wso2.carbon.rssmanager.data.mgt.publisher.exception.RSSDataMgtException;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;

public class RSSDataPublisher extends AbstractOnDemandDataPublisher {

	private DataPublishable commonPublisher = new CommonDataPublisher();

	@Override
    public DataPublisher getDataPublisher() throws RSSDataMgtException {
		return commonPublisher.getDataPublisher();
	}

	@Override
    public String findStreamId(DataPublisher dataPublisher) throws RSSDataMgtException {
		return commonPublisher.findStreamId(dataPublisher);
	}

	@Override
    public String getStreamId(DataPublisher dataPublisher) throws RSSDataMgtException {
		return commonPublisher.getStreamId(dataPublisher);
	}

	@Override
    public void publishStats(DataPublisher dataPublisher, String streamId, PublishEventData data)
			throws RSSDataMgtException {
		commonPublisher.publishStats(dataPublisher, streamId, data);
	}

	@Override
    public void deleteStreamDefinition(DataPublisher dataPublisher) throws RSSDataMgtException {
		commonPublisher.deleteStreamDefinition(dataPublisher);
	}

}
