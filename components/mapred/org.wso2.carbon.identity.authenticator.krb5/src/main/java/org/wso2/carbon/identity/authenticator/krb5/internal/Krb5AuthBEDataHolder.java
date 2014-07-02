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

import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class is used as the singleton data holder token based authenticator BE module.
 */
public class Krb5AuthBEDataHolder {
    private static Krb5AuthBEDataHolder instance = new Krb5AuthBEDataHolder();

    private RealmService realmService;
    private RegistryService registryService;
    private BundleContext bundleContext;
    private ServerConfiguration serverConfiguration;
    
    private Krb5AuthBEDataHolder(){
    }

    public static Krb5AuthBEDataHolder getInstance(){
        return instance;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }
    
    public void setBundleContext(BundleContext bundleContext) {
    	this.bundleContext = bundleContext;
    }
    
    public BundleContext getBundleContext() {
    	return bundleContext;
    }
    
    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
    	this.serverConfiguration = serverConfiguration;
    }
    
    public ServerConfiguration getServerConfiguration() {
    	return this.serverConfiguration;
    }
}
