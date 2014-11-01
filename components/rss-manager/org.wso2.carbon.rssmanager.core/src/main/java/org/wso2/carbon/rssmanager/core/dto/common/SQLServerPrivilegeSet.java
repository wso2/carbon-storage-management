/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Class for hold SQLSERVER privilege set
 */
public class SQLServerPrivilegeSet extends DatabasePrivilegeSet {
	private Integer id;
	private UserDatabaseEntry userDatabaseEntry;
	private String grantPriv = "N";
	private String referencesPriv = "N";
	private String createTmpTablePriv = "N";
	private String lockTablesPriv = "N";
	private String executePriv = "N";
	private String createViewPriv = "N";
	private String showViewPriv = "N";
	private String createRoutinePriv = "N";
	private String alterRoutinePriv = "N";
	private String triggerPriv = "N";
	private String eventPriv = "N";

	public String getGrantPriv() {
		return grantPriv;
	}

	public void setGrantPriv(String grantPriv) {
		this.grantPriv = grantPriv;
	}

	public String getReferencesPriv() {
		return referencesPriv;
	}

	public void setReferencesPriv(String referencesPriv) {
		this.referencesPriv = referencesPriv;
	}

	public String getCreateTmpTablePriv() {
		return createTmpTablePriv;
	}

	public void setCreateTmpTablePriv(String createTmpTablePriv) {
		this.createTmpTablePriv = createTmpTablePriv;
	}

	public String getLockTablesPriv() {
		return lockTablesPriv;
	}

	public void setLockTablesPriv(String lockTablesPriv) {
		this.lockTablesPriv = lockTablesPriv;
	}

	public String getExecutePriv() {
		return executePriv;
	}

	public void setExecutePriv(String executePriv) {
		this.executePriv = executePriv;
	}

	public String getCreateViewPriv() {
		return createViewPriv;
	}

	public void setCreateViewPriv(String createViewPriv) {
		this.createViewPriv = createViewPriv;
	}

	public String getShowViewPriv() {
		return showViewPriv;
	}

	public void setShowViewPriv(String showViewPriv) {
		this.showViewPriv = showViewPriv;
	}

	public String getCreateRoutinePriv() {
		return createRoutinePriv;
	}

	public void setCreateRoutinePriv(String createRoutinePriv) {
		this.createRoutinePriv = createRoutinePriv;
	}

	public String getAlterRoutinePriv() {
		return alterRoutinePriv;
	}

	public void setAlterRoutinePriv(String alterRoutinePriv) {
		this.alterRoutinePriv = alterRoutinePriv;
	}

	public String getTriggerPriv() {
		return triggerPriv;
	}

	public void setTriggerPriv(String triggerPriv) {
		this.triggerPriv = triggerPriv;
	}

	public String getEventPriv() {
		return eventPriv;
	}

	public void setEventPriv(String eventPriv) {
		this.eventPriv = eventPriv;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserDatabaseEntry getUserDatabaseEntry() {
		return userDatabaseEntry;
	}

	public void setUserDatabaseEntry(UserDatabaseEntry userDatabaseEntry) {
		this.userDatabaseEntry = userDatabaseEntry;
	}
}
