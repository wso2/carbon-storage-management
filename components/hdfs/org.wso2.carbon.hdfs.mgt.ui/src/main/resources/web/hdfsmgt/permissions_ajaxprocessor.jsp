

<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.HDFSPermissionAdminHDFSServerManagementException"%>
<%@page import="java.rmi.RemoteException"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionEntry"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionBean"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSPermissionAdminClient"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient"%>

<%
	String userName = request.getParameter("userName");
    String roleName = request.getParameter("roleName");
    boolean readAllowed = Boolean.parseBoolean(request.getParameter("readAllowed"));
    boolean  writeAllowed = Boolean.parseBoolean(request.getParameter("writeAllowed"));
    boolean executeAllowed = Boolean.parseBoolean(request.getParameter("executeAllowed"));
    HDFSPermissionAdminClient hdfsPermissionAdminClient = null;

    	hdfsPermissionAdminClient = new HDFSPermissionAdminClient(config.getServletContext(), session);
    	HDFSPermissionBean permissionBean = new HDFSPermissionBean();

    	HDFSPermissionEntry permissionEntry = new HDFSPermissionEntry();
    	permissionEntry.setReadAllow(readAllowed);
    	permissionEntry.setWriteAllow(writeAllowed);
    	permissionEntry.setExecuteAllow(executeAllowed);
    	permissionBean.setRolePermissions(permissionEntry);
    	   
    	permissionBean.setUserName(userName);
    	permissionBean.setRoleName(roleName);
    	try{
        	hdfsPermissionAdminClient.addRole(roleName, userName, permissionBean);
    	}catch(Exception e)
    	{
    		 CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
    	        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    	}
%>