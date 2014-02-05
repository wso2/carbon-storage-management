/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains util methods common to both FE and the BE.
 */
public class RSSManagerHelper {

    private static List<String> userPrivilegeList = new ArrayList<String>();

    private static List<String> databasePrivilegeList = new ArrayList<String>();

    private static List<String> boolResponsePrivilegeList = new ArrayList<String>();

    private static List<String> strResponsePrivilegeList = new ArrayList<String>();

    private static List<String> intResponsePrivilegeList = new ArrayList<String>();

    private static List<String> blobPrivilegeList = new ArrayList<String>();

    static {
        userPrivilegeList.add(RSSManagerConstants.SELECT_PRIV);
        userPrivilegeList.add(RSSManagerConstants.INSERT_PRIV);
        userPrivilegeList.add(RSSManagerConstants.UPDATE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.DELETE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.CREATE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.DROP_PRIV);
        userPrivilegeList.add(RSSManagerConstants.RELOAD_PRIV);
        userPrivilegeList.add(RSSManagerConstants.SHUTDOWN_PRIV);
        userPrivilegeList.add(RSSManagerConstants.PROCESS_PRIV);
        userPrivilegeList.add(RSSManagerConstants.FILE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.GRANT_PRIV);
        userPrivilegeList.add(RSSManagerConstants.REFERENCES_PRIV);
        userPrivilegeList.add(RSSManagerConstants.INDEX_PRIV);
        userPrivilegeList.add(RSSManagerConstants.ALTER_PRIV);
        userPrivilegeList.add(RSSManagerConstants.SHOW_DB_PRIV);
        userPrivilegeList.add(RSSManagerConstants.SUPER_PRIV);
        userPrivilegeList.add(RSSManagerConstants.CREATE_TMP_TABLE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.LOCK_TABLES_PRIV);
        userPrivilegeList.add(RSSManagerConstants.EXECUTE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.REPL_SLAVE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.REPL_CLIENT_PRIV);
        userPrivilegeList.add(RSSManagerConstants.CREATE_VIEW_PRIV);
        userPrivilegeList.add(RSSManagerConstants.SHOW_VIEW_PRIV);
        userPrivilegeList.add(RSSManagerConstants.CREATE_ROUTINE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.ALTER_ROUTINE_PRIV);
        userPrivilegeList.add(RSSManagerConstants.CREATE_USER_PRIV);
        userPrivilegeList.add(RSSManagerConstants.EVENT_PRIV);
        userPrivilegeList.add(RSSManagerConstants.TRIGGER_PRIV);
        userPrivilegeList.add(RSSManagerConstants.SSL_TYPE);
        userPrivilegeList.add(RSSManagerConstants.SSL_CIPHER);
        userPrivilegeList.add(RSSManagerConstants.X509_ISSUER);
        userPrivilegeList.add(RSSManagerConstants.X509_SUBJECT);
        userPrivilegeList.add(RSSManagerConstants.MAX_QUESTIONS);
        userPrivilegeList.add(RSSManagerConstants.MAX_UPDATES);
        userPrivilegeList.add(RSSManagerConstants.MAX_CONNECTIONS);
        userPrivilegeList.add(RSSManagerConstants.MAX_USER_CONNECTIONS);

        databasePrivilegeList.add(RSSManagerConstants.SELECT_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.INSERT_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.UPDATE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.DELETE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.CREATE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.DROP_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.GRANT_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.REFERENCES_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.INDEX_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.ALTER_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.CREATE_TMP_TABLE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.LOCK_TABLES_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.CREATE_VIEW_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.SHOW_VIEW_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.CREATE_ROUTINE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.ALTER_ROUTINE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.EXECUTE_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.EVENT_PRIV);
        databasePrivilegeList.add(RSSManagerConstants.TRIGGER_PRIV);

        boolResponsePrivilegeList.add(RSSManagerConstants.SELECT_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.INSERT_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.UPDATE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.DELETE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.CREATE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.DROP_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.RELOAD_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.SHUTDOWN_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.PROCESS_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.FILE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.GRANT_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.REFERENCES_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.INDEX_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.ALTER_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.SHOW_DB_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.SUPER_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.CREATE_TMP_TABLE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.LOCK_TABLES_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.EXECUTE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.REPL_SLAVE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.REPL_CLIENT_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.CREATE_VIEW_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.SHOW_VIEW_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.CREATE_ROUTINE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.ALTER_ROUTINE_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.CREATE_USER_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.EVENT_PRIV);
        boolResponsePrivilegeList.add(RSSManagerConstants.TRIGGER_PRIV);

        strResponsePrivilegeList.add(RSSManagerConstants.SSL_TYPE);

        intResponsePrivilegeList.add(RSSManagerConstants.MAX_QUESTIONS);
        intResponsePrivilegeList.add(RSSManagerConstants.MAX_UPDATES);
        intResponsePrivilegeList.add(RSSManagerConstants.MAX_CONNECTIONS);
        intResponsePrivilegeList.add(RSSManagerConstants.MAX_USER_CONNECTIONS);

        blobPrivilegeList.add(RSSManagerConstants.SSL_CIPHER);
        blobPrivilegeList.add(RSSManagerConstants.X509_ISSUER);
        blobPrivilegeList.add(RSSManagerConstants.X509_SUBJECT);

    }

    public static List<String> getUserPrivilegeList() {
        return userPrivilegeList;
    }

    public static List<String> getDatabasePrivilegeList() {
        return databasePrivilegeList;
    }

    public static List<String> getBooleanResponsePrivilegeList() {
        return boolResponsePrivilegeList;
    }

    public static List<String> getStringResponsePrivilegeList() {
        return strResponsePrivilegeList;
    }

    public static List<String> getBlobResponsePrivilegeList() {
        return blobPrivilegeList;
    }

    public static List<String> getIntegerResponsePrivilegeList() {
        return intResponsePrivilegeList;
    }

    /**
     * Extracts the database driver from a given jdbc url.
     *
     * @param url JDBC url.
     * @return JDBC driver as a string.
     */
    public static String getDatabaseDriver(String url) {
        if (url != null) {
            String prefix = url.split(":")[1];
            if (RSSManagerConstants.MYSQL_PREFIX.equals(prefix)) {
                return RSSManagerConstants.MYSQL_XA_DRIVER;
            } else if (RSSManagerConstants.ORACLE_PREFIX.equals(prefix)) {
                return RSSManagerConstants.ORACLE_DRIVER;
            }
        }
        return "";
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static String processDomainName(String domainName) {
        if (domainName == null || "".equals(domainName)) {
            return domainName;
        }
        
        return domainName.replace(".", "_");
    }

    public static String constructConnectionUrl(String url) throws Exception {
        return RSSManagerConstants.JDBC_PREFIX + ":" + RSSManagerHelper.getDatabasePrefix(url) +
                "://" + RSSManagerHelper.validateRSSInstanceHostname(url);
    }

    public static String getDatabasePrefix(String url) {
        if (url != null && !"".equals(url)) {
            return url.split(":")[1];
        }
        return "";
    }

    private static String validateRSSInstanceHostname(String url) throws Exception {
        if (url != null && !"".equals(url)) {
            URI uri;
            try {
                uri = new URI(url.split("jdbc:")[1]);
                return uri.getHost() + ":" + uri.getPort();
            } catch (URISyntaxException e) {
                throw new Exception("JDBC URL '" + url + "' is invalid. Please enter a " +
                        "valid JDBC URL.");
            }
        }
        return "";
    }

}
