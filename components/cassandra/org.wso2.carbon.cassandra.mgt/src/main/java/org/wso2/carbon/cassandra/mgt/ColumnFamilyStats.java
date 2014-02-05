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
package org.wso2.carbon.cassandra.mgt;

/**
 * Statistics for a ColumnFamily
 */
public class ColumnFamilyStats {

    private int liveSSTableCount;
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

    public int getLiveSSTableCount() {
        return liveSSTableCount;
    }

    public void setLiveSSTableCount(int liveSSTableCount) {
        this.liveSSTableCount = liveSSTableCount;
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
}
