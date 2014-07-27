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

package org.wso2.carbon.rssmanager.core.dao.impl.postgres;

import org.wso2.carbon.rssmanager.core.dao.UserPrivilegesDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

import javax.persistence.EntityManager;

public class PostgresUserPrivilegesDAOImpl extends
        AbstractEntityDAO<Integer, UserDatabasePrivilege> implements UserPrivilegesDAO {

    protected PostgresUserPrivilegesDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void updateUserPrivileges(String environmentName, DatabasePrivilegeSet privileges,
                                     RSSInstance rssInstance, DatabaseUser user,
                                     String databaseName) throws RSSDAOException {
        
    }

    @Override
    public void updateUserPrivileges(UserDatabasePrivilege privileges) throws RSSDAOException {

    }

    @Override
    public void removeDatabasePrivileges(String environmentName, int rssInsanceId, String username,
                                         int tenantId) throws RSSDAOException {
        
    }

    @Override
    public void removeDatabasePrivileges(String environmentName, String username,
                                         int tenantId) throws RSSDAOException {

    }

    @Override
    public UserDatabasePrivilege getUserDatabasePrivileges(String environmentName,
                                                           String rssInstanceName,
                                                           String databaseName, String username,
                                                           int tenantId) throws RSSDAOException {
        return null;
    }

    @Override
    public void addUserDatabasePrivileges(UserDatabasePrivilege entity) throws RSSDAOException {

    }


    @Override
    public void removeUserDatabasePrivilegeEntriesByDatabase(RSSInstance rssInstance, String dbName,
                                                             int tenantId) throws RSSDAOException {


    }
}
