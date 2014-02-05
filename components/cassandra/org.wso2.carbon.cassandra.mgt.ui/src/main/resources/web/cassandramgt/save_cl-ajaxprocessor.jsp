<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    if (name == null || "".equals(name.trim())) {
        throw new RuntimeException("Column Name is null or empty"); //TODO
    }
    String mode = request.getParameter("mode");
    if (name == null || "".equals(name.trim())) {
        throw new RuntimeException("mode is null or empty"); //TODO
    }

    name = name.trim();
    String cf = request.getParameter("cf");
    if (cf == null) {
        cf = "";
    }

    String keyspace = request.getParameter("keyspace");
    if (keyspace == null) {
        keyspace = "";
    }
    JSONObject obj = new JSONObject();

    try {
        KeyspaceInformation keyspaceInformation =
                CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
        ColumnFamilyInformation columnFamilyInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, cf);
        ColumnInformation columnInformation = CassandraAdminClientHelper.getColumnInformation(columnFamilyInformation, name);

        String indexType = request.getParameter("indextype");
        if (indexType == null || "".equals(indexType.trim())) {
            indexType = "keys";
        }

        String indexName = request.getParameter("indexname");
        String validationClass = request.getParameter("validationclass");

        if (mode.equals("add")) {
            if (columnInformation == null) {
                columnInformation = new ColumnInformation();
                columnInformation.setName(name);
                columnInformation.setIndexType(indexType);
                columnInformation.setIndexName(indexName);
                columnInformation.setValidationClass(CassandraAdminClientHelper.getValidatorTypeClassForAlias(validationClass));
                //CassandraAdminClientHelper.addColumnInformation(columnFamilyInformation, columnInformation);
                columnFamilyInformation.addColumns(columnInformation);
                CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
                cassandraKeyspaceAdminClient.updateColumnFamily(columnFamilyInformation, session);
                obj.put("status", "success");
            } else {
                obj.put("status", "fail");
            }
        }

        if (mode.equals("edit")) {
            columnInformation.setIndexType(indexType);
            columnInformation.setIndexName(indexName);
            columnInformation.setValidationClass(CassandraAdminClientHelper.getValidatorTypeClassForAlias(validationClass));
            CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
            cassandraKeyspaceAdminClient.updateColumnFamily(columnFamilyInformation, session);
            obj.put("status", "success");
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