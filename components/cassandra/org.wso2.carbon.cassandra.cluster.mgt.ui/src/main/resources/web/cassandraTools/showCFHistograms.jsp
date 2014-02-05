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
<%@ page
        import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterColumnFamilyStatsAdminClient" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyColumnFamilyHistograms" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    ProxyColumnFamilyHistograms[] columnFamilyHistograms=null;
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String columnFamily=request.getParameter(ClusterUIConstants.COLUMN_FAMILY);
    try{
        ClusterColumnFamilyStatsAdminClient clusterColumnFamilyStatsAdminClient=new ClusterColumnFamilyStatsAdminClient(config.getServletContext(),session);
        columnFamilyHistograms=clusterColumnFamilyStatsAdminClient.getColumnFamilyHistograms(hostAddress,keyspace,columnFamily);
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
            label="cassandra.cluster.node.cf.histograms"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.node.cf.histograms" />(<fmt:message key="cassandra.cluster.column.family"/>-<%=columnFamily%>)</h2>
        <div id="workArea">
            <table class="styledLeft" id="nodeInfoTable">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.offset"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.cfstats.SSTables"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.cfstats.writeLatency"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.cfstats.readLatency"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.rowSize"/></th>
                    <th width="10%"><fmt:message key="cassandra.cluster.node.columnCount"/></th>
                </tr>
                </thead>
                <%
                    int i=1;
                    for(ProxyColumnFamilyHistograms c:columnFamilyHistograms){
                        if(i%2==0){
                %>
                <tr class="tableEvenRow">
                            <%}else{%>
                <tr class="tableOddRow">
                    <%}%>
                    <td><%=c.getOffset()%></td>
                    <td><%=c.getSSTables()%></td>
                    <td><%=c.getWriteLatency()%></td>
                    <td><%=c.getReadLatency()%></td>
                    <td><%=c.getRowSize()%></td>
                    <td><%=c.getColumnCount()%></td>
                </tr>
                <%i++;}%>
                <tbody>
            </table>
        </div>
    </div>
</fmt:bundle>