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

import org.wso2.carbon.rssmanager.core.environment.DatabasePrivilegeTemplateEntryDAO;

/**
 * Environment management DAO interface
 */
public interface EnvironmentManagementDAO {

	/**
	 * Get environment DAO implementation
	 * @return EnvironmentDAO
	 */
	EnvironmentDAO getEnvironmentDAO();

	/**
	 * Get rss instance DAO implementation
	 * @return RSSInstanceDAO
	 */
	RSSInstanceDAO getRSSInstanceDAO();

	/**
	 * Get database privilege template DAO implementation
	 * @return DatabasePrivilegeTemplateDAO
	 */
	DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO();

	/**
	 * Get database privilege template entry DAO implementation
	 * @return DatabasePrivilegeTemplateEntryDAO
	 */
	DatabasePrivilegeTemplateEntryDAO getDatabasePrivilegeTemplateEntryDAO();

}
