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
