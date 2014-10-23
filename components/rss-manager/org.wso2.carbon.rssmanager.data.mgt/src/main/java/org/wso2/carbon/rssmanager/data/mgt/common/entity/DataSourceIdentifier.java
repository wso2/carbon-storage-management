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
package org.wso2.carbon.rssmanager.data.mgt.common.entity;

import java.io.Serializable;

import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;

public class DataSourceIdentifier implements Serializable{
	
    private static final long serialVersionUID = 1L;
    
	private final RSSServer rssServer;
	private final DBType type;
	
	public DataSourceIdentifier(RSSServer server, DBType type) {
	    super();
	    this.rssServer = server;
	    this.type = type;
    }

	public RSSServer getRssServer() {
		return rssServer;
	}

	public DBType getType() {
		return type;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((rssServer == null) ? 0 : rssServer.hashCode());
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
	    DataSourceIdentifier other = (DataSourceIdentifier) obj;
	    if (rssServer == null) {
		    if (other.rssServer != null)
			    return false;
	    } else if (!rssServer.equals(other.rssServer))
		    return false;
	    if (type != other.type)
		    return false;
	    return true;
    }

   
}
