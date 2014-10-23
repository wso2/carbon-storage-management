<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<%
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String mode = request.getParameter("mode");

    String keyspace = request.getParameter("keyspace");
    String envName = (String) session.getAttribute("envName");
    String clusterName = (String) session.getAttribute("clusterName");

    String index = request.getParameter("index");
    if (index == null) {
        index = "";
    }

    String cf = request.getParameter("cf");
    if (cf == null) {
        cf = "";
    }

    ColumnFamilyInformation cfInformation = null;
    if (!"".equals(keyspace) && !"".equals(cf)) {
        try {
            KeyspaceInformation keyspaceInformation =
                    CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
            cfInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, cf);
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
         %>
        <script type="text/javascript">
             var callbackUrl = "keyspace_dashboard.jsp?name=<%=keyspace%>&cluster=<%=clusterName%>";
             showErrorDialog('<%=e.getMessage()%>', callbackUrl);
         </script>

        <%

        }
    }
    if (cfInformation == null) {
        cfInformation = new ColumnFamilyInformation();
    }
    CassandraAdminClientHelper.fillDefaultValuesForCF(cfInformation);
    boolean isSuperCl = CassandraAdminClientConstants.COLUMN_TYPE_SUPER.equals(cfInformation.getType());
    String comparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getComparatorType());
    String subComparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getSubComparatorType());
    int id = cfInformation.getId();
    boolean isEditMode = "edit".equals(mode);
    String validationClass =
            CassandraAdminClientHelper.getAliasForValidatorTypeClass(cfInformation.getDefaultValidationClass());
    String keyValidationClass =
            CassandraAdminClientHelper.getAliasForValidatorTypeClass(cfInformation.getKeyValidationClass());
%>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
            label="cassandra.cf.editor"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

<div id="middle">
<h2>
    <%if (isEditMode) {%>
    <h2><fmt:message key="cassandra.edit.cf"/> <%=":" + cf%>
    </h2>
    <% } else { %>
    <h2><fmt:message key="cassandra.new.cf"/></h2>
    <% } %>
</h2>
<div id="workArea">
<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
<tbody>
<tr>
<td>
<div style="margin-top:10px;">
<table border="0" cellpadding="0" cellspacing="0" width="600" id="cfTable"
       class="styledInner">
<tr>
    <td><fmt:message key="cassandra.field.name"/><font color="red">*</font></td>
    <td align="left">
        <% if ("edit".equals(mode)) { %>
        <input id="cf_editor_name" name="cf_editor_name" class="longInput"
               value="<%=cf%>" readonly="readonly"/>
        <% } else { %>
        <input id="cf_editor_name" name="cf_editor_name" class="longInput"
               value="<%=cf%>"/>
        <% } %>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.field.comment"/></td>
    <td align="left">
        <input id="cf_editor_comment" name="cf_editor_comment" class="longInput"
               value="<%=cfInformation.getComment()%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.field.columnType"/></td>
    <td align="left">
        <select id="cf_editor_column_type" name="cf_editor_column_type" class="longInput" onchange="selectCLType()">
            <%
                if(!"edit".equals(mode)){
                if (isSuperCl) {%>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_SUPER%>"
                    selected="selected">
                <fmt:message key="cassandra.field.columnType.super"/></option>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_STANDARD%>">
                <fmt:message
                        key="cassandra.field.columnType.standard"/></option>
            <% } else { %>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_STANDARD%>"
                    selected="selected"><fmt:message
                    key="cassandra.field.columnType.standard"/></option>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_SUPER%>">
                <fmt:message key="cassandra.field.columnType.super"/></option>
            <% }}else{
            if (isSuperCl) {%>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_SUPER%>"
                    selected="selected">
                <fmt:message key="cassandra.field.columnType.super"/></option>
            <% } else { %>
            <option value="<%=CassandraAdminClientConstants.COLUMN_TYPE_STANDARD%>"
                    selected="selected"><fmt:message
                    key="cassandra.field.columnType.standard"/></option>
            <%}}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.field.keyValidationclass"/></td>
    <td align="left">
        <select id="cf_editor_keyvalidationclass" name="cf_editor_keyvalidationclass"
                class="longInput">
            <% if (CassandraAdminClientConstants.ASCIITYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(keyValidationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <% } else {%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.field.comparator"/></td>
    <td align="left">
        <select id="cf_editor_column_comparator" name="cf_editor_column_comparator"
                class="longInput">
            <% if (CassandraAdminClientConstants.ASCIITYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.ascii"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.UTF8TYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.LONGTYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.INTEGERTYPE.equals(comparator)) {%>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>" selected=""><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <% }} else {%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.bytes"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }}%>
        </select>
    </td>
</tr>
<tr id="sub_column_comparator_row" style="<%=isSuperCl?"":"display:none;"%>">
    <td><fmt:message key="cassandra.field.subComparator"/></td>
    <td align="left">
        <select id="cf_editor_sub_column_comparator" name="cf_editor_sub_column_comparator"
                class="longInput">
            <% if (CassandraAdminClientConstants.ASCIITYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.ascii"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.UTF8TYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.LONGTYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }} else if (CassandraAdminClientConstants.INTEGERTYPE.equals(subComparator)) {%>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <% }} else {%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.bytes"/></option>
            <%if(!"edit".equals(mode)){%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% }}%>
        </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.field.defaultValidationclass"/></td>
    <td align="left">
        <select id="cf_editor_validationclass" name="cf_editor_validationclass"
                class="longInput">
            <% if (CassandraAdminClientConstants.ASCIITYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>" selected="selected"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% } else if (CassandraAdminClientConstants.COUNTERCOLUMNTYPE.equals(validationClass)) {%>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>" selected=""><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                    key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <% } else {%>
            <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>" selected="selected">
                <fmt:message
                        key="cassandra.field.comparator.bytes"/></option>
            <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                    key="cassandra.field.comparator.ascii"/></option>
            <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                    key="cassandra.field.comparator.utf8"/></option>
            <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.lexicalUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                    key="cassandra.field.comparator.timeUUID"/></option>
            <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                    key="cassandra.field.comparator.long"/></option>
            <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                    key="cassandra.field.comparator.integer"/></option>
            <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>"><fmt:message
                    key="cassandra.field.validator.countercolumn"/></option>
            <% }%>
        </select>
    </td>
</tr>
</table>
</div>
</td>
</tr>
<tr>
    <td class="buttonRow" colspan="3">
        <input id="saveCFButton" class="button" name="saveCFButton" type="button"
               onclick="savecf('<%=mode%>','<%=index%>','<%=clusterName%>','<%=keyspace%>','<%=id%>'); return false;"
               href="#"
               value="<fmt:message key="cassandra.actions.save"/>"/>
        <input id="cancelCFButton" class="button" name="cancelCFButton" type="button"
               onclick="location.href = 'keyspace_dashboard.jsp?name=<%=keyspace%>&cluster=<%=clusterName%>';"
               href="#"
               value="<fmt:message key="cassandra.actions.cancel"/>"/>

    </td>
</tr>
</tbody>
</table>
</div>
</div>
</fmt:bundle>
