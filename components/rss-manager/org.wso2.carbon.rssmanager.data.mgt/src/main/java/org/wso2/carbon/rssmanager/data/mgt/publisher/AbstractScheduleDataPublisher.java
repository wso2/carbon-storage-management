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
