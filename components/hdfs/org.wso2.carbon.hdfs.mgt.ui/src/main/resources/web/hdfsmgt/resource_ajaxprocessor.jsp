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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>


<!-- YUI inculudes for rich text editor -->
<link rel="stylesheet" type="text/css"
      href="../yui/build/editor/assets/skins/sam/simpleeditor.css"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/element/element-beta-min.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/editor/simpleeditor-min.js"></script>

<!-- other includes -->

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/hdfs_util.js"></script>
<script src="../global-params.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/hdfsui.css"/>


<fmt:bundle basename="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources">

<style type="text/css">
    .yui-skin-sam h3 {
        font-size: 10px !important;
    }

    .yui-toolbar-container .yui-toolbar-titlebar h2 a {
        font-size: 11px !important;
    }
</style>

<div id="resourceTree" style="display:none" class="resourceTreePage">
    <div class="ajax-loading-message">
        <img src="../resources/images/ajax-loader.gif" align="top"/>
        <span><fmt:message key="resource.tree.loading.please.wait"/> ..</span>
    </div>
</div>
<div id="popup-main" style="display:none" class="popup-main"></div>
</fmt:bundle>