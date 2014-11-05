/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.adaptor;

import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface RSSManagerAdaptor {

	/**
	 * Add database to meta repository and to rss instance
	 *
	 * @param database the database properties
	 * @return database object
	 * @throws RSSManagerException if something went wrong when adding database
	 */
	Database addDatabase(Database database) throws RSSManagerException;

	/**
	 * Remove databsse from meta repository and rss instance
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param type database type
	 * @throws RSSManagerException if error occurred when removing database
	 */
	void removeDatabase(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

	/**
	 * Add database user to rss instances
	 *
	 * @param user database user properties
	 * @return database user object
	 * @throws RSSManagerException if error occurred when adding database user
	 */
	DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException;


	/**
	 * Remove database user from rss instance
	 * @param rssInstanceName name of the rss instance
	 * @param username name of the database user
	 * @param type instance type
	 * @throws RSSManagerException if error occurred when removing databse user
	 */
	void removeDatabaseUser(String rssInstanceName, String username, String type) throws RSSManagerException;

	/**
	 * Update database user privileges
	 *
	 * @param privileges set of privileges
	 * @param user database user properties
	 * @param databaseName name of the database
	 * @throws RSSManagerException if error occurred when updating the database user privileges
	 */
	void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                  String databaseName) throws RSSManagerException;

	/**
	 * Attach user to database
	 * @param userDatabaseEntry user database entry which hold the privilege template and database
	 * @param templateEntry set of privileges
	 * @throws RSSManagerException
	 */
	void attachUser(UserDatabaseEntry userDatabaseEntry,
	                DatabasePrivilegeTemplateEntry templateEntry) throws RSSManagerException;

	/**
	 * Deattach user from database
	 * @param userDatabaseEntry contains database user and database information
	 * @throws RSSManagerException if error occurred when de attaching user
	 */
	void detachUser(UserDatabaseEntry userDatabaseEntry) throws RSSManagerException;

	/**
	 * Get database user information
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param username user name of the database user
	 * @param type instance type
	 * @return database user object
	 * @throws RSSManagerException
	 */
	DatabaseUser getDatabaseUser(String rssInstanceName, String username, String type) throws RSSManagerException;

	/**
	 * Get database information
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param type instance type
	 * @return database object
	 * @throws RSSManagerException if error occurred when getting database information
	 */
	Database getDatabase(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

	/**
	 * Get attached database users for database
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param type instance type
	 * @return array of database users
	 * @throws RSSManagerException if error occurred when getting attached users
	 */
	DatabaseUser[] getAttachedUsers(String rssInstanceName,
	                                String databaseName, String type) throws RSSManagerException;

	/**
	 * Get available users to attach to database
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param type instance type
	 * @return array of available database users
	 * @throws RSSManagerException if error occurred while getting available database users to attached
	 */
	DatabaseUser[] getAvailableUsers(String rssInstanceName,
	                                 String databaseName, String type) throws RSSManagerException;

	/**
	 * Get database user privileges
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param username of the database user
	 * @param type
	 * @return
	 * @throws RSSManagerException
	 */
	DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
	                                               String username, String type) throws RSSManagerException;

	/**
	 * Get all databases information
	 *
	 * @return array of database
	 * @throws RSSManagerException if error occur when getting databases
	 */
	Database[] getDatabases() throws RSSManagerException;

	/**
	 * Get database users information
	 *
	 * @return array of database users
	 * @throws RSSManagerException if error occurred when getting database users
	 */
	DatabaseUser[] getDatabaseUsers() throws RSSManagerException;

	/**
	 * Check database is exist
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param type instance type
	 * @return true if database is exist else false
	 * @throws RSSManagerException if error occurred when checking the database existence
	 */
	boolean isDatabaseExist(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

	/**
	 * Check database user is exist
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param username name of the database user
	 * @param type instance type
	 * @return true if database user is exist else false
	 * @throws RSSManagerException if error occurred when checking the database user existence
	 */
	boolean isDatabaseUserExist(String rssInstanceName, String username, String type) throws RSSManagerException;

	/**
	 * Edit database user
	 *
	 * @param environmentName name of the environment
	 * @param databaseUser database user properties
	 * @return database user object
	 * @throws RSSManagerException if error occurred when editing database user
	 */
	DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) throws RSSManagerException;

}
