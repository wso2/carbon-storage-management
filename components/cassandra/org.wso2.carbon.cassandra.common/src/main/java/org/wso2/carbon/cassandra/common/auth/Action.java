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

public class Action {
    // schema management
    public static String ACTION_CREATE = "add"; // CREATE KEYSPACE and CREATE TABLE.
    public static String ACTION_ALTER = "edit";  // ALTER KEYSPACE, ALTER TABLE, CREATE INDEX, DROP INDEX.
    public static String ACTION_DROP = "delete";   // DROP KEYSPACE and DROP TABLE.

    // data access
    public static String ACTION_SELECT = "browse"; // SELECT.
    public static String ACTION_MODIFY = "consume"; // INSERT, UPDATE, DELETE, TRUNCATE.

    // permission management
    public static String ACTION_AUTHORIZE = "authorize"; // GRANT and REVOKE.

    public static String[] ALL_ACTIONS_ARRAY = {ACTION_CREATE, ACTION_ALTER, ACTION_DROP, ACTION_SELECT,
            ACTION_MODIFY, ACTION_AUTHORIZE };
}
