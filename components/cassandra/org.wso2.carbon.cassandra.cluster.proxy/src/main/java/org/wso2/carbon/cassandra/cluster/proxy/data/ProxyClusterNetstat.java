package org.wso2.carbon.cassandra.cluster.proxy.data;/*
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

public class ProxyClusterNetstat {
    private String operationMode;
    private ProxyNetstatStreamingProperties[] proxyNetstatReceivingStreamingProperties;
    private ProxyNetstatStreamingProperties[] proxyNetstatResponseStreamingProperties;
    private ProxyNetstatProperties[] proxyNetstatProperties;

    public ProxyNetstatStreamingProperties[] getProxyNetstatReceivingStreamingProperties() {
        return proxyNetstatReceivingStreamingProperties;
    }

    public void setProxyNetstatReceivingStreamingProperties(
            ProxyNetstatStreamingProperties[] proxyNetstatReceivingStreamingProperties) {
        this.proxyNetstatReceivingStreamingProperties = proxyNetstatReceivingStreamingProperties;
    }

    public ProxyNetstatStreamingProperties[] getProxyNetstatResponseStreamingProperties() {
        return proxyNetstatResponseStreamingProperties;
    }

    public void setProxyNetstatResponseStreamingProperties(
            ProxyNetstatStreamingProperties[] proxyNetstatResponseStreamingProperties) {
        this.proxyNetstatResponseStreamingProperties = proxyNetstatResponseStreamingProperties;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }

    public ProxyNetstatProperties[] getProxyNetstatProperties() {
        return proxyNetstatProperties;
    }

    public void setProxyNetstatProperties(ProxyNetstatProperties[] proxyNetstatProperties) {
        this.proxyNetstatProperties = proxyNetstatProperties;
    }
}
