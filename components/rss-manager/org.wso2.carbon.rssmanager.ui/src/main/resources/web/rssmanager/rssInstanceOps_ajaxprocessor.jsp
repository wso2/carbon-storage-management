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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.RSSInstanceInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String rssInstanceName = request.getParameter("rssInstanceName");
    rssInstanceName = (rssInstanceName != null) ? rssInstanceName : "";
    String serverUrl = request.getParameter("serverUrl");
    String username = request.getParameter("username");
    username = (username != null) ? username : "";
    String password = request.getParameter("password");
    password = (password != null) ? password : "";
    String flag = request.getParameter("flag");
    String serverEnvironment=request.getParameter("serverEnvironment");
    String databaseDriverClass=request.getParameter("databaseDriverClass");
    String instanceType=request.getParameter("instancetype");
    String dbmsType;
    RSSManagerClient client;

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());

    if ("create".equals(flag)) {
        try {
            serverUrl = (serverUrl != null) ? RSSManagerHelper.constructConnectionUrl(serverUrl) : "";
            RSSInstanceInfo rssIns = new RSSInstanceInfo();
            rssIns.setName(rssInstanceName);
            rssIns.setServerURL(serverUrl);
            rssIns.setUsername(username);
            rssIns.setPassword(password);
            dbmsType = RSSManagerHelper.getDatabasePrefix(serverUrl);
            rssIns.setDbmsType(dbmsType.toUpperCase());
            rssIns.setEnvironmentName(serverEnvironment);
            rssIns.setDriverClass(databaseDriverClass);
            if(instanceType!=null && !instanceType.isEmpty()) {
                rssIns.setInstanceType(instanceType);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            }
            rssIns.setServerCategory(RSSManagerConstants.LOCAL);
            client.createRSSInstance(rssIns.getEnvironmentName(), rssIns);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            String msg = "Database server '" + rssIns.getName() + "' has been successfully created";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("drop".equals(flag)) {
        try {
            
            //TODO properly set RSS environment name
            String envName = request.getParameter("envName");
            instanceType = request.getParameter("instanceType");
            client.dropRSSInstance(envName,rssInstanceName,instanceType);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            String msg = "Database server '" + rssInstanceName + "' has been successfully dropped";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("edit".equals(flag)) {
        try {
            serverUrl = (serverUrl != null) ? RSSManagerHelper.constructConnectionUrl(serverUrl) : "";
            RSSInstanceInfo rssIns = new RSSInstanceInfo();
            rssIns.setName(rssInstanceName);
            rssIns.setServerURL(serverUrl);
            rssIns.setUsername(username);
            rssIns.setPassword(password);
            rssIns.setEnvironmentName(serverEnvironment);
            dbmsType = RSSManagerHelper.getDatabasePrefix(serverUrl);
            rssIns.setDbmsType(dbmsType.toUpperCase());
            rssIns.setServerCategory(RSSManagerConstants.LOCAL);
            if(instanceType!=null) {
                rssIns.setInstanceType(instanceType);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            }

            client.editRSSInstance(rssIns.getEnvironmentName(), rssIns);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            String msg = "Configuration of the database server '" + rssIns.getName() +
                    "' has been successfully edited";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("testCon".equals(flag)) {
        try {
            serverUrl = (serverUrl != null) ? RSSManagerHelper.constructConnectionUrl(serverUrl) : "";
            String driverClassName = RSSManagerHelper.getDatabaseDriver(serverUrl);
            client.testConnection(driverClassName, serverUrl, username, password);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            String msg = "Database connection is successful for the URL '" + serverUrl +
                    "' with the username '" + username + "' and the driver class '" +
                    driverClassName + "'";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    }

%>

