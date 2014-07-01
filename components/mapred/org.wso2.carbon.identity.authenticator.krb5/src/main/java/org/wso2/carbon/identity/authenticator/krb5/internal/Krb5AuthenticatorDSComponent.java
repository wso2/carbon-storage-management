/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.authenticator.krb5.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.core.services.authentication.ServerAuthenticator;
import org.wso2.carbon.identity.authenticator.krb5.Krb5Authenticator;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Hashtable;

/**
 * @scr.component name="krb5.authenticator.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="server.configuration"
 *                interface="org.wso2.carbon.base.ServerConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 */
public class Krb5AuthenticatorDSComponent {

    private static final Log log = LogFactory.getLog(Krb5AuthenticatorDSComponent.class);
    private static RealmService realmService;
    private static String DISABLE_HDFS_STARTUP = "disable.hdfs.startup";
    
    protected void activate(ComponentContext ctxt) {

        String disableHdfsStartup = System.getProperty(DISABLE_HDFS_STARTUP);
        if ("true".equals(disableHdfsStartup)) {
            log.debug("HDFS Kerberos authenticator is disabled ");
            return;
        }

        try {
            Krb5Authenticator authenticator = new Krb5Authenticator();
            Hashtable<String, String> props = new Hashtable<String, String>();
            Krb5AuthBEDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());
            props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
            ctxt.getBundleContext().registerService(ServerAuthenticator.class.getName(), authenticator, props);
            log.debug("Carbon Core Services bundle is activated ");
        } catch (Throwable e) {
            log.error("Failed to activate Carbon Core Services bundle ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Carbon Core Services bundle is deactivated ");
    }
   
    protected void setRegistryService(RegistryService registryService) {
        Krb5AuthBEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Krb5AuthBEDataHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        Krb5AuthBEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Krb5AuthBEDataHolder.getInstance().setRealmService(null);
    }
    
    public static BundleContext getBundleContext() throws Exception {
        return Krb5AuthBEDataHolder.getInstance().getBundleContext();
    }

    public static RealmService getRealmService() throws Exception {
        return Krb5AuthBEDataHolder.getInstance().getRealmService();
    }

    public static RegistryService getRegistryService() throws Exception {
        return Krb5AuthBEDataHolder.getInstance().getRegistryService();
    }

    /*public static LoginSubscriptionManagerServiceImpl getLoginSubscriptionManagerServiceImpl() {
        return loginSubscriptionManagerServiceImpl;
    }*/

    protected void setServerConfiguration(ServerConfiguration configuration) {
        Krb5AuthBEDataHolder.getInstance().setServerConfiguration(configuration);
    }
    
    public static ServerConfiguration getServerConfiguration() throws Exception {
        return Krb5AuthBEDataHolder.getInstance().getServerConfiguration();
    }
    
    protected void unsetServerConfiguration(ServerConfiguration configuration) {
    	Krb5AuthBEDataHolder.getInstance().setServerConfiguration(null);
    }

    /*protected void setConfigurationContextService(ConfigurationContextService contextService) {
        CarbonServicesServiceComponent.configContextService = contextService;
    }*/

    /*protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        CarbonServicesServiceComponent.configContextService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() throws Exception{
        if (serverConfiguration == null) {
            String msg = "Axis configuration is null. Some bundles in the system have not started";
            log.error(msg);
            throw new Exception(msg);
        }
        return CarbonServicesServiceComponent.configContextService;
    }*/
}
