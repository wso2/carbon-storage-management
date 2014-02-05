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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.context.CarbonContext;

/**
 * Data Access connector
 */
public class DataAccessService {
    private static Log log = LogFactory.getLog(DataAccessService.class);
    private final DataAccessComponentManager dataAccessComponentManager = DataAccessComponentManager.getInstance();

    public void DataAccessService() {

    }

     /**
     * Create connection to HDFS cluster with current user credentials.
     * @return file system connection.
     * @throws IOException
     */
    public FileSystem mountCurrentUserFileSystem() throws IOException {
        //get the current login user from carbon and
        //get the user file sytem connection object form a list if possible
        //if connection object is not in the object list
        //create shared key and register with backend
        //and create configuration with username and shared key as the passwd
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String userName = carbonContext.getUsername();

        String tenantDomain = carbonContext.getTenantDomain();
        if (tenantDomain != null && !"".equals(tenantDomain)) {
            userName += "@" + tenantDomain;
        }

        Configuration configuration = dataAccessComponentManager.getClusterConfiguration();
        //configuration.set("userName", userName);
        //SharedKeyAdminClient sharedKeyAdminClient = new SharedKeyAdminClient();
        //configuration.set("userPassword",sharedKeyAdminClient.getSharedKey());

        FileSystem fileSystem = FileSystem.get(configuration);
        return fileSystem;
    }

    /**
     * Create connection with HDFS cluster
     *
     * @param fsConfiguration
     * @return
     * @throws IOException
     */
    public FileSystem mountFileSystem(Configuration fsConfiguration) throws IOException {
        FileSystem fileSystem = FileSystem.get(fsConfiguration);
        return fileSystem;
    }

    /**
     * Close the HDFS file system connecion with HDFS cluster
     *
     * @param fileSystem
     * @throws IOException
     */
    public void unmountFileSystem(FileSystem fileSystem) throws IOException {
        fileSystem.close();
    }

    /**
     * Close all file system connections.
     * @throws IOException
     */
    public void unmountAllFileSystems() throws IOException {
        FileSystem.closeAll();
    }


}
