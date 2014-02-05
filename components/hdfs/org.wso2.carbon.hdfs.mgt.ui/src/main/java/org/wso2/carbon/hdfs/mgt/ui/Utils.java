/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.hdfs.mgt.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation;
import org.wso2.carbon.registry.core.RegistryConstants;
//import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
//import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
//import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
//import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
//import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static String[] getFolderPathsFromFolderInformations(FolderInformation[] folders)
    {
    	if(folders != null)
    	{
    	  	String[] folderPaths = new String[folders.length];
	    	for( int i = 0; i< folders.length; i++)
	    		{
	    			if(folders[i] != null){
	    				folderPaths[i]=((FolderInformation)folders[i]).getFolderPath();	
	    			}
	    		}
	    	return folderPaths;
    	}
    	return null;
    }

    public static String getResourceContentURL(HttpServletRequest request, String resourcePath) {

        ServletContext context = request.getSession().getServletContext();
        HttpSession session = request.getSession();

        String serverURL = CarbonUIUtil.getServerURL(context, session);
        String serverRoot = serverURL.substring(0, serverURL.length() - "services/".length());
        try {
            resourcePath = URLEncoder.encode(resourcePath, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return serverRoot + "registry/resources?path=" + resourcePath;
    }
    

    public static FolderInformation[] getChildren(int start, int pageLength, FolderInformation[] childPaths) {
        int availableLength = 0;
        if (childPaths != null && childPaths.length > 0) {
            availableLength = childPaths.length - start;
        }
        if (availableLength < pageLength) {
            pageLength = availableLength;
        }

        FolderInformation[] resultChildPaths = new FolderInformation[pageLength];
        System.arraycopy(childPaths, start, resultChildPaths, 0, pageLength);
        return resultChildPaths;
    }

    public static String getFirstPage(int pageNumber, int pageLength, FolderInformation[] allNodes) {
    	if (allNodes == null || allNodes.length == 0) {
            return "";
        } else {
            int start = (pageNumber - 1) * pageLength;
            if (start < 0 || allNodes.length <= start) {
                return "";
            }
            FolderInformation node = (FolderInformation)allNodes[start];
            String startName = null;
            if(node != null)
            {
            	startName = node.getFolderPath();
	            if (startName.indexOf("/") != -1) {
	                startName = startName.substring(startName.lastIndexOf("/") + 1);
	            }
            }
	            return startName;
        }
    }
    
    public static String getLastPage(int pageNumber, int pageLength, FolderInformation[] allNodes) {
        if (allNodes == null || allNodes.length == 0) {
            return "";
        } else {
            int end = (pageNumber - 1) * pageLength + pageLength - 1;
            if (end >= allNodes.length) {
                end = allNodes.length - 1;
            }
            if (end < 0) {
                return "";
            }
            FolderInformation node = (FolderInformation)allNodes[end];
            String endName = null;
            if(node != null){
	            	endName = ((FolderInformation)allNodes[end]).getFolderPath();
	            if (endName.indexOf("/") != -1) {
	                endName = endName.substring(endName.lastIndexOf("/") + 1);
	            }
            }
            return endName;
        }
    }
    
    public static String getResourceDownloadURL(HttpServletRequest request, String resourcePath, String fileName) {
        resourcePath = resourcePath.replace("&", "%2526");
        return "../../hdfsmgt/hdfsContent?path=" + resourcePath+ "&fileName=" +fileName;
    }

    public static String getResourceViewMode(HttpServletRequest request) {
        String mode = request.getParameter("resourceViewMode");
        if (mode == null) {
            mode = "";
        }
        return mode.trim();
    }

    public static String getResourceConsumer(HttpServletRequest request) {
        String consumer = request.getParameter("resourcePathConsumer");
        if (consumer == null) {
            consumer = "";
        }
        return consumer.trim();
    }

    public static String getResourcePath(HttpServletRequest request) {
        String path = request.getParameter("path");
        if (path == null) {
            path = "";
        }
        return path.trim();
    }

    public static String getSynapseRoot(HttpServletRequest request) {
        String path = request.getParameter("synapseroot");
        if (path == null) {
            path = "";
        }
        return path.trim();
    }

    public static String getTargetDivID(HttpServletRequest request) {
        String consumer = request.getParameter("targetDivID");
        if (consumer == null) {
            consumer = "";
        }
        return consumer.trim();
    }

    public static String resolveResourceKey(String completePath, String root) {

        if (completePath == null || "".equals(completePath)) {
            String msg = "Invalid path - Path cannot be null or empty";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        if (root == null || "".equals(root)) {
            return completePath;
        }

        if (!root.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            root += RegistryConstants.PATH_SEPARATOR;
        }

        if (completePath.startsWith(root)) {
            return completePath.substring(root.length(), completePath.length());
        }
        return "";
    }

    public static String getParentPath(String path)
    {
    	if(path.lastIndexOf("/") == -1){
    		return "/";
    	}else{
    		return path.substring(0,  path.lastIndexOf("/"));
    	}
    }
    
    public static String buildReference(String resourcePath, HDFSAdminClient client, 
                                        String rootName) {
        if (resourcePath == null || resourcePath.length() == 0 || resourcePath.equals("/")) {
            return rootName;
        }

        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }

        try {
            String[] parts = resourcePath.split("/");
            parts[0] = "";
            StringBuffer temp = new StringBuffer();
            StringBuffer reference = new StringBuffer(rootName);
            for (int i = 0; i < parts.length - 1; i++) {
            	if(!temp.toString().equals("/"))
            	{
            	   temp.append("/");
            	}
            	 temp.append(parts[i]);
            	
                FolderInformation[] resourceEntry = client.getCurrentUserFSObjects(temp.toString());
               //was resourceEntry.getCollection()
                if (resourceEntry!= null && resourceEntry.length > 0) {
                     for (int j = 0; j < resourceEntry.length; j++) {
                        String childName = ((FolderInformation)resourceEntry[j]).getFolderPath();
                        if (childName == null || childName.length() == 0) {
                            continue;
                        }
                        if (childName.endsWith("/")) {
                            childName = childName.substring(0, childName.length() - 1);
                        }
                        childName = childName.substring(childName.lastIndexOf("/") + 1);
                        if (childName.equals(parts[i + 1])) {
                            reference.append("_").append(j);
                            break;
                        }
                    }
                } else {
                    return null;
                }
            }
            return reference.toString();
        } catch (HdfsMgtUiComponentException e) {
            return null;
        }
    }

//    public static boolean hasDependencies(DependenciesBean dependenciesBean,String srcPath){
//        boolean hasDependencies = false;
//            if(srcPath == null){
//             return false;
//            }
//
//            AssociationBean[] associations = dependenciesBean.getAssociationBeans();
//            if(dependenciesBean != null && dependenciesBean.getAssociationBeans().length > 0) {
//                for(AssociationBean associationBean: associations) {
//                    if("depends".equals(associationBean.getAssociationType())
//                            && associationBean.getSourcePath().equals(srcPath) ) {
//                        hasDependencies = true;
//                        break;
//                    }
//                }
//            }
//      return hasDependencies;
//    }

    public static String[][] getProperties(HttpServletRequest request) {
        return getProperties(request.getParameter("properties"));
    }

    public static String[][] getProperties(String propertyString) {
        if (propertyString != null && propertyString.trim().length() > 0) {
            String[] keySetWithValues = propertyString.split("\\^\\|\\^");
            String[][] propertyArray = new String[keySetWithValues.length][2];

            for (int i = 0; i < keySetWithValues.length; i++) {
                String keySetWithValue = keySetWithValues[i];
                String[] keyAndValue = keySetWithValue.split("\\^\\^");
                propertyArray[i][0] = keyAndValue[0];
                propertyArray[i][1] = keyAndValue[1];
            }

            return propertyArray;
        }
        return new String[0][];
    }
    
   public static FolderInformation[] sortFolderInfomationList(FolderInformation[] folderInfomation)
    {
    	List<FolderInformation> folders = new ArrayList<FolderInformation>();
    	List<FolderInformation> files = new ArrayList<FolderInformation>();
       	if(folderInfomation != null){
        	for (FolderInformation folderInfo : folderInfomation)
        	{
        		if(folderInfo != null && folderInfo.getFolder()){
        			folders.add(folderInfo);
        		}else{
        			files.add(folderInfo);
        		}
        	}
        	folders.addAll(files);
       	}
      	return folders.toArray(new FolderInformation[folders.size()]);
    }
}
