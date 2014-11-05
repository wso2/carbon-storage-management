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

package org.wso2.carbon.rssmanager.core.environment.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.environment.Environment;

import java.util.Set;

public interface EnvironmentDAO {

	/**
	 * Add new environment
	 * @param environment configuration
	 * @throws RSSDAOException if error occurred while adding new environment
	 */
	void addEnvironment(Environment environment) throws RSSDAOException;

	/**
	 * Remove environment
	 * @param environmentName name of the environment
	 * @throws RSSDAOException if error occurred while removing the environment
	 */
	@Deprecated
	void removeEnvironment(String environmentName) throws RSSDAOException;

	/**
	 * Check whether environment is exist
	 *
	 * @param environmentName name of the environment
	 * @return true if environment found else false
	 * @throws RSSDAOException if error occurred while checking the existence of the environment
	 */
	boolean isEnvironmentExist(String environmentName) throws RSSDAOException;

	/**
	 * Get environment by name
	 *
	 * @param environmentName name of the environment
	 * @return environment object
	 * @throws RSSDAOException if error occureed when getting the environment
	 */
	Environment getEnvironment(String environmentName) throws RSSDAOException;

	/**
	 * Get all environments
	 *
	 * @return set of environments in the system
	 * @throws RSSDAOException if error occurred while getting all environments
	 */
	Set<Environment> getAllEnvironments() throws RSSDAOException;

}
