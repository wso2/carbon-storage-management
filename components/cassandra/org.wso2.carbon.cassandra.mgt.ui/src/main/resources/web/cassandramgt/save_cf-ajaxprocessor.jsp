<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    String keyspace = request.getParameter("keyspace");
    String mode = request.getParameter("mode");
    String comment = request.getParameter("comment");
    String type = request.getParameter("type");
    String comparator = request.getParameter("comparator");
    String subComparator = request.getParameter("subcomparator");
    String keyCacheSize = request.getParameter("keycachesize");
    String rowCacheSize = request.getParameter("rowcachesize");
    String rowCacheTime = request.getParameter("rowcachetime");
    String gcGrace = request.getParameter("gcGrace");
    String minThreshold = request.getParameter("minThreshold");
    String maxThreshold = request.getParameter("maxThreshold");
    String validationClass = request.getParameter("validationclass");
    String keyValidationClass = request.getParameter("keyvalidationclass");
    String id = request.getParameter("id");
    String envName = (String) session.getAttribute("envName");

    JSONObject backendStatus = new JSONObject();
    boolean isColumnFamilyExist = false;

    try {
        KeyspaceInformation keyspaceInformation =
                CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
        ColumnFamilyInformation columnFamilyInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, name);

        if (columnFamilyInformation == null) {
            columnFamilyInformation = new ColumnFamilyInformation();
        } else {
            isColumnFamilyExist = true;
        }

        columnFamilyInformation.setName(name);
        columnFamilyInformation.setKeyspace(keyspace);

        int idAsInt = 0;
        if (id != null) {
            try {
                idAsInt = Integer.parseInt(id.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        if (idAsInt > 0) {
            columnFamilyInformation.setId(idAsInt);
        }

        int gcGraceAsInt = CassandraAdminClientConstants.DEFAULT_GCGRACE;
        if (gcGrace != null) {
            try {
                gcGraceAsInt = Integer.parseInt(gcGrace.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setGcGraceSeconds(gcGraceAsInt);

        int maxThresholdAsInt = CassandraAdminClientConstants.DEFAULT_MAX_COMPACTION_THRESHOLD;
        if (maxThreshold != null) {
            try {
                maxThresholdAsInt = Integer.parseInt(maxThreshold.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setMaxCompactionThreshold(maxThresholdAsInt);

        int minThresholdAsInt = CassandraAdminClientConstants.DEFAULT_MIN_COMPACTION_THRESHOLD;
        if (minThreshold != null) {
            try {
                minThresholdAsInt = Integer.parseInt(minThreshold.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setMinCompactionThreshold(minThresholdAsInt);

        int rawCacheTimeAsInt = CassandraAdminClientConstants.DEFAULT_RAW_CACHE_TIME;
        if (rowCacheTime != null) {
            try {
                rawCacheTimeAsInt = Integer.parseInt(rowCacheTime.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setRowCacheSavePeriodInSeconds(rawCacheTimeAsInt);

        double rowCache = 0;
        if (rowCacheSize != null) {
            try {
                rowCache = Double.parseDouble(rowCacheSize.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setRowCacheSize(rowCache);

        double keyCache = 0;
        if (keyCacheSize != null) {
            try {
                keyCache = Double.parseDouble(keyCacheSize.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        columnFamilyInformation.setKeyCacheSize(keyCache);

        columnFamilyInformation.setComment(comment);
        columnFamilyInformation.setType(type);
        columnFamilyInformation.setComparatorType(comparator);
        columnFamilyInformation.setSubComparatorType(subComparator);
        columnFamilyInformation.setDefaultValidationClass(
                CassandraAdminClientHelper.getValidatorTypeClassForAlias(validationClass));
        columnFamilyInformation.setKeyValidationClass(
                CassandraAdminClientHelper.getValidatorTypeClassForAlias(keyValidationClass));
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient =
                new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        if ("add".equals(mode)) {
            if (!isColumnFamilyExist) {
                cassandraKeyspaceAdminClient.addColumnFamily(columnFamilyInformation, session);
                CassandraAdminClientHelper.addColumnFamilyInformation(keyspaceInformation, columnFamilyInformation);
                backendStatus.put("status", "success");
            } else {
                backendStatus.put("status", "fail");
            }
        } else if ("edit".equals(mode)) {
            cassandraKeyspaceAdminClient.updateColumnFamily(columnFamilyInformation, session);
            backendStatus.put("status", "success");
        }
        out.print(backendStatus);
        out.flush();
    } catch (Exception e) {
        backendStatus.put("status", "fail");
        backendStatus.put("error", e.getMessage());
        backendStatus.put("cause", e.getCause().getMessage());
        out.print(backendStatus);
        out.flush();
    }
%>