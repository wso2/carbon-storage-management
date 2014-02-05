<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.exception.ClusterAdminClientException" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterKeyspaceOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
        ClusterKeyspaceOperationsAdminClient clusterKeyspaceOperationsAdminClient =new ClusterKeyspaceOperationsAdminClient(config.getServletContext(),session);
        clusterKeyspaceOperationsAdminClient.flushKeyspace(hostAddress,keyspace);
        backendStatus.put("success","yes");
    }catch (ClusterAdminClientException e)
    {}
    out.print(backendStatus);
    out.flush();
%>
