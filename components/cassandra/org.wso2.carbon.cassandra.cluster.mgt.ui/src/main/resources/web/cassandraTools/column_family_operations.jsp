<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.info.ClusterKeyspaceInfoAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String keyspace=request.getParameter(ClusterUIConstants.KEYSPACE);
    String hostAddress=request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String[] columnFamilyNames = null;

    try{
        ClusterKeyspaceInfoAdminClient clusterKeyspaceInfoAdminClient =new ClusterKeyspaceInfoAdminClient(config.getServletContext(), session);
        columnFamilyNames= clusterKeyspaceInfoAdminClient.getColumnFamiliesForKeyspace(hostAddress,keyspace);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
            label="cassandra.cluster.tools.column.family.operaions.msg"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.tools.column.family.operaions.msg"/>(<fmt:message key="cassandra.cluster.keyspace"/>-<%=keyspace%>)</h2>

        <div id="workArea">
            <table class="styledLeft" id="columnFamilyOperationTable" width="100%">
                <thead>
                <tr>
                    <th colspan="3"><fmt:message key="cassandra.cluster.tools.column.family.operations.header"/></th>
                </tr>
                </thead>
                <tbody>
                <%if(columnFamilyNames!=null){%>
                            <%
                                for(String columnFamily:columnFamilyNames)
                                {
                            %>
                <tr>
                    <td width="10px" style="text-align:center; !important">

                        <input type="checkbox" name="columnFamily" value="<%=columnFamily%>"  class="chkBox">

                    </td>
                    <td width="200px">
                        <%=columnFamily%>
                    </td>
                    <td>
                        <a href="#" onclick="rebuildCF('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')" class="icon-link" style="background-image:url(images/rebuild.png);"><fmt:message key="cassandra.cluster.operations.cf.rebuild"/></a>
                        <a href="#" onclick="showIndexForm('<%=columnFamily%>')" class="icon-link" style="background-image:url(images/rebuild.png);"><fmt:message key="cassandra.cluster.operations.cf.rebuildWithIndex"/></a>
                        <a href="#" onclick="refreshCF('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')" class="icon-link" style="background-image:url(images/refresh.png);"><fmt:message key="cassandra.cluster.operations.cf.refreshCF"/></a>
                        <a href="#" onclick="showCompactionThresholdForm('<%=columnFamily%>')" class="icon-link" style="background-image:url(images/set.jpg);"><fmt:message key="cassandra.cluster.operations.cf.setCompactionThresholds"/></a>
                        <a href="#" onclick="showCFSnapshotForm('<%=columnFamily%>')" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operation.takeSnapShot"/> </a>
                        <div style="clear:both">
                            <div id="<%=columnFamily%>rebuildWithIndexForm" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.tools.cf.rebuild.table.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.tools.cf.rebuild.table.begin"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=columnFamily%>beginIndex">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.tools.cf.rebuild.table.end"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=columnFamily%>endIndex">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Rebuild" onclick="rebuildWithIndex('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div id="<%=columnFamily%>setCompactionThresholdForm" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.tools.cf.compaction.table.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.tools.cf.compaction.table.min"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=columnFamily%>minThreshold">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.tools.cf.compaction.table.max"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=columnFamily%>maxThreshold">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Set" onclick="setCompactionThresholds('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div id="<%=columnFamily%>CFSnapshotForm" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2">Column Family Snapshot </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td>Tag<span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=columnFamily%>CFSnapshotTag">
                                        </td>
                                    </tr>

                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Snapshot" onclick="takeCFNodeSnapShot('<%=hostAddress%>','<%=keyspace%>','<%=columnFamily%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                        <%}%>
                <%}else{%>
                <tr>
                  <td>
                      <div style="padding-left:10px;color:#666666;font-style:italic;"><fmt:message key="cassandra.cluster.tools.column.family.exists"/></div>
                  </td>
                </tr>
                <%}%>
            </table>
            <br/>
            <table id="columnFamilyOperationLastRow">
                <td colspan="3">
                    <a href="#" onclick="repairColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/repair.png);"><fmt:message key="cassandra.cluster.operations.repair"/></a>
                    <a href="#" onclick="compactColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/compact.png);"><fmt:message key="cassandra.cluster.operations.compacr"/></a>
                    <a href="#" onclick="flushColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/flush.png);"><fmt:message key="cassandra.cluster.operations.flush"/></a>
                    <a href="#" onclick="cleanUpColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/cleanUp.png);"><fmt:message key="cassandra.cluster.operations.cleanup"/></a>
                    <a href="#" onclick="scrubColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/scrub.jpg);"><fmt:message key="cassandra.cluster.operations.scrub"/></a>
                    <a href="#" onclick="upgradeSSTablesColumnFamily('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/upgrade.jpg);"><fmt:message key="cassandra.cluster.operations.upgradeSSTables"/></a>
                </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>