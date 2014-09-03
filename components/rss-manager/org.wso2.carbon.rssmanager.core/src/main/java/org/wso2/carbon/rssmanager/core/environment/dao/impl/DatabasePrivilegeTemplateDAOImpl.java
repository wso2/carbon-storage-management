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

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

import javax.persistence.Query;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabasePrivilegeTemplateDAOImpl extends AbstractEntityDAO<Integer, DatabasePrivilegeTemplate> implements DatabasePrivilegeTemplateDAO {

    private EntityManager entityManager;

    public DatabasePrivilegeTemplateDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
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
