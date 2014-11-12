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
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.RSSInstanceInfo" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseUserInfo" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" language="JavaScript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="Create Database User"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        String username = request.getParameter("username");
        String envName = request.getParameter("environment");
        String instanceType = request.getParameter("rssType");
        String rssInstanceName = request.getParameter("rssInstanceName");
        username = (username == null) ? "" : username;

        int systemRSSInstanceCount = 0;
        RSSManagerClient client = null;
        RSSInstanceInfo[] rssInstances = new RSSInstanceInfo[0];

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        DatabaseUserInfo databaseUserInfo = null;
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
            databaseUserInfo = client.getDatabaseUser(envName,rssInstanceName,username,instanceType);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.edit.dbuser"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="dataForm" onsubmit="return validatePrivileges();">
                <table class="styledLeft" id="databaseUserInfo">
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.property.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.value"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="leftCol-med"><fmt:message key="rss.environment.name"/><font
                                color='red'>*</font>
                        <td>
                            <%=envName%>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="rss.manager.rss.instance.type"/><font
                                color='red'>*</font></td>
                        <td><%=databaseUserInfo.getType()%></td>
                    </tr>
                    <%if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(databaseUserInfo.getType())){%>
                    <tr>
                        <td class="leftCol-med"><fmt:message
                                key="rss.manager.instance.name"/><font
                                color='red'>*</font></td>
                        <td><%=databaseUserInfo.getRssInstanceName()%></td>
                    </tr>
                    <%}%>
                    <tr>
                        <td><fmt:message key="rss.manager.permissions.username"/><font
                                color='red'>*</font></td>
                        <td><%=username%>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rss.manager.permissions.password"/><font
                                color='red'>*</font></td>
                        <td><input type="password" id="password" name="password"
                                   value=""/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rss.manager.default.user.repeat.password"/><font
                                color='red'>*</font></td>
                        <td><input type="password" id="repeatPassword" name="repeatPassword"/></td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   onclick="return editDatabaseUser('<%=rssInstanceName%>', '<%=username%>', '<%=envName%>', '<%=instanceType%>');return false;"
                                   value="<fmt:message key="rss.manager.save"/>"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="dispatchCancelUserCreationRequest()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
            <script type="text/javascript">
                function dispatchCancelUserCreationRequest() {
                    document.getElementById('cancelForm').submit();
                }
                function onComboChange(envCombo) {
                    var opt = envCombo.options[envCombo.selectedIndex].value;
                    window.location = 'createDatabaseUser.jsp?envName=' + opt;
                }
                function onInstanceTypeChange(combo) {
                    var opt = combo.options[combo.selectedIndex].value;
                    window.location = 'createDatabaseUser.jsp?envName=<%=envName%>&instanceType='+opt;
                }
            </script>
            <form action="databaseUsers.jsp?region=region1&item=database_users_submenu"
                  method="post" id="cancelForm">
            </form>
        </div>
    </div>
</fmt:bundle>

