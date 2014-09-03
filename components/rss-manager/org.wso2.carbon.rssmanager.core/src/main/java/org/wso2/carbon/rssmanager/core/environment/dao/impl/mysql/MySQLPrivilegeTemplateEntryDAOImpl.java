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
}
