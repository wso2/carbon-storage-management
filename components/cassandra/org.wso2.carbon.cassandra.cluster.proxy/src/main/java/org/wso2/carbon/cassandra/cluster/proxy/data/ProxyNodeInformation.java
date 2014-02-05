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

public class ProxyNodeInformation {
    private String token;
    private boolean gossipState;
    private String load;
    private int generationNo;
    private long uptime;
    private String dataCenter;
    private String rack;
    private int exceptions;
    private ProxyHeapMemory proxyHeapMemory;
    private ProxyCacheProperties rowProxyCacheProperties;
    private ProxyCacheProperties keyProxyCacheProperties;

    public ProxyCacheProperties getRowProxyCacheProperties() {
        return rowProxyCacheProperties;
    }

    public void setRowProxyCacheProperties(ProxyCacheProperties rowProxyCacheProperties) {
        this.rowProxyCacheProperties = rowProxyCacheProperties;
    }

    public ProxyCacheProperties getKeyProxyCacheProperties() {
        return keyProxyCacheProperties;
    }

    public void setKeyProxyCacheProperties(ProxyCacheProperties keyProxyCacheProperties) {
        this.keyProxyCacheProperties = keyProxyCacheProperties;
    }

    public ProxyHeapMemory getProxyHeapMemory() {
        return proxyHeapMemory;
    }

    public void setProxyHeapMemory(ProxyHeapMemory proxyHeapMemory) {
        this.proxyHeapMemory = proxyHeapMemory;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isGossipState() {
        return gossipState;
    }

    public void setGossipState(boolean gossipState) {
        this.gossipState = gossipState;
    }

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

    public int getGenerationNo() {
        return generationNo;
    }

    public void setGenerationNo(int generationNo) {
        this.generationNo = generationNo;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public int getExceptions() {
        return exceptions;
    }

    public void setExceptions(int exceptions) {
        this.exceptions = exceptions;
    }
}
