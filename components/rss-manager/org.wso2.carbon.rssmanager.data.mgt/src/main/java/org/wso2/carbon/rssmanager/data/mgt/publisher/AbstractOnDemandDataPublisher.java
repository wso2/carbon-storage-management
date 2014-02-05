package org.wso2.carbon.rssmanager.data.mgt.publisher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;

public abstract class AbstractOnDemandDataPublisher implements DataPublishable {

	private static Log log = LogFactory.getLog(AbstractOnDemandDataPublisher.class);

	public void execute(final PublishEventData eventData) {
		try {
			DataPublisher dataPublisher = getDataPublisher();

			String streamId = getStreamId(dataPublisher);

			if (StringUtils.isNotEmpty(streamId) && eventData != null) {
				publishStats(dataPublisher, streamId, eventData);
			} else {
				log.error(" Unexpected Error : Stream Id is null or empty ");
			}

		} catch (Exception ex) {
			log.error(ex);
		}
	}
}
