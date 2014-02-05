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
