<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
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
    boolean isEditMode = "edit".equals(mode);
    String index = request.getParameter("index");
    String envName = (String) session.getAttribute("envName");
    if (index == null) {
        index = "";
    }

    String cf = request.getParameter("cf");
    if (cf == null) {
        cf = "";
    }

    String cl = request.getParameter("cl");
    if (cl == null) {
        cl = "";
    }

    String keyspace = request.getParameter("keyspace");
    if (keyspace == null) {
        keyspace = "";
    }

    ColumnInformation clInformation = null;
    KeyspaceInformation keyspaceInformation =
            CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
    ColumnFamilyInformation familyInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, cf);
    if (!"".equals(cl)) {
        clInformation = CassandraAdminClientHelper.getColumnInformation(familyInformation, cl);
    }

    if (clInformation == null) {
        clInformation = new ColumnInformation();
    }

    CassandraAdminClientHelper.fillDefaultValuesForCL(clInformation);

    String validationClass =
            CassandraAdminClientHelper.getAliasForValidatorTypeClass(clInformation.getValidationClass());

    String defaultValidationClass =  CassandraAdminClientHelper.getAliasForValidatorTypeClass(
        familyInformation.getDefaultValidationClass());
%>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
            label="cassandra.cl.editor"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

<div id="middle">
    <h2>
        <%if (isEditMode) {%>
        <h2><fmt:message key="cassandra.edit.cl"/> <%=":" + cf%>
        </h2>
        <% } else { %>
        <h2><fmt:message key="cassandra.new.cl"/></h2>
        <% } %>
    </h2>
    <div id="workArea">
        <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
            <tbody>
            <tr>
                <td>
                    <div style="margin-top:10px;">
                        <table border="0" cellpadding="0" cellspacing="0" width="600" id="clTable"
                               class="styledInner">
                            <tr>
                                <td><fmt:message key="cassandra.cl.name"/><font color="red">*</font></td>
                                <td align="left">
                                    <% if ("edit".equals(mode)) { %>
                                    <input id="cl_editor_name" name="cl_editor_name" class="longInput"
                                           value="<%=cl%>" readonly="readonly"/>
                                    <% } else { %>
                                    <input id="cl_editor_name" name="cl_editor_name" class="longInput"
                                           value="<%=cl%>"/>
                                    <% } %>
                                </td>
                            </tr>
                            <%--<tr>--%>
                                <%--<td><fmt:message key="cassandra.field.indextype"/></td>--%>
                                <%--<td align="left">--%>
                                    <%--<input id="cl_editor_indextype" name="cl_editor_indextype" class="longInput"--%>
                                           <%--value="<%=clInformation.getIndexType()%>"/>--%>
                                <%--</td>--%>
                            <%--</tr>--%>
                            <tr>
                                <td><fmt:message key="cassandra.field.indexname"/></td>
                                <td align="left">
                                    <input id="cl_editor_indexname" name="cl_editor_indexname" class="longInput"
                                           value="<%=clInformation.getIndexName()%>"/>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="cassandra.field.validationclass"/></td>
                                <td align="left">
                                    <select id="cl_editor_validationclass" name="cl_editor_validationclass"
                                            class="longInput">
                                       <%if(CassandraAdminClientConstants.COUNTERCOLUMNTYPE.equals(defaultValidationClass)){%>
                                               <option value="<%=CassandraAdminClientConstants.COUNTERCOLUMNTYPE%>" selected="selected">
                                               <fmt:message key="cassandra.field.validator.countercolumn"/></option>
                                       <%}else{%>
                                                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"
                                                        selected="selected">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.long"/></option>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.integer"/></option>
                                                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"
                                                        selected="selected">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.long"/></option>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.integer"/></option>
                                                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>"
                                                        selected="selected">
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
                                                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"
                                                        selected="selected">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.long"/></option>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.integer"/></option>
                                                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"
                                                        selected="selected">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.long"/></option>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.integer"/></option>
                                                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(validationClass)) {%>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>" selected="">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.integer"/></option>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.long"/></option>
                                                <% } else {%>
                                                <option value="<%=CassandraAdminClientConstants.BYTESTYPE%>"
                                                        selected="selected">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.bytes"/></option>
                                                <option value="<%=CassandraAdminClientConstants.ASCIITYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.ascii"/></option>
                                                <option value="<%=CassandraAdminClientConstants.UTF8TYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.utf8"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LEXICALUUIDTYPE%>">
                                                    <fmt:message
                                                            key="cassandra.field.comparator.lexicalUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.TIMEUUIDTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.timeUUID"/></option>
                                                <option value="<%=CassandraAdminClientConstants.LONGTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.long"/></option>
                                                <option value="<%=CassandraAdminClientConstants.INTEGERTYPE%>"><fmt:message
                                                        key="cassandra.field.comparator.integer"/></option>
                                                <% }%>
                                          <%}%>
                                    </select>
                                </td>
                            <tr>
                                <td class="buttonRow" colspan="3">
                                    <input id="saveclButton" class="button" name="saveclButton" type="button"
                                           onclick="saveCL('<%=mode%>','<%=index%>','<%=keyspace%>','<%=cf%>'); return false;"
                                           href="#"
                                           value="<fmt:message key="cassandra.actions.save"/>"/>
                                    <input id="cancelclButton" class="button" name="cancelButton" type="button"
                                           onclick="viewCLs('<%=keyspace%>','<%=cf%>');"
                                           href="#"
                                           value="<fmt:message key="cassandra.actions.cancel"/>"/>

                                </td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</fmt:bundle>
