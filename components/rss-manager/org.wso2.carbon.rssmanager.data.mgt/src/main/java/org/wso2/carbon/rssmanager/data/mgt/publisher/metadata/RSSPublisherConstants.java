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
package org.wso2.carbon.rssmanager.data.mgt.publisher.metadata;

import java.io.File;

import org.wso2.carbon.utils.CarbonUtils;

public class RSSPublisherConstants {

	public static final String RSS_MONITOR = "rssMonitor";
	public static final String RSS_MONITOR_SERVICE_NAME = "rssMonitorServiceName";
	public static final String RSS_STATS = "rssStats";
	public static final String CONFIGURATION_LOCATION = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator;
	public static final String CONFIGURATION_FILE_NAME = "rss-monitor-config.xml";
	public static final String META_DATA_PREFIX = "external";

}
