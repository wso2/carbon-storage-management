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
package org.wso2.carbon.cassandra.search.data.json;

import java.util.ArrayList;
import java.util.List;

public class ActivityEvent {
    private int index;
    private String stream;
    private String rowId;
    private String version;
    private String timestamp;
    private String host;
    private String server;
    private String direction;
    private String serviceName;
    private String operationName;

    private List<ColumnValue> content;

    public ActivityEvent(String rowId) {
        this.rowId = rowId;
        content = new ArrayList<ColumnValue>();
    }

    public ActivityEvent() {
        content = new ArrayList<ColumnValue>();
    }

    public ActivityEvent(String stream, String rowId, String version, String timestamp) {
        this.stream = stream;
        this.rowId = rowId;
        this.version = version;
        this.timestamp = timestamp;
        content = new ArrayList<ColumnValue>();
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<ColumnValue> getContent() {
        return content;
    }

    public void setContent(List<ColumnValue> content) {
        this.content = content;
    }

    public void addAttributeValue(ColumnValue record) {
        content.add(record);
    }

    public void addColumnValue(String columnName, String columnValue) {
        content.add(new ColumnValue(columnName, columnValue));
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
