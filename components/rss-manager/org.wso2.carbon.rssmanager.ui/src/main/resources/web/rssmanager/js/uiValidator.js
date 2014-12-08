function validateRSSInstanceProperties() {
    var rssInstanceName = trim(document.getElementById("rssInstanceName").value);
    var serverUrl = trim(document.getElementById("serverUrl").value);
    var dataSourceClassName = trim(document.getElementById("dataSourceClassName").value);
    var serverEnvironmentList = document.getElementById("serverEnvironment");
    var serverEnvironment = serverEnvironmentList[serverEnvironmentList.selectedIndex].value;
    var instanceTypeList = document.getElementById("instancetype");
    var instanceType
    if(instanceTypeList!=null) {
        instanceType = trim(instanceTypeList[instanceTypeList.selectedIndex].value);
    }
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    var repassword = trim(document.getElementById("repassword").value);

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Database server instance name cannot be left blank");
        return false;
    }

    if (serverEnvironment == '' || serverEnvironment == null) {
        CARBON.showWarningDialog("Select a valid environment");
        return false;
    }
    if(instanceTypeList!=null) {
        if (instanceType == '' || instanceType == null) {
            CARBON.showWarningDialog("Select a valid instance type");
            return false;
        }
    }
    if (serverUrl == ''  ||  serverUrl == null) {
        CARBON.showWarningDialog("JDBC URL field cannot be left blank");
        return false;
    }
    if (dataSourceClassName == null || dataSourceClassName == '') {
        CARBON.showWarningDialog("Data Source Class Name field cannot be left blank");
        return false;
    }
    if (username == null || username == '') {
        CARBON.showWarningDialog("Data source administrative username field cannot be left blank");
        return false;
    }
    if (password == null || password == '') {
        CARBON.showWarningDialog("Data source administrative password field cannot be left blank");
        return false;
    }
    if (repassword == null || repassword == '') {
        CARBON.showWarningDialog("Data source confirm administrative password field cannot be left blank");
        return false;
    }
    if (password!=repassword) {
        CARBON.showWarningDialog("Data source password and confirmation password do not match");
        return false;
    }
    return true;
}

