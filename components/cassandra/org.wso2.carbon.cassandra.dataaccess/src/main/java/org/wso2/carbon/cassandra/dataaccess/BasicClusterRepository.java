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
import org.wso2.carbon.context.CarbonContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A cluster repository for a single tenant system
 */
public class BasicClusterRepository implements ClusterRepository {

    private final Map<String, Map<String, Cluster>> clusters = new HashMap<String, Map<String, Cluster>>();

    /**
     * Returns the hector cluster based on the given cluster name
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @return <code>Cluster<code> instance or null if there is no cluster with the given name
     */
    public Cluster getCluster(String owner, String clusterName) {
        Map<String, Cluster> clustersForUser = clusters.get(owner);
        if (clustersForUser != null) {
            return clustersForUser.get(clusterName);
        }
        return null;
    }

    /**
     * Keeps the given cluster in the repository
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @param cluster     <code>Cluster </code> instance
     */
    public void putCluster(String owner, String clusterName, Cluster cluster) {
        Map<String, Cluster> clustersForUser = clusters.get(owner);
        if (clustersForUser == null) {
            clustersForUser = new HashMap<String, Cluster>();
            clusters.put(owner, clustersForUser);
        }
        clustersForUser.put(clusterName, cluster);
    }

    /**
     * Remove the hector cluster instance with the give cluster name and shutdown it
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     */
    public void removeCluster(String owner, String clusterName) {
        Map<String, Cluster> clustersForUser = clusters.remove(owner);
        if (clustersForUser != null) {
            shutdownCluster(clustersForUser.remove(clusterName));
        }
    }

    /**
     * Remove the hector cluster instances created by the given user and shutdown it
     *
     * @param owner Owners of the hector clusters
     */
    public void removeClusters(String owner) {
        Map<String, Cluster> clustersForUser = clusters.remove(owner);
        if (clustersForUser != null) {
            for (Cluster cluster : clustersForUser.values()) {
                shutdownCluster(cluster);
            }
        }
    }

    /**
     * Remove all the clusters created by the current user
     */
    public void removeMyClusters(String owner) {
        removeClusters(CarbonContext.getThreadLocalCarbonContext().getUsername());
    }

    /**
     * remove the all the clusters created in the system
     */
    public void removeAllClusters() {
        for (Map<String, Cluster> clusterMap : clusters.values()) {
            for (Cluster cluster : clusterMap.values()) {
                shutdownCluster(cluster);
            }
        }
        clusters.clear();
    }

    /**
     * Encapsulates the shutdown logic for a hector cluster
     *
     * @param cluster <code>Cluster </code> instance
     */
    private void shutdownCluster(Cluster cluster) {
        if (cluster != null) {
            cluster.getConnectionManager().shutdown();
        }
    }
}

