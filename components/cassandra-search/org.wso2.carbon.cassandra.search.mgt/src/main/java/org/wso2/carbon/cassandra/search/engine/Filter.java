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

package org.wso2.carbon.cassandra.search.engine;

import org.wso2.carbon.cassandra.search.utils.OperationType;

/**
 * Represents an search conditionString set by the user.
 *
 */
public class Filter {
    private String property;
    private String operator;
    private String value;
    private OperationType joinOp = OperationType.AND;

    public Filter(String property, String operator, String value) {
        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public Filter(String property, String operator, String value, OperationType joinOperation) {
        this.property = property;
        this.operator = operator;
        this.value = value;
        this.joinOp = joinOperation;
    }

    public OperationType getJoinOp() {
        return joinOp;
    }

    public void setJoinOp(OperationType joinOp) {
        this.joinOp = joinOp;
    }

    public String getProperty() {
        return property;
    }

    public String getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

}
