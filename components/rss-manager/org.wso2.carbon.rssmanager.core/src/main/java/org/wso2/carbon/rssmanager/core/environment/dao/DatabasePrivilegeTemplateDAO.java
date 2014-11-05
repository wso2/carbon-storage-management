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
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface DatabasePrivilegeTemplateDAO {

	/**
	 * Add database privilege template
	 *
	 * @param databasePrivilegeTemplate privilege template object with privileges
	 * @param environmentId environment id
	 * @throws RSSDAOException if error occur when adding database privilege template
	 */
	void addDatabasePrivilegeTemplate(DatabasePrivilegeTemplate databasePrivilegeTemplate, int environmentId) throws RSSDAOException;

	/**
	 * Get database privilege template
	 *
	 * @param environmentId environment id
	 * @param name privilege template name
	 * @param tenantId tenant id of template owner
	 * @return database privilege template object
	 * @throws RSSDAOException if error occur when getting database privilege template
	 */
	DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(int environmentId, String name,
	                                                        int tenantId) throws RSSDAOException;

	/**
	 * Get database privilege templates of environment
	 * @param environmentId environment id
	 * @param tenantId tenant id of template owner
	 * @return database privilege template object
	 * @throws RSSDAOException if error occur when getting database privilege templates
	 */
	DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
			int environmentId, int tenantId) throws RSSDAOException;

	/**
	 * Check the exitence of a privilege template
	 *
	 * @param environmentId environment id
	 * @param templateName name of the template
	 * @param tenantId tenant id of template owner
	 * @return true if matching database template found else false
	 * @throws RSSDAOException if error occur when checking existence of database privilege template
	 */
	boolean isDatabasePrivilegeTemplateExist(int environmentId, String templateName,
	                                         int tenantId) throws RSSDAOException;

	/**
	 * Remove dataabase privilege template
	 *
	 * @param environmentId environment id
	 * @param templateName name of the template
	 * @param tenantId tenant id of template owner
	 * @throws RSSDAOException if error occur when removing database privilege template
	 */
	void removeDatabasePrivilegeTemplate(int environmentId, String templateName,
	                                     int tenantId) throws RSSDAOException;
}
