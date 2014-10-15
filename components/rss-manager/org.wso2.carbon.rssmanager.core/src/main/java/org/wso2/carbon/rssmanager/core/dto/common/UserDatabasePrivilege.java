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
package org.wso2.carbon.rssmanager.core.dto.common;

/**
 * User database data holder class
 */
public class UserDatabasePrivilege {
	private Integer id;
	private UserDatabaseEntry userDatabaseEntry;
	private String selectPriv = "N";
	private String insertPriv = "N";
	private String updatePriv = "N";
	private String deletePriv = "N";
	private String createPriv = "N";
	private String dropPriv = "N";
	private String indexPriv = "N";
	private String alterPriv = "N";
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

	public String getSelectPriv() {
		return selectPriv;
	}

	public void setSelectPriv(String selectPriv) {
		this.selectPriv = selectPriv;
	}

	public String getInsertPriv() {
		return insertPriv;
	}

	public void setInsertPriv(String insertPriv) {
		this.insertPriv = insertPriv;
	}

	public String getUpdatePriv() {
		return updatePriv;
	}

	public void setUpdatePriv(String updatePriv) {
		this.updatePriv = updatePriv;
	}

	public String getDeletePriv() {
		return deletePriv;
	}

	public void setDeletePriv(String deletePriv) {
		this.deletePriv = deletePriv;
	}

	public String getCreatePriv() {
		return createPriv;
	}

	public void setCreatePriv(String createPriv) {
		this.createPriv = createPriv;
	}

	public String getDropPriv() {
		return dropPriv;
	}

	public void setDropPriv(String dropPriv) {
		this.dropPriv = dropPriv;
	}

	public String getIndexPriv() {
		return indexPriv;
	}

	public void setIndexPriv(String indexPriv) {
		this.indexPriv = indexPriv;
	}

	public String getAlterPriv() {
		return alterPriv;
	}

	public void setAlterPriv(String alterPriv) {
		this.alterPriv = alterPriv;
	}

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
}