function dispatchDropRSSInstanceRequest(rssInstanceName, evnName, instanceType) {
    function forwardToDrop() {
        var url = 'rssInstanceOps_ajaxprocessor.jsp?flag=drop&rssInstanceName=' +
                encodeURIComponent(rssInstanceName)+ "&envName="+ encodeURIComponent(evnName)+"&instanceType="+ encodeURIComponent(instanceType);
        jQuery('#connectionStatusDiv').load(url, displayMessages);
    }
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog('Do you want to delete Database Server Instance?', forwardToDrop);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function deleteInstance(obj) {
    function forwardToDel() {
        var delElement = document.getElementById(obj);
        var instanceTable = document.getElementById("instanceTable");
        instanceTable.removeChild(delElement);
        document.location.href = "rssInstances.jsp";
    }
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog("Do you want to drop database server instance " + obj + "?", forwardToDel);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function dispatchRSSInstanceCreateRequest(flag) {
    var rssInstanceName = document.getElementById("rssInstanceName").value;
    var serverUrl = document.getElementById("serverUrl").value;
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var databaseDriverClass = document.getElementById("dataSourceClassName").value;
    var serverEnvironmentList = document.getElementById("serverEnvironment");
    var instancetypeList = document.getElementById("instancetype");
    var instancetype = "";
    if(instancetypeList!=null) {
        instancetype = instancetypeList[instancetypeList.selectedIndex].value;
    }
    var serverEnvironment = serverEnvironmentList[serverEnvironmentList.selectedIndex].value;
    var url = 'rssInstanceOps_ajaxprocessor.jsp?rssInstanceName=' + encodeURIComponent(rssInstanceName)
            + '&serverUrl=' + encodeURIComponent(serverUrl) + '&username=' + encodeURIComponent(
            username) + '&password=' + encodeURIComponent(password) + '&flag=' + flag + '&serverEnvironment=' + encodeURIComponent(serverEnvironment)
        + '&databaseDriverClass=' + encodeURIComponent(databaseDriverClass) +'&instancetype=' + encodeURIComponent(instancetype);
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayMessages);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function setHostRssInsname() {
    var hostList = document.getElementById('hosts');
    document.getElementById('hostRssInsId').value = hostList[hostList.selectedIndex].id;
}

function setRssInsId() {
    var rssInsList = document.getElementById("hosts");
    document.getElementById("rssInsId").value = rssInsList[rssInsList.selectedIndex].value;
}

function createDatabase() {
	
	var environments = document.getElementById("envCombo");
	var envName = trim(environments[environments.selectedIndex].value);
    var instanceTypes = document.getElementById("instanceTypes");
    var instanceType = trim(instanceTypes[instanceTypes.selectedIndex].value);
	var rssInstances = document.getElementById("rssInstances");
    var rssInstanceName = '';
    if(rssInstances!=null) {
        rssInstanceName = trim(rssInstances[rssInstances.selectedIndex].value);
    }
    var databaseName = trim(document.getElementById("databaseName").value);

    if(rssInstances!=null) {
        if (rssInstanceName == '' || rssInstanceName == null || rssInstanceName == 'SELECT') {
            CARBON.showWarningDialog("Select a valid database instance");
            return false;
        }
    }
    if (databaseName == '' || databaseName == null) {
        CARBON.showWarningDialog("Database name cannot be left blank");
        return false;
    }
    if (instanceType == '' || instanceType == null) {
        CARBON.showWarningDialog("Select instance type");
        return false;
    }
    var validChar = new RegExp("^[a-zA-Z0-9_]+$");
    if (!validChar.test(databaseName)) {
        CARBON.showWarningDialog("Alphanumeric characters and underscores are only allowed in database name");
        return false;
    }
    dispatchDatabaseActionRequest('create', rssInstanceName, databaseName, envName, instanceType);
}

function attachUserToDatabase() {
	var envName = document.getElementById('envName').value;
    var rssInstanceName = document.getElementById('rssInstanceName').value;
    var databaseName = document.getElementById('databaseName').value;
    var templates = document.getElementById('privilegeTemplates');
    var templateName = templates[templates.selectedIndex].value;
    var databaseUsers = document.getElementById('databaseUsers');
    var instanceType = document.getElementById('instanceType').value;
    var username = databaseUsers[databaseUsers.selectedIndex].value;

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Select a valid database instance");
        return false;
    }
    if (databaseName == '' || databaseName == null) {
        CARBON.showWarningDialog("Database name cannot be left blank");
        return false;
    }
    if (username == '' || username == null || username == 'SELECT') {
        CARBON.showWarningDialog("Select a valid database user");
        return false;
    }
    if (templateName == '' || templateName == null || templateName == 'SELECT') {
        CARBON.showWarningDialog("Select a valid database privilege template");
        return false;
    }
    dispatchDatabaseManageAction('attach', rssInstanceName, username, databaseName, envName, instanceType);
}

function dispatchDatabaseManageAction(flag, rssInstanceName, username, databaseName, envName ,instanceType) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var privilegeTemplates = document.getElementById('privilegeTemplates');
    var privilegeTemplate = '';
    if (privilegeTemplates != null) {
        privilegeTemplate = privilegeTemplates[privilegeTemplates.selectedIndex].value;
    } else {
        var tmpTemplate = document.getElementById('privilegeTemplateName');
        if (tmpTemplate != null) {
            privilegeTemplate = tmpTemplate.value;
        }
    }
    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
            '&username=' + encodeURIComponent(username) + '&password=' +
            encodeURIComponent(password) + '&privilegeTemplateName=' +
            encodeURIComponent(privilegeTemplate) + '&databaseName=' + 
            databaseName + '&envName=' + envName+ '&instanceType=' + instanceType;
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayDatabaseManageActionStatus);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function displayDatabaseManageActionStatus(msg, status, xmlhttp) {
    var xmlDoc=xmlhttp.responseXML;
    var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
    var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
    if (msg.search(/has been successfully attached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully detached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to attach user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to detach user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function createDatabaseUser(envName) {
	var username = trim(document.getElementById('username').value);
	var password = document.getElementById('password').value;
	var repeatPass = document.getElementById('repeatPassword').value;
	var rssInstances = document.getElementById('rssInstances');
	var instanceTypes = document.getElementById("instanceTypes");
	var instanceType = trim(instanceTypes[instanceTypes.selectedIndex].value);
	var rssInstanceName = ""
	if (rssInstances != null) {
		rssInstanceName = rssInstances[rssInstances.selectedIndex].value;
	}
	if (username == '' || username == null) {
		CARBON.showWarningDialog("Username field cannot be left blank");
		return false;
	} else if (username.length > 7) {
		CARBON.showWarningDialog("Value in the username field" +
				" entered exceeds the maximum permitted length of 7");
		return false;
	}
	var validChar = new RegExp("^[a-zA-Z0-9_]+$");
	if (!validChar.test(username)) {
		CARBON.showWarningDialog("Only Alphanumeric characters and underscores are "
						+ "allowed in Database Username");
		return false;
	}
	if (password == '' || password == null) {
		CARBON.showWarningDialog("Password field cannot be left blank");
		return false;
	}
	if (repeatPass == '' || repeatPass == null) {
		CARBON.showWarningDialog("Repeat password field cannot be left blank");
		return false;
	}
	if (password != repeatPass) {
		CARBON
				.showErrorDialog("Values in Password and Repeat password fields do not match");
		return false;
	}
	if (instanceType == '' || instanceType == null) {
		CARBON.showWarningDialog("Select instance type");
		return false;
	}
	dispatchDatabaseUserActionRequest('create', rssInstanceName, username, '',
			envName, instanceType);
}

function editDatabaseUserPrivileges(rssInstanceName, username, databaseName, envName, instanceType) {
    dispatchAttachedDatabaseUserActionRequest('edit', rssInstanceName, username, databaseName, envName, instanceType);
    return true;
}

function editDatabaseUser(rssInstanceName, username, envName, instanceType) {
    var password = document.getElementById('password').value;
    var repeatPass = document.getElementById('repeatPassword').value;
    if (username == '' || username == null) {
        CARBON.showWarningDialog("Username field cannot be left blank");
        return false;
    }
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Password field cannot be left blank");
        return false;
    }
    if (repeatPass == '' || repeatPass == null) {
        CARBON.showWarningDialog("Repeat password field cannot be left blank");
        return false;
    }
    if (password != repeatPass) {
        CARBON.showErrorDialog("Values in Password and Repeat password fields do not match");
        return false;
    }
    if (instanceType == '' || instanceType == null) {
        CARBON.showWarningDialog("Select instance type");
        return false;
    }
    dispatchDatabaseUserEditActionRequest('editUser', rssInstanceName, username, envName, instanceType);
}

function dispatchDatabaseUserEditActionRequest(flag, rssInstanceName, username, envName, instanceType) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' +
        encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
        '&username=' + encodeURIComponent(username) + '&password='+encodeURIComponent(password)+
        '&envName='+envName+'&instanceType='+instanceType;
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayMessagesForUser);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
    return false;
}

