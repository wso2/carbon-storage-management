package org.wso2.carbon.rssmanager.core.authorize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAOFactory;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

public class RSSAuthorizer {

	private static final Log log = LogFactory.getLog(RSSAuthorizer.class);

	public static void isUserAuthorize(String resourcePath)
			throws RSSManagerException {
		try {
			RSSManagerDataHolder dataHolder = RSSManagerDataHolder.getInstance();
			UserRealm userRealm = null;
			AuthorizationManager authorizationManager = null;
			boolean authorize = false;

			try {
				userRealm = dataHolder.getRealmForCurrentTenant();
				if (userRealm == null) {
					throw new RSSManagerException("User Realm can't be null");
				}
				authorizationManager = userRealm.getAuthorizationManager();
			}
			catch (UserStoreException e) {
				throw new RSSManagerException("Error getting Authorization Manager.", e);
			}
			catch (RSSManagerCommonException e) {
				throw new RSSManagerException("Error getting User Realm.", e);
			}

			String user = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
			String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
			if (!authorizationManager.isUserAuthorized(tenantLessUsername, resourcePath, RSSAuthorizationUtils.UI_EXECUTE)) {
				throw new RSSManagerException("Permission denied");
			}
		}
		catch (UserStoreException e) {
			throw new RSSManagerException("Error retrieving authorized role list for :"
					+ resourcePath, e);
		}
	}

	public static void createServiceProviderIfNotExist()
			throws RSSManagerException, IdentityApplicationManagementException {
		RSSManagerDataHolder dataHolder = RSSManagerDataHolder.getInstance();
		if(isServiceProviderExist()) {
			return;
		}
		ServiceProvider serviceProvider;
		try {
			serviceProvider = new ServiceProvider();
			serviceProvider.setApplicationName(RSSAuthorizationUtils.SERVICE_PROVIDER_NAME);
			serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
			dataHolder.getAppMgtOSGIService().createApplication(serviceProvider);
		}
		catch (IdentityApplicationManagementException ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}
		catch (Exception ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}
	}

	public static boolean isServiceProviderExist() throws IdentityApplicationManagementException {
		RSSManagerDataHolder dataHolder = RSSManagerDataHolder.getInstance();
		ApplicationBasicInfo[] applicationBasicInfo = dataHolder.getAppMgtOSGIService().getAllApplicationBasicInfo();
		if(applicationBasicInfo != null) {
			for (ApplicationBasicInfo basicInfo : applicationBasicInfo) {
				if (basicInfo.getApplicationName().equals(RSSAuthorizationUtils.SERVICE_PROVIDER_NAME)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ServiceProvider getServiceProvider() throws RSSManagerException {
		RSSManagerDataHolder dataHolder = RSSManagerDataHolder.getInstance();
		ServiceProvider serviceProvider = null;
		try {
			serviceProvider = dataHolder.getAppMgtOSGIService().getApplication(RSSAuthorizationUtils.SERVICE_PROVIDER_NAME);
		}
		catch (IdentityApplicationManagementException ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}
		catch (Exception ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}

		return serviceProvider;
	}

	public static void definePermissionToServiceProviderOfTenant() throws RSSManagerException {
		EnvironmentDAO environmentDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO().getEnvironmentDAO();
		try {
			for(Environment environment : environmentDAO.getAllEnvironments()) {
				definePermissionToServiceProvider(environment.getName());
			}
		}
		catch (RSSDAOException e) {
			throw new RSSManagerException("Error while defining permissions for tenants' service provider", e);
		}
	}

	public static void definePermissionToServiceProvider(String envionmentName)
			throws RSSManagerException {
		RSSManagerDataHolder dataHolder = RSSManagerDataHolder.getInstance();
		ServiceProvider serviceProvider;
		try {
			serviceProvider = dataHolder.getAppMgtOSGIService().getApplication(RSSAuthorizationUtils.SERVICE_PROVIDER_NAME);
			if (serviceProvider == null) {
				return;
			}
			List<ApplicationPermission> permissionList = new ArrayList<ApplicationPermission>();
			PermissionsAndRoleConfig permRoleConfig = serviceProvider.getPermissionAndRoleConfig();
			for (String permission : RSSAuthorizationUtils.getPermissionListForEnvironment(envionmentName)) {
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
			dataHolder.getAppMgtOSGIService().updateApplication(serviceProvider);
		}
		catch (IdentityApplicationManagementException ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}
		catch (Exception ex) {
			throw new RSSManagerException("Error during creating application ", ex);
		}
	}
}
