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

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseUserInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- The v=1 value is appended to the js/uiValidator.js to invalidate the existing cache -->
<script type="text/javascript" src="js/uiValidator.js?v=1" language="javascript"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
                       topPage="false" request="<%=request%>" label="Attached Database Users"/>

    <div id="middle">
        <h2><fmt:message key="rss.manager.attached.database.users"/></h2>

        <%
            RSSManagerClient client = null;
            DatabaseInfo database = null;
			String rssInstanceName = request.getParameter("rssInstanceName");
            String databaseName = request.getParameter("databaseName");
            String envName = request.getParameter("envName");
            
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            rssInstanceName = (rssInstanceName != null) ? rssInstanceName : (String) session.getAttribute("rssInstanceName");
            databaseName = (databaseName != null) ? databaseName : (String) session.getAttribute("databaseName");
            envName = (envName != null) ? envName : (String) session.getAttribute("envName");


            try {
                client = new RSSManagerClient(cookie, backendServerURL, configContext, request.getLocale());
                database = client.getDatabase(envName,rssInstanceName, databaseName,RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                        CarbonUIMessage.ERROR, request, e);
            }
        %>
        <div id="workArea">
            <form method="post" action="#" name="dataForm">
                <table class="styledLeft" id="databaseUserTable">
                    <%
                        if (client != null) {
                            try {
                                DatabaseUserInfo[] users =
                                        client.getUsersAttachedToDatabase(envName,rssInstanceName,
                                                databaseName,RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
                                if (users != null && users.length > 0) {
                    %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.user"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (DatabaseUserInfo user : users) {
                            if (user.getName() != null) {
                    %>
                    <tr>
                        <td id="<%=user.getName()%>"><%=user.getName()%>
                        </td>
                        <td>
                           <%--<% if(enableDbConsole = true){%>--%>
                            <a class="icon-link"
                               style="background-image: url(../rssmanager/images/db-exp.png);"
                               onclick="submitExploreForm('<%=user.getName()%>', '<%=(database != null) ? database.getUrl() : ""%>','<%=(database != null) ? RSSManagerHelper.getDatabaseDriver(database.getUrl()) : ""%>')"
                               href="#"><fmt:message key="rss.manager.explore.database"/>
                            </a>
                            <%--<% }%>--%>
                            <a class="icon-link"
                               style="background-image:url(../rssmanager/images/data-sources-icon.gif);"
                               onclick="createDataSource('<%=database.getRssInstanceName()%>','<%=databaseName%>', '<%=user.getName()%>', '<%=envName%>')"
                               href="#"><fmt:message key="rss.manager.create.datasource"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               href="javascript:submitEditForm('<%=database.getRssInstanceName()%>','<%=database.getName()%>','<%=user.getName()%>', '<%=envName%>')">
                                <fmt:message key="rss.manager.edit.user"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               onclick="detachDatabaseUser('<%=database.getRssInstanceName()%>', '<%=databaseName%>', '<%=user.getName()%>', '<%=envName%>'); return false;"
                               href="#"><fmt:message
                                    key="rss.manager.detach.database.user"/></a>
                        </td>
                    </tr>

                    <%
                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="3">Database does not have any users attached to it yet..</td>
                    </tr>
                    <%
                                }
                            } catch (Exception e) {
                                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                        CarbonUIMessage.ERROR, request, e);
                            }
                        }
                    %>
                    </tbody>
                </table>
                <div id="connectionStatusDiv" style="display: none;"></div>
                <a class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"
                   href="javascript:submitAttachForm('<%=rssInstanceName%>','<%=databaseName%>')">
                    <fmt:message key="rss.manager.attach.database.user"/></a>

                <div style="clear:both;"></div>
            </form>
            <script type="text/javascript">
                function submitAttachForm(rssInstanceName, databaseName) {
                    document.getElementById('rssInstanceName1').value = rssInstanceName;
                    document.getElementById('databaseName').value = databaseName;
                    document.getElementById('addForm').submit();
                }
            </script>
            <form action="attachDatabaseUser.jsp" method="post" id="addForm">
                <input type="hidden" id="rssInstanceName1" name="rssInstanceName"/>
                <input type="hidden" id="databaseName" name="databaseName"/>
                <input type="hidden" id="envName1" name="envName" value="<%=envName%>"/>
            </form>
            <script type="text/javascript">
                function submitEditForm(rssInstanceName, databaseName, username, envName) {
                    document.getElementById('rssInstanceName').value = rssInstanceName;
                    document.getElementById('databaseName1').value = databaseName;
                    document.getElementById('username').value = username;
                    // document.getElementById('envName').value = envName;
                    document.getElementById('flag').value = 'edit';
                    document.getElementById('editForm').submit();
                }
            </script>
            <form action="editDatabaseUser.jsp" method="post" id="editForm">
                <input id="rssInstanceName" name="rssInstanceName" type="hidden"/>
                <input id="databaseName1" name="databaseName" type="hidden"/>
                <input id="username" name="username" type="hidden"/>
                <input id="envName2" name="envName" type="hidden" value='<%=envName%>'/>
                <input id="flag" name="flag" type="hidden"/>
            </form>
            <script type="text/javascript">
                function submitCancelForm() {
                    document.getElementById('cancelForm').submit();
                }
            </script>
            <form action="databases.jsp" method="post" id="cancelForm">
                <input type="hidden" name="rssInstanceName" id="rssInstanceName2"/>
                <input id="envName3" name="envName" type="hidden" value="<%=envName%>"/>
            </form>
            <script type="text/javascript">
                function submitExploreForm(username, url, driver) {
                    document.getElementById('dbConsoleUsername').value = username;
                    document.getElementById('url').value = encodeURIComponent(url);
                    document.getElementById('driver').value = encodeURIComponent(driver);
                    document.getElementById('exploreForm').submit();
                }
            </script>
            <form action="../dbconsole/login.jsp" method="post" id="exploreForm">
                <input type="hidden" id="dbConsoleUsername" name="username"/>
                <input type="hidden" id="url" name="url"/>
                <input type="hidden" id="driver" name="driver"/>
            </form>
        </div>
    </div>
</fmt:bundle>