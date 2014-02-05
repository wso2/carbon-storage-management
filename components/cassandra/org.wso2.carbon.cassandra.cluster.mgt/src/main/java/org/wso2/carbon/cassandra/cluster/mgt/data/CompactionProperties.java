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

public class CompactionProperties {
    private String compactionType;
    private String keyspace;
    private String columFamily;
    private String bytesCompacted;
    private String bytesTotal;
    private String progress;

    public String getCompactionType() {
        return compactionType;
    }

    public void setCompactionType(String compactionType) {
        this.compactionType = compactionType;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getColumFamily() {
        return columFamily;
    }

    public void setColumFamily(String columFamily) {
        this.columFamily = columFamily;
    }

    public String getBytesCompacted() {
        return bytesCompacted;
    }

    public void setBytesCompacted(String bytesCompacted) {
        this.bytesCompacted = bytesCompacted;
    }

    public String getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(String bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
}
