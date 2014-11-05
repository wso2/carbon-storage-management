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

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnvironmentAdaptor implements RSSManagerService {

	private EnvironmentManager environmentManager;

	public EnvironmentAdaptor(EnvironmentManager environmentManager) {
		this.environmentManager = environmentManager;
	}

	/**
	 * @see RSSManagerService#addRSSInstance(String, org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo)
	 */
	public void addRSSInstance(String environmentName, RSSInstanceInfo rssInstance)
			throws RSSManagerException {
		RSSInstance entity = new RSSInstance();
		RSSManagerUtil.createRSSInstance(rssInstance, entity);
		entity = this.getEnvironmentManager().addRSSInstance(entity);
		environmentManager.getEnvironment(rssInstance.getEnvironmentName()).getDSWrapperRepository().addRSSInstanceDSWrapper(entity);
		environmentManager.getEnvironment(rssInstance.getEnvironmentName()).addRSSInstance(entity);
	}

	/**
	 * @see RSSManagerService#removeRSSInstance(String, String, String)
	 */
	public void removeRSSInstance(String environmentName, String rssInstanceName, String type)
			throws RSSManagerException {
		this.getEnvironmentManager().removeRSSInstance(environmentName, rssInstanceName);
		environmentManager.getEnvironment(environmentName).getDSWrapperRepository().removeRSSInstanceDSWrapper(rssInstanceName);
		environmentManager.getEnvironment(environmentName).removeRSSInstance(rssInstanceName);
	}

	/**
	 * @see RSSManagerService#updateRSSInstance(String, org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo)
	 */
	public void updateRSSInstance(String environmentName, RSSInstanceInfo rssInstance)
			throws RSSManagerException {
		RSSInstance entity = new RSSInstance();
		RSSManagerUtil.createRSSInstance(rssInstance, entity);
		this.environmentManager.updateRSSInstance(environmentName, entity);
		environmentManager.getEnvironment(environmentName).getDSWrapperRepository().removeRSSInstanceDSWrapper(rssInstance.getName());
		environmentManager.getEnvironment(environmentName).removeRSSInstance(rssInstance.getName());
		environmentManager.getEnvironment(rssInstance.getEnvironmentName()).getDSWrapperRepository().addRSSInstanceDSWrapper(entity);
		environmentManager.getEnvironment(rssInstance.getEnvironmentName()).addRSSInstance(entity);
	}

	/**
	 * @see RSSManagerService#getRSSInstance(String, String, String)
	 */
	public RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName, String type)
			throws RSSManagerException {
		RSSInstance entity = this.getEnvironmentManager().getRSSInstance(environmentName, rssInstanceName);
		RSSInstanceInfo info = new RSSInstanceInfo();
		RSSManagerUtil.createRSSInstanceInfo(info, entity);
		return info;
	}

	/**
	 * @see RSSManagerService#getRSSInstances(String)
	 */
	public RSSInstanceInfo[] getRSSInstances(String environmentName) throws RSSManagerException {
		RSSInstance[] entities = this.environmentManager.getRSSInstances(environmentName);
		List<RSSInstance> entityList = Arrays.asList(entities);
		List<RSSInstanceInfo> infoList = new ArrayList<RSSInstanceInfo>();
		for (RSSInstance entity : entityList) {
			RSSInstanceInfo info = new RSSInstanceInfo();
			RSSManagerUtil.createRSSInstanceInfo(info, entity);
			infoList.add(info);
		}
		return infoList.toArray(new RSSInstanceInfo[infoList.size()]);
	}

	/**
	 * @see RSSManagerService#getRSSInstancesList()
	 */
	public RSSInstanceInfo[] getRSSInstancesList() throws RSSManagerException {
		RSSInstance[] entities = this.environmentManager.getRSSInstancesList();
		List<RSSInstance> entityList = Arrays.asList(entities);
		List<RSSInstanceInfo> infoList = new ArrayList<RSSInstanceInfo>();
		for (RSSInstance entity : entityList) {
			RSSInstanceInfo info = new RSSInstanceInfo();
			RSSManagerUtil.createRSSInstanceInfo(info, entity);
			infoList.add(info);
		}
		return infoList.toArray(new RSSInstanceInfo[infoList.size()]);
	}

	/**
	 * @see RSSManagerService#addDatabase(String, org.wso2.carbon.rssmanager.core.dto.DatabaseInfo)
	 */
	public DatabaseInfo addDatabase(String environmentName, DatabaseInfo database) throws RSSManagerException {
		Database entity = new Database();
		RSSManagerUtil.createDatabase(database, entity);
		Database returnEntity = this.getRSSManagerAdaptor(environmentName).addDatabase(entity);
		RSSManagerUtil.createDatabaseInfo(database, returnEntity);
		return database;
	}

	/**
	 * @see RSSManagerService#removeDatabase(String, String, String, String)
	 */
	public void removeDatabase(String environmentName, String rssInstanceName, String databaseName,
	                           String type) throws RSSManagerException {
		this.getRSSManagerAdaptor(environmentName).removeDatabase(rssInstanceName, databaseName, type);
	}

	/**
	 * @see RSSManagerService#getDatabases(String)
	 */
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

	/**
	 * @see RSSManagerService#getDatabase(String, String, String, String)
	 */
	public DatabaseInfo getDatabase(String environmentName, String rssInstanceName, String databaseName,
	                                String type) throws RSSManagerException {

		Database entity = this.getRSSManagerAdaptor(environmentName).getDatabase(rssInstanceName,
		                                                                         databaseName, type);
		DatabaseInfo info = new DatabaseInfo();
		RSSManagerUtil.createDatabaseInfo(info, entity);
		return info;
	}

	/**
	 * @see RSSManagerService#isDatabaseExist(String, String, String, String)
	 */
	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               String type) throws RSSManagerException {
		return this.getRSSManagerAdaptor(environmentName)
				.isDatabaseExist(rssInstanceName, databaseName, type);
	}

	/**
	 * @see RSSManagerService#isDatabaseUserExist(String, String, String, String)
	 */
	public boolean isDatabaseUserExist(String environmentName, String rssInstanceName, String username,
	                                   String type) throws RSSManagerException {
		return this.getRSSManagerAdaptor(environmentName)
				.isDatabaseUserExist(rssInstanceName, username, type);
	}

	/**
	 * @see RSSManagerService#addDatabaseUser(String, org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo)
	 */
	public DatabaseUserInfo addDatabaseUser(String environmentName, DatabaseUserInfo user)
			throws RSSManagerException {
		DatabaseUser entity = new DatabaseUser();
		RSSManagerUtil.createDatabaseUser(user, entity);
		entity = this.getRSSManagerAdaptor(environmentName).addDatabaseUser(entity);
		RSSManagerUtil.createDatabaseUserInfo(user, entity);
		return user;
	}

	/**
	 * @see RSSManagerService#removeDatabaseUser(String, String, String, String)
	 */
	public void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
	                               String type) throws RSSManagerException {
		this.getRSSManagerAdaptor(environmentName).removeDatabaseUser(rssInstanceName, username, type);
	}

	/**
	 * @see RSSManagerService#updateDatabaseUserPrivileges(String, DatabasePrivilegeSetInfo, DatabaseUserInfo, String)
	 */
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

	/**
	 * @see RSSManagerService#getDatabaseUser(String, String, String, String)
	 */
	public DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstanceName, String username,
	                                        String type) throws RSSManagerException {
		DatabaseUser entity = this.getRSSManagerAdaptor(environmentName).getDatabaseUser(rssInstanceName,
		                                                                                 username, type);
		DatabaseUserInfo info = new DatabaseUserInfo();
		RSSManagerUtil.createDatabaseUserInfo(info, entity);
		return info;
	}

	/**
	 * @see RSSManagerService#getDatabaseUsers(String)
	 */
	public DatabaseUserInfo[] getDatabaseUsers(String environmentName) throws RSSManagerException {

		DatabaseUser[] entities = this.getRSSManagerAdaptor(environmentName).getDatabaseUsers();
		List<DatabaseUser> entityList = Arrays.asList(entities);
		Set<DatabaseUserInfo> infoList = new HashSet<DatabaseUserInfo>();
		for (DatabaseUser entity : entityList) {
			DatabaseUserInfo info = new DatabaseUserInfo();
			RSSManagerUtil.createDatabaseUserInfo(info, entity);
			infoList.add(info);
		}
		return infoList.toArray(new DatabaseUserInfo[infoList.size()]);
	}

	/**
	 * @see RSSManagerService#attachUser(String, org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo, String)
	 */
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

	/**
	 * @see RSSManagerService#detachUser(String, org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo)
	 */
	public void detachUser(String environmentName, UserDatabaseEntryInfo databaseEntryInfo) throws RSSManagerException {
		UserDatabaseEntry entity = new UserDatabaseEntry();
		RSSManagerUtil.createDatabaseUserEntry(databaseEntryInfo, entity);
		this.getRSSManagerAdaptor(environmentName).detachUser(entity);
	}

	/**
	 * @see RSSManagerService#getAttachedUsers(String, String, String, String)
	 */
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

	/**
	 * @see RSSManagerService#getAvailableUsers(String, String, String, String)
	 */
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

	/**
	 * @see RSSManagerService#getUserDatabasePrivileges(String, String, String, String, String)
	 */
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

	/**
	 * @see RSSManagerService#getDatabaseForTenant(String, String, String, String, String)
	 */
	public DatabaseInfo[] getDatabasesForTenant(String environmentName, String tenantDomain)
			throws RSSManagerException {
		return new DatabaseInfo[0];
	}

	/**
	 * @see RSSManagerService#addDatabaseForTenant(String, org.wso2.carbon.rssmanager.core.dto.DatabaseInfo, String)
	 */
	public void addDatabaseForTenant(String environmentName, DatabaseInfo database, String tenantDomain)
			throws RSSManagerException {

	}

	/**
	 * @see RSSManagerService#getDatabaseForTenant(String, String, String, String, String)
	 */
	public DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName,
	                                         String databaseName, String tenantDomain, String type)
			throws RSSManagerException {
		return null;
	}

	/**
	 * @see RSSManagerService#isDatabasePrivilegeTemplateExist(String, String)
	 */
	public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
			throws RSSManagerException {
		return this.getEnvironmentManager().isDatabasePrivilegeTemplateExist(environmentName, templateName);
	}

	/**
	 * @see RSSManagerService#deleteTenantRSSData(String, String)
	 */
	public boolean deleteTenantRSSData(String environmentName, String tenantDomain)
			throws RSSManagerException {
		//TODO implement this after finalize the proper approach to do this
		return false;
	}

	/**
	 * @see RSSManagerService#addDatabasePrivilegeTemplate(String, org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo)
	 */
	public void addDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
			throws RSSManagerException {
		DatabasePrivilegeTemplate entity = new DatabasePrivilegeTemplate();
		RSSManagerUtil.createDatabasePrivilegeTemplate(template, entity);
		this.environmentManager.createDatabasePrivilegesTemplate(environmentName, entity);
	}

	/**
	 * @see RSSManagerService#removeDatabasePrivilegeTemplate(String, String)
	 */
	public void removeDatabasePrivilegeTemplate(String environmentName, String templateName)
			throws RSSManagerException {
		this.environmentManager.dropDatabasePrivilegesTemplate(environmentName, templateName);
	}

	/**
	 * @see RSSManagerService#updateDatabasePrivilegeTemplate(String, org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo)
	 */
	public void updateDatabasePrivilegeTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
			throws RSSManagerException {
		DatabasePrivilegeTemplate entity = new DatabasePrivilegeTemplate();
		RSSManagerUtil.createDatabasePrivilegeTemplate(template, entity);
		this.environmentManager.editDatabasePrivilegesTemplate(environmentName, entity);
	}

	/**
	 * @see RSSManagerService#getDatabasePrivilegeTemplates(String)
	 */
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

	/**
	 * @see RSSManagerService#getDatabasePrivilegeTemplate(String, String)
	 */
	public DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(String environmentName,
	                                                                  String templateName)
			throws RSSManagerException {
		DatabasePrivilegeTemplate entity = this.getEnvironmentManager()
				.getDatabasePrivilegeTemplate(environmentName, templateName);
		DatabasePrivilegeTemplateInfo info = new DatabasePrivilegeTemplateInfo();
		RSSManagerUtil.createDatabasePrivilegeTemplateInfo(info, entity);
		return info;
	}

	/**
	 * @see RSSManagerService#addCarbonDataSource(String, String, org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo)
	 */
	public void addCarbonDataSource(String environmentName,
	                                String dataSourceName, UserDatabaseEntryInfo entry)
			throws RSSManagerException {
		Database database = this.getRSSManagerAdaptor(environmentName)
				.getDatabase(entry.getRssInstanceName(),
				             entry.getDatabaseName(), entry.getType());
		DatabaseUser databaseuserinfo = this.getRSSManagerAdaptor(
				environmentName).getDatabaseUser(entry.getRssInstanceName(),
		                                         entry.getUsername(), entry.getType());
		DatabaseInfo info = new DatabaseInfo();
		RSSManagerUtil.createDatabaseInfo(info, database);
		DataSourceMetaInfo metaInfo = RSSManagerUtil.createDSMetaInfo(info,
		                                                              entry.getUsername(), databaseuserinfo.getPassword(),
		                                                              dataSourceName);
		try {
			List<CarbonDataSource> dsList = RSSManagerDataHolder.getInstance()
					.getDataSourceService().getAllDataSources();
			for (CarbonDataSource ds : dsList) {
				if (ds.getDSMInfo().getName().equals(dataSourceName)) {
					String msg = "Datasource already exists by name  '"
					             + dataSourceName + "'";
					throw new RSSManagerException(msg,
					                              new DataSourceException());
				}
			}
			RSSManagerDataHolder.getInstance().getDataSourceService()
					.addDataSource(metaInfo);
		} catch (DataSourceException e) {
			String msg = "Error occurred while creating carbon datasource for the database '"
			             + entry.getDatabaseName() + "'";
			throw new RSSManagerException(msg, e);
		}
	}

	/**
	 * Get rss manager adaptor fot the environment
	 *
	 * @param environmentName name of the environment
	 * @return RSSManagerAdaptor
	 */
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

	/**
	 * @see RSSManagerService#editDatabaseUser(String, org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo)
	 */
	public DatabaseUserInfo editDatabaseUser(String environment, DatabaseUserInfo databaseUserInfo) throws RSSManagerException {
		DatabaseUser entity = new DatabaseUser();
		RSSManagerUtil.createDatabaseUser(databaseUserInfo, entity);
		entity = this.getRSSManagerAdaptor(environment).editDatabaseUser(environment, entity);
		RSSManagerUtil.createDatabaseUserInfo(databaseUserInfo, entity);
		return databaseUserInfo;
	}

	/**
	 * @see RSSManagerService#addCarbonDataSource(String, String, org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo)
	 */
	public String getRSSProvider() {
		return RSSConfigurationManager.getInstance().getCurrentRSSConfig().getRSSProvider();
	}

}
