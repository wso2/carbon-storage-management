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

package org.wso2.carbon.cassandra.mgt.util;

import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.wso2.carbon.cassandra.mgt.*;

import java.nio.ByteBuffer;
import java.util.List;

public class CassandraManagementUtils {

    public static void validateColumnInformation(ColumnInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The column information is null");
        }
        String name = information.getName();
        if (name == null || "".equals(name.trim())) {
            throw new CassandraServerManagementException("The column name is null");
        }
    }

    public static void validateKeyspace(String keyspaceName) throws CassandraServerManagementException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraServerManagementException("The keyspace name is empty or null");
        }
    }

    public static void validateCF(String columnFamilyName) throws CassandraServerManagementException {
        if (columnFamilyName == null || "".equals(columnFamilyName.trim())) {
            throw new CassandraServerManagementException("The column family name is empty or null");
        }
    }

    public static ColumnFamilyInformation createColumnFamilyInformation(
            ColumnFamilyDefinition definition) throws CassandraServerManagementException {
        ColumnFamilyInformation information =
                new ColumnFamilyInformation(definition.getKeyspaceName(), definition.getName());
        information.setId(definition.getId());
        information.setComment(definition.getComment());
        information.setComparatorType(definition.getComparatorType().getClassName());
        information.setKeyCacheSize(definition.getKeyCacheSize());
        int gcGrace = definition.getGcGraceSeconds();
        if (gcGrace > 0) {
            information.setGcGraceSeconds(gcGrace);
        }
        int maxThreshold = definition.getMaxCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMaxCompactionThreshold(maxThreshold);
        }
        int minThreshold = definition.getMinCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMinCompactionThreshold(minThreshold);
        }
        information.setReadRepairChance(definition.getReadRepairChance());
        information.setRowCacheSavePeriodInSeconds(definition.getRowCacheSavePeriodInSeconds());
        information.setType(definition.getColumnType().getValue());
        information.setRowCacheSize(definition.getRowCacheSize());
        //return null with 1.0.2 cassandra
        //   information.setSubComparatorType(definition.getSubComparatorType().getClassName());
        information.setDefaultValidationClass(definition.getDefaultValidationClass());
        information.setKeyValidationClass(definition.getKeyValidationClass());

        //TODO change hector to get a columns of a CF on demand
        List<ColumnDefinition> columnDefinitions = definition.getColumnMetadata();
        ColumnInformation[] columnInformations = new ColumnInformation[columnDefinitions.size()];
        int index = 0;
        for (ColumnDefinition column : columnDefinitions) {
            if (column == null) {
                throw new CassandraServerManagementException("Column cannot be null");
            }

            ByteBuffer byteBuffer = column.getName();
            if (byteBuffer == null) {
                throw new CassandraServerManagementException("Column name cannot be null");
            }

            byte[] byteArray = new byte[byteBuffer.remaining()];   //TODO best way to do this
            byteBuffer.get(byteArray);
            String name = new String(byteArray);
            if (name.isEmpty()) {
                throw new CassandraServerManagementException("Column name cannot be empty");
            }

            ColumnInformation columnInformation = new ColumnInformation();
            columnInformation.setName(name);
            columnInformation.setIndexName(column.getIndexName());
            columnInformation.setValidationClass(column.getValidationClass());
            ColumnIndexType columnIndexType = column.getIndexType();
            if (columnIndexType != null) {
                columnInformation.setIndexType(columnIndexType.name());
            }
            columnInformations[index] = columnInformation;
            index++;
        }
        information.setColumns(columnInformations);
        return information;
    }

    public static void validateKeyspaceInformation(KeyspaceInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The keyspace information is null");
        }
        validateKeyspace(information.getName());
    }

    public static ColumnFamilyStats createCFStats(ColumnFamilyStoreMBean cfsMBean) {
        ColumnFamilyStats cfStats = new ColumnFamilyStats();

        cfStats.setLiveSSTableCount(cfsMBean.getLiveSSTableCount());
        cfStats.setLiveDiskSpaceUsed(cfsMBean.getLiveDiskSpaceUsed());
        cfStats.setTotalDiskSpaceUsed(cfsMBean.getTotalDiskSpaceUsed());

        cfStats.setMemtableColumnsCount(cfsMBean.getMemtableColumnsCount());
        cfStats.setMemtableSwitchCount(cfsMBean.getMemtableSwitchCount());
        cfStats.setMemtableDataSize(cfsMBean.getMemtableDataSize());

        cfStats.setReadCount(cfsMBean.getReadCount());
        cfStats.setReadLatency(cfsMBean.getRecentReadLatencyMicros());
        cfStats.setWriteCount(cfsMBean.getWriteCount());
        cfStats.setWriteLatency(cfsMBean.getRecentWriteLatencyMicros());
        cfStats.setPendingTasks(cfsMBean.getPendingTasks());

        return cfStats;
    }

}
