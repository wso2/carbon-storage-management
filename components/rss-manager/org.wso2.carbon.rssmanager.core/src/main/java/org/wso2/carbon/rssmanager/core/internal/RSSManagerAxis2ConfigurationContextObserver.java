/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.authorize.RSSAuthorizationUtils;
import org.wso2.carbon.rssmanager.core.authorize.RSSAuthorizer;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * To get the events when a new Tenant AxisConfig is terminated
 * Remove all the hector cluster instances created by the terminated client
 */
public class RSSManagerAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

	private static final Log log = LogFactory.getLog(RSSManagerAxis2ConfigurationContextObserver.class);

	public void createdConfigurationContext(ConfigurationContext configurationContext) {
		try {
			if(!RSSAuthorizer.isServiceProviderExist()) {
				RSSAuthorizer.createServiceProvider();
				RSSAuthorizer.definePermissionsForTenant();
				//TODO remove when application management feature handle this
				UserRealm userRealm = RSSManagerDataHolder.getInstance().getRealmForCurrentTenant();
				AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
				authorizationManager.authorizeRole(userRealm.getRealmConfiguration().getAdminRoleName(),
						RSSAuthorizationUtils.getApplicationResourcePath(), RSSAuthorizationUtils.UI_EXECUTE);
			}
		} catch (Exception e) {
			log.error("Setting Cassandra permissions for tenant admin role failed.", e);
		}
	}

	public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
	}

}
