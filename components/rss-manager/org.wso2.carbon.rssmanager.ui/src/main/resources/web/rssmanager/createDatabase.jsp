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
            label="Create database"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <%
        RSSManagerClient client = null;
        int systemRSSInstanceCount = 0;
        RSSInstanceInfo[] rssInstances = new RSSInstanceInfo[0];
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        String envName = request.getParameter("envName");
        String instanceType = request.getParameter("instanceType");
        String[] environments = (String[]) session.getAttribute("environments");

        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext,
                    request.getLocale());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                    CarbonUIMessage.ERROR, request, e);
        }
        if (envName == null) {
            envName = environments[0];
        }
        if(instanceType==null || instanceType.isEmpty()) {
            instanceType= RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM;
        }
       
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.new.database"/></h2>

        <div>

        </div>

        <div id="workArea">
            <form method="post" action="#" name="addDatabaseForm"
                  id="addDatabaseForm">
                <%
                    if (client != null) {
                        try {
                            systemRSSInstanceCount = client.getSystemRSSInstanceCount(envName);
                            rssInstances = client.getRSSInstanceList(envName);
                        } catch (Exception e) {
                            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                    CarbonUIMessage.ERROR, request, e);
                        }
                        }
                %>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>Create New Database</th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                </tr>
                                <tr>
                                    <td class="leftCol-med">
                                            <fmt:message key="rss.environment.name"/><font
                                            color='red'>*</font>
                                    <td>
                                        <select id="envCombo" name="envCombo"
                                                onchange="onComboChange(this)">
                                            <%
                                                for (String env : environments) {
                                                    if (envName != null && env.equals(envName.trim())) {
                                            %>
                                            <option id="<%=env%>" value="<%=env%>"
                                                    selected="selected"><%=env%>
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
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.rss.instance.type"/><font
                                            color='red'>*</font></td>
                                    <td><select id="instanceTypes"
                                                name="instanceTypes"  onchange="onInstanceTypeChange(this)">
                                        <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>"
                                                <%if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(instanceType)){%> selected
                                                <%}%>><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM%>
                                        </option>
                                        <option value="<%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>"
                                                <%if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(instanceType)){%> selected
                                                <%}%>
                                                ><%=RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED%>
                                        </option>
                                    </select></td>
                                </tr>
                                <%
                                    if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(instanceType)){
                                %>
                                <tr>
                                <td class="leftCol-med">
                                        <fmt:message key="rss.manager.instance.name"/><font
                                        color='red'>*</font>
                                <td>
                                    <select id="rssInstances" name="rssInstances">
                                            <option id="SELECT" value="SELECT">---SELECT---</option>
                                        <%
                                            for (RSSInstanceInfo rssInstanceInfo : rssInstances) {
                                                if (rssInstanceInfo.getName() != null &&
                                                        RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(rssInstanceInfo.getInstanceType())) {
                                        %>
                                        <option id="<%=rssInstanceInfo.getName()%>" value="<%=rssInstanceInfo.getName()%>">
                                            <%=rssInstanceInfo.getName()%>
                                        </option>
                                        <%}}%>
                                    </select>
                                </td>
                                </tr>
                                <%}%>
                                <tr>
                                    <td align="left"><fmt:message key="rss.manager.db.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="databaseName"
                                               name="databaseName"
                                               size="30" type="text"><font
                                            color='black'>
                                        <%if(!RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equalsIgnoreCase(instanceType)){%>
                                        <%=(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ? "" : "_" + RSSManagerHelper.processDomainName(tenantDomain)%>
                                        <%}%>
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
                                   onclick="return createDatabase('<%=envName%>');return false;"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'databases.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        function onComboChange(combo) {
            var opt = combo.options[combo.selectedIndex].value;
            window.location = 'createDatabase.jsp?envName=' + opt;
        }
        function onInstanceTypeChange(combo) {
            var opt = combo.options[combo.selectedIndex].value;
            window.location = 'createDatabase.jsp?envName=<%=envName%>&instanceType='+opt;
        }
    </script>
</fmt:bundle>
