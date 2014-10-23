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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.TokenRangeInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.AuthorizedRolesInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.common.auth.Action" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%--<jsp:include page="../dialog/display_messages.jsp"/>--%>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.keyspace"
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("name");
    String setPermissions = request.getParameter("setPermissions");
    String envName = (String) session.getAttribute("envName");
    String clusterName = request.getParameter("cluster");
    session.setAttribute("clusterName", clusterName);

    String cassandraClusterName = null;
    String[] userRoles = new String[0];
    String[] allowedRolesCreate = new String[0];
    String[] allowedRolesAlter = new String[0];
    String[] allowedRolesDrop = new String[0];
    String[] allowedRolesSelect = new String[0];
    String[] allowedRolesModify = new String[0];
    String[] allowedRolesAuthorize = new String[0];
    AuthorizedRolesInformation[] rolePermissions = new AuthorizedRolesInformation[0];
    if (keyspace != null && !"".equals(keyspace.trim())) {
        keyspace = keyspace.trim();
        KeyspaceInformation keyspaceInformation = null;
        TokenRangeInformation[] tokenRangeInformations = null;
        try {
            session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
            keyspaceInformation = CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(),
                                                                                    session, keyspace);
            CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient =
                    new CassandraKeyspaceAdminClient(config.getServletContext(), session);
            tokenRangeInformations = cassandraKeyspaceAdminClient.getTokenRange(envName, clusterName, keyspace);
            cassandraClusterName = cassandraKeyspaceAdminClient.getClusterName(envName, clusterName);
            userRoles = cassandraKeyspaceAdminClient.getAllRoles();
            String resourcePath = CassandraAdminClientConstants.CASSANDRA_RESOURCE_ROOT + "/" + envName +
                                        "/" + clusterName + "/" + keyspace;
            rolePermissions = cassandraKeyspaceAdminClient.getResourcePermissionsOfRoles(resourcePath);
            if(rolePermissions == null){
                if(setPermissions != null){
                    %>
                    <script type="text/javascript">
                        var callbackUrl = "cassandra_keyspaces.jsp";
                        showErrorDialog('You are not authorized to set permissions.' , callbackUrl);
                    </script>
                    <%
                }
                rolePermissions = new AuthorizedRolesInformation[0];
            }
            for(AuthorizedRolesInformation info : rolePermissions){
                String permission = info.getPermission();
                String[] roles = info.getAuthorizedRoles();
                if(roles == null){
                    roles = new String[0];
                }
                if(Action.ACTION_CREATE.equals(permission)){
                    allowedRolesCreate = roles;
                } else if(Action.ACTION_ALTER.equals(permission)){
                    allowedRolesAlter = roles;
                } else if(Action.ACTION_DROP.equals(permission)){
                    allowedRolesDrop = roles;
                } else if(Action.ACTION_SELECT.equals(permission)){
                    allowedRolesSelect = roles;
                } else if(Action.ACTION_MODIFY.equals(permission)){
                    allowedRolesModify = roles;
                } else if(Action.ACTION_AUTHORIZE.equals(permission)){
                    allowedRolesAuthorize = roles;
                }
            }
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg); %>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
    }
    assert keyspaceInformation != null;
    String alias = CassandraAdminClientHelper.getAliasForReplicationStrategyClass(keyspaceInformation.getStrategyClass());
    List endPoints = CassandraAdminClientHelper.getCassandraEndPointList();
