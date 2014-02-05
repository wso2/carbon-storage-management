package org.wso2.carbon.rssmanager.data.mgt.publisher;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.exception.RSSDataMgtException;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.PublisherServiceLocator;

public interface DataPublishable {

	static final PublisherServiceLocator pubServiceLoc = new PublisherServiceLocator();
	
	DataPublisher getDataPublisher() throws RSSDataMgtException;

	String findStreamId(final DataPublisher dataPublisher) throws RSSDataMgtException;

	String getStreamId(final DataPublisher dataPublisher) throws RSSDataMgtException;

	void publishStats(final DataPublisher dataPublisher, final String streamId,
	                  final PublishEventData data) throws RSSDataMgtException;

	void deleteStreamDefinition(final DataPublisher dataPublisher) throws RSSDataMgtException;
}
