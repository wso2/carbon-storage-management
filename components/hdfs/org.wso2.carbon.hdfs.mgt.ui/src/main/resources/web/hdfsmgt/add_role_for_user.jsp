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
List<String> userNames = null;
HDFSPermissionAdminClient hdfsPermAdminClient = null;
    try {
    	hdfsPermAdminClient = new HDFSPermissionAdminClient(config.getServletContext(), request.getSession());
    	userNames = hdfsPermAdminClient.getUsersInDomain();
    }catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }
%>
<script type="text/javascript">
    function resetRoleView(){
    	document.addRoleForm.roleName.value="";
    	document.rolePermissions.readAllowed.checked=true;
    	document.rolePermissions.readDeny.checked=false;
    	document.rolePermissions.writeAllowed.checked = true;
    	document.rolePermissions.writeDeny.checked = false;
    	document.rolePermissions.executeAllowed.checked = true;
    	document.rolePermissions.executeDeny.checked = false;
    }
</script>
<fmt:bundle basename="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.user.role"
                       resourceBundle="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">
       <h2><fmt:message key="add.user.role"/></h2>
        <div id="workArea">
	        <form method="post" name="addRoleForm" action="#">
	              <table class="styledLeft">
	                    <thead>
	                    <tr>
	                        <th><fmt:message key="enter.role.details"/></th>
	                    </tr>
	                    </thead>
	                    <tr>
	                        <td class="formRaw">
	                            <table class="normal">
	                                <%
	                                    if(userNames != null && userNames.size() > 0){
	                                %>
	                                <tr>
	                                    <td><fmt:message key="select.user"/></td>
	                                    <td colspan="2"><select onchange="changeBasedOnDomain()" id="user" name="user">
	                                        <%
	                                            for(String username : userNames) {
	                                               
	                                        %>
	                                            <option value="<%=username%>"><%=username%></option>
	                                        <%
	                                          }
	                                            }
	                                        %>
	                                    </select>
	                                    </td>
	                                </tr>
	                                <tr>
	                                    <td><fmt:message key="role.name"/><font color="red">*</font>
	                                    </td>
	                                    <td>
	                                        <%=session.getAttribute("tenantDomain")%>_<input type="text" id="roleName" name="roleName" value=""/>
	                                    </td>
										<!-- td><input type="hidden" name="roleType" value=""/></td -->
	                                </tr>
	                            </table>
	                            <!-- normal table -->
	                        </td>
	                    </tr>
	               </table>
	       </form>
	       <form name="rolePermissions" theme="simple">
	       <table class="styledLeft">
	       <thead><th width="100%"><fmt:message key="add.permissions"/></th></thead>
	       </table>
             <table width="100%" class="styledLeft" border="0" cellpadding="3" cellspacing="0">
               <tbody>
            
                <tr class="perRow">
                    <td colspan="2" align="center" class="subTH"><fmt:message key="read"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="write"/></td>
                    <td colspan="2" align="center" class="subTH"><fmt:message key="execute"/></td>
                </tr>
                 <tr>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                    <td align="center" style="font-weight:normal;" class="middle-header"><fmt:message key="allow"/></td>
                    <td align="center" style="font-weight:normal;"
                        class="lineSeperationRight middle-header"><fmt:message key="deny"/>
                    </td>
                </tr>

                 <tr>
                   <td><input type="checkbox" id="readAllowed"
                               onmouseup="handlePeerCheckbox('readAllowed', 'readDeny')"
                               name="readAllowed"
                               value="ra"
                               checked="true"
                               disabled="true"/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="readDeny"
                                                           onmouseup="handlePeerCheckbox('readDeny', 'readAllowed')"
                                                           name="readDeny"
                                                           value="rd" 
                                                           disabled="true"/>
                    <td><input type="checkbox" id="writeAllowed"
                               onclick="handlePeerCheckbox('writeAllowed', 'writeDeny')"
                               name="writeAllowed"
                               value="wa"
                               checked="true"
                               disabled="true"/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="writeDeny"
                                                           onmouseup="handlePeerCheckbox('writeDeny', 'writeAllowed')"
                                                           name="writeDeny"
                                                           value="wd" 
                                                           disabled="true"/>
                    <td><input type="checkbox" id="executeAllowed"
                               onmouseup="handlePeerCheckbox('executeAllowed', 'executeDeny')"
                               name="executeAllowed"
                               value="da" 
                               checked="true"
                               disabled="true"/>
                    </td>
                    <td class="lineSeperationRight"><input type="checkbox"
                                                           id="executeDeny"
                                                           onmouseup="handlePeerCheckbox('executeDeny', 'executeAllowed')"
                                                           name="executeDeny"
                                                           value="dd" 
                                                           disabled="true"/></td>
                    </tbody>
            </table>
        </form>
          <table>
           <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" onclick= "addRole('roleName')" value="<fmt:message key="finish"/>">
                            <input type="button" class="button" value="<fmt:message key="reset"/>" onclick="resetRoleView();"/>
                        </td>
                    </tr>
         </table>
        </div>
         </div>  
   </fmt:bundle>