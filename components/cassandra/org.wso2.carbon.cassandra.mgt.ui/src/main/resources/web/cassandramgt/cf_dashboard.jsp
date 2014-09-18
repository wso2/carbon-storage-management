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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.cluster.xsd.ColumnFamilyStats" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraClusterAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.AuthorizedRolesInformation" %>
<%@ page import="org.wso2.carbon.cassandra.common.auth.Action" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.cf"
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keyspaceName");
    String columnFamily = request.getParameter("cfName");
    String setPermissions = request.getParameter("setPermissions");
    String envName = (String) session.getAttribute("envName");
    String clusterName = (String) session.getAttribute("clusterName");
    ColumnFamilyInformation cfInformation = null;
    ColumnFamilyStats columnFamilyStats = null;
    String[] userRoles = new String[0];
    String[] allowedRolesCreate = new String[0];
    String[] allowedRolesAlter = new String[0];
    String[] allowedRolesDrop = new String[0];
    String[] allowedRolesSelect = new String[0];
    String[] allowedRolesModify = new String[0];
    String[] allowedRolesAuthorize = new String[0];
    AuthorizedRolesInformation[] rolePermissions = new AuthorizedRolesInformation[0];

    if (keyspace != null && !"".equals(keyspace.trim()) && columnFamily != null && !"".equals(columnFamily.trim())) {

        try {
            keyspace = keyspace.trim();
            columnFamily = columnFamily.trim();
            KeyspaceInformation keyspaceInformation =
                    CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
            cfInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, columnFamily);
            CassandraClusterAdminClient cassandraClusterAdminClient =
                    new CassandraClusterAdminClient(config.getServletContext(), session);
            columnFamilyStats = cassandraClusterAdminClient.getColumnFamilyStats(keyspace, columnFamily);
            CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient =
                new CassandraKeyspaceAdminClient(config.getServletContext(), session);
            userRoles = cassandraKeyspaceAdminClient.getAllRoles();
            String resourcePath = CassandraAdminClientConstants.CASSANDRA_RESOURCE_ROOT + "/" + envName + "/"
                                        + clusterName + "/" + keyspace + "/" + columnFamily;
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
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
            %>
            <script type="text/javascript">
                window.location.href = "../admin/error.jsp";
            </script>
        <%
        }
        %>

    <% if (cfInformation != null) {
        boolean isSuperCl = CassandraAdminClientConstants.COLUMN_TYPE_SUPER.equals(cfInformation.getType());
        String comparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getComparatorType());
        String subComparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getSubComparatorType());
        String validationClass =
                CassandraAdminClientHelper.getAliasForValidatorTypeClass(cfInformation.getDefaultValidationClass());
        String keyValidationClass =
                CassandraAdminClientHelper.getAliasForValidatorTypeClass(cfInformation.getKeyValidationClass());
    %>
    <div id="middle">
        <h2><fmt:message key="cassandra.cf.dashboard"/> ( <%=envName%> > <%=clusterName%> > <%=keyspace%>
                                                    > <%=columnFamily%>) </h2>
        <div id="workArea">
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                <td width="50%" colspan="2">
                    <table class="styledLeft" id="cfInfoTable" style="margin-left: 0px;" width="100%">
                        <thead>
                        <tr>
                            <th colspan="2" align="left"><fmt:message key="cassandra.cf.details"/></th>
                        </tr>
                        </thead>
                        <tr>
                            <td><fmt:message key="cassandra.field.name"/></td>
                            <td align="left"><%=cfInformation.getName()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.comment"/></td>
                            <td align="left"><%=cfInformation.getComment()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.columnType"/></td>
                            <td align="left">
                                <% if (isSuperCl) {%>
                                <fmt:message key="cassandra.field.columnType.super"/>
                                <% } else { %>
                                <fmt:message
                                        key="cassandra.field.columnType.standard"/>
                                <% } %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.keyValidationclass"/></td>
                            <td align="left">
                                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(keyValidationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.ascii"/>
                                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(keyValidationClass)) {%>
                                <fmt:message
                                    key="cassandra.field.comparator.utf8"/>
                                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(keyValidationClass)) {%>

                                <fmt:message
                                        key="cassandra.field.comparator.lexicalUUID"/>
                                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(keyValidationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.timeUUID"/>
                                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(keyValidationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.long"/>
                                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(keyValidationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.integer"/>
                                <% } else {%>
                                <fmt:message
                                        key="cassandra.field.comparator.bytes"/>
                                <% }%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.comparator"/></td>
                            <td align="left">
                                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(comparator)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.ascii"/>
                                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(comparator)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.utf8"/>
                                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(comparator)) {%>

                                <fmt:message
                                        key="cassandra.field.comparator.lexicalUUID"/>
                                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(comparator)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.timeUUID"/>
                                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(comparator)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.long"/>
                                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(comparator)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.integer"/>
                                <% } else {%>
                                <fmt:message
                                        key="cassandra.field.comparator.bytes"/>
                                <% }%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.defaultValidationclass"/></td>
                            <td align="left">
                                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(validationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.ascii"/>
                                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(validationClass)) {%>
                                <fmt:message
                                    key="cassandra.field.comparator.utf8"/>
                                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(validationClass)) {%>

                                <fmt:message
                                        key="cassandra.field.comparator.lexicalUUID"/>
                                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(validationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.timeUUID"/>
                                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(validationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.long"/>
                                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(validationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.comparator.integer"/>
                                <% } else if (CassandraAdminClientConstants.COUNTERCOLUMNTYPE.equals(validationClass)) {%>
                                <fmt:message
                                        key="cassandra.field.validator.countercolumn"/>
                                <% } else {%>

                                <fmt:message
                                        key="cassandra.field.comparator.bytes"/>
                                <% }%>
                            </td>
                        </tr>
                    </table>
                </td>
                <%
                if (columnFamilyStats != null) {
                %>
                <td width="20%">&nbsp;</td>
                <td>

                    <div id="cfstatsTableDIv">
                        <table class="styledLeft" id="cfstatsTable"
                               style="margin-left: 0px;" width="100%">
                            <thead>
                            <tr>
                                <th width="50%" colspan="2">
                                    <fmt:message key="cassandra.cf.stats"/>
                                </th>
                            </tr>
                            </thead>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.liveSSTableCount"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getLiveSSTableCount()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.liveDiskSpaceUsed"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getLiveDiskSpaceUsed()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.totalDiskSpaceUsed"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getTotalDiskSpaceUsed()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.memtableColumnsCount"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getMemtableColumnsCount()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.memtableDataSize"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getMemtableDataSize()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.memtableSwitchCount"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getMemtableSwitchCount()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.readCount"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getReadCount()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.readLatency"/>
                                </td>
                                <td>
                                    <% double readLatency = columnFamilyStats.getReadLatency();
                                        if (Double.isNaN(readLatency)) {
                                    %>-<%
                                } else {
                                %> <%=readLatency%>
                                    <%} %>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.writeCount"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getWriteCount()%>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.writeLatency"/>
                                </td>
                                <td>
                                    <% double writeLatency = columnFamilyStats.getWriteLatency();
                                        if (Double.isNaN(writeLatency)) {
                                    %>-<%
                                } else {
                                %> <%=writeLatency%>
                                    <%} %>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="cassandra.cf.stats.pendingTasks"/>
                                </td>
                                <td>
                                    <%=columnFamilyStats.getPendingTasks()%>
                                </td>
                            </tr>
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
                        ColumnInformation[] columns = cfInformation.getColumns();
                        String clTableDisplay = "display:none;";
                        if (columns != null && columns.length != 0) {
                            clTableDisplay = "";
                        }
                    %>

                    <td width="80%" colspan="3">
                        <div style="margin-top:0px;">
                            <% if(!keyspace.equals("system") && !keyspace.equals("system_auth")
                                    && !keyspace.equals("system_traces")) {%>
                            <a class="add-icon-link" onclick="addCL('<%=columnFamily%>','<%=keyspace%>');" href="#">
                                <fmt:message key="cassandra.add.new.cl"/></a>
                            <% } %>
                            <br><br><br>
                        </div>
                        <div id="serviceClientDiv" style="<%=clTableDisplay%>">
                            <table class="styledLeft" id="clTable1" style="margin-left: 0px;" width="100%">
                                <thead>
                                <tr>
                                    <th width="25%"><fmt:message key="cassandra.cl.name"/></th>
                                    <th width="25%"><fmt:message key="cassandra.cl.validator.type"/></th>
                                    <th width="25%"><fmt:message key="cassandra.field.indexname"/></th>
                                    <% if(!keyspace.equals("system") && !keyspace.equals("system_auth") &&
                                            !keyspace.equals("system_traces")) {%>
                                        <th width="25%"><fmt:message key="cassandra.actions"/></th>
                                    <% } %>
                                </tr>
                                </thead>
                                <tbody id="clBody">
                                <%
                                    int j = 0;
                                    if (columns != null && columns.length != 0) {
                                        for (ColumnInformation column : columns) {
                                            String name = column.getName();
                                            String indexName = column.getIndexName();
                                            if(column.getIndexName() == null || column.getIndexName().isEmpty()){
                                                indexName = "--NOT INDEXED--";
                                            }
                                            String validatorClass = column.getValidationClass();
                                            String[] splitValidator = validatorClass.split("\\.");
                                            String validator = splitValidator[splitValidator.length - 1];
                                            j++;
                                %>
                                            <tr id="clRaw<%=j%>">
                                                <td id="clTD<%=j%>"><%=name%></td>
                                                <td><%=validator%></td>
                                                <td><%=indexName%></td>
                                                <% if(!keyspace.equals("system") && !keyspace.equals("system_auth")
                                                        && !keyspace.equals("system_traces")) {%>
                                                    <td>
                                                        <input type="hidden" name="clName<%=j%>" id="clName<%=j%>"
                                                               value="<%=name%>"/>
                                                        <a class="delete-icon-link"
                                                           onclick="deleteCL('<%=keyspace%>','<%=columnFamily%>','<%=j%>');"
                                                           href="#"><fmt:message
                                                                key="cassandra.actions.delete"/></a>
                                                    </td>
                                                <% } %>
                                            </tr>
                                <%
                                        }
                                    }
                                %>
                                <input type="hidden" name="clCount" id="clCount" value="<%=j%>"/>
                                </tbody>
                            </table>
                            <br>
                        </div>
                    </td>
                </tr>
            </table>
            <script type="text/javascript">
                alternateTableRows('cfInfoTable', 'tableEvenRow', 'tableOddRow');
                alternateTableRows('cfstatsTable', 'tableEvenRow', 'tableOddRow');
                alternateTableRows('clTable1', 'tableEvenRow', 'tableOddRow');
                alternateTableRows('clTable2', 'tableEvenRow', 'tableOddRow');
            </script>
            <br><br>
        <%
        if(rolePermissions.length != 0){
        %>
            <h2 id="permissionArea">Permissions for Column Family : <%=columnFamily%></h2>
            <form method="post" action="share_cf-processor.jsp" name="dataForm">
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
                               <input type="hidden" name="cfName" id="cfName" value="<%=columnFamily%>"/>
                               <input class="button" type="submit" value="Save">
                               <input id="cancelKSButton" class="button" name="cancelKSButton" type="button" href="#"
                                  onclick="location.href = 'cf_dashboard.jsp?keyspaceName=<%=keyspace%>&cfName=<%=columnFamily%>#permissionArea';"
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
<% }%>
<% }%>
</fmt:bundle>
