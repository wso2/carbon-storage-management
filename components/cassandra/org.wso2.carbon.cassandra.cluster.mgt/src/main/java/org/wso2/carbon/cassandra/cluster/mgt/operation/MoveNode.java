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
package org.wso2.carbon.cassandra.cluster.mgt.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.mbean.ClusterMBeanProxy;

public class MoveNode implements Runnable {
    private static Log log = LogFactory.getLog(MoveNode.class);
    private String newToken;

    public MoveNode(String newToken) {
        this.newToken = newToken;
    }

    public String getNewToken() {
        return newToken;
    }

    public void setNewToken(String newToken) {
        this.newToken = newToken;
    }


    @Override
    public void run() {
        try {
            ClusterMBeanProxy.getClusterStorageMBeanService().moveNode(newToken);
        } catch (ClusterDataAdminException e) {
            log.info("Error while move node",e);
        }
    }
}
