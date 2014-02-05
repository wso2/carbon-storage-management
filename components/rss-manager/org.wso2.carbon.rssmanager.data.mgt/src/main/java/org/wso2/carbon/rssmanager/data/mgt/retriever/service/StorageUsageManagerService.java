package org.wso2.carbon.rssmanager.data.mgt.retriever.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.TenantDBInfo;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.CommonStorageUsageManager;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.Manager;

public class StorageUsageManagerService {

    private Manager getStorageUsageManager() throws UsageManagerException {
        StorageMetaDataConfig config = StorageMetaDataConfig.getInstance();
        if (config == null) {
            throw new UsageManagerException("StorageMetaDataConfig is not properly initialized and is null");
        }
        return new CommonStorageUsageManager();
    }

    public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws UsageManagerException {

        Manager manager = getStorageUsageManager();
        List<UsageStatistic> statistics = manager.getGlobalStatistics(identifier);
        return statistics;
    }
    
    

    public List<UsageStatistic> getGlobalStatisticsForAllDBs() throws UsageManagerException, RSSManagerException {
        Set<RSSServer> rssInstanceList = StorageMetaDataConfig.getRssInfoReciever().getRSSInstances();
        try {
            StorageMetaDataConfig.getInstance().createDataSources(rssInstanceList);
        } catch (DataSourceException e) {
            throw new UsageManagerException(e);
        }
        
        Map<String, TenantDBInfo> dbInformationMap = StorageMetaDataConfig.getRssInfoReciever().getTenantDBInformationMap();

        Manager manager = getStorageUsageManager();
        List<UsageStatistic> allStats = manager.getGlobalStatisticsForAllDBs();
        
        List<UsageStatistic> validStats = new ArrayList<UsageStatistic>();
        
        for (UsageStatistic stat : allStats) {
        	String dbName = stat.getDatabaseName();
        	
        	if((!stat.isValid()) || dbInformationMap.get(dbName) == null){
        		continue;
        	}
        	//TODO
        	String tenantId  = dbInformationMap.get(dbName).getTenantDomainName();
            stat.setTenantId(tenantId);
            validStats.add(stat);
        }
        return validStats;
    }

}