function setJDBCValues(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    document.getElementById("serverUrl").value = selectedValue.substring(
            0, selectedValue.indexOf("#"));
}

function testConnection() {
    var rssInstanceName = trim(document.getElementById("rssInstanceName").value);
    var serverUrl = trim(document.getElementById("serverUrl").value);
    var username = trim(document.getElementById("username").value);
    var password = trim(document.getElementById("password").value);
    //var driverClass = trim(document.getElementById("driverClass").value);
    var databaseEngine = document.getElementById("databaseEngine");
    var instanceType = trim(databaseEngine[databaseEngine.selectedIndex].value);

    if (rssInstanceName == '' || rssInstanceName == null) {
        CARBON.showWarningDialog("Database server instance name cannot be left blank");
        return false;
    }
    if (instanceType == '' || instanceType == null) {
        CARBON.showWarningDialog("Select a valid instance type");
        return false;
    }
    if (serverUrl == '' || serverUrl == null) {
        CARBON.showWarningDialog("JDBC url field cannot be left blank");
        return false;
    }
    if (username == '' || username == null) {
        CARBON.showWarningDialog("Administrative username field cannot Be left blank");
        return false;
    }
    if (password == '' || password == null) {
        CARBON.showWarningDialog("Administrative password field cannot be left blank");
        return false;
    }
    var jdbcUrl = trim(document.getElementById('serverUrl').value);
    var driverClass = '';
    if (jdbcUrl != null && jdbcUrl != '') {
        driverClass = trim(getJdbcDriver(jdbcUrl));
        if (driverClass != null && driverClass != '') {
            var url = 'rssInstanceOps_ajaxprocessor.jsp?flag=testCon&driverClass=' + encodeURIComponent(
                    driverClass) + '&serverUrl=' + encodeURIComponent(retrieveValidatedUrl(jdbcUrl)) +
                    '&username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(
                    password);
            sessionAwareFunction(function() {
                jQuery('#connectionStatusDiv').load(url, displayMsg);
            }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
        } else {
            CARBON.showErrorDialog("Invalid JDBC URL '" + jdbcUrl + "'. Please enter an appropriate JDBC URL.");
        }
    }
    return false;
}

