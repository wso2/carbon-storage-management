package org.wso2.carbon.cassandra.cluster.mgt.data;/*
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

public class ColumnFamilyInformation {
    private String columnFamilyName;
    private int SSTableCount;
    private long liveDiskSpaceUsed;
    private long totalDiskSpaceUsed;
    private long memtableColumnsCount;
    private long memtableDataSize;
    private int memtableSwitchCount;
    private long readCount;
    private double readLatency;
    private long writeCount;
    private double writeLatency;
    private int pendingTasks;
    private long numberOfKeys;
    private long bloomFilterFalsePostives;
    private double bloomFilterFalseRatio;
    private long bloomFilterSpaceUsed;
    private long compactedRowMinimumSize;
    private long compactedRowMaximumSize;
    private long compactedRowMeanSize;

    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    public void setColumnFamilyName(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    public int getSSTableCount() {
        return SSTableCount;
    }

    public void setSSTableCount(int SSTableCount) {
        this.SSTableCount = SSTableCount;
    }

    public long getLiveDiskSpaceUsed() {
        return liveDiskSpaceUsed;
    }

    public void setLiveDiskSpaceUsed(long liveDiskSpaceUsed) {
        this.liveDiskSpaceUsed = liveDiskSpaceUsed;
    }

    public long getTotalDiskSpaceUsed() {
        return totalDiskSpaceUsed;
    }

    public void setTotalDiskSpaceUsed(long totalDiskSpaceUsed) {
        this.totalDiskSpaceUsed = totalDiskSpaceUsed;
    }

    public long getMemtableColumnsCount() {
        return memtableColumnsCount;
    }

    public void setMemtableColumnsCount(long memtableColumnsCount) {
        this.memtableColumnsCount = memtableColumnsCount;
    }

    public long getMemtableDataSize() {
        return memtableDataSize;
    }

    public void setMemtableDataSize(long memtableDataSize) {
        this.memtableDataSize = memtableDataSize;
    }

    public int getMemtableSwitchCount() {
        return memtableSwitchCount;
    }

    public void setMemtableSwitchCount(int memtableSwitchCount) {
        this.memtableSwitchCount = memtableSwitchCount;
    }

    public long getReadCount() {
        return readCount;
    }

    public void setReadCount(long readCount) {
        this.readCount = readCount;
    }

    public double getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(double readLatency) {
        this.readLatency = readLatency;
    }

    public long getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(long writeCount) {
        this.writeCount = writeCount;
    }

    public double getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(double writeLatency) {
        this.writeLatency = writeLatency;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public long getNumberOfKeys() {
        return numberOfKeys;
    }

    public void setNumberOfKeys(long numberOfKeys) {
        this.numberOfKeys = numberOfKeys;
    }

    public long getBloomFilterFalsePostives() {
        return bloomFilterFalsePostives;
    }

    public void setBloomFilterFalsePostives(long bloomFilterFalsePostives) {
        this.bloomFilterFalsePostives = bloomFilterFalsePostives;
    }

    public double getBloomFilterFalseRatio() {
        return bloomFilterFalseRatio;
    }

    public void setBloomFilterFalseRatio(double bloomFilterFalseRatio) {
        this.bloomFilterFalseRatio = bloomFilterFalseRatio;
    }

    public long getBloomFilterSpaceUsed() {
        return bloomFilterSpaceUsed;
    }

    public void setBloomFilterSpaceUsed(long bloomFilterSpaceUsed) {
        this.bloomFilterSpaceUsed = bloomFilterSpaceUsed;
    }

    public long getCompactedRowMinimumSize() {
        return compactedRowMinimumSize;
    }

    public void setCompactedRowMinimumSize(long compactedRowMinimumSize) {
        this.compactedRowMinimumSize = compactedRowMinimumSize;
    }

    public long getCompactedRowMaximumSize() {
        return compactedRowMaximumSize;
    }

    public void setCompactedRowMaximumSize(long compactedRowMaximumSize) {
        this.compactedRowMaximumSize = compactedRowMaximumSize;
    }

    public long getCompactedRowMeanSize() {
        return compactedRowMeanSize;
    }

    public void setCompactedRowMeanSize(long compactedRowMeanSize) {
        this.compactedRowMeanSize = compactedRowMeanSize;
    }




}
