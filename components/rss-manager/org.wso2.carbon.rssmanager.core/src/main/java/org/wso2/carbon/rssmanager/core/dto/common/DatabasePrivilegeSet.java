/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dto.common;

import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

import javax.persistence.*;

@MappedSuperclass
public abstract class DatabasePrivilegeSet extends AbstractEntity<Integer, DatabasePrivilegeSet> {

    private static final long serialVersionUID = -3525295617675141739L;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @Id
    @TableGenerator(name = "USER_DATABASE_PRIVILEGE_TABLE_GEN", table = "USER_DATABASE_PRIVILEGE_SEQUENCE_TABLE", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "EMP_SEQ")
    @Column(name = "ID", columnDefinition = "INTEGER")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "USER_DATABASE_PRIVILEGE_TABLE_GEN")
    private Integer id;

    @ManyToOne(cascade = {CascadeType.DETACH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_DATABASE_ENTRY_ID", nullable = false)
    private UserDatabaseEntry userDatabaseEntry;

    @Column(name = "SELECT_PRIV")
    private String selectPriv = "N";
    @Column(name = "INSERT_PRIV")
    private String insertPriv = "N";
    @Column(name = "UPDATE_PRIV")
    private String updatePriv = "N";
    @Column(name = "DELETE_PRIV")
    private String deletePriv = "N";
    @Column(name = "CREATE_PRIV")
    private String createPriv = "N";
    @Column(name = "DROP_PRIV")
    private String dropPriv = "N";
    @Column(name = "INDEX_PRIV")
    private String indexPriv = "N";
    @Column(name = "ALTER_PRIV")
    private String alterPriv = "N";

    public DatabasePrivilegeSet() {}

    public String getSelectPriv() {
        return selectPriv;
    }

    public void setSelectPriv(String selectPriv) {
        this.selectPriv = selectPriv;
    }

    public String getInsertPriv() {
        return insertPriv;
    }

    public void setInsertPriv(String insertPriv) {
        this.insertPriv = insertPriv;
    }

    public String getUpdatePriv() {
        return updatePriv;
    }

    public void setUpdatePriv(String updatePriv) {
        this.updatePriv = updatePriv;
    }

    public String getDeletePriv() {
        return deletePriv;
    }

    public void setDeletePriv(String deletePriv) {
        this.deletePriv = deletePriv;
    }

    public String getCreatePriv() {
        return createPriv;
    }

    public void setCreatePriv(String createPriv) {
        this.createPriv = createPriv;
    }

    public String getDropPriv() {
        return dropPriv;
    }

    public void setDropPriv(String dropPriv) {
        this.dropPriv = dropPriv;
    }

    public String getIndexPriv() {
        return indexPriv;
    }

    public void setIndexPriv(String indexPriv) {
        this.indexPriv = indexPriv;
    }

    public String getAlterPriv() {
        return alterPriv;
    }

    public void setAlterPriv(String alterPriv) {
        this.alterPriv = alterPriv;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserDatabaseEntry getUserDatabaseEntry() {
        return userDatabaseEntry;
    }

    public void setUserDatabaseEntry(UserDatabaseEntry userDatabaseEntry) {
        this.userDatabaseEntry = userDatabaseEntry;
    }


    
}
