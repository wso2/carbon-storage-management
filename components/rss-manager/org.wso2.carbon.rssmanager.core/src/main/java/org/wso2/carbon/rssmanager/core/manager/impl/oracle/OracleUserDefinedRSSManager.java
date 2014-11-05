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
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */

public class OracleUserDefinedRSSManager extends UserDefinedRSSManager {

    public OracleUserDefinedRSSManager(Environment environment) {
        super(environment);
    }

    public Database addDatabase(Database database) throws RSSManagerException {
	    //TODO implement when improve the oracle support
        return null;  
    }

    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
    }

    public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
	    return false;
    }

	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		//TODO implement when improve the oracle support
		return null;
	}

	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
		//TODO implement when improve the oracle support
		return null;
    }

    public void removeDatabaseUser(String rssInstanceName,
                                   String username) throws RSSManagerException {
	    //TODO implement when improve the oracle support
    }

    public DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                           String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
	    return new DatabaseUser[0];
    }

    public DatabaseUser[] getAvailableUsers(String rssInstanceName,
                                            String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
	    return new DatabaseUser[0];
    }

    public void attachUser(UserDatabaseEntry ude,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
	    //TODO implement when improve the oracle support
    }

    public void detachUser(UserDatabaseEntry ude) throws RSSManagerException {
	    //TODO implement when improve the oracle support
    }

    public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
	    //TODO implement when improve the oracle support
	    return false;
    }

    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
    }

    public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) {
	    //TODO implement when improve the oracle support
	    return null;
    }
}

