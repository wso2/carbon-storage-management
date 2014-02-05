/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.wso2.carbon.hdfs.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSPermissionAdminStub;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionBean;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSPermissionEntry;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.util.ArrayList;
import java.util.List;


public class HDFSPermissionAdminClient {

    private HDFSPermissionAdminStub hdfsPermissionAdminStub;
    private static Log log = LogFactory.getLog(HDFSPermissionAdminClient.class);

    public HDFSPermissionAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public HDFSPermissionAdminClient(javax.servlet.ServletContext servletContext,
                                     javax.servlet.http.HttpSession httpSession) throws HdfsMgtUiComponentException {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws HdfsMgtUiComponentException {
        try {
            String serviceURL = serverURL + "HDFSPermissionAdmin";
            hdfsPermissionAdminStub = new HDFSPermissionAdminStub(ctx, serviceURL);
            ServiceClient client = hdfsPermissionAdminStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            options.setTimeOutInMilliSeconds(10000);
        } catch (Exception e) {
            throw new HdfsMgtUiComponentException("Exception Occurred while initializing " +
                    "HDFSPermissionAdminClient.", e, log);
        }
    }

    public boolean addRole(String roleName, String user, HDFSPermissionBean permission)
            throws HdfsMgtUiComponentException {
        try {
            return hdfsPermissionAdminStub.addRole(roleName, user, permission);
        } catch (Exception e) {
            throw new HdfsMgtUiComponentException("Exception Occurred while adding roles", e, log);
        }
    }

    public List<String> getUsersInDomain() throws HdfsMgtUiComponentException {
        try {
            List<String> domainUsers = new ArrayList<String>();
            String[] users = hdfsPermissionAdminStub.getTenantUsers();
            if (users != null) {
                for (String user : users) {
                    domainUsers.add(user);
                }
            }
            return domainUsers;
        } catch (Exception e) {
            throw new HdfsMgtUiComponentException("Exception Occurred while getting user in domain.", e, log);
        }
    }

    public List<HDFSPermissionBean> getHDFSRolesWithPermissions() throws HdfsMgtUiComponentException {
        try {
            List<HDFSPermissionBean> roles = new ArrayList<HDFSPermissionBean>();
            HDFSPermissionBean[] roleArray = hdfsPermissionAdminStub.getHDFSRolesWithPermissions();
            if (roleArray != null) {
                for (HDFSPermissionBean role : roleArray) {
                    roles.add(role);
                }
            }
            return roles;
        } catch (Exception e) {
            throw new HdfsMgtUiComponentException("Exception Occurred while getting HDFS roles", e, log);
        }
    }


}
