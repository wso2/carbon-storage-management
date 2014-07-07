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

package org.wso2.carbon.rssmanager.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.EntityType;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public abstract class SystemRSSManager extends AbstractRSSManager {

    public SystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public Database[] getDatabases() throws RSSManagerException {
        Database[] databases = new Database[0];
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
            closeJPASession();
        }
        return databases;
    }

    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
                    tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
            closeJPASession();
        }
        return users;
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        DatabaseUser user = null;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseUserDAO()
                    .resolveRSSInstanceByUser(this.getEnvironmentName(),
                            rssInstanceName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            username, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                getEntityManager().rollbackJPATransaction();
                throw new RSSManagerException(
                        "Database user '" + username + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }
            user = getRSSDAO().getDatabaseUserDAO()
                    .getDatabaseUser(getEnvironmentName(), rssInstance.getName(), username,
                            tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
                    "database user '" + username + "' from RSS metadata " + "repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            //closeJPASession();
        }
        return user;
    }

    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        Database database = null;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(getEnvironmentName(),
                            rssInstanceName,
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx) {
                    getEntityManager().rollbackJPATransaction();
                }
                throw new RSSManagerException(
                        "Database '" + databaseName + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }
            database = getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(),
                    rssInstance.getName(),
                    databaseName, tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
                    "database '" + databaseName + "' from RSS metadata " + "repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            //closeJPASession();
        }
        return database;
    }

    public DatabaseUser[] getAttachedUsers(String rssInstanceName, String databaseName)
            throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(getEnvironmentName(),
                            rssInstanceName,
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                getEntityManager().rollbackJPATransaction();
                throw new RSSManagerException(
                        "Database '" + databaseName + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }
            users = getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                    rssInstance.getName(),
                    databaseName, tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata " + "corresponding to the " +
                    "database users attached to the database '" + databaseName +
                    "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
        return users;
    }

	public DatabaseUser[] getAvailableUsers(String rssInstanceName, String databaseName)
	                                                                                    throws RSSManagerException {
		DatabaseUser[] users = new DatabaseUser[0];
		boolean inTx = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			rssInstanceName = getRSSDAO().getDatabaseDAO()
			                             .resolveRSSInstanceByDatabase(getEnvironmentName(),
			                                                           rssInstanceName,
			                                                           databaseName,
			                                                           RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
			                                                           tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			DatabaseUserDAO dao = getRSSDAO().getDatabaseUserDAO();
			DatabaseUser[] existingUsers = dao.getAvailableDatabaseUsers(getEnvironmentName(),
			                                                             rssInstance.getName(), databaseName,
			                                                             tenantId);

			List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
			DatabaseUser[] assignedUsers = dao.getAssignedDatabaseUsers(getEnvironmentName(),
			                                                            rssInstanceName, databaseName,
			                                                            tenantId);

			List<DatabaseUser> attachedUsers = Collections.EMPTY_LIST;
			if (assignedUsers != null && assignedUsers.length > 0) {
				attachedUsers = Arrays.asList(assignedUsers);
			}

			for (DatabaseUser user : existingUsers) {
				if (!attachedUsers.contains(user)) {
					availableUsers.add(user);
				}
			}

			users = availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
			if (inTx) {
				getEntityManager().endJPATransaction();
			}
		} catch (Exception e) {
			if (inTx) {
				getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while retrieving metadata corresponding to available " + "database users to be attached to the database'" + databaseName + "' from RSS metadata repository : " + e.getMessage();
			handleException(msg, e);
		} finally {
			closeJPASession();
		}
		return users;
	}

    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
                                                          String username) throws RSSManagerException {
        DatabasePrivilegeSet privilegesSet = null;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName = getRSSDAO().getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(getEnvironmentName(),
                            rssInstanceName,
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx) {
                    getEntityManager().rollbackJPATransaction();
                }
                throw new RSSManagerException(
                        "Database '" + databaseName + "' does not exist " + "in RSS instance '" +
                                rssInstanceName + "'");
            }

            UserDatabasePrivilege privileges = getRSSDAO().getUserPrivilegesDAO()
                    .getUserDatabasePrivileges(this.getEnvironmentName(),
                            rssInstance.getName(), databaseName, username,
                            tenantId);
            if (privileges != null) {
                privilegesSet = new MySQLPrivilegeSet();
            }
            RSSManagerUtil.createDatabasePrivilegeSet(privilegesSet, privileges);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while retrieving metadata corresponding to the " +
                    "database privileges assigned to database user '" + username +
                    "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
        return privilegesSet;
    }

    public RSSInstance resolveRSSInstanceByDatabase(String databaseName) throws RSSManagerException {
        RSSInstance rssInstance;
        boolean inTx = false;
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO()
                    .getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(this.getEnvironmentName(),
                            null,
                            databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
                            tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
            return this.getEnvironment().getRSSInstance(rssInstanceName);
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw new RSSManagerException("Error occurred while resolving RSS instance", e);
        } finally {

        }
    }

    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(getEnvironmentName(),
                    rssInstanceName, databaseName, tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while checking whether the database " + "named '" +
                    databaseName + "' exists in RSS instance '" + rssInstanceName + "': " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
        return isExist;
    }

    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String username) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),
                    rssInstanceName, username,
                    tenantId);
            if (inTx) {
                getEntityManager().endJPATransaction();
            }
        } catch (RSSDAOException e) {
            if (inTx) {
                getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while checking whether the database " + "user named '" +
                    username + "' already exists in RSS instance '" + rssInstanceName + "': " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
        return isExist;
    }

    /**
     * transactional operations moved to common place -- tx not committed in here but in subclass
     */

    protected void removeDatabase(AtomicBoolean isInTx, String rssInstanceName, String databaseName,
                                  RSSInstance rssInstance) throws RSSManagerException, RSSDAOException {

        int tenantId = RSSManagerUtil.getTenantId();
        DatabaseDAO dao = this.getRSSDAO().getDatabaseDAO();
        Database database =
                dao.getDatabase(getEnvironmentName(), rssInstanceName, databaseName, tenantId);

        //this.closeJPASession();

        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);

        //overrideJPASession(dao);
        joinTransaction();
        //database = dao.merge(database);
        if (database.getUserDatabaseEntries() != null &&
                !database.getUserDatabaseEntries().isEmpty()) {
            for (UserDatabaseEntry entry : database.getUserDatabaseEntries()) {
            	UserDatabasePrivilege privileges = entry.getUserPrivileges();
                if (privileges != null ) {
                   // this.getRSSDAO().getUserPrivilegesDAO().remove(privileges);
                    //entry.setUserPrivileges(null);
                }
                //entry.setUserPrivileges(null);
               // this.getRSSDAO().getUserDatabaseEntryDAO().remove(entry);
            }
        }
        
        this.getRSSDAO().getUserPrivilegesDAO().removeUserDatabasePrivilegeEntriesByDatabase(rssInstance, database.getName(), tenantId);
		this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(database.getId());
        //database.setUserDatabaseEntries(null);
        this.getRSSDAO().getDatabaseDAO().removeDatabase(database);

    }

    protected void removeDatabaseUser(AtomicBoolean isInTx, String type,
                                      String username) throws RSSManagerException, RSSDAOException {

        final int tenantId = RSSManagerUtil.getTenantId();
        DatabaseUserDAO dao = this.getRSSDAO().getDatabaseUserDAO();
        DatabaseUser user = dao.getDatabaseUser(getEnvironmentName(), username, tenantId);
        boolean isExist = (user == null ? false : true);
        if (!isExist) {
            String msg = "Database user '" + user.getName() + "' not exists";
            throw new EntityAlreadyExistsException(msg);
        }
        
        List<UserDatabaseEntry> userDBEntries = user.getUserDatabaseEntries();
        if(userDBEntries != null && !userDBEntries.isEmpty()){
        	String msg = "Database user '" + user.getName() + "' already attached to a Database ";
            throw new EntityAlreadyExistsException(msg);
        }
        //this.closeJPASession();
		/* Initiating the transaction */
        boolean inTx = this.getEntityManager().beginTransaction();
        isInTx.set(inTx);

		/*this.getRSSDAO().getUserPrivilegesDAO()
		    .removeDatabasePrivileges(getEnvironmentName(), username, tenantId);*/
        //overrideJPASession(dao);
        joinTransaction();
        user = (DatabaseUser) dao.merge(user);
        this.getRSSDAO().getDatabaseUserDAO().removeDatabaseUser(user);

    }

    protected RSSInstance detachUser(AtomicBoolean isInTx,
                                     UserDatabaseEntry entry) throws RSSManagerException, RSSDAOException {
        Database database = this.getDatabase(entry.getRssInstanceName(), entry.getDatabaseName());
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
            throw new EntityNotFoundException(msg);
        }

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(entry.getDatabaseName());
        if (rssInstance == null) {
            String msg = "RSS instance '" + entry.getRssInstanceName() + "' does not exist";
            throw new EntityNotFoundException(msg);
        }

        final int tenantId = RSSManagerUtil.getTenantId();
        UserDatabaseEntryDAO dao = this.getRSSDAO().getUserDatabaseEntryDAO();
        UserDatabaseEntry userDBEntry = dao.getUserDatabaseEntry(getEnvironment().getId(),rssInstance.getId(), entry, tenantId);
        if(userDBEntry == null){
        	String msg = "Database '" + entry.getDatabaseName() + "' does not attached User "+ entry.getUsername();
        	throw new EntityNotFoundException(msg);
        }
        //this.closeJPASession();
        /* Initiating the distributed transaction */
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        
       /* 
        
        this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByUser(userDBEntry.getId());*/
        //overrideJPASession(dao);
        joinTransaction();
        //userDBEntry = (UserDatabaseEntry) dao.merge(userDBEntry);
        if (userDBEntry.getUserPrivileges() != null) {
            this.getRSSDAO().getUserPrivilegesDAO().remove(userDBEntry.getUserPrivileges());
            userDBEntry.setUserPrivileges(null);
        }

        dao.remove(userDBEntry);
        database.getUserDatabaseEntries().remove(userDBEntry);
        return rssInstance;
    }

    protected Database addDatabase(AtomicBoolean isInTx, Database database, RSSInstance rssInstance,
                                   String qualifiedDatabaseName) throws RSSManagerException, RSSDAOException {
        RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
        final int tenantId = RSSManagerUtil.getTenantId();
        RSSInstanceDAO instanceDAO = (RSSInstanceDAO) EntityType.RSSInstance.getEntityDAO(getEntityManager());
        RSSInstance serverEntity = instanceDAO.getRSSInstance(getEnvironmentName(), rssInstance.getName(), MultitenantConstants.SUPER_TENANT_ID);

       //closeJPASession();

        boolean inTx = getEntityManager().beginTransaction();
       	//boolean inTx = getEntityManager().getAndSetTransaction();
        isInTx.set(inTx);
        //instanceDAO.overrideJPASession(getEntityManager().getJpaUtil().getJPAEntityManager());
        //getEntityManager().beginTransactionOnly();
        joinTransaction();
        serverEntity = instanceDAO.merge(serverEntity);
        database.setName(qualifiedDatabaseName);
        database.setRssInstanceName(rssInstance.getName());
        String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
        database.setUrl(databaseUrl);
        database.setType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        /* creates a reference to the database inside the metadata repository */

        database.setRssInstance(serverEntity);
        database.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseDAO().insert(database);
        //getEntityManager().getJpaUtil().getJPAEntityManager().flush();
        return database;

    }

    protected DatabaseUser addDatabaseUser(AtomicBoolean isInTx, DatabaseUser user,
                                           String qualifiedUsername) throws RSSManagerException, RSSDAOException {

        boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
        if (isExist) {
            String msg = "Database user '" + qualifiedUsername + "' already exists";
            throw new EntityAlreadyExistsException(msg);
        }

		/* Sets the fully qualified username */
        user.setName(qualifiedUsername);
        user.setRssInstanceName(user.getRssInstanceName());
        user.setType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        Set<String> envNames = Collections.singleton(getEnvironmentName());

        EnvironmentDAO entityDAO = (EnvironmentDAO) EntityType.Environment.getEntityDAO(getEntityManager());
        Set<Environment> envrionments = entityDAO.getEnvironments(envNames);

        Set<RSSInstance> servers = new HashSet<RSSInstance>();
        for (Environment env : envrionments) {
        	user.setEnvironmentId(env.getId());
            if (env.getRssInstanceEntities() != null) {

                for (RSSInstance server : env.getRssInstanceEntities()) {
                    if (MultitenantConstants.SUPER_TENANT_ID == server.getTenantId().intValue()) {
                        servers.addAll(env.getRssInstanceEntities());
                    }
                }

            }
        }

        closeJPASession();

		/* Initiating the distributed transaction */
        user.setInstances(servers);
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        final int tenantId = RSSManagerUtil.getTenantId();
        user.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseUserDAO().merge(user);

        return user;
    }

    protected void attachUser(AtomicBoolean isInTx, UserDatabaseEntry entry,
                              DatabasePrivilegeSet privileges, RSSInstance rssInstance)
            throws RSSManagerException,
            RSSDAOException {
        boolean inTx = false;

        String rssInstanceName = rssInstance.getName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        if (rssInstance == null) {
            String msg = "RSS instance " + rssInstanceName + " does not exist";

            throw new EntityNotFoundException(msg);
        }

        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";

            throw new EntityNotFoundException(msg);
        }

        DatabaseUser user = this.getDatabaseUser(rssInstanceName, username);
        if (user == null) {
            String msg = "Database user '" + entry.getUsername() + "' does not exist";

            throw new EntityNotFoundException(msg);
        }

        entry.setDatabaseId(database.getId());
        entry.setUserId(user.getId());

        entry.setDatabase(database);
        entry.setDatabaseUser(user);

        inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        UserDatabasePrivilege privilegeEntity = new UserDatabasePrivilege();
        RSSManagerUtil.createDatabasePrivilege(privileges, privilegeEntity);
        
        //ntry.setUserPrivileges(null);
        entry.setUserPrivileges(privilegeEntity);
        privilegeEntity.setUserDatabaseEntry(entry);
        this.getRSSDAO().getUserDatabaseEntryDAO().insert(entry);
        //this.getRSSDAO().getUserPrivilegesDAO().insert(privilegeEntity);
    }


}
