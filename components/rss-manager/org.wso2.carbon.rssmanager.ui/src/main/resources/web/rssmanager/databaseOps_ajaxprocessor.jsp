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
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabaseInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String databaseName = request.getParameter("databaseName");
    String flag = request.getParameter("flag");
    String rssInstanceName = request.getParameter("rssInstanceName");
    String envName = request.getParameter("envName");
    String instanceType = request.getParameter("instanceType");

    RSSManagerClient client;
    String msg;
    String xml;

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    
    response.setContentType("text/xml; charset=UTF-8");
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
    
    if ("create".equals(flag)) {
        try {
            DatabaseInfo db = new DatabaseInfo();
            db.setName(databaseName);
            db.setRssInstanceName(rssInstanceName);
            db.setType(instanceType.trim());
            client.createDatabase(envName, db);

            PrintWriter pw = response.getWriter();
            msg = "Database '" + db.getName() + "' has been successfully created";
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>";
            pw.write(xml);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            msg = e.getMessage();
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>";
            pw.write(xml);
            pw.flush();
        }
    } else if ("drop".equals(flag)) {
        try {
            client.dropDatabase(envName,rssInstanceName, databaseName, instanceType.trim());
            PrintWriter pw = response.getWriter();
            msg = "Database '" + databaseName + "' has been successfully dropped";
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>" ;
            pw.write(xml);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            msg = e.getMessage();
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>" ;
            pw.write(xml);
            pw.flush();
        }
    }

%>


