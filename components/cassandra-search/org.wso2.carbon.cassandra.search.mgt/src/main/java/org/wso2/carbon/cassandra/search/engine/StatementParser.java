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

import org.wso2.carbon.cassandra.search.exception.CassandraSearchException;
import org.wso2.carbon.cassandra.search.utils.OperationType;
import org.wso2.carbon.cassandra.search.utils.SearchConstants;

import java.util.*;

public class StatementParser {
    private Map<String, List<Filter>> streamFilters;
    private Map<String, List<Filter>> allFiltersMap = null;

    public StatementParser() {
        this.streamFilters = new LinkedHashMap<String, List<Filter>>();
        allFiltersMap      = new HashMap<String, List<Filter>>();
    }

    //todo - use proper query parser
    public Map<String, List<Filter>> extractFilters(String searchQuery) throws CassandraSearchException {
        if(searchQuery == null || searchQuery.isEmpty()) {
            throw new CassandraSearchException("Null or Empty search query..");
        }

        String query;

        query = searchQuery.replaceAll(" AND | and ", "|AND|");
        query = query.replaceAll(" OR | or ", "|OR|");

        String[] conditions = query.split("\\|");
        try {
            for (int i = 0; i < conditions.length; i++) {
                OperationType joinOp = null;

                if (i != 0) {
                    try {
                        joinOp = OperationType.valueOf(conditions[i++]);
                    } catch (Exception e) {
                        --i;
                    }
                }

                String operator = getOperator(conditions[i]);

                if(operator == null) {
                    throw new CassandraSearchException("Unable to extract Filters from the search query..");
                }

                int operatorPos = conditions[i].indexOf(operator);
                String streamAndProperty = conditions[i].substring(0, operatorPos).trim();
                String value = !(operator.equals(SearchConstants.GE) || operator.equals(SearchConstants.LE)) ?
                        conditions[i].substring(operatorPos + 1).trim() : conditions[i].substring(operatorPos + 2).trim();

                String streamName = "";
                String property = "";
                String [] streamPropData = null;

                if(!streamAndProperty.contains("'")) {
                    int streamSeparatorPos = streamAndProperty.lastIndexOf('.');
                    streamName = streamAndProperty.substring(0, streamSeparatorPos).trim();
                    property = streamAndProperty.substring(streamSeparatorPos + 1).trim();
                } else {
                    if(streamAndProperty.contains("'.'")) {
                        streamPropData = streamAndProperty.split("'\\.'");

                        streamName = streamPropData[0].replaceAll("'", "").trim();
                        property   = streamPropData[1].replaceAll("'", "").trim();
                    } else if(streamAndProperty.contains(".'")) {
                        streamPropData = streamAndProperty.split("\\.'");

                        streamName = streamPropData[0].replaceAll("'", "").trim();
                        property = streamPropData[1].replaceAll("'", "").trim();
                    } else if(streamAndProperty.contains("'.")) {
                        streamPropData = streamAndProperty.split("'\\.");

                        streamName = streamPropData[0].replaceAll("'", "").trim();
                        property = streamPropData[1].replaceAll("'", "").trim();
                    }
                }

                Filter searchFilter = new Filter(property, operator, value, joinOp);

                List<Filter> filtersList = null;
                if(!streamFilters.containsKey(streamName)) {
                    filtersList = new ArrayList<Filter>();
                    filtersList.add(searchFilter);
                    streamFilters.put(streamName, filtersList);
                } else {
                    streamFilters.get(streamName).add(searchFilter);
                }
            }
        } catch (Exception e) {
            throw new CassandraSearchException("Unable to extract Filters from the search query: " + e.getMessage());
        }

        return streamFilters;
    }

    public String getOperator(String criteria) {
        if(criteria.indexOf(SearchConstants.GE) > 0) {
            return SearchConstants.GE;
        }
        if(criteria.indexOf(SearchConstants.LE) > 0) {
            return SearchConstants.LE;
        }
        if(criteria.indexOf(SearchConstants.EQ) > 0) {
            return SearchConstants.EQ;
        }
        if(criteria.indexOf(SearchConstants.LT) > 0) {
            return SearchConstants.LT;
        }
        if(criteria.indexOf(SearchConstants.GT) > 0) {
            return SearchConstants.GT;
        }
        if(criteria.indexOf(SearchConstants.CONTAINS) > 0) {
            return SearchConstants.CONTAINS;
        }
        if(criteria.indexOf(SearchConstants.NOT_CONTAIN) > 0) {
            return SearchConstants.NOT_CONTAIN;
        }
        return null;
    }
}
