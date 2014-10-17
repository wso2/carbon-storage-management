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
