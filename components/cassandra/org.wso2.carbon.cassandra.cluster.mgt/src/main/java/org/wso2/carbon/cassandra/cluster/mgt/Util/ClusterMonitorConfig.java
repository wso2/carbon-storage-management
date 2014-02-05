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
package org.wso2.carbon.cassandra.cluster.mgt.Util;

public class ClusterMonitorConfig {

    private static  String nodeId;
    private static  String username;
    private static  String password;
    private static  String receiverUrl;
    private static  String secureUrl;
    private static  String cronExpression;
    private static  boolean isMonitoringEnable;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ClusterMonitorConfig.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ClusterMonitorConfig.password = password;
    }

    public static String getReceiverUrl() {
        return receiverUrl;
    }

    public static void setReceiverUrl(String receiverUrl) {
        ClusterMonitorConfig.receiverUrl = receiverUrl;
    }

    public static String getSecureUrl() {
        return secureUrl;
    }

    public static void setSecureUrl(String secureUrl) {
        ClusterMonitorConfig.secureUrl = secureUrl;
    }

    public static String getCronExpression() {
        return cronExpression;
    }

    public static void setCronExpression(String cronExpression) {
        ClusterMonitorConfig.cronExpression = cronExpression;
    }

    public static boolean isMonitoringEnable() {
        return isMonitoringEnable;
    }

    public static void setMonitoringEnable(boolean monitoringEnable) {
        isMonitoringEnable = monitoringEnable;
    }

    public static String getNodeId() {
        return nodeId;
    }

    public static void setNodeId(String nodeId) {
        ClusterMonitorConfig.nodeId = nodeId;
    }
}

