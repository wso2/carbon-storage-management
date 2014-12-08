/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cassandra.sharedkey;

import org.wso2.carbon.cassandra.server.CarbonCassandraAuthenticator;
import org.wso2.carbon.cassandra.server.cache.UserAccessKeyCache;
import org.wso2.carbon.cassandra.sharedkey.internal.CassandraSharedKeyDSComponent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.Caching;

public class SharedKeyPublisher {

    private static final String CASSANDRA_ACCESS_KEY_CACHE = "CASSANDRA_ACCESS_KEY_CACHE";
    private static final String CASSANDRA_ACCESS_CACHE_MANAGER = "CASSANDRA_ACCESS_CACHE_MANAGER";

	public void injectAccessKey(String username, String password, String targetUser, 
			                                               String accessKey) throws Exception {
		RealmService realmService = CassandraSharedKeyDSComponent.getRealmService();
		if (realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
				         getUserStoreManager().authenticate(username, password)) {
            UserAccessKeyCache userAccessKeyCache = new UserAccessKeyCache(accessKey);
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                cc.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                cc.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
                Cache<String, UserAccessKeyCache> cache = Caching.getCacheManagerFactory()
                        .getCacheManager(CASSANDRA_ACCESS_CACHE_MANAGER).getCache(CASSANDRA_ACCESS_KEY_CACHE);
                cache.put(targetUser,userAccessKeyCache);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

		}
		return;
	}
}
