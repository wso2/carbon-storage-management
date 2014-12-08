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
