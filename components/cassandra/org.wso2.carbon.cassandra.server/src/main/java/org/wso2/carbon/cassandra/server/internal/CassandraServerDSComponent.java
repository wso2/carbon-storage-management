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
package org.wso2.carbon.cassandra.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cassandra.server.CassandraServerConstants;
import org.wso2.carbon.cassandra.server.CassandraServerController;
import org.wso2.carbon.cassandra.server.TenantCreationListener;
import org.wso2.carbon.cassandra.server.service.CassandraServerService;
import org.wso2.carbon.cassandra.server.service.CassandraServerServiceImpl;
import org.wso2.carbon.cassandra.server.util.CassandraServerUtil;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * @scr.component name="org.wso2.carbon.cassandra.server.component" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class CassandraServerDSComponent {

    private static Log log = LogFactory.getLog(CassandraServerDSComponent.class);

    private static final String CASSANDRA_YAML_PATH = CarbonUtils.getEtcCarbonConfigDirPath() +
            File.separator + "cassandra.yaml";
    private static final String CASSANDRA_CONFIG_DIR_PATH = CarbonUtils.getEtcCarbonConfigDirPath();
    private static final String DEFAULT_CONF = "/org/wso2/carbon/cassandra/server/deployment/cassandra_default.yaml";

    /**
     * WSO2 Carbon Port for carbon.xml
     */
    private static int CARBON_DEFAULT_PORT_OFFSET = 0;
    private static String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";

    private static final String DISABLE_CASSANDRA_SERVER_STARTUP = "disable.cassandra.server.startup";
    private static final String DEFAULT_CASSANDRA_RPC_PORT = "cassandra.rpc.port";
    private static final String DEFAULT_CASSANDRA_STORAGE_PORT = "cassandra.storage.port";
    private static final String DEFAULT_CASSANDRA_SSL_STORAGE_PORT = "cassandra.ssl.storage.port";
    private static final String DEFAULT_CASSANDRA_NATIVE_TRANSPORT_PORT = "cassandra.native.transport.port";
    private static final String DEFAULT_CASSANDRA_YAML_PATH = "cassandra.config";
    private static final String DEFAULT_CASSANDRA_CONFIG_DIR_PATH = "cassandra.config.dir";

    private CassandraServerController cassandraServerController;

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting the Cassandra Server component");
            }

            /* initialize and start the Cassandra server */
            String cassandraConfLocation = DEFAULT_CONF;
            if (isConfigurationExists()) {
                cassandraConfLocation = "file:" + CASSANDRA_YAML_PATH;
            }
            System.setProperty(DEFAULT_CASSANDRA_YAML_PATH, cassandraConfLocation);
            System.setProperty(DEFAULT_CASSANDRA_CONFIG_DIR_PATH, CASSANDRA_CONFIG_DIR_PATH);
            System.setProperty("cassandra-foreground", "yes");
            int carbonPortOffset = CassandraServerUtil.getPortOffset();

            int cassandraRPCPort =
                    CassandraServerUtil.readPortFromSystemVar(
                            CassandraServerConstants.ServerConfiguration.CASSANDRA_RPC_PORT,
                            carbonPortOffset, DEFAULT_CASSANDRA_RPC_PORT);
            System.setProperty("cassandra.rpc_port", Integer.toString(cassandraRPCPort));

            int cassandraStoragePort =
                    CassandraServerUtil.readPortFromSystemVar(
                            CassandraServerConstants.ServerConfiguration.CASSANDRA_STORAGE_PORT,
                            carbonPortOffset, DEFAULT_CASSANDRA_STORAGE_PORT);
            System.setProperty("cassandra.storage_port", Integer.toString(cassandraStoragePort));

            int cassandraSSLStoragePort =
                    CassandraServerUtil.readPortFromSystemVar(
                            CassandraServerConstants.ServerConfiguration.CASSANDRA_SSL_STORAGE_PORT,
                            carbonPortOffset, DEFAULT_CASSANDRA_SSL_STORAGE_PORT);
            System.setProperty("cassandra.ssl_storage_port", Integer.toString(cassandraSSLStoragePort));

            int cassandraNativeTransportPort =
                    CassandraServerUtil.readPortFromSystemVar(
                            CassandraServerConstants.ServerConfiguration.CASSANDRA_NATIVE_TRANSPORT_PORT,
                            carbonPortOffset, DEFAULT_CASSANDRA_NATIVE_TRANSPORT_PORT
                    );
            System.setProperty("cassandra.native_transport_port", Integer.toString(cassandraNativeTransportPort));

            cassandraServerController = new CassandraServerController();
            //register OSGI service
            CassandraServerService cassandraServerService =
                    new CassandraServerServiceImpl(cassandraServerController);

            /* Loading tenant specific data */
            componentContext.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new CassandraServerAxis2ConfigContextObserver(), null);

            componentContext.getBundleContext().registerService(
                    CassandraServerService.class.getName(), cassandraServerService, null);
            componentContext.getBundleContext().registerService(TenantMgtListener.class.getName(),
                    new TenantCreationListener(), null);
            //Disable Cassandra server
            String disableServerStartup = System.getProperty(DISABLE_CASSANDRA_SERVER_STARTUP);
            if ("true".equals(disableServerStartup)) {
                log.debug("Cassandra server is not started in service activator");
                return;
            }
            cassandraServerController.start();
        } catch (Throwable e) {
            log.error("Error occurred while initializing Cassandra Server component", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the Cassandra Server component");
        }
        /* Stop and destroy the Cassandra server */
        if (cassandraServerController != null) {
            cassandraServerController.shutdown();
        }
    }

    protected void setRealmService(RealmService realmService) {
        CassandraServerDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        CassandraServerDataHolder.getInstance().setRealmService(null);
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        CassandraServerDataHolder.getInstance().setAuthenticationService(authenticationService);
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        CassandraServerDataHolder.getInstance().setAuthenticationService(null);
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfigService) {
        CassandraServerDataHolder.getInstance().setServerConfigurationService(serverConfigService);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigService) {
        CassandraServerDataHolder.getInstance().setServerConfigurationService(serverConfigService);
    }

    /**
     * Checks the existence of the cassandra.yaml
     *
     * @return true if cassandra.yaml is in conf/etc directory
     */
    private boolean isConfigurationExists() {
        if (!new File(CASSANDRA_YAML_PATH).exists()) {
            log.info("There is no " + CASSANDRA_YAML_PATH + ". Using the default " +
                    "configuration");
            return false;
        } else {
            return true;
        }
    }

}