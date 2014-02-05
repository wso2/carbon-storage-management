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

import me.prettyprint.hector.api.Cluster;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-tenant aware <code>ClusterRepository</code> implementation
 */
public class MultitenantClusterRepository implements ClusterRepository {

    private Map<String, BasicClusterRepository> clusters = new ConcurrentHashMap<String, BasicClusterRepository>();

    /**
     * Get the <code>ClusterRepository</code> of the current tenant and retrieve the hector cluster with the given name
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @return <code>Cluster </code> instance or null
     */
    public Cluster getCluster(String owner, String clusterName) {
        BasicClusterRepository clusterRepository = getClusterRepository(owner);
        if (clusterRepository != null) {
            return clusterRepository.getCluster(owner, clusterName);
        }
        return null;
    }

    /**
     * Get the <code>ClusterRepository</code> of the current tenant and put the hector cluster with the given mame
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @param cluster     <code>Cluster </code> instance
     */
    public void putCluster(String owner, String clusterName, Cluster cluster) {
        BasicClusterRepository clusterRepository = getClusterRepository(owner);
        if (clusterRepository == null) {
            clusterRepository = new BasicClusterRepository();
            clusters.put(owner, clusterRepository);
        }
        clusterRepository.putCluster(owner, clusterName, cluster);
    }

    /**
     * Get the <code>ClusterRepository</code> of the current tenant and remove the hector cluster with the given name from it
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     */
    public void removeCluster(String owner, String clusterName) {
        BasicClusterRepository clusterRepository = getClusterRepository(owner);
        if (clusterRepository != null) {
            clusterRepository.removeCluster(owner, clusterName);
        }
    }

    /**
     * Remove all the created by the users of the given tenant
     *
     * @param owner Owners of the hector clusters
     */
    public void removeClusters(String owner) {
//        int tenantID = Integer.parseInt(owner);
        String tenantDomain = MultitenantUtils.getTenantDomain(owner);
        BasicClusterRepository clusterRepository = clusters.remove(tenantDomain);
        if (clusterRepository != null) {
            clusterRepository.removeAllClusters();
        }
    }

    /**
     * Remove all the created by the users of the current tenant
     * @param owner owner of the cluster repo
     */
    public void removeMyClusters(String owner) {
        BasicClusterRepository clusterRepository = removeClusterRepository(owner);
        if (clusterRepository != null) {
            //clusterRepository.removeAllClusters();
            clusterRepository.removeMyClusters(owner);
        }
    }

    /**
     * Remove all the clusters in the system
     */
    public void removeAllClusters() {
        for (BasicClusterRepository clusterRepository : clusters.values()) {
            clusterRepository.removeAllClusters();
        }
        clusters.clear();
    }

    /**
     * A helper method to get ClusterRepository of the current tenant
     *
     * @param owner owner of the cluster repo
     * @return <code>ClusterRepository</code>
     */
    private BasicClusterRepository getClusterRepository(String owner) {
        String tenantDomain = MultitenantUtils.getTenantDomain(owner);
//        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return clusters.get(tenantDomain);
    }

    /**
     * A helper method to remove ClusterRepository of the current tenant
     *
     * @param owner owner of the cluster repo
     * @return <code>ClusterRepository</code>
     */
    private BasicClusterRepository removeClusterRepository(String owner) {
        String tenantDomain = MultitenantUtils.getTenantDomain(owner);

//        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return clusters.remove(tenantDomain);
    }
}
