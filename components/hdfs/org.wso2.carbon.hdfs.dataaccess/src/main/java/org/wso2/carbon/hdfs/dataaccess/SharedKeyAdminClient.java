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
package org.wso2.carbon.hdfs.dataaccess;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hdfs.dataaccess.interanal.DataAccessServiceDSComponent;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * Shared Key Managment client
 */
public class SharedKeyAdminClient extends AbstractAdmin {

    private static final String USER_ACCESSKEY_ATTR_NAME = "hdfs.user.password";
    private static final String HDFS_AUTH_CONF = "repository" + File.separator + "conf"
                                                   + File.separator + "advanced" + File.separator
                                                   + "hadfs-auth.xml";


    private static Log log = LogFactory.getLog(SharedKeyAdminClient.class);

    public String getSharedKey() throws AxisFault {

        String sharedKey = (String) super.getHttpSession().getAttribute(USER_ACCESSKEY_ATTR_NAME);
        if (sharedKey == null) {
            try {
                synchronized (this) {
                    sharedKey =
                            (String) super.getHttpSession()
                                    .getAttribute(USER_ACCESSKEY_ATTR_NAME);
                    if (sharedKey == null) {

                        OMElement HDFSAuthConfig = loadHdfsAuthConfigXML();
                        String epr = null;
                        OMElement serverEPR = HDFSAuthConfig.getFirstChildWithName(new QName("EPR"));
                        if (serverEPR != null) {
                            String url = serverEPR.getText();
                            if (url != null && !"".equals(url.trim())) {
                                epr = url;
                            }
                        }

                        String username = null;
                        OMElement cassandraUser = HDFSAuthConfig.getFirstChildWithName(new QName("User"));
                        if (cassandraUser != null) {
                            String user = cassandraUser.getText();
                            if (user != null && !"".equals(user.trim())) {
                                username = user;
                            }
                        }

                        String password = null;
                        OMElement cassandraPasswd = HDFSAuthConfig.getFirstChildWithName(new QName("Password"));
                        if (cassandraPasswd != null) {
                            String passwd = cassandraPasswd.getText();
                            if (passwd != null && !"".equals(passwd.trim())) {
                                password = passwd;
                            }
                        }
                        String targetUser = (String) super.getHttpSession().
                                getAttribute(ServerConstants.USER_LOGGED_IN);
                        String targetDomain = (String) super.getTenantDomain();
                        if (targetDomain != null) {
                            targetUser = targetUser + "@" + targetDomain;
                        }
                        sharedKey = UUID.randomUUID().toString();
                        super.getHttpSession().setAttribute(USER_ACCESSKEY_ATTR_NAME, sharedKey);
                        OMElement payload = getPayload(username, password, targetUser, sharedKey);
                        ServiceClient serviceClient = new ServiceClient(DataAccessServiceDSComponent.getConfigCtxService()
                                                                                .getClientConfigContext(), null);
                        Options options = new Options();
                        options.setAction("urn:injectAccessKey");
                        options.setProperty(Constants.Configuration.TRANSPORT_URL, epr);
                        serviceClient.setOptions(options);
                        serviceClient.sendRobust(payload);
                        serviceClient.cleanupTransport();
                    }
                }
            } catch (AxisFault e) {
                sharedKey = null;
                super.getHttpSession().removeAttribute(USER_ACCESSKEY_ATTR_NAME);
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        return sharedKey;
    }

    public static OMElement getPayload(String username, String password, String targetUser,
                                       String accessKey) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://sharedkey.hadoop.carbon.wso2.org", "m0");

        OMElement getKey = factory.createOMElement("giveMeAccessKey", ns);

        OMElement usernameElem = factory.createOMElement("username", ns);
        OMElement passwordElem = factory.createOMElement("password", ns);
        OMElement targetUserElem = factory.createOMElement("targetUser", ns);
        OMElement targetAccessKeyElem = factory.createOMElement("accessKey", ns);

        usernameElem.setText(username);
        passwordElem.setText(password);
        targetUserElem.setText(targetUser);
        targetAccessKeyElem.setText(accessKey);

        getKey.addChild(usernameElem);
        getKey.addChild(passwordElem);
        getKey.addChild(targetUserElem);
        getKey.addChild(targetAccessKeyElem);

        return getKey;
    }

    private OMElement loadHdfsAuthConfigXML() {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + HDFS_AUTH_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + HDFS_AUTH_CONF + ". Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<Hadoop/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            log.error(HDFS_AUTH_CONF + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            log.error("Invalid XML for " + HDFS_AUTH_CONF + " located in " +
                      "the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
            }
        }
        return null;
    }

}
