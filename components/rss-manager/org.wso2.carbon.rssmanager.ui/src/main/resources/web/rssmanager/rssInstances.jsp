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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.RSSInstanceInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="RSS Instances"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
	<carbon:jsi18n
			resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.JSResources"
			request="<%=request%>" i18nObjectName="rssmanagerjsi18n"/>
    <script type="text/javascript" src="js/uiValidator.js"></script>
    <%
        RSSManagerClient client = null;
        String rssInstanceName;
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

        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }

        RSSInstanceInfo[] rssInstances;
        if (client != null) {
            try {
                    rssInstances=client.getRSSInstanceList();
    %>
    <div id="middle">
        <h2><fmt:message key="rss.manager.instances"/></h2>

        <div id="workArea">
            <form method="post" action="rssInstances.jsp" name="rssInstanceDataForm">
                <table class="styledLeft" id="instanceTable">
                    <% if (rssInstances.length > 0) { %>
                    <thead>
                    <tr>
                        <th width="20%"><fmt:message key="rss.manager.instance.name"/></th>
                        <th width="20%"><fmt:message key="rss.environment.name"/></th>
                        <th width="20%"><fmt:message key="rss.manager.instance.type"/></th>
                        <th width="20%"><fmt:message key="rss.manager.server.category"/></th>
                        <th width="60%"><fmt:message key="rss.manager.actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (RSSInstanceInfo rssInstance : rssInstances) {
                            if (rssInstance != null) {
                                rssInstanceName = rssInstance.getName();
                    %>
                    <tr id="tr_<%=rssInstanceName%>">
                        <td id="td_<%=rssInstanceName%>"><%=rssInstanceName%>
                        </td>
                        <td id="td_<%=rssInstance.getEnvironmentName()%>"><%=rssInstance.getEnvironmentName()%>
                        </td>
                        <td id="td_<%=rssInstance.getInstanceType()%>"><%=rssInstance.getInstanceType()%>
                        </td>
                        <td id="td_<%=rssInstance.getServerCategory()%>"><%=rssInstance.getServerCategory()%>
                        </td>
                        <td>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"
                               onclick="dispatchEditRSSInstanceRequest('<%=rssInstance.getName()%>','<%=rssInstance.getEnvironmentName()%>','<%=rssInstance.getInstanceType()%>')"><fmt:message
                                    key="rss.manager.edit.instance"/></a>
                            <a class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"
                               href="#"
                               onclick="dispatchDropRSSInstanceRequest('<%=rssInstance.getName()%>','<%=rssInstance.getEnvironmentName()%>','<%=rssInstance.getInstanceType()%>');"><fmt:message
                                    key="rss.manager.drop.instance"/></a>
                        </td>
                    <%
                            }
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="3">No database server instances defined yet..</td>
                    </tr>
                    <%
                                }
                            } catch (Exception e) {
                                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                        CarbonUIMessage.ERROR, request, e);
                            }
                        }
                    %>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td colspan="5">
                            <a class="icon-link"
                               style="background-image:url(../admin/images/add.gif);"
                               href="createRSSInstance.jsp"><fmt:message
                                    key="rss.manager.add.new.instance"/></a>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
            <script type="text/javascript">
                function dispatchEditRSSInstanceRequest(rssInstanceName,environment,type) {
                    document.getElementById('rssInstanceName').value = rssInstanceName;
                    document.getElementById('rssEnvironment').value = environment;
                    document.getElementById('rssType').value = type;
                    document.getElementById('editForm').submit();
                }
            </script>
            <form action="editRSSInstance.jsp" method="post" id="editForm">
                <input id="rssInstanceName" name="rssInstanceName" type="hidden"/>
                <input id="rssEnvironment" name="rssEnvironment" type="hidden"/>
                <input id="rssType" name="rssType" type="hidden"/>
            </form>
        </div>
    </div>
</fmt:bundle>