%>
<div id="middle">
    <h2><fmt:message key="cassandra.keyspace.dashboard"/> (<%=envName%> > <%=clusterName%> > <%=keyspace%>) </h2>

    <div id="workArea">

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <td width="50%">
                    <table class="styledLeft" id="keyspaceInfoTable" style="margin-left: 0px;"
                           width="100%">
                        <thead>
                        <tr>
                            <th colspan="2" align="left"><fmt:message
                                    key="cassandra.keyspace.details"/></th>
                        </tr>
                        </thead>
                        <tr>
                            <td width="30%"><fmt:message key="cassandra.cluster.name"/></td>
                            <td><%=cassandraClusterName%>
                            </td>
                        </tr>
                        <tr>
                            <td width="30%"><fmt:message key="cassandra.keyspace.name"/></td>
                            <td><%=keyspaceInformation.getName()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.ReplicationStrategy"/></td>
                            <td align="left">
                                <% if (CassandraAdminClientConstants.OLD_NETWORK.equals(alias)) { %>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.oldnetwork"/>
                                <%} else if (CassandraAdminClientConstants.NETWORK.equals(alias)) {%>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.network"/>
                                <%} else {%>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.simple"/>
                                <% } %>
                            </td>
                        </tr>
                        <tr>
                            <% if (CassandraAdminClientConstants.NETWORK.equals(alias)) {  %>
                                <td><fmt:message key="cassandra.field.ReplicationFactors"/></td>
                                <td align="left">
                                    <%
                                        String[] options = keyspaceInformation.getStrategyOptions();
                                        String optionString = "";
                                        if(options != null && options.length != 0){
                                            for(String option : options){
                                                String key = option.substring(0,option.lastIndexOf("_"));
                                                String value = option.substring(option.lastIndexOf("_") + 1);
                                                optionString = optionString + " , " + key + ":" + value;
                                            }
                                            if(optionString.length() > 3){
                                                optionString = optionString.substring(3);
                                            }
                                        }
                                    %>
                                    <%=optionString%>
                                </td>
                            <%} else {%>
                                <td><fmt:message key="cassandra.field.ReplicationFactor"/></td>
                                <td><%=keyspaceInformation.getReplicationFactor()%></td>
                            <% } %>
                        </tr>
                    </table>
                </td>
                <%

                    if (tokenRangeInformations != null && tokenRangeInformations.length > 0) {
                %>
                <td width="10px">&nbsp;</td>
                <td>

                    <div id="tokenRanageTableDIv">
                        <table class="styledLeft" id="tokenRanageTable"
                               style="margin-left: 0px;" width="100%">
                            <thead>
                            <tr>
                                <th>
                                    <fmt:message key="cassandra.keyspac.endponts"/>
                                </th>
                                    <%--<th>--%>
                                    <%--<fmt:message key="cassandra.keyspac.starttoken"/>--%>
                                    <%--</th>--%>
                                    <%--<th>--%>
                                    <%--<fmt:message key="cassandra.keyspac.endtoken"/>--%>
                                    <%--</th>--%>
                            </tr>
                            </thead>
                            <% if (!endPoints.isEmpty()) {
                                for (Iterator ep = endPoints.iterator(); ep.hasNext(); ) {
                            %>
                            <tr>
                                <td><%=ep.next().toString()%>
                                </td>
                            </tr>
                            <%
                                }
                            %>
                            <%} else {%>

                            <% for (TokenRangeInformation rangeInformation : tokenRangeInformations) {%>
                            <tr>
                                <td>
                                    <%
                                        String[] eps = rangeInformation.getEndpoints();
                                        String epsAsString = "";
                                        if (eps != null && eps.length > 0) {
                                            for (String ep : eps) {
                                                epsAsString += "," + ep;
                                            }
                                        }
                                        epsAsString = epsAsString.substring(1);
                                    %>
                                    <%=epsAsString%>
                                </td>
                                    <%--<td>--%>
                                    <%--<%=rangeInformation.getStartToken()%>--%>
                                    <%--</td>--%>
                                    <%--<td>--%>
                                    <%--<%=rangeInformation.getEndToken()%>--%>
                                    <%--</td>--%>
                            </tr>
                            <% }%>
                            <%} %>
                        </table>
                    </div>
                </td>
                <% } %>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <%
                    ColumnFamilyInformation[] columnFamilies = keyspaceInformation.getColumnFamilies();
                    String cfTableDisplay = "display:none;";
                    if (columnFamilies != null && columnFamilies.length != 0) {
                        cfTableDisplay = "";
                    }
                %>

                <td width="50%">
                    <div id="serviceClientDiv" style="<%=cfTableDisplay%>">
                        <table class="styledLeft" id="cfTable" style="margin-left: 0px;"
                               width="100%">
                            <thead>
                            <tr>
                                <th width="20%"><fmt:message key="cassandra.cf.name"/></th>
                                <% if(!keyspace.equals("system") && !keyspace.equals("system_auth")
                                    && !keyspace.equals("system_traces")) {%>
                                <th width="60%"><fmt:message key="cassandra.actions"/></th>
                                <% } %>
                            </tr>
                            </thead>
                            <tbody id="cfBody">
                            <%
                                int j = 0;
                                if (columnFamilies != null && columnFamilies.length != 0) {
                                    for (; j < columnFamilies.length; j++) {
                                        ColumnFamilyInformation columnFamily = columnFamilies[j];
                                        String name = columnFamily.getName();
                            %>
                            <tr id="cfRaw<%=j%>">
                                <td id="clTD<%=j%>">
                                    <a id="clTDLink<%=j%>"
                                       onclick="viewCLs('<%=keyspace%>','<%=name%>');"
                                       href="#"><%=name%>
                                    </a>
                                </td>
                                <% if(!keyspace.equals("system") && !keyspace.equals("system_auth")
                                    && !keyspace.equals("system_traces")) {%>
                                <td>
                                     <input type="hidden" name="cfName<%=j%>" id="cfName<%=j%>" value="<%=name%>"/>
                                 <%
                                 if(rolePermissions.length != 0){
                                 %>
                                    <a class="edit-icon-link"
                                       onclick="location.href = 'cf_dashboard.jsp?keyspaceName=<%=keyspace%>&cfName=<%=name%>&setPermissions=true#permissionArea';"
                                       href="#"><fmt:message
                                            key="cassandra.actions.share"/></a>
                                 <%
                                 }
                                 %>
                                    <a class="edit-icon-link"
                                       onclick="showCFEditor('<%=keyspace%>','<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.edit"/></a>
                                    <a class="delete-icon-link"
                                       onclick="deletecf('<%=clusterName%>','<%=keyspace%>','<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.delete"/></a>
                                </td>
                                <%}%>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            <input type="hidden" name="cfCount" id="cfCount" value="<%=j%>"/>
                            </tbody>
                        </table>
                    </div>
                    <% if(!keyspace.equals("system") && !keyspace.equals("system_auth")
                        && !keyspace.equals("system_traces")) {%>
                    <div style="margin-top:0px;">
                        <a class="add-icon-link" onclick="addcf('<%=keyspace%>','<%=clusterName%>');" href="#">
                            <fmt:message key="cassandra.add.new.cf"/></a>
                    </div>
                    <%}%>
                </td>
            </tr>
        </table>

        <form name="dataForm" method="post" action="">
            <input name="backURL" type="hidden" id="hiddenField" value="">
        </form>

        <script type="text/javascript">
            alternateTableRows('keyspaceInfoTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('cfTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <br> <br>
    <%
    if(rolePermissions.length != 0){
    %>
        <h2 id="permissionArea">Permissions for Keyspace : <%=keyspace%></h2>
        <form method="post" action="share_keyspace-processor.jsp" name="dataForm">
            <table class="styledLeft" id="ksRoleTable" style="margin-left: 0px;" width="100%">
                <thead>
                    <tr>
                        <th>Role</th>
                        <th>Permission</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (String userRole : userRoles) {
                    %>
                    <tr>
                        <td>
                            <%=userRole%>
                        </td>
                        <td>
                            <%
                                boolean checkedStatusCreate = false;
                                for (String allowedUserCreate : allowedRolesCreate) {
                                    if (allowedUserCreate.equalsIgnoreCase(userRole)) {
                                        checkedStatusCreate = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_CREATE%>"
                                       value="createRight" <%if(checkedStatusCreate){%>
                                       checked <%}%> >
                                Create&nbsp;&nbsp;&nbsp;&nbsp;
                                <%
                                boolean checkedStatusAlter = false;
                                for (String allowedUserAlter : allowedRolesAlter) {
                                    if (allowedUserAlter.equalsIgnoreCase(userRole)) {
                                        checkedStatusAlter = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_ALTER%>"
                                value="AlterRight" <%if(checkedStatusAlter){%>
                                    checked <%}%> >
                                Alter&nbsp;&nbsp;&nbsp;&nbsp;
                                <%
                                boolean checkedStatusDrop = false;
                                for (String allowedUserDrop : allowedRolesDrop) {
                                    if (allowedUserDrop.equalsIgnoreCase(userRole)) {
                                        checkedStatusDrop = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_DROP%>"
                                value="DropRight" <%if(checkedStatusDrop){%>
                                    checked <%}%> >
                                Drop&nbsp;&nbsp;&nbsp;&nbsp;
                                <%
                                boolean checkedStatusSelect = false;
                                for (String allowedUserSelect : allowedRolesSelect) {
                                    if (allowedUserSelect.equalsIgnoreCase(userRole)) {
                                        checkedStatusSelect = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_SELECT%>"
                                value="SelectRight" <%if(checkedStatusSelect){%>
                                    checked <%}%> >
                                Select&nbsp;&nbsp;&nbsp;&nbsp;
                                <%
                                boolean checkedStatusModify = false;
                                for (String allowedUserModify : allowedRolesModify) {
                                    if (allowedUserModify.equalsIgnoreCase(userRole)) {
                                        checkedStatusModify = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_MODIFY%>"
                                value="ModifyRight" <%if(checkedStatusModify){%>
                                    checked <%}%> >
                                Modify&nbsp;&nbsp;&nbsp;&nbsp;
                                <%
                                boolean checkedStatusAuthorize = false;
                                for (String allowedUserAuthorize : allowedRolesAuthorize) {
                                    if (allowedUserAuthorize.equalsIgnoreCase(userRole)) {
                                        checkedStatusAuthorize = true;
                                    }
                                }
                                %>
                                <input type="checkbox" name="<%=userRole + Action.ACTION_AUTHORIZE%>"
                                value="AuthorizeRight" <%if(checkedStatusAuthorize){%>
                                    checked <%}%> >
                                Authorize
                            </td>
                        </tr>
                    <%
                        }
                    %>
                    <tr>
                        <td class="buttonRow" colspan="2" style="padding-top:10px;">
                           <input type="hidden" name="keyspaceName" id="keyspaceName" value="<%=keyspace%>"/>
                           <input class="button" type="submit" value="Save">
                           <input id="cancelKSButton" class="button" name="cancelKSButton" type="button" href="#"
                                   onclick="location.href = 'keyspace_dashboard.jsp?name=<%=keyspace%>&cluster=<%=clusterName%>#permissionArea';"
                                   value="<fmt:message key="cassandra.actions.cancel"/>"/>
                        </td>
                    </tr>
                </tbody>
            </table>
            <br>
        </form>
    <%
    }
    %>
    </div>
</div>
<%}%>
</fmt:bundle>
