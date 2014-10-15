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
 * Class for hold ORACLE privilege set
 */
public class OraclePrivilegeSet extends DatabasePrivilegeSet {

	private String executePriv;
	private String debugPriv;
	private String flashbackPriv;
	private String readPriv;
	private String referencesPriv;
	private String underPriv;
	private String writePriv;
	private String onCommitRefreshPriv;
	private String queryRewritePriv;

	public String getExecutePriv() {
		return executePriv;
	}

	public void setExecutePriv(String executePriv) {
		this.executePriv = executePriv;
	}

	public String getDebugPriv() {
		return debugPriv;
	}

	public void setDebugPriv(String debugPriv) {
		this.debugPriv = debugPriv;
	}

	public String getFlashbackPriv() {
		return flashbackPriv;
	}

	public void setFlashbackPriv(String flashbackPriv) {
		this.flashbackPriv = flashbackPriv;
	}

	public String getReadPriv() {
		return readPriv;
	}

	public void setReadPriv(String readPriv) {
		this.readPriv = readPriv;
	}

	public String getReferencesPriv() {
		return referencesPriv;
	}

	public void setReferencesPriv(String referencesPriv) {
		this.referencesPriv = referencesPriv;
	}

	public String getUnderPriv() {
		return underPriv;
	}

	public void setUnderPriv(String underPriv) {
		this.underPriv = underPriv;
	}

	public String getWritePriv() {
		return writePriv;
	}

	public void setWritePriv(String writePriv) {
		this.writePriv = writePriv;
	}

	public String getOnCommitRefreshPriv() {
		return onCommitRefreshPriv;
	}

	public void setOnCommitRefreshPriv(String onCommitRefreshPriv) {
		this.onCommitRefreshPriv = onCommitRefreshPriv;
	}

	public String getQueryRewritePriv() {
		return queryRewritePriv;
	}

	public void setQueryRewritePriv(String queryRewritePriv) {
		this.queryRewritePriv = queryRewritePriv;
	}

}
