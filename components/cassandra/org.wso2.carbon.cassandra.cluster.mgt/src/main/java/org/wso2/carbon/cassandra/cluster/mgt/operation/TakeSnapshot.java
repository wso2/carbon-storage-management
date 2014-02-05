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

public class TakeSnapshot implements Runnable {

    private static Log log = LogFactory.getLog(ClearSnapshot.class);
    private String tag;
    private String keyspace;
    private String cf;

    public TakeSnapshot(String tag, String keyspace, String cf) {
        this.tag = tag;
        this.keyspace = keyspace;
        this.cf = cf;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }
    @Override
    public void run(){

        try {
            ClusterMBeanProxy.getClusterStorageMBeanService().takeSnapShot(tag,cf,keyspace);
        } catch (ClusterDataAdminException e) {
            log.info("Error while taking the snapshot",e);
        }

    }
}
