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

import java.sql.PreparedStatement;

/**
 * DatabaseUserDAO interface
 */
public interface DatabaseUserDAO {

	/**
	 * Method to add database user information to the RSS meta data repository. This method takes an argument of native
	 * add database user prepared statement which needs to be executed along with the meta repository insert as native
	 * sql operations not transactional
	 *
	 * @param nativeAddUserStatement native add database user statement
	 * @param user database user configuration
	 * @throws RSSDAOException if something went wrong when adding database configuration details to the meta repository
	 */
	void addDatabaseUser(PreparedStatement nativeAddUserStatement, DatabaseUser user) throws RSSDAOException;

	/**
	 * Method to remove database user configuration information from RSS metadata repository. This method takes an argument of native
	 * remove database user prepared statement which needs to be executed along with the meta repository database entry removal as native
	 * sql operations not transactional
	 *
	 * @param nativeRemoveUserStatement native remove database user statement
	 * @param user database user configuration to removed
	 * @throws RSSDAOException if something went wrong when remove database configuration details from the meta repository
	 */
	void removeDatabaseUser(PreparedStatement nativeRemoveUserStatement, DatabaseUser user) throws RSSDAOException;

	/**
	 * Check whether database user exist
	 *
	 * @param environmentName name of the environment
	 * @param username username of the database user
	 * @param tenantId tenant id of the database user added system user
	 * @param instanceType instance type
	 * @return boolean true if user exists else false
	 * @throws RSSDAOException  if something went wrong when checking database user existence
	 */
	boolean isDatabaseUserExist(String environmentName, String username,
	                            int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Get database user
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param username username of the database user
	 * @param tenantId tenant id of the database user added system user
	 * @param instanceType instance type
	 * @return database user object
	 * @throws RSSDAOException if something went wrong when fetch database user data
	 */
	DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName, String username,
	                             int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Get database user without specifying rss instance name
	 * @param environmentName name of the environment
	 * @param username username of the database user
	 * @param tenantId tenant id of the database user added system user
	 * @param instanceType instance type
	 * @return database user object
	 * @throws RSSDAOException if something went wrong when fetch database user data
	 */
	DatabaseUser getDatabaseUser(String environmentName,
	                             String username, int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Get all database users belongs to a tenant in a environment for given instance type
	 * @param environmentName name of the environment
	 * @param tenantId tenant id of the database user added system user
	 * @param instanceType instance type
	 * @return array of all the database users match with given data
	 * @throws RSSDAOException if something went wrong when fetch database users data for tenant
	 */
	DatabaseUser[] getDatabaseUsers(String environmentName, int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Resolve rss instance name by database user
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceType name of the rss instance
	 * @param username database username
	 * @param tenantId tenant id of the database user added system user
	 * @return rss instance name
	 * @throws RSSDAOException if something went wrong when resolving rss instance name from database user name
	 */
	String resolveRSSInstanceNameByUser(String environmentName, String rssInstanceType, String username,
	                                    int tenantId) throws RSSDAOException;
}
