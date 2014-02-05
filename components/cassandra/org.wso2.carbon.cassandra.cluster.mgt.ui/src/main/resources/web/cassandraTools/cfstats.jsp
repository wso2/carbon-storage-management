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
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyKeyspaceInfo" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    ProxyKeyspaceInfo[] keyspaceInfo=null;
    try{
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        keyspaceInfo=clusterNodeStatsAdminClient.getCfstats(hostAddress);

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


<fmt:bundle basename="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources">   t
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.cluster.node.stats.CFStats"
        resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script>
    jQuery(document).ready(function () {
        initSections("hidden");
    });

</script>
<style>
    .sectionSeperator {
        margin-bottom: 5px;
    }

    .sectionSub {
        padding: 0;
        margin: 0 0 10px 0;
    }
</style>

<div id="middle">
<h2><fmt:message key="cassandra.cluster.nodeInfo.cfstats" />-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
    <%=hostName%>
    <%}else{%>
    <%=hostAddress%>
    <%}%></h2>
<div id="workArea">
<%
    for(ProxyKeyspaceInfo k:keyspaceInfo){
%>
<div class="sectionSeperator togglebleTitle"><%=k.getKeyspaceName()%></div>
<div class="sectionSub">
<div class="sectionSeperator togglebleTitle" style="margin-left: 20px"><fmt:message key="cassandra.cluster.cfstats.keyspaceInfo"/> </div>
<div class="sectionSub" style="margin-left: 20px">
    <table id="keyspaceInfo<%=k.getKeyspaceName()%>" class="styledLeft">
        <tbody>
        <tr>
            <td>
                <fmt:message key="cassandra.cluster.cfstats.readCount"/>
            </td>
            <td>
                <%=k.getTableReadCount()%>
            </td>
        </tr>
        <tr class="tableEvenRow">
            <td>
                <fmt:message key="cassandra.cluster.cfstats.readLatency"/>(ms)
            </td>
            <td>
                <%=k.getTableReadLatency()%>
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="cassandra.cluster.cfstats.writeCount"/>
            </td>
            <td>
                <%=k.getTableWriteCount()%>
            </td>
        </tr>
        <tr class="tableEvenRow">
            <td>
                <fmt:message key="cassandra.cluster.cfstats.writeLatency"/> (ms)
            </td>
            <td>
                <%=k.getTableWriteLatency()%>
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="cassandra.cluster.cfstats.pendingTasks"/>
            </td>
            <td>
                <%=k.getTablePendingTasks()%>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div class="sectionSeperator togglebleTitle" style="margin-left: 20px">Column Family Info</div>
<div class="sectionSub" style="margin-left: 20px">
    <%ProxyColumnFamilyInformation[] columnFamilyInformations=k.getProxyColumnFamilyInformations();
        for(ProxyColumnFamilyInformation c:columnFamilyInformations)
        {
    %>
    <div class="sectionSeperator togglebleTitle" style="margin-left: 40px"><%=c.getColumnFamilyName()%></div>
    <div class="sectionSub" style="margin-left: 40px">
        <table id="ColumnFamilyInfo<%=c.getColumnFamilyName()%>" class="styledLeft">
            <tbody>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.SSTableCount"/>
                </td>
                <td>
                    <%=c.getSSTableCount()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.spaceUsed"/>(live)
                </td>
                <td>
                    <%=c.getLiveDiskSpaceUsed()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.spaceUsed"/>(total)
                </td>
                <td>
                    <%=c.getTotalDiskSpaceUsed()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.noOfKeys"/> (estimate)
                </td>
                <td>
                    <%=c.getNumberOfKeys()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.memtableColumnCount"/>
                </td>
                <td>
                    <%=c.getMemtableColumnsCount()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.memtableDataSize"/>
                </td>
                <td>
                    <%=c.getMemtableDataSize()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.memtableSwitchCount"/>
                </td>
                <td>
                    <%=c.getMemtableSwitchCount()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.readCount"/>
                </td>
                <td>
                    <%=c.getReadCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.readLatency"/>(ms)
                </td>
                <td>
                    <%=c.getReadLatency()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.writeCount"/>
                </td>
                <td>
                    <%=c.getWriteCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.writeLatency"/>(ms)
                </td>
                <td>
                    <%=c.getWriteLatency()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.pendingTasks"/>
                </td>
                <td>
                    <%=c.getPendingTasks()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.bloomFilterFalsePosiives"/>
                </td>
                <td>
                    <%=c.getBloomFilterFalsePostives()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.bloomFilterFalseRatio"/>
                </td>
                <td>
                    <%=c.getBloomFilterFalseRatio()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.bloomFilterSpaceUsed"/>
                </td>
                <td>
                    <%=c.getBloomFilterSpaceUsed()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.compactedRowMinSize"/>
                </td>
                <td>
                    <%=c.getCompactedRowMinimumSize()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.compactedRowMaxSize"/>
                </td>
                <td>
                    <%=c.getCompactedRowMaximumSize()%>
                </td>
            </tr>
            <tr class="tableEvenRow">
                <td>
                    <fmt:message key="cassandra.cluster.cfstats.compactedRowMeanSize"/>
                </td>
                <td>
                    <%=c.getCompactedRowMeanSize()%>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <%}%>
</div>
</div>
<%}%>
</div>
</div>
</fmt:bundle>