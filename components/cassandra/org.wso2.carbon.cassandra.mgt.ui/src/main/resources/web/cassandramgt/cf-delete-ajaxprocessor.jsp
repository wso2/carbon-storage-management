<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String keyspace = request.getParameter("keyspace");
    String name = request.getParameter("name");
    JSONObject obj = new JSONObject();
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        cassandraKeyspaceAdminClient.deleteColumnFamily(keyspace, name);
        KeyspaceInformation keyspaceInformation =
                (KeyspaceInformation) session.getAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        if (keyspaceInformation != null) {
            CassandraAdminClientHelper.removeColumnFamilyInformation(keyspaceInformation, name);
            obj.put("status", "success");
        } else{
            obj.put("status", "fail");
        }
        out.print(obj);
        out.flush();
    } catch (Exception e) {
        obj.put("status", "fail");
        obj.put("error", e.getMessage());
        if(e.getCause() != null){
            obj.put("cause", e.getCause().getMessage());
        }
        out.print(obj);
        out.flush();
    }

%>