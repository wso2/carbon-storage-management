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

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.codehaus.jackson.JsonEncoding" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keySpace");
    String columnFamily = request.getParameter("columnFamily");
    String rowId = request.getParameter("row_id");
    String startKey = request.getParameter("startKey");
    String endKey = request.getParameter("endKey");
    boolean isReversed = Boolean.valueOf(request.getParameter("isReversed"));
    int echoValue = Integer.parseInt(request.getParameter("sEcho"));
    int displayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
    int displayLenght = Integer.parseInt(request.getParameter("iDisplayLength"));
    String searchKey = request.getParameter("sSearch");
    CassandraExplorerAdminClient cassandraExplorerAdminClient = null;

    try {
        cassandraExplorerAdminClient =
                new CassandraExplorerAdminClient(config.getServletContext(), session);

    Column[] columns;
    int noOfTotalColumns;
    int noOfFilteredColumns;

    noOfTotalColumns = cassandraExplorerAdminClient.getNoOfColumns(keyspace, columnFamily, rowId);

    if (searchKey != null && !searchKey.isEmpty()) {
        columns = cassandraExplorerAdminClient.searchColumns(keyspace, columnFamily, rowId, searchKey,
                                                             displayStart, displayLenght);
        noOfFilteredColumns = cassandraExplorerAdminClient.getNoOfFilteredResultsoforColumns(keyspace, columnFamily,
                                                                                             rowId, searchKey);
    } else {
        columns = cassandraExplorerAdminClient.
                getPaginateSliceforColumns(keyspace, columnFamily, rowId, displayStart, displayLenght);
        noOfFilteredColumns = noOfTotalColumns;
    }
    int totalDisplayRecords = 0;
    if (columns != null) {
        totalDisplayRecords = columns.length;
    }
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("sEcho",echoValue);
    jsonObject.put("iTotalRecords",noOfTotalColumns);
    jsonObject.put("iTotalDisplayRecords",noOfFilteredColumns);

    JSONArray valuesArray = new JSONArray();
    if (columns != null) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null) {
                JSONArray columnValueArray = new JSONArray();
                columnValueArray.add( columns[i].getName());
                columnValueArray.add(StringEscapeUtils.escapeXml(columns[i].getValue()));
                columnValueArray.add((new Date(columns[i].getTimeStamp()/1000)).toString());

                valuesArray.add(columnValueArray);
                }
            }
    }
    jsonObject.put("aaData",valuesArray);
    response.getWriter().print(jsonObject.toJSONString());
    } catch (Exception e) {%>
<script type="text/javascript">
    location.href = "cassandra_connect.jsp?region=region1&item=cassandra_explorer_connect_menu";
</script>
<% }
  %>

