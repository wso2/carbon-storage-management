<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    List<String> hosts=(List<String>)session.getAttribute(ClusterUIConstants.HOSTS);
%>


<fmt:bundle basename="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.cluster.node.stats"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.node.stats" />-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%></h2>
        <div id="workArea">
            <table class="styledLeft" id="nodeOperationTable">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.node.stats.table.header.name" /></th>
                    <th><fmt:message key="cassandra.cluster.node.stats.table.header.stats.types" /></th>
                </tr>
                </thead>
                                                                                              .
                <tbody>
                <tr>
                    <td><%if("unknown".equalsIgnoreCase(hostName)){%>
                        <%=hostAddress%>
                        <%}else{%>
                        <%=hostName%>
                        <%}%>
                    </td>
                    <td>
                        <a href="nodeInfo.jsp?hostAddress=<%=hostAddress%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/info.jpg);"><fmt:message key="cassandra.cluster.node.stats.nodeInfo" /></a>
                        <a href="cfstats.jsp?hostAddress=<%=hostAddress%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/statI.jpg);"><fmt:message key="cassandra.cluster.node.stats.cfstats" /></a>
                        <a href="#" onclick="showVersion('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/info.jpg);"><fmt:message key="cassandra.cluster.node.stats.version" /></a>
                        <a href="tpstats.jsp?hostAddress=<%=hostAddress%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/statI.jpg);"><fmt:message key="cassandra.cluster.node.stats.tpstats" /></a>
                        <a href="compactionStats.jsp?hostAddress=<%=hostAddress%>&hostName=<%=hostName%>" class="icon-link" style="background-image:url(images/statI.jpg);"><fmt:message key="cassandra.cluster.node.stats.cstats" /></a>
                        <a href="#" onclick="showGossipInfo('<%=hostAddress%>','<%=hostName%>')" class="icon-link" style="background-image:url(images/info.jpg);"><fmt:message key="cassandra.cluster.node.stats.ginfo" /></a>
                        <a href="#" onclick="showNetstatForm()" class="icon-link" style="background-image:url(images/statI.jpg);"><fmt:message key="cassandra.cluster.node.stats.netstat" /></a>
                        <a href="#" onclick="rangeKeySample('<%=hostAddress%>','<%=hostName%>')" class="icon-link" style="background-image:url(images/range.png);"><fmt:message key="cassandra.cluster.node.stats.range" /></a>
                        <div style="clear:both">
                            <div id="netstatForm" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.stats.netstat.form"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.selectHost"/><span class="required">*</span></td>
                                        <td>
                                            <select name="availableHosts" id="availableHosts">
                                            <%for(String host:hosts){
                                            if(!host.isEmpty()){%>
                                                <option value="<%=host%>"><%=host%>

                                            <%}}%>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Show" onclick="stowNetstat('<%=hostAddress%>','<%=hostName%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
            <table>
                <tr>
                    <td><div style="margin-top:0px;">
                        <a class="keyspace-stats-icon-link"  href="keyspace_stats.jsp?hostAddress=<%=hostAddress%>&hostName=<%=hostName%>"><fmt:message key="cassandra.cluster.keyspace.stats"/></a>
                    </div></td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>