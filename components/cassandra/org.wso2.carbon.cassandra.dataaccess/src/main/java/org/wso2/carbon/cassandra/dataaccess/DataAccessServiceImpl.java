/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.dataaccess;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.internal.DataAccessDependencyHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The default implementation for <code>DataAccessService</code>.
 */

public class DataAccessServiceImpl implements DataAccessService {

    private static Log log = LogFactory.getLog(DataAccessServiceImpl.class);

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String LOCAL_HOST_NAME = "localhost";

    private final MultitenantClusterRepository clusterRepository =
            new MultitenantClusterRepository();
    /* A lock object to be used to synchronize the creation of a hector cluster */
    private final Object lock = new Object();

    /**
     * Returns a Cassandra cluster for the given user
     *
     * @param clusterInformation Information about the cluster to be accessed
     * @return Not null <code> Cluster</code> instance
     * @see #getCluster(ClusterInformation)
     */
    public Cluster getCluster(ClusterInformation clusterInformation) {

        if (clusterInformation == null) {
            throw new DataAccessComponentException("Provided Cluster information instance is null", log);
        }

        String username = clusterInformation.getUsername();
        String clusterName = clusterInformation.getClusterName();

        if (clusterName == null) {
            clusterName = MultitenantUtils.getTenantDomain(username);
        }
        //destroyCluster(clusterName);
        //destroyAllClusters();
        Cluster cluster = clusterRepository.getCluster(username, clusterName);

        if (cluster == null) {

            synchronized (lock) {
                cluster = clusterRepository.getCluster(username, clusterName);
                if (cluster != null) {
                    return cluster;
                }

                Map<String, String> credentials = new HashMap<String, String>();
                credentials.put(USERNAME_KEY, username);
                credentials.put(PASSWORD_KEY, clusterInformation.getPassword());

                CassandraHostConfigurator configurator = clusterInformation.getCassandraHostConfigurator();
                if (configurator == null) {
                    configurator = createCassandraHostConfigurator();
                }
                String clusterUUID = UUID.randomUUID().toString();
                clusterName = clusterName + username + clusterUUID;
                cluster = HFactory.createCluster(clusterName, configurator, credentials);
                clusterRepository.putCluster(username, clusterName, cluster);

            }
        }
        return cluster;
    }

    public Cluster getCluster(ClusterInformation clusterInformation, boolean resetConnection) {

        if (clusterInformation == null) {
            throw new DataAccessComponentException("Provided Cluster information instance is null", log);
        }

        String username = clusterInformation.getUsername();
        String clusterName = clusterInformation.getClusterName();
        //String cluterPassword = clusterInformation.getPassword();
//            if(resetConnection){
//                destroyClustersOfCurrentTenant();
//                //destroyCluster(clusterName);
//                //destroyAllClusters();
//            }
        Cluster cluster = clusterRepository.getCluster(username, clusterName);
        if (cluster == null) {
            synchronized (lock) {
                cluster = clusterRepository.getCluster(username, clusterName);
                if (cluster != null) {
                    return cluster;
                }
                Map<String, String> credentials = new HashMap<String, String>();
                credentials.put(USERNAME_KEY, username);
                credentials.put(PASSWORD_KEY, clusterInformation.getPassword());

                CassandraHostConfigurator configurator = clusterInformation.getCassandraHostConfigurator();
                if (configurator == null) {
                    configurator = createCassandraHostConfigurator();
                }
                String clusterUUID = UUID.randomUUID().toString();
                clusterName = clusterName + username + clusterUUID;
                cluster = HFactory.createCluster(clusterName, configurator, credentials);
                clusterRepository.putCluster(username, clusterName, cluster);
            }
        }
        return cluster;
    }

