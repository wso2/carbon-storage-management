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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
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
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.EntityType;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.JPAManagerUtil;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.PersistenceManager;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptor;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptorFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class EnvironmentManager {

	private Environment[] environments;
	private String[] environmentNames;
	private EntityManager entityManager;
	private EnvironmentManagementDAO envManagementDAO;
	private static final Log log = LogFactory.getLog(EnvironmentManager.class);

	public EnvironmentManager(Environment[] environments) {
		this.environments = environments;
		this.environmentNames = this.processEnvironments();
	}

	protected void closeJPASession() {
		getEntityManager().getJpaUtil().closeEnitityManager();
	}

	protected void overrideJPASession(EntityBaseDAO dao) {
		dao.overrideJPASession(getEntityManager().getJpaUtil().getJPAEntityManager());
	}
	
	protected void joinTransaction(){
		getEntityManager().getJpaUtil().getJPAEntityManager().joinTransaction();
	}
	
	private Environment validateEnvironment(String envName, EnvironmentDAO dao) throws RSSManagerException {
		if(envName == null || envName.trim().length() == 0 ){
			throw new RSSManagerException(" Environment name is null ");
		}
		Environment env = dao.getEnvironment(envName);
		if (env == null) {
			throw new RSSManagerException(" Environment doesn't exist ");
		}
		return env;
	}

	public void removeEnvironment(String environmentName) throws RSSManagerException {
		boolean inTx = false;
		try {
			int tenantId = RSSManagerUtil.getTenantId();
			
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			//closeJPASession();

			inTx = this.getEntityManager().beginTransaction();
			//overrideJPASession(envDao);
			joinTransaction();

			//env = envDao.merge(env);
			envDao.remove(env);
			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSManagerException e) {
			if (inTx) {
				this.getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while removing RSS environment '" + environmentName + "'";
			handleException(msg, e);
		} finally {
			closeJPASession();
		}
	}

	public RSSInstance addRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
		boolean inTx = false;
		try {
			int tenantId = RSSManagerUtil.getTenantId();
			
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(rssInstance.getEnvironmentName(),envDao);
			
			RSSInstanceDAO dao = this.getEnvironmentDAOMgr().getRSSInstanceDAO();
			RSSInstance existingInstance = dao.getRSSInstance(rssInstance.getEnvironmentName(), rssInstance.getName(), tenantId);
			if(existingInstance != null){
				throw new RSSManagerException(" RSSInstance already exist ");
			}
			rssInstance.setEnvironment(env);
			closeJPASession();

			inTx = this.getEntityManager().beginTransaction();
			rssInstance.setTenantId((long) tenantId);
			this.getEnvironmentDAOMgr().getRSSInstanceDAO()
			    .insert(rssInstance);
			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
			return rssInstance;
		} catch (Exception e) {
			if (inTx) {
				this.getEntityManager().rollbackJPATransaction();
			}
			throw new RSSManagerException(
			                              "Error occurred while adding RSS instance '" + rssInstance.getName() + "' to environment '" + rssInstance.getEnvironmentName() + "'" + " Reason : "+e.getMessage());
		} finally {
			closeJPASession();
		}
	}

	public void removeRSSInstance(String environmentName, String rssInstanceName) throws RSSManagerException {
		boolean inTx = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			
			RSSInstanceDAO dao = this.getEnvironmentDAOMgr().getRSSInstanceDAO();
			RSSInstance entity = dao.getRSSInstance(environmentName, rssInstanceName, tenantId);
			//closeJPASession();

			inTx = getEntityManager().beginTransaction();
			//overrideJPASession(dao);
			joinTransaction();
			//entity = dao.merge(entity);
			dao.remove(entity);
			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSDAOException e) {
			if (inTx) {
				getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while removing metadata related to " + "RSS instance '" + rssInstanceName + "' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			closeJPASession();
		}
	}

	public void updateRSSInstance(String environmentName, RSSInstance rssInstance) throws RSSManagerException {
		boolean inTx = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			
			RSSInstanceDAO dao = this.getEnvironmentDAOMgr().getRSSInstanceDAO();
			RSSInstance entity = dao.getRSSInstance(environmentName, rssInstance.getName(), tenantId);

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

			//closeJPASession();

			inTx = this.getEntityManager().beginTransaction();
			//overrideJPASession(dao);
			joinTransaction();
			dao.saveOrUpdate(entity);
			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSDAOException e) {
			if (inTx) {
				this.getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while updating metadata related to " + "RSS instance '" + rssInstance.getName() + "' in RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
	}

	private EntityManager getEntityManager() {
		return entityManager;
	}

	public RSSInstance getRSSInstance(String environmentName, String rssInstanceName)
	                                                                                 throws RSSManagerException {
		RSSInstance rssInstance = null;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstance = this.getEnvironmentDAOMgr().getRSSInstanceDAO()
			                  .getRSSInstance(environmentName, rssInstanceName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to " + "RSS instance '" + rssInstanceName + "', from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
		return rssInstance;
	}

	public int getSystemRSSInstanceCount(String environmentName) throws RSSManagerException {
		try {
			RSSInstance[] sysRSSInstances = this.getEnvironmentDAOMgr()
			                                    .getRSSInstanceDAO()
			                                    .getRSSInstances(environmentName,
			                                                     MultitenantConstants.SUPER_TENANT_ID);
			return sysRSSInstances.length;
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving the system RSS instance count : " + e.getMessage();
			throw new RSSManagerException(msg, e);
		} finally {
			this.closeJPASession();
		}
	}

	public RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException {
		RSSInstance[] rssInstances = new RSSInstance[0];
		Set<RSSInstance> serverSet = new HashSet<RSSInstance>();
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			RSSInstanceDAO dao = this.getEnvironmentDAOMgr().getRSSInstanceDAO();
			RSSInstance[] UserDefinedInstances = dao.getUserDefinedRSSInstances(environmentName, tenantId);
			RSSInstance[] systemServers = dao.getSystemRSSInstances(environmentName, 0);
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
			String msg = "Error occurred while retrieving metadata related to " + "RSS instances from RSS metadata repository : " + e.getMessage();
			this.handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
		return rssInstances;
	}

	public void initEnvironments(String rssProvider, RSSManagementRepository repository)
	                                                                                    throws RSSManagerException {

		/* Initializing RSS transaction manager wrapper */
		RSSTransactionManager rssTxManager = new RSSTransactionManager(
		                                                               RSSManagerDataHolder.getInstance()
		                                                                                   .getTransactionManager());
		/* Initializing entity manager used in RSS DAO */
		DataSource dataSource = RSSDAOFactory.resolveDataSource(repository.getDataSourceConfig());

		Set<String> unitNames = PersistenceManager.getPersistentUnitNames();
		String unitName = unitNames.iterator().next();

		this.entityManager = new EntityManager(rssTxManager, dataSource,
		                                       new JPAManagerUtil(PersistenceManager.getEMF(unitName)));

		this.envManagementDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO(null, entityManager);

		for (Environment environment : this.getEnvironments()) {
			this.addEnvironment(environment);			
			
		}
		
		Set<Environment> allEnv = this.envManagementDAO.getEnvironmentDAO().getAllEnvironments();
		for(Environment environment : allEnv){
			Set<RSSInstance> servers = environment.getRssInstanceEntities();
			environment.setRSSInstances( servers == null ? null : servers.toArray(new RSSInstance[servers.size()]));
			RSSManagerAdaptor rmAdaptor = RSSManagerAdaptorFactory.getRSSManagerAdaptor(rssProvider, environment,
			                                                                            repository);
			environment.init(rmAdaptor);
		}
		environments = allEnv.toArray(new Environment[allEnv.size()]);	
		this.closeJPASession();
		
	}

	public void addEnvironment(Environment environment) throws RSSManagerException {

		boolean inTx = false;
		try {
			int tenantId = RSSManagerUtil.getTenantId();
			Set<RSSInstance> rssInstances = new HashSet<RSSInstance>();
			EnvironmentDAO envDAO = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			RSSInstanceDAO instanceDAO = this.getEnvironmentDAOMgr().getRSSInstanceDAO();
			Environment managedEnv = envDAO.getEnvironment(environment.getName());
			boolean isEvnExist = (managedEnv == null ? false : true);
			RSSInstance[] instances = instanceDAO.getSystemRSSInstances(environment.getName(), tenantId);

			Map<String, RSSInstance> rssInstancesMapFromConfig = new HashMap<String, RSSInstance>();
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

			Map<String, RSSInstance> rssInstanceMapFromDB = new HashMap<String, RSSInstance>();
			//this.closeJPASession();

			inTx = this.getEntityManager().beginTransaction();
			joinTransaction();
			if (!isEvnExist) {
				envDAO.insert(environment);
				managedEnv = environment;
			} else {

			}


			//By doing this it will not ignore dynamically added new instances, which are not in rss-config.xml
			for (RSSInstance instanceFromDB : instances) {	
				if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(instanceFromDB.getInstanceType()) || 
						RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(instanceFromDB.getInstanceType())) {
					
				} else {
					throw new RSSManagerException("The instance type '" + instanceFromDB.getInstanceType() + "' is invalid.");
				}
				
				String key = instanceFromDB.getName();
				RSSInstance instanceFromBoth = rssInstancesMapFromConfig.get(key);
				
				if(instanceFromBoth != null){
					instanceFromBoth.setTenantId((long) tenantId);
					instanceFromBoth.setEnvironment(managedEnv);
					RSSManagerUtil.applyInstanceChanges(instanceFromDB, instanceFromBoth);
					
				}
				rssInstanceMapFromDB.put((managedEnv.getName() + instanceFromDB.getName() + tenantId), instanceFromDB);
				rssInstances.add(instanceFromDB);

			}

			//By doing this it will not ignore new instances added to rss-config.xml but not in RSS DB
			Iterator<Entry<String, RSSInstance>> iterServer = rssInstancesMapFromConfig
					.entrySet().iterator();
			while (iterServer.hasNext()) {
				Entry<String, RSSInstance> serverEntry = iterServer.next();
				RSSInstance inst = serverEntry.getValue();
				if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(inst.getInstanceType()) || 
						RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(inst.getInstanceType())) {
					
				} else {
					throw new RSSManagerException("The instance type '" + inst.getInstanceType() + "' is invalid.");
				}
				
				String key = (managedEnv.getName() + inst.getName() + tenantId);
				if (!rssInstanceMapFromDB.containsKey(key)) {
					inst.setTenantId((long) tenantId);
					inst.setEnvironment(managedEnv);
					rssInstances.add(inst);					
				}

			}

			if (!isEvnExist) {
				managedEnv.setRssInstanceEntities(rssInstances);
				envDAO.merge(managedEnv);
			} else {

				for (RSSInstance entity : rssInstances) {
					if (entity.getId() == null) {
						entity.setEnvironment(managedEnv);
						instanceDAO.insert(entity);
						managedEnv.getRssInstanceEntities().add(entity);

					} else {
						instanceDAO.merge(entity);
					}

				}

			}

			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (Exception e) {
			if (inTx) {
				this.getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while initialize RSS environment '" + environment.getName() + "'";
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
	}
	

	public void handleException(String msg, Exception e) throws RSSManagerException {
		log.error(msg, e);
		throw new RSSManagerException(msg, e);
	}

	public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName)
	                                                                                            throws RSSManagerException {
		boolean isExist = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			isExist = this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO()
			              .isDatabasePrivilegeTemplateExist(environmentName, templateName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while checking whether the database " + "privilege template named '" + templateName + "' already exists : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
		return isExist;
	}

	public void dropDatabasePrivilegesTemplate(String environmentName, String templateName)
	                                                                                       throws RSSManagerException {
		boolean inTx = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			DatabasePrivilegeTemplateDAO dao = this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO();
			DatabasePrivilegeTemplate template = dao.getDatabasePrivilegesTemplate(environmentName,
			                                                                       templateName, tenantId);

			//this.closeJPASession();

			inTx = getEntityManager().beginTransaction();
			//this.overrideJPASession(dao);
			joinTransaction();
			//template = dao.merge(template);
			this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateEntryDAO().remove(template.getEntry());
			template.setEntry(null);
			dao.remove(template);
			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSDAOException e) {
			if (inTx) {
				getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while removing metadata related to " + "database privilege template '" + templateName + "', from RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
	}

	public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(String environmentName)
	                                                                                        throws RSSManagerException {
		DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			
			templates = this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO()
			                .getDatabasePrivilegesTemplates(environmentName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to database " + "privilege templates : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
		return templates;
	}

	public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(String environmentName, String templateName)
	                                                                                                          throws RSSManagerException {
		DatabasePrivilegeTemplate template = null;
		try {
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			
			final int tenantId = RSSManagerUtil.getTenantId();
			template = this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO()
			               .getDatabasePrivilegesTemplate(environmentName, templateName, tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving metadata corresponding to database " + "privilege template '" + templateName + "', from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
		}
		return template;
	}

	public void createDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplate template)
	                                                                                                        throws RSSManagerException {
		boolean inTx = false;
		try {
			if (template == null) {
				String msg = "Database privilege template information cannot be null";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			boolean isExist = this.getEnvironmentDAOMgr()
			                      .getDatabasePrivilegeTemplateDAO()
			                      .isDatabasePrivilegeTemplateExist(environmentName, template.getName(),
			                                                        tenantId);
			if (isExist) {
				String msg = "A database privilege template named '" + template.getName() + "' already exists";
				log.error(msg);
				throw new RSSManagerException(msg);
			}

			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);
			//closeJPASession();

			inTx = getEntityManager().beginTransaction();
			joinTransaction();
			template.setEnvironment(env);
			template.setTenantId(tenantId);
			DatabasePrivilegeTemplateEntry entry = new DatabasePrivilegeTemplateEntry();
			RSSManagerUtil.createDatabasePrivilegeTemplateEntry(template.getPrivileges(), entry);
			template.setEntry(entry);
			entry.setPrivilegeTemplate(template);
			template.setTenantId(tenantId);

			// template.setEntry(null);
			this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO().insert(template);
			// entry.setPrivilegeTemplate(template);
			// this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateEntryDAO().insert(entry);

			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSDAOException e) {
			if (inTx) {
				getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while adding metadata related to " + "database privilege template '" + template.getName() + "', to RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			closeJPASession();
			/*
			 * if (inTx) {
			 * getEntityManager().endJPATransaction();
			 * }
			 */
		}
	}

	public void editDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplate template)
	                                                                                                      throws RSSManagerException {
		boolean inTx = false;
		try {
			if (template == null) {
				String msg = "Database privilege template information cannot be null";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			
			EnvironmentDAO envDao = this.getEnvironmentDAOMgr().getEnvironmentDAO();
			Environment env = validateEnvironment(environmentName,envDao);

			DatabasePrivilegeTemplateDAO dao = this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateDAO();
			DatabasePrivilegeTemplate entity = dao.getDatabasePrivilegesTemplate(environmentName,
			                                                                     template.getName(), tenantId);
			
			if(entity == null){
				throw new RSSManagerException(" Template doesn't exist : "+template.getName());
			}
			/*template.setEnvironment(entity.getEnvironment());
			template.setId(entity.getId());
			template.setTenantId(tenantId);*/
			DatabasePrivilegeSet privilegeSet = template.getPrivileges();
			RSSManagerUtil.createDatabasePrivilegeTemplateEntry(privilegeSet, entity.getEntry());
			/*template.setEntry(entity.getEntry());
			if (entity.getEntry() != null)
				entity.getEntry().setPrivilegeTemplate(template);*/

			//closeJPASession();

			inTx = getEntityManager().beginTransaction();

			//overrideJPASession(dao);
			joinTransaction();
			dao.saveOrUpdate(entity);
			this.getEnvironmentDAOMgr().getDatabasePrivilegeTemplateEntryDAO().merge(entity.getEntry());

			if (inTx) {
				this.getEntityManager().endJPATransaction();
			}
		} catch (RSSDAOException e) {
			if (inTx) {
				getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while updating metadata corresponding to database " + "privilege template '" + template.getName() + "', in RSS metadata " + "repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			this.closeJPASession();
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

	private EnvironmentManagementDAO getEnvironmentDAOMgr() {
		return envManagementDAO;
	}

	public Environment[] getEnvironments() {
		return environments;
	}

	public String[] getEnvironmentNames() {
		return environmentNames;
	}

}
