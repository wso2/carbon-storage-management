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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeTemplateInfo" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Database Privilege Templates"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        String templateName;
        RSSManagerClient client = null;
        String environmentName = request.getParameter("envName");
        String[] environments = (String[]) session.getAttribute("environments");

        DatabasePrivilegeTemplateInfo[] templates;
        try {
            String backendServerUrl = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(
                            CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());

        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
        }
        if (environments == null) {
            if (client != null) {
                try {
                    environments = client.getRSSEnvironmentNames();
                    session.setAttribute("environments", environments);
                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                            CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
        }
        if (environments == null || environments.length <= 0) {
            CarbonUIMessage.sendCarbonUIMessage("No RSS Environment has been configured",
                    CarbonUIMessage.ERROR, request);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
                }
            }
        }
       if (environments != null && environments.length > 0) {
           if (environmentName == null) {
               environmentName = environments[0];
           }
    %>
    <div id="middle">
        <h2><fmt:message key="rss.manager.database.privilege.templates"/></h2>

        <div id="workArea">
            <div>
                <fmt:message key="rss.environment.name"/> <select id="envCombo" name="envCombo"
                                                                  onchange="onComboChange(this)">
                <%
                    for (String env : environments) {
                        if (env.equals(environmentName.trim())) {
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

            <form method="post" action="#" name="dataForm">
                <table class="styledLeft" id="privilegeTemplateTable">
                    <%
                        if (client != null) {
                            try {
                                templates = client.getDatabasePrivilegesTemplates(environmentName);
                                if (templates.length > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message
                                key="rss.manager.database.privilege.template.name"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabasePrivilegeTemplateInfo template : templates) {
                            if (template != null) {
                                templateName = template.getName();
                    %>
                    <tr id="tr_<%=templateName%>">
                        <td id="td_<%=template.getName()%>"><%=templateName%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               href="javascript:dispatchEditDatabasePrivilegeTemplateRequest('<%=template.getName()%>', '<%=environmentName%>')"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="dispatchDropDatabasePrivilegeTemplateRequest('<%=template.getName()%>', '<%=environmentName%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    } else { %>
                    <tr>
                        <td colspan="2">No templates created yet...</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
                <%
                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                            CarbonUIMessage.ERROR, request, e);
                %>
                <script type="text/javascript">
                    location.href = "../admin/error.jsp";
                </script>
                <%
                        }
                    }
                %>
                <div id="connectionStatusDiv" style="display: none;"></div>
                <a class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"
                   href="javascript:submitAddDbPrivilegeTemplate()"><fmt:message
                        key="rss.manager.add.database.privilege.template"/></a>

                <div style="clear:both"></div>
            </form>
            <script type="text/javascript">
                function dispatchEditDatabasePrivilegeTemplateRequest(privilegeTemplateName) {
                    document.getElementById('privilegeTemplateName').value = privilegeTemplateName;
                    document.getElementById('editForm').submit();
                }
                function onComboChange(envCombo) {
                    var opt = envCombo.options[envCombo.selectedIndex].value;
                    window.location = 'databasePrivilegeTemplates.jsp?envName=' + opt;
                }
            </script>
            <form method="post" action="editDatabasePrivilegeTemplate.jsp" id="editForm">
                <input type="hidden" name="privilegeTemplateName" id="privilegeTemplateName"/>
                <input type="hidden" name="envName" id="envName" value="<%=environmentName%>"/>
            </form>
            <script type="text/javascript">
                function submitAddDbPrivilegeTemplate() {
                    document.getElementById('addPrivilegeTemplate').submit();
                }
            </script>
            <form action="createDatabasePrivilegeTemplate.jsp" method="post" id="addPrivilegeTemplate">
                <input type="hidden" id="envName" name="envName" value="<%=environmentName%>"/>
            </form>
        </div>
    </div>
    <% } %>
</fmt:bundle>
