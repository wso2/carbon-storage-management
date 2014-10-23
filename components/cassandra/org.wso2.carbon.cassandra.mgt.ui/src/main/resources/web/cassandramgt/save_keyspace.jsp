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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<%
    String envName = (String) session.getAttribute("envName");
    String clusterName = (String) session.getAttribute("clusterName");
    String name = request.getParameter("name");
    String rf = request.getParameter("rf");
    String rs = request.getParameter("rs");
    String mode = request.getParameter("mode");
    String[] nrfs = request.getParameterValues("nrfs");
    String replicationStrategy = CassandraAdminClientHelper.getReplicationStrategyClassForAlias(rs);
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        KeyspaceInformation keyspaceInformation = new KeyspaceInformation();
        keyspaceInformation.setName(name);
        keyspaceInformation.setEnvironmentName(envName);
        keyspaceInformation.setClusterName(clusterName);
        int replicationFactor = 1;
        if (rf != null && !"".equals(rf.trim())) {
            try {
                replicationFactor = Integer.parseInt(rf.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        keyspaceInformation.setStrategyClass(replicationStrategy);
        if(CassandraAdminClientConstants.NETWORK_CLASS.equals(replicationStrategy)){
            if(nrfs != null && nrfs.length != 0){
                keyspaceInformation.setStrategyOptions(nrfs);
            }
        } else {
            keyspaceInformation.setReplicationFactor(replicationFactor);
        }
        if ("add".equals(mode)) {
            cassandraKeyspaceAdminClient.addKeyspace(keyspaceInformation, session);
        } else {
            cassandraKeyspaceAdminClient.updateKeyspace(keyspaceInformation, session);
        }
        %>

       <script type="text/javascript">
           location.href = "../cassandramgt/cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu";
       </script>

        <%
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        String errorMessage = e.getMessage();
        if(e.getCause()!= null){
            errorMessage = e.getCause().getMessage();
        }
%>
<script type="text/javascript">
     var callbackUrl = "cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu";
     showErrorDialog('<%=errorMessage%>', callbackUrl);
 </script>
<%
    }
%>
