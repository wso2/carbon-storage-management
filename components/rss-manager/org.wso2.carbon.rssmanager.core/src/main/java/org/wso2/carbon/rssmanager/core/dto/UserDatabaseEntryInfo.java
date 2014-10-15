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
 * Class to represent a user's permissions to a specific database
 */
public class UserDatabaseEntryInfo implements Serializable {

	private static final long serialVersionUID = -6519110782592048084L;

	private String username;

	private String databaseName;

	private String rssInstanceName;

	private MySQLPrivilegeSetInfo privileges;

	private String type;

	public UserDatabaseEntryInfo() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public MySQLPrivilegeSetInfo getPrivileges() {
		return privileges;
	}

	public void setPrivileges(MySQLPrivilegeSetInfo privileges) {
		this.privileges = privileges;
	}

	public String getRssInstanceName() {
		return rssInstanceName;
	}

	public void setRssInstanceName(String rssInstanceName) {
		this.rssInstanceName = rssInstanceName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
