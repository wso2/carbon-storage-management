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
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Row" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<head>
<script type="text/javascript" language="javascript" src="js/datatables/js/jquery.dataTables.js"></script>
<script type="text/javascript" language="javascript" src="js/cassandra_cf_explorer.js"></script>
<link href="css/resetDataTables.css" rel="stylesheet" media="all"/>
<style type="text/css" title="currentStyle">
    @import "js/datatables/css/demo_page.css";
    @import "js/datatables/css/demo_table.css";
    @import "js/datatables/css/jquery.dataTables_themeroller.css";
    @import "js/datatables/css/jquery.dataTables.css";
</style>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keyspace");
    String columnFamily = request.getParameter("columnFamily");
%>
<%--TODO refactor JSI bundle--%>
<fmt:bundle basename="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="<%=request.getParameter(/"columnFamily/")%>"
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><%=keyspace%> : <%=columnFamily%>
        </h2>

        <div id="workArea">
            <div id="container">
                <div id="dynamic"></div>
                <div class="spacer"></div>
            </div>
            <script type="text/javascript">
                $(document).ready(function () {
                    $('#dynamic').html('<table cellpadding="10" cellspacing="0" border="0" class="display dataTable" id="example"></table>');
                    $('#example').dataTable({
                        "sAjaxSource":"row_datatable_ajaxprocessor.jsp",
                        "aoColumns":[
                            { "sTitle":"Row ID" },
                            { "sTitle":"Column 1" },
                            { "sTitle":"Column 2" },
                            { "sTitle":"Column 3" },
                            { "sTitle":"    " }
                        ],
                        "sPaginationType":"full_numbers",
                        "bProcessing":true,
                        "bServerSide":true,
                        "fnServerData": function ( sSource, aoData, fnCallback, oSettings ) {
                            callback = function(json){
                                if(json.status == "success"){
                                    fnCallback(json.data);
                                } else {
                                    var callbackUrl = "cassandra_keyspaces.jsp";
                                    showErrorDialog(json.message, callbackUrl);
                                }
                            };
                            aoData.push(
                                {"name":"columnFamily", "value":'<%=columnFamily%>'},
                                {"name":"keySpace", "value":'<%=keyspace%>'});
                            oSettings.jqXHR = $.ajax({
                                "dataType": 'json',
                                "type": "POST",
                                "url": sSource,
                                "data": aoData,
                                "success": callback
                            });
                        }
                    });
                });
            </script>
            <div style="clear:both"></div>
        </div>
    </div>
</fmt:bundle>