<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterNodeOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterNodeStatsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    String version=null;
    try{
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        ClusterNodeStatsAdminClient clusterNodeStatsAdminClient =new ClusterNodeStatsAdminClient(config.getServletContext(),session);
        version=clusterNodeStatsAdminClient.getVersion(hostAddress);
        if(version!=null)
        {
        backendStatus.put("versionC",version);
        backendStatus.put("success","yes");
        }
    }catch (Exception e)
    {}
    out.print(backendStatus);
    out.flush();
%>