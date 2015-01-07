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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.cassandra.mgt.authorize.CassandraAuthorizer;
import org.wso2.carbon.cassandra.mgt.environment.EnvironmentManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="org.wso2.carbon.cassandra.mgt.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.cassandra.dataaccess.component"
 * interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService" cardinality="1..1"
 * policy="dynamic" bind="setDataAccessService" unbind="unSetDataAccessService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="org.wso2.carbon.base.api.ServerConfigurationService"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.identity.application.mgt.ApplicationManagementService"
 * interface="org.wso2.carbon.identity.application.mgt.ApplicationManagementService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setApplicationManagementService"
 * unbind="unsetApplicationManagementService"
 */
public class CassandraAdminDSComponent {

    private static Log log = LogFactory.getLog(CassandraAdminDSComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Admin bundle is activated.");
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        //TODO avoid setting admin username here
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
        EnvironmentManager environmentManager = new EnvironmentManager();
        componentContext.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                new CassandraAxis2ConfigurationContextObserver(), null);
        try {
            CassandraAuthorizer.createServiceProvider();
            environmentManager.initEnvironments();
            CassandraAdminDataHolder.getInstance().setEnvironmentManager(environmentManager);
            if (log.isDebugEnabled()) {
                log.debug("Cassandra Environments are initialized.");
            }
            CassandraAdminDataHolder.getInstance().setInitialized(true);
        } catch (CassandraServerManagementException e) {
            log.error("Cassandra Environments Initialization Failed.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Admin bundle is deactivated.");
        }
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        CassandraAdminDataHolder.getInstance().setDataAccessService(dataAccessService);
    }

    protected void unSetDataAccessService(DataAccessService dataAccessService) {
        CassandraAdminDataHolder.getInstance().setDataAccessService(null);
    }

    protected void setRealmService(RealmService realmService) {
        CassandraAdminDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        CassandraAdminDataHolder.getInstance().setRealmService(null);
    }

    protected void setConfigurationContext(ConfigurationContextService ctxService) {
        CassandraAdminDataHolder.getInstance().setConfigurationContextService(ctxService);
    }

    protected void unsetConfigurationContext(ConfigurationContextService ctxService) {
        CassandraAdminDataHolder.getInstance().setConfigurationContextService(null);
    }

    public void unsetServerConfiguration(ServerConfigurationService serverConfigService) {
        CassandraAdminDataHolder.getInstance().setServerConfigurationService(null);
    }

    public void setServerConfiguration(ServerConfigurationService serverConfigService) {
        CassandraAdminDataHolder.getInstance().setServerConfigurationService(serverConfigService);
    }

    protected void setApplicationManagementService(ApplicationManagementService service){
        if (log.isDebugEnabled()) {
            log.debug("Setting ApplicationManagementService");
        }
        CassandraAdminDataHolder.getInstance().setApplicationManagementService(service);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService service){
        if (log.isDebugEnabled()) {
            log.debug("Unsetting ApplicationManagementService");
        }
        CassandraAdminDataHolder.getInstance().setApplicationManagementService(null);
    }
}
