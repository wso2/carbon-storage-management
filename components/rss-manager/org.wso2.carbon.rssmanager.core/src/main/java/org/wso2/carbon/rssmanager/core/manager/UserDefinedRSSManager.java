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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class UserDefinedRSSManager extends AbstractRSSManager implements RSSManager{

    public UserDefinedRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    /**
     * Get databases of user defined RSS Instances for the environment
     * @return
     * @throws RSSManagerException
     */
    public Database[] getDatabases() throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return databases;
    }

    /**
     * Get database users of user defined RSS Instances for the environment
     * @return
     * @throws RSSManagerException
     */
    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
                    tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return users;
    }

    /**
     * Get database user of given user defined rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param username username of the database user
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),username,tenantId,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            if (!isExist) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(),
                    rssInstanceName, username, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database user '" + username + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        }
    }

    /**
     * Get attached database users give user defined rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                                     String databaseName) throws RSSManagerException {
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            return getRSSDAO().getUserDatabaseEntryDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users already attached to database '" + databaseName + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + ", from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        }
    }

    /**
     * Get available users to attach to rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser[] getAvailableUsers(String rssInstanceName,
                                                              String databaseName) throws RSSManagerException {
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            UserDatabaseEntryDAO dao = getRSSDAO().getUserDatabaseEntryDAO();
            DatabaseUser[] existingUsers =
                    dao.getAssignedDatabaseUsers(getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            Set<String> usernames = new HashSet<String>();
            for (DatabaseUser user : existingUsers) {
                usernames.add(user.getName());
            }

            DatabaseUser[] tmp = dao.getAvailableDatabaseUsers(getEnvironmentName(),
                    rssInstanceName, databaseName,
                    tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
            for (DatabaseUser user : tmp) {
                String username = user.getName();
                if (!usernames.contains(username)) {
                    availableUsers.add(user);
                }
            }
            return availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users available to be attached to database '" + databaseName +
                    "' which belongs to the RSS instance '" + rssInstanceName + ", from RSS " +
                    "metadata repository : " + e.getMessage(), e);
        }
    }

    /**
     * Get database user privileges in user define RSS Instance of a given database user
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @param username username of the database user
     * @return DatabasePrivilegeSet
     * @throws RSSManagerException
     */
    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
    	DatabasePrivilegeSet privilegesSet = null;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            UserDatabasePrivilege privileges =  getRSSDAO().getUserPrivilegesDAO().getUserDatabasePrivileges(getEnvironmentName(),
                    rssInstance.getName(), databaseName, username, tenantId);

            if(privileges != null){
				privilegesSet = new MySQLPrivilegeSet();
			}
			RSSManagerUtil.createDatabasePrivilegeSet(privilegesSet, privileges);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "the privileges assigned to database user '" + username + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + " upon the database '" +
                    databaseName + "', from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        }
        return privilegesSet;
    }
}
