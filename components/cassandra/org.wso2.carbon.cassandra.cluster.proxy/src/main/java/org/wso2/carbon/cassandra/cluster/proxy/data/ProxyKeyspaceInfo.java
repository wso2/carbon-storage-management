package org.wso2.carbon.cassandra.cluster.proxy.data;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

public class ProxyKeyspaceInfo {
    private String keyspaceName;
    private long tableReadCount;
    private long tableWriteCount;
    private int tablePendingTasks;
    private double tableTotalReadTime;
    private double tableTotalWriteTime;
    private double tableReadLatency;
    private double tableWriteLatency;

    private ProxyColumnFamilyInformation[] proxyColumnFamilyInformations;

    public double getTableReadLatency() {
        return tableReadLatency;
    }

    public void setTableReadLatency(double tableReadLatency) {
        this.tableReadLatency = tableReadLatency;
    }

    public double getTableWriteLatency() {
        return tableWriteLatency;
    }

    public void setTableWriteLatency(double tableWriteLatency) {
        this.tableWriteLatency = tableWriteLatency;
    }

    public long getTableReadCount() {
        return tableReadCount;
    }

    public void setTableReadCount(long tableReadCount) {
        this.tableReadCount = tableReadCount;
    }

    public long getTableWriteCount() {
        return tableWriteCount;
    }

    public void setTableWriteCount(long tableWriteCount) {
        this.tableWriteCount = tableWriteCount;
    }

    public int getTablePendingTasks() {
        return tablePendingTasks;
    }

    public void setTablePendingTasks(int tablePendingTasks) {
        this.tablePendingTasks = tablePendingTasks;
    }

    public double getTableTotalReadTime() {
        return tableTotalReadTime;
    }

    public void setTableTotalReadTime(double tableTotalReadTime) {
        this.tableTotalReadTime = tableTotalReadTime;
    }

    public double getTableTotalWriteTime() {
        return tableTotalWriteTime;
    }

    public void setTableTotalWriteTime(double tableTotalWriteTime) {
        this.tableTotalWriteTime = tableTotalWriteTime;
    }

    public ProxyColumnFamilyInformation[] getProxyColumnFamilyInformations() {
        return proxyColumnFamilyInformations;
    }

    public void setProxyColumnFamilyInformations(ProxyColumnFamilyInformation[] proxyColumnFamilyInformations) {
        this.proxyColumnFamilyInformations = proxyColumnFamilyInformations;
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }



}
