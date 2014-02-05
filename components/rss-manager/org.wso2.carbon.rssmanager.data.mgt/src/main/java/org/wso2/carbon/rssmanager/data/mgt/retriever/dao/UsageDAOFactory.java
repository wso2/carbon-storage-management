package org.wso2.carbon.rssmanager.data.mgt.retriever.dao;

import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.impl.MySQLUsageDAOImpl;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.impl.OracleUsageDAOImpl;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.Manager;

public class UsageDAOFactory {
	
	public static UsageDAO getUsageDAO(final DBType type, final Manager mg){
		UsageDAO dao = null;
		switch(type){
			case MYSQL: 
				
				dao = new MySQLUsageDAOImpl(mg);
				break;
			case ORACLE: 
				
				dao = new OracleUsageDAOImpl(mg);
				break;
		}
		
		return dao;
	}

}
