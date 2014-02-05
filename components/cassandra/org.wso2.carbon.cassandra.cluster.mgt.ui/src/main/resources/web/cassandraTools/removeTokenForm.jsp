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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterNodeOperationsAdminClient" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String tokenRemovalStatus = null;
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    Set<String> tokens=(Set<String>)session.getAttribute(ClusterUIConstants.TOKENS);
    try {
        ClusterNodeOperationsAdminClient clusterNodeOperationsAdminClient=new ClusterNodeOperationsAdminClient(config.getServletContext(), session);
        tokenRemovalStatus=clusterNodeOperationsAdminClient.getTokenRemovalStatus(hostAddress);
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
            label="cassandra.nodes"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2>
           <fmt:message key="cassandra.node.removeToken.msg"/>-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%>
        </h2>
        <div id="workArea">

            <table class="styledLeft" id="removeToken">
                <tbody id="nodeBody">
                <tr>
                <td>
                        <fmt:message key="cassandra.node.removeToken.status"/>-<%=tokenRemovalStatus%>
                </td>
                </tr>
                </tbody>
            </table>
            <br />
            <table class="styledLeft" id="removeTokenOptions">
                <thead>
                <tr>
                    <th colspan="1"><fmt:message key="cassandra.node.removeToken.header"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <a href="#" onclick="forceRemoveToken('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/stop.jpg);"><fmt:message key="cassandra.cluster.node.operation.forceRemoveToken" /></a>
                        <a href="#" onclick="showTokenRemovalForm()" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operation.removeToken" /></a>
                        <div style="clear:both">
                            <div id="selectTokenForm" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.node.remove.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.node.remove.name"/><span class="required">*</span></td>
                                        <td>
                                            <select name="availableTokens" id="availableTokens">
                                                <%
                                                    for(String s:tokens)
                                                    {
                                                %>
                                                <option value="<%=s%>"><%=s%></option>
                                                <%}%>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Remove" onclick="removeSelectedToken('<%=hostAddress%>')">
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
            <script type="text/javascript">
                alternateTableRows('nodeTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>

