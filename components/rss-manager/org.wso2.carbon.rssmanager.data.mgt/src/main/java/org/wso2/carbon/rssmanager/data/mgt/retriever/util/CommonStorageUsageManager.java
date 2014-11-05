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
