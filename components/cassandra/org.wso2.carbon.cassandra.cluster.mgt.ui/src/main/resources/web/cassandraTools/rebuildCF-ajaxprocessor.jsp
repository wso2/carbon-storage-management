<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterColumnFamilyOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        String columnFamily=request.getParameter(ClusterUIConstants.COLUMN_FAMILY);
        ClusterColumnFamilyOperationsAdminClient clusterColumnFamilyOperationsAdminClient =new ClusterColumnFamilyOperationsAdminClient(config.getServletContext(),session);
        clusterColumnFamilyOperationsAdminClient.rebuildCF(hostAddress,keyspace, columnFamily);
        backendStatus.put("success","yes");
    }catch (Exception e) {}
    out.print(backendStatus);
    out.flush();
%>