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
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.info.ClusterKeyspaceInfoAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<link href="css/clusterTools_ui.css" rel="stylesheet" media="all"/>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String[] ksNames = null;
    String tableStyle="display:none;";
    String hostAddress =null;
    String hostName=null;
    ClusterKeyspaceInfoAdminClient clusterKeyspaceInfoAdminClient =null;
    try {
        hostAddress =request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
        clusterKeyspaceInfoAdminClient =new ClusterKeyspaceInfoAdminClient(config.getServletContext(), session);
        ksNames = clusterKeyspaceInfoAdminClient.getKeyspaces(hostAddress);
        if (ksNames != null && ksNames.length > 0) {
            tableStyle="";
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.cluster.keyspaces.stats.msg"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.keyspaces.stats.msg" />(<fmt:message key="cassandra.cluster.node"/>-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%>)</h2>
        <div id="workArea">
            <table class="styledLeft" id="columnFamilyOperationTable" style="<%=tableStyle%>">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.keyspace"/> </th>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.operations"/> </th>
                </tr>
                </thead>
                <%
                    if(ksNames!=null)
                    {
                        int count=0;
                        for(String keyspace:ksNames)
                        {
                            count++;
                %>
                <tbody>
                        <%if(count%2==0){%>
                <tr class="tableEvenRow">
                            <%}else{%>
                <tr>
                    <%}%>
                    <td><a href="#" onclick="displayColumnFamilyStats('<%=hostAddress%>','<%=keyspace%>','<%=hostName%>')" ><%=keyspace%></a>
                    <td>
                        <a href="keyspaceOwnership.jsp?hostAddress=<%=hostAddress%>&keyspace=<%=keyspace%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/owner.jpg);"><fmt:message key="cassandra.cluster.stats.ownership"/> </a>
                        <a href="describeRingJMX.jsp?hostAddress=<%=hostAddress%>&keyspace=<%=keyspace%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/info.jpg);"><fmt:message key="cassandra.cluster.stats.describeRingJMX"/></a>
                    </td>
                </tr>
                        <%}
                 }%>
            </table>

        </div>
    </div>
</fmt:bundle>