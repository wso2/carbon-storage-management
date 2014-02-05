<%@page import="org.wso2.carbon.hdfs.mgt.ui.HdfsMgtUiComponentException"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSFileOperationAdminHDFSServerManagementException"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation" %>
<%@ page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminHDFSServerManagementException" %>
<%  /*read the full path to folder to create*/
    String folderPath = request.getParameter("folderPath");
    if (folderPath == null || "".equals(folderPath.trim())) {
        throw new RuntimeException("Folder Path is null or empty");
    }
   // JSONObject fsOpStatus = new JSONObject();
    HDFSAdminClient hdfsAdminClient = null;
    try {
        hdfsAdminClient = new HDFSAdminClient(config.getServletContext(), session);
        boolean fsOperationStatus;

        if (hdfsAdminClient != null) {
           fsOperationStatus = hdfsAdminClient.createFolder(folderPath);
                if (fsOperationStatus) {
                    out.print("File creation success ..!!!");
                    //fsOpStatus.put("CREATE","SUCCESS");
                } else {
                    out.print("File creation failed....xxxxx");
                  //  fsOpStatus.put("CREATE","FAIL");

                }
        }
    }catch (HdfsMgtUiComponentException e) {
    	 response.setStatus(500);
         %><%=e.getMessage()%><%
         return;
    }  
%>

<!-- <script type="text/javascript"> -->
<!--      window.location.href = "../admin/error.jsp"; -->
<!-- </script> -->
<%

%>
