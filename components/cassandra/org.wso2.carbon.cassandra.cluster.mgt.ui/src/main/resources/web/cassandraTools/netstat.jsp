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
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNetstatProperties" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyClusterNetstat" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNetstatStreamingProperties" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String selectedHost=request.getParameter(ClusterUIConstants.SELECTED_HOST);
    ProxyClusterNetstat proxyClusterNetstat=null;
    try{
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        proxyClusterNetstat=clusterNodeStatsAdminClient.getNetstat(hostAddress,selectedHost);
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
            label="cassandra.cluster.stats.netstat"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.stats.netstat" />-<%=selectedHost%></h2>
        <div id="workArea">
            <h3><fmt:message key="cassandra.cluster.stats.netstat.operationMode"/> :<%=proxyClusterNetstat.getOperationMode()%></h3>
            <br/>
            <h3><fmt:message key="cassandra.cluster.stats.netstat.receivingS"/></h3>
            <table class="styledLeft" id="receiveStream">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.netstat.receivingH"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.netstat.fileNames"/></th>
                </tr>
                </thead>
                <tbody>
                <%for(ProxyNetstatStreamingProperties s:proxyClusterNetstat.getProxyNetstatReceivingStreamingProperties()){%>
                <tr>
                    <td>
                        <%=s.getHost()%>
                    </td>
                    <td>
                        <%if(s.getFileName()[0]!=null){
                            for(String ss:s.getFileName()){
                        %>
                        <%=ss%>
                        <br/>
                        <%}}else{%>
                        <fmt:message key="cassandra.cluster.stats.netstat.receivingN"/>
                        <%}%>
                    </td>
                </tr>
                <%}%>
                </tbody>
            </table>
            <br/>
            <h3><fmt:message key="cassandra.cluster.stats.netstat.responseS"/></h3>
            <table class="styledLeft" id="responseStream">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.netstat.responseH"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.netstat.fileNames"/></th>
                </tr>
                </thead>
                <tbody>
                <%for(ProxyNetstatStreamingProperties r:proxyClusterNetstat.getProxyNetstatResponseStreamingProperties()){%>
                <tr>
                    <td>
                        <%=r.getHost()%>
                    </td>
                    <td>
                        <%
                            int i=0;
                            for(String ss:r.getFileName()){
                                if(r.getFileName()[i]!=null){
                        %>
                        <%=ss%>
                        <br/>
                        <%}else{%>
                        <fmt:message key="cassandra.cluster.stats.netstat.responseN"/>
                        <%}}%>
                    </td>
                </tr>
                <%}%>
                </tbody>
            </table>
            <br/>
            <h3><fmt:message key="cassandra.cluster.stats.netstat.pool.properties"/></h3>
            <table class="styledLeft" id="poolPropertyTable">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.stats.netstat.poolName"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.netstat.pool.active"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.netstat.pool.pending"/></th>
                    <th width="20%"><fmt:message key="cassandra.cluster.stats.netstat.pool.completed"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    int i=1;
                    for(ProxyNetstatProperties p:proxyClusterNetstat.getProxyNetstatProperties()){
                        if(i%2==0){
                %>
                <tr class="tableEvenRow">
                            <%}else{%>
                <tr class="tableOddRow">
                    <%}%>
                    <td><%=p.getPoolName()%></td>
                    <td><%=p.getActive()%></td>
                    <td><%=p.getPending()%></td>
                    <td><%=p.getCompleted()%></td>
                </tr>
                <%
                i++;
                }%>
                </tbody>
            </table>
            <br />
        </div>
    </div>
</fmt:bundle>