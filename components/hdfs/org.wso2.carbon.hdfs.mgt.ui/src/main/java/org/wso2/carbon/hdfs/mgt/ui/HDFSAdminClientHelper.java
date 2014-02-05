/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hdfs.mgt.ui;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.List;

/**
 * HDFS Admin Client Helper
 */
public class HDFSAdminClientHelper {

    public static void uploadFile(HttpServletRequest request, HttpSession session)
            throws FileUploadException {

        if (ServletFileUpload.isMultipartContent(request)) {
            ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List fileItems = servletFileUpload.parseRequest(request);
            FileItem fileItem = null;
            String recordId = null;
            String fileComment = null;
            Iterator it = fileItems.iterator();
            while (it.hasNext()) {
                FileItem fileItemTemp = (FileItem) it.next();
                if (!fileItemTemp.isFormField()) {
                    fileItem = fileItemTemp;
                } else if ("fileName".equals(fileItemTemp.getFieldName())) {
                    recordId = fileItemTemp.getString();
                } else if ("filePath".equals(fileItemTemp.getFieldName())) {
                    fileComment = fileItemTemp.getString();
                }
            }
        }
    }
}
