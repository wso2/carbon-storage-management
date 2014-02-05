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

public class ThreadPoolProperties {
    private String threadPoolPropertyName;
    private int active;
    private long pending;
    private long completed;
    private int blocked;
    private int allTimeBlocked;

    public String getThreadPoolPropertyName() {
        return threadPoolPropertyName;
    }

    public void setThreadPoolPropertyName(String threadPoolPropertyName) {
        this.threadPoolPropertyName = threadPoolPropertyName;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getAllTimeBlocked() {
        return allTimeBlocked;
    }

    public void setAllTimeBlocked(int allTimeBlocked) {
        this.allTimeBlocked = allTimeBlocked;
    }
}
