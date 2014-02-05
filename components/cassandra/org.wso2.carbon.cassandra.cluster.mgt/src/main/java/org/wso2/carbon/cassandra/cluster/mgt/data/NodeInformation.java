package org.wso2.carbon.cassandra.cluster.mgt.data;/*
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

public class NodeInformation {
    private String token;
    private boolean gossipState;
    private String load;
    private int generationNo;
    private long uptime;
    private String dataCenter;
    private String rack;
    private int exceptions;
    private HeapMemory heapMemory;
    private CacheProperties rowCacheProperties;
    private CacheProperties keyCacheProperties;
    private boolean thriftState;
    public boolean getThriftState() {
        return thriftState;
    }

    public void setThriftState(boolean thriftState) {
        this.thriftState = thriftState;
    }

    ;

    public CacheProperties getRowCacheProperties() {
        return rowCacheProperties;
    }

    public void setRowCacheProperties(CacheProperties rowCacheProperties) {
        this.rowCacheProperties = rowCacheProperties;
    }

    public CacheProperties getKeyCacheProperties() {
        return keyCacheProperties;
    }

    public void setKeyCacheProperties(CacheProperties keyCacheProperties) {
        this.keyCacheProperties = keyCacheProperties;
    }

    public HeapMemory getHeapMemory() {
        return heapMemory;
    }

    public void setHeapMemory(HeapMemory heapMemory) {
        this.heapMemory = heapMemory;
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
