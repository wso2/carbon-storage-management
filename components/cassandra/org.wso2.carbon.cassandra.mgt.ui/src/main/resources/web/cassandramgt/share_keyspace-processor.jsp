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
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.common.auth.Action" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.AuthorizedRolesInformation" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<%
    String keyspaceName = request.getParameter("keyspaceName");
    String envName = (String) session.getAttribute("envName");
    String clusterName = (String) session.getAttribute("clusterName");

    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        String path = CassandraAdminClientConstants.CASSANDRA_RESOURCE_ROOT + "/" +
            envName + "/" + clusterName + "/" + keyspaceName;
        String[] allowedRolesCreate = new String[0];
        String[] allowedRolesAlter = new String[0];
        String[] allowedRolesDrop = new String[0];
        String[] allowedRolesSelect = new String[0];
        String[] allowedRolesModify = new String[0];
        String[] allowedRolesAuthorize = new String[0];
        AuthorizedRolesInformation[] rolePermissions = cassandraKeyspaceAdminClient.getResourcePermissionsOfRoles(path);
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
        String[] userRoles = cassandraKeyspaceAdminClient.getAllRoles();
        String[] actions = Action.ALL_ACTIONS_ARRAY;

        List<AuthorizedRolesInformation> setPermissionList = new ArrayList<AuthorizedRolesInformation>();
        List<AuthorizedRolesInformation> clearPermissionList = new ArrayList<AuthorizedRolesInformation>();

        for (String userRole : userRoles) {
            for(String currentAction : actions){
                String sharedRoleActionUpdated = request.getParameter(userRole + currentAction);
                AuthorizedRolesInformation info = new AuthorizedRolesInformation();
                info.setResource(path);
                info.setPermission(currentAction);
                info.setAuthorizedRoles(new String[]{userRole});
                if (sharedRoleActionUpdated != null && !sharedRoleActionUpdated.isEmpty()) {
                    setPermissionList.add(info);
                }else {
                    clearPermissionList.add(info);
                }
            }
        }

        AuthorizedRolesInformation[] setPermissionArray =
                setPermissionList.toArray(new AuthorizedRolesInformation[setPermissionList.size()]);
        AuthorizedRolesInformation[] clearPermissionArray =
                clearPermissionList.toArray(new AuthorizedRolesInformation[clearPermissionList.size()]);

        cassandraKeyspaceAdminClient.authorizeRolesForResource(setPermissionArray);
        cassandraKeyspaceAdminClient.clearResourcePermissions(clearPermissionArray);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
     var callbackUrl = "keyspace_dashboard.jsp?name=<%=keyspaceName%>&cluster=<%=clusterName%>#permissionArea";
     showErrorDialog('<%=e.getMessage()%>', callbackUrl);
 </script>
<%}%>

<script type="text/javascript">
   window.location = "keyspace_dashboard.jsp?name=<%=keyspaceName%>&cluster=<%=clusterName%>#permissionArea";
</script>