/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.cassandra.explorer.service;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.wso2.carbon.cassandra.explorer.connection.ConnectionManager;
import org.wso2.carbon.cassandra.explorer.data.Column;
import org.wso2.carbon.cassandra.explorer.exception.CassandraExplorerException;
import org.wso2.carbon.cassandra.explorer.utils.CFInfo;
import org.wso2.carbon.cassandra.explorer.utils.CassandraUtils;
import org.wso2.carbon.core.AbstractAdmin;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * KeySpace Explorer for Cassandra
 */
public class CassandraExplorerAdmin extends AbstractAdmin {

    private static final StringSerializer stringSerializer = new StringSerializer();
    private static final ByteBufferSerializer byteBufferSerializer = new ByteBufferSerializer();
    private ByteBuffer emptyByteBuffer = ByteBufferUtil.bytes("");

    /**
     * @param keyspaceName Selected KeySpace by tenant
     * @param columnFamily Selected Column Family by tenant
     * @return no Of Rows
     */
    public int getNoOfRows(String keyspaceName, String columnFamily)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);

        RangeSlicesQuery<ByteBuffer, ByteBuffer, ByteBuffer> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                                byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys(emptyByteBuffer, emptyByteBuffer);
        rangeSlicesQuery.setRowCount(ConnectionManager.getMaxResultCount());
        rangeSlicesQuery.setReturnKeysOnly();
        QueryResult<OrderedRows<ByteBuffer, ByteBuffer, ByteBuffer>> result;
        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }
        return result.get().getCount();
    }

    /**
     * Method to get Paginate Slice for Rows
     *
     * @param keyspaceName Name of the keyspace
     * @param columnFamily Column Family Name
     * @param startingNo   Starting Number
     * @param limit        Display Limit
     * @return
     * @throws CassandraExplorerException
     */
    public org.wso2.carbon.cassandra.explorer.data.Row[] getRowPaginateSlice
            (String keyspaceName, String columnFamily, int startingNo, int limit)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        RangeSlicesQuery<ByteBuffer, ByteBuffer, ByteBuffer> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                                byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys(emptyByteBuffer, emptyByteBuffer);
        rangeSlicesQuery.setRange(emptyByteBuffer, emptyByteBuffer, false, 3);
        rangeSlicesQuery.setRowCount(startingNo + 1);

        ArrayList<org.wso2.carbon.cassandra.explorer.data.Row> rowlist =
                new ArrayList<org.wso2.carbon.cassandra.explorer.data.Row>();

        QueryResult<OrderedRows<ByteBuffer, ByteBuffer, ByteBuffer>> result;
        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }
        ByteBuffer endKey = emptyByteBuffer;
        if (result.get().peekLast() != null) {
            endKey = result.get().peekLast().getKey();
        }
        rangeSlicesQuery.setRowCount(limit);
        rangeSlicesQuery.setKeys(endKey, emptyByteBuffer);

        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }

        for (Row<ByteBuffer, ByteBuffer, ByteBuffer> cassandraRow : result.get().getList()) {
            org.wso2.carbon.cassandra.explorer.data.Row row =
                    new org.wso2.carbon.cassandra.explorer.data.Row();
            row.setRowId(CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getKeyCassandraSerializer(), cassandraRow.getKey()));
            List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList = cassandraRow.
                    getColumnSlice().getColumns();
            Column[] columns = new Column[hColumnsList.size()];
            for (int i = 0; i < hColumnsList.size(); i++) {
                // we are sending only 3 columns max
                if (i == 3) {
                    break;
                }
                HColumn hColumn = hColumnsList.get(i);
                Column column = new Column();
                column.setName(cleanNonXmlChars(CassandraUtils.getStringDeserialization(
                        columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes())));
                String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                        getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());
                column.setValue(cleanNonXmlChars(value));
                column.setTimeStamp(hColumn.getClock());
                columns[i] = column;
            }
            row.setColumns(columns);
            rowlist.add(row);
        }
        org.wso2.carbon.cassandra.explorer.data.Row rows[] =
                new org.wso2.carbon.cassandra.explorer.data.Row[rowlist.size()];
        return rowlist.toArray(rows);

    }

    public org.wso2.carbon.cassandra.explorer.data.Row[] searchRows(String keyspaceName,
                                                                    String columnFamily,
                                                                    String searchKey,
                                                                    int startingNo, int limit)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        RangeSlicesQuery<ByteBuffer, ByteBuffer, ByteBuffer> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                                byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys(emptyByteBuffer, emptyByteBuffer);
        rangeSlicesQuery.setRange(emptyByteBuffer, emptyByteBuffer, false, 3);
        rangeSlicesQuery.setRowCount(ConnectionManager.getMaxResultCount());

        ArrayList<org.wso2.carbon.cassandra.explorer.data.Row> rowlist = new ArrayList<org.wso2.carbon.cassandra.explorer.data.Row>();

        QueryResult<OrderedRows<ByteBuffer, ByteBuffer, ByteBuffer>> result;
        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }
        for (Row<ByteBuffer, ByteBuffer, ByteBuffer> cassandraRow : result.get().getList()) {
            org.wso2.carbon.cassandra.explorer.data.Row row =
                    new org.wso2.carbon.cassandra.explorer.data.Row();

            String rowKey = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getKeyCassandraSerializer(), cassandraRow.getKey());
            //check if search key present in the row keys.
            if (rowKey.contains(searchKey)) {
                row.setRowId(cleanNonXmlChars(rowKey));
                List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList = cassandraRow.
                        getColumnSlice().getColumns();
                Column[] columns = new Column[hColumnsList.size()];
                for (int i = 0; i < hColumnsList.size(); i++) {
                    // we are sending only 3 columns max
                    if (i == 3) {
                        break;
                    }

                    HColumn hColumn = hColumnsList.get(i);
                    Column column = new Column();
                    column.setName(CassandraUtils.getStringDeserialization(
                            columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes()));

                    String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                            getColumnValueCassandraSerializer(hColumn.getNameBytes()),
                                                                           hColumn.getValueBytes());
                    column.setValue(value);
                    column.setTimeStamp(hColumn.getClock());
                    columns[i] = column;
                }
                row.setColumns(columns);
                rowlist.add(row);
            }
        }
        if (rowlist.size() < limit) {
            limit = rowlist.size();
        }
        // To render last paginate result set. Eg: if 270 results are there to render last 70
        // results
        if ((limit + startingNo) > rowlist.size()) {
            limit = rowlist.size() - startingNo;
        }
        org.wso2.carbon.cassandra.explorer.data.Row rows[] =
                new org.wso2.carbon.cassandra.explorer.data.Row[limit];

        for (int i = 0; i < limit; i++) {
            rows[i] = rowlist.get(startingNo + i);
        }
        return rows;
    }

    public int getNoOfRowSearchResults(String keyspaceName, String columnFamily
            , String searchKey)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        RangeSlicesQuery<ByteBuffer, ByteBuffer, ByteBuffer> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                                byteBufferSerializer);
        rangeSlicesQuery.setColumnFamily(columnFamily);
        rangeSlicesQuery.setKeys(emptyByteBuffer, emptyByteBuffer);
        rangeSlicesQuery.setReturnKeysOnly();
        rangeSlicesQuery.setRowCount(ConnectionManager.getMaxResultCount());

        ArrayList<org.wso2.carbon.cassandra.explorer.data.Row> rowlist =
                new ArrayList<org.wso2.carbon.cassandra.explorer.data.Row>();
        QueryResult<OrderedRows<ByteBuffer, ByteBuffer, ByteBuffer>> result;
        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }

        for (Row<ByteBuffer, ByteBuffer, ByteBuffer> cassandraRow : result.get().getList()) {
            org.wso2.carbon.cassandra.explorer.data.Row row =
                    new org.wso2.carbon.cassandra.explorer.data.Row();

            String rowKey = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getKeyCassandraSerializer(), cassandraRow.getKey());
            //check if search key present in the row keys.
            if (rowKey.contains(searchKey)) {
                rowlist.add(row);
            }
        }
        return rowlist.size();
    }

    public Column[] searchColumns(String keyspaceName, String columnFamily,
                                  String rowName, String searchKey, int startingNo, int limit)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        SliceQuery<ByteBuffer, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                          byteBufferSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        if (rowName instanceof String) {
            sliceQuery.setKey(ByteBuffer.wrap(rowName.getBytes()));
        } else {
            sliceQuery.setKey(columnFamilyInfo.getKeySerializer().toByteBuffer(rowName));
        }
        sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false,
                            ConnectionManager.getMaxResultCount());
        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result;
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }

        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList;
        ArrayList<Column> columnsList = new ArrayList<Column>();
        hColumnsList = result.get().getColumns();

        for (HColumn hColumn : hColumnsList) {
            String columnKey = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes());
            String columnValue = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());

            if ((columnKey.contains(searchKey) || columnValue.contains(searchKey))) {
                Column column = new Column();

                columnKey = cleanNonXmlChars(columnKey);
                columnValue = cleanNonXmlChars(columnValue);

                column.setName(columnKey);
                column.setValue(columnValue);
                column.setTimeStamp(hColumn.getClock());

                columnsList.add(column);
            }
        }
        // if no of results returned are fewer than limit (or display size)
        if (columnsList.size() < limit) {
            limit = columnsList.size();
        }

        Column[] columnArray = new Column[limit];
        for (int i = 0; i < limit; i++) {
            columnArray[i] = columnsList.get(startingNo + i);
        }
        return columnArray;
    }

    public int getNoOfColumnSearchResults(String keyspaceName, String columnFamily,
                                          String rowName, String searchKey)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        SliceQuery<ByteBuffer, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                          byteBufferSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        if (rowName instanceof String) {
            sliceQuery.setKey(ByteBuffer.wrap(rowName.getBytes()));
        } else {
            sliceQuery.setKey(columnFamilyInfo.getKeySerializer().toByteBuffer(rowName));
        }
        sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false,
                            ConnectionManager.getMaxResultCount());
        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result;
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }
        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList;
        hColumnsList = result.get().getColumns();

        int columnCount = 0;
        for (HColumn hColumn : hColumnsList) {

            String columnKey = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes());
            String columnValue = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());

            if ((columnKey.contains(searchKey) || columnValue.contains(searchKey))) {
                columnCount++;
            }
        }
        return columnCount;
    }

    public Column[] getColumnPaginateSlice(String keyspaceName, String columnFamily, String rowName,
                                           int startingNo, int limit)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);

        //get the results up to the startingNo
        SliceQuery<ByteBuffer, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                          byteBufferSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        if (rowName instanceof String) {
            sliceQuery.setKey(ByteBuffer.wrap(rowName.getBytes()));
        } else {
            sliceQuery.setKey(columnFamilyInfo.getKeySerializer().toByteBuffer(rowName));
        }

        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result;
        if (startingNo != 0) {
            sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false,
                                startingNo + 1);
            try {
                result = sliceQuery.execute();
            } catch (HectorException exception) {
                throw new CassandraExplorerException(exception.getMessage(), exception);
            }
            List<HColumn<ByteBuffer, ByteBuffer>> tmpHColumnsList = result.get().getColumns();

            //TODO handle if results are empty
            HColumn startingColumn = tmpHColumnsList.get(tmpHColumnsList.size() - 1);
            ByteBuffer startingColumnName = (ByteBuffer) startingColumn.getName();

            sliceQuery.setRange(startingColumnName, emptyByteBuffer, false, limit);
        } else {
            sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false, limit);
        }
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }

        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList;
        ArrayList<Column> columnsList = new ArrayList<Column>();
        hColumnsList = result.get().getColumns();

        for (HColumn hColumn : hColumnsList) {
            Column column = new Column();

            String key = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes());
            String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());

            key = cleanNonXmlChars(key);
            value = cleanNonXmlChars(value);

            column.setName(key);
            column.setValue(value);
            column.setTimeStamp(hColumn.getClock());

            columnsList.add(column);
        }
        Column[] columnArray = new Column[columnsList.size()];
        return columnsList.toArray(columnArray);
    }

    public int getNoOfColumns(String keyspaceName, String columnFamily, String rowName)
            throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, keyspaceName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                                                                     columnFamily);
        
        SliceQuery<ByteBuffer, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, byteBufferSerializer, byteBufferSerializer,
                                          byteBufferSerializer);
        sliceQuery.setColumnFamily(columnFamily);
        if (rowName instanceof String) {
            sliceQuery.setKey(ByteBuffer.wrap(rowName.getBytes()));
        } else {
            sliceQuery.setKey(columnFamilyInfo.getKeySerializer().toByteBuffer(rowName));
        }
        sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false,
                            ConnectionManager.getMaxResultCount());
        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result;
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception);
        }

        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList;
        hColumnsList = result.get().getColumns();
        return hColumnsList.size();
    }

    public boolean connectToCassandraCluster(String clusterName, String connectionUrl,
                                             String userName, String password)
            throws CassandraExplorerException {
        Map<String, String> credentials = new HashMap<String, String>();
        if (connectionUrl == null || connectionUrl.isEmpty()) {
            throw new CassandraExplorerException("Connection URL is empty. Please provide Cassandra"
                                                 + " Connection URL to connect");
        }
        if (userName != null && !userName.isEmpty() && password != null) {
            credentials.put("username", userName);
            credentials.put("password", password);
        }
        String parsedClusterName = "";
        if (clusterName.contains(":")) {
            parsedClusterName = clusterName.replace(":", "_");
        }
        ConnectionManager connectionManager = new
                ConnectionManager(parsedClusterName, connectionUrl, credentials);
        return connectionManager.isConnected();
    }

    public String[] getKeyspaces() throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Iterator<KeyspaceDefinition> keyspaceItr = null;
        try {
            keyspaceItr = cluster.describeKeyspaces().iterator();
        } catch (HectorException exception) {
            throw new CassandraExplorerException("Error in retrieving keyspaces. " +
                                                 exception.getMessage(), exception);
        }
        ArrayList<String> keyspaceNames = new ArrayList();
        while (keyspaceItr != null && keyspaceItr.hasNext()) {
            keyspaceNames.add(keyspaceItr.next().getName());
        }
        String[] keySpaceNameArray = new String[keyspaceNames.size()];
        return keyspaceNames.toArray(keySpaceNameArray);
    }

    public String[] getColumnFamilies(String keySpace) throws CassandraExplorerException {
        Cluster cluster = ConnectionManager.getCluster();
        if (cluster == null) {
            throw new CassandraExplorerException("No connection to Cluster available");
        }
        Iterator<ColumnFamilyDefinition> keyspaceItr = cluster.describeKeyspace(keySpace).getCfDefs()
                .iterator();
        ArrayList<String> columnFamiliyNamesList = new ArrayList();
        while (keyspaceItr.hasNext()) {
            columnFamiliyNamesList.add(keyspaceItr.next().getName());
        }
        String[] keySpaceNameArray = new String[columnFamiliyNamesList.size()];
        return columnFamiliyNamesList.toArray(keySpaceNameArray);
    }

    //Setting Maximum row result count

    public void setMaxRowCount(int maxRowCount) {
        ConnectionManager.setMaxResultCount(maxRowCount);
    }

    private String cleanNonXmlChars(String value) {
        String parsedString = "";
        if (value != null) {
            parsedString = value.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", " ");
        }
        if (parsedString.trim().isEmpty()) {
            parsedString = "***Non displayable value***";
        }
        return parsedString;
    }

}

