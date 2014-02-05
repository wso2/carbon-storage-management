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
package org.wso2.carbon.rssmanager.data.mgt.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerServiceComponent;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.publisher.internal.StorageDataPublishManager;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.UsageManagerDataHolder;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.transaction.manager.TransactionManagerDummyService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="org.wso2.carbon.rssmanager.data.mgt.component" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * 
 * @scr.reference name="rss.manager"  interface="org.wso2.carbon.rssmanager.core.service.RSSManagerService"
 * cardinality="1..1" policy="dynamic" bind="setRSSManagerService" unbind="unsetRSSManagerService"
 * 
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * 
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class StorageDataPublisherServiceComponent {
    private static Log log = LogFactory.getLog(StorageDataPublisherServiceComponent.class);

    private TaskService taskService;
    private RSSManagerService RSSManagerService;
    private SecretCallbackHandlerService secretCallbackHandlerService;
    private RealmService realmService;
    
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("RSS Cluster tools Admin bundle is activated.");
        }
        try {            
            BundleContext bundleContext = componentContext.getBundleContext();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                    MultitenantConstants.SUPER_TENANT_ID);
            
            bundleContext.registerService(TransactionManagerDummyService.class.getName(),
                                          new TransactionManagerDummyService(), null);
            UsageManagerDataHolder.getInstance().initTransactionManager();
            
            //initialize configurations
            StorageMetaDataConfig.getInstance().init();
            StorageDataPublishManager.getInstance().init(taskService,secretCallbackHandlerService);
            UsageManagerDataHolder.getInstance().setRSSManagerService(this.RSSManagerService);
            UsageManagerDataHolder.getInstance().setRealmService(realmService);
        
        } catch (Exception e) {
        	log.error(e);
        	if(log.isDebugEnabled())
            {
                log.debug("Error while initializing configurations ",e);
            }
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
       
        
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("RSS Cluster tools Admin bundle is deactivated.");
        }
        StorageDataPublishManager.getInstance().destroy();
        StorageMetaDataConfig.getInstance().destroy();
        UsageManagerDataHolder.getInstance().setRSSManagerService(null);

    }


    protected void setTaskService(TaskService taskService) {
        this.taskService=taskService;
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
    }

    protected void unsetTaskService(TaskService taskService) {
        this.taskService=null;
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }

    }
    
    protected void setRSSManagerService(RSSManagerService RSSManagerService) {
    	this.RSSManagerService = RSSManagerService;
        if (log.isDebugEnabled()) {
            log.debug("Setting the RSS Admin Internal");
        }
    }

    protected void unsetRSSManagerService(RSSManagerService RSSManagerService) {
    	RSSManagerService = null;
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the RSS Admin Internal");
        }

    }
    
    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
		if (log.isDebugEnabled()) {
			log.debug("SecretCallbackHandlerService acquired");
		}
		this.secretCallbackHandlerService = secretCallbackHandlerService;
		
	}

	
	protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
		this.secretCallbackHandlerService = null;
	}


    /**
     * Sets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService associated realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }
}
