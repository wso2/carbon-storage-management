/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dto.common;

import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;

/**
 * Class to represent a user's permissions to a specific database.
 */
public class UserDatabaseEntry {
	private Integer id;
	private Integer userId;
	private DatabaseUser databaseUser;
	private Integer databaseId;
	private Database database;
	private UserDatabasePrivilege userPrivileges;
	private String username;
	private String databaseName;
	private int rssInstanceId;
	private String rssInstanceName;
	private DatabasePrivilegeSet privileges;
	private String type;

	public UserDatabaseEntry() {
	}

	public UserDatabaseEntry(int id, String username, String databaseName, String rssInstanceName) {
		this.id = id;
		this.username = username;
		this.databaseName = databaseName;
		this.rssInstanceName = rssInstanceName;
	}

	public UserDatabaseEntry(int userId, String username, int databaseId, String databaseName,
	                         int rssInstanceId, String rssInstanceName) {
		this.userId = userId;
		this.username = username;
		this.databaseId = databaseId;
		this.databaseName = databaseName;
		this.rssInstanceId = rssInstanceId;
		this.rssInstanceName = rssInstanceName;
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

	public DatabasePrivilegeSet getPrivileges() {
		return privileges;
	}

	public void setPrivileges(DatabasePrivilegeSet privileges) {
		this.privileges = privileges;
	}

	public String getRssInstanceName() {
		return rssInstanceName;
	}

	public void setRssInstanceName(String rssInstanceName) {
		this.rssInstanceName = rssInstanceName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}

	public int getRssInstanceId() {
		return rssInstanceId;
	}

	public void setRssInstanceId(int rssInstanceId) {
		this.rssInstanceId = rssInstanceId;
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DatabaseUser getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(DatabaseUser databaseUser) {
		this.databaseUser = databaseUser;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserDatabasePrivilege getUserPrivileges() {
		return userPrivileges;
	}

	public void setUserPrivileges(UserDatabasePrivilege userPrivileges) {
		this.userPrivileges = userPrivileges;
	}
}
