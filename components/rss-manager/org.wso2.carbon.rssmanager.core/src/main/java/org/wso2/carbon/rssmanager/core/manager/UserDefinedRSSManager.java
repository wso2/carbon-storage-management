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

package org.wso2.carbon.rssmanager.core.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public abstract class UserDefinedRSSManager extends AbstractRSSManager {

    public UserDefinedRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public Database[] getDatabases() throws RSSManagerException {
        Database[] databases = new Database[0];
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
                    tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),
                            rssInstanceName, username, tenantId);
            if (isExist) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' already exists " +
                        "in the RSS instance '" + rssInstanceName + "'");
            }
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(),
                    rssInstance.getName(), username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database user '" + username + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    this.getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database '" + databaseName + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                                     String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users already attached to database '" + databaseName + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + ", from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabaseUser[] getAvailableUsers(String rssInstanceName,
                                                              String databaseName) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            DatabaseUser[] existingUsers =
                    getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                            rssInstanceName, databaseName, tenantId);
            Set<String> usernames = new HashSet<String>();
            for (DatabaseUser user : existingUsers) {
                usernames.add(user.getName());
            }
            DatabaseUser[] tmp =
                    getRSSDAO().getDatabaseUserDAO().getDatabaseUsersByRSSInstance(getEnvironmentName(),
                            rssInstanceName, tenantId);
            List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
            for (DatabaseUser user : tmp) {
                String username = user.getName();
                if (!usernames.contains(username)) {
                    availableUsers.add(user);
                }
            }
            return availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database users available to be attached to database '" + databaseName +
                    "' which belongs to the RSS instance '" + rssInstanceName + ", from RSS " +
                    "metadata repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
    	
    	DatabasePrivilegeSet privilegesSet = null;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    this.getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            UserDatabasePrivilege privileges =  getRSSDAO().getUserPrivilegesDAO().getUserDatabasePrivileges(getEnvironmentName(),
                    rssInstance.getName(), databaseName, username, tenantId);
            
            if(privileges != null){
				privilegesSet = new MySQLPrivilegeSet();
			}
			RSSManagerUtil.createDatabasePrivilegeSet(privilegesSet, privileges);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "the privileges assigned to database user '" + username + "' which " +
                    "belongs to the RSS instance '" + rssInstanceName + " upon the database '" +
                    databaseName + "', from RSS metadata " +
                    "repository : " + e.getMessage(), e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
        
        return privilegesSet;
    }

    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(getEnvironmentName(),
                    rssInstanceName, databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "named '" + databaseName + "' exists in RSS instance '" + rssInstanceName +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String username) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),
                    rssInstanceName, username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "user named '" + username + "' already exists in RSS instance '" +
                    rssInstanceName + "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

}
