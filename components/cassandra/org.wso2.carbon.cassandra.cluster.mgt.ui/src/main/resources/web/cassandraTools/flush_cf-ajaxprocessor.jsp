<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterColumnFamilyOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
        String columnFamiliesIn=request.getParameter(ClusterUIConstants.COLUMN_FAMILIES);
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        String[] column_families=columnFamiliesIn.toString().trim().split(ClusterUIConstants.SEPARATOR);
        ClusterColumnFamilyOperationsAdminClient clusterColumnFamilyOperationsAdminClient =new ClusterColumnFamilyOperationsAdminClient(config.getServletContext(),session);
        clusterColumnFamilyOperationsAdminClient.flushColumnFamilies(hostAddress,keyspace, column_families);
        backendStatus.put("success","yes");
    }catch (Exception e) {}
    out.print(backendStatus);
    out.flush();
%>