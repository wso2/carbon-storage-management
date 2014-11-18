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
package org.wso2.carbon.rssmanager.core.authorize;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtOSGIUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class RSSAuthorizationUtils {

	public static final String DATABASE_RESOURCE = "Database";

	public static final String RSSINSTANCE_RESOURCE = "Rss Instance";

	public static final String DATABASE_USER_RESOURCE = "Database User";

	public static final String ATTACH_DATABASE_USER_RESOURCE = "Attach Database User";

	public static final String ENVIRONMENT_RESOURCE = "Environment";

	public static final String SYSTEM_RESOURCE = "System";

	public static final String USER_DEFINED_RESOURCE = "User Defined";

	public static final String SERVICE_PROVIDER_NAME = "rssmanager";

	public static final String UI_EXECUTE = "ui.execute";

	public static String[] getPermissionListForEnvironment(String envionmentName) {
		String[] permissions = new String[24];
		//create permission resource set for system instances
		permissions[0] = buildAddActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, RSSINSTANCE_RESOURCE);
		permissions[1] = buildEditActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, RSSINSTANCE_RESOURCE);
		permissions[2] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, RSSINSTANCE_RESOURCE);
		permissions[3] = buildAddActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, DATABASE_RESOURCE);
		permissions[4] = buildEditActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, DATABASE_RESOURCE);
		permissions[5] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, DATABASE_RESOURCE);
		permissions[6] = buildAddActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName, DATABASE_USER_RESOURCE);
		permissions[7] = buildEditActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName,
							DATABASE_USER_RESOURCE);
		permissions[8] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName,
							DATABASE_USER_RESOURCE);
		permissions[9] = buildAddActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		permissions[10] = buildEditActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		permissions[11] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, SYSTEM_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		//create permission resource set for user defined instances
		permissions[12] = buildAddActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							RSSINSTANCE_RESOURCE);
		permissions[13] = buildEditActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							RSSINSTANCE_RESOURCE);
		permissions[14] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							RSSINSTANCE_RESOURCE);
		permissions[15] = buildAddActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName, DATABASE_RESOURCE);
		permissions[16] = buildEditActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName, DATABASE_RESOURCE);
		permissions[17] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							DATABASE_RESOURCE);
		permissions[18] = buildAddActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							DATABASE_USER_RESOURCE);
		permissions[19] = buildEditActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							DATABASE_USER_RESOURCE);
		permissions[20] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							DATABASE_USER_RESOURCE);
		permissions[21] = buildAddActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		permissions[22] = buildEditActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		permissions[23] = buildDeleteActionResource(ENVIRONMENT_RESOURCE, USER_DEFINED_RESOURCE, envionmentName,
							ATTACH_DATABASE_USER_RESOURCE);
		return permissions;
	}

	public enum ActionResource {
		ADD("Add"), EDIT("Edit"), DELETE("Delete");

		String action;

		private ActionResource(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}
	}

	public static String buildAddActionResource(String... resources) {
		String completePermissionResource = StringUtils.EMPTY;
		for(String resource : resources) {
			completePermissionResource += resource + "/";
		}
		return completePermissionResource + ActionResource.ADD.getAction();
	}

	public static String buildEditActionResource(String... resources) {
		String completePermissionResource = StringUtils.EMPTY;
		for(String resource : resources) {
			completePermissionResource += resource + "/";
		}
		return completePermissionResource + ActionResource.EDIT.getAction();
	}

	public static String buildDeleteActionResource(String... resources) {
		String completePermissionResource = StringUtils.EMPTY;
		for(String resource : resources) {
			completePermissionResource += resource + "/";
		}
		return completePermissionResource + ActionResource.DELETE.getAction();
	}

	public static String getApplicationResourcePath() {
		StringBuilder applicationResourcePath = new StringBuilder();
		applicationResourcePath.append(CarbonConstants.UI_PERMISSION_NAME);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(ApplicationMgtOSGIUtil.APPLICATION_ROOT_PERMISSION);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(SERVICE_PROVIDER_NAME);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		return applicationResourcePath.toString();
	}

	public static String getPermissionResource(String environmentName, String instanceType, String resource, String action)
			throws RSSManagerException {
		StringBuilder applicationResourcePath = new StringBuilder();
		applicationResourcePath.append(getApplicationResourcePath());
		applicationResourcePath.append(ENVIRONMENT_RESOURCE);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(instanceType)) {
			applicationResourcePath.append(SYSTEM_RESOURCE);
		} else {
			applicationResourcePath.append(USER_DEFINED_RESOURCE);
		}
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(environmentName);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		if(DATABASE_RESOURCE.equals(resource)) {
			applicationResourcePath.append(DATABASE_RESOURCE);
		} else if (RSSINSTANCE_RESOURCE.equals(resource)) {
			applicationResourcePath.append(RSSINSTANCE_RESOURCE);
		} else if (DATABASE_USER_RESOURCE.equals(resource)) {
			applicationResourcePath.append(DATABASE_USER_RESOURCE);
		} else if (ATTACH_DATABASE_USER_RESOURCE.equals(resource)) {
			applicationResourcePath.append(resource);
		} else {
			throw new RSSManagerException("No resource specified for build the permission path");
		}
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(action);
		return applicationResourcePath.toString();
	}
}
