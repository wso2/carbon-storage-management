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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.RSSInstanceInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

    <carbon:breadcrumb
            label="Edit RSS Instance"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        String instanceType;
        String rssInstanceName = request.getParameter("rssInstanceName");
        String rssEnvironment = request.getParameter("rssEnvironment");
        String rssType = request.getParameter("rssType");

        String backendServerUrl = CarbonUIUtil.getServerURL(
                getServletConfig().getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        RSSManagerClient client = new RSSManagerClient(cookie, backendServerUrl, configContext,
                request.getLocale());
        RSSInstanceInfo rssIns = null;
        String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        String[] environments=null;
        try {
            rssIns = client.getRSSInstance(rssEnvironment,rssInstanceName,rssType);
            environments=client.getRSSEnvironmentNames();
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }

        if (rssIns != null) {
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.edit.instance.info"/></h2>

        <div id="workArea">

            <form method="post" action="#" name="editInstanceForm"
                  id="editInstanceForm" onsubmit="return validateRSSInstanceProperties('edit')">
                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=rssIns.getName()%>" id="rssInstanceName"
                                               name="rssInstanceName"
                                               size="30" type="text" readonly="readonly"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.environment.name"/><font
                                            color='red'>*</font></td>
                                    <td><label>
                                        <select name="serverEnvironment" id="serverEnvironment" disabled>
                                            <option value="">----SELECT----
                                            </option>
                                            <%for(String environment:environments) {
                                            if(environment.equalsIgnoreCase(rssIns.getEnvironmentName())) {
                                            %>
                                            <option value="<%=environment%>" selected="selected">
                                                <%=environment%>
                                            </option>
                                            <%} else {%>
                                            <option value="<%=environment%>">
                                                <%=environment%>
                                            </option>
                                            <%}}%>
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
                                            <select name="instancetype" id="instancetype" disabled>
                                                <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>"
                                                        <%if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(rssIns.getInstanceType())) {%>
                                                        selected
                                                        <%}%>><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>
                                                </option>
                                                <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>"
                                                        <%if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(rssIns.getInstanceType())) {%>
                                                        selected
                                                        <%}%>><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>
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
                                    <td><label>
                                        <select name="databaseEngine" id="databaseEngine"
                                                onchange="setJDBCValues(this,document)" disabled>
                                            <option value="<%=rssIns.getDbmsType()%>" selected="selected"><%=rssIns.getDbmsType().toUpperCase()%>
                                            </option>
                                        </select>
                                    </label>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.instance.url"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=rssIns.getServerURL()%>" id="serverUrl"
                                               name="serverUrl"
                                               size="60" type="text"></td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.datasource.class.name"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input id="dataSourceClassName" name="dataSourceClassName" class="longInput"
                                               value="<%=rssIns.getDriverClass()%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.username"/><font
                                            color='red'>*</font></td>
                                    <td><input value="<%=rssIns.getUsername()%>" id="username"
                                               name="username"
                                               size="30" type="text"></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.instance.password"/><font
                                            color='red'>*</font></td>
                                    <td><input type="password" id="password"
                                               name="password"
                                               size="30" type="password"
                                               value="<%=""%>"></td>
                                </tr>
                                <tr>
                                    <td align="leftCol-med"><fmt:message
                                            key="rss.manager.confirm.instance.password"/><font
                                            color='red'>*</font></td>
                                    <td>
                                        <input type="password" id="repassword" type="password" name="repassword" class="longInput"
                                               value=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>

                        <td class="buttonRow" colspan="3">
                            <div id="connectionStatusDiv" style="display: none;"></div>
                            <input type="hidden" id="flag" name="flag" value="edit"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.test.connection"/>"
                                   onclick="return testConnection(); return false;"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.save"/>"
                                   onclick="if(validateRSSInstanceProperties()) {dispatchRSSInstanceCreateRequest('edit');} return false;"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'rssInstances.jsp'"/>
                        </td>

                    </tr>
                </table>
            </form>
        </div>
    </div>

    <%
    } else {

    %>
    <script type="text/javascript">
        document.location.href = "rssInstances.jsp";
    </script>
    <%
        }
    %>
</fmt:bundle>
