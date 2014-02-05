package org.wso2.carbon.cassandra.cluster.proxy.data;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

public class ProxyColumnFamilyHistograms {
    private long offset;
    private long SSTables;
    private long writeLatency;
    private long readLatency;
    private long rowSize;
    private long columnCount;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSSTables() {
        return SSTables;
    }

    public void setSSTables(long SSTables) {
        this.SSTables = SSTables;
    }

    public long getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(long writeLatency) {
        this.writeLatency = writeLatency;
    }

    public long getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(long readLatency) {
        this.readLatency = readLatency;
    }

    public long getRowSize() {
        return rowSize;
    }

    public void setRowSize(long rowSize) {
        this.rowSize = rowSize;
    }

    public long getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(long columnCount) {
        this.columnCount = columnCount;
    }
}
