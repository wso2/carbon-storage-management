package org.wso2.carbon.rssmanager.data.mgt.retriever.util;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.UsageDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;

public class CommonStorageUsageManager extends StorageUsageManager{
	private static Log log = LogFactory.getLog(CommonStorageUsageManager.class);
	

    public List<UsageStatistic> getGlobalStatisticsForAllDBs() throws UsageManagerException {
        
        Set<DataSourceIdentifier> identifierSet = StorageMetaDataConfig.getInstance().getDataSourceMap().keySet();
        List<UsageStatistic> stats = getConcurrentUsageCollector().getConcurrentStatistics(identifierSet);
               
        return stats;
    }

    public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws UsageManagerException{
    	List<UsageStatistic> stats = null;
    	UsageDAO dao = null;
        try {
        	dao = ((UsageDAO) getPooledObjectHelper().getPooledDAO(identifier.getType()));
            stats = dao.getGlobalStatistics(identifier);
        } catch (Exception ex) {
            log.error(ex);
            throw new UsageManagerException(ex);
        }finally{
        	try {
	            getPooledObjectHelper().returnPooledDAO(identifier.getType(), dao);
            } catch (Exception ex) {
            	log.error(ex);
                throw new UsageManagerException(ex);
            }
        }
        return stats;
    }
    

    

}
