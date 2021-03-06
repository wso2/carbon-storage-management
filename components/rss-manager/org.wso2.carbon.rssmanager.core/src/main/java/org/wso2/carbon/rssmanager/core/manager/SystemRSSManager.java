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
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAOFactory;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class SystemRSSManager extends AbstractRSSManager{

	public SystemRSSManager(Environment environment) {
		super(environment);
	}

	/**
	 * Get databases of system RSS Instances for the environment
	 *
	 * @return array of databases
	 * @throws RSSManagerException if error occurred while getting databases
	 */
	public Database[] getDatabases() throws RSSManagerException {
		Database[] databases = new Database[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
            databases = getDatabaseDAO().getDatabases(this.getEnvironmentName(), tenantId,
                                                      RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata " +
			             "corresponding to databases, from RSS metadata repository : " +
			             e.getMessage();
			handleException(msg, e);
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata " + "corresponding to databases, from "
					+ "RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return databases;
	}

	/**
	 * Add database user
	 *
	 * @param conn            rss meta repository instance database connection
	 * @param user              database user properties
	 * @param qualifiedUsername fully qualified username
	 * @param rssInstance       name of the rss instance
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected DatabaseUser addDatabaseUser(Connection conn, DatabaseUser user,
	                                       String qualifiedUsername, RSSInstance rssInstance)
			throws RSSManagerException, RSSDAOException, RSSDatabaseConnectionException {
		boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, rssInstance.getInstanceType());
		if (isExist) {
			String msg = "Database user '" + qualifiedUsername + "' already exists";
			throw new RSSManagerException(msg);
		}
		/* Sets the fully qualified username */
		user.setName(qualifiedUsername);
		user.setRssInstanceName(user.getRssInstanceName());
		EnvironmentManagementDAO entityDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO();
		Environment envrionment = entityDAO.getEnvironmentDAO().getEnvironment(getEnvironmentName());
		Set<RSSInstance> servers = new HashSet<RSSInstance>();
		user.setEnvironmentId(envrionment.getId());
		servers.add(rssInstance);
		user.setInstances(servers);
		final int tenantId = RSSManagerUtil.getTenantId();
		user.setTenantId(tenantId);
		this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(conn, user);
		return user;
	}

	/**
	 * Check whether database user exist
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param instanceType    rss instance type
	 * @return boolean
	 * @throws RSSManagerException
	 */
	public boolean isDatabaseUserExist(String rssInstanceName,
	                                   String username, String instanceType)
			throws RSSManagerException, RSSDatabaseConnectionException {
		boolean isExist = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			isExist = getRSSDAO().getDatabaseUserDAO().isSystemDatabaseUserExist(getEnvironmentName(),
					username, tenantId, instanceType);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while checking whether the database " + "user named '" +
			             username + "' already exists in RSS instance '" + rssInstanceName + "': " +
			             e.getMessage();
			handleException(msg, e);
		}
		return isExist;
	}

	/**
	 * Get database users of system RSS Instances for the environment
	 *
	 * @return get system database users
	 * @throws RSSManagerException if error occurred when getting system database users
	 */
	public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
            users = getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),tenantId,
                                                          RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata " +
			             "corresponding to database users, from RSS metadata repository : " +
			             e.getMessage();
			handleException(msg, e);
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata " +
					"corresponding to database users, from RSS metadata repository : " +
					e.getMessage();
			handleException(msg, e);
		}
		return users;
	}

	/**
	 * Get database user of given system rss instance
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param username        username of the database user
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred getting specified system user
	 */
	public DatabaseUser getDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
		DatabaseUser user = null;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			user = getDatabaseUserDAO().getSystemDatabaseUser(getEnvironmentName(), username, tenantId,
					RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
			             "database user '" + username + "' from RSS metadata " + "repository : " +
			             e.getMessage();
			handleException(msg, e);
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata corresponding to the " +
					"database user '" + username + "' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return user;
	}

	/**
	 * Get attached database users give system rss instance
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred getting attached users
	 */
	public DatabaseUser[] getAttachedUsers(String rssInstanceName, String databaseName)
			throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			//get actual rss instance name since this is a system database instance where we hide
			// the real rss instance name from user
			rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(getEnvironmentName(), databaseName,
					RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				throw new RSSManagerException(
						"Database '" + databaseName + "' does not exist " + "in RSS instance '" +
								rssInstanceName + "'"
				);
			}
			users = getUserDatabaseEntryDAO().getAssignedDatabaseUsers(getEnvironmentName(), rssInstance.getName(),
					databaseName, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
					"database users attached to the database '" + databaseName +
					"' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata corresponding to the " +
					"database users attached to the database '" + databaseName + "' from RSS metadata repository : " + e
					.getMessage();
			handleException(msg, e);
		}
		return users;
	}

	/**
	 * Get available users to attach to rss instance
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @return DatabaseUser
	 * @throws RSSManagerException if error occurred getting available users
	 */
	public DatabaseUser[] getAvailableUsers(String rssInstanceName, String databaseName)
			throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			//get actual rss instance name since this is a system database instance where
			// we hide the real rss instance name from user
			rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(getEnvironmentName(), databaseName,
					RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            return getUserDatabaseEntryDAO().getAvailableDatabaseUsers(getEnvironmentName(), rssInstanceName, databaseName,
                    tenantId,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to available "
                         + "database users to be attached "
                         + "to the database'" + databaseName + "' from RSS metadata repository : "
                         + e.getMessage();
			handleException(msg, e);
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata corresponding to available "
					+ "database users to be attached  to the database'" + databaseName + "' from RSS metadata repository : "
					+ e.getMessage();
			handleException(msg, e);
		}
		return users;
	}

	/**
	 * Get database user privileges in system RSS Instance of a given database user
	 *
	 * @param rssInstanceName name of the RSS Instance
	 * @param databaseName    name of the database
	 * @param username        username of the database user
	 * @return DatabasePrivilegeSet
	 * @throws RSSManagerException if error occurred while getting user privileges
	 */
	public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
	                                                      String username)
			throws RSSManagerException {
		DatabasePrivilegeSet privilegesSet = null;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceName = getRSSDAO().getDatabaseDAO().resolveRSSInstanceNameByDatabase(getEnvironmentName(), databaseName,
                                                      RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
					                                  tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				throw new RSSManagerException(
						"Database '" + databaseName + "' does not exist " + "in RSS instance '" +
						rssInstanceName + "'"
				);
			}
            Database database = getDatabaseDAO().getDatabase(this.getEnvironmentName(),
                                                             rssInstanceName,
                                                             databaseName, tenantId,
                                                             RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            DatabaseUser databaseUser = getDatabaseUserDAO().getSystemDatabaseUser(
		            this.getEnvironmentName(),
		            username, tenantId,
		            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			UserDatabaseEntry userDatabaseEntry = getRSSDAO().getUserDatabaseEntryDAO()
                    .getUserDatabaseEntry(database.getId(), databaseUser.getId());
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
		} catch (RSSDatabaseConnectionException e) {
			String msg = "Database server error occurred while retrieving metadata corresponding to the " +
					"database privileges assigned to database user '" + username +
					"' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return privilegesSet;
	}

	/**
	 * Add database user
	 *
	 * @param conn            rss meta repository instance database connection
	 * @param user              database user properties
	 * @param qualifiedUsername fully qualified username
	 * @param instanceType      rss instance type
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected DatabaseUser addDatabaseUser(Connection conn, DatabaseUser user,
	                                       String qualifiedUsername, String instanceType)
			throws RSSManagerException, RSSDAOException, RSSDatabaseConnectionException {

		boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, instanceType);
		if (isExist) {
			String msg = "Database user '" + qualifiedUsername + "' already exists";
			throw new RSSManagerException(msg);
		}
		/* Sets the fully qualified username */
		final int tenantId = RSSManagerUtil.getTenantId();
		user.setName(qualifiedUsername);
		EnvironmentManagementDAO entityDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO();
		Set<RSSInstance> servers = new HashSet<RSSInstance>(Arrays.asList(
				entityDAO.getRSSInstanceDAO().getSystemRSSInstances(
						this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID)));
		user.setInstances(servers);
		user.setTenantId(tenantId);
		this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(conn, user);
		return user;
	}
}
