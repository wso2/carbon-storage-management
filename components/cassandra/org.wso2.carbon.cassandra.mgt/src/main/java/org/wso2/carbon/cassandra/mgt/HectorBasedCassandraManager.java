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

public class HectorBasedCassandraManager implements CassandraManager {

    @Override
    public String getClusterName() throws CassandraServerManagementException {
        return null;  
    }

    @Override
    public String[] getKeyspaces(ClusterInformation cfInfo) throws CassandraServerManagementException {
        return new String[0];  
    }

    @Override
    public KeyspaceDefinition getKeyspaceDefinition(String ksName) throws CassandraServerManagementException {
        return null;  
    }

    @Override
    public void addColumnFamily(ColumnFamilyInformation cfInfo) throws CassandraServerManagementException {
        
    }

    @Override
    public void updateColumnFamily(ColumnFamilyInformation cfInfo) throws CassandraServerManagementException {
        
    }

    @Override
    public boolean deleteKeyspace(String ksName) throws CassandraServerManagementException {
        return false;  
    }

    @Override
    public void addKeyspace(KeyspaceInformation ksInfo) throws CassandraServerManagementException {
        
    }

    @Override
    public void updatedKeyspace(KeyspaceInformation ksInfo) throws CassandraServerManagementException {
        
    }

    @Override
    public ColumnFamilyInformation getColumnFamilyOfCurrentUser(String ksName, String cfName) throws CassandraServerManagementException {
        return null;  
    }

    @Override
    public KeyspaceInformation getKeyspaceofCurrentUser(String ksName) throws CassandraServerManagementException {
        return null;  
    }

    @Override
    public String[] listColumnFamiliesOfCurrentUser(String ksName) throws CassandraServerManagementException {
        return new String[0];  
    }
}
