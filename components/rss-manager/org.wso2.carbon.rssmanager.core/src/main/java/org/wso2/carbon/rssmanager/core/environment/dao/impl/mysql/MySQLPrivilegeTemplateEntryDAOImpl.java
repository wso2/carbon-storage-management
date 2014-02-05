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

package org.wso2.carbon.rssmanager.core.environment.dao.impl.mysql;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.environment.DatabasePrivilegeTemplateEntryDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class MySQLPrivilegeTemplateEntryDAOImpl extends AbstractEntityDAO<Integer, DatabasePrivilegeTemplateEntry> implements DatabasePrivilegeTemplateEntryDAO {

    private EntityManager entityManager;

    public MySQLPrivilegeTemplateEntryDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }
    
    @Override
    public void addPrivilegeTemplateEntries(String environmentName, int templateId,
                                            DatabasePrivilegeSet privileges,
                                            int tenantId) throws RSSDAOException {
    	
    	DatabasePrivilegeTemplateEntry entity = new DatabasePrivilegeTemplateEntry();
    	RSSManagerUtil.createDatabasePrivilegeTemplateEntry(privileges, entity);    	
    	super.saveOrUpdate(entity);
    }

   /* @Override
    public void addPrivilegeTemplateEntries(String environmentName, int templateId,
                                            DatabasePrivilegeSet privileges,
                                            int tenantId) throws RSSDAOException {
        MySQLPrivilegeSet mysqlPrivs = (MySQLPrivilegeSet) privileges;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE_ENTRY "
                    + " (TEMPLATE_ID, SELECT_PRIV, INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) "
                    + " VALUES(?, "
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getSelectPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getInsertPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getUpdatePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getDeletePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getCreatePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getDropPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getGrantPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getReferencesPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getIndexPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getAlterPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getCreateTmpTablePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getLockTablesPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getCreateViewPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getShowViewPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getCreateRoutinePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getAlterRoutinePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getExecutePriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getEventPriv(), true)
                    + RSSDAOUtil.getParameterizedValue(mysqlPrivs.getTriggerPriv(), false) + ")";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, templateId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred setting database privilege template " +
                    "properties : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/

   /* @Override
    public void updatePrivilegeTemplateEntries(String environmentName,
                                               String templateName, DatabasePrivilegeSet privileges,
                                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        MySQLPrivilegeSet mysqlPrivs = (MySQLPrivilegeSet) privileges;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET SELECT_PRIV = ?, INSERT_PRIV = ?, UPDATE_PRIV = ?, DELETE_PRIV = ?, CREATE_PRIV = ?, DROP_PRIV = ?, GRANT_PRIV = ?, REFERENCES_PRIV = ?, INDEX_PRIV = ?, ALTER_PRIV = ?, CREATE_TMP_TABLE_PRIV = ?, LOCK_TABLES_PRIV = ?, CREATE_VIEW_PRIV = ?, SHOW_VIEW_PRIV = ?, CREATE_ROUTINE_PRIV = ?, ALTER_ROUTINE_PRIV = ?, EXECUTE_PRIV = ?, EVENT_PRIV = ?, TRIGGER_PRIV = ? WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mysqlPrivs.getSelectPriv());
            stmt.setString(2, mysqlPrivs.getInsertPriv());
            stmt.setString(3, mysqlPrivs.getUpdatePriv());
            stmt.setString(4, mysqlPrivs.getDeletePriv());
            stmt.setString(5, mysqlPrivs.getCreatePriv());
            stmt.setString(6, mysqlPrivs.getDropPriv());
            stmt.setString(7, mysqlPrivs.getGrantPriv());
            stmt.setString(8, mysqlPrivs.getReferencesPriv());
            stmt.setString(9, mysqlPrivs.getIndexPriv());
            stmt.setString(10, mysqlPrivs.getAlterPriv());
            stmt.setString(11, mysqlPrivs.getCreateTmpTablePriv());
            stmt.setString(12, mysqlPrivs.getLockTablesPriv());
            stmt.setString(13, mysqlPrivs.getCreateViewPriv());
            stmt.setString(14, mysqlPrivs.getShowViewPriv());
            stmt.setString(15, mysqlPrivs.getCreateRoutinePriv());
            stmt.setString(16, mysqlPrivs.getAlterRoutinePriv());
            stmt.setString(17, mysqlPrivs.getExecutePriv());
            stmt.setString(18, mysqlPrivs.getEventPriv());
            stmt.setString(19, mysqlPrivs.getTriggerPriv());
            stmt.setString(20, templateName);
            stmt.setInt(21, tenantId);
            stmt.setString(22, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while editing the database privilege " +
                    "template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/

    /*@Override
    public void removePrivilegeTemplateEntries(String environmentName, String templateName,
                                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE TEMPLATE_ID = (SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while removing database privilege " +
                    "template entries : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
