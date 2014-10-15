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
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public abstract class UserDefinedRSSManager extends AbstractRSSManager implements RSSManager {

	public UserDefinedRSSManager(Environment environment) {
		super(environment);
	}

	/**
	 * Get databases of user defined RSS Instances for the environment from meta repository
	 *
	 * @return array of databases
	 * @throws RSSManagerException if error occurred when getting databases
	 */
	public Database[] getDatabases() throws RSSManagerException {
		Database[] databases = new Database[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			databases = getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId,
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
	 * Get database users of user defined RSS Instances for the environment from meta repository
	 *
	 * @return database user array
	 * @throws RSSManagerException if error occurred when getting database users
	 */
	public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			users = getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
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
	 * Get database user of given user defined rss instance from meta repository
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param username        username of the database user
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred getting database user
	 */
	public DatabaseUser getDatabaseUser(String rssInstanceName,
	                                    String username) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			boolean isExist = getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(), username, tenantId,
					                                                     RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			if (!isExist) {
				throw new RSSManagerException("Database user '" + username + "' does not exist " +
				                              "in RSS instance '" + rssInstanceName + "'");
			}
			return getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(), rssInstanceName, username, tenantId,
			                                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database user '" + username + "' belongs to the RSS instance '" +
			                              rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Get attached database users give user defined rss instance
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred getting attached users
	 */
	public DatabaseUser[] getAttachedUsers(String rssInstanceName,
	                                       String databaseName) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase( this.getEnvironmentName(), databaseName,
							RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				throw new RSSManagerException("Database '" + databaseName
				                              + "' does not exist " + "in RSS instance '"
				                              + rssInstanceName + "'");
			}
			return getUserDatabaseEntryDAO().getAssignedDatabaseUsers(getEnvironmentName(), rssInstance.getName(),
			                                                          databaseName, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database users already attached to database '" + databaseName + "' which " +
			                              "belongs to the RSS instance '" + rssInstanceName + ", from RSS metadata " +
			                              "repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Get available users to attach to rss instance
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred while getting available users
	 */
	public DatabaseUser[] getAvailableUsers(String rssInstanceName,
	                                        String databaseName) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			DatabaseUser[] availableDatabaseUsers = getUserDatabaseEntryDAO().getAvailableDatabaseUsers(getEnvironmentName(), rssInstanceName, databaseName,
			                                                   tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			return availableDatabaseUsers;
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database users available to be attached to database '" + databaseName +
			                              "' which belongs to the RSS instance '" + rssInstanceName + ", from RSS " +
			                              "metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Get database user privileges in user define RSS Instance of a given database user
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @param username        username of the database user
	 * @return DatabasePrivilegeSet
	 * @throws RSSManagerException if error occurred getting database user privileges from meta repository
	 */
	public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
	                                                      String username) throws RSSManagerException {
		DatabasePrivilegeSet privilegesSet = null;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceName = getRSSDAO().getDatabaseDAO()
					.resolveRSSInstanceNameByDatabase(getEnvironmentName(),
					                                  databaseName,
					                                  RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
					                                  tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				throw new RSSManagerException(
						"Database '" + databaseName + "' does not exist " + "in RSS instance '" +
						rssInstanceName + "'"
				);
			}
			Database database = getDatabaseDAO().getDatabase(this.getEnvironmentName(), rssInstanceName,
			                                                             databaseName, tenantId,
			                                                             RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			DatabaseUser databaseUser = getDatabaseUserDAO().getDatabaseUser(this.getEnvironmentName(),
			                                                                             username, tenantId,
			                                                                             RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			UserDatabaseEntry userDatabaseEntry = getUserDatabaseEntryDAO().getUserDatabaseEntry(database.getId(), databaseUser.getId());
			UserDatabasePrivilege privileges = userDatabaseEntry.getUserPrivileges();
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
