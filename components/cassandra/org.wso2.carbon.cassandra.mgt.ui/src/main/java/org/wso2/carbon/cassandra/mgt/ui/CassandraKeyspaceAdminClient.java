/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.mgt.stub.ks.CassandraKeyspaceAdminStub;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.AuthorizedRolesInformation;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation;
import org.wso2.carbon.cassandra.mgt.stub.ks.xsd.TokenRangeInformation;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpSession;
import java.lang.String;

/**
 * The WS dataaccess to access the Cassandra Admin Service
 */

public class CassandraKeyspaceAdminClient {

    private static final Log log = LogFactory.getLog(CassandraKeyspaceAdminClient.class);

    private CassandraKeyspaceAdminStub cassandraAdminStub;

    public CassandraKeyspaceAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraKeyspaceAdminClient(javax.servlet.ServletContext servletContext,
                                        javax.servlet.http.HttpSession httpSession)
            throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "CassandraKeyspaceAdmin";
        cassandraAdminStub = new CassandraKeyspaceAdminStub(ctx, serviceURL);
        ServiceClient client = cassandraAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     *    Returns Cluster Name
     * @return    cluster name
     * @throws CassandraAdminClientException
     */

    public String getClusterName() throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.getClusterName();
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving Cluster Name !", e, log);
        }
    }

    /**
     * Gets all keyspaces in a cluster
     *
     * @param clusterName The name of the cluster
     * @param username    The name of the current user
     * @param password    The password of the current user
     * @return A <code>String</code> array representing the names of keyspaces
     * @throws CassandraAdminClientException For errors during locating   kepspaces
     */

    public String[] lisKeyspaces(String clusterName, String username, String password)
            throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.listKeyspaces(clusterName, username, password);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving keyspace names !", e, log);
        }
    }

    /**
     * Get all the keyspaces belong to the currently singed up user
     *
     * @return A <code>String</code> array representing the names of keyspaces
     * @throws CassandraAdminClientException For errors during locating  kepspaces
     */
    public String[] listKeyspacesOfCurrentUSer() throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.listKeyspacesOfCurrentUser();
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving keyspace names !", e, log);
        }
    }

    /**
     * Get all the CFs belong to the currently singed up user for a given keyspace
     *
     * @param keyspaceName the name of the keyspace
     * @return A <code>String</code> array representing the names of CFs
     * @throws CassandraAdminClientException For errors during locating CFs
     */
    public String[] listColumnFamiliesOfCurrentUser(String keyspaceName)
            throws CassandraAdminClientException {
        validateKeyspace(keyspaceName);
        try {
            return cassandraAdminStub.listColumnFamiliesOfCurrentUser(keyspaceName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving CF names !", e, log);
        }
    }

    /**
     * Get the CF information for a given cf which belongs to the currently singed up user
     *
     * @param keyspaceName the name of the keyspace
     * @param cfName       the name of the CF
     * @return An instance of <code>ColumnFamilyInformation </code>
     * @throws CassandraAdminClientException For errors during locating the CF
     */
    public ColumnFamilyInformation getColumnFamilyInformationOfCurrentUser(String keyspaceName,
                                                                           String cfName)
            throws CassandraAdminClientException {
        validateKeyspace(keyspaceName);
        validateCF(cfName);
        try {
            return cassandraAdminStub.getColumnFamilyOfCurrentUser(keyspaceName, cfName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving the CF for the name " + cfName, e, log);
        }
    }

    /**
     * Set permissions for a resource
     *
     * @param infoList AuthorizedRolesInformation List
     * @return true if the operation was successful.
     * @throws CassandraAdminClientException For errors during sharing the resource
     */
    public boolean authorizeRolesForResource(AuthorizedRolesInformation[] infoList) throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.authorizeRolesForResource(infoList);
        } catch (Throwable e) {
            throw new CassandraAdminClientException("Error sharing a resource !", e, log);
        }
    }

    /**
     * Clear permissions for a resource
     *
     * @param infoList AuthorizedRolesInformation List
     * @return true if the operation was successful.
     * @throws CassandraAdminClientException For errors during sharing the resource
     */
    public boolean clearResourcePermissions(AuthorizedRolesInformation[] infoList) throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.clearResourcePermissions(infoList);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error clear authorization for a resource !", e, log);
        }
    }

    /**
     * Get all the meta-data belong to the currently singed up user for a given keyspace
     *
     * @param keyspaceName the name of the keyspace
     * @return A <code>KeyspaceInformation</code> representing the meta-data of a keyspace
     * @throws CassandraAdminClientException For errors during locating keyspace
     */
    public KeyspaceInformation getKeyspaceOfCurrentUser(String keyspaceName)
            throws CassandraAdminClientException {
        validateKeyspace(keyspaceName);
        try {
            return cassandraAdminStub.getKeyspaceofCurrentUser(keyspaceName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving keyspace !", e, log);
        }
    }

    /**
     * Create a new keyspace
     *
     * @param keyspaceInformation keyspace information
     * @throws CassandraAdminClientException For errors during adding a keyspace
     */
    public void addKeyspace(KeyspaceInformation keyspaceInformation, HttpSession session)
            throws CassandraAdminClientException {
        validateKeyspaceInformation(keyspaceInformation);
        try {
            cassandraAdminStub.addKeyspace(keyspaceInformation);
        } catch (Exception e) {
            session.setAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE, null);
            throw new CassandraAdminClientException("Error adding the keyspace !", e, log);
        }
    }

    /**
     * Updates an existing keyspace
     *
     * @param keyspaceInformation keyspace information
     * @throws CassandraAdminClientException For errors during adding a keyspace
     */
    public void updateKeyspace(KeyspaceInformation keyspaceInformation, HttpSession session)
            throws CassandraAdminClientException {
        validateKeyspaceInformation(keyspaceInformation);
        try {
            cassandraAdminStub.updatedKeyspace(keyspaceInformation);
        } catch (Exception e) {
            session.setAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE, null);
            throw new CassandraAdminClientException("Error updating the keyspace !", e, log);
        }
    }

    /**
     * Get all users
     *
     * @return A <code>String</code> array representing the names of all the users
     * @throws CassandraAdminClientException For errors during looking up users
     */
    public String[] getAllRoles() throws CassandraAdminClientException {
        try {
            return cassandraAdminStub.getAllRoles();
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error retrieving user names !", e, log);
        }
    }

    /**
     * Remove a keyspace
     *
     * @param keyspaceName the name of the keyspace
     * @return true for success
     * @throws CassandraAdminClientException For errors during removing a keyspace
     */
    public boolean deleteKeyspace(String keyspaceName) throws CassandraAdminClientException {
        validateKeyspace(keyspaceName);
        try {
            return cassandraAdminStub.deleteKeyspace(keyspaceName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error removing the keyspace !", e, log);
        }

    }

    /**
     * Deletes a CF
     *
     * @param keyspaceName     the name of the keyspace
     * @param columnFamilyName CF's name
     * @return true for success
     * @throws CassandraAdminClientException for errors during removing a CF
     */
    public boolean deleteColumnFamily(String keyspaceName, String columnFamilyName)
            throws CassandraAdminClientException {
        validateKeyspace(keyspaceName);
        validateCF(columnFamilyName);
        try {
            return cassandraAdminStub.deleteColumnFamily(keyspaceName, columnFamilyName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Error removing the CF !", e, log);
        }
    }

    /**
     * Add a CF under a given keyspace
     *
     * @param columnFamilyInformation information about a CF
     * @throws CassandraAdminClientException for errors during adding a CF
     */
    public void addColumnFamily(ColumnFamilyInformation columnFamilyInformation, HttpSession session)
            throws CassandraAdminClientException {
        validateColumnFamilyInformation(columnFamilyInformation);
        try {
            cassandraAdminStub.addColumnFamily(columnFamilyInformation);
        } catch (Exception e) {
            session.setAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE, null);
            throw new CassandraAdminClientException("Error adding the CF !", e, log);
        }
    }

    /**
     * Update an existing CF under a given keyspace
     *
     * @param columnFamilyInformation CF information
     * @throws CassandraAdminClientException for errors during adding a CF
     */
    public void updateColumnFamily(ColumnFamilyInformation columnFamilyInformation, HttpSession session)
            throws CassandraAdminClientException {
        validateColumnFamilyInformation(columnFamilyInformation);
        try {
            cassandraAdminStub.updateColumnFamily(columnFamilyInformation);
        } catch (Exception e) {
            session.setAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE, null);
            throw new CassandraAdminClientException("Error updating the CF !", e, log);
        }
    }

    /**
     * Get the token range of a given key space
     *
     * @param keyspace keyspace name
     * @return Token range as a list
     * @throws CassandraAdminClientException for errors during calling backend service
     */
    public TokenRangeInformation[] getTokenRange(String keyspace)
            throws CassandraAdminClientException {
        validateKeyspace(keyspace);
        try {
            return cassandraAdminStub.getTokenRange(keyspace);
        } catch (Exception e) {
            log.error("Error getting the token range of the keyspace : " + keyspace, e);
        }
        return null;
    }

    /**
     * Get permission information of all roles for a given resource (Root/Keyspace/Column Family)
     * @param path
     * @return
     */
    public AuthorizedRolesInformation[] getResourcePermissionsOfRoles(String path){
        try{
            return cassandraAdminStub.getResourcePermissionsOfRoles(path);
        } catch (Exception e){
            log.error("Error retrieving role list" + path,e);
        }
        return new AuthorizedRolesInformation[0];
    }

    /**
     * validate Keyspace Name
     * @param keyspaceName
     * @throws CassandraAdminClientException
     */
    private void validateKeyspace(String keyspaceName) throws CassandraAdminClientException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraAdminClientException("The keyspace name is empty or null", log);
        }
    }

    private void validateKeyspaceInformation(KeyspaceInformation keyspaceInformation)
            throws CassandraAdminClientException {
        if (keyspaceInformation == null) {
            throw new CassandraAdminClientException("The keyspace information is empty or null", log);
        }
        validateKeyspace(keyspaceInformation.getName());
    }

    private void validateColumnFamilyInformation(ColumnFamilyInformation columnFamilyInformation)
            throws CassandraAdminClientException {
        if (columnFamilyInformation == null) {
            throw new CassandraAdminClientException("The column family information is empty or null", log);
        }
        validateKeyspace(columnFamilyInformation.getKeyspace());
        validateCF(columnFamilyInformation.getName());
    }

    private void validateCF(String columnFamilyName) throws CassandraAdminClientException {
        if (columnFamilyName == null || "".equals(columnFamilyName.trim())) {
            throw new CassandraAdminClientException("The column family name name is empty or null", log);
        }
    }
}
