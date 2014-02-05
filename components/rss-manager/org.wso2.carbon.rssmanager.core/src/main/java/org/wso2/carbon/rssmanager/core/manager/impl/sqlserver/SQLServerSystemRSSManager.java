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
package org.wso2.carbon.rssmanager.core.manager.impl.sqlserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class SQLServerSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(SQLServerSystemRSSManager.class);

    public SQLServerSystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public Database addDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        final String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            String msg = "Database '" + qualifiedDatabaseName + "' already exists";
            log.error(msg);
            throw new EntityAlreadyExistsException(msg);
        }

        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            super.addDatabase(isInTx, database, rssInstance, qualifiedDatabaseName);

            stmt.execute();

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            try {
                conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            String msg = "Error while creating the database '" + qualifiedDatabaseName +
                    "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
        return database;
    }

    public void removeDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }
        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);

            removeDatabase(isInTx, rssInstance.getName(), databaseName, rssInstance);

            stmt.execute();

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            throw new RSSManagerException("Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;
        Map<String,String> mapUserwithInstance = new HashMap<String,String>();
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(user);
        String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());

        try {
            super.addDatabaseUser(isInTx, user, qualifiedUsername);

            for (RSSInstanceDSWrapper wrapper : getEnvironment().getDSWrapperRepository()
                    .getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    PrivilegedCarbonContext.endTenantFlow();

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);
                    String password = user.getPassword();
                    RSSManagerUtil.checkIfParameterSecured(qualifiedUsername);
                    String sql = "CREATE LOGIN " + qualifiedUsername + " WITH PASSWORD = '" + password + "'";
                    stmt = conn.prepareStatement(sql);
                    
                    stmt.execute();
                    mapUserwithInstance.put(rssInstance.getName(), qualifiedUsername);
                    conn.commit();
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while creating the database " +
                    "user '" + qualifiedUsername + "' : " + e.getMessage();
            
            if(!mapUserwithInstance.isEmpty()){
                dropAddedUsers(mapUserwithInstance);
            }
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
        return user;
    }
    
	private void dropAddedUsers(Map<String, String> mapUserwithInstance) {
		Set<Entry<String, String>> entries = mapUserwithInstance.entrySet();
		for (Entry<String, String> entry : entries) {
			String userName = entry.getValue();
			String instanceName = entry.getKey();
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = getConnection(instanceName);
				conn.setAutoCommit(false);

				String sql = "DROP LOGIN " + userName;
				stmt = conn.prepareStatement(sql);
				stmt.execute();
				conn.commit();
			} catch (Exception ex) {
				log.error(ex);
			} finally {
				RSSManagerUtil.cleanupResources(null, stmt, conn);
			}

		}
	}

    public void removeDatabaseUser(String type, String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        try {
            removeDatabaseUser(isInTx, type, username);

            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                            MultitenantConstants.SUPER_TENANT_ID);
                    rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    PrivilegedCarbonContext.endTenantFlow();

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);
                    RSSManagerUtil.checkIfParameterSecured(username);
                    String sql = "DROP LOGIN " + username;
                    stmt = conn.prepareStatement(sql);
                    stmt.execute();
                    conn.commit();
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }

            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtDetachUser = null;
        PreparedStatement stmtAddUser = null;
        PreparedStatement stmtGrant = null;
        PreparedStatement stmtDeny = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        String username = user.getName();

        try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }

            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                    this.getEnvironmentName(), null, databaseName,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                throw new EntityNotFoundException("Database '" + databaseName + "' does not exist in " +
                        "RSS instance '" + user.getRssInstanceName() + "'");
            }

            user.setRssInstanceName(rssInstance.getName());

            UserDatabasePrivilege entity = this.getRSSDAO()
                    .getUserPrivilegesDAO()
                    .getUserDatabasePrivileges(getEnvironmentName(),
                            rssInstanceName, databaseName,
                            user.getUsername(), tenantId);
            RSSManagerUtil.createDatabasePrivilege(privileges, entity);
            closeJPASession();

            boolean inTx = getEntityManager().beginTransaction();
            isInTx.set(inTx);

            this.getRSSDAO().getUserPrivilegesDAO().merge(entity);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sqlUseDb = "USE " + databaseName;
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlDetachUser = "DROP USER " + username;
            stmtDetachUser = conn.prepareStatement(sqlDetachUser);
            String sqlAddUser = "CREATE USER " + username + " FOR LOGIN " + username;
            stmtAddUser = conn.prepareStatement(sqlAddUser);
            String[] privilegeQueries = getPrivilegeQueries((MySQLPrivilegeSet) privileges,
                    username);
            if (privilegeQueries[0] != null) {
                stmtGrant = conn.prepareStatement(privilegeQueries[0]);
            }
            if (privilegeQueries[1] != null) {
                stmtDeny = conn.prepareStatement(privilegeQueries[1]);
            }

            stmtUseDb.execute();
            stmtDetachUser.execute();
            stmtAddUser.execute();
            if (stmtGrant != null) {
                stmtGrant.execute();
            }
            if (stmtDeny != null) {
                stmtDeny.execute();
            }

            conn.commit();

            /* ending distributed transaction */
            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while updating privileges of the database user '" +
                    user.getName() + "' for the database '" + databaseName + "' : " +
                    e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtDetachUser, null);
            RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
            RSSManagerUtil.cleanupResources(null, stmtGrant, null);
            RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
        }
    }

    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtAddUser = null;
        PreparedStatement stmtGrant = null;
        PreparedStatement stmtDeny = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);

        try {
            super.attachUser(isInTx, entry, privileges, rssInstance);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            if (privileges == null) {
                privileges = entry.getPrivileges();
            }
            String sqlUseDb = "USE " + databaseName;
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlAddUser = "CREATE USER " + username + " FOR LOGIN " + username;
            stmtAddUser = conn.prepareStatement(sqlAddUser);

            /*if (!(privileges instanceof SQLServerPrivilegeSet)) {
                throw new RuntimeException("Invalid privilege set defined");
            }*/
            String[] privilegeQueries = getPrivilegeQueries((MySQLPrivilegeSet) privileges,
                    username);
            if (privilegeQueries[0] != null) {
                stmtGrant = conn.prepareStatement(privilegeQueries[0]);
            }
            if (privilegeQueries[1] != null) {
                stmtDeny = conn.prepareStatement(privilegeQueries[1]);
            }

            stmtUseDb.execute();
            stmtAddUser.execute();
            if (stmtGrant != null) {
                stmtGrant.execute();
            }
            if (stmtDeny != null) {
                stmtDeny.execute();
            }

            conn.commit();

            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
            RSSManagerUtil.cleanupResources(null, stmtGrant, null);
            RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
        }
    }

    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmtUseDb = null;
        PreparedStatement stmtDetachUser = null;

        try {
            RSSInstance rssInstance = detachUser(isInTx, entry);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sqlUseDb = "USE " + entry.getDatabaseName();
            stmtUseDb = conn.prepareStatement(sqlUseDb);
            String sqlDetachUser = "DROP USER " + entry.getUsername();
            stmtDetachUser = conn.prepareStatement(sqlDetachUser);

            stmtUseDb.execute();
            stmtDetachUser.execute();

            conn.commit();

            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" +
                    entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                    "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
            RSSManagerUtil.cleanupResources(null, stmtDetachUser, conn);
            closeJPASession();
        }
    }

    private enum PRIVILEGE {
        SELECT("SELECT"),
        INSERT("INSERT"),
        UPDATE("UPDATE"),
        DELETE("DELETE"),
        CREATE("CREATE AGGREGATE, CREATE ASSEMBLY, CREATE ASYMMETRIC KEY, CREATE CERTIFICATE, " +
                "CREATE CONTRACT, CREATE DEFAULT, CREATE FULLTEXT CATALOG, CREATE FUNCTION, " +
                "CREATE MESSAGE TYPE, CREATE PROCEDURE, CREATE QUEUE, CREATE REMOTE SERVICE BINDING, " +
                "CREATE ROLE, CREATE RULE, CREATE SCHEMA, CREATE SERVICE, CREATE SYMMETRIC KEY, " +
                "CREATE SYNONYM, CREATE TABLE, CREATE TYPE, CREATE XML SCHEMA COLLECTION"),
        DROP(null),
        GRANT("WITH GRANT OPTION"),
        REFERENCES("REFERENCES"),
        INDEX(null),
        ALTER("ALTER, ALTER ANY APPLICATION ROLE, ALTER ANY ASSEMBLY, ALTER ANY ASYMMETRIC KEY, " +
                "ALTER ANY CERTIFICATE, ALTER ANY CONTRACT, ALTER ANY DATABASE AUDIT, " +
                "ALTER ANY DATASPACE, ALTER ANY FULLTEXT CATALOG, ALTER ANY MESSAGE TYPE, " +
                "ALTER ANY REMOTE SERVICE BINDING, ALTER ANY ROLE, ALTER ANY SCHEMA, " +
                "ALTER ANY SERVICE, ALTER ANY SYMMETRIC KEY, ALTER ANY USER"),
        CREATE_TEMP_TABLE(null),
        LOCK_TABLES(null),
        CREATE_VIEW("CREATE VIEW"),
        SHOW_VIEW(null),
        CREATE_ROUTINE("CREATE ROUTE"),
        ALTER_ROUTINE("ALTER ANY ROUTE"),
        EXECUTE("EXEC"),
        EVENT("CREATE DATABASE DDL EVENT NOTIFICATION, ALTER ANY DATABASE EVENT NOTIFICATION"),
        TRIGGER("ALTER ANY DATABASE DDL TRIGGER");

        private String text;

        PRIVILEGE(String text) {
            this.text = text;
        }

        private String getText() {
            return text;
        }
    }

    private String[] getPrivilegeQueries(MySQLPrivilegeSet privilegeSet, String username) {
        String[] queryArray = new String[2];
        List<String> grantList = new ArrayList<String>();
        List<String> denyList = new ArrayList<String>();
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getSelectPriv(),
                PRIVILEGE.SELECT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getInsertPriv(),
                PRIVILEGE.INSERT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getUpdatePriv(),
                PRIVILEGE.UPDATE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDeletePriv(),
                PRIVILEGE.DELETE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreatePriv(),
                PRIVILEGE.CREATE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDropPriv(),
                PRIVILEGE.DROP);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getReferencesPriv(),
                PRIVILEGE.REFERENCES);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getIndexPriv(),
                PRIVILEGE.INDEX);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterPriv(),
                PRIVILEGE.ALTER);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateTmpTablePriv(),
                PRIVILEGE.CREATE_TEMP_TABLE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getLockTablesPriv(),
                PRIVILEGE.LOCK_TABLES);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateViewPriv(),
                PRIVILEGE.CREATE_VIEW);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getShowViewPriv(),
                PRIVILEGE.SHOW_VIEW);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateRoutinePriv(),
                PRIVILEGE.CREATE_ROUTINE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterRoutinePriv(),
                PRIVILEGE.ALTER_ROUTINE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getExecutePriv(),
                PRIVILEGE.EXECUTE);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getEventPriv(),
                PRIVILEGE.EVENT);
        addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getTriggerPriv(),
                PRIVILEGE.TRIGGER);

        //creating grant query
        String grantString = "GRANT ";
        for (String privilegeString : grantList) {
            grantString += privilegeString.concat(",");
        }
        grantString =
                grantString.substring(0, grantString.length() - 1).concat(" TO ").concat(username);
        if (!grantList.isEmpty()) {
            if (isGranted(privilegeSet.getGrantPriv())) {
                grantString = grantString.concat(" ").concat(PRIVILEGE.GRANT.getText());
            }
        } else {
            grantString = null;
        }

        //creating deny query
        String denyString = "DENY ";
        for (String privilegeString : denyList) {
            denyString += privilegeString.concat(",");
        }
        if (!denyList.isEmpty()) {
            denyString =
                    denyString.substring(0, denyString.length() - 1).concat(" TO ").
                            concat(username).concat(" CASCADE");
        } else {
            denyString = null;
        }

        queryArray[0] = grantString;
        queryArray[1] = denyString;
        return queryArray;
    }

    private void addToGrantedListOrDenyList(List<String> grantList, List<String> denyList,
                                            String grantedOrNotString, PRIVILEGE enumPrivilege) {
        if (enumPrivilege.getText() == null) {                // permission is not supported
            return;
        }
        if (isGranted(grantedOrNotString)) {
            grantList.add(enumPrivilege.getText());
        } else {
            denyList.add(enumPrivilege.getText());
        }
    }

    private boolean isGranted(String granted) {
        return granted.equals("Y");
    }

}
