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
package org.wso2.carbon.rssmanager.core.manager.impl.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.PostgresPrivilegeSet;
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
import org.wso2.carbon.utils.xml.StringUtils;

public class PostgresSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(PostgresSystemRSSManager.class);

    public PostgresSystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public Database addDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        String qualifiedDatabaseName = RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());
        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            throw new EntityNotFoundException(
                    "RSS instance " + database.getRssInstanceName() + " does not exist");
        }

        RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);

        try {
            super.addDatabase(isInTx, database, rssInstance, qualifiedDatabaseName);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

			/*
             * this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(
			 * getEnvironmentName(),
			 * Connection.TRANSACTION_SERIALIZABLE);
			 */

			/*
             * Actual database creation is committed just before committing the
			 * meta info into RSS
			 * management repository. This is done as it is not possible to
			 * control CREATE, DROP,
			 * ALTER operations within a JTA transaction since those operations
			 * are committed
			 * implicitly
			 */
            stmt.execute();
            disAllowedConnect(conn, qualifiedDatabaseName, "PUBLIC");


            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
            // conn.commit();

            return database;
        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw new RSSManagerException(
                    "Error while creating the database '" + qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } catch (RSSDAOException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            try {
                // conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            throw new RSSManagerException(
                    "Error while creating the database '" + qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    public void removeDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        PreparedStatement delStmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException(
                    "Unresolvable RSS Instance. Database " + databaseName + " does not exist");
        }

        RSSManagerUtil.checkIfParameterSecured(databaseName);
        try {

            super.removeDatabase(isInTx, rssInstance.getName(), databaseName, rssInstance);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);

			/*
             * Actual database creation is committed just before committing the
			 * meta info into RSS
			 * management repository. This is done as it is not possible to
			 * control CREATE, DROP,
			 * ALTER operations within a JTA transaction since those operations
			 * are committed
			 * implicitly
			 */
            stmt.execute();
            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }


            // conn.commit();
        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }

            throw new RSSManagerException(
                    "Error while dropping the database '" + databaseName + "' on RSS " + "instance '" + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } catch (RSSDAOException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }

            throw new RSSManagerException(
                    "Error while dropping the database '" + databaseName + "' on RSS " + "instance '" + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSManagerUtil.cleanupResources(null, delStmt, null);
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String qualifiedUsername = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Map<String,String> mapUserwithInstance = new HashMap<String,String>();

        qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());
			/* Committing distributed transaction */
			
	    /* Sets the fully qualified username */
        user.setName(qualifiedUsername);
        user.setRssInstanceName(user.getRssInstanceName());
        user.setType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        RSSManagerUtil.checkIfParameterSecured(qualifiedUsername);

        try {
            super.addDatabaseUser(isInTx, user, qualifiedUsername);
			/* Committing distributed transaction */

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
                    conn.setAutoCommit(true);

                    boolean hasPassword = (!StringUtils.isEmpty(user.getPassword()));

                    StringBuilder sql = new StringBuilder(" CREATE USER " + qualifiedUsername);
                    if (hasPassword) {
                        RSSManagerUtil.checkIfParameterSecured(user.getPassword());
                        sql.append(" WITH PASSWORD '").append(user.getPassword()).append("'");
                    }
                    stmt = conn.prepareStatement(sql.toString());
                    
    					
    					/*
    					 * Actual database user creation is committed just before
    					 * committing the meta
    					 * info into RSS management repository. This is done as it
    					 * is not possible to
    					 * control CREATE, DROP, ALTER, etc operations within a JTA
    					 * transaction since
    					 * those operations are committed implicitly
    					 */
                    stmt.execute();
                    mapUserwithInstance.put(rssInstance.getName(), qualifiedUsername);
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }

            }

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
            return user;
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (e instanceof EntityAlreadyExistsException) {
                handleException(e.getMessage(), e);
            }
            String msg = "Error while creating the database user '" +
                    user.getName() + "' on RSS instance '" + user.getRssInstanceName() +
                    "' : " + e.getMessage();
            
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
    
    private void dropAddedUsers(Map<String,String> mapUserwithInstance){
    	Set<Entry<String, String>> entries = mapUserwithInstance.entrySet();
    	for(Entry<String, String> entry : entries){
    		String userName = entry.getValue();
    		String instanceName = entry.getKey();
    		Connection conn = null;
    		PreparedStatement dropOwnedStmt = null;
            PreparedStatement dropUserStmt = null;
    		try{
    			conn = getConnection(instanceName);
    			conn.setAutoCommit(true);

    			String sql = "drop owned by " + userName;
                dropOwnedStmt = conn.prepareStatement(sql);
                dropUserStmt = conn.prepareStatement(" drop user " + userName);
                
                dropOwnedStmt.execute();
                dropUserStmt.execute();
    		}catch(Exception ex){
    			log.error(ex);
    		} finally {
    			 RSSManagerUtil.cleanupResources(null, dropOwnedStmt, null);
                 RSSManagerUtil.cleanupResources(null, dropUserStmt, conn);
            }
            
    	}
    }

    public void removeDatabaseUser(String type, String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement dropOwnedStmt = null;
        PreparedStatement dropUserStmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
			/* committing the distributed transaction */

        RSSManagerUtil.checkIfParameterSecured(username);
        try {
            removeDatabaseUser(isInTx, type, username);
			/* committing the distributed transaction */

            for (RSSInstanceDSWrapper wrapper : getEnvironment().getDSWrapperRepository()
                    .getAllRSSInstanceDSWrappers()) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    RSSInstance rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    PrivilegedCarbonContext.endTenantFlow();

                    conn = getConnection(wrapper.getRssInstance().getName());
                    conn.setAutoCommit(true);

                    String sql = "drop owned by " + username;
                    dropOwnedStmt = conn.prepareStatement(sql);
                    dropUserStmt = conn.prepareStatement(" drop user " + username);

                /*
                 * Actual database creation is committed just before
                 * committing the meta info into RSS
                 * management repository. This is done as it is not possible
                 * to control CREATE, DROP,
                 * ALTER operations within a JTA transaction since those
                 * operations are committed
                 * implicitly
                 */
                    dropOwnedStmt.execute();
                    dropUserStmt.execute();

                } finally {
                    RSSManagerUtil.cleanupResources(null, dropOwnedStmt, null);
                    RSSManagerUtil.cleanupResources(null, dropUserStmt, conn);
                }

            }
            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }
        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error while dropping the database user '" + username + "' on RSS instances : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSManagerException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw e;
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw new RSSManagerException(e);
        } finally {
            RSSManagerUtil.cleanupResources(null, dropOwnedStmt, conn);
            closeJPASession();
        }
    }

    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        Connection dbConn = null;
        Connection conn = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

		/*
		 * if (!(privileges instanceof PostgresPrivilegeSet)) {
		 * throw new RuntimeException("Invalid privilege set defined");
		 * }
		 * PostgresPrivilegeSet postgresPrivs = (PostgresPrivilegeSet)
		 * privileges;
		 */

        try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }
            PostgresPrivilegeSet postgresPrivs = new PostgresPrivilegeSet();
            createPostgresPrivilegeSet(postgresPrivs, privileges);

            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                    this.getEnvironmentName(), null, databaseName,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                String msg = "Database '" + databaseName + "' does not exist " + "in RSS instance '" + user.getRssInstanceName() + "'";
                throw new EntityNotFoundException(msg);
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

            dbConn = getConnection(rssInstance.getName(), databaseName);
            dbConn.setAutoCommit(true);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);

            revokeAllPrivileges(conn, databaseName, user.getName());
            composePreparedStatement(dbConn, databaseName, user.getName(), postgresPrivs);

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }

        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while editing privileges  : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw new RSSManagerException(e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
            RSSManagerUtil.cleanupResources(null, null, dbConn);
            closeJPASession();
        }
    }

    private void createPostgresPrivilegeSet(PostgresPrivilegeSet postgresPrivs,
                                            DatabasePrivilegeSet privileges) {
        postgresPrivs.setAlterPriv(privileges.getAlterPriv());
        postgresPrivs.setConnectPriv("Y");
        postgresPrivs.setCreatePriv(privileges.getCreatePriv());
        postgresPrivs.setDeletePriv(privileges.getDeletePriv());
        postgresPrivs.setDropPriv(privileges.getDropPriv());
        postgresPrivs.setExecutePriv("Y");
        postgresPrivs.setIndexPriv(privileges.getIndexPriv());
        postgresPrivs.setInsertPriv(privileges.getInsertPriv());
        postgresPrivs.setReferencesPriv("Y");
        postgresPrivs.setSelectPriv(privileges.getSelectPriv());
        postgresPrivs.setTemporaryPriv("Y");
        postgresPrivs.setTempPriv("Y");
        postgresPrivs.setTriggerPriv("Y");
        postgresPrivs.setTruncatePriv("Y");
        postgresPrivs.setUpdatePriv(privileges.getUpdatePriv());
        postgresPrivs.setUsagePriv("Y");

        if (privileges instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet myPriv = (MySQLPrivilegeSet) privileges;

            postgresPrivs.setExecutePriv(myPriv.getExecutePriv());
            postgresPrivs.setReferencesPriv(myPriv.getReferencesPriv());
            postgresPrivs.setTriggerPriv(myPriv.getTriggerPriv());
            postgresPrivs.setUsagePriv(myPriv.getUpdatePriv());
        }
    }

    public void attachUser(UserDatabaseEntry entry, DatabasePrivilegeSet privileges)
            throws RSSManagerException {
        Connection conn = null;
        Connection dbConn = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException(
                    "Database '" + databaseName + "' does not exist in " + "RSS instance '" + rssInstanceName + "'");
        }
			/* ending distributed transaction */

        try {

            super.attachUser(isInTx, entry, privileges, rssInstance);
			/* ending distributed transaction */

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            dbConn = getConnection(rssInstance.getName(), databaseName);
            dbConn.setAutoCommit(true);

			/*
			 * Actual database user attachment is committed just before
			 * committing the meta info into RSS
			 * management repository. This is done as it is not possible to
			 * control CREATE, DROP,
			 * ALTER operations within a JTA transaction since those operations
			 * are committed
			 * implicitly
			 */
            grantConnect(conn, databaseName, username);
			
			/*if (!(privileges instanceof PostgresPrivilegeSet)) {
				throw new RuntimeException("Invalid privilege set defined");
			}*/
            PostgresPrivilegeSet postgresPrivs = new PostgresPrivilegeSet();
            createPostgresPrivilegeSet(postgresPrivs, privileges);
            this.composePreparedStatement(dbConn, databaseName, username, postgresPrivs);

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }

        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " + "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while adding metadata into the RSS Management " + "Repository on user attachment";
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
            RSSManagerUtil.cleanupResources(null, null, dbConn);
            closeJPASession();
        }
    }

    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        Connection conn = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSManagerUtil.checkIfParameterSecured(username);
        RSSManagerUtil.checkIfParameterSecured(databaseName);

        try {
			/* Committing the transaction */

            RSSInstance rssInstance = detachUser(isInTx, entry);
			/* Committing the transaction */

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);

			/*
			 * Actual database user detachment is committed just before
			 * committing the meta info
			 * into RSS management repository. This is done as it is not
			 * possible to control CREATE,
			 * DROP, ALTER operations within a JTA transaction since those
			 * operations are committed
			 * implicitly
			 */
            revokeAllPrivileges(conn, databaseName, username);
            disAllowedConnect(conn, databaseName, username);

            if (isInTx.get()) {
                this.getEntityManager().endJPATransaction();
            }

        } catch (SQLException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " + "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            throw new RSSManagerException(e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
            closeJPASession();
        }
    }

    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          PostgresPrivilegeSet privileges) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);

        boolean grantEnable = false;
        if ("Y".equalsIgnoreCase(privileges.getDropPriv())) {
            grantEnable = true;
        }
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.DATABASE,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.SCHEMA, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.TABLE, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.FUNCTION,
                grantEnable);
        // composePreparedStatement(con, databaseName, username, privileges,
        // PRIVILEGESTYPE.LANGUAGE,
        // grantEnable);
        // composePreparedStatement(con, databaseName, username, privileges,
        // PRIVILEGESTYPE.LARGE_OBJECT, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.SEQUENCE,
                grantEnable);
        // composePreparedStatement(con, databaseName, username, privileges,
        // PRIVILEGESTYPE.TABLESPACE, grantEnable);
    }

    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          PostgresPrivilegeSet privileges, PrivilegeTypes type,
                                          boolean grantEnable) throws SQLException, RSSManagerException {

        String grantOption = " WITH GRANT OPTION ";
        String grantee = "";
        if (type.equals(PrivilegeTypes.TABLE) || type.equals(PrivilegeTypes.SEQUENCE) || type.equals(PrivilegeTypes.FUNCTION)) {
            grantee = " ALL " + type.name() + "S IN SCHEMA PUBLIC ";
        } else if (type.equals(PrivilegeTypes.DATABASE)) {
            grantee = type.name() + " " + databaseName;
        } else if (type.equals(PrivilegeTypes.SCHEMA)) {
            grantee = type.name() + " PUBLIC ";
        }
        String privilegesString = createPrivilegesString(privileges, type);
        if (privilegesString == null) {
            return;
        }
        StringBuilder sql = new StringBuilder(
                "GRANT " + privilegesString + " ON " + grantee + " TO " + username);
        if (grantEnable) {
            sql.append(grantOption);
        }
        PreparedStatement stmt = con.prepareStatement(sql.toString());
        stmt.executeUpdate();
        stmt.close();
    }

    private void grantConnect(Connection con, String databaseName, String username) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st = con.prepareStatement(" GRANT CONNECT ON DATABASE " + databaseName + " TO " + username);
        st.executeUpdate();
        st.close();
    }

    private void grantUsage(Connection con, String databaseName, String username) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st = con.prepareStatement(" GRANT USAGE ON SCHEMA public TO " + username);
        st.executeUpdate();
        st.close();
    }

    private String createPrivilegesString(final PostgresPrivilegeSet privileges, PrivilegeTypes type) {
        List<Privileges> privList = new ArrayList<Privileges>();

        switch (type) {
            case TABLE:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.INSERT, privileges.getInsertPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                addToPrivilegesList(privList, Privileges.DELETE, privileges.getDeletePriv());
                addToPrivilegesList(privList, Privileges.REFERENCES, privileges.getReferencesPriv());
                addToPrivilegesList(privList, Privileges.TRIGGER, privileges.getTriggerPriv());
                privList.add(Privileges.TRUNCATE);
                break;

            case DATABASE:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                addToPrivilegesList(privList, Privileges.TEMPORARY, privileges.getTempPriv());
                privList.add(Privileges.TEMP);
                privList.add(Privileges.CONNECT);
                break;

            case SEQUENCE:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                privList.add(Privileges.USAGE);
                break;

            case FUNCTION:
                addToPrivilegesList(privList, Privileges.EXECUTE, privileges.getExecutePriv());
                break;

            case LANGUAGE:
                privList.add(Privileges.USAGE);
                break;

            case LARGE_OBJECT:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                break;

            case SCHEMA:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                privList.add(Privileges.USAGE);
                break;

            case TABLESPACE:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                break;
        }

        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getDropPriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getGrantPriv());

        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getIndexPriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getAlterPriv());

        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getLockTablesPriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getCreateViewPriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getShowViewPriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getCreateRoutinePriv());
        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getAlterRoutinePriv());

        // addToPrivilegesList(privList,Privileges.SELECT,
        // privileges.getEventPriv());

        if (privList.isEmpty()) {
            return null;
        }

        StringBuilder privilegesPart = new StringBuilder();

        Iterator<Privileges> iter = privList.iterator();
        while (iter.hasNext()) {
            privilegesPart.append(iter.next().name());
            if (iter.hasNext()) {
                privilegesPart.append(" , ");
            }
        }
        return privilegesPart.toString();
    }

    private void addToPrivilegesList(final List<Privileges> privList, Privileges privEnum, String priv) {
        if ("Y".equalsIgnoreCase(priv)) {
            privList.add(privEnum);
        }
    }

    private void disAllowedConnect(Connection con, String databaseName, String userName) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement st = con.prepareStatement("REVOKE connect ON DATABASE " + databaseName + " FROM " + userName);
        st.executeUpdate();
        st.close();
    }

    private void revokeAllPrivileges(Connection con, String databaseName, String userName)
            throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement st = con.prepareStatement("revoke all on database " + databaseName + " from " + userName);
        st.executeUpdate();
        st.close();
    }

    private enum Privileges {
        SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, CREATE, CONNECT, TEMPORARY, EXECUTE,
        USAGE, TEMP;
    }

    private enum PrivilegeTypes {
        TABLE, DATABASE, SEQUENCE, FUNCTION, LANGUAGE, LARGE_OBJECT, SCHEMA, TABLESPACE;
    }

}