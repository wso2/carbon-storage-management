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
package org.wso2.carbon.rssmanager.data.mgt.retriever.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.StatisticType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.AbstractDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.UsageDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.Manager;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.UsageManagerConstants;

public class OracleUsageDAOImpl extends AbstractDAO implements UsageDAO{
	private static Log log = LogFactory.getLog(OracleUsageDAOImpl.class);
	
	
	public OracleUsageDAOImpl(Manager mg){
		super(mg);
	}

	public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws Exception {
		
        Connection conn = getManager().getDBConnection(identifier);
        
        List<UsageStatistic> statistics = new ArrayList<UsageStatistic>();
        
        String sql = StorageMetaDataConfig.getInstance().getQueryMap().get(UsageManagerConstants.ORACLE_STORAGE_SIZE_QUERY);
       
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
        	stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs != null && rs.next()) {                
            	UsageStatistic stats = new UsageStatistic();
                String dbUsage = rs.getString(StatisticType.DISK_USAGE.name());
                String dbUser = rs.getString(StatisticType.DATABASE_NAME.name());
                
                createStatistics(stats, StatisticType.DISK_USAGE.name(), dbUsage);
                createStatistics(stats, StatisticType.DATABASE_NAME.name(), dbUser);
                
                createStatistics(stats, StatisticType.TENANT_ID.name(), identifier.getRssServer().getRssInstanceId());
                createStatistics(stats, StatisticType.HOST_ADDRESS.name(), identifier.getRssServer().getServerURL());
                
                
                stats.setValid(true);
                
                statistics.add(stats);
                
            }
            
        }catch(Exception ex){
        	log.error(ex);
        	throw ex;
        }finally{
        	close(stmt, rs, conn);
        }
        
        
       return statistics;
    }
	
	
}