    /**
     * Returns a Cassandra cluster for the current carbon user
     *
     * @return Not null <code> Cluster</code> instance
     */
    public Cluster getClusterForCurrentUser(String sharedKey) {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String userName = carbonContext.getUsername();

        String tenantDomain = carbonContext.getTenantDomain();
        String superTenantDomainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !superTenantDomainName.equals(tenantDomain)) {
            userName += "@" + tenantDomain;
        }
        ClusterConfiguration configuration =
                DataAccessDependencyHolder.getInstance().getClusterConfiguration();

        ClusterInformation clusterInformation = new ClusterInformation(userName, sharedKey);
        //clusterInformation.setClusterName(configuration.getClusterName());
        //set tenant user name as the cluster name
        clusterInformation.setClusterName(userName);
        clusterInformation.setCassandraHostConfigurator(createCassandraHostConfigurator());

        return getCluster(clusterInformation);
    }

    public Cluster getClusterForCurrentUser(String sharedKey, boolean resetConnection) {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String userName = carbonContext.getUsername();

        String tenantDomain = carbonContext.getTenantDomain();
        String superTenantDomainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomain != null && !superTenantDomainName.equals(tenantDomain)) {
            userName += "@" + tenantDomain;
        }
        ClusterConfiguration configuration =
                DataAccessDependencyHolder.getInstance().getClusterConfiguration();

        ClusterInformation clusterInformation = new ClusterInformation(userName, sharedKey);
        clusterInformation.setClusterName(configuration.getClusterName());
        clusterInformation.setCassandraHostConfigurator(createCassandraHostConfigurator());

        return getCluster(clusterInformation, resetConnection);
    }

    /**
     * Destroy the <code>Cluster</code> having given name and is belonged to the current user
     *
     * @param name the name of the <code>Cluster</code> instance to be destroyed
     */
    public void destroyCluster(String name) {
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        clusterRepository.removeCluster(userName, name);
    }

    /**
     * @see DataAccessService#destroyClustersOfCurrentTenant()
     */
    public void destroyClustersOfCurrentTenant() {
        clusterRepository.removeMyClusters(CarbonContext.getThreadLocalCarbonContext().getUsername());
    }

    /**
     * @param tenantID id of the tenant
     * @see DataAccessService#destroyClustersOfTenant(int)
     */
    public void destroyClustersOfTenant(int tenantID) {
        clusterRepository.removeClusters(String.valueOf(tenantID));
    }

    /**
     * @see DataAccessService#destroyAllClusters()
     */
    public void destroyAllClusters() {
        clusterRepository.removeAllClusters();
    }

    /**
     * Factory method to create a <code>CassandraHostConfigurator</code> using the cassandra configuration
     *
     * @return <code>CassandraHostConfigurator</code> instance
     */
    private CassandraHostConfigurator createCassandraHostConfigurator() {

        ClusterConfiguration configuration =
                DataAccessDependencyHolder.getInstance().getClusterConfiguration();

        String carbonCassandraRPCPort;
        carbonCassandraRPCPort = System.getProperty("cassandra.rpc_port");
        String cassandraHosts;
        int cassandraDefaultPort = 0;

        cassandraHosts = configuration.getNodesString();

        // if configuration is not available on file
        if (cassandraHosts == null || "".equals(cassandraHosts)) {
            cassandraHosts = LOCAL_HOST_NAME + ":" + carbonCassandraRPCPort;
            cassandraDefaultPort = Integer.parseInt(carbonCassandraRPCPort);
        }

        CassandraHostConfigurator configurator = new CassandraHostConfigurator(cassandraHosts);
        configurator.setAutoDiscoverHosts(configuration.isAutoDiscovery());
        configurator.setAutoDiscoveryDelayInSeconds(configuration.getAutoDiscoveryDelay());

        if (cassandraDefaultPort > 0) {
            configurator.setPort(cassandraDefaultPort);
        } else {
            int configurationDefaultPort = configuration.getDefaultPort();
            configurator.setPort(configurationDefaultPort);
        }
        return configurator;
    }
}
