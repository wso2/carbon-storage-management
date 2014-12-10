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

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtOSGIUtil;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.util.ArrayList;
import java.util.List;

public class CassandraAuthorizationUtils {

	public static final String KEYSPACE_RESOURCE = "Keyspace";

	public static final String COLUMN_FAMILY_RESOURCE = "Column Family";

	public static final String COLUMN_RESOURCE = "Column";

	public static final String ALL_ENVIRONMENT_RESOURCE = "All Environments";

	public static final String SERVICE_PROVIDER_NAME = "Cassandra";

	public static final String UI_EXECUTE = "ui.execute";

	/**
	 * Create the permission list for the application per environment
	 *
	 * @param environmentName name of the environment
	 * @return permission list
	 */
	public static String[] getPermissionListForEnvironment(String environmentName) {
		List<String> permissions = new ArrayList<String>();
		//create permission resource set for system instances
		permissions.add(buildAddActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				KEYSPACE_RESOURCE));
		permissions.add(buildEditActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				KEYSPACE_RESOURCE));
		permissions.add(buildDeleteActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				KEYSPACE_RESOURCE));
		permissions.add(buildAddActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_FAMILY_RESOURCE));
		permissions.add(buildEditActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_FAMILY_RESOURCE));
		permissions.add(buildDeleteActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_FAMILY_RESOURCE));
		permissions.add(buildAddActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_RESOURCE));
		permissions.add(buildEditActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_RESOURCE));
		permissions.add(buildDeleteActionResource(ALL_ENVIRONMENT_RESOURCE, environmentName,
				COLUMN_RESOURCE));
		return permissions.toArray(new String[permissions.size()]);
	}

	/**
	 * Build the add permission resource from given resource list
	 *
	 * @param resources set of resources
	 * @return permission string
	 */
	public static String buildAddActionResource(String... resources) {
		StringBuilder addPermissionResource = new StringBuilder();
		for (String resource : resources) {
			addPermissionResource.append(resource);
			addPermissionResource.append("/");
		}
		addPermissionResource.append(ActionResource.ADD.getAction());
		return addPermissionResource.toString();
	}

	/**
	 * Build the edit permission resource from given resource list
	 *
	 * @param resources set of resources
	 * @return permission string
	 */
	public static String buildEditActionResource(String... resources) {
		StringBuilder editPermissionResource = new StringBuilder();
		for (String resource : resources) {
			editPermissionResource.append(resource);
			editPermissionResource.append("/");
		}
		editPermissionResource.append(ActionResource.EDIT.getAction());
		return editPermissionResource.toString();
	}

	/**
	 * Build the delete permission resource from given resource list
	 *
	 * @param resources set of resources
	 * @return permission string
	 */
	public static String buildDeleteActionResource(String... resources) {
		StringBuilder deletePermissionResource = new StringBuilder();
		for (String resource : resources) {
			deletePermissionResource.append(resource);
			deletePermissionResource.append("/");
		}
		deletePermissionResource.append(ActionResource.DELETE.getAction());
		return deletePermissionResource.toString();
	}

	/**
	 * Get service provider application path
	 *
	 * @return application resource path
	 */
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

	/**
	 * Build permission resource for given resources
	 *
	 * @param environmentName name of the environment
	 * @param resource        sub resource under the environment
	 * @param action          action to perform on resource
	 * @return permission resource path
	 * @throws CassandraServerManagementException if something went wrong when building the permission path
	 */
	public static String getPermissionResource(String environmentName, String resource, String action)
			throws CassandraServerManagementException {
		StringBuilder applicationResourcePath = new StringBuilder();
		applicationResourcePath.append(getApplicationResourcePath());
		applicationResourcePath.append(ALL_ENVIRONMENT_RESOURCE);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(environmentName);
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		if (KEYSPACE_RESOURCE.equals(resource)) {
			applicationResourcePath.append(KEYSPACE_RESOURCE);
		} else if (COLUMN_FAMILY_RESOURCE.equals(resource)) {
			applicationResourcePath.append(COLUMN_FAMILY_RESOURCE);
		} else if (COLUMN_RESOURCE.equals(resource)) {
			applicationResourcePath.append(COLUMN_RESOURCE);
		} else {
			throw new CassandraServerManagementException("No sub resource specified for build the permission path");
		}
		applicationResourcePath.append(RegistryConstants.PATH_SEPARATOR);
		applicationResourcePath.append(action);
		return applicationResourcePath.toString();
	}

	/**
	 * RSS action resources
	 */
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
}
