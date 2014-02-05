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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String[] columnFamilyNames = null;

    try{
        ClusterKeyspaceInfoAdminClient clusterKeyspaceInfoAdminClient =new ClusterKeyspaceInfoAdminClient(config.getServletContext(), session);
        columnFamilyNames= clusterKeyspaceInfoAdminClient.getColumnFamiliesForKeyspace(hostAddress, keyspace);
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
            label="cassandra.cluster.node.stats.CFStats"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.tools.column.family.stats.msg"/>(<fmt:message key="cassandra.cluster.keyspace"/> -<%=keyspace%>)</h2>

        <div id="workArea">
            <table class="styledLeft" id="columnFamilyOperationTable" width="100%">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cluster.column.family"/></th>
                    <th width="60%"><fmt:message key="cassandra.cluster.statTypes"/> </th>
                </tr>
                </thead>
                <tbody>
                        <%if(columnFamilyNames!=null){%>
                        <%
                                for(String columnFamily:columnFamilyNames)
                                {
                            %>
                <tr>
                    <td width="200px">
                        <%=columnFamily%>
                    </td>
                    <td>
                        <a href="showCFHistograms.jsp?hostAddress=<%=hostAddress%>&keyspace=<%=keyspace%>&columnFamily=<%=columnFamily%>" class="icon-link" style="background-image:url(images/histograms.jpg);"><fmt:message key="cassandra.cluster.stats.histograms"/> </a>
                        <a href="#" onclick="showEndpointsForm()" class="icon-link" style="background-image:url(images/endpoints.jpg);"><fmt:message key="cassandra.cluster.stats.endpoints"/></a>
                        <a href="#" onclick="showSSTablesForm()" class="icon-link" style="background-image:url(images/sstables.jpg);"><fmt:message key="cassandra.cluster.stats.sstables"/></a>
                        <a href="#" onclick="showCompactionThreshoulds('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')" class="icon-link" style="background-image:url(images/compact.png);"><fmt:message key="cassandra.cluster.stats.compactionT"/></a>
                        <div style="clear:both">
                            <div id="showEndpoints" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.cfstats.endpoints"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.cfstats.key"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="endpointKey">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Show" onclick="showEndpoint('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div id="showSSTables" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.cfstats.SSTables"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.cfstats.key"/> <span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="sstablesKey">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Show" onclick="showSSTables('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                <%}}%>
                </tbody>
                </table>
        </div>
    </div>
</fmt:bundle>