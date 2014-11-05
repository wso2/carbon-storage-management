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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cassandra.common.CassandraConstants;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

public class RegistryAccessor {

    private static final Log log = LogFactory.getLog(RegistryAccessor.class);

    public void addEnvironmentToRegistry(Environment env) throws CassandraServerManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Registry registry = cc.getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            if (!registry.resourceExists(CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH)) {
                Collection environmentsCollection = registry.newCollection();
                registry.put(CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH,
                        environmentsCollection);
            }
            Collection envCollection = registry.newCollection();
            envCollection.setProperty(CassandraConstants.Configurations.ENVIRONMENT_NAME, env.getEnvironmentName());
            envCollection.setProperty(CassandraConstants.Configurations.IS_EXTERNAL, String.valueOf(env.isExternal()));
            registry.put(CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/"
                    + env.getEnvironmentName(), envCollection);
            if (!CassandraConstants.Environments.CASSANDRA_DEFAULT_ENVIRONMENT.equals(env.getEnvironmentName())) {
                Collection clusterCollection = registry.newCollection();
                registry.put(CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" +
                        env.getEnvironmentName() + "/" +
                        CassandraConstants.Environments.CASSANDRA_CLUSTERS, clusterCollection);
                for (Cluster cluster : env.getClusters()) {
                    Resource clustersResource = registry.newResource();
                    clustersResource.setProperty(CassandraConstants.Configurations.CLUSTER_NAME, cluster.getName());
                    clustersResource.setProperty(CassandraConstants.Configurations.DATASOURCE_NAME,
                            cluster.getDataSourceJndiName());
                    registry.put(CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" +
                            env.getEnvironmentName() + "/" +
                            CassandraConstants.Environments.CASSANDRA_CLUSTERS + "/" +
                            cluster.getName(), clustersResource);
                }
            }
        } catch (Exception e) {
            log.error("Unable to add Environment '" + env.getEnvironmentName() + "' to registry", e);
            throw new CassandraServerManagementException("Unable to add Environment '"
                    + env.getEnvironmentName() + "' to registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public Environment getEnvironmentFromRegistry(String environmentName)
            throws CassandraServerManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Registry registry = CarbonContext.getThreadLocalCarbonContext()
                    .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            String resourcePath = CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH
                    + "/" + environmentName;
            if (registry.resourceExists(resourcePath)) {
                return this.getEnvironmentFromRegistryCollection(resourcePath, registry);
            }
        } catch (Exception e) {
            log.error("Unable to get Environment '" + environmentName + "' from registry", e);
            throw new CassandraServerManagementException("Unable to get Environment '"
                    + environmentName + "' from registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        throw new CassandraServerManagementException("Cassandra Environment doesn't exist ");
    }

    public Environment[] getAllEnvironmentsFromRegistry() throws CassandraServerManagementException {
        String resourcePath = CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Registry registry = CarbonContext.getThreadLocalCarbonContext()
                    .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            if (registry.resourceExists(resourcePath)) {
                Collection environmentsCollection = (Collection) registry.get(resourcePath);
                Environment[] environments = new Environment[environmentsCollection.getChildCount()];
                int i = 0;
                for (String envPath: environmentsCollection.getChildren()) {
                    environments[i++] = this.getEnvironmentFromRegistryCollection(envPath, registry);
                }
                return environments;
            }
        } catch (Exception e) {
            log.error("Unable to get Environments from registry", e);
            throw new CassandraServerManagementException("Unable to get Environments from registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        log.warn("Resource Path '" + resourcePath + "' does not exits. Returning 0 environments.");
        return new Environment[0];
    }

    public void deleteEnvironmentFromRegistry(String environmentName)
            throws CassandraServerManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Registry registry = CarbonContext.getThreadLocalCarbonContext()
                    .getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            String resourcePath = CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH
                    + "/" + environmentName;
            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath);
            }
        } catch (Exception e) {
            log.error("Unable to delete Environment '" + environmentName + "' from registry", e);
            throw new CassandraServerManagementException("Unable to delete Environment '"
                    + environmentName + "' from registry", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private Environment getEnvironmentFromRegistryCollection(String envPath, Registry registry)
            throws RegistryException {
        Collection envCollection = (Collection) registry.get(envPath);
        Environment environment = new Environment();
        environment.setEnvironmentName(envCollection.getProperty(
                CassandraConstants.Configurations.ENVIRONMENT_NAME));
        environment.setExternal(Boolean.valueOf(envCollection.getProperty(
                CassandraConstants.Configurations.IS_EXTERNAL)));
        String clusterResourcePath = CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_REGISTRY_PATH + "/" +
                environment.getEnvironmentName() + "/" + CassandraConstants.Environments.CASSANDRA_CLUSTERS;
        if (!CassandraConstants.Environments.CASSANDRA_DEFAULT_ENVIRONMENT.equals(environment.getEnvironmentName())) {
            Collection clusterCollection = (Collection) registry.get(clusterResourcePath);
            Cluster[] clusters = new Cluster[clusterCollection.getChildCount()];
            int j = 0;
            for (String clusterName : clusterCollection.getChildren()) {
                Resource clusterResource = registry.get(clusterName);
                Cluster cluster = new Cluster();
                cluster.setName(clusterResource.getProperty(
                        CassandraConstants.Configurations.CLUSTER_NAME));
                cluster.setDataSourceJndiName(clusterResource.getProperty(
                        CassandraConstants.Configurations.DATASOURCE_NAME));
                clusters[j++] = cluster;
            }
            environment.setClusters(clusters);
        }
        return environment;
    }

}
