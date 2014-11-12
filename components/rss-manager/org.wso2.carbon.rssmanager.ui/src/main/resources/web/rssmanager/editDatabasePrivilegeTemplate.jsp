<!--
~ Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeTemplateInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeSetInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.MySQLPrivilegeSetInfo" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" language="JavaScript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
<carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                   topPage="false" request="<%=request%>" label="Edit Privilege Template"/>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
<%
    String templateName = request.getParameter("privilegeTemplateName");
    RSSManagerClient client;
    DatabasePrivilegeTemplateInfo template = null;
    DatabasePrivilegeSetInfo privileges = null;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    
    String envName = request.getParameter("envName");
    
    
    try {
        client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
        template = client.getDatabasePrivilegesTemplate(envName,templateName);
        if (template != null) {
            privileges = template.getPrivileges();
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    }

%>

<div id="middle">
<h2><fmt:message key="rss.manager.edit.database.privilege.template"/></h2>

<div id="workArea">
<%
    if (template != null) {
        try {
%>
<form method="post" action="databasePrivilegeTemplateOps_ajaxprocessor.jsp" name="dataForm"
      id="dataForm">
    <table class="styledLeft" id="databasePrivilegeTemplateData">
        <thead>
        <tr>
            <th width="20%"><fmt:message key="rss.manager.property.name"/></th>
            <th width="60%"><fmt:message key="rss.manager.value"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><fmt:message
                    key="rss.manager.database.privilege.template.name"/></td>
            <td><input type="text" id="privilegeTemplateName" name="privilegeTemplateName"
                       value="<%=template.getName()%>" readonly="readonly"/>
            </td>
        </tr>
        </tbody>
    </table>
    <table class="styledLeft" id="databaseUserInfo">
        <thead>
        <tr>
            <th width="20%"><fmt:message key="rss.manager.permission.name"/></th>
            <th width="60%"><input type="checkbox" id="selectAll" name="selectAll"
                                   onclick="selectAllOptions()"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><fmt:message key="rss.manager.permissions.select"/></td>
            <%if (privileges != null && "Y".equals(privileges.getSelectPriv())) {%>
            <td><input type="checkbox" name="select_priv" id="select_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="select_priv" id="select_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.insert"/></td>
            <%if (privileges != null && "Y".equals(privileges.getInsertPriv())) {%>
            <td><input type="checkbox" name="insert_priv" id="insert_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="insert_priv" id="insert_priv" /></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.update"/></td>
            <%if (privileges != null && "Y".equals(privileges.getUpdatePriv())) {%>
            <td><input type="checkbox" name="update_priv" id="update_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="update_priv" id="update_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.delete"/></td>
            <%if (privileges != null && "Y".equals(privileges.getDeletePriv())) {%>
            <td><input type="checkbox" name="delete_priv" id="delete_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="delete_priv" id="delete_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create"/></td>
            <%if (privileges != null && "Y".equals(privileges.getCreatePriv())) {%>
            <td><input type="checkbox" name="create_priv" id="create_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_priv" id="create_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.drop"/></td>
            <%if (privileges != null && "Y".equals(privileges.getDropPriv())) {%>
            <td><input type="checkbox" name="drop_priv" id="drop_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="drop_priv" id="drop_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.grant"/></td>
            <%if (privileges != null  && privileges instanceof MySQLPrivilegeSetInfo && "Y".equals(((MySQLPrivilegeSetInfo)privileges).getGrantPriv())) {%>
            <td><input type="checkbox" name="grant_priv" id="grant_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="grant_priv" id="grant_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.references"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getReferencesPriv())) {%>
            <td><input type="checkbox" name="references_priv" id="references_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="references_priv" id="references_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.index"/></td>
            <%if (privileges != null && "Y".equals(privileges.getIndexPriv())) {%>
            <td><input type="checkbox" name="index_priv" id="index_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="index_priv" id="index_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.alter"/></td>
            <%if (privileges != null && "Y".equals(privileges.getAlterPriv())) {%>
            <td><input type="checkbox" name="alter_priv" id="alter_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="alter_priv" id="alter_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.temp.table"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getCreateTmpTablePriv())) {%>
            <td><input type="checkbox" name="create_tmp_table_priv" id="create_tmp_table_priv" checked="checked"/>
            </td>
            <%} else {%>
            <td><input type="checkbox" name="create_tmp_table_priv" id="create_tmp_table_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.lock.tables"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getLockTablesPriv())) {%>
            <td><input type="checkbox" name="lock_tables_priv" id="lock_tables_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="lock_tables_priv" id="lock_tables_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.view"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getCreateViewPriv())) {%>
            <td><input type="checkbox" name="create_view_priv" id="create_view_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_view_priv" id="create_view_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.show.view"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getShowViewPriv())) {%>
            <td><input type="checkbox" name="show_view_priv" id="show_view_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="show_view_priv" id="show_view_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.create.routine"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getCreateRoutinePriv())) {%>
            <td><input type="checkbox" name="create_routine_priv" id="create_routine_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="create_routine_priv" id="create_routine_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.alter.routine"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getAlterRoutinePriv())) {%>
            <td><input type="checkbox" name="alter_routine_priv" id="alter_routine_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="alter_routine_priv" id="alter_routine_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.execute"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getExecutePriv())) {%>
            <td><input type="checkbox" name="execute_priv" id="execute_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="execute_priv" id="execute_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.event"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getEventPriv())) {%>
            <td><input type="checkbox" name="event_priv" id="event_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="event_priv" id="event_priv"/></td>
            <%}%>
        </tr>
        <tr>
            <td><fmt:message key="rss.manager.permissions.trigger"/></td>
            <%if (privileges != null && privileges instanceof MySQLPrivilegeSetInfo &&  "Y".equals(((MySQLPrivilegeSetInfo)privileges).getTriggerPriv())) {%>
            <td><input type="checkbox" name="trigger_priv" id="trigger_priv" checked="checked"/></td>
            <%} else {%>
            <td><input type="checkbox" name="trigger_priv" id="trigger_priv"/></td>
            <%}%>
        </tr>
        </tbody>
    </table>
    <div id="connectionStatusDiv" style="display: none;"></div>
    <input id="flag" name="flag" type="hidden" value="edit"/>
    <tr>
        <td class="buttonRow" colspan="2">
            <input class="button" type="button"
                   value="<fmt:message key="rss.manager.save"/>"
                    onclick="return createDatabasePrivilegeTemplate('edit', '<%=envName%>'); return false;"/>
            <%--<input class="button" type="button"--%>
                   <%--value="<fmt:message key="rss.manager.save"/>"--%>
                    <%--onclick="return editDatabasePrivilegeTemplate('<%=templateName%>', 'edit')"/>--%>
            <input class="button" type="button"
                   value="<fmt:message key="rss.manager.cancel"/>"
                   onclick="document.location.href='databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&ordinal=0&envName=<%=envName%>'"/>

        </td>
    </tr>
</form>
</div>
<%
        } catch (Exception e) {

        }
    }
%>

</div>
</fmt:bundle>

