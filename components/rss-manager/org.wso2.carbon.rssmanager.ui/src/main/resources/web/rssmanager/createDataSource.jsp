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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.RSSInstanceInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Create Datasource"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        String rssInstanceName = request.getParameter("rssInstanceNameDS");
        String databaseName = request.getParameter("databaseNameDS");
        String username = request.getParameter("usernameDS");
        String envName = request.getParameter("envNameDS");
        String instanceType = request.getParameter("instanceTypeDS");
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.create.datasource"/></h2>

        <div id="workArea">
            <form method="post" action="#" name="addDatasourceForm"
                  id="addDatasourceForm">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>Create New Datasource</th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                </tr>
                                <tr>
                                    <td class="leftCol-med">
                                            <fmt:message key="rss.environment.name"/></td>
                                    <td>
                                    <input readonly value="<%=envName%>" id="environmentname"
                                               name="environmentname"
                                               size="30" type="text"><font
                                            color='black'>
                                    </font>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.rss.instance.type"/></td>
                                    <td>
                                    <input readonly value="<%=rssInstanceName%>" id="rssinstancetype"
                                               name="rssinstancetype"
                                               size="30" type="text"><font
                                            color='black'>
                                    </font>
                                    </td>
                                </tr>
                                
                                <tr>
                                <td class="leftCol-med">
                                        <fmt:message key="rss.manager.instance.name"/>
                                <td>
                                  <input readonly value="<%=instanceType%>" id="rssinstancename"
                                               name="rssinstancename"
                                               size="30" type="text"><font
                                            color='black'>
                                    </font>
                                </td>
                                </tr>
                                <tr>
                                <td class="leftCol-med">
                                        <fmt:message key="rss.manager.db.username"/>
                                <td>
                                  <input readonly value="<%=username%>" id="username"
                                               name="username"
                                               size="30" type="text"><font
                                            color='black'>
                                    </font>
                                </td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message key="rss.manager.ds.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="datasourcename"
                                               name="datasourcename"
                                               size="30" type="text"><font
                                            color='black'>
                                    </font></td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                      <div id="connectionStatusDiv" style="display: none;"></div>
                   
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.create"/>"
                                   onclick="createDataSource('<%=rssInstanceName%>','<%=databaseName%>','<%=username%>','<%=envName%>','<%=instanceType%>')"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="cancel('<%=rssInstanceName%>','<%=databaseName%>', '<%=username%>', '<%=envName%>' , '<%=instanceType%>')"/>
                        </td>
                    </tr>
                </table>
            </form>
            <script type="text/javascript">
                function cancel(rssInstanceName, databaseName, username, envName, instanceType) {
                    document.getElementById('rssInstanceName').value = rssInstanceName;
                	document.getElementById('databaseName').value = databaseName;
                    document.getElementById('username').value = username;
                    document.getElementById('envName').value = envName;
                    document.getElementById('instanceType').value = instanceType;
                    document.getElementById('cancelForm').submit();
                }
            </script>
            <form action="attachedDatabaseUsers.jsp" method="post" id="cancelForm">
                <input type="hidden" id="rssInstanceName" name="rssInstanceName"/>
                <input type="hidden" id="databaseName" name="databaseName"/>
                <input type="hidden" id="username" name="username"/>
                <input type="hidden" id="envName" name="envName"/>
                <input type="hidden" id="instanceType" name="instanceType"/>
            </form>
        </div>
    </div>
</fmt:bundle>
