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

package org.wso2.carbon.rssmanager.core.dto.restricted;

import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;

import java.util.List;
import java.util.Set;


/**
 * Class to represent a database user.
 */
public class DatabaseUser {

	private static final long serialVersionUID = 1L;

	private Long version;
	private Integer id;
	private String username;
	private String password;
	private String rssInstanceName;
	private String type;
	private Integer tenantId;
	private Integer environmentId;
	private List<UserDatabaseEntry> userDatabaseEntries;
	private Set<RSSInstance> instances;

	public DatabaseUser(String name, String password, String rssInstanceName, String type) {
		this.username = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
		this.type = type;
	}

	public DatabaseUser(int id, String name, String password, String rssInstanceName, String type) {
		this.id = id;
		this.username = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
		this.type = type;
	}

	public DatabaseUser() {
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<UserDatabaseEntry> getUserDatabaseEntries() {
		return userDatabaseEntries;
	}

	public void setUserDatabaseEntries(List<UserDatabaseEntry> userDatabaseEntries) {
		this.userDatabaseEntries = userDatabaseEntries;
	}

	public Set<RSSInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<RSSInstance> instances) {
		this.instances = instances;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}
}
