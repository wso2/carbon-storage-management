/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.rssmanager.core.dto;

import java.io.Serializable;

/**
 * Class to represent an RSS Server Instance.
 */

public class RSSInstanceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String rssInstanceName;

	private String dbmsType;

	private String instanceType;

	private String serverCategory;

	private String serverURL;

	private String environmentName;

	private String username;

	private String password;

	private String driverClass;

	private boolean isFromConfig;

	private SSHInformationConfigInfo sshInformationConfig;

	private SnapshotConfigInfo snapshotConfig;

	public RSSInstanceInfo() {
	}

	public String getRssInstanceName() {
		return rssInstanceName;
	}

	public void setRssInstanceName(String name) {
		this.rssInstanceName = name;
	}

	public String getDbmsType() {
		return dbmsType;
	}

	public void setDbmsType(String dbmsType) {
		this.dbmsType = dbmsType;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getServerCategory() {
		return serverCategory;
	}

	public void setServerCategory(String serverCategory) {
		this.serverCategory = serverCategory;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

    public SSHInformationConfigInfo getSshInformationConfig() {
        return sshInformationConfig;
    }

    public void setSshInformationConfig(SSHInformationConfigInfo sshInformationConfig) {
        this.sshInformationConfig = sshInformationConfig;
    }

    public SnapshotConfigInfo getSnapshotConfig() {
        return snapshotConfig;
    }

    public void setSnapshotConfig(SnapshotConfigInfo snapshotConfig) {
        this.snapshotConfig = snapshotConfig;
    }

	public boolean isFromConfig() {
		return isFromConfig;
	}

	public void setFromConfig(boolean isFromConfig) {
		this.isFromConfig = isFromConfig;
	}
}
