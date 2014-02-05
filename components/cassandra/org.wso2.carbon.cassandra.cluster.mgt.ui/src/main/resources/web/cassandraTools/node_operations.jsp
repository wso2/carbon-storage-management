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
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.operation.ClusterNodeOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.constants.ClusterUIConstants" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNodeInitialInfo" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<link rel="stylesheet" type="text/css" href="css/clusterTools_ui.css"/>
<%
    String hostAddress =request.getParameter(ClusterUIConstants.HOST_ADDRESS);
    String hostName=request.getParameter(ClusterUIConstants.HOST_NAME);
    String token = null;
    String nodeCount=session.getAttribute(ClusterUIConstants.NODE_COUNT).toString();
    Set<String> dataCenters =(Set<String>)session.getAttribute(ClusterUIConstants.DATA_CENTERS);
    Set<String> snapshotTags=new HashSet<String>();
    String[] snapshots;
    boolean isGossipEnable=true;
    boolean isRPCEnable=true;
    boolean isIncrementalBackUpEnable=false;
    boolean isJoin=true;
    try{
        ClusterNodeOperationsAdminClient clusterNodeOperationsAdminClient =new ClusterNodeOperationsAdminClient(config.getServletContext(),session);
        ProxyNodeInitialInfo proxyNodeInitialInfo=clusterNodeOperationsAdminClient.getNodeInitialInfo(hostAddress);
        isGossipEnable= proxyNodeInitialInfo.getGossipEnable();
        isRPCEnable= proxyNodeInitialInfo.getRPCEnable();
        isIncrementalBackUpEnable= proxyNodeInitialInfo.getIncrementalBackupEnable();
        isJoin= proxyNodeInitialInfo.getJoin();
        token=proxyNodeInitialInfo.getToken();
        snapshots=proxyNodeInitialInfo.getSnapshotNames();
        if(snapshots.length>1)
        {
            int i=0;
            for(String s:snapshots)
            {
                if(i==0)
                {
                    i++;
                    continue;
                }
                i++;
                snapshotTags.add(s.split(ClusterUIConstants.SEPARATOR_SNAPSHOTS)[0]);
            }
        }
        else
        {
            snapshotTags=null;
        }
    }catch (Exception e)
    {
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
        label="cassandra.cluster.node.operation.msg"
        resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<div id="middle">
<h2><fmt:message key="cassandra.cluster.node.operation.msg" />-<%if(!"unknown".equalsIgnoreCase(hostName) && hostName!=null){%>
    <%=hostName%>
    <%}else{%>
    <%=hostAddress%>
    <%}%></h2>
<div id="workArea">
<table class="styledLeft" id="nodeOperationTable">
<thead>
<tr>
    <th><fmt:message key="cassandra.cluster.node.operation.table.header.catagery" /></th>
    <th><fmt:message key="cassandra.cluster.node.operation.header" /></th>
</tr>
</thead>

<tbody>
<tr>
    <td><fmt:message key="cassandra.cluster.node.operation.header.general" />
    </td>
    <td>
        <a href="#" onclick="decommissionNode('<%=nodeCount%>','<%=hostAddress%>')" class="icon-link" style="background-image:url(images/decommission.png);"><fmt:message key="cassandra.cluster.node.operations.decommission" /></a>
        <a href="#" onclick="drainNode('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/drain.png);"><fmt:message key="cassandra.cluster.node.operation.drain" /></a>
        <a href="#" onclick="showTokenForm()" class="icon-link" style="background-image:url(images/move.png);"><fmt:message key="cassandra.cluster.node.operation.move" /></a>
        <a href="#" onclick="performGC('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/garbage_collection.png);"><fmt:message key="cassandra.cluster.node.operation.PerformGC" /></a>
        <%if(!isJoin){%>
        <a href="#" onclick="joinRing('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/join.jpg);"><fmt:message key="cassandra.cluster.node.operation.join" /></a>
        <%}%>

        <div style="clear:both">
            <div id="myDiv" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.move.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.move.newToken"/><span class="required">*</span></td>
                        <td>
                            <input type="text" id="newToken">
                            (<fmt:message key="cassandra.cluster.node.move.currentToken"/><span id="currentToken">:<%=token%></span>)
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Move" onclick="moveNode('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

    </td>
</tr>
<tr class="tableEvenRow">
    <td><fmt:message key="cassandra.cluster.node.operation.header.backUp" />
    </td>
    <td>
        <a href="#" onclick="showTakeSnapShotForm()" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operation.takeSnapShot" /></a>
        <a href="#" onclick="showClearSnapShotForm()" class="icon-link" style="background-image:url(images/clear.png);"><fmt:message key="cassandra.cluster.node.operation.clearSnapShot" /></a>
        <%if(!isIncrementalBackUpEnable){%>
        <a href="#" onclick="enableIncrementalBackUp('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operation.enableIncrementalBackup" /></a>
        <%}else{%>
        <a href="#" onclick="disableIncrementalBackUp('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/stop.jpg);"><fmt:message key="cassandra.cluster.node.operation.disableIncrementalBackup" /></a>
        <%}%>
        <div style="clear:both">
            <div id="divSnapShot" style="display:none">
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
                            <input type="text" id="snapshotTag">
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Snapshot" onclick="takeNodeSnapShot('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div style="clear:both">
            <div id="divClearSnapShot" style="display:none">
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
                            <select name="clearSnapshotTag" id="clearSnapshotTag">
                                <%if(snapshotTags!=null)
                                {
                                    Iterator<String> iterator=snapshotTags.iterator();
                                    String tag;
                                    while(iterator.hasNext())
                                    {
                                        tag=iterator.next();
                                %>
                                <option value="<%=tag%>"><%=tag%></option>
                                <%
                                    }
                                }else{%>
                                <option value="notAvailable"><fmt:message key="cassandra.cluster.node.notAvailable"/></option>
                                <%}%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Clear Snapshot" onclick="clearNodeSnapShot('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </td>
</tr>
<tr>
    <td><fmt:message key="cassandra.cluster.node.operation.header.server" />
    </td>
    <td>
        <%if(isRPCEnable){%>
        <a href="#" onclick="disableRPC('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/stop.jpg);"><fmt:message key="cassandra.cluster.node.operation.stopRPCServer" /></a>
        <%}else{%>
        <a href="#" onclick="enableRPC('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operation.startRPCServer" /></a>
        <%}%>

        <%if(isGossipEnable){%>
        <a href="#" onclick="disableGossip('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/stop.jpg);"><fmt:message key="cassandra.cluster.node.operation.stopGossip" /></a>
        <%}else{%>
        <a href="#" onclick="enableGossip('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operation.startGossip" /></a>
        <%}%>
    </td>
</tr>

<tr class="tableEvenRow">
    <td><fmt:message key="cassandra.cluster.node.operation.header.cache" />
    </td>
    <td>
        <a href="#" onclick="showInvalidateCacheForm()" class="icon-link" style="background-image:url(images/clear.png);"><fmt:message key="cassandra.cluster.node.operation.invalidateRowCache" /></a>
        <a href="#" onclick="showCacheFrom()" class="icon-link" style="background-image:url(images/set.jpg);"><fmt:message key="cassandra.cluster.node.operation.setCache" /></a>
        <div style="clear:both">
            <div id="setCacheForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.cache.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.cache.type"/><span class="required">*</span></td>
                        <td>
                            <select name="cacheType" id="cacheType">
                                <option value="row"><fmt:message key="cassandra.cluster.row"/> </option>
                                <option value="key"><fmt:message key="cassandra.cluster.key"/></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.cache.capacity"/><span class="required">*</span></td>
                        <td>
                            <input type="text" id="cacheCapacity">(MB)
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Set" onclick="setCacheCapacity('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div style="clear:both">
            <div id="invalidateCacheForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.icache.header"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.cache.type"/><span class="required">*</span></td>
                        <td>
                            <select name="invalidateCacheType" id="invalidateCacheType">
                                <option value="row"><fmt:message key="cassandra.cluster.row"/> </option>
                                <option value="key"><fmt:message key="cassandra.cluster.key"/></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Invalidate" onclick="invalidateCache('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </td>
</tr>

<tr>
    <td><fmt:message key="cassandra.cluster.node.operation.other" />
    </td>
    <td>
        <a href="#" onclick="removeToken('<%=hostAddress%>')" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operation.removeToken" /></a>
        <a href="#" onclick="showCompactionForm()" class="icon-link" style="background-image:url(images/stop.jpg);"><fmt:message key="cassandra.cluster.node.operation.stopCompaction" /></a>
        <a href="#" onclick="showRebuildForm()" class="icon-link" style="background-image:url(images/rebuild.png);"><fmt:message key="cassandra.cluster.node.operation.rebuild" /></a>
        <a href="#" onclick="showStreamFrom()" class="icon-link" style="background-image:url(images/set.jpg);"><fmt:message key="cassandra.cluster.node.operation.setStreamThroughput" /></a>
        <a href="#" onclick="showCompactionTForm()" class="icon-link" style="background-image:url(images/set.jpg);"><fmt:message key="cassandra.cluster.node.operation.setCompactionThroughput" /></a>
        <div style="clear:both">
            <div id="stopCompactionForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.compaction.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.compaction.name"/><span class="required">*</span></td>
                        <td>
                            <select name="compactionTypes" id="compactionTypes">
                                <option value="COMPACTION" selected="selected">COMPACTION</option>
                                <option value="VALIDATION">VALIDATION</option>
                                <option value="CLEANUP">CLEANUP</option>
                                <option value="SCRUB">SCRUB</option>
                                <option value="INDEX_BUILD">INDEX_BUILD</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Stop" onclick="stopCompaction('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div style="clear:both">
            <div id="rebuildForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.rebuild.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.rebuild.name"/><span class="required">*</span></td>
                        <td>
                            <select name="availableDataCenters" id="availableDataCenters">
                                <%
                                    Iterator<String> iterator= dataCenters.iterator();
                                    String dataCenter;
                                    while(iterator.hasNext()){
                                        dataCenter=iterator.next();
                                %>
                                <option value="<%=dataCenter%>"><%=dataCenter%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Rebuild" onclick="rebuild('<%=hostAddress%>')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div style="clear:both">
            <div id="setStreamThroughputForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.stream.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.stream.name"/><span class="required">*</span></td>
                        <td>
                            <input type="text" id="streamThroughput">(MB/s)
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Set" onclick="setThroughput('<%=hostAddress%>','stream')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div style="clear:both">
            <div id="setCompactionThroughputForm" style="display:none">
                <table cellpadding="0" cellspacing="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="cassandra.cluster.node.compactionT.header"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><fmt:message key="cassandra.cluster.node.compactionT.name"/><span class="required">*</span></td>
                        <td>
                            <input type="text" id="compactionThroughput">(MB/s)
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button" value="Set" onclick="setThroughput('<%=hostAddress%>','compaction')">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </td>
</tr>
</tbody>
</table>
<table>
    <tr>
        <td><div style="margin-top:0px;">
            <a class="keyspace-operations-icon-link" onclick="displayKeyspaceOperations('<%=hostAddress%>','<%=hostName%>');" href="#"><fmt:message key="cassandra.cluster.keyspace.operations"/></a>
        </div></td>
    </tr>
</table>
</div>
</div>
</fmt:bundle>