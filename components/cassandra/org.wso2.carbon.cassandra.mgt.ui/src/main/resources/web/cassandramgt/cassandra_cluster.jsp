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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.cluster.xsd.NodeInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>
<%
    NodeInformation[] nodes = null;
    String nodeTableDisplay = "display:none;";
    try {
        CassandraClusterAdminClient cassandraClusterAdminClient = new CassandraClusterAdminClient(config.getServletContext(), session);
        nodes = cassandraClusterAdminClient.listNodes();
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

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.nodes"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
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
                    <th width="20%"><fmt:message key="cassandra.node.address"/></th>
                    <th width="20%"><fmt:message key="cassandra.node.state"/></th>
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
                            NodeInformation nodeInformation = nodes[j];
                %>
                <tr id="nodeRaw<%=j%>">
                    <td><%=nodeInformation.getAddress()%>
                    </td>
                    <td><%=nodeInformation.getState()%>
                    </td>
                    <td><%=nodeInformation.getStatus()%>
                    </td>
                    <td><%=nodeInformation.getLoad()%>
                    </td>
                    <td><%=nodeInformation.getToken()%>
                    </td>
                    <td><%=nodeInformation.getOwn()%>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                <input type="hidden" name="nodeCount" id="nodeCount" value="<%=j%>"/>
                </tbody>
            </table>

            <%--<table class="styledLeft">--%>
                <%--<tr>--%>
                    <%--<td>--%>
                        <%--<div style="margin-top:0px;">--%>
                            <%--<a name="addNode"></a>--%>
                            <%--<a class="add-icon-link" href="#addNode">--%>
                                <%--<fmt:message key="cassandra.new.node"/></a>--%>
                        <%--</div>--%>
                    <%--</td>--%>
                <%--</tr>--%>
            <%--</table>--%>
            <script type="text/javascript">
                alternateTableRows('nodeTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>

