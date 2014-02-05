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

package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.manager.adaptor.EnvironmentAdaptor;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class RSSManagerServiceImpl implements RSSManagerService {

    private static final Log log = LogFactory.getLog(RSSManagerService.class);

    public void addRSSInstance(String environmentName,
                               RSSInstanceInfo rssInstance) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().addRSSInstance(environmentName, rssInstance);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating RSS instance '" + rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public void removeRSSInstance(String environmentName, String rssInstanceName,
                                  String type) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().removeRSSInstance(environmentName, rssInstanceName, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the RSS instance '" + rssInstanceName + "'";
            handleException(msg, e);
        }
    }

    public void updateRSSInstance(String environmentName,
                                  RSSInstanceInfo rssInstance) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().updateRSSInstance(environmentName, rssInstance);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the configuration of RSS instance '" +
                    rssInstance.getName() + "'";
            handleException(msg, e);
        }
    }

    public RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName,
                                      String type) throws RSSManagerException {
        RSSInstanceInfo metadata = null;
        try {
            metadata =
                    this.getEnvironmentAdaptor().getRSSInstance(environmentName, rssInstanceName,
                            type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of RSS instance '" +
                    rssInstanceName + "'";
            handleException(msg, e);
        }
        return metadata;
    }

    public RSSInstanceInfo [] getRSSInstances(String environmentName) throws RSSManagerException {
        RSSInstanceInfo [] rssInstances = new RSSInstanceInfo [0];
        try {
            rssInstances = this.getEnvironmentAdaptor().getRSSInstances(environmentName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred in retrieving the RSS instance list";
            handleException(msg, e);
        }
        return rssInstances;
    }

    public DatabaseInfo addDatabase(String environmentName,
                                DatabaseInfo database) throws RSSManagerException {
        try {
            return this.getEnvironmentAdaptor().addDatabase(environmentName, database);
        } catch (RSSManagerException e) {
            String msg = "Error in creating the database '" + database.getName() + "'";
            handleException(msg, e);
        }
        return null;
    }

    public void removeDatabase(String environmentName, String rssInstanceName,
                               String databaseName, String type) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().removeDatabase(environmentName, rssInstanceName,
                    databaseName, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database '" + databaseName + "'";
            handleException(msg, e);
        }
    }

    public DatabaseInfo [] getDatabases(String environmentName) throws RSSManagerException {
        DatabaseInfo [] databases = new DatabaseInfo [0];
        try {
            databases = this.getEnvironmentAdaptor().getDatabases(environmentName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the database list of the tenant";
            handleException(msg, e);
        }
        return databases;
    }


    public boolean deleteTenantRSSData(String environmentName, String tenantDomain)
            throws RSSManagerException {
        boolean isDeleted = false;
        if (!RSSManagerUtil.isSuperTenantUser()) {
            throw new RSSManagerException("Unahuthorized access");
        }
        try {
            isDeleted = this.getEnvironmentAdaptor().deleteTenantRSSData(environmentName,
                    tenantDomain);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the database list of the tenant '"
                    + tenantDomain + "'";
            handleException(msg, e);
        }
        return isDeleted;
    }

    public String[] getEnvironments() throws RSSManagerException {
        String[] environments = new String[0];
        try {
            if (!RSSManagerUtil.isSuperTenantUser()) {
                throw new RSSManagerException("Unauthorized access ");
            }
            environments = this.getEnvironmentAdaptor().getEnvironments();
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the list of available RSS environments";
            handleException(msg, e);
        }
        return environments;
    }

    public DatabaseInfo getDatabase(String environmentName, String rssInstanceName,
                                String databaseName, String type) throws RSSManagerException {
        DatabaseInfo database = null;
        try {
            database = this.getEnvironmentAdaptor().getDatabase(environmentName, rssInstanceName,
                    databaseName, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the configuration of the database '" +
                    databaseName + "'";
            handleException(msg, e);
        }
        return database;
    }

    public boolean isDatabaseExist(String environmentName, String rssInstanceName,
                                   String databaseName, String type) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabaseExist(environmentName, rssInstanceName, databaseName, type);
    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String username, String type) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabaseUserExist(environmentName, rssInstanceName, username, type);
    }

    public DatabaseUserInfo addDatabaseUser(String environmentName,
                                        DatabaseUserInfo user) throws RSSManagerException {
        try {
            return this.getEnvironmentAdaptor().addDatabaseUser(environmentName, user);
        } catch (RSSManagerException e) {
            String msg =
                    "Error occurred while creating the database user '" + user.getName() + "'";
            handleException(msg, e);
        }
        return null;
    }

    public void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
                                   String type) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().removeDatabaseUser(environmentName, rssInstanceName, username, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the user '" + username + "'";
            handleException(msg, e);
        }
    }

    public void updateDatabaseUserPrivileges(String environmentName,
                                             DatabasePrivilegeSetInfo privileges,
                                             DatabaseUserInfo user,
                                             String databaseName) throws RSSManagerException {
        this.getEnvironmentAdaptor().updateDatabaseUserPrivileges(environmentName, privileges, user,
                databaseName);
    }


    public DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstanceName, String username,
                                        String type) throws RSSManagerException {
        DatabaseUserInfo user = null;
        try {
            user = this.getEnvironmentAdaptor().getDatabaseUser(environmentName, rssInstanceName, username, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privileges of the user '" +
                    username + "'";
            handleException(msg, e);
        }
        return user;
    }

    public DatabaseUserInfo [] getDatabaseUsers(String environmentName) throws RSSManagerException {
        DatabaseUserInfo [] users = new DatabaseUserInfo [0];
        try {
            users = this.getEnvironmentAdaptor().getDatabaseUsers(environmentName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database user list";
            handleException(msg, e);
        }
        return users;
    }

    public void addDatabasePrivilegeTemplate(
            String environmentName, DatabasePrivilegeTemplateInfo template) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().addDatabasePrivilegeTemplate(environmentName, template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while creating the database privilege template '" +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public void removeDatabasePrivilegeTemplate(String environmentName,
                                                String templateName) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().removeDatabasePrivilegeTemplate(environmentName,
                    templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while dropping the database privilege template '" +
                    templateName + "'";
            handleException(msg, e);
        }
    }

    public void updateDatabasePrivilegeTemplate(
            String environmentName, DatabasePrivilegeTemplateInfo template) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().updateDatabasePrivilegeTemplate(environmentName, template);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while editing the database privilege template " +
                    template.getName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeTemplateInfo [] getDatabasePrivilegeTemplates(
            String environmentName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplates(environmentName);
    }

    public DatabasePrivilegeTemplateInfo getDatabasePrivilegeTemplate(
            String environmentName, String templateName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplate(environmentName,
                templateName);
    }

//    public void createCarbonDataSource(String databaseName, String username) throws
//            RSSManagerException {
//        RSSDAO dao = RSSDAOFactory.getRSSDAO();
//
//        int tenantId = this.getCurrentTenantId();
//        PrivilegedCarbonContext.startTenantFlow();
//        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
//
//        DatabaseInfo database;
//        DatabaseUserInfo user;
//        String dsName = null;
//        try {
//            database = dao.getDatabase(databaseName);
//            user = dao.getDatabaseUser(username);
//
//            DataSourceMetaInfo.DataSourceDefinition dsDef =
//                    new DataSourceMetaInfo.DataSourceDefinition();
//            dsDef.setDsXMLConfiguration(null);
//            dsDef.setType(null);
//
//            DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
//            dsName = database.getName() + "_" + user.getUsername();
//            metaInfo.setName(dsName);
//            metaInfo.setDefinition(dsDef);
//
//            RSSManagerServiceComponent.getDataSourceService().addDataSource(metaInfo);
//        } catch (RSSManagerException e) {
//            String msg = "Error occurred while creating datasource'" +
//                    username + "'";
//            handleException(msg, e);
//        } catch (DataSourceException e) {
//            String msg = "Error occurred while creating the datasource '" + dsName + "'";
//            handleException(msg, e);
//        } finally {
//            PrivilegedCarbonContext.endTenantFlow();
//        }
//    }

    public void attachUser(String environmentName, UserDatabaseEntryInfo ude,
                           String templateName) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().attachUser(environmentName, ude, templateName);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while attaching database user '" + ude.getUsername() +
                    "' to the database '" + ude.getDatabaseName() + "' with the database user " +
                    "privileges define in the database privilege template '" + templateName + "'";
            handleException(msg, e);
        }
    }

    public void detachUser(String environmentName,
                           UserDatabaseEntryInfo ude) throws RSSManagerException {
        try {
            this.getEnvironmentAdaptor().detachUser(environmentName, ude);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while detaching the database user '" + ude.getUsername() +
                    "' from the database '" + ude.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabaseUserInfo [] getAttachedUsers(String environmentName, String rssInstanceName,
                                           String databaseName, String type) throws RSSManagerException {
        DatabaseUserInfo [] users = new DatabaseUserInfo [0];
        try {
            users = this.getEnvironmentAdaptor().getAttachedUsers(environmentName, rssInstanceName,
                    databaseName, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users attached to " +
                    "the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }

    public DatabaseUserInfo [] getAvailableUsers(String environmentName, String rssInstanceName,
                                            String databaseName, String type) throws RSSManagerException {
        DatabaseUserInfo [] users = new DatabaseUserInfo [0];
        try {
            users = this.getEnvironmentAdaptor().getAvailableUsers(environmentName, rssInstanceName,
                    databaseName, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving database users available to be " +
                    "attached to the database '" + databaseName + "'";
            handleException(msg, e);
        }
        return users;
    }

    public void addCarbonDataSource(String environmentName,
                                    UserDatabaseEntryInfo entry) throws RSSManagerException {
        DatabaseInfo database =
                this.getDatabase(environmentName, entry.getRssInstanceName(),
                        entry.getDatabaseName(), entry.getType());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerDataHolder.getInstance().getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeSetInfo getUserDatabasePrivileges(
            String environmentName, String rssInstanceName, String databaseName,
            String username, String type) throws RSSManagerException {
        DatabasePrivilegeSetInfo privileges = null;
        try {
            privileges =
                    this.getEnvironmentAdaptor().getUserDatabasePrivileges(environmentName,
                            rssInstanceName, databaseName, username, type);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the permissions granted to the user '" +
                    username + "' on database '" + databaseName + "'";
            handleException(msg, e);
        }
        return privileges;
    }

    public DatabaseInfo [] getDatabasesForTenant(String environmentName,
                                            String tenantDomain) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabasesForTenant(environmentName, tenantDomain);
    }

    public void addDatabaseForTenant(String environmentName, DatabaseInfo database,
                                     String tenantDomain) throws RSSManagerException {
    	this.getEnvironmentAdaptor().addDatabaseForTenant(environmentName, database, tenantDomain);
    }

    public DatabaseInfo getDatabaseForTenant(String environmentName, String rssInstanceName,
                                         String databaseName,
                                         String tenantDomain, String type) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabaseForTenant(environmentName, rssInstanceName, databaseName, tenantDomain, type);
    }

    public boolean isDatabasePrivilegeTemplateExist(String environmentName,
                                                    String templateName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabasePrivilegeTemplateExist(environmentName, templateName);
    }

    private EnvironmentAdaptor getEnvironmentAdaptor() throws RSSManagerException {
        EnvironmentAdaptor adaptor =
                RSSConfigurationManager.getInstance().getRSSManagerEnvironmentAdaptor();
        if (adaptor == null) {
            throw new IllegalArgumentException("RSS Manager Environment Adaptor is not " +
                    "initialized properly");
        }
        return adaptor;
    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

}

