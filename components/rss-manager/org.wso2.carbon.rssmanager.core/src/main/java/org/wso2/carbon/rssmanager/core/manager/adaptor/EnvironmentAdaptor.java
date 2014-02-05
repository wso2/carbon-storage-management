/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.adaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo;
import org.wso2.carbon.rssmanager.core.dto.MySQLPrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.EnvironmentManager;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class EnvironmentAdaptor implements RSSManagerService {

	private EnvironmentManager environmentManager;

	public EnvironmentAdaptor(EnvironmentManager environmentManager) {
		this.environmentManager = environmentManager;
	}

	public void addRSSInstance(String environmentName, RSSInstanceInfo rssInstance)
	                                                                               throws RSSManagerException {
		RSSInstance entity = new RSSInstance();
		RSSManagerUtil.createRSSInstance(rssInstance, entity);
		this.getEnvironmentManager().addRSSInstance(entity);
	}

	public void removeRSSInstance(String environmentName, String rssInstanceName, String type)
	                                                                                          throws RSSManagerException {
		this.getEnvironmentManager().removeRSSInstance(environmentName, rssInstanceName);
	}

	public void updateRSSInstance(String environmentName, RSSInstanceInfo rssInstance)
	                                                                                  throws RSSManagerException {
		RSSInstance entity = new RSSInstance();
		RSSManagerUtil.createRSSInstance(rssInstance, entity);
		this.getEnvironmentManager().updateRSSInstance(environmentName, entity);
	}

	public RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName, String type)
	                                                                                                  throws RSSManagerException {
		RSSInstance entity = this.getEnvironmentManager().getRSSInstance(environmentName, rssInstanceName);
		RSSInstanceInfo info = new RSSInstanceInfo();
		RSSManagerUtil.createRSSInstanceInfo(info, entity);
		return info;
	}

	public RSSInstanceInfo[] getRSSInstances(String environmentName) throws RSSManagerException {
		RSSInstance[] entities = this.getEnvironmentManager().getRSSInstances(environmentName);
		List<RSSInstance> entityList = Arrays.asList(entities);
		List<RSSInstanceInfo> infoList = new ArrayList<RSSInstanceInfo>();
		for (RSSInstance entity : entityList) {
			RSSInstanceInfo info = new RSSInstanceInfo();
			RSSManagerUtil.createRSSInstanceInfo(info, entity);
			infoList.add(info);
		}
		return infoList.toArray(new RSSInstanceInfo[infoList.size()]);
	}

	public DatabaseInfo addDatabase(String environmentName, DatabaseInfo database) throws RSSManagerException {
		Database entity = new Database();
		RSSManagerUtil.createDatabase(database, entity);
		Database returnEntity = this.getRSSManagerAdaptor(environmentName).addDatabase(entity);
		RSSManagerUtil.createDatabaseInfo(database, returnEntity);
		return database;
	}

	public void removeDatabase(String environmentName, String rssInstanceName, String databaseName,
	                           String type) throws RSSManagerException {
		this.getRSSManagerAdaptor(environmentName).removeDatabase(rssInstanceName, databaseName, type);
	}

	public DatabaseInfo[] getDatabases(String environmentName) throws RSSManagerException {

		Database[] entities = this.getRSSManagerAdaptor(environmentName).getDatabases();
		List<Database> entityList = Arrays.asList(entities);
		List<DatabaseInfo> infoList = new ArrayList<DatabaseInfo>();
		for (Database entity : entityList) {
			DatabaseInfo info = new DatabaseInfo();
			RSSManagerUtil.createDatabaseInfo(info, entity);
			infoList.add(info);
		}

		return infoList.toArray(new DatabaseInfo[infoList.size()]);
	}

	public DatabaseInfo getDatabase(String environmentName, String rssInstanceName, String databaseName,
	                                String type) throws RSSManagerException {

		Database entity = this.getRSSManagerAdaptor(environmentName).getDatabase(rssInstanceName,
		                                                                         databaseName, type);
		DatabaseInfo info = new DatabaseInfo();
		RSSManagerUtil.createDatabaseInfo(info, entity);
		return info;
	}

	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               String type) throws RSSManagerException {
		return this.getRSSManagerAdaptor(environmentName)
		           .isDatabaseExist(rssInstanceName, databaseName, type);
	}

	public boolean isDatabaseUserExist(String environmentName, String rssInstanceName, String username,
	                                   String type) throws RSSManagerException {
		return this.getRSSManagerAdaptor(environmentName)
		           .isDatabaseUserExist(rssInstanceName, username, type);
	}

	public DatabaseUserInfo addDatabaseUser(String environmentName, DatabaseUserInfo user)
	                                                                                      throws RSSManagerException {
		DatabaseUser entity = new DatabaseUser();
		RSSManagerUtil.createDatabaseUser(user, entity);
		entity = this.getRSSManagerAdaptor(environmentName).addDatabaseUser(entity);
		RSSManagerUtil.createDatabaseUserInfo(user, entity);
		return user;
	}

	public void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
	                               String type) throws RSSManagerException {
		this.getRSSManagerAdaptor(environmentName).removeDatabaseUser(rssInstanceName, username, type);
	}

	public void updateDatabaseUserPrivileges(String environmentName, DatabasePrivilegeSetInfo privileges,
	                                         DatabaseUserInfo user, String databaseName)
	                                                                                    throws RSSManagerException {
		DatabaseUser entityUser = new DatabaseUser();
		RSSManagerUtil.createDatabaseUser(user, entityUser);
		DatabasePrivilegeSet entitySet = new MySQLPrivilegeSet();
		RSSManagerUtil.createDatabasePrivilegeSet(privileges, entitySet);
		this.getRSSManagerAdaptor(environmentName).updateDatabaseUserPrivileges(entitySet, entityUser,
		                                                                        databaseName);
	}

	public DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstanceName, String username,
	                                        String type) throws RSSManagerException {
		DatabaseUser entity = this.getRSSManagerAdaptor(environmentName).getDatabaseUser(rssInstanceName,
		                                                                                 username, type);
		DatabaseUserInfo info = new DatabaseUserInfo();
		RSSManagerUtil.createDatabaseUserInfo(info, entity);
		return info;
	}

	public DatabaseUserInfo[] getDatabaseUsers(String environmentName) throws RSSManagerException {

		DatabaseUser[] entities = this.getRSSManagerAdaptor(environmentName).getDatabaseUsers();
		List<DatabaseUser> entityList = Arrays.asList(entities);
		List<DatabaseUserInfo> infoList = new ArrayList<DatabaseUserInfo>();
		for (DatabaseUser entity : entityList) {
			DatabaseUserInfo info = new DatabaseUserInfo();
			RSSManagerUtil.createDatabaseUserInfo(info, entity);
			infoList.add(info);
		}

		return infoList.toArray(new DatabaseUserInfo[infoList.size()]);
	}

	public void attachUser(String environmentName, UserDatabaseEntryInfo ude, String templateName)
	                                                                                              throws RSSManagerException {
		// TODO fix this with a proper DatabasePrivilegeTemplate
		DatabasePrivilegeTemplate entity = this.getEnvironmentManager()
		                                       .getDatabasePrivilegeTemplate(environmentName, templateName);
		DatabasePrivilegeTemplateEntry entry = entity.getEntry();
		UserDatabaseEntry userEntity = new UserDatabaseEntry();
		RSSManagerUtil.createDatabaseUserEntry(ude, userEntity);
		this.getRSSManagerAdaptor(environmentName).attachUser(userEntity, entry);
	}

	public void detachUser(String environmentName, UserDatabaseEntryInfo ude) throws RSSManagerException {
		UserDatabaseEntry entity = new UserDatabaseEntry();
		RSSManagerUtil.createDatabaseUserEntry(ude, entity);
		this.getRSSManagerAdaptor(environmentName).detachUser(entity);
	}

	public DatabaseUserInfo[] getAttachedUsers(String environmentName, String rssInstanceName,
	                                           String databaseName, String type) throws RSSManagerException {

		DatabaseUser[] entities = this.getRSSManagerAdaptor(environmentName)
		                              .getAttachedUsers(rssInstanceName, databaseName, type);
		List<DatabaseUser> entityList = Arrays.asList(entities);
		List<DatabaseUserInfo> infoList = new ArrayList<DatabaseUserInfo>();
		for (DatabaseUser entity : entityList) {
			DatabaseUserInfo info = new DatabaseUserInfo();
			RSSManagerUtil.createDatabaseUserInfo(info, entity);
			infoList.add(info);
		}

		return infoList.toArray(new DatabaseUserInfo[infoList.size()]);
	}

	public DatabaseUserInfo[] getAvailableUsers(String environmentName, String rssInstanceName,
	                                            String databaseName, String type) throws RSSManagerException {
		DatabaseUser[] entities = this.getRSSManagerAdaptor(environmentName)
		                              .getAvailableUsers(rssInstanceName, databaseName, type);
		List<DatabaseUser> entityList = Arrays.asList(entities);
		List<DatabaseUserInfo> infoList = new ArrayList<DatabaseUserInfo>();
		for (DatabaseUser entity : entityList) {
			DatabaseUserInfo info = new DatabaseUserInfo();
			RSSManagerUtil.createDatabaseUserInfo(info, entity);
			infoList.add(info);
		}

		return infoList.toArray(new DatabaseUserInfo[infoList.size()]);
	}

	public DatabasePrivilegeSetInfo getUserDatabasePrivileges(String environmentName, String rssInstanceName,
	                                                          String databaseName, String username,
	                                                          String type) throws RSSManagerException {

		DatabasePrivilegeSet entity = this.getRSSManagerAdaptor(environmentName)
		                                  .getUserDatabasePrivileges(rssInstanceName, databaseName, username,
		                                                             type);
		DatabasePrivilegeSetInfo info = new MySQLPrivilegeSetInfo();
		RSSManagerUtil.createDatabasePrivilegeSetInfo(info, entity);
		return info;
	}

	public DatabaseInfo[] getDatabasesForTenant(String environmentName, String tenantDomain)
	                                                                                        throws RSSManagerException {
		return new DatabaseInfo[0];
	}

	public void addDatabaseForTenant(String environmentName, DatabaseInfo database, String tenantDomain)
	                                                                                                    throws RSSManagerException {

	}

	public DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName,
	                                         String databaseName, String tenantDomain, String type)
	                                                                                               throws RSSManagerException {
		return null;
	}

	public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
	                                                                                            throws RSSManagerException {
		return this.getEnvironmentManager().isDatabasePrivilegeTemplateExist(environmentName, templateName);
	}

	public boolean deleteTenantRSSData(String environmentName, String tenantDomain)
	                                                                               throws RSSManagerException {
		// try {
		// PrivilegedCarbonContext.startTenantFlow();
		// PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
		// tenantId);
		// return this.getRSSManager(environmentName).deleteTenantRSSData();
		// } catch (RSSManagerException e) {
		// String msg =
		// "Error occurred while deleting RSS tenant data tenantId '"
		// + tenantId + "'";
		// throw new RSSManagerException(msg, e);
		// } finally {
		// PrivilegedCarbonContext.endTenantFlow();
		// }
		return false;
	}

	public void addDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                                        throws RSSManagerException {
		DatabasePrivilegeTemplate entity = new DatabasePrivilegeTemplate();
		RSSManagerUtil.createDatabasePrivilegeTemplate(template, entity);
		this.getEnvironmentManager().createDatabasePrivilegesTemplate(environmentName, entity);
	}

	public void removeDatabasePrivilegeTemplate(String environmentName, String templateName)
	                                                                                        throws RSSManagerException {
		this.getEnvironmentManager().dropDatabasePrivilegesTemplate(environmentName, templateName);
	}

	public void updateDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                                           throws RSSManagerException {
		DatabasePrivilegeTemplate entity = new DatabasePrivilegeTemplate();
		RSSManagerUtil.createDatabasePrivilegeTemplate(template, entity);
		this.getEnvironmentManager().editDatabasePrivilegesTemplate(environmentName, entity);
	}

	public DatabasePrivilegeTemplateInfo[] getDatabasePrivilegeTemplates(String environmentName)
	                                                                                            throws RSSManagerException {
		DatabasePrivilegeTemplate[] entities = this.getEnvironmentManager()
		                                           .getDatabasePrivilegeTemplates(environmentName);
		List<DatabasePrivilegeTemplate> entityList = Arrays.asList(entities);
		List<DatabasePrivilegeTemplateInfo> infoList = new ArrayList<DatabasePrivilegeTemplateInfo>();
		for (DatabasePrivilegeTemplate entity : entityList) {
			DatabasePrivilegeTemplateInfo info = new DatabasePrivilegeTemplateInfo();
			RSSManagerUtil.createDatabasePrivilegeTemplateInfo(info, entity);
			infoList.add(info);
		}

		return infoList.toArray(new DatabasePrivilegeTemplateInfo[infoList.size()]);
	}

	public DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(String environmentName,
	                                                                  String templateName)
	                                                                                      throws RSSManagerException {
		DatabasePrivilegeTemplate entity = this.getEnvironmentManager()
		                                       .getDatabasePrivilegeTemplate(environmentName, templateName);
		DatabasePrivilegeTemplateInfo info = new DatabasePrivilegeTemplateInfo();
		RSSManagerUtil.createDatabasePrivilegeTemplateInfo(info, entity);
		return info;
	}

	public void addCarbonDataSource(String environmentName, UserDatabaseEntryInfo entry)
	                                                                                    throws RSSManagerException {
		Database database = this.getRSSManagerAdaptor(environmentName)
		                        .getDatabase(entry.getRssInstanceName(), entry.getDatabaseName(),
		                                     entry.getType());
		DatabaseInfo info = new DatabaseInfo();
		RSSManagerUtil.createDatabaseInfo(info, database);
		DataSourceMetaInfo metaInfo = RSSManagerUtil.createDSMetaInfo(info, entry.getUsername());
		try {
			RSSManagerDataHolder.getInstance().getDataSourceService().addDataSource(metaInfo);
		} catch (DataSourceException e) {
			String msg = "Error occurred while creating carbon datasource for the database '" + entry.getDatabaseName() + "'";
			throw new RSSManagerException(msg, e);
		}
	}

	private RSSManagerAdaptor getRSSManagerAdaptor(String environmentName) throws RSSManagerException {
		EnvironmentManager environmentManager = this.getEnvironmentManager();
		Environment environment = environmentManager.getEnvironment(environmentName);
		if (environment == null) {
			throw new IllegalArgumentException("Invalid RSS environment '" + environmentName + "'");
		}
		RSSManagerAdaptor adaptor = environment.getRSSManagerAdaptor();
		if (adaptor == null) {
			throw new RSSManagerException("RSS Manager is not initialized properly and " + "thus, is null");
		}
		return adaptor;
	}

	private EnvironmentManager getEnvironmentManager() {
		return environmentManager;
	}

	public String[] getEnvironments() throws RSSManagerException {
		return this.getEnvironmentManager().getEnvironmentNames();
	}

}
