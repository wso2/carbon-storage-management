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
package org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource;

public final class TenantDBInfo {

	private final String databaseID;
	private final String databaseName;
	private final String databaseType;
	private final String serverInstanceId;
	private final String tenantId;
	private final String tenantDomainName;

    public TenantDBInfo(String databaseID, String databaseName, String databaseType,
                        String serverInstanceName, String tenantId, String tenantDomainName) {
	    super();
	    this.databaseID = databaseID;
	    this.databaseName = databaseName;
	    this.databaseType = databaseType;
	    this.serverInstanceId = serverInstanceName;
	    this.tenantId = tenantId;
	    this.tenantDomainName = tenantDomainName;
    }

	public String getDatabaseID() {
        return databaseID;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getServerInstanceId() {
        return serverInstanceId;
    }

    public String getTenantId() {
        return tenantId;
    }
    

    public String getTenantDomainName() {
		return tenantDomainName;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((databaseID == null) ? 0 : databaseID.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    TenantDBInfo other = (TenantDBInfo) obj;
	    if (databaseID == null) {
		    if (other.databaseID != null)
			    return false;
	    } else if (!databaseID.equals(other.databaseID))
		    return false;
	    return true;
    }

    
	
	

}
