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
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    RSSManagerClient client = null;
    String rssProvider = null;
    String tenantDomain = null;
    try {
        String backendServerUrl =
                CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        client = new RSSManagerClient(cookie, backendServerUrl, configContext,
                request.getLocale());
        tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        rssProvider = client.getRSSProvider();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    }

    String[] environments = null;
    if (client != null) {
        try {
            environments = client.getRSSEnvironmentNames();
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                    CarbonUIMessage.ERROR, request, e);
        }
    }
%>
<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

    <carbon:breadcrumb
            label="Create RSS Instance"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <carbon:jsi18n
		    resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
		    request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <div id="middle">
        <h2><fmt:message key="rss.manager.new.instance"/></h2>

        <div id="workArea">

            <form method="post" action="#" name="createRSSInstanceForm"
                  id="createRSSInstanceForm"
                  onsubmit="return validateRSSInstanceProperties('create')">
                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="rssInstanceName"
                                               name="rssInstanceName" class="longInput"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.environment.name"/><font
                                            color='red'>*</font></td>
                                    <td><label>
                                    <select name="serverEnvironment" id="serverEnvironment">
                                        <option value="">----SELECT----
                                        </option>
                                        <%for(String environment:environments) {%>
                                        <option value="<%=environment%>">
                                            <%=environment%>
                                        </option>
                                        <%}%>
                                    </select>
                                </label></td>
                                </tr>
                                <%
                                if(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
                                %>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.rss.instance.type"/>
                                        <font color="red">*</font>
                                    </td>
                                    <td>
                                        <label>
                                            <select name="instancetype" id="instancetype">
                                                <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>" selected><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>
                                                </option>
                                                <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>"><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>
                                                </option>
                                            </select>
                                        </label>
                                    </td>
                                <tr>
                                <%}%>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.type"/>
                                        <font color="red">*</font>
                                    </td>
                                    <td>
                                        <input value="<%=rssProvider%>" id="provider"
                                        name="provider"
                                        size="30" type="text" readonly="readonly">
                                    </td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.instance.url"/><font
                                            color='red'>*</font></td>
                                    <%
                                        String serverUrl = null;
                                        String driver = null;
                                    if(RSSManagerConstants.H2.equalsIgnoreCase(rssProvider)) {
                                        serverUrl = "jdbc:h2:tcp://<server>[:<port>]/[<path>]<databaseName>";
                                        driver = "org.h2.Driver";
                                    } else if(RSSManagerConstants.MYSQL.equalsIgnoreCase(rssProvider)) {
                                        serverUrl = "jdbc:mysql://[machine-name/ip]:[port]";
                                        driver = "com.mysql.jdbc.Driver";
                                    }if(RSSManagerConstants.POSTGRES.equalsIgnoreCase(rssProvider)) {
                                        serverUrl = "jdbc:postgresql://[machine-name/ip]:[port]";
                                        driver = "org.postgresql.Driver";
                                    }if(RSSManagerConstants.SQLSERVER.equalsIgnoreCase(rssProvider)) {
                                        serverUrl = "dbc:sqlserver://[machine-name/ip]";
                                        driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                                    }if(RSSManagerConstants.ORACLE.equalsIgnoreCase(rssProvider)) {
                                        serverUrl = "jdbc:oracle:thin:@//[machine-name/ip][:port]";
                                        driver = "oracle.jdbc.driver.OracleDriver";
                                    }
                                    %>
                                    <td><input value="<%=serverUrl%>" id="serverUrl"
                                               name="serverUrl"
                                               class="longInput"></td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.datasource.class.name"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input id="dataSourceClassName" name="dataSourceClassName" class="longInput"
                                               value="<%=driver%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.instance.username"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input id="username" name="username" class="longInput"
                                               value=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.instance.password"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input type="password" id="password" name="password" class="longInput"
                                               value=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.confirm.instance.password"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input id="repassword" type="password" name="repassword" class="longInput"
                                               value=""/>
                                    </td>
                                </tr>
                                <%--<tr>
                                    <td align="leftCol-med">
                                        <fmt:message key="rss.manager.datasource.properties"/>
                                    </td>
                                    <td>
                                        <div id="nameValueAdd">
                                            <a class="icon-link"
                                               href="#addNameLink"
                                               onclick="addDataSourceProperties();"
                                               style="background-image: url(../admin/images/add.gif);">
                                                <fmt:message key="rss.manager.add.property"/></a>

                                            <div style="clear:both;"></div>
                                        </div>--%>
                                        <%--<div>
                                            <table cellpadding="0" cellspacing="0" border="0"
                                                   class="styledLeft"
                                                   id="dsPropertyTable"
                                                   style="display:none;">
                                                <thead>
                                                <tr>
                                                    <th style="width:40%"><fmt:message
                                                            key="rss.manager.prop.name"/></th>
                                                    <th style="width:40%"><fmt:message
                                                            key="rss.manager.prop.value"/></th>
                                                    <th style="width:20%"><fmt:message
                                                            key="rss.manager.prop.action"/></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                </tbody>
                                            </table>

                                        </div>--%>
                                    <%--</td>--%>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="3">
                            <div id="connectionStatusDiv" style="display: none;"></div>
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.test.connection"/>"
                                   onclick="return testConnection();"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.save"/>"
                                   onclick="if(validateRSSInstanceProperties()) {dispatchRSSInstanceCreateRequest('create');} return false;"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'rssInstances.jsp'"/>
                        </td>

                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>
