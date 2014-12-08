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
