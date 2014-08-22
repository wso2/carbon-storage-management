package org.wso2.carbon.rssmanager.core.dto.common;

import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

import javax.persistence.*;

@Entity
@Table(name="RM_DB_PRIVILEGE_TEMPLATE_ENTRY")
public class DatabasePrivilegeTemplateEntry extends AbstractEntity<Integer,DatabasePrivilegeTemplateEntry>{
	
	/**
	 * 
	 */
    private static final long serialVersionUID = -3527266973461993292L;
    
    
    @Version
    @Column(name="VERSION") 
    private Long version;
    
    @Id
	@TableGenerator(name="DB_PRIVILEGE_TEMPLATE_ENTRY_TABLE_GEN", table="DB_PRIVILEGE_TEMPLATE_ENTRY_SEQUENCE_TABLE", pkColumnName="SEQ_NAME",
    valueColumnName="SEQ_COUNT", pkColumnValue="EMP_SEQ")
	@Column(name="ID", columnDefinition="INTEGER")
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DB_PRIVILEGE_TEMPLATE_ENTRY_TABLE_GEN")
    private Integer id;
    
    @OneToOne(cascade={CascadeType.DETACH}, fetch=FetchType.EAGER)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    private DatabasePrivilegeTemplate privilegeTemplate;
    
    
    @Column(name="SELECT_PRIV")
	private String selectPriv = "N";
    @Column(name="INSERT_PRIV")
    private String insertPriv = "N";
    @Column(name="UPDATE_PRIV")
    private String updatePriv = "N";
    @Column(name="DELETE_PRIV")
    private String deletePriv = "N";
    @Column(name="CREATE_PRIV")
    private String createPriv = "N";
    @Column(name="DROP_PRIV")
    private String dropPriv = "N";
    @Column(name="INDEX_PRIV")
    private String indexPriv = "N";
    @Column(name="ALTER_PRIV")
    private String alterPriv = "N";
	
    @Column(name="GRANT_PRIV")
	private String grantPriv = "N";
    @Column(name="REFERENCES_PRIV")
    private String referencesPriv = "N";
    @Column(name="CREATE_TMP_TABLE_PRIV")
    private String createTmpTablePriv = "N";
    @Column(name="LOCK_TABLES_PRIV")
    private String lockTablesPriv = "N";
    @Column(name="EXECUTE_PRIV")
    private String executePriv = "N";
    @Column(name="CREATE_VIEW_PRIV")
    private String createViewPriv = "N";
    @Column(name="SHOW_VIEW_PRIV")
    private String showViewPriv = "N";
    @Column(name="CREATE_ROUTINE_PRIV")
    private String createRoutinePriv = "N";
    @Column(name="ALTER_ROUTINE_PRIV")
    private String alterRoutinePriv = "N";
    @Column(name="TRIGGER_PRIV")
    private String triggerPriv = "N";
    @Column(name="EVENT_PRIV")
    private String eventPriv = "N";
    
    
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
	public String getGrantPriv() {
		return grantPriv;
	}
	public void setGrantPriv(String grantPriv) {
		this.grantPriv = grantPriv;
	}
	public String getReferencesPriv() {
		return referencesPriv;
	}
	public void setReferencesPriv(String referencesPriv) {
		this.referencesPriv = referencesPriv;
	}
	public String getCreateTmpTablePriv() {
		return createTmpTablePriv;
	}
	public void setCreateTmpTablePriv(String createTmpTablePriv) {
		this.createTmpTablePriv = createTmpTablePriv;
	}
	public String getLockTablesPriv() {
		return lockTablesPriv;
	}
	public void setLockTablesPriv(String lockTablesPriv) {
		this.lockTablesPriv = lockTablesPriv;
	}
	public String getExecutePriv() {
		return executePriv;
	}
	public void setExecutePriv(String executePriv) {
		this.executePriv = executePriv;
	}
	public String getCreateViewPriv() {
		return createViewPriv;
	}
	public void setCreateViewPriv(String createViewPriv) {
		this.createViewPriv = createViewPriv;
	}
	public String getShowViewPriv() {
		return showViewPriv;
	}
	public void setShowViewPriv(String showViewPriv) {
		this.showViewPriv = showViewPriv;
	}
	public String getCreateRoutinePriv() {
		return createRoutinePriv;
	}
	public void setCreateRoutinePriv(String createRoutinePriv) {
		this.createRoutinePriv = createRoutinePriv;
	}
	public String getAlterRoutinePriv() {
		return alterRoutinePriv;
	}
	public void setAlterRoutinePriv(String alterRoutinePriv) {
		this.alterRoutinePriv = alterRoutinePriv;
	}
	public String getTriggerPriv() {
		return triggerPriv;
	}
	public void setTriggerPriv(String triggerPriv) {
		this.triggerPriv = triggerPriv;
	}
	public String getEventPriv() {
		return eventPriv;
	}
	public void setEventPriv(String eventPriv) {
		this.eventPriv = eventPriv;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public DatabasePrivilegeTemplate getPrivilegeTemplate() {
		return privilegeTemplate;
	}
	public void setPrivilegeTemplate(DatabasePrivilegeTemplate privilegeTemplate) {
		this.privilegeTemplate = privilegeTemplate;
	}
    

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}


}
