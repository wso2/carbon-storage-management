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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.environment.DatabasePrivilegeTemplateEntryDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.mysql.MySQLPrivilegeTemplateEntryDAOImpl;

public class EnvironmentManagementDAOImpl implements EnvironmentManagementDAO {

    private EntityManager entityManager;

    public EnvironmentManagementDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EnvironmentDAO getEnvironmentDAO() {
        return new EnvironmentDAOImpl(this.getEntityManager());
    }

    @Override
    public RSSInstanceDAO getRSSInstanceDAO() {
        return new RSSInstanceDAOImpl(this.getEntityManager());
    }

    @Override
    public DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO() {
        return new DatabasePrivilegeTemplateDAOImpl(this.getEntityManager());
    }

    @Override
    public DatabasePrivilegeTemplateEntryDAO getDatabasePrivilegeTemplateEntryDAO() {
        return new MySQLPrivilegeTemplateEntryDAOImpl(this.getEntityManager());  
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
