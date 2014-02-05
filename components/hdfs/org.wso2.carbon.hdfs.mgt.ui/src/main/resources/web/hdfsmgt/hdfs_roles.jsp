<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
 <%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSPermissionAdminHDFSServerManagementException"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSPermissionAdminClient"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionBean"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionEntry"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<script type="text/javascript" src="js/hdfs_util.js?v1"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="resources-i18n-ajaxprocessor.jsp"/>

<%
List<HDFSPermissionBean> roles = null;
HDFSPermissionAdminClient hdfsPermAdminClient = null;
    try {
    	hdfsPermAdminClient = new HDFSPermissionAdminClient(config.getServletContext(), request.getSession());
    	roles = hdfsPermAdminClient.getHDFSRolesWithPermissions();
    }catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }
%>
<fmt:bundle basename="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.user.role"
                       resourceBundle="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">
       <h2>HDFS Roles</h2>
        <div id="workArea">
            <table class="styledLeft" id="ksRoleTable" style="margin-left: 0px;" width="100%">
                <thead>
                    <tr>
                        <th width="30%">Role</th>
                        <th>Permissions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (HDFSPermissionBean bean : roles) {
                            if(bean == null){
                                continue;
                            }
                            HDFSPermissionEntry entry = bean.getRolePermissions();
                            if(entry == null){
                                continue;
                            }
                    %>
                    <tr>
                        <td>
                            <%=bean.getRoleName()%>
                        </td>
                        <td>
                            <%
                            String permissionString = "";
                            if(entry.isReadAllowSpecified()){
                                permissionString = permissionString + " r ";
                            } else {
                                permissionString = permissionString + " - ";
                            }
                            if(entry.isWriteAllowSpecified()){
                                permissionString = permissionString + " w ";
                            } else {
                                permissionString = permissionString + " - ";
                            }
                            if(entry.isExecuteAllowSpecified()){
                                permissionString = permissionString + " x ";
                            } else {
                                permissionString = permissionString + " - ";
                            }
                            %>
                            <%=permissionString%>
                        </td>
                     </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>