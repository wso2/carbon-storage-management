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
package org.wso2.carbon.rssmanager.core.service;

import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface RSSManagerService {

	/**
	 * Add new RSS Instances to the system
	 *
	 * @param environmentName environment name which RSS instance belongs
	 * @param rssInstance     RSS Instance properties
	 * @throws RSSManagerException if error occurred when adding rss instance
	 */
	void addRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws RSSManagerException;

	/**
	 * Remove RSS Instance from system
	 *
	 * @param environmentName environment name which RSS instance belongs
	 * @param rssInstanceName name of RSS Instance to be removed
	 * @param type            RSS Instance type
	 * @throws RSSManagerException if error occurred removing rss instance
	 */
	void removeRSSInstance(String environmentName, String rssInstanceName, String type)
			throws RSSManagerException;

	/**
	 * Update RSS Instance
	 *
	 * @param environmentName environment name which RSS instance belongs
	 * @param rssInstance     RSS Instance updated properties
	 * @throws RSSManagerException if error occurred updating rss instance
	 */
	void updateRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws RSSManagerException;

	/**
	 * Get details of a RSS instance
	 *
	 * @param environmentName the environment name which RSS instance belongs
	 * @param rssInstanceName name of RSS Instance
	 * @param type            RSS Instance type
	 * @return matching RSS Instance for the given rssInstanceName
	 * @throws RSSManagerException if error occurred when getting rss instance
	 */
	RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName, String type)
			throws RSSManagerException;

	/**
	 * Get available RSS Instance of a environment
	 *
	 * @param environmentName the environment name which RSS instances to be retrieve
	 * @return RSSInstanceInfo list
	 * @throws RSSManagerException if error occurred when getting rss instance
	 */
	RSSInstanceInfo[] getRSSInstances(String environmentName) throws RSSManagerException;

	/**
	 * Get all RSS Instances across the environments
	 *
	 * @return RSSInstanceInfo list
	 * @throws RSSManagerException if error occurred when getting rss instance list
	 */
	RSSInstanceInfo[] getRSSInstancesList() throws RSSManagerException;

	/**
	 * Add new database
	 *
	 * @param environmentName name of the environment which database needs to be added to
	 * @param database        database properties
	 * @return created database info
	 * @throws RSSManagerException if error occurred when adding database
	 */
	DatabaseInfo addDatabase(String environmentName, DatabaseInfo database) throws RSSManagerException;

	/**
	 * Remove database
	 *
	 * @param environmentName name of the environment which database will be deleted from
	 * @param rssInstanceName rssInstance of database which it belongs
	 * @param databaseName    name of the database
	 * @param type            RSS Instance type
	 * @throws RSSManagerException if error occurred removing database
	 */
	void removeDatabase(String environmentName, String rssInstanceName, String databaseName, String type)
			throws RSSManagerException;

	/**
	 * Get all databases of a environment
	 *
	 * @param environmentName the environment name which databases needs to be retrieve
	 * @return database info array
	 * @throws RSSManagerException if error occurred when getting databases
	 */
	DatabaseInfo[] getDatabases(String environmentName) throws RSSManagerException;

	/**
	 * Get database info of a given database
	 *
	 * @param environmentName the environment name which database belongs
	 * @param rssInstanceName the rssInstanceName which database belongs
	 * @param databaseName    name of the database
	 * @param type            RSS Instance type
	 * @return DatabaseInfo
	 * @throws RSSManagerException if error occurred when getting database
	 */
	DatabaseInfo getDatabase(String environmentName, String rssInstanceName, String databaseName, String type)
			throws RSSManagerException;

	/**
	 * Check whether database is exist
	 *
	 * @param environmentName the environment name which database belongs
	 * @param rssInstanceName the rssInstanceName which database belongs
	 * @param databaseName    name of the database
	 * @param type            RSS Instance type
	 * @return true if database exist else false
	 * @throws RSSManagerException if error occurred when checking database existence
	 */
	boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName, String type)
			throws RSSManagerException;

	/**
	 * @param environmentName the environment name which database user belongs
	 * @param rssInstanceName the rssInstanceName which database user belongs
	 * @param username        the username of the database user
	 * @param type            RSS Instance type
	 * @return boolean true if database user exist else false
	 * @throws RSSManagerException if error occurred when checking database user existence
	 */
	boolean isDatabaseUserExist(String environmentName, String rssInstanceName, String username, String type)
			throws RSSManagerException;

	/**
	 * Add Database user
	 *
	 * @param environmentName name of the environment which database user needs to be added to
	 * @param user            User Obj with required properties
	 * @return DatabaseUserInfo
	 * @throws RSSManagerException true f error occurred when adding database user
	 */
	DatabaseUserInfo addDatabaseUser(String environmentName, DatabaseUserInfo user) throws RSSManagerException;

	/**
	 * Remove database user
	 *
	 * @param environmentName name of the environment which database user belongs
	 * @param rssInstanceName name of the RSS Instance which database user belongs
	 * @param username        the username of the database user
	 * @param type            RSS Instance type
	 * @throws RSSManagerException if error occurred when removing database user
	 */
	void removeDatabaseUser(String environmentName, String rssInstanceName, String username, String type)
			throws RSSManagerException;

	/**
	 * Update database user privileges
	 *
	 * @param environmentName name of the environment which database user and database resides
	 * @param privileges      updated database user privileges
	 * @param user            database user Obj with properties
	 * @param databaseName    name of the database
	 * @throws RSSManagerException if error occurred when updating user privileges
	 */
	void updateDatabaseUserPrivileges(String environmentName, DatabasePrivilegeSetInfo privileges,
	                                  DatabaseUserInfo user, String databaseName) throws RSSManagerException;

	/**
	 * Get database user properties
	 *
	 * @param environmentName name of the environment which database user belongs
	 * @param rssInstanceName name of the RSS Instance which database user belongs
	 * @param username        the username of the database user
	 * @param type            RSS Instance type
	 * @return DatabaseUserInfo object
	 * @throws RSSManagerException if error occurred when getting database user
	 */
	DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstanceName, String username, String type)
			throws RSSManagerException;

	/**
	 * Get all database users in a environment
	 *
	 * @param environmentName the name of the environment
	 * @return DatabaseUserInfo array
	 * @throws RSSManagerException if error occurred when getting database users
	 */
	DatabaseUserInfo[] getDatabaseUsers(String environmentName) throws RSSManagerException;

	/**
	 * Attach database user to a database
	 *
	 * @param environmentName name of the environment which database user and database resides
	 * @param ude             user and database privileges template properties in Obj
	 * @param templateName    database template name
	 * @throws RSSManagerException if error occurred when attaching database user
	 */
	void attachUser(String environmentName, UserDatabaseEntryInfo ude, String templateName)
			throws RSSManagerException;

	/**
	 * Deattach user from the database
	 *
	 * @param environmentName name of the environment which database user resides
	 * @param databaseEntryInfo             user and database privileges template properties in Obj
	 * @throws RSSManagerException if error occurred when de-attaching database user
	 */
	void detachUser(String environmentName, UserDatabaseEntryInfo databaseEntryInfo) throws RSSManagerException;

	/**
	 * Get users attached to the database
	 *
	 * @param environmentName name of the environment which database resides
	 * @param rssInstanceName name of the RSS Instance which database belongs
	 * @param databaseName    name of the database
	 * @param type            RSS Instance type
	 * @return DatabaseUserInfo array
	 * @throws RSSManagerException if error occurred when getting attached database users
	 */
	DatabaseUserInfo[] getAttachedUsers(String environmentName, String rssInstanceName, String databaseName,
	                                    String type) throws RSSManagerException;

	/**
	 * Get available users remain to attach to the database. This will return the available database users which can
	 * attach to the given database
	 *
	 * @param environmentName name of the environment which database resides
	 * @param rssInstanceName name of the RSS Instance which database belongs
	 * @param databaseName    name of the database
	 * @param type            RSS Instance type
	 * @return DatabaseUserInfo array
	 * @throws RSSManagerException if error occurred when getting available database users
	 */
	DatabaseUserInfo[] getAvailableUsers(String environmentName, String rssInstanceName, String databaseName,
	                                     String type) throws RSSManagerException;

	/**
	 * Get database user privileges of a given database user
	 *
	 * @param environmentName name of the environment which database user resides
	 * @param rssInstanceName name of the RSS Instance which database user belongs
	 * @param databaseName    name of the database
	 * @param username        the username of the database user
	 * @param type            RSS Instance type
	 * @return DatabasePrivilegeSetInfo
	 * @throws RSSManagerException if error occurred when getting database user privileges
	 */
	DatabasePrivilegeSetInfo getUserDatabasePrivileges(String environmentName, String rssInstanceName,
	                                                   String databaseName, String username, String type)
			throws RSSManagerException;

	/**
	 * Get all the databases under the tenant
	 *
	 * @param environmentName name of the environment which database resides
	 * @param tenantDomain    tenant domain of which needs to query the available databases
	 * @return List<DatabaseInfo>
	 * @throws RSSManagerException if error occurred when getting databases for tenant
	 */
	DatabaseInfo[] getDatabasesForTenant(String environmentName, String tenantDomain)
			throws RSSManagerException;

	/**
	 * Add database for a tenant
	 *
	 * @param environmentName the name of the environment
	 * @param database        database properties
	 * @param tenantDomain    name of the tenant domain
	 * @throws RSSManagerException if error occurred when adding databases for tenant
	 */
	void addDatabaseForTenant(String environmentName, DatabaseInfo database, String tenantDomain)
			throws RSSManagerException;

	/**
	 * Get tenant database
	 *
	 * @param environmentName the name of the environment
	 * @param rssInstanceName name of RSS Instance
	 * @param databaseName    name of the database
	 * @param tenantDomain    name of the tenant domain
	 * @param type            RSS Instance type
	 * @return DatabaseInfo
	 * @throws RSSManagerException if error occurred when adding databases for tenant
	 */
	DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName, String databaseName,
	                                  String tenantDomain, String type) throws RSSManagerException;

	/**
	 * Check whether database privilege template is exist
	 *
	 * @param environmentName name of the environment which database privilege template resides
	 * @param templateName    name of the privilege template
	 * @return boolean
	 * @throws RSSManagerException if error occurred when checking privilege template existence
	 */
	boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
			throws RSSManagerException;

	/**
	 * Delete RSS Data related a tenant
	 *
	 * @param environmentName name of the environment which RSS Data needs to be deleted
	 * @param tenantDomain    name of the tenant domain
	 * @return boolean
	 * @throws RSSManagerException if error occurred when deleting tenant rss data
	 */
	boolean deleteTenantRSSData(String environmentName, String tenantDomain)
			throws RSSManagerException;

	/**
	 * Add database privilege template
	 *
	 * @param environmentName name of the environment which database privilege template needs to be added to
	 * @param template        template properties
	 * @throws RSSManagerException if error occurred when adding database privilege template
	 */
	void addDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
			throws RSSManagerException;

	/**
	 * Remove database privilege template
	 *
	 * @param environmentName name of the environment which database privilege template resides
	 * @param templateName    name of the privilege template
	 * @throws RSSManagerException if error occurred when removing database privilege template
	 */
	void removeDatabasePrivilegeTemplate(String environmentName, String templateName)
			throws RSSManagerException;

	/**
	 * Update database privilege template
	 *
	 * @param environmentName name of the environment which database privilege template resides
	 * @param template        updated template properties
	 * @throws RSSManagerException if error occurred when updating database privilege template
	 */
	void updateDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
			throws RSSManagerException;

	/**
	 * Get database privilege templates of given environment
	 *
	 * @param environmentName the name of the environment
	 * @return DatabasePrivilegeTemplateInfo
	 * @throws RSSManagerException if error occurred when getting database privilege templates
	 */
	DatabasePrivilegeTemplateInfo[] getDatabasePrivilegeTemplates(String environmentName)
			throws RSSManagerException;

	/**
	 * Get properties of a given database template
	 *
	 * @param environmentName name of the environment which database privilege template resides
	 * @param templateName    name of the privilege template
	 * @return DatabasePrivilegeTemplateInfo
	 * @throws RSSManagerException if error occurred when getting database privilege templates
	 */
	DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(String environmentName, String templateName)
			throws RSSManagerException;

	/**
	 * Add carbon data source
	 *
	 * @param environmentName the name of the environment
	 * @param dataSourceName  name of the data source
	 * @param entry           data source properties
	 * @throws RSSManagerException if error occurred when adding carbon data source
	 */
	void addCarbonDataSource(String environmentName, String dataSourceName, UserDatabaseEntryInfo entry) throws RSSManagerException;

	/**
	 * Get all available environments
	 *
	 * @return String[]
	 * @throws RSSManagerException if error occurred when getting environments
	 */
	String[] getEnvironments() throws RSSManagerException;

	/**
	 * Edit database user
	 *
	 * @param environmentName  the environment name which database user belongs
	 * @param databaseUserInfo database user properties
	 * @return DatabaseUserInfo
	 * @throws RSSManagerException if error occurred when getting editing database user
	 */
	DatabaseUserInfo editDatabaseUser(String environmentName, DatabaseUserInfo databaseUserInfo) throws RSSManagerException;

	/**
	 * Get RSS provider of the system
	 *
	 * @return String
	 */
	String getRSSProvider();
}