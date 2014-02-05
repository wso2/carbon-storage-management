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

public class ProxyCacheProperties {
    private long cacheSize;
    private long cacheCapacity;
    private long cacheHits;
    private long cacheRequests;
    private double cacheRecentHitRate;
    private long cacheSavePeriodInSeconds;

    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public long getCacheCapacity() {
        return cacheCapacity;
    }

    public void setCacheCapacity(long cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public long getCacheHits() {
        return cacheHits;
    }

    public void setCacheHits(long cacheHits) {
        this.cacheHits = cacheHits;
    }

    public long getCacheRequests() {
        return cacheRequests;
    }

    public void setCacheRequests(long cacheRequests) {
        this.cacheRequests = cacheRequests;
    }

    public double getCacheRecentHitRate() {
        return cacheRecentHitRate;
    }

    public void setCacheRecentHitRate(double cacheRecentHitRate) {
        this.cacheRecentHitRate = cacheRecentHitRate;
    }

    public long getCacheSavePeriodInSeconds() {
        return cacheSavePeriodInSeconds;
    }

    public void setCacheSavePeriodInSeconds(long cacheSavePeriodInSeconds) {
        this.cacheSavePeriodInSeconds = cacheSavePeriodInSeconds;
    }
}
