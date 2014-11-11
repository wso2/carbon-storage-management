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
<%@page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseUserInfo"%>
<%-- <%@page import="org.wso2.carbon.rssmanager.core.dto.xsd.config.environment.RSSEnvironmentContext"%> --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeTemplateInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Attach database user"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        RSSManagerClient client = null;
        String rssInstanceName = request.getParameter("rssInstanceName");
        String instanceType = request.getParameter("instanceType");
        String databaseName = request.getParameter("databaseName");
        String envName = request.getParameter("envName");
               
        rssInstanceName = (rssInstanceName != null) ? rssInstanceName : "";
        databaseName = (databaseName != null) ? databaseName : "";
	 
        session.setAttribute("rssInstanceName", rssInstanceName);
        session.setAttribute("databaseName", databaseName);
        session.setAttribute("envName", envName);
        session.setAttribute("instanceType", instanceType);

        DatabaseUserInfo[] availableUsers = new DatabaseUserInfo[0];

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext,
                    request.getLocale());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                    CarbonUIMessage.ERROR, request, e);
        }
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.attach.database.user"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="databaseUserInfo"
                  id="databaseUserInfo">
                <%
                    if (client != null) {
                        try {
                            availableUsers =
                                    client.getAvailableUsersToAttachToDatabase(envName,rssInstanceName,databaseName,instanceType);
                        } catch (Exception e) {
                            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                    CarbonUIMessage.ERROR, request, e);
                        }
                %>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>Attach database user</th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                             		<td class="leftCol-med"><fmt:message
                                        key="rss.environment.name"/><font
                                            color='red'>*</font>
                              		</td>
                                    <td>
                                    	<input type="text" id="envName" name="envName" readonly="readonly" value="<%=envName%>" />
                                    </td>
                            	</tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=rssInstanceName%>" id="rssInstanceName"
                                               name="rssInstanceName"
                                               size="30" type="text" readonly="readonly"></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.rss.instance.type"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=instanceType%>" id="instanceType"
                                               name="instanceType"
                                               size="30" type="text" readonly="readonly"></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message key="rss.manager.db.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=databaseName%>" id="databaseName"
                                               name="databaseName"
                                               size="30" type="text" readonly="readonly"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.available.database.users.to.be.attached"/><font
                                            color='red'>*</font></td>
                                    <td><select id="databaseUsers"
                                                name="databaseUsers">
                                        <option id="SELECT" value="SELECT">---SELECT---</option>
                                        <%
                                            if (availableUsers != null &&
                                                    availableUsers.length > 0) {
                                                for (DatabaseUserInfo user : availableUsers) {
                                                    if (user != null) {
                                        %>
                                        <option id="<%=user.getName()%>"
                                                value="<%=user.getName()%>"><%=user.getName()%>
                                        </option>
                                        <%
                                                        }
                                                    }
                                                }
                                            }
                                        %>
                                    </select></td>
                                </tr>
                                <tr>
                                    <td><fmt:message
                                            key="rss.manager.database.privileges.template"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <select id="privilegeTemplates" name="privilegeTemplates">
                                            <option id="SELECT" value="SELECT">---SELECT---</option>
                                            <%
                                                if (client != null) {
                                                    try {
                                                        DatabasePrivilegeTemplateInfo[] templates =
                                                                client.getDatabasePrivilegesTemplates(envName);

                                                        if (templates.length > 0) {
                                                            for (DatabasePrivilegeTemplateInfo template : templates) {
                                            %>
                                            <option id="<%=template.getName()%>"
                                                    value="<%=template.getName()%>">
                                                <%=template.getName()%>
                                            </option>
                                            <%
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        CarbonUIMessage.sendCarbonUIMessage(
                                                                e.getMessage(), CarbonUIMessage.ERROR,
                                                                request, e);
                                                    }
                                                }
                                            %>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.create"/>"
                                   onclick="return attachUserToDatabase(); return false"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'attachedDatabaseUsers.jsp?ordinal=1'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
