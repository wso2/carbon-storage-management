package org.wso2.carbon.rssmanager.data.mgt.retriever.dao;

import java.util.List;

import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.pool.Poolable;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;

public interface UsageDAO extends Poolable{

	public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws Exception;
}
