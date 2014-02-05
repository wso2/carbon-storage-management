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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

public class DatabasePrivilegeTemplateDAOImpl extends AbstractEntityDAO<Integer, DatabasePrivilegeTemplate> implements DatabasePrivilegeTemplateDAO {

    private EntityManager entityManager;

    public DatabasePrivilegeTemplateDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }
    
    public void addDatabasePrivilegesTemplate(String environmentName,
                                              DatabasePrivilegeTemplate template,
                                              int tenantId) throws RSSDAOException {
    	template.setTenantId(tenantId);
    	super.saveOrUpdate(template);
    }

    /*public void addDatabasePrivilegesTemplate(String environmentName,
                                              DatabasePrivilegeTemplate template,
                                              int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE (NAME,TENANT_ID,ENVIRONMENT_ID) VALUES(?,?,(SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)) ";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, template.getName());
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSDAOException("Database privilege was not created");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                template.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while creating database privilege " +
                    "template '" + template.getName() + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }
*/

    public void removeDatabasePrivilegesTemplate(String environmentName, String templateName,
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
            throw new RSSDAOException("Error occurred while dropping the database privilege " +
                    "template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }
    
    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
                                                                   String environmentName, String templateName, int tenantId) throws RSSDAOException {
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT tp from DatabasePrivilegeTemplate tp inner join fetch tp.environment left join fetch tp.entry where tp.name = :name and tp.tenantId = :tenantId and tp.environment.name = :envName ");
		query.setParameter("name", templateName);
		query.setParameter("tenantId", tenantId);
		query.setParameter("envName", environmentName);
		
		DatabasePrivilegeTemplate template = null;
		List<DatabasePrivilegeTemplate> result = query.getResultList();
		if(result != null && !result.isEmpty()){
			template = result.iterator().next();
		}
		return template;
    }
    
    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
                                                                      String environmentName, int tenantId) throws RSSDAOException {
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT tp from DatabasePrivilegeTemplate tp inner join fetch tp.environment left join fetch tp.entry where tp.tenantId = :tenantId and tp.environment.name = :envName ");
		query.setParameter("tenantId", tenantId);
		query.setParameter("envName", environmentName);
		
		List<DatabasePrivilegeTemplate> result = query.getResultList();
		DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
		if(result != null && !result.isEmpty()){
			templates = result.toArray(new DatabasePrivilegeTemplate[result.size()]);
		}
		return templates;
		
    }
    
    public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName,
                                                    int tenantId) throws RSSDAOException {
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT tp from DatabasePrivilegeTemplate tp join tp.environment en where tp.name = :name and tp.tenantId = :tenantId and en.name = :envName ");
		query.setParameter("name", templateName);
		query.setParameter("tenantId", tenantId);
		query.setParameter("envName", environmentName);
		
		boolean isExist = false;
		List<DatabasePrivilegeTemplate> result = query.getResultList();
		if(result != null && !result.isEmpty()){
			isExist = true;
		}
		return isExist;
    }

    /*public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            String environmentName, String templateName, int tenantId) throws RSSDAOException {
        DatabasePrivilegeTemplate template = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV" +
            		" FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.NAME = ? AND p.TENANT_ID = ? AND p.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                template = this.createDatabasePrivilegeTemplateFromRS(rs);
            }
            return template;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving database privilege " +
                    "template information : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }SELECT tp from DatabasePrivilegeTemplate tp join tp.environment en where tp.name = :name and tp.tenantId = :tenantId and en.name = :envName ");
*/

    /*public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            String environmentName, int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT p.ID, p.NAME, p.TENANT_ID, e.SELECT_PRIV, e.INSERT_PRIV, e.UPDATE_PRIV, e.DELETE_PRIV, e.CREATE_PRIV, e.DROP_PRIV, e.GRANT_PRIV, e.REFERENCES_PRIV, e.INDEX_PRIV, e.ALTER_PRIV, e.CREATE_TMP_TABLE_PRIV, e.LOCK_TABLES_PRIV, e.CREATE_VIEW_PRIV, e.SHOW_VIEW_PRIV, e.CREATE_ROUTINE_PRIV, e.ALTER_ROUTINE_PRIV, e.EXECUTE_PRIV, e.EVENT_PRIV, e.TRIGGER_PRIV" +
            		" FROM RM_DB_PRIVILEGE_TEMPLATE p, RM_DB_PRIVILEGE_TEMPLATE_ENTRY e WHERE p.ID = e.TEMPLATE_ID AND p.TENANT_ID = ?  AND p.ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
            rs = stmt.executeQuery();
            List<DatabasePrivilegeTemplate> result = new ArrayList<DatabasePrivilegeTemplate>();
            while (rs.next()) {
                result.add(createDatabasePrivilegeTemplateFromRS(rs));
            }
            return result.toArray(new DatabasePrivilegeTemplate[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving database privilege " +
                    "templates : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }*/

    private String parameterized(String param, boolean withComma){
    	String end = "'";
    	if(withComma){
    		end = "',";
    	}
    	return "'"+param+end;
    }

   /* public boolean isDatabasePrivilegeTemplateExist(String environmentName, String templateName,
                                                    int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isExist = false;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT ID FROM RM_DB_PRIVILEGE_TEMPLATE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, templateName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int templateId = rs.getInt("ID");
                if (templateId > 0) {
                    isExist = true;
                }
            }
            return isExist;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while checking the existence " +
                    "of database privilege template '" + templateName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }
*/
    private DatabasePrivilegeTemplate createDatabasePrivilegeTemplateFromRS(ResultSet rs) throws
            SQLException, RSSDAOException {
        int id = rs.getInt("ID");
        String templateName = rs.getString("NAME");
        MySQLPrivilegeSet privileges = new MySQLPrivilegeSet();
        privileges.setSelectPriv(rs.getString("SELECT_PRIV"));
        privileges.setInsertPriv(rs.getString("INSERT_PRIV"));
        privileges.setUpdatePriv(rs.getString("UPDATE_PRIV"));
        privileges.setDeletePriv(rs.getString("DELETE_PRIV"));
        privileges.setCreatePriv(rs.getString("CREATE_PRIV"));
        privileges.setDropPriv(rs.getString("DROP_PRIV"));
        privileges.setGrantPriv(rs.getString("GRANT_PRIV"));
        privileges.setReferencesPriv(rs.getString("REFERENCES_PRIV"));
        privileges.setIndexPriv(rs.getString("INDEX_PRIV"));
        privileges.setAlterPriv(rs.getString("ALTER_PRIV"));
        privileges.setCreateTmpTablePriv(rs.getString("CREATE_TMP_TABLE_PRIV"));
        privileges.setLockTablesPriv(rs.getString("LOCK_TABLES_PRIV"));
        privileges.setCreateViewPriv(rs.getString("CREATE_VIEW_PRIV"));
        privileges.setShowViewPriv(rs.getString("SHOW_VIEW_PRIV"));
        privileges.setCreateRoutinePriv(rs.getString("CREATE_ROUTINE_PRIV"));
        privileges.setAlterRoutinePriv(rs.getString("ALTER_ROUTINE_PRIV"));
        privileges.setExecutePriv(rs.getString("EXECUTE_PRIV"));
        privileges.setEventPriv(rs.getString("EVENT_PRIV"));
        privileges.setTriggerPriv(rs.getString("TRIGGER_PRIV"));

        return new DatabasePrivilegeTemplate(id, templateName, privileges);
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
