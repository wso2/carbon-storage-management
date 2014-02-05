/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.CassandraMBeanLocator;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.lang.management.ManagementFactory;

/**
 * Keeps the runtime objects required by the operation of the cassandra admin service
 */

public class CassandraAdminDataHolder {

    private static Log log = LogFactory.getLog(CassandraAdminDataHolder.class);

    private static CassandraAdminDataHolder thisInstance = new CassandraAdminDataHolder();
    /* For accessing Cassandra clusters */
    private DataAccessService dataAccessService;
    /* For accessing cassandra(component) server configuration */
    private RealmService realmService;

    private CassandraMBeanLocator mbeanLocator;

    private ConfigurationContextService configCtxService;
    private ServerConfigurationService serverConfigurationService;

    public static CassandraAdminDataHolder getInstance() {
        return thisInstance;
    }

    private CassandraAdminDataHolder() {
        this.mbeanLocator = new CassandraMBeanLocator(ManagementFactory.getPlatformMBeanServer());
    }

    public DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public void setDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configCtxService;
    }

    public void setConfigurationContextService(ConfigurationContextService configCtxService) {
        this.configCtxService = configCtxService;
    }

    public ServerConfigurationService getServerConfigurationService(){
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public UserRealm getRealmForCurrentTenant() throws CassandraServerManagementException {
        try {
            return realmService.getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().
                    getTenantId());
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error accessing the UserRealm for " +
                    "super tenant : " + e);
        }
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public CassandraMBeanLocator getCassandraMBeanLocator() {
        return mbeanLocator;
    }

}
