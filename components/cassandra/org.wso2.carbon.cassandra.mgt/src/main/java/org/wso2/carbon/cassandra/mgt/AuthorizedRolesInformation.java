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
package org.wso2.carbon.cassandra.mgt;

/**
 * Roles authorized for an action on a resource
 */
public class AuthorizedRolesInformation {

    private String resource;
    private String action;
    private String[] authorizedRoles = null;

    public AuthorizedRolesInformation(String resourcePath, String action, String[] roles){
        this.resource = resourcePath;
        this.action = action;
        this.authorizedRoles = roles;
    }

    public AuthorizedRolesInformation(String resourcePath, String permission){
        this.resource = resourcePath;
        this.action = permission;
        this.authorizedRoles = new String[0];
    }

    public AuthorizedRolesInformation(){
        this.resource = null;
        this.action = null;
        this.authorizedRoles = new String[0];
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPermission() {
        return action;
    }

    public void setPermission(String action) {
        this.action = action;
    }

    public String[] getAuthorizedRoles() {
        return authorizedRoles;
    }

    public void setAuthorizedRoles(String[] authorizedRoles) {
        this.authorizedRoles = authorizedRoles;
    }
}
