package org.wso2.carbon.rssmanager.data.mgt.retriever.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.StatisticType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.AbstractDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.UsageDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.Manager;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.UsageManagerConstants;

public class MySQLUsageDAOImpl extends AbstractDAO implements UsageDAO{
	private static Log log = LogFactory.getLog(MySQLUsageDAOImpl.class);
	
	public MySQLUsageDAOImpl(Manager mg){
		super(mg);
	}

	public List<UsageStatistic> getGlobalStatistics(DataSourceIdentifier identifier) throws Exception {
		
        Connection conn = getManager().getDBConnection(identifier);
        
        List<UsageStatistic> statistics = new ArrayList<UsageStatistic>();
        String sql = StorageMetaDataConfig.getInstance().getQueryMap().get(UsageManagerConstants.MYSQL_STORAGE_SIZE_QUERY);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
        	stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            while (rs != null && rs.next()) {
            	UsageStatistic stats = new UsageStatistic();
                String databaseName = rs.getString(StatisticType.DATABASE_NAME.name());
                String diskUsage = rs.getString(StatisticType.DISK_USAGE.name());
                String freeSpace = rs.getString(StatisticType.FREE_SPACE.name());

                createStatistics(stats, StatisticType.DATABASE_NAME.name(), databaseName);
                createStatistics(stats, StatisticType.DISK_USAGE.name(), diskUsage);
                createStatistics(stats, StatisticType.FREE_SPACE.name(), freeSpace);
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
