package org.wso2.carbon.rssmanager.data.mgt.publisher;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;

public abstract class AbstractScheduleDataPublisher extends AbstractTask implements DataPublishable {

	private static Log log = LogFactory.getLog(AbstractScheduleDataPublisher.class);

	protected abstract List<PublishEventData> getStatsInfo() throws Exception;

	public void execute() {
		try {
			DataPublisher dataPublisher = getDataPublisher();
			String streamId = getStreamId(dataPublisher);
			List<PublishEventData> eventData = getStatsInfo();
			
			boolean proceed = true;

			if (StringUtils.isEmpty(streamId)) {
				log.error(" Unexpected Error : Stream Id is null or empty ");
				proceed = false;
			}
			
			if (eventData.isEmpty()) {
				log.error(" Unexpected Error : No event Data to publish ");
				proceed = false;
			}
			
			if (proceed) {
				for (PublishEventData event : eventData) {
					publishStats(dataPublisher, streamId, event);
				}
				
			} 
		} catch (Exception ex) {
			log.error(ex);
		}

	}

}
