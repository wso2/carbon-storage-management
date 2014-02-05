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

import me.prettyprint.cassandra.service.CassandraHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cassandra Server component's configuration
 */
public class ClusterConfiguration {

    private static final Log log = LogFactory.getLog(ClusterConfiguration.class);

    private static final String DEFAULT_CLUSTER_NAME = "Test Cluster";
    private static final int DEFAULT_AUTO_DISCOVERY_INTERVAL = 30;
    private static final String DEPLOYMENT_EMBEDDED = "embedded";
    private static final String DEPLOYMENT_EXTERNAL = "external";

    private String authenticationServer = DEFAULT_CLUSTER_NAME;

    /* Keeps the user vs his or her nodes, each node as host:port*/
    private final Map<String, String> userNodes = new HashMap<String, String>();  //TODO move

    /* A list of cassandra nodes*/
    private final List<String> nodes = new ArrayList<String>();

    private String nodesString;

    /* The name of the cluster*/
    private String clusterName = DEFAULT_CLUSTER_NAME;

    private boolean autoDiscovery = true;
    private int autoDiscoveryDelay = DEFAULT_AUTO_DISCOVERY_INTERVAL;
    private String deploymentMode = DEPLOYMENT_EMBEDDED;
    private int defaultPort = CassandraHost.DEFAULT_PORT;

    public String getAuthenticationServer() {
        return authenticationServer;
    }

    public void setAuthenticationServer(String authenticationServer) {
        this.authenticationServer = authenticationServer;
    }

    public void addNode(String nodeURL) {
        validateNodeLocation(nodeURL);
        nodes.add(nodeURL);
    }

    public List<String> getNodes() {
        return nodes;
    }

    /**
     * Assign a user to a node location
     *
     * @param username     the name of the user
     * @param nodeLocation the location of a node to keep data of the user
     */
    public void addNodeLocation(String username, String nodeLocation) {
        validateUsername(username);
        validateNodeLocation(nodeLocation);
        userNodes.put(username.trim(), nodeLocation.trim());
    }

    /**
     * Returns the location of a node to keep data of the user
     *
     * @param username the name of the user
     * @return nodeLocation  the location of a node to keep data of the user
     */
    public String getNodeLocation(String username) {
        validateUsername(username);
        return userNodes.get(username.trim());
    }

    private void validateUsername(String username) throws DataAccessComponentException {
        if (username == null || "".equals(username.trim())) {
            throw new DataAccessComponentException("Username is null or empty", log);
        }
    }

    private void validateNodeLocation(String nodeLocation) throws DataAccessComponentException {
        if (nodeLocation == null || "".equals(nodeLocation.trim())) {
            throw new DataAccessComponentException("Node location is null or empty", log);
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodesString() {
        return nodesString;
    }

    public void setNodesString(String nodesString) {
        this.nodesString = nodesString;
    }

    public boolean isAutoDiscovery() {
        return autoDiscovery;
    }

    public void setAutoDiscovery(boolean autoDiscovery) {
        this.autoDiscovery = autoDiscovery;
    }

    public int getAutoDiscoveryDelay() {
        return autoDiscoveryDelay;
    }

    public void setAutoDiscoveryDelay(int autoDiscoveryDelay) {
        this.autoDiscoveryDelay = autoDiscoveryDelay;
    }

    public void setDeploymentMode(String deploymentMode) {
        this.deploymentMode = deploymentMode;
    }

    public boolean isDeploymentModeEmbedded() {
        return DEPLOYMENT_EMBEDDED.equals(deploymentMode);
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }
}
