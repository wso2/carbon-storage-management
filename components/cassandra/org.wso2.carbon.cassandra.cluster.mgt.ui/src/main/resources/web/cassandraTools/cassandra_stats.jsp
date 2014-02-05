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
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.stub.data.xsd.ClusterRingInformation" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.info.ClusterRingInfoAdminClient" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyClusterRingInformation" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterNodeStatsAdminClient" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    ProxyClusterRingInformation[] nodes = null;
    String nodeTableDisplay = "display:none;";
    List<String> availableHosts=new ArrayList<String>();
    try {
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient=new ClusterNodeStatsAdminClient(config.getServletContext(), session);
        nodes = clusterNodeStatsAdminClient.getRing(null,null);
        if (nodes != null && nodes.length > 0) {
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
            <h2><fmt:message key="cassandra.nodes.msg"/></h2>
        </h2>
        <div id="workArea">

            <table class="styledLeft" id="nodeTable" style="<%=nodeTableDisplay%>">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cluster.node"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.state"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.datacenter"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.rack"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.status"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.load"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.token"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.own"/></th>
                </tr>
                </thead>
                <tbody id="nodeBody">
                <%
                    int j = 0;
                    if (nodes != null && nodes.length != 0) {
                        for (; j < nodes.length; j++) {
                            ProxyClusterRingInformation nodeInformation = nodes[j];
                            availableHosts.add(nodeInformation.getAddress());

                %>
                <tr id="nodeRaw<%=j%>">
                    <td>
                    <a href="#" onclick="displayNodeStats('<%=nodeInformation.getAddress()%>','<%=nodeInformation.getToken()%>','<%=nodeInformation.getEndPoint()%>','<%=nodeInformation.getStatus()%>')" ><%if("unknown".equalsIgnoreCase(nodeInformation.getEndPoint())){%>
                        <%=nodeInformation.getAddress()%>
                        <%}else{%>
                        <%=nodeInformation.getEndPoint()%>
                        <%}%></a>
                    </td>
                    <td><%=nodeInformation.getState()%>
                    </td>
                    <td><%=nodeInformation.getDataCenter()%>
                    </td>
                    <td><%=nodeInformation.getRack()%>
                    </td>
                    <td><%=nodeInformation.getStatus()%>
                    </td>
                    <td><%=nodeInformation.getLoad()%>
                    </td>
                    <td><%=nodeInformation.getToken()%>
                    </td>
                    <td><%=nodeInformation.getEffectiveOwnership()%>
                    </td>
                </tr>
                <%
                        }
                    }
                    session.setAttribute(ClusterUIConstants.NODE_COUNT,j);
                    session.setAttribute(ClusterUIConstants.HOSTS,availableHosts);
                %>
                <input type="hidden" name="nodeCount" id="nodeCount" value="<%=j%>"/>
                </tbody>
            </table>
            <script type="text/javascript">
                alternateTableRows('nodeTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>

