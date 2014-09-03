<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String envName = (String) session.getAttribute("envName");
    String cf = request.getParameter("cf");
    if (cf == null || "".equals(cf.trim())) {
        throw new RuntimeException("Column Family Name is null or empty"); //TODO
    }

    String name = request.getParameter("name");
    if (name == null || "".equals(name.trim())) {
        throw new RuntimeException("Column Name is null or empty"); //TODO
    }
    name = name.trim();
    JSONObject obj = new JSONObject();
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);

        KeyspaceInformation keyspaceInformation =
                (KeyspaceInformation) session.getAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        if (keyspaceInformation != null) {
            ColumnFamilyInformation columnFamilyInformation =
                    CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, cf);
            CassandraAdminClientHelper.removeColumnInformation(columnFamilyInformation, name);
            cassandraKeyspaceAdminClient.updateColumnFamily(columnFamilyInformation, session);
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