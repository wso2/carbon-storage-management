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
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseUserInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- The v=1 value is appended to the js/uiValidator.js to invalidate the existing cache -->
<script type="text/javascript" src="js/uiValidator.js?v=1" language="javascript"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="Database Users"/>
    <carbon:jsi18n
		    resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
		    request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <div id="middle">
        <h2><fmt:message key="rss.manager.users"/></h2>

        <%
            RSSManagerClient client = null;

            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String environmentName = request.getParameter("envName");
            String[] environments = (String[]) session.getAttribute("environments");

            try {
                client =
                        new RSSManagerClient(cookie, backendServerURL, configContext,
                        request.getLocale());
            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                        CarbonUIMessage.ERROR, request, e);
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

        <div id="workArea">
            <div>
                <fmt:message key="rss.environment.name"/> <select id="envCombo" name="envCombo"
                                                                      onchange="onComboChange(this)">
                <%

                    for (String env : environments) {
                        if (environmentName != null && env.equals(environmentName.trim())) {
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
                <table class="styledLeft" id="databaseUserTable">
                    <%
                        if (client != null) {
                            try {
                                DatabaseUserInfo[] users = client.getDatabaseUsers(environmentName);
                                if (users != null && users.length > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="30%"><fmt:message key="rss.manager.user"/></th>
                        <th width="30%"><fmt:message key="rss.manager.instance.type"/></th>
                        <th width="30%"><fmt:message key="rss.manager.instance.name"/></th>
                        <th width="40%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabaseUserInfo user : users) {
                            if (user != null) {
                    %>
                    <tr>
                        <td id="<%=user.getName()%>"><%=user.getName()%>
                        </td>
                        <td><%=user.getType()%>
                        <td><%=user.getRssInstanceName()%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               onclick="dispatchEditDatabaseUser('<%=user.getRssInstanceName()%>', '<%=user.getName()%>', '<%=environmentName%>', '<%=user.getType()%>')"
                               href="#"><fmt:message
                                    key="rss.manager.edit.database"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               onclick="dropDatabaseUser('<%=user.getRssInstanceName()%>', '<%=user.getName()%>', '<%=environmentName%>', '<%=user.getType()%>')"
                               href="#"><fmt:message
                                    key="rss.manager.delete.database"/></a>
                        </td>
                    </tr>

                    <%
                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="3">No database users defined yet..</td>
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
                <div id="connectionStatusDiv" style="display: none;"></div>
                <a class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"
                   href="javascript:submitAddForm()"><fmt:message key="rss.manager.add.user"/></a>

                <div style="clear:both;"></div>
            </form>
            <script type="text/javascript">
                function submitAddForm() {
                    document.getElementById('addForm').submit();
                }
            </script>
            <form action="createDatabaseUser.jsp" method="post" id="addForm">
                <input type="hidden" id="rssInstanceName1" name="rssInstanceName"/>
                <input type="hidden" id="envName" name="envName" value="<%=environmentName%>"/>
            </form>
            <script type="text/javascript">
                function submitCancelForm() {
                    document.getElementById('cancelForm').submit();
                }
            </script>
            <form action="databases.jsp" method="post" id="cancelForm">
                <input type="hidden" name="rssInstanceName" id="rssInstanceName2"/>
            </form>
            <script type="text/javascript">
                function submitExploreForm(userName, url, driver) {
                    document.getElementById('dbConsoleUseruame').value = userName;
                    document.getElementById('url').value = encodeURIComponent(url);
                    document.getElementById('driver').value = encodeURIComponent(driver);
                    document.getElementById('exploreForm').submit();
                }
                function onComboChange(envCombo) {
                    var opt = envCombo.options[envCombo.selectedIndex].value;
                    window.location = 'databaseUsers.jsp?envName=' + opt;
                }

                function dispatchEditDatabaseUser(rssInstanceName, username, envName, instanceType) {
                    document.getElementById('rssInstanceName').value = rssInstanceName;
                    document.getElementById('environment').value = envName;
                    document.getElementById('username').value = username;
                    document.getElementById('rssType').value = instanceType;
                    document.getElementById('editUser').submit();
                }
            </script>
            <form action="editDatabaseUser.jsp" method="post" id="editUser">
                <input id="rssInstanceName" name="rssInstanceName" type="hidden"/>
                <input id="environment" name="environment" type="hidden"/>
                <input id="rssType" name="rssType" type="hidden"/>
                <input id="username" name="username" type="hidden"/>
            </form>
            <form action="../dbconsole/login.jsp" method="post" id="exploreForm">
                <input type="hidden" id="dbConsoleUsername" name="userName"/>
                <input type="hidden" id="url" name="url"/>
                <input type="hidden" id="driver" name="driver"/>
            </form>
        </div>
    </div>
    <% } %>
</fmt:bundle>
