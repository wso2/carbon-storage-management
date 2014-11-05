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
