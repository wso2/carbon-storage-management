package org.wso2.carbon.cassandra.cluster.mgt.data;/*
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

public class ClusterNetstat {
    private String operationMode;
    private NetstatStreamingProperties[] netstatReceivingStreamingProperties;
    private NetstatStreamingProperties[] netstatResponseStreamingProperties;
    private NetstatProperties[] netstatProperties;

    public NetstatStreamingProperties[] getNetstatReceivingStreamingProperties() {
        return netstatReceivingStreamingProperties;
    }

    public void setNetstatReceivingStreamingProperties(
            NetstatStreamingProperties[] netstatReceivingStreamingProperties) {
        this.netstatReceivingStreamingProperties = netstatReceivingStreamingProperties;
    }

    public NetstatStreamingProperties[] getNetstatResponseStreamingProperties() {
        return netstatResponseStreamingProperties;
    }

    public void setNetstatResponseStreamingProperties(
            NetstatStreamingProperties[] netstatResponseStreamingProperties) {
        this.netstatResponseStreamingProperties = netstatResponseStreamingProperties;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }

    public NetstatProperties[] getNetstatProperties() {
        return netstatProperties;
    }

    public void setNetstatProperties(NetstatProperties[] netstatProperties) {
        this.netstatProperties = netstatProperties;
    }
}
