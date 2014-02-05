/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.cassandra.mgt;

import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;

public interface CassandraManager {

    String getClusterName() throws CassandraServerManagementException;

    String[] getKeyspaces(ClusterInformation cfInfo) throws CassandraServerManagementException;

    KeyspaceDefinition getKeyspaceDefinition(String ksName) throws CassandraServerManagementException;

    void addColumnFamily(ColumnFamilyInformation cfInfo) throws CassandraServerManagementException;

    void updateColumnFamily(ColumnFamilyInformation cfInfo) throws CassandraServerManagementException;

    boolean deleteKeyspace(String ksName) throws CassandraServerManagementException;

    void addKeyspace(KeyspaceInformation ksInfo) throws CassandraServerManagementException;

    void updatedKeyspace(KeyspaceInformation ksInfo) throws CassandraServerManagementException;

    ColumnFamilyInformation getColumnFamilyOfCurrentUser(
            String ksName, String cfName) throws CassandraServerManagementException;

    KeyspaceInformation getKeyspaceofCurrentUser(String ksName) throws CassandraServerManagementException;

    String[] listColumnFamiliesOfCurrentUser(String ksName) throws CassandraServerManagementException;



}
