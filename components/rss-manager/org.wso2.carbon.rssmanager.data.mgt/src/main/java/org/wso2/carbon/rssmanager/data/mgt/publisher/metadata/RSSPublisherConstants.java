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
