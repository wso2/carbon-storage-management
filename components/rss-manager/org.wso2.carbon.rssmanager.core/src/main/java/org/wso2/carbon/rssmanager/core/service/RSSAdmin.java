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
package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.adaptor.EnvironmentAdaptor;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RSSAdmin extends AbstractAdmin implements RSSManagerService {

	private static final Log log = LogFactory.getLog(RSSAdmin.class);

	/**
	 * @see RSSManagerService#addRSSInstance
	 */
	public void addRSSInstance(String environmentName,
	                           RSSInstanceInfo rssInstance) throws RSSManagerException {
		this.getEnvironmentAdaptor().addRSSInstance(environmentName, rssInstance);
	}

	/**
	 * @see RSSManagerService#removeRSSInstance
	 */
	public void removeRSSInstance(String environmentName, String rssInstanceName,
	                              String type) throws RSSManagerException {
		this.getEnvironmentAdaptor().removeRSSInstance(environmentName, rssInstanceName, type);
	}

	/**
	 * @see RSSManagerService#updateRSSInstance
	 */
	public void updateRSSInstance(String environmentName,
	                              RSSInstanceInfo rssInstance) throws RSSManagerException {
		this.getEnvironmentAdaptor().updateRSSInstance(environmentName, rssInstance);
	}

	/**
	 * @see RSSManagerService#getRSSInstance
	 */
	public RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName,
	                                      String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getRSSInstance(environmentName, rssInstanceName, type);
	}

	/**
	 * @see RSSManagerService#getRSSInstances
	 */
	public RSSInstanceInfo[] getRSSInstances(String environmentName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getRSSInstances(environmentName);
	}

	/**
	 * @see RSSManagerService#getRSSInstancesList
	 */
	public RSSInstanceInfo[] getRSSInstancesList() throws RSSManagerException {
		return this.getEnvironmentAdaptor().getRSSInstancesList();
	}

	/**
	 * @see RSSManagerService#addDatabase
	 */
	public DatabaseInfo addDatabase(String environmentName,
	                                DatabaseInfo database) throws RSSManagerException {
		return this.getEnvironmentAdaptor().addDatabase(environmentName, database);
	}

	/**
	 * @see RSSManagerService#removeDatabase
	 */
	public void removeDatabase(String environmentName, String rssInstanceName,
	                           String databaseName, String type) throws RSSManagerException {
		this.getEnvironmentAdaptor().removeDatabase(environmentName, rssInstanceName, databaseName, type);
	}

	/**
	 * @see RSSManagerService#getDatabases
	 */
	public DatabaseInfo[] getDatabases(String environmentName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabases(environmentName);
	}

	/**
	 * @see RSSManagerService#getDatabase
	 */
	public DatabaseInfo getDatabase(String environmentName, String rssInstanceName,
	                                String databaseName, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabase(environmentName, rssInstanceName,
		                                                databaseName, type);
	}

	/**
	 * @see RSSManagerService#isDatabaseExist
	 */
	public boolean isDatabaseExist(String environmentName, String rssInstanceName,
	                               String databaseName, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().isDatabaseExist(environmentName, rssInstanceName,
		                                                    databaseName, type);
	}

	/**
	 * @see RSSManagerService#isDatabaseUserExist
	 */
	public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
	                                   String username, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().isDatabaseUserExist(environmentName, rssInstanceName,
		                                                        username, type);
	}

	/**
	 * @see RSSManagerService#isDatabasePrivilegeTemplateExist
	 */
	public boolean isDatabasePrivilegeTemplateExist(
			String environmentName, String templateName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().isDatabasePrivilegeTemplateExist(environmentName,
		                                                                     templateName);
	}

	/**
	 * @see RSSManagerService#addDatabaseUser
	 */
	public DatabaseUserInfo addDatabaseUser(String environmentName,
	                                        DatabaseUserInfo user) throws RSSManagerException {
		String username = user.getName().trim();
		if (!StringUtils.isAlphanumeric(username)) {
			String msg = "Only Alphanumeric characters and underscores "
			             + "are allowed in database username";
			log.error(msg);
			throw new RSSManagerException(msg);
		} else {
			return this.getEnvironmentAdaptor().addDatabaseUser(
					environmentName, user);
		}
	}

	/**
	 * @see RSSManagerService#removeDatabaseUser
	 */
	public void removeDatabaseUser(String environmentName, String rssInstanceName,
	                               String username, String type) throws RSSManagerException {
		this.getEnvironmentAdaptor().removeDatabaseUser(environmentName, rssInstanceName, username, type);
	}

	/**
	 * @see RSSManagerService#deleteTenantRSSData
	 */
	public boolean deleteTenantRSSData(String environmentName, String tenantDomain)
			throws RSSManagerException {
		return this.getEnvironmentAdaptor().deleteTenantRSSData(environmentName, tenantDomain);
	}

	/**
	 * @see RSSManagerService#updateDatabaseUserPrivileges
	 */
	public void updateDatabaseUserPrivileges(String environmentName,
	                                         DatabasePrivilegeSetInfo privileges,
	                                         DatabaseUserInfo user,
	                                         String databaseName) throws RSSManagerException {
		this.getEnvironmentAdaptor().updateDatabaseUserPrivileges(environmentName, privileges, user,
		                                                          databaseName);
	}

	/**
	 * @see RSSManagerService#getDatabaseUser
	 */
	public DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstance,
	                                        String username, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabaseUser(environmentName, rssInstance, username, type);
	}

	/**
	 * @see RSSManagerService#getDatabaseUsers
	 */
	public DatabaseUserInfo[] getDatabaseUsers(
			String environmentName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabaseUsers(environmentName);
	}

	public void addDatabasePrivilegeTemplate(String environmentName,
	                                         DatabasePrivilegeTemplateInfo template) throws RSSManagerException {
		String tempplateName = template.getName().trim();
		if (!StringUtils.isAlphanumeric(tempplateName)) {
			String msg = "Only Alphanumeric characters and underscores "
			             + "are allowed in database privilege template name";
			log.error(msg);
			throw new RSSManagerException(msg);
		} else {
			this.getEnvironmentAdaptor().addDatabasePrivilegeTemplate(
					environmentName, template);
		}
	}

	/**
	 * @see RSSManagerService#removeDatabasePrivilegeTemplate
	 */
	public void removeDatabasePrivilegeTemplate(String environmentName,
	                                            String templateName) throws RSSManagerException {
		this.getEnvironmentAdaptor().removeDatabasePrivilegeTemplate(environmentName, templateName);
	}

	/**
	 * @see RSSManagerService#updateDatabasePrivilegeTemplate
	 */
	public void updateDatabasePrivilegeTemplate(
			String environmentName,
			DatabasePrivilegeTemplateInfo template) throws RSSManagerException {
		this.getEnvironmentAdaptor().updateDatabasePrivilegeTemplate(environmentName, template);
	}

	/**
	 * @see RSSManagerService#getDatabasePrivilegeTemplates
	 */
	public DatabasePrivilegeTemplateInfo[] getDatabasePrivilegeTemplates(
			String environmentName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplates(environmentName);
	}

	/**
	 * @see RSSManagerService#getDatabasePrivilegeTemplate
	 */
	public DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(
			String environmentName, String templateName) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplate(environmentName,
		                                                                 templateName);
	}

	/**
	 * @see RSSManagerService#attachUser
	 */
	public void attachUser(String environmentName, UserDatabaseEntryInfo ude,
	                       String templateName) throws RSSManagerException {
		this.getEnvironmentAdaptor().attachUser(environmentName, ude, templateName);
	}

	/**
	 * @see RSSManagerService#detachUser
	 */
	public void detachUser(String environmentName,
	                       UserDatabaseEntryInfo databaseEntryInfo) throws RSSManagerException {
		this.getEnvironmentAdaptor().detachUser(environmentName, databaseEntryInfo);
	}

	/**
	 * @see RSSManagerService#getAttachedUsers
	 */
	public DatabaseUserInfo[] getAttachedUsers(String environmentName, String rssInstanceName,
	                                           String databaseName, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getAttachedUsers(environmentName, rssInstanceName,
		                                                     databaseName, type);
	}

	/**
	 * @see RSSManagerService#getAvailableUsers
	 */
	public DatabaseUserInfo[] getAvailableUsers(String environmentName, String rssInstanceName,
	                                            String databaseName, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getAvailableUsers(environmentName, rssInstanceName,
		                                                      databaseName, type);
	}

	/**
	 * @see RSSManagerService#addCarbonDataSource
	 */
	public void addCarbonDataSource(String environmentName,
	                                String dataSourceName, UserDatabaseEntryInfo entry)
			throws RSSManagerException {
		this.getEnvironmentAdaptor().addCarbonDataSource(environmentName,
		                                                 dataSourceName, entry);
	}

	/**
	 * @see RSSManagerService#getUserDatabasePrivileges
	 */
	public DatabasePrivilegeSetInfo getUserDatabasePrivileges(
			String environmentName, String rssInstanceName, String databaseName,
			String username, String type) throws RSSManagerException {
		return this.getEnvironmentAdaptor().getUserDatabasePrivileges(environmentName,
		                                                              rssInstanceName, databaseName, username, type);
	}

	/**
	 * @see RSSManagerService#getDatabasesForTenant
	 */
	public DatabaseInfo[] getDatabasesForTenant(String environmentName,
	                                            String tenantDomain) throws RSSManagerException {
		int tenantId = -1;
		DatabaseInfo[] databases = null;
		if (!RSSManagerUtil.isSuperTenantUser()) {
			String msg = "Unauthorized operation, only super tenant is authorized. " +
			             "Tenant domain :" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
			             " permission denied";
			throw new RSSManagerException(msg);
		}
		try {
			tenantId = RSSManagerUtil.getTenantId(tenantDomain);
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
			databases = this.getDatabases(environmentName);
		} catch (RSSManagerCommonException e) {
			String msg = "Error occurred while retrieving database list of tenant '" +
			             tenantDomain + "'";
			throw new RSSManagerException(msg, e);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
		return databases;
	}

	/**
	 * @see RSSManagerService#addDatabaseForTenant
	 */
	public void addDatabaseForTenant(String environmentName, DatabaseInfo database,
	                                 String tenantDomain) throws RSSManagerException {
		if (!RSSManagerUtil.isSuperTenantUser()) {
			String msg = "Unauthorized operation, only super tenant is authorized to perform " +
			             "this operation permission denied";
			log.error(msg);
			throw new RSSManagerException(msg);
		}
		try {
			int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
			this.addDatabase(environmentName, database);
		} catch (RSSManagerException e) {
			log.error("Error occurred while creating database for tenant : " + e.getMessage(), e);
			throw e;
		} catch (RSSManagerCommonException e) {
			String msg = "Error occurred while creating database '" + database.getName() +
			             "' for tenant '" + tenantDomain + "' on RSS instance '" +
			             database.getRssInstanceName() + "'";
			throw new RSSManagerException(msg, e);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * @see RSSManagerService#getDatabaseForTenant
	 */
	public DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName,
	                                         String databaseName,
	                                         String tenantDomain, String type) throws RSSManagerException {
		DatabaseInfo metaData = null;
		if (!RSSManagerUtil.isSuperTenantUser()) {
			String msg = "Unauthorized operation, only super tenant is authorized to perform " +
			             "this operation permission denied";
			log.error(msg);
			throw new RSSManagerException(msg);
		}
		try {
			int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
			metaData = this.getDatabase(environmentName, rssInstanceName, databaseName, type);
		} catch (RSSManagerCommonException e) {
			String msg = "Error occurred while retrieving metadata of the database '" +
			             databaseName + "' belonging to tenant '" + tenantDomain + "'";
			throw new RSSManagerException(msg, e);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
		return metaData;
	}

	/**
	 * @see RSSManagerService#getEnvironments
	 */
	public String[] getEnvironments() throws RSSManagerException {
		return this.getEnvironmentAdaptor().getEnvironments();
	}

	/**
	 * Test the RSS instance connection using a mock database connection test.
	 *
	 * @param driverClass JDBC Driver class
	 * @param jdbcURL     JDBC url
	 * @param username    username
	 * @param password    password
	 * @return Success or failure message
	 * @throws RSSManagerException RSSDAOException
	 */
	public void testConnection(String driverClass, String jdbcURL, String username,
	                           String password) throws RSSManagerException {
		Connection conn = null;
		int tenantId = RSSManagerUtil.getTenantId();

		if (driverClass == null || driverClass.length() == 0) {
			String msg = "Driver class is missing";
			throw new RSSManagerException(msg);
		}
		if (jdbcURL == null || jdbcURL.length() == 0) {
			String msg = "Driver connection URL is missing";
			throw new RSSManagerException(msg);
		}
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

			Class.forName(driverClass).newInstance();
			conn = DriverManager.getConnection(jdbcURL, username, password);
			if (conn == null) {
				String msg = "Unable to establish a JDBC connection with the database server";
				throw new RSSManagerException(msg);
			}
		} catch (Exception e) {
			String msg = "Error occurred while testing database connectivity : " + e.getMessage();
			handleException(msg, e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	private EnvironmentAdaptor getEnvironmentAdaptor() throws RSSManagerException {
		EnvironmentAdaptor adaptor =
				RSSConfigurationManager.getInstance().getRSSManagerEnvironmentAdaptor();
		if (adaptor == null) {
			throw new RuntimeException("RSS Manager Environment Adaptor is not " +
			                           "initialized properly");
		}
		return adaptor;
	}

	private void handleException(String msg, Exception e) throws RSSManagerException {
		log.error(msg, e);
		throw new RSSManagerException(msg, e);
	}

	/**
	 * @see RSSManagerService#editDatabaseUser
	 */
	@Override
	public DatabaseUserInfo editDatabaseUser(String environmentName, DatabaseUserInfo databaseUserInfo) throws RSSManagerException {
		return this.getEnvironmentAdaptor().editDatabaseUser(environmentName, databaseUserInfo);
	}

	/**
	 * @see RSSManagerService#getRSSProvider
	 */
	@Override
	public String getRSSProvider() {
		return RSSConfigurationManager.getInstance().getCurrentRSSConfig().getRSSProvider();
	}

}
