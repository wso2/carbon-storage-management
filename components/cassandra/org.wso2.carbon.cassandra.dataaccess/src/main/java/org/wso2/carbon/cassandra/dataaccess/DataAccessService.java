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
import me.prettyprint.hector.api.Keyspace;

/**
 * To provides the access to the underlying data store. Currently,this is coupled to the hector Cassandra dataaccess
 */
public interface DataAccessService {

    /**
     * Returns a connector for a Cassandra cluster. Hector API can be used for further operations
     *
     * @param clusterInformation Information about the cluster to be accessed
     * @return <code>Cluster</code> for accessing the required Cassandra cluster
     */
    Cluster getCluster(ClusterInformation clusterInformation);

    /**
     * Returns a connector for a Cassandra Cluster after resetting the previous connection.
     * @param resetConnection
     * @return
     */

    Cluster getCluster(ClusterInformation clusterInformation,boolean resetConnection);

    /**
     * Returns the cluster assigned by the carbon for the current user
     *
     * @return code>Cluster</code> for accessing a specific user's Cassandra cluster
     */
    Cluster getClusterForCurrentUser(String sharedKey);

    /**
     *
     * @param sharedKey
     * @param resetConnection
     * @return
     */

     Cluster getClusterForCurrentUser(String sharedKey, boolean resetConnection);

    /**
     * Shutdown and remove the reference to the <code>Cluster</code> instance corresponded to the cluster given name
     *
     * @param name the name of the <code>Cluster</code> instance to be destroyed
     */
    void destroyCluster(String name);

    /**
     * Shutdown and remove the references to the <code>Cluster</code> instances belonged to the current tenant
     */
    void destroyClustersOfCurrentTenant();

    /**
     * Shutdown and remove the references to the <code>Cluster</code> instances belonged to a given tenant
     *
     * @param tenantID id of the tenant
     */
    void destroyClustersOfTenant(int tenantID);

    /**
     * Shutdown and remove the references to the <code>Cluster</code> instances in the system
     */
    void destroyAllClusters();

}
