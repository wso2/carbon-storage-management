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

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="js/uiValidator.js" title="RSS UI Validator JS"/>
<script type="text/javascript" src="global-params.js"/>

<carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                   topPage="true"
                   request="<%=request%>"
                   label="Databases"/>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">

    <%
        RSSManagerClient client = null;

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String environmentName = request.getParameter("envName");
        String[] environments = (String[]) session.getAttribute("environments");
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext,
                    request.getLocale());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
        }
        if (environments == null || environments.length == 0) {
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
                if (environments == null || environments.length < 0) {
                    CarbonUIMessage.sendCarbonUIMessage("Failed to load the available environment list",
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
        <h2><fmt:message key="rss.manager.databases"/></h2>

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
                <div id="connectionStatusDiv" style="display: none;"></div>

                <table class="styledLeft" id="database_table">
                    <%
                        if (client != null) {
                            try {
                                DatabaseInfo[] databases = client.getDatabaseList(environmentName);
                                if (databases.length > 0) {
                    %>
                    <thead>
                    <tr>
                        <th><fmt:message key="rss.manager.db.name"/></th>
                        <th><fmt:message key="rss.manager.instance.name"/></th>
                        <th><fmt:message key="rss.manager.rss.instance.type"/></th>
                        <th><fmt:message key="rss.manager.db.url"/></th>
                        <th><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabaseInfo database : databases) {
                            if (database != null) {
                    %>

                    <tr id="tr_<%=database.getRssInstanceName()%>_<%=database.getName()%>">
                        <td><%=database.getName()%>
                        </td>
                        <td><%=database.getRssInstanceName()%>
                        </td>
                        <td><%=database.getType()%>
                        </td>
                        <td><%=database.getUrl()%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               onclick="submitManageForm('<%=database.getRssInstanceName()%>','<%=database.getName()%>', '<%=environmentName%>', '<%=database.getType()%>')"><fmt:message
                                    key="rss.manager.manage.database"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               onclick="dropDatabase('<%=database.getRssInstanceName()%>', '<%=database.getName()%>', '<%=environmentName%>', '<%=database.getType()%>')"><fmt:message
                                    key="rss.manager.delete.database"/></a>
                        </td>
                    </tr>
                    <%

                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="5">No databases created yet.</td>
                    </tr>
                    <%
                                }
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

                    </tbody>
                </table>
                <a class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"
                   onclick="createDBForm('<%=environmentName%>')"><fmt:message
                        key="rss.manager.add.new.database"/></a>

                <div style="clear:both"></div>
            </form>
            <script type="text/javascript">
                function submitManageForm(rssInstanceName, databaseName, envName, instanceType) {
                    document.getElementById('rssInstanceName').value = rssInstanceName;
                    document.getElementById('databaseName').value = databaseName;
                    document.getElementById('envName1').value = envName;
                    document.getElementById('instanceType').value = instanceType;
                    document.getElementById('manageForm').submit();
                }
                function submitDropForm(databaseName) {
                    document.getElementById('databaseName').value = databaseName;
                    document.getElementById('flag').value = 'drop';
                    document.getElementById('dropForm').submit();
                }
                function createDBForm(envName) {
                    document.getElementById('createDBForm').submit();
                }
                function onComboChange(combo) {
                    var opt = combo.options[combo.selectedIndex].value;
                    window.location = 'databases.jsp?envName=' + opt;
                }
            </script>
            <form action="attachedDatabaseUsers.jsp" method="post" id="manageForm">
                <input type="hidden" id="rssInstanceName" name="rssInstanceName"/>
                <input type="hidden" id="envName1" name="envName" value="<%=environmentName%>"/>
                <input type="hidden" id="databaseName" name="databaseName"/>
                <input type="hidden" id="instanceType" name="instanceType"/>
            </form>
            <form action="createDatabase.jsp" method="post" id="createDBForm">
                <input type="hidden" id="envName2" name="envName" value="<%=environmentName%>"/>
            </form>
            <form action="databaseOps_ajaxprocessor.jsp" method="post" id="dropForm">
                <input type="hidden" id="rssInstanceName1" name="rssInstanceName"/>
                <input type="hidden" id="envName3" name="envName" value="<%=environmentName%>"/>
                <input type="hidden" id="flag" name="flag"/>
            </form>
        </div>
    </div>
<% } %>
</fmt:bundle>

