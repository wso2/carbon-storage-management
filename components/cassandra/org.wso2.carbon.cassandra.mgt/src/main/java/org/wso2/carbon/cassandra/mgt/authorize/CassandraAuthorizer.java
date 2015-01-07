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
package org.wso2.carbon.cassandra.mgt.authorize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.cassandra.mgt.environment.Environment;
import org.wso2.carbon.cassandra.mgt.environment.RegistryAccessor;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

public class CassandraAuthorizer {

	private static final Log log = LogFactory.getLog(CassandraAuthorizer.class);

	/**
	 * Check whether current user is authorize to given resource
	 *
	 * @param resourcePath resource path to check the permission
	 * @throws CassandraServerManagementException if user is not authorize
	 */
	public static void isUserAuthorize(String resourcePath)
			throws CassandraServerManagementException {
		try {
			CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
			UserRealm userRealm;
			AuthorizationManager authorizationManager;
			try {
				userRealm = dataHolder.getRealmForCurrentTenant();
				if (userRealm == null) {
					throw new CassandraServerManagementException("User Realm can't be null");
				}
				authorizationManager = userRealm.getAuthorizationManager();
			} catch (UserStoreException e) {
				throw new CassandraServerManagementException("Error getting Authorization Manager.", e);
			}

			String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
			String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(username);
			if (!authorizationManager.isUserAuthorized(tenantLessUsername, resourcePath, CassandraAuthorizationUtils
					.UI_EXECUTE)) {
				log.debug("Permission denied for the user to the resource " + resourcePath);
				throw new CassandraServerManagementException("Permission denied for the user");
			}
		} catch (UserStoreException e) {
			throw new CassandraServerManagementException("Error checking the resource authorize permissions:" + resourcePath, e);
		}
	}

	/**
	 * Create cassandra manager service provider if not exist
	 * @throws CassandraServerManagementException if anything went wrong while creating the service provider
	 */
	public static void createServiceProvider() throws CassandraServerManagementException {
		CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
		ServiceProvider serviceProvider;
		try {
			//Check whether service provider existence
			if (isServiceProviderExist()) {
				return;
			}
			serviceProvider = new ServiceProvider();
			serviceProvider.setApplicationName(CassandraAuthorizationUtils.SERVICE_PROVIDER_NAME);
			serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
			dataHolder.getApplicationManagementService().createApplication(serviceProvider);
		} catch (IdentityApplicationManagementException ex) {
			throw new CassandraServerManagementException("Error during creating application ", ex);
		}
	}

	/**
	 * Check whether cassandra manager service provider existence. For cassandra manager we have define service provider for
	 * each tenant
	 * This will check availability of the service provider for each tenant
	 *
	 * @return true if service provider exist, else false
	 * @throws IdentityApplicationManagementException if something wrong when checking the service provider existence
	 */
	public static boolean isServiceProviderExist() throws IdentityApplicationManagementException {
		CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
		ApplicationBasicInfo[] applicationBasicInfo = dataHolder.getApplicationManagementService().getAllApplicationBasicInfo();
		if (applicationBasicInfo != null) {
			for (ApplicationBasicInfo basicInfo : applicationBasicInfo) {
				if (CassandraAuthorizationUtils.SERVICE_PROVIDER_NAME.equals(basicInfo.getApplicationName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get service provider of the current user
	 *
	 * @return service provider
	 * @throws CassandraServerManagementException if something went wrong when getting service provider
	 */
	public static ServiceProvider getServiceProvider() throws CassandraServerManagementException {
		CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
		ServiceProvider serviceProvider = null;
		try {
			serviceProvider = dataHolder.getApplicationManagementService().getApplication(CassandraAuthorizationUtils.SERVICE_PROVIDER_NAME);
		} catch (IdentityApplicationManagementException ex) {
			throw new CassandraServerManagementException("Error during creating application ", ex);
		}
		return serviceProvider;
	}

	/**
	 * Define set of permissions for the tenant
	 *
	 * @throws CassandraServerManagementException if something went wrong when defining permissions to tenant
	 */
	public static void definePermissionsForTenant() throws CassandraServerManagementException {
		RegistryAccessor registryAccessor = RegistryAccessor.getInstance();
		for(Environment environment : registryAccessor.getAllEnvironmentsFromRegistry()) {
			definePermissions(environment.getEnvironmentName());
		}
	}

	/**
	 * Define permissions required in given environment
	 *
	 * @param environmentName name of the environment
	 * @throws CassandraServerManagementException if something went wrong while defining the permissions
	 */
	public static void definePermissions(String environmentName)
			throws CassandraServerManagementException {
		CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
		ServiceProvider serviceProvider;
		try {
			serviceProvider = dataHolder.getApplicationManagementService().getApplication(CassandraAuthorizationUtils.SERVICE_PROVIDER_NAME);
			if (serviceProvider == null) {
				return;
			}
			List<ApplicationPermission> permissionList = new ArrayList<ApplicationPermission>();
			PermissionsAndRoleConfig permRoleConfig = serviceProvider.getPermissionAndRoleConfig();
			for (String permission : CassandraAuthorizationUtils.getPermissionListForEnvironment(environmentName)) {
				ApplicationPermission appPerm = new ApplicationPermission();
				appPerm.setValue(permission);
				permissionList.add(appPerm);
			}
			if (permRoleConfig != null) {
				permRoleConfig.setPermissions(permissionList.toArray(new ApplicationPermission[permissionList.size()]));
			} else {
				permRoleConfig = new PermissionsAndRoleConfig();
				permRoleConfig.setPermissions(permissionList.toArray(new ApplicationPermission[permissionList.size()]));
				serviceProvider.setPermissionAndRoleConfig(permRoleConfig);
			}
			dataHolder.getApplicationManagementService().updateApplication(serviceProvider);
		} catch (IdentityApplicationManagementException ex) {
			throw new CassandraServerManagementException("Error during creating application ", ex);
		}
	}
}
