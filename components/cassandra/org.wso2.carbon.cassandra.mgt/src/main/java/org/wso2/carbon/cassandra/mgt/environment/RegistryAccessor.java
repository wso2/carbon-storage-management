/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cassandra.mgt.environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.common.CassandraConstants;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.ResourceImpl;

public class RegistryAccessor {

    private static final Log log = LogFactory.getLog(RegistryAccessor.class);
    public final String DATASOURCE_NAME = "DatasourceName";
    public final String ENVIRONMENT_NAME = "EnvironmentName";
    public final String IS_EXTERNAL = "IsExternal";

    public void addEnvironmentToRegistry(Environment env) throws CassandraServerManagementException {
        try {
            Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            if (!registry.resourceExists(CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH)) {
                Collection envCollection = registry.newCollection();
                registry.put(CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH, envCollection);
            }
            ResourceImpl envResource = new ResourceImpl();
            envResource.setName(env.getEnvironmentName());
            envResource.setProperty(ENVIRONMENT_NAME, env.getEnvironmentName());
            envResource.setProperty(DATASOURCE_NAME, env.getDataSourceName());
            envResource.setProperty(IS_EXTERNAL, String.valueOf(env.isExternal()));
            registry.put(CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" + envResource.getName(), envResource);
        } catch (Exception e) {
            log.error("Unable to add Environment '" + env.getEnvironmentName() + "' to registry", e);
            throw new CassandraServerManagementException("Unable to add Environment '" + env.getEnvironmentName() + "' to registry", e);
        }
    }

    public Environment getEnvironmentFromRegistry(String environmentName) throws CassandraServerManagementException {
        try {
            Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            String resourcePath = CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" + environmentName;
            if (registry.resourceExists(resourcePath)) {
                Environment env = new Environment();
                Resource resource = registry.get(resourcePath);
                env.setEnvironmentName(resource.getProperty(ENVIRONMENT_NAME));
                env.setDataSourceName(resource.getProperty(DATASOURCE_NAME));
                env.setExternal(Boolean.valueOf(resource.getProperty(IS_EXTERNAL)));
                return env;
            }
        } catch (Exception e) {
            log.error("Unable to get Environment '" + environmentName + "' from registry", e);
            throw new CassandraServerManagementException("Unable to get Environment '" + environmentName + "' from registry", e);
        }
        return null; // Resource does not exist
    }

    public Environment[] getAllEnvironmentsFromRegistry() throws CassandraServerManagementException {
        String resourcePath = CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH;
        try {
            Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            if (registry.resourceExists(resourcePath)) {
                CollectionImpl envCollection = (CollectionImpl) registry.get(resourcePath);
                Environment[] environments = new Environment[envCollection.getChildCount()];
                int i = 0;
                for (String envName : envCollection.getChildren()) {
                    Resource envResource = registry.get(envName);
                    Environment environment = new Environment();
                    environment.setEnvironmentName(envResource.getProperty(ENVIRONMENT_NAME));
                    environment.setDataSourceName(envResource.getProperty(DATASOURCE_NAME));
                    environment.setExternal(Boolean.valueOf(envResource.getProperty(IS_EXTERNAL)));
                    environments[i++] = environment;
                }
                return environments;
            }
        } catch (Exception e) {
            log.error("Unable to get Environments from registry", e);
            throw new CassandraServerManagementException("Unable to get Environments from registry", e);
        }
        log.warn("Resource Path '" + resourcePath + "' does not exits. Returning 0 environments." );
        return new Environment[0]; // Resource does not exist
    }

    public void deleteEnvironmentFromRegistry(String environmentName) throws CassandraServerManagementException {
        try {
            Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            String resourcePath = CassandraConstants.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" + environmentName;
            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath);
            }
        } catch (Exception e) {
            log.error("Unable to delete Environment '" + environmentName + "' from registry", e);
            throw new CassandraServerManagementException("Unable to delete Environment '" + environmentName + "' from registry", e);
        }
    }

}
