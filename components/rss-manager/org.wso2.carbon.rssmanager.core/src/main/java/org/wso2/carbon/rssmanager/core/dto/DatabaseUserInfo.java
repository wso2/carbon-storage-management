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
 * Class to represent a database user.
 */

public class DatabaseUserInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;

	private String password;

	private String rssInstanceName;

	private String type;

	public DatabaseUserInfo(String name, String password, String rssInstanceName, String type) {
		this.username = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
		this.type = type;
	}

	public DatabaseUserInfo() {
	}

	public String getName() {
		return username;
	}

	public void setName(String name) {
		this.username = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DatabaseUserInfo that = (DatabaseUserInfo) o;
		if (!username.equals(that.username)) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime +result + ((username == null) ? 0 : username.hashCode());
		return result;
	}
}
