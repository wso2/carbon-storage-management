/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.hdfs.mgt;

public class HDFSPermissionEntry {

    private boolean readAllow;
    private boolean writeAllow;
    private boolean executeAllow;

    public boolean isExecuteAllow() {
		return executeAllow;
	}

	public void setExecuteAllow(boolean executeAllow) {
		this.executeAllow = executeAllow;
	}

	public boolean isReadAllow() {
        return readAllow;
    }

    public void setReadAllow(boolean readAllow) {
        this.readAllow = readAllow;
    }

    public boolean isWriteAllow() {
        return writeAllow;
    }

    public void setWriteAllow(boolean writeAllow) {
        this.writeAllow = writeAllow;
    }
}
