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

package org.wso2.carbon.rssmanager.core.environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAOFactory;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptor;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptorFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EnvironmentManager {

	private Environment[] environments;
	private String[] environmentNames;
	private EnvironmentDAO environmentDAO;
	private RSSInstanceDAO rssInstanceDAO;
	private DatabasePrivilegeTemplateDAO privilegeTemplateDAO;
	private DatabasePrivilegeTemplateEntryDAO privilegeTemplateEntryDAO;
	private static final Log log = LogFactory.getLog(EnvironmentManager.class);

	public EnvironmentManager(Environment[] environments) {
		this.environments = environments;
		this.environmentNames = this.processEnvironments();
		environmentDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO().getEnvironmentDAO();
		rssInstanceDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO().getRSSInstanceDAO();
		privilegeTemplateDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO().getDatabasePrivilegeTemplateDAO();
		privilegeTemplateEntryDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO().getDatabasePrivilegeTemplateEntryDAO();
	}

	/**
	 * Validate existence of environment in meta repository
	 * @param environmentName name of the environment
	 * @return valid environment
	 * @throws RSSManagerException if error occurred when checking environment validity
	 */
	private Environment validateEnvironment(String environmentName) throws RSSManagerException {
		if (environmentName == null || environmentName.trim().length() == 0) {
			throw new RSSManagerException(" Environment name is null ");
		}
		Environment environment;
		try {
			environment = environmentDAO.getEnvironment(environmentName);
			if (environment == null) {
				throw new RSSManagerException(" Environment doesn't exist ");
			}
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error while getting environment " + environmentName, e);
		}
		return environment;
	}

	/**
	 * Remove environment from meta repository
	 *
	 * @param environmentName name of the environment
	 * @throws RSSManagerException if error occurred when removing environment
	 */
	@Deprecated
	public void removeEnvironment(String environmentName) throws RSSManagerException {
		try {
			environmentDAO.removeEnvironment(environmentName);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while removing RSS environment '" + environmentName + "'";
			handleException(msg, e);
		}
	}

	/**
	 * Add rss instance to meta repository
	 *
	 * @param rssInstance rss instance configuration
	 * @return rss instance
	 * @throws RSSManagerException if error occured when adding rss instance
	 */
	public RSSInstance addRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
		try {
			int tenantId = RSSManagerUtil.getTenantId();
			Environment environment = validateEnvironment(rssInstance.getEnvironmentName());
			RSSInstance existingInstance = rssInstanceDAO.getRSSInstance(rssInstance.getEnvironmentName(), rssInstance.getName(), tenantId);
			if (existingInstance != null) {
				throw new RSSManagerException("RSSInstance "+rssInstance.getName()+" already exist");
			}
			rssInstance.setEnvironment(environment);
			rssInstance.setTenantId((long) tenantId);
			rssInstanceDAO.addRSSInstance(environment.getName(), rssInstance, tenantId);
			return rssInstance;
		} catch (RSSDAOException e) {
			throw new RSSManagerException(
					"Error occurred while adding RSS instance '" + rssInstance.getName() + "' to environment '" +
					rssInstance.getEnvironmentName() + "'" + " Reason : " + e.getMessage());
		}
	}

	/**
	 * Remove rss instance from the database
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name if the rss instance
	 * @throws RSSManagerException
	 */
	public void removeRSSInstance(String environmentName, String rssInstanceName) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceDAO.removeRSSInstance(environmentName, rssInstanceName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while removing metadata related to " + "RSS instance '" + rssInstanceName + "' " +
			             "from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * Update the rss instance meta repository
	 *
	 * @param environmentName name of the environment
	 * @param rssInstance rss instance configuration
	 * @throws RSSManagerException if error occur when updating rss instance
	 */
	public void updateRSSInstance(String environmentName, RSSInstance rssInstance) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment env = validateEnvironment(environmentName);
			RSSInstance entity = rssInstanceDAO.getRSSInstance(environmentName, rssInstance.getName(), tenantId);
			entity.setServerURL(rssInstance.getDataSourceConfig() == null ? rssInstance.getServerURL() : rssInstance.getDataSourceConfig()
					.getRdbmsConfiguration()
					.getUrl());
			entity.setServerCategory(rssInstance.getServerCategory());
			entity.setAdminUserName(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminUserName() : rssInstance.getDataSourceConfig()
					.getRdbmsConfiguration()
					.getUsername());
			entity.setAdminPassword(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminPassword() : rssInstance.getDataSourceConfig()
					.getRdbmsConfiguration()
					.getPassword());
			entity.setTenantId((long) tenantId);
			entity.setDbmsType(rssInstance.getInstanceType());
			entity.setEnvironment(env);
			rssInstanceDAO.updateRSSInstance(environmentName, entity, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while updating metadata related to " + "RSS instance '" + rssInstance.getName() +
			             "' in RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * Get rss instance from the meta repository
	 *
	 * @param environmentName name of the environment
	 * @param rssInstanceName name if the rss instance
	 * @return rss instance
	 * @throws RSSManagerException if error occurred when getting rss instance
	 */
	public RSSInstance getRSSInstance(String environmentName, String rssInstanceName)
			throws RSSManagerException {
		RSSInstance rssInstance = null;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstance = rssInstanceDAO.getRSSInstance(environmentName, rssInstanceName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to " + "RSS instance '" + rssInstanceName +
			             "', from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return rssInstance;
	}

	/**
	 * Get rss instances of environment from meta repository
	 *
	 * @param environmentName name of the environment
	 * @return array of rss instances
	 * @throws RSSManagerException if error occur when getting rss instances
	 */
	public RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException {
		RSSInstance[] rssInstances = new RSSInstance[0];
		Set<RSSInstance> serverSet = new HashSet<RSSInstance>();
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			RSSInstance[] UserDefinedInstances = rssInstanceDAO.getUserDefinedRSSInstances(environmentName, tenantId);
			RSSInstance[] systemServers = rssInstanceDAO.getSystemRSSInstances(environmentName, 0);
			if (UserDefinedInstances != null && UserDefinedInstances.length > 0) {
				serverSet.addAll(Arrays.asList(UserDefinedInstances));
			}
			if (systemServers != null && systemServers.length > 0) {
				RSSInstance sysIns = Arrays.asList(systemServers).iterator().next();
				sysIns.setName(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
				serverSet.add(sysIns);
			}
			rssInstances = serverSet.toArray(new RSSInstance[serverSet.size()]);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata related to " + "RSS instances from RSS metadata repository : "
			             + e.getMessage();
			this.handleException(msg, e);
		}
		return rssInstances;
	}

	/**
	 * Get all rss instance list
	 *
	 * @return array of rss instances
	 * @throws RSSManagerException if error occurred while getting all rss instances
	 */
	public RSSInstance[] getRSSInstancesList() throws RSSManagerException {
		RSSInstance[] rssInstances = new RSSInstance[0];
		Set<RSSInstance> serverSet = new HashSet<RSSInstance>();
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			RSSInstance[] systemServers = rssInstanceDAO.getSystemRSSInstances(tenantId);
			RSSInstance[] UserDefinedInstances = rssInstanceDAO.getUserDefinedRSSInstances(tenantId);
			if (UserDefinedInstances != null && UserDefinedInstances.length > 0) {
				serverSet.addAll(Arrays.asList(UserDefinedInstances));
			}
			if (systemServers != null && systemServers.length > 0) {
				serverSet.addAll(Arrays.asList(systemServers));
			}
			rssInstances = serverSet.toArray(new RSSInstance[serverSet.size()]);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata related to " + "RSS instances from RSS metadata repository : "
			             + e.getMessage();
			this.handleException(msg, e);
		}
		return rssInstances;
	}

	/**
	 * Initialize environments
	 *
	 * @param rssProvider system rss provider
	 * @param repository rss management repository
	 * @throws RSSManagerException if error occurred when initializing environments
	 */
	public void initEnvironments(String rssProvider, RSSManagementRepository repository) throws RSSManagerException {
		//Add environments to the meta repository
		for (Environment environment : this.getEnvironments()) {
			this.addEnvironment(environment);
		}
		try {
			Set<Environment> allEnvironments = environmentDAO.getAllEnvironments();
			for (Environment environment : allEnvironments) {
				RSSInstance[] servers = rssInstanceDAO.getAllRSSInstancesOfEnvironment(environment.getName());
				environment.setRSSInstances(servers == null ? null : servers);
				RSSManagerAdaptor managerAdaptor = RSSManagerAdaptorFactory.getRSSManagerAdaptor(rssProvider, environment,
				                                                                            repository);
				environment.init(managerAdaptor);
			}
			environments = allEnvironments.toArray(new Environment[allEnvironments.size()]);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while getting all environments";
			handleException(msg, e);
		}
	}

	/**
	 * Add environment to the system at the initialization
	 *
	 * @param environment the environment configuration
	 * @throws RSSManagerException if error occur when adding environment
	 */
	public void addEnvironment(Environment environment) throws RSSManagerException {
		try {
			int tenantId = RSSManagerUtil.getTenantId();
			Set<RSSInstance> rssInstances = new HashSet<RSSInstance>();
			Environment managedEnv = environmentDAO.getEnvironment(environment.getName());
			boolean isEvnExist = (managedEnv.getName() == null ? false : true);
			RSSInstance[] instances = rssInstanceDAO.getSystemRSSInstances(environment.getName(), tenantId);
			Map<String, RSSInstance> rssInstancesMapFromConfig = new HashMap<String, RSSInstance>();//to hold rss instance from config
			//add rss instance from the configuration to map
			for (RSSInstance rssInstance : environment.getRSSInstances()) {
				rssInstance.setServerURL(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
				rssInstance.setAdminPassword(rssInstance.getDataSourceConfig().getRdbmsConfiguration()
						                             .getPassword());
				rssInstance.setAdminUserName(rssInstance.getDataSourceConfig().getRdbmsConfiguration()
						                             .getUsername());
				rssInstance.setTenantId((long) tenantId);
				rssInstance.setDriverClassName(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getDriverClassName());
				rssInstancesMapFromConfig.put(rssInstance.getName(), rssInstance);
			}

			Map<String, RSSInstance> rssInstanceMapFromDB = new HashMap<String, RSSInstance>();//to hold rss instance from database
			if (!isEvnExist) {
				environmentDAO.addEnvironment(environment);
				environment = environmentDAO.getEnvironment(environment.getName());
				DatabasePrivilegeTemplate privilegeTemplate = RSSManagerUtil.createDeafultDBPrivilegeTemplate();
				privilegeTemplate.setTenantId(RSSManagerUtil.getTenantId());
				privilegeTemplateDAO.addDatabasePrivilegeTemplate(privilegeTemplate, environment.getId());
				managedEnv = environment;
			}

			//By doing this it will not ignore dynamically added new instances, which are not in rss-config.xml
			for (RSSInstance instanceFromDB : instances) {
				if (!RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(instanceFromDB.getInstanceType()) &&
				    !RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(instanceFromDB.getInstanceType())) {
					throw new RSSManagerException("The instance type '" + instanceFromDB.getInstanceType() + "' is invalid.");
				}

				RSSInstance instanceFromBoth = rssInstancesMapFromConfig.get(instanceFromDB.getName());
				//if instance is exist in the database then update it to the rss instances in the  configuration
				if (instanceFromBoth != null) {
					instanceFromBoth.setTenantId((long) tenantId);
					instanceFromBoth.setEnvironment(managedEnv);
					RSSManagerUtil.applyInstanceChanges(instanceFromDB, instanceFromBoth);
				}
				rssInstanceMapFromDB.put((managedEnv.getName() + instanceFromDB.getName() + tenantId), instanceFromDB);
				rssInstances.add(instanceFromDB);
			}

			//By doing this it will not ignore new instances added to rss-config.xml but not in RSS DB
			Iterator<Entry<String, RSSInstance>> serverEntries = rssInstancesMapFromConfig
					.entrySet().iterator();
			while (serverEntries.hasNext()) {
				Entry<String, RSSInstance> serverEntry = serverEntries.next();
				RSSInstance instance = serverEntry.getValue();
				if (!RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(instance.getInstanceType()) &&
				    !RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(instance.getInstanceType())) {
					throw new RSSManagerException("The instance type '" + instance.getInstanceType() + "' is invalid.");
				}

				String key = (managedEnv.getName() + instance.getName() + tenantId);
				if (!rssInstanceMapFromDB.containsKey(key)) {
					instance.setTenantId((long) tenantId);
					instance.setEnvironment(managedEnv);
					rssInstances.add(instance);
				}
			}

			//Add new rss instances to the meta repository and update existing rss instances
			for (RSSInstance entity : rssInstances) {
				if (entity.getId() == null) {
					entity.setEnvironment(managedEnv);
					rssInstanceDAO.addRSSInstance(environment.getName(), entity, tenantId);
				} else {
					rssInstanceDAO.updateRSSInstance(environment.getName(), entity, tenantId);
				}
				managedEnv.getRssInstanceEntities().add(entity);
			}
		} catch (Exception e) {
			String msg = "Error occurred while initialize RSS environment '" + environment.getName() + "'";
			handleException(msg, e);
		}
	}

	public void handleException(String msg, Exception e) throws RSSManagerException {
		log.error(msg, e);
		throw new RSSManagerException(msg, e);
	}

	/**
	 * Check whether database privilege template exist
	 *
	 * @param environmentName name of the environment
	 * @param templateName name of the privilege template
	 * @return true if privilege template exist else false
	 * @throws RSSManagerException if error occurred when checking the privilege template existence
	 */
	public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
			throws RSSManagerException {
		boolean isExist = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment environment = environmentDAO.getEnvironment(environmentName);
			isExist = privilegeTemplateDAO.isDatabasePrivilegeTemplateExist(environment.getId(), templateName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while checking whether the database " + "privilege template named '" +
			             templateName + "' already exists : " + e.getMessage();
			handleException(msg, e);
		}
		return isExist;
	}

	/**
	 * Drop database privilege template
	 *
	 * @param environmentName name of the environment
	 * @param templateName name of the template
	 * @throws RSSManagerException if error occurs when drop privilege template
	 */
	public void dropDatabasePrivilegesTemplate(String environmentName, String templateName)
			throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment env = validateEnvironment(environmentName);
			privilegeTemplateDAO.removeDatabasePrivilegeTemplate(env.getId(), templateName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while removing metadata related to " + "database privilege template '" +
			             templateName + "', from RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * Get database privilege templates of environment
	 *
	 * @param environmentName name of the environment
	 * @return array of privilege templates
	 * @throws RSSManagerException if error occurred when getting privilege templates
	 */
	public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(String environmentName)
			throws RSSManagerException {
		DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment env = validateEnvironment(environmentName);
			templates = privilegeTemplateDAO.getDatabasePrivilegesTemplates(env.getId(), tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to database " + "privilege templates : " + e.getMessage();
			handleException(msg, e);
		}
		return templates;
	}

	/**
	 * Get database privilege template
	 *
	 * @param environmentName name of the environment
	 * @param templateName name of the template
	 * @return database privilege template object
	 * @throws RSSManagerException if error occur when getting database privilege template
	 */
	public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(String environmentName, String templateName)
			throws RSSManagerException {
		DatabasePrivilegeTemplate template = null;
		try {
			Environment env = validateEnvironment(environmentName);
			final int tenantId = RSSManagerUtil.getTenantId();
			template = privilegeTemplateDAO.getDatabasePrivilegesTemplate(env.getId(), templateName, tenantId);
			DatabasePrivilegeTemplateEntry privilegeTemplateEntry = privilegeTemplateEntryDAO.getPrivilegeTemplateEntry(template.getId());
			template.setEntry(privilegeTemplateEntry);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to database " + "privilege template '"
			             + templateName + "', from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		}
		return template;
	}

	/**
	 * Add database privilege template
	 *
	 * @param environmentName name of the environment
	 * @param template dataase privilege template
	 * @throws RSSManagerException if error occurred adding template
	 */
	public void createDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplate template)
			throws RSSManagerException {
		try {
			if (template == null) {
				String msg = "Database privilege template information cannot be null";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment env = validateEnvironment(environmentName);
			boolean isExist = privilegeTemplateDAO.isDatabasePrivilegeTemplateExist(env.getId(), template.getName(),
					                                  tenantId);
			if (isExist) {
				String msg = "A database privilege template named '" + template.getName() + "' already exists";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
			template.setEnvironment(env);
			template.setTenantId(tenantId);
			DatabasePrivilegeTemplateEntry entry = new DatabasePrivilegeTemplateEntry();
			RSSManagerUtil.createDatabasePrivilegeTemplateEntry(template.getPrivileges(), entry);
			template.setEntry(entry);
			entry.setPrivilegeTemplate(template);
			template.setTenantId(tenantId);
			privilegeTemplateDAO.addDatabasePrivilegeTemplate(template, env.getId());
		} catch (RSSDAOException e) {
			String msg = "Error occurred while adding metadata related to " + "database privilege template '" +
			             template.getName() + "', to RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * Edit database privilege template
	 *
	 * @param environmentName name of the environment
	 * @param template database privilege template
	 * @throws RSSManagerException if error occurs when editing database privilege template
	 */
	public void editDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplate template)
			throws RSSManagerException {
		try {
			if (template == null) {
				String msg = "Database privilege template information cannot be null";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			Environment env = validateEnvironment(environmentName);
			DatabasePrivilegeTemplate entity = privilegeTemplateDAO.getDatabasePrivilegesTemplate(env.getId(),
			                                                                     template.getName(), tenantId);
			if (entity == null) {
				throw new RSSManagerException(" Template doesn't exist : " + template.getName());
			}
			DatabasePrivilegeSet privilegeSet = template.getPrivileges();
			DatabasePrivilegeTemplateEntry privilegeTemplateEntry = new DatabasePrivilegeTemplateEntry();
			RSSManagerUtil.createDatabasePrivilegeTemplateEntry(privilegeSet, privilegeTemplateEntry);
			privilegeTemplateEntryDAO.updatePrivilegeTemplateEntry(env.getId(),
			                                                                                                entity.getId(), privilegeTemplateEntry);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while updating metadata corresponding to database " + "privilege template '" + template.getName() +
			             "', in RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		}
	}

	private String[] processEnvironments() {
		String[] names = new String[this.getEnvironments().length];
		for (int i = 0; i < this.getEnvironments().length; i++) {
			names[i] = this.getEnvironments()[i].getName();
		}
		return names;
	}

	public Environment getEnvironment(String environmentName) {
		for (Environment environment : this.getEnvironments()) {
			if (environment.getName().equals(environmentName)) {
				return environment;
			}
		}
		return null;
	}

	public Environment[] getEnvironments() {
		return environments;
	}

	public String[] getEnvironmentNames() {
		return environmentNames;
	}

}
