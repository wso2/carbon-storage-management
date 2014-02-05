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

/**
 * Represents the hector cluster repository
 */
public interface ClusterRepository {

    /**
     * Locate and return the hector cluster instance for the given cluster name
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @return <code>Cluster </code> instance or null
     */
    Cluster getCluster(String owner, String clusterName);

    /**
     * Register the hector cluster instance with the given cluster name
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     * @param cluster     <code>Cluster </code> instance
     */
    void putCluster(String owner, String clusterName, Cluster cluster);

    /**
     * Remove the hector cluster instance for the given cluster name from the system
     *
     * @param owner       Owners of the hector cluster
     * @param clusterName name of the hector cluster
     */
    void removeCluster(String owner, String clusterName);

    /**
     * Remove all the hector cluster instances belonged to the given user
     *
     * @param owner Owners of the hector clusters
     */
    void removeClusters(String owner);

    /**
     * Remove all the hector cluster instances belonged to the current user
     */
    void removeMyClusters(String owner);

    /**
     * Remove all the hector cluster instances in the system
     */
    void removeAllClusters();
}
