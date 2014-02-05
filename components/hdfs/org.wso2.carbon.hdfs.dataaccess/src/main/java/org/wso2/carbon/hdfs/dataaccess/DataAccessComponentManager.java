/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hdfs.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.wso2.carbon.identity.authentication.SharedKeyAccessService;

/**
 * Data Access Component Manager
 */
public class DataAccessComponentManager {
    private static Log log = LogFactory.getLog(DataAccessComponentManager.class);
    private static DataAccessComponentManager ourInstance = new DataAccessComponentManager();

    /* To be used to find hdfs component configuration*/
    private static Configuration clusterConfiguration = new ClusterConfiguration().getDefaultConfiguration();
    private SharedKeyAccessService sharedKeyAccessService;

    private boolean initialized = false;

    public static DataAccessComponentManager getInstance() {
        return ourInstance;
    }

    public DataAccessComponentManager() {

    }

//    public void init(SharedKeyAccessService sharedKeyAccessService) {
//        this.sharedKeyAccessService = sharedKeyAccessService;
//        this.initialized = true;
//    }

    public void init() {
//        this.sharedKeyAccessService = sharedKeyAccessService;
        this.initialized = true;

    }

    public boolean isInitialized() {
        return initialized;
    }

    public Configuration getClusterConfiguration() {
       // assertInitialized();
        return clusterConfiguration;
    }

    public Configuration getUserClusterConfiguration(String tenantId){
        //verify user and setuser confi
        return clusterConfiguration;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new DataAccessComponentException("HDFS DataAccess component has not been initialized", log);
        }
    }

//    public SharedKeyAccessService getSharedKeyAccessService() {
//        assertInitialized();
//        return sharedKeyAccessService;
//    }
}