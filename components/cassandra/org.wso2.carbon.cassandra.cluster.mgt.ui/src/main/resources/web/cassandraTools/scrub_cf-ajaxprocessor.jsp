<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterColumnFamilyOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
        String[] column_families=request.getParameter(ClusterUIConstants.COLUMN_FAMILIES).toString().split(ClusterUIConstants.SEPARATOR);
        org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterColumnFamilyOperationsAdminClient clusterColumnFamilyOperationsAdminClient =new org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterColumnFamilyOperationsAdminClient(config.getServletContext(),session);
        clusterColumnFamilyOperationsAdminClient.scrubColumnFamilies(hostAddress,keyspace, column_families);
        backendStatus.put("success","yes");
    }catch (Exception e) {}
    out.print(backendStatus);
    out.flush();
%>