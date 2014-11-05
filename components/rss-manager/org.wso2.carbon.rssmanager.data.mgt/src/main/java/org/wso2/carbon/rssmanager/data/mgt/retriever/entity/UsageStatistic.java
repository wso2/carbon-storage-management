/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.data.mgt.retriever.entity;

import java.io.Serializable;

public class UsageStatistic implements Serializable{

	
    private static final long serialVersionUID = 1L;
    
	private String hostAddress;
	private String hostName;
	private String readCount;
	private String readLatency;
	private String writeCount;
	private String writeLatency;
	private String abortedClients;
	private String abortedConnections;
	private String bytesReceived;
	private String bytesSent;
	private String connections;
	private String createdTmpDiskTables;
	private String createdTmpTables;
	private String createdTmpFiles;
	private String openTables;
	private String openFiles;
	private String openStreams;
	private String openedTables;
	private String questions;
	private String tableLocksImmediate;
	private String tableLocksWaited;
	private String threadsCached;
	private String threadsCreated;
	private String threadsConnected;
	private String threadsRunning;
	private String uptime;
    private String diskUsage;
    private String freeSpace;
    private String databaseName;
    private String tenantId;
    private boolean valid;
    

    public String getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(String freeSpace) {
        this.freeSpace = freeSpace;
    }

    

    public String getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(String diskUsage) {
        this.diskUsage = diskUsage;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHostAddress() {
		return hostAddress;
	}
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getReadCount() {
		return readCount;
	}
	public void setReadCount(String readCount) {
		this.readCount = readCount;
	}
	public String getReadLatency() {
		return readLatency;
	}
	public void setReadLatency(String readLatency) {
		this.readLatency = readLatency;
	}
	public String getWriteCount() {
		return writeCount;
	}
	public void setWriteCount(String writeCount) {
		this.writeCount = writeCount;
	}
	public String getWriteLatency() {
		return writeLatency;
	}
	public void setWriteLatency(String writeLatency) {
		this.writeLatency = writeLatency;
	}
	public String getAbortedClients() {
		return abortedClients;
	}
	public void setAbortedClients(String abortedClients) {
		this.abortedClients = abortedClients;
	}
	public String getAbortedConnections() {
		return abortedConnections;
	}
	public void setAbortedConnections(String abortedConnections) {
		this.abortedConnections = abortedConnections;
	}
	public String getBytesReceived() {
		return bytesReceived;
	}
	public void setBytesReceived(String bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
	public String getBytesSent() {
		return bytesSent;
	}
	public void setBytesSent(String bytesSent) {
		this.bytesSent = bytesSent;
	}
	public String getConnections() {
		return connections;
	}
	public void setConnections(String connections) {
		this.connections = connections;
	}
	public String getCreatedTmpDiskTables() {
		return createdTmpDiskTables;
	}
	public void setCreatedTmpDiskTables(String createdTmpDiskTables) {
		this.createdTmpDiskTables = createdTmpDiskTables;
	}
	public String getCreatedTmpTables() {
		return createdTmpTables;
	}
	public void setCreatedTmpTables(String createdTmpTables) {
		this.createdTmpTables = createdTmpTables;
	}
	public String getCreatedTmpFiles() {
		return createdTmpFiles;
	}
	public void setCreatedTmpFiles(String createdTmpFiles) {
		this.createdTmpFiles = createdTmpFiles;
	}
	public String getOpenTables() {
		return openTables;
	}
	public void setOpenTables(String openTables) {
		this.openTables = openTables;
	}
	public String getOpenFiles() {
		return openFiles;
	}
	public void setOpenFiles(String openFiles) {
		this.openFiles = openFiles;
	}
	public String getOpenStreams() {
		return openStreams;
	}
	public void setOpenStreams(String openStreams) {
		this.openStreams = openStreams;
	}
	public String getOpenedTables() {
		return openedTables;
	}
	public void setOpenedTables(String openedTables) {
		this.openedTables = openedTables;
	}
	public String getQuestions() {
		return questions;
	}
	public void setQuestions(String questions) {
		this.questions = questions;
	}
	public String getTableLocksImmediate() {
		return tableLocksImmediate;
	}
	public void setTableLocksImmediate(String tableLocksImmediate) {
		this.tableLocksImmediate = tableLocksImmediate;
	}
	public String getTableLocksWaited() {
		return tableLocksWaited;
	}
	public void setTableLocksWaited(String tableLocksWaited) {
		this.tableLocksWaited = tableLocksWaited;
	}
	public String getThreadsCached() {
		return threadsCached;
	}
	public void setThreadsCached(String threadsCached) {
		this.threadsCached = threadsCached;
	}
	public String getThreadsCreated() {
		return threadsCreated;
	}
	public void setThreadsCreated(String threadsCreated) {
		this.threadsCreated = threadsCreated;
	}
	public String getThreadsConnected() {
		return threadsConnected;
	}
	public void setThreadsConnected(String threadsConnected) {
		this.threadsConnected = threadsConnected;
	}
	public String getThreadsRunning() {
		return threadsRunning;
	}
	public void setThreadsRunning(String threadsRunning) {
		this.threadsRunning = threadsRunning;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	
	

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
    public String toString() {
	    return "UsageStatistic [hostAddress=" + hostAddress + ", hostName=" + hostName +
	           ", readCount=" + readCount + ", readLatency=" + readLatency + ", writeCount=" +
	           writeCount + ", writeLatency=" + writeLatency + ", abortedClients=" +
	           abortedClients + ", abortedConnections=" + abortedConnections + ", bytesReceived=" +
	           bytesReceived + ", bytesSent=" + bytesSent + ", connections=" + connections +
	           ", createdTmpDiskTables=" + createdTmpDiskTables + ", createdTmpTables=" +
	           createdTmpTables + ", createdTmpFiles=" + createdTmpFiles + ", openTables=" +
	           openTables + ", openFiles=" + openFiles + ", openStreams=" + openStreams +
	           ", openedTables=" + openedTables + ", questions=" + questions +
	           ", tableLocksImmediate=" + tableLocksImmediate + ", tableLocksWaited=" +
	           tableLocksWaited + ", threadsCached=" + threadsCached + ", threadsCreated=" +
	           threadsCreated + ", threadsConnected=" + threadsConnected + ", threadsRunning=" +
	           threadsRunning + ", uptime=" + uptime + ", diskUsage=" + diskUsage + ", freeSpace=" +
	           freeSpace + ", databaseName=" + databaseName + ", tenantId=" + tenantId +
	           ", valid=" + valid + "]";
    }


	
	
	
	
	
}
