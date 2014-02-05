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

	void addRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws RSSManagerException;

	void removeRSSInstance(String environmentName, String rssInstanceName, String type)
	                                                                                   throws RSSManagerException;

	void updateRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws RSSManagerException;

	RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName, String type)
	                                                                                       throws RSSManagerException;

	RSSInstanceInfo[] getRSSInstances(String environmentName) throws RSSManagerException;

	DatabaseInfo addDatabase(String environmentName, DatabaseInfo database) throws RSSManagerException;

	void removeDatabase(String environmentName, String rssInstanceName, String databaseName, String type)
	                                                                                                     throws RSSManagerException;

	DatabaseInfo[] getDatabases(String environmentName) throws RSSManagerException;

	DatabaseInfo getDatabase(String environmentName, String rssInstanceName, String databaseName, String type)
	                                                                                                      throws RSSManagerException;

	boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName, String type)
	                                                                                                         throws RSSManagerException;

	boolean isDatabaseUserExist(String environmentName, String rssInstanceName, String username, String type)
	                                                                                                         throws RSSManagerException;

	DatabaseUserInfo addDatabaseUser(String environmentName, DatabaseUserInfo user) throws RSSManagerException;

	void removeDatabaseUser(String environmentName, String rssInstanceName, String username, String type)
	                                                                                                     throws RSSManagerException;

	void updateDatabaseUserPrivileges(String environmentName, DatabasePrivilegeSetInfo privileges,
	                                  DatabaseUserInfo user, String databaseName) throws RSSManagerException;

	DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstanceName, String username, String type)
	                                                                                                          throws RSSManagerException;

	DatabaseUserInfo[] getDatabaseUsers(String environmentName) throws RSSManagerException;

	void attachUser(String environmentName, UserDatabaseEntryInfo ude, String templateName)
	                                                                                   throws RSSManagerException;

	void detachUser(String environmentName, UserDatabaseEntryInfo ude) throws RSSManagerException;

	DatabaseUserInfo[] getAttachedUsers(String environmentName, String rssInstanceName, String databaseName,
	                                String type) throws RSSManagerException;

	DatabaseUserInfo[] getAvailableUsers(String environmentName, String rssInstanceName, String databaseName,
	                                 String type) throws RSSManagerException;

	DatabasePrivilegeSetInfo getUserDatabasePrivileges(String environmentName, String rssInstanceName,
	                                               String databaseName, String username, String type)
	                                                                                                 throws RSSManagerException;

	DatabaseInfo[] getDatabasesForTenant(String environmentName, String tenantDomain)
	                                                                                          throws RSSManagerException;

	void addDatabaseForTenant(String environmentName, DatabaseInfo database, String tenantDomain)
	                                                                                         throws RSSManagerException;

	DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName, String databaseName,
	                              String tenantDomain, String type) throws RSSManagerException;

	boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
	                                                                                                  throws RSSManagerException;

	boolean deleteTenantRSSData(String environmentName, String tenantDomain)
	                                                                                     throws RSSManagerException;

	void addDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                             throws RSSManagerException;

	void removeDatabasePrivilegeTemplate(String environmentName, String templateName)
	                                                                                 throws RSSManagerException;

	void updateDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                                throws RSSManagerException;

	DatabasePrivilegeTemplateInfo[] getDatabasePrivilegeTemplates(String environmentName)
	                                                                                 throws RSSManagerException;

	DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(String environmentName, String templateName)
	                                                                                                   throws RSSManagerException;

	void addCarbonDataSource(String environmentName, UserDatabaseEntryInfo entry) throws RSSManagerException;

	String[] getEnvironments() throws RSSManagerException;

}