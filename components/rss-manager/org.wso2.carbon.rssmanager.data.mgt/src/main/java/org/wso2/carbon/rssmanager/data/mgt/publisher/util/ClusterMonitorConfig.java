/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.rssmanager.data.mgt.publisher.util;

public final class ClusterMonitorConfig {

    private   final String nodeId;
    private   final String username;
    private   final String password;
    private   final String receiverUrl;
    private   final String secureUrl;
    private   final String cronExpression;
    private   final boolean isMonitoringEnable;
    private   final String  dataCollectors;
    
    
    
	public ClusterMonitorConfig(String nodeId, String username, String password,
                                 String receiverUrl, String secureUrl, String cronExpression,
                                 boolean isMonitoringEnable,String dataCollectors) {
	    super();
	    this.nodeId = nodeId;
	    this.username = username;
	    this.password = password;
	    this.receiverUrl = receiverUrl;
	    this.secureUrl = secureUrl;
	    this.cronExpression = cronExpression;
	    this.isMonitoringEnable = isMonitoringEnable;
	    this.dataCollectors = dataCollectors;
    }

	


	public String getNodeId() {
		return nodeId;
	}



	public String getUsername() {
		return username;
	}



	public String getPassword() {
		return password;
	}



	public String getReceiverUrl() {
		return receiverUrl;
	}



	public String getSecureUrl() {
		return secureUrl;
	}



	public String getCronExpression() {
		return cronExpression;
	}



	public boolean isMonitoringEnable() {
		return isMonitoringEnable;
	}




	public String getDataCollectors() {
		return dataCollectors;
	}
	
	

    
}

