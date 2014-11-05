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
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;

import java.sql.PreparedStatement;

public interface UserDatabaseEntryDAO {

	/**
	 * Method to add database user entry information to the RSS meta data repository. This method takes an argument of native
	 * add database user entry prepared statement which needs to be executed along with the meta repository insert as native
	 * sql operations not transactional
	 *
	 * @param nativeAttachUserStatement native attach user statement
	 * @param environmentName name of the environment
	 * @param entry user database entry which hold the user details and privileges
	 * @param tenantId tenant id
	 * @return database user entry id
	 * @throws RSSDAOException if something went wrong when adding database user data to meta repository
	 */
	int addUserDatabaseEntry(PreparedStatement nativeAttachUserStatement, String environmentName, UserDatabaseEntry entry,
	                         int tenantId) throws RSSDAOException;

	/**
	 * Method to remove database user configuration information from RSS metadata repository. This method takes an argument of native
	 * remove database user entry prepared statement which needs to be executed along with the meta repository database entry removal as native
	 * sql operations not transactional
	 *
	 * @param databaseId database id
	 * @throws RSSDAOException if something went wrong when remove database entries by database id
	 */
	void removeUserDatabaseEntriesByDatabase(Integer databaseId) throws RSSDAOException;

	/**
	 * Det user database entry
	 *
	 * @param databaseId database id
	 * @param userId user id
	 * @return user database entry object with required fields
	 * @throws RSSDAOException if something went wrong when querying user database entry
	 */
	UserDatabaseEntry getUserDatabaseEntry(int databaseId, int userId) throws RSSDAOException;

	/**
	 * Get assigned database users for particular database
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param tenantId tenant id of user that added the database users
	 * @param instanceType instance type
	 * @return array of attache database users
	 * @throws RSSDAOException if something went wrong when query the assigned database users
	 */
	DatabaseUser[] getAssignedDatabaseUsers(String environmentName, String rssInstanceName,
	                                               String databaseName,
	                                               int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Get available database users to attached for a database
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param tenantId tenant id of user that added the database users
	 * @param instanceType instance type
	 * @return array of available database users to attach
	 * @throws RSSDAOException if something went wrong when query available database users
	 */
	public DatabaseUser[] getAvailableDatabaseUsers(String environmentName, String rssInstanceName,
	                                                String databaseName, int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Method to remove database user entry configuration information from RSS metadata repository. This method takes an argument of native
	 * remove database user from database prepared statement which needs to be executed along with the meta repository database entry removal as native
	 * sql operations not transactional
	 *
	 * @param nativeDeattachUserStatement native deattach user statement
	 * @param dbId database id
	 * @param userId database user if
	 * @throws RSSDAOException if something went wrong when removing database user entry
	 */
	public void removeUserDatabaseEntry(PreparedStatement nativeDeattachUserStatement, int dbId, int userId) throws RSSDAOException;

	/**
	 * Check whether database user is attached to one or more databases
	 *
	 * @param userId database user id
	 * @return true if user attached else false
	 * @throws RSSDAOException if something went wrong when checking database user existence
	 */
	public boolean isDatabaseUserEntriesExist(int userId) throws RSSDAOException;
}
