package org.wso2.carbon.cassandra.sharedkey.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.authentication.SharedKeyAccessService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.cassandra.sharedkey.component"
 *                immediate="true"
 * @scr.reference name="identity.sharekey.service"
 *                interface="org.wso2.carbon.identity.authentication.SharedKeyAccessService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setSharedKeyAccessService" unbind="unSetSharedKeyAccessService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */	 
public class CassandraSharedKeyDSComponent {

	private static SharedKeyAccessService sharedKeyPublisher;
	private static RealmService realmService;

	protected void activate(ComponentContext ctxt) {
		
	}
	
	public static SharedKeyAccessService getSharedKeyPublisher() {
		return sharedKeyPublisher;
	}

	public static RealmService getRealmService() {
		return realmService;
	}

	protected void setSharedKeyAccessService(
			SharedKeyAccessService sharedKeyService) {
		CassandraSharedKeyDSComponent.sharedKeyPublisher = sharedKeyService;
	}

	protected void unSetSharedKeyAccessService(
			SharedKeyAccessService sharedKeyService) {
		CassandraSharedKeyDSComponent.sharedKeyPublisher = null;
	}

	protected void setRealmService(RealmService realmService) {
		CassandraSharedKeyDSComponent.realmService = realmService;
	}

	protected void unsetRealmService(RealmService realmService) {
		CassandraSharedKeyDSComponent.realmService = null;
	}

}
