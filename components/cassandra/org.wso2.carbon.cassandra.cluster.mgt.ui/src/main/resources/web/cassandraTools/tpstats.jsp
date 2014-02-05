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
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyThreadPoolInfo" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyThreadPoolDroppedProperties" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyThreadPoolProperties" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    ProxyThreadPoolInfo proxyThreadPoolInfo=null;
    ProxyThreadPoolDroppedProperties[] proxyThreadPoolDroppedProperties=null;
    ProxyThreadPoolProperties[] proxyThreadPoolProperties=null;
    try{
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        proxyThreadPoolInfo=clusterNodeStatsAdminClient.getTpstats(hostAddress);
        proxyThreadPoolDroppedProperties=proxyThreadPoolInfo.getProxyThreadPoolDroppedProperties();
        proxyThreadPoolProperties=proxyThreadPoolInfo.getProxyThreadPoolProperties();
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
            label="cassandra.cluster.node.tpstats"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.node.tpstats" />-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%></h2>
        <div id="workArea">
            <h3><fmt:message key="cassandra.cluster.node.general" /></h3>
            <table class="styledLeft" id="threadPoolStatsTable">
                <thead>
                <tr>
                    <th width="5%"><fmt:message key="cassandra.cluster.node.tpstats.name" /></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.tpstats.active" /></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.tpstats.pending" /></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.tpstats.completed" /></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.tpstats.blocked" /></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.tpstats.blockedA" /></th>
                </tr>
                </thead>
                <%
                    int i=1;
                    for(ProxyThreadPoolProperties p:proxyThreadPoolProperties){
                if(i%2==0){
                %>
                <tr class="tableEvenRow">
                <%}else{%>
                <tr class="tableOddRow">
                <%}%>
                    <td><%=p.getThreadPoolPropertyName()%></td>
                    <td><%=p.getActive()%></td>
                    <td><%=p.getPending()%></td>
                    <td><%=p.getCompleted()%></td>
                    <td><%=p.getBlocked()%></td>
                    <td><%=p.getAllTimeBlocked()%></td>
                </tr>
                <%
                i++;
                }%>
                <tbody>
            </table>
            <br />
            <h3><fmt:message key="cassandra.cluster.stats.tpstats.dropP"/></h3>
            <table class="styledLeft" id="cachePropertyTable">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.tpstats.messageT"/></th>
                    <th width="60%"><fmt:message key="cassandra.cluster.stats.tpstats.dropped"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                int j=1;
                for(ProxyThreadPoolDroppedProperties d:proxyThreadPoolDroppedProperties){
                if(j%2==0){
                %>
                <tr class="tableEvenRow">
                            <%}else{%>
                <tr class="tableOddRow">
                            <%}%>
                <td><%=d.getPropertyName()%></td>
                <td><%=d.getDroppedCount()%></td>
                </tr>
                <%
                    j++;
                }%>
                </tbody>
            </table>
            <br/>
        </div>
    </div>
</fmt:bundle>