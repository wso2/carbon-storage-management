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
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeTemplateInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.DatabasePrivilegeSetInfo" %>
<%@ page import="org.wso2.carbon.rssmanager.core.dto.xsd.MySQLPrivilegeSetInfo" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    RSSManagerClient client;
    String flag = request.getParameter("flag");
    String privilegeTemplateName = request.getParameter("privilegeTemplateName");
    String envName = request.getParameter("envName");
    

    //Database privileges
    String selectPriv = request.getParameter("select_priv");
    String insertPriv = request.getParameter("insert_priv");
    String updatePriv = request.getParameter("update_priv");
    String deletePriv = request.getParameter("delete_priv");
    String createPriv = request.getParameter("create_priv");
    String dropPriv = request.getParameter("drop_priv");
    String grantPriv = request.getParameter("grant_priv");
    String referencesPriv = request.getParameter("references_priv");
    String indexPriv = request.getParameter("index_priv");
    String alterPriv = request.getParameter("alter_priv");
    String createTmpTablePriv = request.getParameter("create_tmp_table_priv");
    String lockTablesPriv = request.getParameter("lock_tables_priv");
    String createViewPriv = request.getParameter("create_view_priv");
    String showViewPriv = request.getParameter("show_view_priv");
    String createRoutinePriv = request.getParameter("create_routine_priv");
    String alterRoutinePriv = request.getParameter("alter_routine_priv");
    String executePriv = request.getParameter("execute_priv");
    String eventPriv = request.getParameter("event_priv");
    String triggerPriv = request.getParameter("trigger_priv");
    selectPriv = ("true".equals(selectPriv)) ? "Y" : "N";
    insertPriv = ("true".equals(insertPriv)) ? "Y" : "N";
    updatePriv = ("true".equals(updatePriv)) ? "Y" : "N";
    deletePriv = ("true".equals(deletePriv)) ? "Y" : "N";
    createPriv = ("true".equals(createPriv)) ? "Y" : "N";
    dropPriv = ("true".equals(dropPriv)) ? "Y" : "N";
    grantPriv = ("true".equals(grantPriv)) ? "Y" : "N";
    referencesPriv = ("true".equals(referencesPriv)) ? "Y" : "N";
    indexPriv = ("true".equals(indexPriv)) ? "Y" : "N";
    alterPriv = ("true".equals(alterPriv)) ? "Y" : "N";
    createTmpTablePriv = ("true".equals(createTmpTablePriv)) ? "Y" : "N";
    lockTablesPriv = ("true".equals(lockTablesPriv)) ? "Y" : "N";
    createViewPriv = ("true".equals(createViewPriv)) ? "Y" : "N";
    showViewPriv = ("true".equals(showViewPriv)) ? "Y" : "N";
    createRoutinePriv = ("true".equals(createRoutinePriv)) ? "Y" : "N";
    alterRoutinePriv = ("true".equals(alterRoutinePriv)) ? "Y" : "N";
    executePriv = ("true".equals(executePriv)) ? "Y" : "N";
    eventPriv = ("true".equals(eventPriv)) ? "Y" : "N";
    triggerPriv = ("true".equals(triggerPriv)) ? "Y" : "N";

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    String msg;
    String xml;

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
        	MySQLPrivilegeSetInfo privileges = new MySQLPrivilegeSetInfo();
            privileges.setSelectPriv(selectPriv);
            privileges.setInsertPriv(insertPriv);
            privileges.setUpdatePriv(updatePriv);
            privileges.setDeletePriv(deletePriv);
            privileges.setCreatePriv(createPriv);
            privileges.setDropPriv(dropPriv);
            privileges.setGrantPriv(grantPriv); 
            privileges.setReferencesPriv(referencesPriv); 
            privileges.setIndexPriv(indexPriv);
            privileges.setAlterPriv(alterPriv);
            privileges.setCreateTmpTablePriv(createTmpTablePriv);
            privileges.setLockTablesPriv(lockTablesPriv);
            privileges.setCreateViewPriv(createViewPriv);
            privileges.setShowViewPriv(showViewPriv);
            privileges.setCreateRoutinePriv(createRoutinePriv);
            privileges.setAlterRoutinePriv(alterRoutinePriv);
            privileges.setExecutePriv(executePriv);
            privileges.setEventPriv(eventPriv);
            privileges.setTriggerPriv(triggerPriv); 

            DatabasePrivilegeTemplateInfo template = new DatabasePrivilegeTemplateInfo();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privileges);
            client.createDatabasePrivilegesTemplate(envName, template);

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + template.getName() +
                    "' has been successfully created";
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
    } else if ("drop".equals(flag)) {
        try {
            client.dropDatabasePrivilegesTemplate(envName,privilegeTemplateName);

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + privilegeTemplateName +
                    "' has been successfully dropped";
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>";
            pw.write(xml);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            msg = e.getMessage();
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>" ;
            pw.write(xml);
            pw.flush();
        }
    } else if ("edit".equals(flag)) {
    		MySQLPrivilegeSetInfo privileges = new MySQLPrivilegeSetInfo();
            privileges.setSelectPriv(selectPriv);
            privileges.setInsertPriv(insertPriv);
            privileges.setUpdatePriv(updatePriv);
            privileges.setDeletePriv(deletePriv);
            privileges.setCreatePriv(createPriv);
            privileges.setDropPriv(dropPriv);
            privileges.setGrantPriv(grantPriv);
            privileges.setReferencesPriv(referencesPriv);
            privileges.setIndexPriv(indexPriv);
            privileges.setAlterPriv(alterPriv);
            privileges.setCreateTmpTablePriv(createTmpTablePriv);
            privileges.setLockTablesPriv(lockTablesPriv);
            privileges.setCreateViewPriv(createViewPriv);
            privileges.setShowViewPriv(showViewPriv);
            privileges.setCreateRoutinePriv(createRoutinePriv);
            privileges.setAlterRoutinePriv(alterRoutinePriv);
            privileges.setExecutePriv(executePriv);
            privileges.setEventPriv(eventPriv);
            privileges.setTriggerPriv(triggerPriv);

        try {
            DatabasePrivilegeTemplateInfo template = new DatabasePrivilegeTemplateInfo();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privileges);
            client.editDatabasePrivilegesTemplate(envName,template);

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + privilegeTemplateName +
                    "' has been successfully edited";
            xml = "<Response><Message>" + msg + "</Message><Environment>" + envName + "</Environment></Response>";
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


