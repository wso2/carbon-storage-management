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

import java.util.Set;

/**
 * Class to represent a Database Instance created by an RSS Server.
 */
public class Database {
	private Integer id;
	private String url;
	private String databaseType;
	private String rssInstanceUrl;
	private String name;
	private String type;
	private String rssInstanceName;
	private RSSInstance rssInstance;
	private Integer tenantId;
	private Set<UserDatabaseEntry> userDatabaseEntries;

	public Database(int id, String name, String rssInstanceName, String url, String type) {
		this.id = id;
		this.url = url;
		this.type = type;
		this.name = name;
		this.rssInstanceName = rssInstanceName;
	}

	public Database() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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


	public RSSInstance getRssInstance() {
		return rssInstance;
	}

	public void setRssInstance(RSSInstance rssInstance) {
		this.rssInstance = rssInstance;
	}

	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public Set<UserDatabaseEntry> getUserDatabaseEntries() {
		return userDatabaseEntries;
	}

	public void setUserDatabaseEntries(Set<UserDatabaseEntry> userDatabaseEntries) {
		this.userDatabaseEntries = userDatabaseEntries;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public String getRssInstanceUrl() {
		return rssInstanceUrl;
	}

	public void setRssInstanceUrl(String rssInstanceUrl) {
		this.rssInstanceUrl = rssInstanceUrl;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
}
