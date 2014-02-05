<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>

<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Row" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keySpace");
    String columnFamily = request.getParameter("columnFamily");
    //  String rowId = request.getParameter("row_id");
    String startKey = request.getParameter("startKey");
    String endKey = request.getParameter("endKey");
    boolean isReversed = Boolean.valueOf(request.getParameter("isReversed"));
    int echoValue = Integer.parseInt(request.getParameter("sEcho"));
    int displayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
    int displayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
    String searchKey = request.getParameter("sSearch");
    CassandraExplorerAdminClient cassandraExplorerAdminClient = null;
    Row[] rows = new Row[0];
    int noOfTotalRows = 0;
    int noOfFilteredRows = 0;
    JSONObject jsonResponse = new JSONObject();
    try {
        cassandraExplorerAdminClient =
                new CassandraExplorerAdminClient(config.getServletContext(), session);


        noOfTotalRows = cassandraExplorerAdminClient.getNoOfRows(keyspace, columnFamily);

        if (searchKey != null && !searchKey.isEmpty()) {
            rows = cassandraExplorerAdminClient.searchRows(keyspace, columnFamily, searchKey,
                                                           displayStart, displayLength);
            noOfFilteredRows = cassandraExplorerAdminClient.getNoOfFilteredResultsoforRows(keyspace,
                                                                                           columnFamily,
                                                                                           searchKey);
        } else {
            rows = cassandraExplorerAdminClient.
                    getPaginateSliceforRows(keyspace, columnFamily, displayStart, displayLength);
            noOfFilteredRows = noOfTotalRows;
        }

        int totalDisplayRecords = 0;
        if (rows != null) {
            totalDisplayRecords = rows.length;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sEcho", echoValue);
        jsonObject.put("iTotalRecords", noOfTotalRows);
        jsonObject.put("iTotalDisplayRecords", noOfFilteredRows);

        JSONArray dataArray = new JSONArray();
        if (rows != null) {
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] != null) {
                    JSONArray valueArray = new JSONArray();
                    valueArray.add(rows[i].getRowId());
                    Column[] columns = rows[i].getColumns();
                    if(columns == null){
                        columns = new Column[0];
                    }
                    for (int j = 0; j < columns.length; j++) {
                        valueArray.add("<pre>KEY: " + StringEscapeUtils.escapeXml(columns[j].getName()) + "\nVALUE: " +
                            StringEscapeUtils.escapeXml(columns[j].getValue()) + "</pre>");
                    }
                    if (columns.length < 3) {
                        for (int k = 0; k < 3 - columns.length; k++) {
                            valueArray.add("No data");
                        }
                    }
                    valueArray.add("<a class=\"view-icon-link\" href=\"#\" \" onclick=\"" +
                                   "getDataPageForRow(\'" + keyspace + "\',\'" + columnFamily + "\',\'"
                                   + rows[i].getRowId() + "\')\">View more</a>");
                    dataArray.add(valueArray);
                }
            }
        }
        jsonObject.put("aaData", dataArray);
        jsonResponse.put("data", jsonObject);
        jsonResponse.put("status", "success");
        response.getWriter().print(jsonResponse.toJSONString());
    } catch (Exception e) {
        jsonResponse.put("status", "fail");
        jsonResponse.put("message", e.getMessage());
        response.getWriter().print(jsonResponse);
    }
%>