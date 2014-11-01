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

package org.wso2.carbon.rssmanager.core.environment.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;

/**
 * RSS instance DAO interface
 */
public interface RSSInstanceDAO {

	/**
	 * Add new rss instance to environment
	 *
	 * @param environmentName name of the environment
	 * @param instance rss instance configuration
	 * @param tenantId tenant id
	 * @throws RSSDAOException if error occurred when add new rss instance
	 */
	void addRSSInstance(String environmentName, RSSInstance instance,
	                    int tenantId) throws RSSDAOException;

	/**
	 * Remove rss instance
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param tenantId tenant id
	 * @throws RSSDAOException if error occurred when removing the rss instance
	 */
	void removeRSSInstance(String environmentName, String rssInstanceName,
	                       int tenantId) throws RSSDAOException;

	/**
	 * Update the rss instance
	 * @param environmentName name of the environment
	 * @param instance rss instance configuration
	 * @param tenantId tenant id
	 * @throws RSSDAOException if error occurred when updating the rss instance
	 */
	void updateRSSInstance(String environmentName, RSSInstance instance,
	                       int tenantId) throws RSSDAOException;

	/**
	 * Check whether rss instance is exist
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName rss instance name
	 * @return true of matching rss instance found else false
	 * @throws RSSDAOException if error occurred when checking rss instance existence
	 */
	boolean isRSSInstanceExist(String environmentName, String rssInstanceName) throws RSSDAOException;

	/**
	 * Get rss instance by name
	 * @param environmentName name of the environment
	 * @param rssInstanceName name of the rss instance
	 * @param tenantId tenant id
	 * @return rss instance object
	 * @throws RSSDAOException if error occurred getting rss instance
	 */
	RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
	                           int tenantId) throws RSSDAOException;

	/**
	 * Get system rss instances of environment
	 *
	 * @param environmentName name of the environment
	 * @param tenantId tenant id
	 * @return array of system rss instances
	 * @throws RSSDAOException if error occurred when getting system rss instances
	 */
	RSSInstance[] getSystemRSSInstances(String environmentName, int tenantId) throws RSSDAOException;

	/**
	 * Get user defined rss instances of environment
	 *
	 * @param environmentName name of the environment
	 * @param tenantId tenant id
	 * @return array of user defined rss instances
	 * @throws RSSDAOException if error occurred when getting user defined rss instances
	 */
	RSSInstance[] getUserDefinedRSSInstances(String environmentName, int tenantId) throws RSSDAOException;

	/**
	 * Get all user defined rss instances
	 * @param tenantId tenant id
	 * @return array of user defined rss instances
	 * @throws RSSDAOException if error occurred when getting user defined rss instances
	 */
	RSSInstance[] getUserDefinedRSSInstances(int tenantId) throws RSSDAOException;

	/**
	 * Get all system rss instances
	 * @param tenantId tenant id
	 * @return array of system rss instances
	 * @throws RSSDAOException if error occurred when getting system rss instances
	 */
	RSSInstance[] getSystemRSSInstances(int tenantId) throws RSSDAOException;

	/**
	 * Get all rss instances of a given tenant
	 * @param environmentName name of the environment
	 * @param tenantId tenant id
	 * @return array of rss instances
	 * @throws RSSDAOException if error occurred when getting rss instances
	 */
	RSSInstance[] getRSSInstances(String environmentName, int tenantId) throws RSSDAOException;

	/**
	 * Get all rss isntances in environment
	 *
	 * @param environmentName name of the environment
	 * @return array of rss instances
	 * @throws RSSDAOException if error occurred when getting rss instances
	 */
	RSSInstance[] getAllRSSInstancesOfEnvironment(String environmentName) throws RSSDAOException;

}
