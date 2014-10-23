/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class SystemRSSManager extends AbstractRSSManager implements RSSManager{

    public SystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    /**
     * Get databases of system RSS Instances for the environment
     * @return
     * @throws RSSManagerException
     */
    public Database[] getDatabases() throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(this.getEnvironmentName(), tenantId,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return databases;
    }

    /**
     * Get database users of system RSS Instances for the environment
     * @return
     * @throws RSSManagerException
     */
    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
                    tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return users;
    }

    /**
     * Get database user of given system rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param username username of the database user
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        DatabaseUser user = null;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            user = getRSSDAO().getDatabaseUserDAO()
                    .getDatabaseUser(getEnvironmentName(), username,
                            tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
                    "database user '" + username + "' from RSS metadata " + "repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return user;
    }

    /**
     * Get attached database users give system rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser[] getAttachedUsers(String rssInstanceName, String databaseName)
            throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(getEnvironmentName(),
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                getEntityManager().rollbackJPATransaction();
                throw new RSSManagerException(
                        "Database '" + databaseName + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }
            users = getRSSDAO().getUserDatabaseEntryDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                    rssInstance.getName(),
                    databaseName, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
                    "database users attached to the database '" + databaseName +
                    "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        }
        return users;
    }

    /**
     * Get available users to attach to rss instance
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @return DatabaseUser
     * @throws RSSManagerException
     */
	public DatabaseUser[] getAvailableUsers(String rssInstanceName, String databaseName)
	                                                                                    throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceName = getRSSDAO().getDatabaseDAO()
			                             .resolveRSSInstanceByDatabase(getEnvironmentName(),
			                                                           databaseName,
			                                                           RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
			                                                           tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			UserDatabaseEntryDAO dao = getRSSDAO().getUserDatabaseEntryDAO();
			DatabaseUser[] existingUsers = dao.getAvailableDatabaseUsers(getEnvironmentName(),
			                                                             rssInstance.getName(), databaseName,
			                                                             tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
			DatabaseUser[] assignedUsers = dao.getAssignedDatabaseUsers(getEnvironmentName(),
			                                                            rssInstanceName, databaseName,
			                                                            tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			List<DatabaseUser> attachedUsers = Collections.EMPTY_LIST;
			if (assignedUsers != null && assignedUsers.length > 0) {
				attachedUsers = Arrays.asList(assignedUsers);
			}
			for (DatabaseUser user : existingUsers) {
				if (!attachedUsers.contains(user)) {
					availableUsers.add(user);
				}
			}
			users = availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
		} catch (Exception e) {
			String msg = "Error occurred while retrieving metadata corresponding to available " + "database users to be attached to the database'" + databaseName + "' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return users;
	}

    /**
     * Get database user privileges in system RSS Instance of a given database user
     * @param rssInstanceName name of the RSS Instance
     * @param databaseName name of the database
     * @param username username of the database user
     * @return DatabasePrivilegeSet
     * @throws RSSManagerException
     */
    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
                                                          String username) throws RSSManagerException {
        DatabasePrivilegeSet privilegesSet = null;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(getEnvironmentName(),
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                throw new RSSManagerException(
                        "Database '" + databaseName + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }
            UserDatabasePrivilege privileges = getRSSDAO().getUserPrivilegesDAO()
                    .getUserDatabasePrivileges(this.getEnvironmentName(),
                            rssInstance.getName(), databaseName, username,
                            tenantId);
            if (privileges != null) {
                privilegesSet = new MySQLPrivilegeSet();
            }
            RSSManagerUtil.createDatabasePrivilegeSet(privilegesSet, privileges);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata corresponding to the " +
                    "database privileges assigned to database user '" + username +
                    "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        }
        return privilegesSet;
    }
}
