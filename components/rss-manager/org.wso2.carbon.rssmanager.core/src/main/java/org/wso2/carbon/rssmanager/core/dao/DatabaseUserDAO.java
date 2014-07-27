/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;

public interface DatabaseUserDAO extends EntityBaseDAO<Integer, DatabaseUser>{

	@Deprecated
    void addDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user,
                         int tenantId) throws RSSDAOException;
	
	void addDatabaseUser(DatabaseUser user) throws RSSDAOException;

    void removeDatabaseUser(DatabaseUser user) throws RSSDAOException;

    boolean isDatabaseUserExist(String environmentName, String username,
                                int tenantId,String instanceType) throws RSSDAOException;
    DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName, String username,
                                 int tenantId,String instanceType) throws RSSDAOException;
    
    DatabaseUser getDatabaseUser(String environmentName,
                                 String username, int tenantId, String instanceType) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsers(String environmentName, int tenantId, String instanceType) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByRSSInstance(String environmentName, String rssInstanceName,
                                                 int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByDatabase(String environmentName, String rssInstanceName,
                                              String database, int tenantId) throws RSSDAOException;

    String resolveRSSInstanceByUser(String environmentName,
                                    String rssInstanceType, String username,
                                    int tenantId) throws RSSDAOException;
}
