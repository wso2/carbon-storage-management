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

package org.wso2.carbon.rssmanager.core.manager.impl.oracle;

import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;

public class OracleUserDefinedRSSManager extends UserDefinedRSSManager {

    public OracleUserDefinedRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }


    @Override
    public Database addDatabase(Database database) throws RSSManagerException {
        return null;  
    }

    @Override
    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        
    }

    @Override
    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        return null;  
    }

    @Override
    public void removeDatabaseUser(String rssInstanceName,
                                   String username) throws RSSManagerException {
        
    }

    @Override
    public DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                           String databaseName) throws RSSManagerException {
        return new DatabaseUser[0];  
    }

    @Override
    public DatabaseUser[] getAvailableUsers(String rssInstanceName,
                                            String databaseName) throws RSSManagerException {
        return new DatabaseUser[0];  
    }

    @Override
    public void attachUser(UserDatabaseEntry ude,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        
    }

    @Override
    public void detachUser(UserDatabaseEntry ude) throws RSSManagerException {
        
    }

    @Override
    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        
    }

}
