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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * A factory method to create a <code>ClusterConfiguration</code> instance from the XML configuration
 */
public class ClusterConfigurationFactory {

    private static Log log = LogFactory.getLog(ClusterConfigurationFactory.class);
    private static final String EXTERNAL_CASSANDRA_ATTRIBUTE = "externalCassandra";

    /**
     * Create an instance of <code>ClusterConfiguration</code> from the XML configuration
     *
     * @param severElement XML representing the server's configuration
     * @return Not null <code>ClusterConfiguration</code> instance
     */
    public static ClusterConfiguration create(OMElement severElement) {

        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();

        OMElement deployment = severElement.getFirstChildWithName(new QName("Deployment"));
        if (deployment != null) {
            String mode = deployment.getText();
            if (mode != null && !"".equals(mode.trim())) {
                clusterConfiguration.setDeploymentMode(mode.trim());
            }
        }

        OMElement cluster = severElement.getFirstChildWithName(new QName("Cluster"));
        if (cluster != null) {
            OMElement nameElement = cluster.getFirstChildWithName(new QName("Name"));
            if (nameElement != null) {
                String name = nameElement.getText();
                if (name == null || "".equals(name.trim())) {
                    throw new DataAccessComponentException("Name is null or empty", log);
                }
                clusterConfiguration.setClusterName(name.trim());
            }

            OMElement portElement = cluster.getFirstChildWithName(new QName("DefaultPort"));
            if (portElement != null) {
                String port = portElement.getText();
                if (port != null && !"".equals(port.trim())) {
                    try {
                        clusterConfiguration.setDefaultPort(Integer.parseInt(port.trim()));
                    } catch (NumberFormatException e) {
                        throw new DataAccessComponentException("An invalid number for defaultport : " + port, log);
                    }
                }
            }

            /**
             * If externalCassandra attribute is not set in the hector configuration, it will assign default value as true
             *
             */
            OMElement nodesElement = cluster.getFirstChildWithName(new QName("Nodes"));
            if (nodesElement != null) {
                Boolean isExternalCassandra = false;
                String externalCassandra = "";
	            if (nodesElement.getAttribute(new QName(EXTERNAL_CASSANDRA_ATTRIBUTE)) != null) {
		            externalCassandra = nodesElement.getAttributeValue(new QName(EXTERNAL_CASSANDRA_ATTRIBUTE));
	            }
	            if (externalCassandra != null && !"".equals(externalCassandra)) {
		            isExternalCassandra = Boolean.parseBoolean(externalCassandra.trim());
	            } else {
		            isExternalCassandra = true;
	            }
                String nodesString = nodesElement.getText();
                if (nodesString != null && !"".endsWith(nodesString.trim())) {
                    if(isExternalCassandra) {
                        clusterConfiguration.setNodesString(nodesString.trim());
                        String nodes[] = nodesString.split(",");
                        for (String node : nodes) {
                            clusterConfiguration.addNode(node);
                        }
                    } else {
                        String host=nodesString.split(":")[0];
                        int port=Integer.parseInt(nodesString.split(":")[1].trim());
                        port=port+getPortOffset();
                        String newNodeString=host+":"+port;
                        clusterConfiguration.setNodesString(newNodeString.trim());
                        clusterConfiguration.addNode(newNodeString.trim());
                    }
                }
            }

            OMElement autoDiscoveryElement = cluster.getFirstChildWithName(new QName("AutoDiscovery"));
            if (autoDiscoveryElement != null) {
                String disable = autoDiscoveryElement.getAttributeValue(new QName("disable"));
                if (disable != null && !"".equals(disable.trim())) {
                    clusterConfiguration.setAutoDiscovery(!Boolean.parseBoolean(disable.trim()));
                }
                String delay = autoDiscoveryElement.getAttributeValue(new QName("delay"));
                if (delay != null && !"".equals(delay.trim())) {
                    int delayAsInt = -1;
                    try {
                        delayAsInt = Integer.parseInt(delay.trim());
                    } catch (NumberFormatException ignored) {
                    }
                    if (delayAsInt > 0) {
                        clusterConfiguration.setAutoDiscoveryDelay(delayAsInt);
                    }
                }
            }
        }

        return clusterConfiguration;
    }

    /**
     * Read Carbon Server port offset
     * @return offset number
     */
    private static int getPortOffset() {
        String portOffset = System.getProperty("portOffset",
                CarbonUtils.getServerConfiguration().getFirstProperty("Ports.Offset"));
        return Integer.parseInt(portOffset);
    }
}
