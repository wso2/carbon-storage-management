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
package org.wso2.carbon.cassandra.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A bean representing a keyspace meta-data
 */
public class KeyspaceInformation {

    private static final Log log = LogFactory.getLog(KeyspaceInformation.class);
    private String name;
    private String strategyClass = "org.apache.cassandra.locator.SimpleStrategy";
    private int replicationFactor = 1;
    private ColumnFamilyInformation[] columnFamilies;
    private String[] strategyOptions = new String[0];    //key and value are separated by an underscore
    private String environmentName;
    private String clusterName;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public KeyspaceInformation(String name) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("The keyspace name is empty or null");
        }
        this.name = name.trim();
    }

    public KeyspaceInformation() {
    }

    public String getName() {
        return name;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        if (replicationFactor > 0) {
            this.replicationFactor = replicationFactor;
        }
    }

    public String getStrategyClass() {
        return strategyClass;
    }

    public void setStrategyClass(String strategyClass) {
        if (strategyClass != null && !"".equals(strategyClass.trim())) {
            this.strategyClass = strategyClass.trim();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnFamilyInformation[] getColumnFamilies() {
        return columnFamilies;
    }

    public void setColumnFamilies(ColumnFamilyInformation[] columnFamilies) {
        this.columnFamilies = columnFamilies;
    }

    public String[] getStrategyOptions() {
        return strategyOptions;
    }

    public void setStrategyOptions(String[] strategyOptions) {
        this.strategyOptions = strategyOptions;
    }
}
