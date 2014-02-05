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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterNodeStatsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyCompactionStats" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyCompactionProperties" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    ProxyCompactionStats proxyCompactionStats=null;
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    try{
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        proxyCompactionStats=clusterNodeStatsAdminClient.getCompactionStats(hostAddress);
    }
    catch (Exception e)
    {
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
            label="cassandra.cluster.node.compactionStats"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.node.compactionStats" />-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%></h2>
        <div id="workArea">
            <table class="styledLeft" id="compactionPropertyTable" width="50%">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="cassandra.cluster.node.compactionProperties"/></th>
                </tr>
                </thead>
                <tbody>
                <tr class="tableOddRow">
                    <td width="20%"><fmt:message key="cassandra.cluster.compaction.pendingTasks"/></td>
                    <td width="60"><%=proxyCompactionStats.getPendingTasks()%></td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.compaction.activeRTime"/>
                    </td>
                    <td><%=proxyCompactionStats.getActiveCompactionRemainingTime()%></td>
                </tr>
                </tbody>
            </table>
            <br/>
            <%if(proxyCompactionStats.getProxyCompactionProperties()!=null && proxyCompactionStats.getProxyCompactionProperties()[0]!=null){%>

            <h3><fmt:message key="cassandra.cluster.cacheProperties"/></h3>
            <table class="styledLeft" id="cachePropertyTable">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cluster.compactionTypes"/> </th>
                    <th width="20%"><fmt:message key="cassandra.cluster.keyspace"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.column.family"/> </th>
                    <th width="20%"><fmt:message key="cassandra.cluster.bytesCompacted"/> </th>
                    <th width="20%"><fmt:message key="cassandra.cluster.bytesTotal"/> </th>
                    <th width="20%"><fmt:message key="cassandra.cluster.progress"/> </th>
                </tr>
                </thead>
                <tbody>
                <%for(ProxyCompactionProperties p:proxyCompactionStats.getProxyCompactionProperties()){%>
                <tr>
                    <td><%=p.getCompactionType()%></td>
                    <td><%=p.getKeyspace()%></td>
                    <td><%=p.getColumFamily()%></td>
                    <td><%=p.getBytesCompacted()%></td>
                    <td><%=p.getBytesTotal()%></td>
                    <td><%=p.getProgress()%></td>
                </tr>
                <%}%>
                </tbody>
            </table>
            <br/>
            <%}%>
        </div>
    </div>
</fmt:bundle>