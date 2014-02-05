/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cassandra.common.auth;

import org.apache.cassandra.auth.Permission;

import java.io.File;

public class AuthUtils {

    public static final String RESOURCE_PATH_PREFIX = File.separator + "permission" + File.separator +
            "admin" + File.separator + "cassandra";

    public static Permission getCassandraPermission(String action) {
        if (Action.ACTION_CREATE.equals(action)) {
            return Permission.CREATE;
        } else if (Action.ACTION_ALTER.equals(action)) {
            return Permission.ALTER;
        } else if (Action.ACTION_DROP.equals(action)) {
            return Permission.DROP;
        } else if (Action.ACTION_SELECT.equals(action)) {
            return Permission.SELECT;
        } else if (Action.ACTION_MODIFY.equals(action)) {
            return Permission.MODIFY;
        } else if (Action.ACTION_AUTHORIZE.equals(action)) {
            return Permission.AUTHORIZE;
        } else {
            return null;
        }
    }

    public static String getActionForCassandraPermission(Permission perm) {
        switch (perm) {
            case CREATE:
                return Action.ACTION_CREATE;
            case ALTER:
                return Action.ACTION_ALTER;
            case DROP:
                return Action.ACTION_DROP;
            case SELECT:
                return Action.ACTION_SELECT;
            case MODIFY:
                return Action.ACTION_MODIFY;
            case AUTHORIZE:
                return Action.ACTION_AUTHORIZE;
            default:
                return null;
        }
    }
}