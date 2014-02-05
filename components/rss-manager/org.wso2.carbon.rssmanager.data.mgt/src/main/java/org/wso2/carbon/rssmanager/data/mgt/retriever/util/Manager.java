package org.wso2.carbon.rssmanager.data.mgt.retriever.util;

import java.sql.Connection;
import java.util.List;

import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;

public interface Manager {
	
	public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws UsageManagerException;

    public List<UsageStatistic> getGlobalStatisticsForAllDBs() throws UsageManagerException;
	
	public Connection getDBConnection(DataSourceIdentifier identifier) throws Exception;


}
