/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.hdfs.dataaccess;

import org.apache.hadoop.fs.FileSystem;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.io.IOException;
import java.util.Map;

public class HDFSUserOperationListener implements UserOperationEventListener {

    @Override
    public int getExecutionOrderId() {
        return 0;
    }

    @Override
    public boolean doPreAuthenticate(String s, Object o, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostAuthenticate(String s, boolean b, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreAddUser(String s, Object o, String[] strings, Map<String, String> stringStringMap, String s1,
                                UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostAddUser(String s, Object o, String[] strings, Map<String, String> stringStringMap, String s1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    
    public boolean doPostAddUser(String s, UserStoreManager userStoreManager) throws UserStoreException {


        DataAccessService dataAccessService = new DataAccessService();
        FileSystem hdfsFS = null;
        try {
            //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
            hdfsFS = dataAccessService.mountCurrentUserFileSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean doPreUpdateCredential(String s, Object o, Object o1, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
	public boolean doPostUpdateCredential(String s, Object credential,
			UserStoreManager userStoreManager) throws UserStoreException {
		return false;
	}

    @Override
    public boolean doPreUpdateCredentialByAdmin(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String s, Object o, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }


    public boolean doPostUpdateCredentialByAdmin(String s, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreDeleteUser(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostDeleteUser(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreSetUserClaimValue(String s, String s1, String s2, String s3, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostSetUserClaimValue(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreSetUserClaimValues(String s, Map<String, String> stringStringMap, String s1,
                                           UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostSetUserClaimValues(String s, Map<String, String> stringStringMap, String s1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }


    public boolean doPostSetUserClaimValues(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String s, String[] strings, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String s, String s1, String s2, UserStoreManager userStoreManager)
            throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doPreAddRole(String s, String[] strings, Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPostAddRole(String s, String[] strings, Permission[] permissions, UserStoreManager userStoreManager) throws UserStoreException {
    	
    	return false;  
    }

    @Override
    public boolean doPreDeleteRole(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPostDeleteRole(String s, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPreUpdateRoleName(String s, String s1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPostUpdateRoleName(String s, String s1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String s, String[] strings, String[] strings1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String s, String[] strings, String[] strings1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String s, String[] strings, String[] strings1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String s, String[] strings, String[] strings1, UserStoreManager userStoreManager) throws UserStoreException {
        return false;  
    }
}
