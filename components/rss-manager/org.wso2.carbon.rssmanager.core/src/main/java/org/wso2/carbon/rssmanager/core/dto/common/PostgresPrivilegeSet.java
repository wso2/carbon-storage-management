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
 * Class for hold POSTGRES privilege set
 */
public class PostgresPrivilegeSet extends DatabasePrivilegeSet {

	private String truncatePriv;
	private String referencesPriv;
	private String triggerPriv;
	private String connectPriv;
	private String temporaryPriv;
	private String executePriv;
	private String usagePriv;
	private String tempPriv;

	public String getTruncatePriv() {
		return truncatePriv;
	}

	public void setTruncatePriv(String truncatePriv) {
		this.truncatePriv = truncatePriv;
	}

	public String getReferencesPriv() {
		return referencesPriv;
	}

	public void setReferencesPriv(String referencesPriv) {
		this.referencesPriv = referencesPriv;
	}

	public String getTriggerPriv() {
		return triggerPriv;
	}

	public void setTriggerPriv(String triggerPriv) {
		this.triggerPriv = triggerPriv;
	}

	public String getConnectPriv() {
		return connectPriv;
	}

	public void setConnectPriv(String connectPriv) {
		this.connectPriv = connectPriv;
	}

	public String getTemporaryPriv() {
		return temporaryPriv;
	}

	public void setTemporaryPriv(String temporaryPriv) {
		this.temporaryPriv = temporaryPriv;
	}

	public String getExecutePriv() {
		return executePriv;
	}

	public void setExecutePriv(String executePriv) {
		this.executePriv = executePriv;
	}

	public String getUsagePriv() {
		return usagePriv;
	}

	public void setUsagePriv(String usagePriv) {
		this.usagePriv = usagePriv;
	}

	public String getTempPriv() {
		return tempPriv;
	}

	public void setTempPriv(String tempPriv) {
		this.tempPriv = tempPriv;
	}

}
