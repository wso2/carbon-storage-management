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
package org.wso2.carbon.hdfs.namenode;

import java.util.Arrays;
import java.util.List;

/**
 * check the authoriy of the user for the access
 */
public class HDFSAuthority {
    // pass user protocol (what else ?? )
    public boolean isUserAuthorized(String userName) throws Exception {
        HDFSNameNodeComponentManager componentManager = HDFSNameNodeComponentManager.getInstance();
        boolean isUserexist = componentManager.getRealmForCurrentTenant().getUserStoreManager().isExistingUser(userName);
        List<String> userRoles = Arrays.asList(componentManager.getRealmForCurrentTenant().getUserStoreManager().getRoleListOfUser(userName));
        //get protocol list
        //authorize  user to protocol
        if (isUserexist) {
            return true;
        }
        return false;

    }
}