function retrieveValidatedUrl(url) {
    var prefix = url.split(':')[1];
    var hostname = url.split('//')[1].split('/')[0];
    return 'jdbc:' + prefix + "://" + hostname;
}

function dropDatabaseUser(rssInstanceName, username, envName, instanceType) {
    function forwardToDel() {
        dispatchDatabaseUserActionRequest('drop', rssInstanceName, username, '', envName, instanceType)
    }

    CARBON.showConfirmationDialog("Do you want to drop the user?", forwardToDel);
}

function dropDatabase(rssInstanceName, databaseName, envName, instanceTyoe) {
    function forwardToDel() {
        dispatchDatabaseActionRequest('drop', rssInstanceName, databaseName, envName, instanceTyoe);
    }

    CARBON.showConfirmationDialog("Do you want to drop the database?", forwardToDel);

}

function manageDatabase(rssInstanceName, databaseName) {
    //document.location.href = 'databaseUsers.jsp?rssInsId=' + encodeURIComponent(rssInsId) + '&dbInsId=' +
    // encodeURIComponent(dbInsId);
    document.location.href = 'databaseUsers.jsp?rssInstanceName=' + encodeURIComponent(rssInstanceName) +
            '&databaseName=' + encodeURIComponent(databaseName);
}

function redirectToEditPage(obj, rssInsId) {
    var rowId = $(obj).parents('tr:eq(0)').attr('id');
    var instanceName = rowId.substring("tr_".length, rowId.length);
    document.location.href = "editRSSInstance.jsp?instanceName=" +
            encodeURIComponent(instanceName) + "&flag=edit&rssInsId=" +
            encodeURIComponent(rssInsId);
}

function dispatchDatabaseUserActionRequest(flag, rssInstanceName, username, databaseName, envName, instanceType) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var privilegeTemplates = document.getElementById('privilegeTemplates');
    var privilegeTemplate = '';
    if (privilegeTemplates != null) {
        privilegeTemplate = privilegeTemplates[privilegeTemplates.selectedIndex].value;
    } else {
        var tmpTemplate = document.getElementById('privilegeTemplateName');
        if (tmpTemplate != null) {
            privilegeTemplate = tmpTemplate.value;
        }
        if (privilegeTemplate == null || privilegeTemplate == '') {
            privilegeTemplate = 'none';
        }
    }
    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
            '&username=' + encodeURIComponent(username) + '&password=' +
            encodeURIComponent(password) + '&privilegeTemplateName=' +
            encodeURIComponent(privilegeTemplate) + '&databaseName=' + databaseName + '&envName='+envName+'&instanceType='+instanceType;
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayMessagesForUser);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
    return false;
}

function dispatchAttachedDatabaseUserActionRequest(flag, rssInstanceName, username, databaseName, envName,instanceType) {
    var tmpPassword = document.getElementById('password');
    var password = '';
    if (tmpPassword != null) {
        password = tmpPassword.value;
    }
    var privilegeTemplates = document.getElementById('privilegeTemplates');
    var privilegeTemplate = '';
    if (privilegeTemplates != null) {
        privilegeTemplate = privilegeTemplates[privilegeTemplates.selectedIndex].value;
    } else {
        var tmpTemplate = document.getElementById('privilegeTemplateName');
        if (tmpTemplate != null) {
            privilegeTemplate = tmpTemplate.value;
        }
        if (privilegeTemplate == null || privilegeTemplate == '') {
            privilegeTemplate = 'none';
        }
    }

    var select_priv = document.getElementById("select_priv").checked;
    var insert_priv = document.getElementById("insert_priv").checked;
    var update_priv = document.getElementById("update_priv").checked;
    var delete_priv = document.getElementById("delete_priv").checked;
    var create_priv = document.getElementById("create_priv").checked;
    var drop_priv = document.getElementById("drop_priv").checked;
    var grant_priv = document.getElementById("grant_priv").checked;
    var references_priv = document.getElementById("references_priv").checked;
    var index_priv = document.getElementById("index_priv").checked;
    var alter_priv = document.getElementById("alter_priv").checked;
    var create_tmp_table_priv = document.getElementById("create_tmp_table_priv").checked;
    var lock_tables_priv = document.getElementById("lock_tables_priv").checked;
    var create_view_priv = document.getElementById("create_view_priv").checked;
    var show_view_priv = document.getElementById("show_view_priv").checked;
    var create_routine_priv = document.getElementById("create_routine_priv").checked;
    var alter_routine_priv = document.getElementById("alter_routine_priv").checked;
    var execute_priv = document.getElementById("execute_priv").checked;
    var event_priv = document.getElementById("event_priv").checked;
    var trigger_priv = document.getElementById("trigger_priv").checked;

    var url = 'databaseUserOps_ajaxprocessor.jsp?rssInstanceName=' + encodeURIComponent(rssInstanceName) + '&flag=' + encodeURIComponent(flag) +
            '&username=' + encodeURIComponent(username) + '&password=' +
            encodeURIComponent(password) + '&privilegeTemplateName=' +
            encodeURIComponent(privilegeTemplate) + '&databaseName=' + databaseName +
            '&select_priv=' + select_priv + '&insert_priv=' + insert_priv + '&update_priv=' +
            update_priv + '&delete_priv=' + delete_priv + '&create_priv=' + create_priv +
            '&drop_priv=' + drop_priv + '&grant_priv=' + grant_priv + '&references_priv=' +
            references_priv + '&index_priv=' + index_priv + '&alter_priv=' + alter_priv +
            '&create_tmp_table_priv=' + create_tmp_table_priv + '&lock_tables_priv=' +
            lock_tables_priv + '&create_view_priv=' + create_view_priv + '&show_view_priv=' +
            show_view_priv + '&create_routine_priv=' + create_routine_priv + '&alter_routine_priv='
            + alter_routine_priv + '&execute_priv=' + execute_priv + '&event_priv=' + event_priv +
            '&trigger_priv=' + trigger_priv +'&envName='+ envName+'&instanceType='+ instanceType;
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayMessagesForEditedUser);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function populateSelectedUsername() {
    var userList = document.getElementById('users');
    document.getElementById('selectedUsername').value = userList[userList.selectedIndex].value;
}

