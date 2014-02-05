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
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNodeInformation" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyCacheProperties" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    ProxyNodeInformation nodeInformation = null;
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    ProxyCacheProperties keyProxyCacheProperties=null;
    ProxyCacheProperties rowProxyCacheProperties=null;
    try{
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        nodeInformation=clusterNodeStatsAdminClient.getNodeInfo(hostAddress);
        keyProxyCacheProperties=nodeInformation.getKeyProxyCacheProperties();
        rowProxyCacheProperties=nodeInformation.getRowProxyCacheProperties();
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
            label="cassandra.cluster.node.info"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.node.info" />-<%if("unknown".equalsIgnoreCase(hostName)){%>
            <%=hostAddress%>
            <%}else{%>
            <%=hostName%>
            <%}%></h2>
        <div id="workArea">
            <h3><fmt:message key="cassandra.cluster.node.general"/></h3>
            <table class="styledLeft" id="nodeInfoTable">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cluster.nodeInfo.header.name" /></th>
                    <th width="60%"><fmt:message key="cassandra.cluster.nodeInfo.header.stats.types" /></th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.token" />
                    </td>
                    <td>
                        <%=nodeInformation.getToken()%>
                    </td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.node.gossipS" />
                    </td>
                    <td>
                        <%=nodeInformation.getGossipState()%>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.load" />
                    </td>
                    <td>
                        <%=nodeInformation.getLoad()%>
                    </td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.node.generationN" />
                    </td>
                    <td>
                        <%=nodeInformation.getGenerationNo()%>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.uptime" />(Seconds)
                    </td>
                    <td>
                        <%=nodeInformation.getUptime()%>
                    </td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.node.heapMemory" />(MB)
                    </td>
                    <td>
                        <%=nodeInformation.getProxyHeapMemory().getUseMemory()%>/<%=nodeInformation.getProxyHeapMemory().getMaxMemory()%>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.datacenter" />
                    </td>
                    <td>
                        <%=nodeInformation.getDataCenter()%>
                    </td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.node.rack" />
                    </td>
                    <td>
                        <%=nodeInformation.getRack()%>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.exceptionC" />
                    </td>
                    <td>
                        <%=nodeInformation.getExceptions()%>
                    </td>
                </tr>
                <tbody>
            </table>
            <br/>
            <h3><fmt:message key="cassandra.cluster.node.cache.properties"/></h3>
            <table class="styledLeft" id="cachePropertyTable">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.cache.type"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.cache.size"/>(bytes)</th>
                    <th width="20%"><fmt:message key="cassandra.cluster.node.cache.capacity"/>(bytes)</th>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.cache.requests"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.cache.hits"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.node.cache.HitRate"/></th>
                    <th width="30%"><fmt:message key="cassandra.cluster.node.cache.save"/>(s)</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <fmt:message key="cassandra.cluster.node.cache.key"/>
                    </td>
                    <td><%=keyProxyCacheProperties.getCacheSize()%></td>
                    <td><%=keyProxyCacheProperties.getCacheCapacity()%></td>
                    <td><%=keyProxyCacheProperties.getCacheRequests()%></td>
                    <td><%=keyProxyCacheProperties.getCacheHits()%></td>
                    <td><%=keyProxyCacheProperties.getCacheRecentHitRate()%></td>
                    <td><%=keyProxyCacheProperties.getCacheSavePeriodInSeconds()%></td>
                </tr>
                <tr class="tableEvenRow">
                    <td>
                        <fmt:message key="cassandra.cluster.node.cache.row"/>
                    </td>
                    <td><%=rowProxyCacheProperties.getCacheSize()%></td>
                    <td><%=rowProxyCacheProperties.getCacheCapacity()%></td>
                    <td><%=rowProxyCacheProperties.getCacheRequests()%></td>
                    <td><%=rowProxyCacheProperties.getCacheHits()%></td>
                    <td><%=rowProxyCacheProperties.getCacheRecentHitRate()%></td>
                    <td><%=rowProxyCacheProperties.getCacheSavePeriodInSeconds()%></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>