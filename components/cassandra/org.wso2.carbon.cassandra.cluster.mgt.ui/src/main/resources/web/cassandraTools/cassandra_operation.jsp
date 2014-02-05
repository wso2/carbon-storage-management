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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterNodeStatsAdminClient" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyClusterRingInformation" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    ProxyClusterRingInformation[] nodes = null;
    String nodeTableDisplay = "display:none;";
    Set<String> tokens=null;
    Set<String> dataCenters=null;
    try {
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(), session);
        nodes = clusterNodeStatsAdminClient.getRing(null,null);
        if (nodes != null && nodes.length > 0) {
            tokens=new HashSet<String>();
            dataCenters=new HashSet<String>();
            nodeTableDisplay = "";
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
            label="cassandra.cluster.node.operation.msg"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2>
            <h2><fmt:message key="cassandra.nodes.operation.msg"/></h2>
        </h2>
        <div id="workArea">

            <table class="styledLeft" id="nodeTable" style="<%=nodeTableDisplay%>">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="cassandra.cluster.node"/></th>
                    <th width="10%"><fmt:message key="cassandra.node.status"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.actions"/></th>
                </tr>
                </thead>
                <tbody id="nodeBody">
                <%
                    int j = 0;
                    if (nodes != null && nodes.length != 0) {
                        int activeNodes=0;
                        for (; j < nodes.length; j++) {
                            ProxyClusterRingInformation nodeInformation = nodes[j];

                %>
                <tr id="nodeRaw<%=j%>">
                    <td>
                        <%if("unknown".equalsIgnoreCase(nodeInformation.getEndPoint())){%>
                        <%=nodeInformation.getAddress()%>
                        <%}else{%>
                        <%=nodeInformation.getEndPoint()%>
                        <%}%>
                    </td>
                    <td><%=nodeInformation.getStatus()%>
                    </td>
                    <td>
                        <a class="view-icon-link" href="#" onclick="displayNodeOperations('<%=nodeInformation.getAddress()%>','<%=nodeInformation.getToken()%>','<%=nodeInformation.getEndPoint()%>','<%=nodeInformation.getStatus()%>');"><fmt:message key="cassandra.node.view"/></a>
                    </td>
                </tr>
                <%
                        tokens.add(nodeInformation.getToken());
                        dataCenters.add(nodeInformation.getDataCenter());
                        if(nodeInformation.getStatus().equalsIgnoreCase(ClusterUIConstants.UP))
                            activeNodes++;
                    }
                %>
                <%
                        session.setAttribute(ClusterUIConstants.NODE_COUNT,activeNodes);
                        session.setAttribute(ClusterUIConstants.TOKENS,tokens);
                        session.setAttribute(ClusterUIConstants.DATA_CENTERS,dataCenters);

                    }
                %>
                </tbody>
            </table>
            <script type="text/javascript">
                alternateTableRows('nodeTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>

