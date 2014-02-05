package org.wso2.carbon.rssmanager.data.mgt.common;

import java.io.File;

import org.wso2.carbon.utils.CarbonUtils;

public class RSSPublisherConstants {

	public static final String RSS_MONITOR = "rssMonitor";
	public static final String RSS_MONITOR_SERVICE_NAME = "rssMonitorServiceName";
	public static final String RSS_STATS = "rssStats";
	public static final String CONFIGURATION_LOCATION = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator;
	public static final String CONFIGURATION_FILE_NAME = "rss-monitor-config.xml";
	public static final String META_DATA_PREFIX = "external";
	
	public static final String MYSQL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
	public static final String ORACLE_DATA_SOURCE_CLASS_NAME = "oracle.jdbc.xa.client.OracleXADataSource";
	public static final String MSSQL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
	public static final String URL_PROPERTY = "URL";
	public static final String USERNAME_PROPERTY = "user";
	public static final String PASSWORD_PROPERTY = "password";
	
	public static final String ENCODING = "UTF-8";
	
	public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

}
