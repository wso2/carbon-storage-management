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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/jquery.js"></script>
<script type="text/javascript" src="../admin/js/jquery.form.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>

<fmt:bundle basename="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.explorer.connect"
            resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

<%
  String connectionFailure;
    connectionFailure = request.getParameter("conFailure");
  if(null != connectionFailure && !connectionFailure.trim().isEmpty() && connectionFailure.trim().equalsIgnoreCase("true")){
      %>
       <script type="text/javascript">
        jQuery(document).ready(function () {
            CARBON.showErrorDialog('Connection Error.<br>Please retry with correct connection details.', function () {
                CARBON.closeWindow();
            }, function () {
                CARBON.closeWindow();
            });
        });
    </script>
  <%  }
%>
    <div id="middle">
        <h2>Connect</h2>

        <div id="workArea">
            <form id="connect_form" method="POST" action="cassandra_connect_ajaxprocessor.jsp">
                <table class="carbonFormTable">
                        <%-- <tr>
                            <td>Cluster Name</td>
                            <td><input type="text" name="cluster_name" id="cluster_name"/>
                                <div class="sectionHelp">
                                    Enter the Name of the Cluster eg: ClusterOne.
                                </div>
                            </td>
                        </tr>--%>
                    <tr>
                        <td class="leftCol-med labelField">Connection Url<span
                                class="required">*</span></td>
                        <td><input type="text" name="connection_url" id="connection_url"/>
                        </td>
                        <div class="sectionHelp">
                            Connection URL eg: localhost:9160.
                        </div>
                    </tr>
                    <tr>
                        <td class="labelField">User Name</td>
                        <td><input type="text" name="user_name" id="username"/></td>
                    </tr>
                    <tr>
                        <td class="labelField">Password</td>
                        <td><input type="password" name="password" id="password"/></td>
                    </tr>
                    <tr>
                        <td class="labelField">Maximum Result count</td>
                        <td>
                            <select name="max_row_count">
                                <option value="1000">1000</option>
                                <option value="10000">10000</option>
                                <option value="100000">100000</option>
                            </select>
                        </td>
                    </tr>

                </table>
                <div class="buttonRow">
                    <input type="submit" value="Connect">
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>