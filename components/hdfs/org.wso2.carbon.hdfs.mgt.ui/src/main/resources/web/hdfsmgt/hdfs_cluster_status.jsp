<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient" %>


<fmt:bundle basename="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.hdfs.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="hdfsjsi18n"/>
    <carbon:breadcrumb
            label="hdfs.fs"
            resourceBundle="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2>Hadoop File System Status </h2>
        <div id="hadoopStatus">
            <%--TO DO hadoop Heart Beat.--%>
        </div>

    </div>
</fmt:bundle>