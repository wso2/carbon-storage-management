<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<%

        recordId = request.getParameter("filePath");
%>

<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
    <script type="text/javascript">
        function validate() {
            var fileName = document.imageUpload.imageFilename.value;
            //            if (fileName == '') {
        <%--CARBON.showErrorDialog('<fmt:message key="select.rule.service"/>');--%>
            //            } else if (fileName.lastIndexOf(".aar") == -1 && fileName.lastIndexOf(".rsl") == -1) {
        <%--CARBON.showErrorDialog('<fmt:message key="select.rule.file"/>');--%>
            //            } else {
            document.imageUpload.submit();
            //            }
        }
    </script>

    <div id="middle">
         <h2><fmt:message key="title.emr"/></h2>
        <h4><fmt:message key="th.record.upload.docs"/></h4>

        <div id="workArea">
            <form method="post" name="imageUpload" action="fileSave.jsp"
                  enctype="multipart/form-data" target="_self">
                <table class="styledLeft">
                    <%--<thead>--%>
                    <%--<tr>--%>
                        <%--<th colspan="2"><fmt:message key="title.upload.images"/> (.png or .gif)</th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="path.to.file"/>
                                            </label>
                                    </td>
                                    <td>
                                        <input type="file" id="imageFilename" name="imageFilename"
                                               size="40"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="hidden" name="recordId" id="recordId"
                                   value="<%=recordId%>">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="button.upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button"
                                   onclick="location.href = '../index.jsp'"
                                   value=" <fmt:message key="button.cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
