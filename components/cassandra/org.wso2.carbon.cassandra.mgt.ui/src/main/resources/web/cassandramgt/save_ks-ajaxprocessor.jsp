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

    String[] ksNames = null;

    try {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        ksNames = cassandraKeyspaceAdminClient.listKeyspacesOfCurrentUSer();
        if (ksNames != null && ksNames.length > 0) {
                for (String  ks : ksNames) {
                    if(name.equalsIgnoreCase(ks)){
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
