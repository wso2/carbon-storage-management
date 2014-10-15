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
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;

import java.sql.PreparedStatement;

/**
 * DatabaseDAO interface class
 */
public interface DatabaseDAO {

	/**
	 * Method to add database information to the RSS meta data repository. This method takes an argument of native
	 * add database prepared statement which needs to be executed along with the meta repository insert as native
	 * sql operations not transactional
	 *
	 * @param nativeAddDBStatement native add database statement which needs to be executed
	 * @param database Database configuration
	 * @throws RSSDAOException If some error occurs while adding database configuration
	 *                         information to RSS meta data repository
	 */
	void addDatabase(PreparedStatement nativeAddDBStatement, Database database) throws RSSDAOException;

	/**
	 * Method to remove database configuration information from RSS metadata repository. This method takes an argument of native
	 * remove database prepared statement which needs to be executed along with the meta repository database entry removal as native
	 * sql operations not transactional
	 *
	 * @param nativeRemoveDBStatement native remove database statement to be executed
	 * @param database database object that needs to be removed
	 * @throws RSSDAOException If some error occurs while removing database configuration
	 *                         information from RSS meta data repository
	 */
	void removeDatabase(PreparedStatement nativeRemoveDBStatement, Database database) throws RSSDAOException;

	/**
	 * Check database existence
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName name of the database
	 * @param tenantId tenant id of the database owner which needs to check existence
	 * @param instanceType instance type of the database
	 * @return boolean true if database exist
	 * @throws RSSDAOException if some error occurred during checking the existence
	 */
	 boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Method to get configuration information of a particular database.
	 *
	 * @param environmentName name of the RSS environment
	 * @param databaseName    name of the database
	 * @param rssInstanceName name of the rss instance
	 * @param tenantId        tenant id of the owner of the database
	 * @return Configuration information of the requested database
	 * @throws RSSDAOException If some error occurs while retrieving the configuration
	 *                         information of a given database
	 */
	Database getDatabase(String environmentName, String rssInstanceName, String databaseName, int tenantId, String instanceType)
			throws RSSDAOException;

	/**
	 * Method to get configuration information of a particular database without specifying rss instance name
	 *
	 * @param environmentName name of the RSS environment
	 * @param databaseName    name of the database
	 * @param tenantId        tenant id of the owner of the database
	 * @return Configuration information of the requested database
	 * @throws RSSDAOException If some error occurs while retrieving the configuration
	 *                         information of a given database
	 */
	Database getDatabase(String environmentName, String databaseName, int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Method to retrieve all the database configurations belong to a particular tenant in environment for given instance type
	 *
	 * @param environmentName name of the RSS environment
	 * @param tenantId        tenant id
	 * @param instanceType    instance type
	 * @return Array of database configurations belong to a tenant
	 * @throws RSSDAOException If some error occurs while retrieving the configurations of
	 *                         the databases belong to a tenant in given instance type and environment
	 */
	public Database[] getDatabases(String environmentName, int tenantId, String instanceType) throws RSSDAOException;

	/**
	 * Method to retrieve all the database configurations belong to a particular tenant in environment
	 *
	 * @param environmentName Name of the RSS environment
	 * @param tenantId        Tenant ID
	 * @return Array of database configurations belong to a tenant
	 * @throws RSSDAOException If some error occurs while retrieving the configurations of
	 *                         the databases belong to a tenant in environment
	 */
	Database[] getAllDatabases(String environmentName, int tenantId) throws RSSDAOException;

	/**
	 * Resolve rss instance name by database
	 * @param environmentName name of the environment
	 * @param databaseName name of the database
	 * @param type database instance type
	 * @param tenantId tenant id of the database owner
	 * @return name of the rss instance that the specified database belongs
	 * @throws RSSDAOException If some error occurs while resolving the rss instance name by database
	 */
	public String resolveRSSInstanceNameByDatabase(String environmentName,
	                                               String databaseName, String type,
	                                               int tenantId) throws RSSDAOException;


}
