<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("isExist","no");

    KeyspaceInformation[] keyspaces = null;

    try {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        keyspaces = cassandraKeyspaceAdminClient.listKeyspacesOfCurrentUSer((String) session.getAttribute("envName"));
        if (keyspaces != null && keyspaces.length > 0) {
                for (KeyspaceInformation  ks : keyspaces) {
                    if(name.equalsIgnoreCase(ks.getName())){
                      backendStatus.put("isExist","yes");
                    }
                }
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

    out.print(backendStatus);
    out.flush();
%>
