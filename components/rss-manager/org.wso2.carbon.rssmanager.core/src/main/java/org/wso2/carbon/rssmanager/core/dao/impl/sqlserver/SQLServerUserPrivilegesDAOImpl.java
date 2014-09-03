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

package org.wso2.carbon.rssmanager.core.dao.impl.sqlserver;

import org.wso2.carbon.rssmanager.core.dao.UserPrivilegesDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.persistence.Query;
import java.util.List;

public class SQLServerUserPrivilegesDAOImpl extends
        AbstractEntityDAO<Integer, UserDatabasePrivilege> implements UserPrivilegesDAO {

    private EntityManager entityManager;

    protected SQLServerUserPrivilegesDAOImpl(EntityManager entityManager) {
        super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void updateUserPrivileges(UserDatabasePrivilege privileges) throws RSSDAOException{
        //super.saveOrUpdate(privileges);
    }

    public void removeDatabasePrivileges(String environmentName, int rssInsanceId, String username,
                                         int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager()
                .createQuery(" DELETE FROM UserDatabasePrivilege pr JOIN  pr.userDatabaseEntry pe WHERE pe.databaseUser.username = :username AND pe.databaseUser = :uTenantId AND pe.databaseUser.instances.id = :instanceId  AND " +
                        " pe.databaseUser.instances.tenantId = :tenantId AND pe.databaseUser.instances.environment.name = :evname  ");

        query.setParameter("tenantId", tenantId);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceId", rssInsanceId);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("username", username);

        query.executeUpdate();
    }

    public void removeDatabasePrivileges(String environmentName, String username,
                                         int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager()
                .createQuery(" DELETE FROM UserDatabasePrivilege pr JOIN  pr.userDatabaseEntry pe WHERE pe.databaseUser.username = :username AND pe.databaseUser = :uTenantId  AND " +
                        " pe.databaseUser.instances.tenantId = :tenantId AND pe.databaseUser.instances.environment.name = :evname  ");

        query.setParameter("tenantId", tenantId);
        query.setParameter("evname", environmentName);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("username", username);

        query.executeUpdate();
    }

    public UserDatabasePrivilege getUserDatabasePrivileges(String environmentName, String rssInstanceName,
                                                           String databaseName, String username, int tenantId)
            throws RSSDAOException {
        UserDatabasePrivilege entity = null;

        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" SELECT pr FROM UserDatabasePrivilege pr WHERE pr.id IN ( "
                        + " SELECT pr.id FROM UserDatabasePrivilege pr  JOIN  pr.userDatabaseEntry pe JOIN pe.databaseUser du JOIN du.instances si "
                        +" WHERE du.username = :username AND du.tenantId = :uTenantId AND si.name = :instanceName  "
                        +" AND si.tenantId = :tenantId AND si.environment.name = :evname ) "
                        +" AND pr.id IN ( "
                        +" SELECT pr.id FROM UserDatabasePrivilege pr  JOIN  pr.userDatabaseEntry pe JOIN pe.database du JOIN du.rssInstance si "
                        +" WHERE du.name = :dbName AND du.tenantId = :uTenantId AND si.name = :instanceName  "
                        +" AND si.tenantId = :tenantId AND si.environment.name = :evname ) "
                );

        query.setParameter("tenantId", MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("dbName", databaseName);
        query.setParameter("username", username);

        List<UserDatabasePrivilege> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            entity = result.iterator().next();
        }

        return entity;

    }

    public void addUserDatabasePrivileges(UserDatabasePrivilege entity) throws RSSDAOException {
        super.saveOrUpdate(entity);
    }

    public void removeUserDatabasePrivilegeEntriesByDatabase(RSSInstance rssInstance, String dbName,
                                                             int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" DELETE FROM UserDatabasePrivilege pr JOIN  pr.userDatabaseEntry pe WHERE pe.database.name = :dbName AND pe.database.tenantId = :dTenantId AND pe.database.rssInstance.id = :instanceId  ");

        query.setParameter("tenantId", tenantId);
        query.setParameter("instanceId", rssInstance.getId());
        query.setParameter("dTenantId", tenantId);
        query.setParameter("dbName", dbName);

        query.executeUpdate();
    }

    private org.wso2.carbon.rssmanager.core.dao.util.EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void updateUserPrivileges(String environmentName, DatabasePrivilegeSet privileges,
                                     RSSInstance rssInstance, DatabaseUser user, String databaseName)
            throws RSSDAOException {
        // TODO Auto-generated method stub

    }
    
}
