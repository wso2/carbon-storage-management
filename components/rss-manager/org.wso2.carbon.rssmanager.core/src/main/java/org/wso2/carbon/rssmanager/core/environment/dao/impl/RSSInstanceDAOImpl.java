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

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.persistence.Query;
import java.util.List;

public class RSSInstanceDAOImpl extends AbstractEntityDAO<Integer, RSSInstance> implements RSSInstanceDAO {

    private EntityManager entityManager;

    public RSSInstanceDAOImpl(EntityManager entityManager) {
        super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void addRSSInstance(String environmentName, RSSInstance rssInstance,
                               int tenantId) throws RSSDAOException {
        rssInstance.setServerURL(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.insert(rssInstance);
    }

    public void addRSSInstanceIfNotExist(String environmentName, RSSInstance rssInstance,
                                         int tenantId) throws RSSDAOException {
        rssInstance.setServerURL(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.saveOrUpdate(rssInstance);
    }

    public void removeRSSInstance(String environmentName, String name,
                                  int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" DELETE FROM RSSInstance ri  join  ri.environment  WHERE ri.name = :name AND ri.tenantId = :tenantId AND ri.environment.name = :evname");
        query.setParameter("name", name);
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        query.executeUpdate();
    }

    public void removeRSSInstance(List<RSSInstance> instances) throws RSSDAOException {
        super.removeAll(instances);
    }

    public void updateRSSInstance(String environmentName, RSSInstance rssInstance, int tenantId)
            throws RSSDAOException {
        rssInstance.setServerURL(rssInstance.getDataSourceConfig() == null ? rssInstance.getServerURL() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminUserName() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminPassword() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.saveOrUpdate(rssInstance);

    }


    public RSSInstance getRSSInstance(String environmentName, String instanceName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.name = :name AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("name", instanceName);
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        RSSInstance instance = null;
        List<RSSInstance> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            instance = result.iterator().next();
        }

        return instance;
    }

    public RSSInstance[] getRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE  ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    public RSSInstance[] getSystemRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }
    
    public RSSInstance[] getUserDefinedRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    /**
     * Get rss system instances for user
     * @param tenantId
     * @return
     */
    public RSSInstance[] getSystemRSSInstances(int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    /**
     * Get rss system instances for user
     * @param tenantId
     * @return
     */
    public RSSInstance[] getSystemRSSInstancesInEnvironment(int tenantId, String environmentName) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId " +
                "AND en.name=:envName");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        query.setParameter("envName", environmentName);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    /**
     * Get rss user defined instances for user
     * @param tenantId
     * @return
     */
    public RSSInstance[] getUserDefinedRSSInstances(int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
