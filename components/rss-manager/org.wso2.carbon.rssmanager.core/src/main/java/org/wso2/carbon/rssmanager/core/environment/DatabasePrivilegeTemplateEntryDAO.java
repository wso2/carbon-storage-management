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

package org.wso2.carbon.rssmanager.core.environment;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;

public interface DatabasePrivilegeTemplateEntryDAO {

	/**
	 * Add privilege template entry
	 *
	 * @param environmentId environment id
	 * @param templateId database privilege template id
	 * @param entry privilege template entry with privileges
	 * @throws RSSDAOException if error occur while adding database privilege template entry
	 */
	void addPrivilegeTemplateEntry(int environmentId, int templateId, DatabasePrivilegeTemplateEntry entry) throws RSSDAOException;

	/**
	 * Get privilege template entry
	 *
	 * @param templateId database privilege template id
	 * @return database privilege template entry object
	 * @throws RSSDAOException if error occurred when getting privilege template entry
	 */
	DatabasePrivilegeTemplateEntry getPrivilegeTemplateEntry(int templateId) throws RSSDAOException;

	/**
	 * update database privilege template entry
	 *
	 * @param environmentId the environment id
	 * @param templateId database privilege template id
	 * @param updatedEntry  privilege template entry with updated privileges
	 * @throws RSSDAOException if error occurred when updating privilege template entry
	 */
	void updatePrivilegeTemplateEntry(int environmentId, int templateId, DatabasePrivilegeTemplateEntry updatedEntry) throws RSSDAOException;
}
