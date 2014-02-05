<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterNodeOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterNodeStatsAdminClient" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.mgt.ui.stats.ClusterColumnFamilyStatsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    int[] thresholds=null;
    try{
        String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
        String columnFamily=request.getParameter(ClusterUIConstants.COLUMN_FAMILY);
        ClusterColumnFamilyStatsAdminClient clusterColumnFamilyStatsAdminClient =new ClusterColumnFamilyStatsAdminClient(config.getServletContext(),session);
        thresholds=clusterColumnFamilyStatsAdminClient.getCompactionThresholds(hostAddress,keyspace,columnFamily);
        if(thresholds!=null)
        {
            backendStatus.put("min",thresholds[0]);
            backendStatus.put("max",thresholds[1]);
            backendStatus.put("success","yes");
        }
    }catch (Exception e)
    {}
    out.print(backendStatus);
    out.flush();
%>