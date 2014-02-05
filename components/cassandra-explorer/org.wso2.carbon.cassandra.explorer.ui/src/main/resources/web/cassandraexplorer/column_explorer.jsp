<!--
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
-->
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<head>
<script type="text/javascript" language="javascript" src="js/datatables/js/jquery.js"></script>
<script type="text/javascript" language="javascript"
        src="js/datatables/js/jquery.dataTables.js"></script>
<link href="css/resetDataTables.css" rel="stylesheet" media="all"/>
<style type="text/css" title="currentStyle">
    @import "js/datatables/css/demo_page.css";
    @import "js/datatables/css/demo_table.css";
    @import "js/datatables/css/jquery-ui-1.8.4.custom.css";
    @import "js/datatables/css/jquery.dataTables_themeroller.css";
    @import "js/datatables/css/jquery.dataTables.css";
</style>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keyspace");
    String columnFamily = request.getParameter("columnFamily");
    String columnKey = request.getParameter("columnKey");
    String rowID = request.getParameter("rowID");
    CassandraExplorerAdminClient cassandraExplorerAdminClient
            = new CassandraExplorerAdminClient(config.getServletContext(), session);
%>
<%--TODO refactor JSI bundle--%>
<fmt:bundle basename="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="<%=request.getParameter("rowID")%>"
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="cassandra.cf.row"/> : <%=rowID%>
        </h2>

        <div id="workArea">
            <div id="container">
                <div id="dynamic"></div>
                <div class="spacer"></div>
            </div>

            <script type="text/javascript" charset="utf-16">
                /* Data set */
                $(document).ready(function () {
                    $('#dynamic').html('<table cellpadding="10" cellspacing="0" border="0" class="display dataTable" id="example"></table>');
                    $('#example').dataTable({
                                                "sAjaxSource":"column_datatable_ajaxprocessor.jsp",
                                                "aoColumns":[
                                                    { "sTitle":"Column Name" },
                                                    { "sTitle":"Column Value" },
                                                    { "sTitle":"Time Stamp" }
                                                ],
                                                "sPaginationType":"full_numbers",
                                                "bProcessing":true,
                                                "bServerSide":true,
                                                "fnServerParams":function (aoData) {
                                                    aoData.push({ "name":"row_id", "value":'<%=rowID%>' },
                                                                {"name":"columnFamily", "value":'<%=columnFamily%>'},
                                                                {"name":"keySpace", "value":'<%=keyspace%>'});
                                                }
                                            }
                    );
                });
            </script>
            <div style="clear:both"></div>
        </div>
    </div>
</fmt:bundle>