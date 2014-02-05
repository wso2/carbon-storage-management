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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
        import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterNodeOperationsAdminClient" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.info.ClusterKeyspaceInfoAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<link href="css/clusterTools_ui.css" rel="stylesheet" media="all"/>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String[] ksNames = null;
    String tableStyle="display:none;";
    String hostAddress =null;
    String hostName = null;
    Map<String,Set<String>> snapshotTagsByKeypace = null;
    ClusterKeyspaceInfoAdminClient clusterKeyspaceInfoAdminClient =null;
    try {
        hostAddress =request.getParameter(ClusterUIConstants.HOST_ADDRESS);
        hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
        clusterKeyspaceInfoAdminClient =new ClusterKeyspaceInfoAdminClient(config.getServletContext(), session);
        ksNames = clusterKeyspaceInfoAdminClient.getKeyspaces(hostAddress);
        if (ksNames != null && ksNames.length > 0) {
            tableStyle="";
        }
        ClusterNodeOperationsAdminClient clusterNodeOperationsAdminClient =new ClusterNodeOperationsAdminClient(config.getServletContext(),session);
        String[] snapshotTags=null;
        snapshotTags=clusterNodeOperationsAdminClient.getSnapshotTags(hostAddress);
        snapshotTagsByKeypace=new HashMap<String, Set<String>>();
        if(snapshotTags.length>0)
        {
            Set<String> tempTagNames;
            int i=0;
            String[] temp;
            for(String s:snapshotTags)
            {
                if(i ==0)
                {
                    i++;
                    continue;
                }
                temp=s.split(ClusterUIConstants.SEPARATOR_SNAPSHOTS);
                if(temp.length>1)
                {
                    if((snapshotTagsByKeypace.get(temp[1]))!=null)
                    {
                        tempTagNames=snapshotTagsByKeypace.get(temp[1]);
                        tempTagNames.add(temp[0]);
                        snapshotTagsByKeypace.put(temp[1],tempTagNames);
                    }
                    else
                    {
                        tempTagNames=new HashSet<String>();
                        tempTagNames.add(temp[0]);
                        snapshotTagsByKeypace.put(temp[1],tempTagNames);
                    }
                }
                i++;
            }
        }
        else
        {
            snapshotTagsByKeypace=null;
        }
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
            label="cassandra.cluster.tools.keyspaces.operations.msg"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="cassandra.cluster.tools.keyspaces.operations.msg" />(<fmt:message key="cassandra.cluster.node"/>-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
            <%=hostName%>
            <%}else{%>
            <%=hostAddress%>
            <%}%>)</h2>
        <div id="workArea">
            <table class="styledLeft" id="columnFamilyOperationTable" style="<%=tableStyle%>">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.keyspace"/> </th>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.operations"/> </th>
                </tr>
                </thead>
                <%
                    if(ksNames!=null)
                    {
                        int count=0;
                        for(String keyspace:ksNames)
                        {
                            count++;
                %>
                <tbody>
                        <%if(count%2==0){%>
                <tr class="tableEvenRow">
                            <%}else{%>
                <tr>
                    <%}%>
                    <td><a href="#" onclick="displayColumnFamlilyOperations('<%=hostAddress%>','<%=keyspace%>','<%=hostName%>')" ><%=keyspace%></a>
                    <td>
                        <a href="#" onclick="repairKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/repair.png);"><fmt:message key="cassandra.cluster.operations.repair"/></a>
                        <a href="#" onclick="compactKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/compact.png);"><fmt:message key="cassandra.cluster.operations.compacr"/></a>
                        <a href="#" onclick="flushKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/flush.png);"><fmt:message key="cassandra.cluster.operations.flush"/></a>
                        <a href="#" onclick="cleanUpKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/cleanUp.png);"><fmt:message key="cassandra.cluster.operations.cleanup"/></a>
                        <a href="#" onclick="scrubKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/scrub.jpg);"><fmt:message key="cassandra.cluster.operations.scrub"/></a>
                        <a href="#" onclick="upgradeSSTablesKeyspace('<%=hostAddress%>','<%=keyspace%>')" class="icon-link" style="background-image:url(images/upgrade.jpg);"><fmt:message key="cassandra.cluster.operations.upgradeSSTables"/></a>
                        <a href="#" onclick="showKSTakeSnapShotForm('<%=keyspace%>')" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operation.takeSnapShot" /></a>
                        <a href="#" onclick="showKSClearSnapShotForm('<%=keyspace%>')" class="icon-link" style="background-image:url(images/clear.png);"><fmt:message key="cassandra.cluster.node.operation.clearSnapShot" /></a>
                        <div style="clear:both">
                            <div id="<%=keyspace%>DivTake" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.node.snapshot.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.node.snapshot.tag"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="<%=keyspace%>TakeTag">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Snapshot" onclick="takeKSNodeSnapShot('<%=hostAddress%>','<%=keyspace%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div style="clear:both">
                            <div id="<%=keyspace%>DivClear" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.node.clearSnapshot.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.node.clearSnapshot.tag"/><span class="required">*</span></td>
                                        <td>
                                            <select name="ClearSnapshotTagKS" id="ClearSnapshotTagKS">
                                                <%if(snapshotTagsByKeypace!=null){
                                                    Set<String> temp= snapshotTagsByKeypace.get(keyspace);
                                                    if(temp!=null)
                                                    {
                                                        Iterator<String> iterator=temp.iterator();
                                                        String tag;
                                                        while(iterator.hasNext()){
                                                            tag =iterator.next();
                                                %>
                                                <option value="<%=tag%>" selected="selected"><%=tag%></option>
                                                <%}}else{%>
                                                <option value="NotAvailable1">Not Available</option>
                                                <%}}else{%>
                                                <option value="NotAvailable2">Not Available</option>
                                                <%}%>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Clear" onclick="clearKSNodeSnapShot('<%=hostAddress%>','<%=keyspace%>')">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                        <%}
                 }%>
            </table>

        </div>
    </div>
</fmt:bundle>