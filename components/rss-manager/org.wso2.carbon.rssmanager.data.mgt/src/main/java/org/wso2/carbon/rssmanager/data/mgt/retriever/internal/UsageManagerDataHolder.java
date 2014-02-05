/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.data.mgt.retriever.internal;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.UsageManagerConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class UsageManagerDataHolder {

	private static final Log log = LogFactory.getLog(UsageManagerDataHolder.class);

    private TransactionManager transactionManager;

    private RSSManagerService RSSManagerService;
    
    private RealmService realmService;

    private static UsageManagerDataHolder thisInstance = new UsageManagerDataHolder();

    private UsageManagerDataHolder() {}

    public static UsageManagerDataHolder getInstance() {
        return thisInstance;
    }

    public void initTransactionManager(){
    	TransactionManager transactionManager = lookupTransactionManager();    	
    	UsageManagerDataHolder.getInstance().setTransactionManager(transactionManager);
    }

    private  TransactionManager lookupTransactionManager() {
        TransactionManager transactionManager = null;
        try {
            Object txObj = InitialContext.doLookup(
                    UsageManagerConstants.STANDARD_USER_TRANSACTION_JNDI_NAME);
            if (txObj instanceof TransactionManager) {
                transactionManager = (TransactionManager) txObj;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find transaction manager at: "
                        + UsageManagerConstants.STANDARD_USER_TRANSACTION_JNDI_NAME, e);
            }
            /* ignore, move onto next step */
        }
        if (transactionManager == null) {
            try {
                transactionManager = InitialContext.doLookup(
                        UsageManagerConstants.STANDARD_TRANSACTION_MANAGER_JNDI_NAME);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find transaction manager at: " +
                            UsageManagerConstants.STANDARD_TRANSACTION_MANAGER_JNDI_NAME, e);
                }
                /* we'll do the lookup later, maybe user provided a custom JNDI name */
            }
        }
        return transactionManager;
    }
    
    /**
     * Retrieves the tenant domain name for a given tenant ID
     *
     * @param tenantId Tenant Id
     * @return Domain name of corresponds to the provided tenant ID
     * @throws RSSManagerException Thrown when there's any error while retrieving the tenant
     *                             domain for the provided tenant ID
     */
    public String getTenantDomainFromTenantId(int tenantId) throws RSSManagerException {
        TenantManager tenantMgr = this.getTenantManager();
        try {
            return tenantMgr.getDomain(tenantId);
        } catch (UserStoreException e) {
            throw new RSSManagerException("Error occurred while retrieving tenant domain for " +
                    "the given tenant ID");
        }
    }
    
    /**
     * Retrieves the associated TenantManager
     *
     * @return TenantManager
     */
    public TenantManager getTenantManager() {
        return getRealmService().getTenantManager();
    }


    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public RSSManagerService getRSSManagerService() {
        return RSSManagerService;
    }

    public void setRSSManagerService(RSSManagerService RSSManagerService) {
        this.RSSManagerService = RSSManagerService;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

	public RealmService getRealmService() {
		return realmService;
	}

	public void setRealmService(RealmService realmService) {
		this.realmService = realmService;
	}
    
    


}
