<%@page import="org.wso2.carbon.hdfs.mgt.ui.HdfsMgtUiComponentException"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminHDFSServerManagementException" %>

<%  /*read the full path to folder to create*/
    String filePath = request.getParameter("filePath");

    if (filePath == null || "".equals(filePath.trim())) {
        throw new RuntimeException("File Path is null or empty");
    }

    HDFSAdminClient hdfsAdminClient = null;
    try {
        hdfsAdminClient = new HDFSAdminClient(config.getServletContext(), session);
        hdfsAdminClient.createFile(filePath,null);
    } catch (HdfsMgtUiComponentException e) {
   	 response.setStatus(500);
     %><%=e.getMessage()%><%
     return;
    }
%>

