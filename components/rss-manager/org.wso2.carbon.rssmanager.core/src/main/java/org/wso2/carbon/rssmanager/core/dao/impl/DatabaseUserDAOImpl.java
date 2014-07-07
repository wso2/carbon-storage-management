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

package org.wso2.carbon.rssmanager.core.dao.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class DatabaseUserDAOImpl extends AbstractEntityDAO<Integer, DatabaseUser> implements DatabaseUserDAO {

    private EntityManager entityManager;

    public DatabaseUserDAOImpl(EntityManager entityManager) {
        super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void addDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user,
                                int tenantId) throws RSSDAOException {
        user.setTenantId(tenantId);
        super.insert(user);
    }

    @Override
    public void addDatabaseUser(DatabaseUser user, int tenantId) throws RSSDAOException {
        user.setTenantId(tenantId);
        super.saveOrUpdate(user);
    }

    public void removeDatabaseUser(DatabaseUser user) throws RSSDAOException {
        super.remove(user);
    }

    @Override
    public void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
                                   int tenantId) throws RSSDAOException {

    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String username, int tenantId) throws RSSDAOException {

        boolean isExist = false;
        if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(rssInstanceName)) {
            Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si WHERE us.username = :username AND us.tenantId = :uTenantId AND us.type = :type  AND " + ""
                    + " si.environment.name = :evname");
            query.setParameter("username", username);
            query.setParameter("type", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            query.setParameter("evname", environmentName);
            query.setParameter("uTenantId", tenantId);

            DatabaseUser user = null;
            List<DatabaseUser> result = query.getResultList();
            if (result != null && !result.isEmpty()) {
                user = result.iterator().next();
                isExist = true;
            }
        } else {
            Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si WHERE us.username = :username AND us.tenantId = :uTenantId AND us.type = :type AND si.name = :instanceName  AND " + ""
                    + " si.environment.name = :evname");
            query.setParameter("username", username);
            query.setParameter("type", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            query.setParameter("evname", environmentName);
            query.setParameter("instanceName", rssInstanceName);
            query.setParameter("uTenantId", tenantId);

            DatabaseUser user = null;
            List<DatabaseUser> result = query.getResultList();
            if (result != null && !result.isEmpty()) {
                user = result.iterator().next();
                isExist = true;
            }
        }
        return isExist;
    }

    public DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName,
                                        String username, int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si WHERE us.username = :username AND us.tenantId = :uTenantId AND si.name = :instanceName  AND " + ""
                + " si.tenantId = :tenantId AND si.environment.name = :evname");
        query.setParameter("username", username);
        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);

        DatabaseUser user = null;
        List<DatabaseUser> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            user = result.iterator().next();
        }
        return user;
    }

    public DatabaseUser getDatabaseUser(String environmentName,
                                        String username, int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si WHERE us.username = :username AND us.tenantId = :uTenantId  AND " + ""
                + " si.tenantId = :tenantId AND si.environment.name = :evname");
        query.setParameter("username", username);
        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("uTenantId", tenantId);

        DatabaseUser user = null;
        List<DatabaseUser> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            user = result.iterator().next();
        }
        return user;
    }

    public DatabaseUser[] getDatabaseUsers(String environmentName,
                                           int tenantId, String databaseUserType) throws RSSDAOException {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT us  FROM DatabaseUser us  join us.instances si WHERE  us.tenantId = :uTenantId  AND "
                + " si.environment.name = :evname AND us.type = :type");
        query.setParameter("evname", environmentName);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("type", databaseUserType);

        DatabaseUser[] user = new DatabaseUser[0];
        List<DatabaseUser> result = query.getResultList();
        if (result != null) {
        	Set<DatabaseUser> userSet = new HashSet<DatabaseUser>();
        	userSet.addAll(result);
            user = userSet.toArray(new DatabaseUser[userSet.size()]);
        }
        return user;
    }

    public DatabaseUser[] getAssignedDatabaseUsers(String environmentName, String rssInstanceName,
                                                   String databaseName,
                                                   int tenantId) throws RSSDAOException {

        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(
                " SELECT us FROM DatabaseUser us  join  us.instances si, Database db  join  db.userDatabaseEntries ue WHERE  us.tenantId = :uTenantId AND si.name = :instanceName  AND " + ""
                        + " si.tenantId = :tenantId AND si.environment.name = :evname AND ue.databaseUser.id = us.id AND db.name = :dbName");

        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("dbName", databaseName);


        DatabaseUser[] user = new DatabaseUser[0];
        List<DatabaseUser> result = query.getResultList();
        if (result != null) {
        	Set<DatabaseUser> userSet = new HashSet<DatabaseUser>();
        	userSet.addAll(result);
            user = userSet.toArray(new DatabaseUser[userSet.size()]);
        }
        return user;

    }

    @Override
    public String resolveRSSInstanceByUser(String environmentName, String rssInstanceName,
                                           String rssInstanceType, String username,
                                           int tenantId) throws RSSDAOException {

        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT si FROM DatabaseUser us  join  us.instances si WHERE us.username = :username AND us.tenantId = :uTenantId AND us.type = :type AND "
                + " si.environment.name = :evname");
        query.setParameter("username", username);
        query.setParameter("type", rssInstanceType);
        query.setParameter("evname", environmentName);
        /*query.setParameter("instanceName", rssInstanceName);*/
        query.setParameter("uTenantId", tenantId);

        RSSInstance rssInstance = null;
        String instancenName = null;
        List<RSSInstance> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            rssInstance = result.iterator().next();
            instancenName = rssInstance.getName();
        }
        return instancenName;

    }

    public DatabaseUser[] getDatabaseUsersByRSSInstance(String environmentName,
                                                        String rssInstanceName,
                                                        int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }


    public DatabaseUser[] getDatabaseUsersByDatabase(String environmentName,
                                                     String rssInstanceName, String database,
                                                     int tenantId) throws RSSDAOException {
        return new DatabaseUser[0];
    }

    public DatabaseUser[] getAvailableDatabaseUsers(String environmentName, String rssInstanceName,
                                                    String databaseName, int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si, Database db  left join  db.userDatabaseEntries ue WHERE  us.tenantId = :uTenantId AND si.name = :instanceName  AND "  +
                " si.tenantId = :tenantId AND si.environment.name = :evname  ");

        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);

        DatabaseUser[] user = new DatabaseUser[0];
        List<DatabaseUser> result = query.getResultList();
        if (result != null) {
        	Set<DatabaseUser> userSet = new HashSet<DatabaseUser>();
        	userSet.addAll(result);
            user = userSet.toArray(new DatabaseUser[userSet.size()]);
        }
        return user;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
