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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<%
    response.setHeader("Cache-Control", "no-cache");

    String keyspace = request.getParameter("name");
    if (keyspace == null) {
        keyspace = "";
    }

    String mode = request.getParameter("mode");
    if (mode == null || "".equals(mode.trim())) {
        if ("".equals(keyspace)) {
            mode = "add";
        } else {
            mode = "edit";
        }
    }

    boolean isEditMode = "edit".equals(mode);
    if (!isEditMode) {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
    }

    KeyspaceInformation keyspaceInformation = null;
    if (!"".equals(keyspace)) {
        try {
            keyspaceInformation =
                    CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg); %>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
        }
    }

    if (keyspaceInformation == null) {
        keyspaceInformation = new KeyspaceInformation();
        keyspaceInformation.setName("");
    }

    if (keyspaceInformation.getReplicationFactor() <= 0) {
        keyspaceInformation.setReplicationFactor(1);   //TODO use a constant
    }

    String alias = CassandraAdminClientHelper.getAliasForReplicationStrategyClass(keyspaceInformation.getStrategyClass());


%>
<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.ks.editor"
            resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2>
            <%if (isEditMode) {%>
            <h2><fmt:message key="cassandra.edit.keyspace"/> <%=" : " + keyspace%>
            </h2>
            <% } else { %>
            <h2><fmt:message key="cassandra.new.keyspace"/></h2>
            <% } %>
        </h2>
        <div id="workArea">
            <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                <tbody>
                <tr>
                    <td>
                        <div style="margin-top:10px;">
                            <table border="0" cellpadding="0" cellspacing="0" width="600" id="ksTable"
                                   class="styledInner">
                                <tr>
                                    <td><fmt:message key="cassandra.field.name"/><font color="red">*</font></td>
                                    <td align="left">
                                        <% if (isEditMode) {%>
                                        <input id="ks_editor_name" name="ks_editor_name" class="longInput"

                                               value="<%=keyspaceInformation.getName().trim()%>"
                                               readonly="readonly"/>
                                        <%} else { %>
                                        <input id="ks_editor_name" name="ks_editor_name" class="longInput"
                                               value="<%=keyspaceInformation.getName().trim()%>"/>
                                        <% } %>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="cassandra.field.ReplicationStrategy"/></td>
                                    <td align="left">
                                        <select class="longInput" name="ks_editor_rs"
                                                id="ks_editor_rs" onchange="onTopologyChanged();">
                                            <% if (CassandraAdminClientConstants.OLD_NETWORK.equals(alias)) { %>
                                            <option value="oldnetwork" selected="selected"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.oldnetwork"/></option>
                                            <option value="simple"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.simple"/></option>
                                            <option value="network"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.network"/></option>
                                            <%} else if (CassandraAdminClientConstants.NETWORK.equals(alias)) {%>
                                            <option value="network" selected="selected"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.network"/></option>
                                            <option value="oldnetwork"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.oldnetwork"/></option>
                                            <option value="simple"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.simple"/></option>
                                            <%} else {%>
                                            <option value="simple" selected="selected"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.simple"/></option>
                                            <option value="oldnetwork"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.oldnetwork"/></option>
                                            <option value="network"><fmt:message
                                                    key="cassandra.field.ReplicationStrategy.network"/></option>
                                            <% } %>
                                        </select>
                                    </td>
                                </tr>
                                <script type="text/javascript">
                                    window.onload = onTopologyChanged;
                                </script>
                                <tr class="simpleStrategy">
                                    <td><fmt:message key="cassandra.field.ReplicationFactor"/></td>
                                    <td align="left" width="84.8%">
                                        <input id="ks_editor_rf" name="ks_editor_rf" class="longInput"
                                               value="<%=keyspaceInformation.getReplicationFactor()%>"/>
                                    </td>
                                </tr>
                                <tr class="networkStrategy">
                                    <td><fmt:message key="cassandra.field.ReplicationFactor"/></td>
                                    <td align="left">
                                        <table id="rfTable" width="60%" class="styledLeft" style="margin-left: 0px;">
                                            <thead>
                                                <tr>
                                                    <th width="20%">
                                                        <fmt:message key="cassandra.field.datacenter.name"/>
                                                    </th>
                                                    <th width="20%">
                                                        <fmt:message key="cassandra.field.ReplicationFactor"/>
                                                    </th>
                                                    <th width="15%"/>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <%
                                                String[] strategyOptions = keyspaceInformation.getStrategyOptions();
                                                if (strategyOptions != null && strategyOptions.length != 0) {
                                                    int i = 1;
                                                    for (String option : strategyOptions){
                                                        String key = option.substring(0,option.lastIndexOf("_"));
                                                        String value = option.substring(option.lastIndexOf("_") + 1);
                                                        if("replication_factor".equals(key)){
                                                            key = "";
                                                        }
                                                    %>
                                                        <tr id="rfTable_<%=i%>">
                                                            <td>
                                                                <input id="dcName_<%=i%>" type="text" name="dcName_<%=i%>" value="<%=key%>"/>
                                                            </td>
                                                            <td>
                                                                <input id="rfCount_<%=i%>" type="text" name="rfCount_<%=i%>" value="<%=value%>"/>
                                                            </td>
                                                            <% if (i == 1) { %>
                                                            <td><span><a onClick="removeDCRow('rfTable_<%=i%>')" style='background-image:
                                                                    url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove</a></span>
                                                            </td>
                                                            <% } else {  %>
                                                            <td>
                                                                <span><a onClick="removeDCRow('rfTable_<%=i%>')" style='background-image:
                                                                    url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove</a></span>
                                                            </td>
                                                            <% } %>
                                                        </tr>

                                                        <script type="text/javascript">
                                                            rfRowNum++;
                                                        </script>
                                                    <%
                                                    }
                                                } else { %>
                                                    <tr id="rfTable_1">
                                                        <td>
                                                            <input type="text" id="dcName_1" name="dcName_1" value=""/>
                                                        </td>
                                                        <td>
                                                            <input type="text" id="rfCount_1" name="rfCount_1" value=""/>
                                                        </td>
                                                        <td>
                                                            <span><a onClick="removeDCRow('rfTable_1');" style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove</a></span>
                                                        </td>
                                                    </tr>
                                                <% } %>
                                            </tbody>
                                        </table>
                                        <span><a onClick="addDCRow();" style='background-image:
                                                 url(../admin/images/add.gif);'class='icon-link addIcon'>Add New</a></span>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="3">
                        <input id="saveKSButton" class="button" name="saveKSButton" type="button"
                               onclick="saveKeyspace('<%=mode%>');"
                               value="<fmt:message key="cassandra.actions.save"/>"/>
                        <input id="cancelKSButton" class="button" name="cancelKSButton" type="button"
                               onclick="location.href = 'cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu';"
                               value="<fmt:message key="cassandra.actions.cancel"/>"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
