<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSTreeEntryBean"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.Utils"%>
<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.Stack" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
//The path for viewing the tree. this is set when a user is navigating from details view.
	String treeNavigationPath = request.getParameter("treeNavigationPath");
    String reference = request.getParameter("reference");
    if ("compute".equals(reference) && treeNavigationPath != null) {
        // From detail to tree view
        try {
            String cookie =
                    (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            HDFSAdminClient client =
                    new HDFSAdminClient(config.getServletContext(), request.getSession());
            reference = Utils.buildReference(treeNavigationPath, client, "treeViewRoot");
        } catch (Exception ignored) {
            // We won't expand the collection, if an error occurs.
        }
        session.setAttribute("treeNavigationPath", treeNavigationPath);
        session.setAttribute("reference", reference);
    } else if (treeNavigationPath != null) {
        session.setAttribute("treeNavigationPath", treeNavigationPath);
        session.setAttribute("reference", reference);
        session.setAttribute( "viewType", "std" );
        return;
    } else if (session.getAttribute("treeNavigationPath") != null) {
        treeNavigationPath = (String)session.getAttribute("treeNavigationPath");
        reference = (String)session.getAttribute("reference");
    } else {
        treeNavigationPath = "/";
        reference = "treeViewRoot";
    }
    //set the tree view session
    session.setAttribute( "viewType", "tree" );
%>
<div>
    <jsp:include page="hdfs_tree_ajaxprocessor.jsp">
        <jsp:param name="displayTreeNavigation" value="<%=treeNavigationPath%>" />
        <jsp:param name="rootName" value="treeViewRoot" />
    </jsp:include>
    <%
    	if (treeNavigationPath != null && !treeNavigationPath.equals("") && reference != null) {
                        Stack<String> pathStack = new Stack<String>();
                        String path = treeNavigationPath;
                        if (!path.equals("/")) {
                            boolean isCollection = false;
                            try {
                                String cookie =
                                        (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                                HDFSAdminClient client =
                                        new HDFSAdminClient(config.getServletContext(), request.getSession());
                            } catch (Exception ignored) {                        // We won't expand the collection, if an error occurs.
                            }
                 
                        }
                        //until the path is traversed to the root, add the paths to the stack.
                        while (path != null && !path.equals("/")) {
                            pathStack.push(path);
                            path = Utils.getParentPath(path);
                        }
                        int count = 0;
                        StringBuffer sb = new StringBuffer();
                        //load the subtree for every path.
                        sb.append("<script type=\"text/javascript\">\n" +
                                  "        loadSubTree('/', 'treeViewRoot', 'null', 'false', function() {");
                        int depth = pathStack.size();
                        while (!pathStack.empty()) {
                            String temp = pathStack.pop();
                            count++;
                            sb.append("loadSubTree('");
                            sb.append(temp);
                            sb.append("', '");
                            temp = reference;
                            for (int i = count; i < depth; i++) {
                                if (temp.lastIndexOf("_") > 0) {
                                    temp = temp.substring(0, temp.lastIndexOf("_"));
                                } else {
                                    temp = "treeViewRoot";
                                }
                            }
                            sb.append(temp);
                            sb.append("', 'null', 'false'");
                            if (pathStack.empty()) {
                                sb.append(")");
                            } else {
                                sb.append(", function() {");
                            }
                        }
                        count--;
                        while(count > 0) {
                            count--;
                            sb.append("})");
                        }
                        sb.append("});\n    </script>");
    %><%=sb.toString()%>
    <% } %>
</div>