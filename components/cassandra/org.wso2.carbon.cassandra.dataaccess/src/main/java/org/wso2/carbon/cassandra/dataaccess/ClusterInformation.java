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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encapsulates the information required to access a Cassandra cluster
 * <p/> Username and Password should be provided. Currently, the clusterName also has to be provided.
 */

public class ClusterInformation {

    private static final Log log = LogFactory.getLog(ClusterInformation.class);

    /* The credentials to access a Cassandra cluster*/
    private String username;
    private String password;
    /* The name of the cluster */
    private String clusterName;    // TODO this should be able to be given by the plugin

    /* For configuring hector dataaccess */
    private CassandraHostConfigurator cassandraHostConfigurator;

    public ClusterInformation(String username, String password) {

        if (username == null || "".equals(username)) {
            throw new DataAccessComponentException("Username should be provided to access a Cassandra cluster.", log);
        }

        if (password == null || "".equals(password)) {
            throw new DataAccessComponentException("Password should be provided to access a Cassandra cluster.", log);
        }
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public CassandraHostConfigurator getCassandraHostConfigurator() {
        return cassandraHostConfigurator;
    }

    public void setCassandraHostConfigurator(CassandraHostConfigurator cassandraHostConfigurator) {
        this.cassandraHostConfigurator = cassandraHostConfigurator;
    }
}
