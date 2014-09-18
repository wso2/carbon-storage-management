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

/**
 * A bean representing meta-data about a FC
 * TODO use constants for default values
 */
public class ColumnFamilyInformation {

    private String environmentName;
    private String clusterName;
    private String keyspace;
    private String name;
    private String type = "Standard";
    private String comment;
    private double rowCacheSize;

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

    private int rowCacheSavePeriodInSeconds = 7;
    private double keyCacheSize;
    private double readRepairChance;
    private int gcGraceSeconds = 864000;
    private int maxCompactionThreshold = 32;
    private int minCompactionThreshold = 4;
    private String comparatorType = "BytesType";
    private String subComparatorType = "BytesType";
    private int id;
    private ColumnInformation[] columns;  // This is because axis2 does not support List
    private String defaultValidationClass;
    private String keyValidationClass = "BytesType";

    public ColumnFamilyInformation() {
        this.keyspace = null;
        this.name = null;
    }

    public ColumnFamilyInformation(String keyspace, String name) {
        this.keyspace = keyspace;
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRowCacheSize(double rowCacheSize) {
        this.rowCacheSize = rowCacheSize;
    }

    public void setRowCacheSavePeriodInSeconds(int rowCacheSavePeriodInSeconds) {
        this.rowCacheSavePeriodInSeconds = rowCacheSavePeriodInSeconds;
    }

    public void setKeyCacheSize(double keyCacheSize) {
        this.keyCacheSize = keyCacheSize;
    }

    public void setReadRepairChance(double readRepairChance) {
        this.readRepairChance = readRepairChance;
    }

    public void setGcGraceSeconds(int gcGraceSeconds) {
        this.gcGraceSeconds = gcGraceSeconds;
    }

    public void setMaxCompactionThreshold(int maxCompactionThreshold) {
        this.maxCompactionThreshold = maxCompactionThreshold;
    }

    public void setMinCompactionThreshold(int minCompactionThreshold) {
        this.minCompactionThreshold = minCompactionThreshold;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public double getRowCacheSize() {
        return rowCacheSize;
    }

    public int getRowCacheSavePeriodInSeconds() {
        return rowCacheSavePeriodInSeconds;
    }

    public double getKeyCacheSize() {
        return keyCacheSize;
    }

    public double getReadRepairChance() {
        return readRepairChance;
    }

    public int getGcGraceSeconds() {
        return gcGraceSeconds;
    }

    public int getMaxCompactionThreshold() {
        return maxCompactionThreshold;
    }

    public int getMinCompactionThreshold() {
        return minCompactionThreshold;
    }

    public String getComparatorType() {
        return comparatorType;
    }

    public void setComparatorType(String comparatorType) {
        this.comparatorType = comparatorType;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubComparatorType(String subComparatorType) {
        this.subComparatorType = subComparatorType;
    }

    public String getSubComparatorType() {
        return subComparatorType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ColumnInformation[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnInformation[] columns) {
        this.columns = columns;
    }

    public String getDefaultValidationClass() {
        return defaultValidationClass;
    }

    public void setDefaultValidationClass(String defaultValidationClass) {
        this.defaultValidationClass = defaultValidationClass;
    }

    public String getKeyValidationClass() {
        return keyValidationClass;
    }

    public void setKeyValidationClass(String keyValidationClass) {
        this.keyValidationClass = keyValidationClass;
    }
}