function forwardToRedirector(rssInstId, dbInstId) {
    var selectedUsername = document.getElementById('selectedUsername');
    document.location.href = 'redirector.jsp?rssInstId=' + encodeURIComponent(rssInstId) +
            '&dbInstId=' + encodeURIComponent(dbInstId) + '&username=' + encodeURIComponent(
            selectedUsername);
}

function dispatchDatabaseActionRequest(flag, rssInstanceName, databaseName, envName, instanceType) {
    var url = 'databaseOps_ajaxprocessor.jsp?flag=' + flag + '&rssInstanceName=' +
            encodeURIComponent(rssInstanceName) + '&databaseName=' +
            encodeURIComponent(databaseName)+'&envName='+envName+'&instanceType='+instanceType;
    sessionAwareFunction(function() {
        jQuery('#connectionStatusDiv').load(url, displayDatabaseActionStatus);
    }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function deleteUser(userId) {
    document.location.href = "userProcessor.jsp?flag=delete&userId=" + encodeURIComponent(userId);
}

function setFlag(flag) {
    document.getElementById('flag').value = flag;
}

function setSSLType() {
    var sslTypes = document.getElementById(sslTypes);
    document.getElementById("ssl_type").value = sslTypes[sslTypes.selectedIndex].value;
}

function populateCheckBox(obj, val) {
    if (val == 'Y') {
        obj.checked = true;
    } else if (val == 'N') {
        obj.checked = false;
    }
}

function redirectToPrivilegeGroupsPage(rssInsId, dbInsId) {
    document.location.href = 'databasePrivilegeTemplates.jsp?dbInsId=' + encodeURIComponent(dbInsId) +
            '&rssInsId=' + encodeURIComponent(rssInsId);
}

function redirectToUsersPage() {
    document.location.href = 'databaseUsers.jsp';
}

function setPrivilegeGroup() {
    var privilegeGroups = document.getElementById('privilegeGroups');
    document.getElementById('privilegeGroup').value = privilegeGroups[privilegeGroups.selectedIndex].value;
}

function checkSelectedPrivileges() {
    var isSelected = false;
    var privileges = new Array();
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Insert_priv"));
    privileges.push(document.getElementById("Update_priv"));
    privileges.push(document.getElementById("Delete_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Select_priv"));
    privileges.push(document.getElementById("Lock_tables_priv"));
    privileges.push(document.getElementById("Create_view_priv"));
    privileges.push(document.getElementById("Show_view_priv"));
    privileges.push(document.getElementById("Create_routine_priv"));
    privileges.push(document.getElementById("Alter_routine_priv"));
    privileges.push(document.getElementById("Event_priv"));
    privileges.push(document.getElementById("Trigger_priv"));
    for (var i = 0; i < privileges.length; i++) {
        var val = privileges.pop();
        if (val != '' && val != null) {
            isSelected = true;
        }
    }
    if (!isSelected) {
        CARBON.showWarningDialog("No privilege has been selected. User might not be able to " +
                "login to the database");
    }
}

function createDatabasePrivilegeTemplate(flag, envName) {
	if (envName == null) {
		var environments = document.getElementById("envCombo");
		var envName = trim(environments[environments.selectedIndex].value);
	}
	var templateName = trim(document.getElementById('privilegeTemplateName').value);
	if (templateName == '' || templateName == null) {
		CARBON.showWarningDialog("'Database privilege template name' field cannot be left blank");
		return false;
	}
	var validChar = new RegExp("^[a-zA-Z0-9_]+$");
	if (!validChar.test(templateName)) {
		CARBON.showWarningDialog("Only Alphanumeric characters and underscores are "
						+ "allowed in database privilege template name");
		return false;
	}
	var url = composeDatabasePrivilegeTemplateActionUrl(flag, templateName,
			envName);

	sessionAwareFunction(function() {
		jQuery('#connectionStatusDiv').load(url, displayPrivilegeTemplateActionStatus);
	}, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function validateDatabasePrivilegeTemplateName() {
    var templateName = trim(document.getElementById('privilegeTemplateName').value);
    if (templateName == '' || templateName == null) {
        CARBON.showWarningDialog("'Database privilege template name' field cannot be left blank");
        return false;
    }
    return true;
}

function displayPrivilegeTemplateActionStatus(msg, status, xmlhttp) {
    var xmlDoc=xmlhttp.responseXML;
    var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
    var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }
            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database privilege template/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database privilege template/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databasePrivilegeTemplates.jsp?region=region1&item=privilege_groups_submenu&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function composeDatabasePrivilegeTemplateActionUrl(flag, templateName, envName) {
    var select_priv = document.getElementById("select_priv").checked;
    var insert_priv = document.getElementById("insert_priv").checked;
    var update_priv = document.getElementById("update_priv").checked;
    var delete_priv = document.getElementById("delete_priv").checked;
    var create_priv = document.getElementById("create_priv").checked;
    var drop_priv = document.getElementById("drop_priv").checked;
    var grant_priv = document.getElementById("grant_priv").checked;
    var references_priv = document.getElementById("references_priv").checked;
    var index_priv = document.getElementById("index_priv").checked;
    var alter_priv = document.getElementById("alter_priv").checked;
    var create_tmp_table_priv = document.getElementById("create_tmp_table_priv").checked;
    var lock_tables_priv = document.getElementById("lock_tables_priv").checked;
    var create_view_priv = document.getElementById("create_view_priv").checked;
    var show_view_priv = document.getElementById("show_view_priv").checked;
    var create_routine_priv = document.getElementById("create_routine_priv").checked;
    var alter_routine_priv = document.getElementById("alter_routine_priv").checked;
    var execute_priv = document.getElementById("execute_priv").checked;
    var event_priv = document.getElementById("event_priv").checked;
    var trigger_priv = document.getElementById("trigger_priv").checked;

    return 'databasePrivilegeTemplateOps_ajaxprocessor.jsp?flag=' + flag + '&privilegeTemplateName=' + templateName +
            '&select_priv=' + select_priv + '&insert_priv=' + insert_priv + '&update_priv=' +
            update_priv + '&delete_priv=' + delete_priv + '&create_priv=' + create_priv +
            '&drop_priv=' + drop_priv + '&grant_priv=' + grant_priv + '&references_priv=' +
            references_priv + '&index_priv=' + index_priv + '&alter_priv=' + alter_priv +
            '&create_tmp_table_priv=' + create_tmp_table_priv + '&lock_tables_priv=' +
            lock_tables_priv + '&create_view_priv=' + create_view_priv + '&show_view_priv=' +
            show_view_priv + '&create_routine_priv=' + create_routine_priv + '&alter_routine_priv='
            + alter_routine_priv + '&execute_priv=' + execute_priv + '&event_priv=' + event_priv +
            '&trigger_priv=' + trigger_priv + '&envName='+envName;
}

function dispatchDropDatabasePrivilegeTemplateRequest(privilegeTemplateName, envName) {
    function forwardToDel() {
        var url = 'databasePrivilegeTemplateOps_ajaxprocessor.jsp?privilegeTemplateName=' +
                encodeURIComponent(privilegeTemplateName) + '&flag=drop'+ '&envName='+envName;
        sessionAwareFunction(function() {
            jQuery('#connectionStatusDiv').load(url, displayPrivilegeTemplateActionStatus);
        }, rssmanagerjsi18n["rss.manager.session.expire.message"]);
        return false;
    }
    CARBON.showConfirmationDialog('Do you want to drop database privilege template?', forwardToDel);
}

function displayMessages(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database server instance/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database server instance/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'rssInstances.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function displayDatabaseActionStatus(msg, status, xmlhttp) {
        var xmlDoc=xmlhttp.responseXML;
        var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
        var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
        if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp?region=region1&item=databases_submenu&ordinal=0&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp?region=region1&item=databases_submenu&ordinal=0&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to drop database/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp?region=region1&item=databases_submenu&ordinal=0&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create database/) != -1) {
        jQuery(document).ready(function() {
            CARBON.showErrorDialog(msg);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databases.jsp?region=region1&item=databases_submenu&ordinal=0&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    }
}

function displayMessagesForUser(msg, status, xmlhttp) {
    var xmlDoc=xmlhttp.responseXML;
    var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
    var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully saved/) != -1) {
        jQuery(document).ready(function () {
            function handleOK() {
                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName=' + env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create user/) != -1) {
        jQuery(document).ready(function() {
//            function handleOK() {
//                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
//            }
//
//            CARBON.showErrorDialog(msg, handleOK);
            CARBON.showErrorDialog(msg);
        });
    } else if (msg.search(/Failed to edit user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
//            function handleOK() {
//                window.location = 'databaseUsers.jsp?region=region1&item=database_users_submenu&envName='+env;
//            }
//
//            CARBON.showErrorDialog(msg, handleOK());
            CARBON.showErrorDialog(msg);
        });
    }
}

function displayMessagesForEditedUser(msg, status, xmlhttp) {
	var xmlDoc=xmlhttp.responseXML;
    var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
    var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
	
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to edit user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?ordinal=1&envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMessagesForAttachedUser(msg) {
    if (msg.search(/has been successfully created/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully edited/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/has been successfully dropped/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to create user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to edit user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'databaseUsers.jsp';
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMsg(msg) {
    var successMsg = new RegExp("^Database connection is successful");
    if (msg.search(successMsg) == -1) //if match failed
    {
        CARBON.showErrorDialog(msg);
    } else {
        CARBON.showInfoDialog(msg);
    }

}

function validateJdbcUrl(url) {
    if (url.split(":")[0] != 'jdbc') {
        CARBON.showErrorDialog("Invalid JDBC URL");
        return false;
    }
    return true;
}

function rightTrim(str) {
    for (var i = str.length - 1; i >= 0 && (str.charAt(i) == ' '); i--) {
        str = str.substring(0, i);
    }
    return str;
}

function leftTrim(str) {
    for (var i = 0; i >= 0 && (str.charAt(i) == ' '); i++) {
        str = str.substring(i + 1, str.length);
    }
    return str;
}

function trim(str) {
    return leftTrim(rightTrim(str));
}

function getJdbcDriver(instanceUrl) {
    var prefix = instanceUrl.split(':')[1];
    if (prefix == 'mysql') {
        return 'com.mysql.jdbc.Driver';
    } else if (prefix == 'oracle') {
        return 'oracle.jdbc.driver.OracleDriver';
    }
    return '';
}

function createDataSource(rssInstanceName, databaseName, username, envName,
		instanceType) {
	var dsName = trim(document.getElementById('datasourcename').value);
	var url = 'databaseUserOps_ajaxprocessor.jsp?dsName=' + dsName
			+ '&databaseName=' + databaseName + '&username=' + username
			+ '&rssInstanceName=' + rssInstanceName + '&flag=createDS'
			+ '&envName=' + envName + '&instanceType=' + instanceType;

	sessionAwareFunction(function() {
		jQuery('#connectionStatusDiv').load(url, displayMessagesForCarbonDS);
	}, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function detachDatabaseUser(rssInstanceName, databaseName, username, envnName,instanceType) {
	function forwardToDetach() {
		var url = 'databaseUserOps_ajaxprocessor.jsp?databaseName=' + databaseName + '&username=' +
			username + '&rssInstanceName=' + rssInstanceName + '&flag=detach' + '&envName=' +envnName+ '&instanceType=' +instanceType;
		jQuery('#connectionStatusDiv').load(url, displayMessagesForDatabaseUserActions);
	}
	sessionAwareFunction(function() {
		CARBON.showConfirmationDialog("Do you want to detach the database user '" + username +
										"' from the database '" + databaseName + "'?", forwardToDetach);
	}, rssmanagerjsi18n["rss.manager.session.expire.message"]);
}

function displayMessagesForDatabaseUserActions(msg,status, xmlhttp) {
    var xmlDoc=xmlhttp.responseXML;
    var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
    var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;
    if (msg.search(/has been successfully attached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });

    } else if (msg.search(/has been successfully detached/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?envName='+env;
            }

            CARBON.showInfoDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to attach database user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else if (msg.search(/Failed to detach database user/) != -1) {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK);
        });
    } else {
        jQuery(document).ready(function() {
            function handleOK() {
                window.location = 'attachedDatabaseUsers.jsp?envName='+env;
            }

            CARBON.showErrorDialog(msg, handleOK());
        });
    }
}

function displayMessagesForCarbonDS(msg, status, xmlhttp) {
	var xmlDoc = xmlhttp.responseXML;
	var msg = xmlDoc.getElementsByTagName("Message")[0].childNodes[0].nodeValue;
	var env = xmlDoc.getElementsByTagName("Environment")[0].childNodes[0].nodeValue;

	if (msg.search(/has been successfully created/) != -1) {
		jQuery(document).ready(function() {
			function handleOK() {
				window.location = 'attachedDatabaseUsers.jsp?envName=' + env;
			}

			CARBON.showInfoDialog(msg, handleOK);
		});
	} else if (msg.search(/Unable to create carbon datasource/) != -1) {
		jQuery(document).ready(function() {
			function handleOK() {
				window.location = 'attachedDatabaseUsers.jsp?envName=' + env;
			}

			CARBON.showErrorDialog(msg);
		});
	} else if (msg.search(/Datasource already exists/) != -1) {
		jQuery(document).ready(function() {
			function handleOK() {
				window.location = 'attachedDatabaseUsers.jsp?envName=' + env;
			}

			CARBON.showInfoDialog(msg, handleOK);
		});
	} else {
		CARBON.showErrorDialog('Unable to create carbon datasource');
	}
}

function exploreDatabase(userId, url, driver) {
    document.location.href = '../dbconsole/login.jsp?userId=' + userId +
            '&url=' + url + '&driver=' + driver;
}

function selectAllOptions() {
    var selectAll = document.getElementById('selectAll');
    var c = new Array();
    c = document.getElementsByTagName('input');
    if (selectAll.checked) {
        for (var i = 0; i < c.length; i++) {
            if (c[i].type == 'checkbox') {
                c[i].checked = true;
            }
        }
    } else {
        for (var j = 0; j < c.length; j++) {
            if (c[j].type == 'checkbox') {
                c[j].checked = false;
            }
        }
    }
}

function addDataSourceProperties() {
    //check to see if there are empty fields left
    var theTable = document.getElementById('dsPropertyTable');
    var inputs = theTable.getElementsByTagName('input');
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].value == "") {
            CARBON.showErrorDialog("Cannot add a property with empty key or value. Please " +
                    "specify a key and a value");
            return;
        }
    }
    addServiceParamRow("", "", "dsPropertyTable", "deleteDataSourcePropRow");
    if (document.getElementById('dsPropertyTable').style.display == "none") {
        document.getElementById('dsPropertyTable').style.display = "";
    }
}


function alternateTableRows(id, evenStyle, oddStyle) {
    if (document.getElementsByTagName) {
        if (document.getElementById(id)) {
            var table = document.getElementById(id);
            var rows = table.getElementsByTagName("tr");
            for (var i = 0; i < rows.length; i++) {
                //manipulate rows
                if (i % 2 == 0) {
                    rows[i].className = evenStyle;
                } else {
                    rows[i].className = oddStyle;
                }
            }
        }
    }
}

function addServiceParamRow(key, value, table, delFunction) {
    addRowForSP(key, value, table, delFunction);
}

function addRowForSP(prop1, prop2, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spName";
    inputElem.value = prop1;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+prop1+' />';


    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spValue";
    inputElem.value = prop2;
    param2Cell.appendChild(inputElem);

    var delCell = document.createElement('td');
    delCell.innerHTML = '<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";

    alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
}

function deleteDataSourcePropRow(index) {
    CARBON.showConfirmationDialog("Do you want to delete the property?" , function() {
        document.getElementById('dsPropertyTable').deleteRow(index);
        if (document.getElementById('dsPropertyTable').rows.length == 1) {
            document.getElementById('dsPropertyTable').style.display = 'none';
        }
    });
}


