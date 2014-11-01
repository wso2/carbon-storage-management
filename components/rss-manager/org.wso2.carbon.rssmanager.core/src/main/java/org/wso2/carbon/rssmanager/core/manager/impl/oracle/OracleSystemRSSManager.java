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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */
public class OracleSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(OracleSystemRSSManager.class);

    public OracleSystemRSSManager(Environment environment) {
        super(environment);
    }

    public Database addDatabase(Database database) throws RSSManagerException {
        throw new UnsupportedOperationException("CreateDatabase operation is not supported " +
                "for Oracle");
    }

    public void removeDatabase(String rssInstanceName, String name) throws RSSManagerException {
        throw new UnsupportedOperationException("dropDatabase operation is not supported " +
                "for Oracle");
    }

    public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
	    //TODO implement when improve the oracle support
	    return false;
    }

	@Override
	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		//TODO implement when improve the oracle support
		return null;
	}

	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
		//TODO implement when improve the oracle support
		return null;
	}

    public void removeDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
        //TODO implement when improve the oracle support
    }

    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
       //TODO implement when improve the oracle support
    }

    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        throw new UnsupportedOperationException("attachUserToDatabase operation is not " +
                "supported for Oracle");
    }

    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        throw new UnsupportedOperationException("detachUserFromDatabase operation is not " +
                "supported for Oracle");
    }

    public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
        //TODO implement when improve the oracle support
        return false;
    }

    public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) {
        //TODO implement when improve the oracle support
        return null;
    }
}
