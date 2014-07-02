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

import javax.persistence.Query;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;

public interface UserDatabaseEntryDAO extends EntityBaseDAO<Integer, UserDatabaseEntry>{

    int addUserDatabaseEntry(String environmentName, UserDatabaseEntry entry,
                             int tenantId) throws RSSDAOException;
    
    void removeUserDatabaseEntriesByUser(Integer userId) throws RSSDAOException;
    
    void removeUserDatabaseEntriesByDatabase(Integer dbId) throws RSSDAOException;

   /* void removeUserDatabaseEntry(String environmentName, int rssInstanceId, String username,
                                 String type, int tenantId) throws RSSDAOException;

    void removeUserDatabaseEntriesByDatabase(String environmentName, int rssInstanceId,
                                             String databaseName, String type,
                                             int tenantId) throws RSSDAOException;

    void removeUserDatabaseEntriesByUser(String environmentName, int rssInstanceId,
                                         String username, String type,
                                         int tenantId) throws RSSDAOException;*/

    UserDatabaseEntry getUserDatabaseEntry(Integer envId, Integer instanceId, UserDatabaseEntry entry,
                                           int tenantId) throws RSSDAOException;

    UserDatabaseEntry[] getUserDatabaseEntries(String environmentName, UserDatabaseEntry entries,
                                               int tenantId) throws RSSDAOException;

}
