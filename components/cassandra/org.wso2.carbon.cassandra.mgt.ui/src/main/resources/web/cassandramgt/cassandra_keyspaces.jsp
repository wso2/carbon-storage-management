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
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.AuthorizedRolesInformation" %>
<%@ page import="org.wso2.carbon.cassandra.common.auth.Action" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>
<%
    KeyspaceInformation[] keyspaces = null;
    String ksTableDisplay = "display:none;";
    String[] userRoles = new String[0];
    String[] allowedRolesCreate = new String[0];
    String[] allowedRolesAlter = new String[0];
    String[] allowedRolesDrop = new String[0];
    String[] allowedRolesSelect = new String[0];
    String[] allowedRolesModify = new String[0];
    String[] allowedRolesAuthorize = new String[0];
    String[] environments = null;
    String envName = null;
    AuthorizedRolesInformation[] rolePermissions = new AuthorizedRolesInformation[0];
    try {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);

        envName = request.getParameter("envName");
        if(envName == null){
            envName = (String) session.getAttribute("envName");
            if(envName == null){
                envName = "DEFAULT";
            }
        }
        session.setAttribute("envName", envName);
        environments = (String[]) session.getAttribute("environments");
        if(environments == null){
            environments = cassandraKeyspaceAdminClient.getAllEnvironments();
            session.setAttribute("environments", environments);
        }

        keyspaces = cassandraKeyspaceAdminClient.listKeyspacesOfCurrentUSer(envName);
        if (keyspaces != null && keyspaces.length > 0) {
            ksTableDisplay = "";
        }
        userRoles = cassandraKeyspaceAdminClient.getAllRoles();
        String resourcePath = CassandraAdminClientConstants.CASSANDRA_RESOURCE_ROOT + "/" + envName;
        rolePermissions = cassandraKeyspaceAdminClient.getResourcePermissionsOfRoles(resourcePath);
        if(rolePermissions == null){
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
            label="cassandra.keyspaces.msg"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2>
            <h2><fmt:message key="cassandra.keyspaces.msg"/></h2>
        </h2>
        <div id="workArea">
            <div>
                <fmt:message key="cassandra.environment.name"/>
                <select id="envCombo" name="envCombo" onchange="onComboChange(this)">
                <%
                    if (envName == null) {
                        envName = environments[0];
                    }
                    for (String env : environments) {
                        if(env == null) continue;
                        if (env.equals(envName.trim())) {
                            %>
                            <option id="<%=env%>" value="<%=env%>" selected="selected"><%=env%>
                            </option>
                            <%
                        } else {
                            %>
                            <option id="<%=env%>" value="<%=env%>"><%=env%>
                            </option>
                            <%
                        }
                    }
                %>
                </select><br><br>
            </div>
            <%
            if(keyspaces == null || keyspaces.length == 0){
            %>
                <div>No Keyspaces Available</div>
            <%
            }
            %>
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td class="formRaw">
                        <table class="styledLeft" id="keyspaceTable" style="<%=ksTableDisplay%>">
                            <thead>
                            <tr>
                                <th width="15%"><fmt:message key="cassandra.keyspace.name"/></th>
                                <th width="10%"><fmt:message key="cassandra.cluster.name"/></th>
                                <th width="45%"><fmt:message key="cassandra.actions"/></th>
                            </tr>
                            </thead>
                            <tbody id="keyspaceBody">
                            <%
                                int j = 0;
                                if (keyspaces != null && keyspaces.length != 0) {
                                    for (; j < keyspaces.length; j++) {
                                        String name = keyspaces[j].getName();
                                        if(name.equals("system_auth") || name.equals("system_traces") ||
                                            name.equals("system") || name.equals("definitions")){
                                        %>
                                            <tr id="keyspaceRaw<%=j%>">
                                                <td id="keyspaceTD<%=j%>">
                                                    <a id="keyspaceTDLink<%=j%>"
                                                       onclick="location.href = 'keyspace_dashboard.jsp?name=' + '<%=name%>' + '&cluster=' + '<%=keyspaces[j].getClusterName()%>';"
                                                       href="#"><%=name%>
                                                    </a>
                                                </td>
                                                <td>
                                                    <div><%=keyspaces[j].getClusterName()%></div>
                                                </td>
                                                <td>
                                                   <div>N/A</div>
                                                </td>
                                            </tr>
                                        <%
                                        }else{
                                    %>
                                        <tr id="keyspaceRaw<%=j%>">
                                            <td id="keyspaceTD<%=j%>">
                                                <a id="keyspaceTDLink<%=j%>"
                                                   onclick="location.href = 'keyspace_dashboard.jsp?name=' + '<%=name%>' + '&cluster=' + '<%=keyspaces[j].getClusterName()%>';"
                                                   href="#"><%=name%>
                                                </a>
                                            </td>
                                            <td>
                                                <div><%=keyspaces[j].getClusterName()%></div>
                                            </td>
                                            <td>
                                                <input type="hidden" name="keyspaceName<%=j%>" id="keyspaceName<%=j%>" value="<%=name%>"/>
                                                <input type="hidden" name="clusterName<%=j%>" id="clusterName<%=j%>" value="<%=keyspaces[j].getClusterName()%>"/>
                                                <%
                                                if(rolePermissions.length != 0){
                                                %>
                                                <a class="edit-icon-link" href="#"
                                                   onclick="location.href = 'keyspace_dashboard.jsp?name=<%=name%>&cluster=<%=keyspaces[j].getClusterName()%>&setPermissions=true#permissionArea';"
                                                   href="#"><fmt:message
                                                        key="cassandra.actions.share"/></a>
                                                <%}%>
                                                <a class="edit-icon-link" href="#"
                                                   onclick="location.href = 'add_edit_keyspace.jsp?region=region1&item=cassandra_ks_mgt_create_menu&mode=edit&name=<%=name%>&cluster=<%=keyspaces[j].getClusterName()%>';">
                                                   <fmt:message key="cassandra.actions.edit"/></a>
                                                <a class="delete-icon-link"
                                                   onclick="deleteKeyspace('<%=j%>');"
                                                   href="#"><fmt:message
                                                        key="cassandra.actions.delete"/></a>
                                            </td>
                                        </tr>
                                    <%
                                        }
                                    }
                                }
                            %>
                            <input type="hidden" name="keyspaceCount" id="keyspaceCount" value="<%=j%>"/>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>
            <script type="text/javascript">
                alternateTableRows('keyspaceTable', 'tableEvenRow', 'tableOddRow');
            </script>
        <br> <br>
    <%
    if(rolePermissions.length != 0){
    %>
        <h2 id="permissionArea">Permissions for All Keyspaces</h2>
        <form method="post" action="share_root-processor.jsp" name="dataForm">
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
                               <input class="button" type="submit" value="Save">
                               <input id="cancelKSButton" class="button" name="cancelKSButton" type="button" href="#"
                                       onclick="location.href = 'cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu#permissionArea';"
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
        <script type="text/javascript">
            function onComboChange(combo) {
                var opt = combo.options[combo.selectedIndex].value;
                window.location = 'cassandra_keyspaces.jsp?envName=' + opt;
            }
        </script>
</fmt:bundle>
