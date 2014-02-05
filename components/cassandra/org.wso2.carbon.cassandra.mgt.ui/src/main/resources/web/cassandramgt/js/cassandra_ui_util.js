function deleteKeyspace(index) {
    var opCount = document.getElementById("keyspaceCount");
    opCount.value = parseInt(opCount.value) - 1;
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.keyspace.delete.confirmation"], function() {
        var ksName = document.getElementById("keyspaceName" + index).value;
        var url = 'ks-delete-ajaxprocessor.jsp?name=' + ksName;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.ks.error.occurred']);
                           return false;
                       } else {
                            if (data.status == "success") {
                                deleteRaw('keyspace', index);
                            } else {
                               if(data.error == null){
                                   CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.ks.error.occurred']);
                              } else {
                                   if(data.cause == null){
                                       CARBON.showErrorDialog(data.error);
                                   } else {
                                       var forward = "cassandra_keyspaces.jsp";
                                       showErrorDialog(data.cause, forward);
                                   }
                              }
                              return false;
                            }
                       }
                   }, "json");
    });

    return false;
}

function deletecf(keyspace, index) {
    var opCount = document.getElementById("cfCount");
    opCount.value = parseInt(opCount.value) - 1;
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cf.delete.confirmation"], function() {
        var cfName = document.getElementById("cfName" + index).value;
        var url = 'cf-delete-ajaxprocessor.jsp?name=' + cfName + "&keyspace=" + keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.cf.error.occurred']);
                           return false;
                       } else {
                           if (data.status == "success") {
                               deleteRaw('cf', index);
                           } else {
                              if(data.error == null){
                                  CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.cf.error.occurred']);
                             } else {
                                  if(data.cause == null){
                                      CARBON.showErrorDialog(data.error);
                                  } else {
                                      var forward = "keyspace_dashboard.jsp?name=" + keyspace;
                                      showErrorDialog(data.cause, forward);
                                  }
                             }
                             return false;
                           }
                       }
                   }, "json");
    });

    return false;
}

function deleteCL(keyspace, cf, index) {
    var clCount = document.getElementById("clCount");
        clCount.value = parseInt(clCount.value) - 1;
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cl.delete.confirmation"], function() {
        var clName = document.getElementById("clName" + index).value;
        var url = "delete-cl-ajaxprocessor.jsp?cf=" + cf + "&name=" + clName;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.cl.error.occurred']);
                           return false;
                       } else {
                            if (data.status == "success") {
                                deleteRaw('cl', index);
                            } else {
                                 if(data.error == null){
                                     CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.cl.error.occurred']);
                                } else {
                                     if(data.cause == null){
                                         CARBON.showErrorDialog(data.error);
                                     } else {
                                         var forward = "cf_dashboard.jsp?cfName=" + cf + "&keyspaceName=" + keyspace;
                                         showErrorDialog(data.cause, forward);
                                     }
                                }
                            return false;
                            }
                       }
                   }, "json");
    });
    return false;
}

function showCLEditor(mode, name, cf, keyspace) {
    location.href = 'add_edit_cl.jsp?region=region1&item=cassandra_ks_mgt_create_menu&cl=' + name +
                    "&mode=" + mode + "&cf=" + cf + "&keyspace=" + keyspace;
}

function showShareCFEditor(keyspace, index) {
    location.href = 'share_cf_editor.jsp?region=region1&item=cassandra_ks_mgt_create_menu&cf=' +
                    document.getElementById("cfName" + index).value + "&keyspace=" + keyspace;
}

function saveKeyspaceSharedStatus(keyspace){
    location.href = 'share_keyspace-processor.jsp?keyspaceName=' + keyspace;
}

function saveRootSharedStatus(){
    location.href = 'share_root-processor.jsp';
}

function shareOrClearKeyspace(keyspace, action) {
    var role = getSelectedValue("share_keyspace_users");
    if (!validateRole(role)) {
        return false;
    }
    location.href = 'share_keyspace.jsp?name=' + keyspace + '&role=' + role + "&action=" + action;
}

function validateRole(role) {
    if ('selectuser' == role) {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.role.is.empty"]);
        return false;
    }
    return true;
}

function showCFEditor(keyspace, index) {
    var extraURLParameters = "&index=" + index + "&cf=" + document.getElementById("cfName" + index).value + "&keyspace=" + keyspace;
    add_edit_cf("edit", extraURLParameters);
}

function viewFCs(index) {
    var ksName = document.getElementById("keyspaceName" + index).value;
    location.href = 'keyspace_dashboard.jsp?name=' + ksName;
}
function viewCLs(keyspace, cf) {
    location.href = 'cf_dashboard.jsp?keyspaceName=' + keyspace + "&cfName=" + cf;
}

function addCL(cf, keyspace) {
    showCLEditor("add", "", cf, keyspace);
}

function addcf(keyspace) {
    add_edit_cf("add", "&keyspace=" + keyspace);
}

function add_edit_cf(mode, extractURLParameters) {
    location.href = 'add_edit_cf.jsp?region=region1&item=cassandra_ks_mgt_create_menu&mode=' + mode + extractURLParameters;
}

function hideEditor() {
    CARBON.closeWindow();
}

function saveKeyspace(mode) {
    var name = document.getElementById("ks_editor_name").value;
    if (name == '') {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.keyspace.is.empty"]);
        return false;
    }
    var pattern=/\W/;        //checking for non-word characters
    var match = pattern.test(name);
    if (match == true) {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.keyspace.name.is.invalid"]);
        return false;
    }

    var query = "";

    var rs = getSelectedValue("ks_editor_rs");
    if(rs == "network"){
        var inputs = document.getElementById("rfTable").getElementsByTagName("input");
        var numOfInputs = inputs.length;
        for(var i=0; i<numOfInputs; i=i+2){
            if(inputs[i].value != "" && inputs[i+1].value != ""){
                var rfAsint = parseInt(inputs[i+1].value);
                if (rfAsint == undefined || rfAsint <= 0 || isNaN(rfAsint)) {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.replicationfactors.are.invalid"]);
                    return false;
                }
                if (rfAsint > 2147483647) {
                    CARBON.showErrorDialog(cassandrajsi18n["cassandra.replicationfactor.is.outofrange"]);
                    return false;
                }
                query = query + "nrfs=" + inputs[i].value + "_" + rfAsint + "&";
            }
        }
        if(query != ""){
           query = query.substr(0, query.length - 1);
        }
    } else {
        var rf = document.getElementById("ks_editor_rf").value;
        var rfAsint = parseInt(rf);
        if (rfAsint == undefined || rfAsint <= 0 || isNaN(rfAsint)) {
            CARBON.showErrorDialog(cassandrajsi18n["cassandra.replicationfactor.is.invalid"]);
            return false;
        }
        if (rfAsint > 2147483647) {
            CARBON.showErrorDialog(cassandrajsi18n["cassandra.replicationfactor.is.outofrange"]);
            return false;
        }
    }
    var url = 'save_ks-ajaxprocessor.jsp?name=' + name;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.showWarningDialog(cassandrajsi18n["cassandra." + mode + ".cl.load.error"]);
                   } else {
                       if ((data.isExist == "no" && mode == "add") || (data.isExist == "yes" && mode == "edit")) {
                           if(rs == "network"){
                                location.href = 'save_keyspace.jsp?name=' + name + '&rs=' + rs + '&mode=' + mode + '&' + query;
                           } else {
                            location.href = 'save_keyspace.jsp?name=' + name + '&rf=' + rf + '&rs=' + rs + '&mode=' + mode;
                           }
                       } else {
                           if(data.error == null){
                                CARBON.showErrorDialog(cassandrajsi18n["cassandra." + mode + ".ks.duplicate.error"]);
                                } else {
                                   if(data.cause == null){
                                       CARBON.showErrorDialog(data.error);
                                   } else {
                                       var forward = "cassandra_keyspaces.jsp";
                                       showErrorDialog(data.cause, forward);
                                   }
                                }
                           return false;
                       }
                   }
               }, "json");
    return false;
}

function saveCL(mode, index, keyspace, cf) {
    var name = document.getElementById("cl_editor_name").value;
    if (name == '') {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.clname.is.empty"]);
        return false;
    }

    var indexname = document.getElementById("cl_editor_indexname").value;
    if (indexname == null || indexname == '') {
        //non-indexed column which is valid
    } else{
        var pattern=/\W/;        //Capital W: for non-word characters
        var match = pattern.test(indexname);
        if (match == true) {
            CARBON.showErrorDialog(cassandrajsi18n["cassandra.indexname.is.invalid"]);
            return false;
        }
    }

    var validationclass = document.getElementById("cl_editor_validationclass").value;

    var url = 'save_cl-ajaxprocessor.jsp?name=' + name + '&indexname=' + indexname +
              "&mode=" + mode + "&validationclass=" + validationclass + "&cf=" + cf + "&keyspace=" + keyspace;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.showWarningDialog(cassandrajsi18n["cassandra." + mode + ".cl.load.error"]);
                   } else {
                       if (data.status == "success") {
                           viewCLs(keyspace, cf);
                       } else {
                            if(data.error == null){
                                CARBON.showErrorDialog(cassandrajsi18n["cassandra." + mode + ".cl.duplicate.error"]);
                           } else {
                                if(data.cause == null){
                                    CARBON.showErrorDialog(data.error);
                                } else {
                                    var forward = "cf_dashboard.jsp?cfName=" + cf + "&keyspaceName=" + keyspace;
                                    showErrorDialog(data.cause, forward);
                                }
                           }
                           return false;
                       }
                   }

               }, "json");
    return false;
}

function savecf(mode, index, keyspace, id) {
    var formValidaterMesg = '';
    var name = document.getElementById("cf_editor_name").value;
    if (name == '') {
        formValidaterMesg = "<p>" + cassandrajsi18n["cassandra.cf.name.is.empty"] + "</p><p/>";
        //CARBON.showErrorDialog(cassandrajsi18n["cassandra.cf.name.is.empty"]);
        //return false;
    }else{
        var pattern=/\W/;        //Capital W: for non-word characters
        var match = pattern.test(name);
        if (match == true) {
            formValidaterMesg = "<p>" + cassandrajsi18n["cassandra.cf.name.is.invalid"] + "</p><p/>";
        }
    }
    var comment = document.getElementById("cf_editor_comment").value;
    var type = getSelectedValue("cf_editor_column_type");
    var comparator = getSelectedValue("cf_editor_column_comparator");
    var keyvalidationclass = getSelectedValue("cf_editor_keyvalidationclass");
//    var subcomparator = getSelectedValue("cf_editor_sub_column_comparator");
//    var keycachesize = document.getElementById("cf_editor_keycache_size").value;
//    var rowcachesize = document.getElementById("cf_editor_rowcache_size").value;
//    var rowcachetime = document.getElementById("cf_editor_rowcachetime").value;
    var validationclass = getSelectedValue("cf_editor_validationclass");
//    var gcGrace = document.getElementById("cf_editor_gc_grace").value;
//    var minThreshold = document.getElementById("cf_editor_min_threshold").value;
//    var maxThreshold = document.getElementById("cf_editor_max_threshold").value;

//    var keycachesizeAsfloat = parseFloat(keycachesize);
//    if (keycachesizeAsfloat == undefined ||  isNaN(keycachesize)) {
//        //CARBON.showErrorDialog(cassandrajsi18n["cassandra.cf.keycachesize.is.invalid"]);
//        //return false;
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.cf.keycachesize.is.invalid"] + "</p></p>";
//    } else if (keycachesizeAsfloat < 0 || keycachesizeAsfloat > 1) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.keycachesize.is.outofrange"] + "</p></p>";
//    }
//
//    var rowcachesizeAsfloat = parseFloat(rowcachesize);
//    if (rowcachesizeAsfloat == undefined || isNaN(rowcachesize)) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.rowcachesize.is.invalid"] + "</p></p>";
//    } else if (rowcachesizeAsfloat < 0 || rowcachesizeAsfloat > 1) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.rowcachesize.is.outofrange"] + "</p></p>";
//    }
//
//    var rowcachetimeAsint = parseInt(rowcachetime);
//    if (rowcachetimeAsint == undefined || rowcachetimeAsint <= 0 || isNaN(rowcachetime)) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.cf.rowcachetime.is.invalid"] + "</p></p>";
//    } else if (rowcachetimeAsint > 2147483647) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.rowcachetime.is.outofrange"] + "</p></p>";
//    }
//
//    var gcGraceAsint = parseInt(gcGrace);
//    if (gcGraceAsint == undefined || gcGraceAsint <= 0 || isNaN(gcGrace)) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.cf.gcGrace.is.invalid"] + "</p></p>";
//    } else if (gcGraceAsint > 2147483647) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.gcGrace.is.outofrange"] + "</p></p>";
//    }
//
//    var minThresholdAsint = parseInt(minThreshold);
//    if (gcGraceAsint == undefined || minThresholdAsint <= 0 || isNaN(minThreshold)) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.cf.minThreshold.is.invalid"] + "</p></p>";
//    } else if (minThresholdAsint > 2147483647) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.minThreshold.is.outofrange"] + "</p></p>";
//    }
//
//    var maxThresholdAsint = parseInt(maxThreshold);
//    if (gcGraceAsint == undefined || maxThresholdAsint <= 0 || isNaN(maxThreshold)) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.cf.maxThreshold.is.invalid"] + "</p></p>";
//    } else if (maxThresholdAsint > 2147483647) {
//        formValidaterMesg = formValidaterMesg + "<p>" + cassandrajsi18n["cassandra.maxThreshold.is.outofrange"] + "</p></p>";
//    }

    if (formValidaterMesg != '') {
        CARBON.showErrorDialog(formValidaterMesg);
        return false;
    }
//    var url = 'save_cf-ajaxprocessor.jsp?name=' + name + '&keyspace=' + keyspace + '&comment=' + comment
//                      + '&type=' + type + "&comparator=" + comparator + '&subcomparator=' + subcomparator + "&keycachesize=" + keycachesize
//                      + '&gcGrace=' + gcGrace + "&minThreshold=" + minThreshold + '&maxThreshold=' + maxThreshold + '&rowcachetime=' + rowcachetime
//                      + '&rowcachesize=' + rowcachesize + '&validationclass=' + validationclass + '&id=' + id + '&mode=' + mode;

    var url = 'save_cf-ajaxprocessor.jsp?name=' + name + '&keyspace=' + keyspace + '&comment=' + comment
        + '&type=' + type + "&comparator=" + comparator + '&keyvalidationclass=' + keyvalidationclass
        + '&validationclass=' + validationclass + '&id=' + id + '&mode=' + mode;


    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.showWarningDialog(cassandrajsi18n["cassandra.add.cf.load.error"]);
                   } else {
                       if (data.status == "success") {
                           location.href = 'keyspace_dashboard.jsp?name=' + keyspace;
                       } else {
                           if(data.error == null){
                              CARBON.showErrorDialog(cassandrajsi18n["cassandra." + mode + ".cf.duplicate.error"]);
                           } else {
                              if(data.cause == null){
                                  CARBON.showErrorDialog(data.error);
                              } else {
                                  var forward = "keyspace_dashboard.jsp?name=" + keyspace;
                                  showErrorDialog(data.cause, forward);
                              }
                           }
                           return false;
                       }
                   }
               }, "json");
    return false;
}

function edit_keyspace_raw(value, index) {
    document.getElementById("keyspaceName" + index).value = value;
    var td = document.getElementById("keyspaceTD" + index);
    var oldLink = document.getElementById("keyspaceTDLink" + index);
    td.removeChild(oldLink);
    td.appendChild(createGotoLink(index, value, "keyspace", null));
}

function edit_cf_raw(value, index) {
    document.getElementById("cfName" + index).value = value;
    var td = document.getElementById("cfTD" + index);
    td.innerHTML = value;
}

function add_keyspace_raw(value) {

    var keyspaceCount = document.getElementById("keyspaceCount");
    var i = keyspaceCount.value;

    var currentCount = parseInt(i);

    currentCount = currentCount + 1;

    keyspaceCount.value = currentCount;

    var keyspacetable = document.getElementById("keyspaceTable");
    keyspacetable.style.display = "";
    var keyspacetbody = document.getElementById("keyspaceBody");

    var keyspaceRaw = document.createElement("tr");
    keyspaceRaw.setAttribute("id", "keyspaceRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.id = 'keyspaceTD' + i;
    nameTD.appendChild(createGotoLink(i, value, "keyspace", null));

    var actionTD = document.createElement("td");
    actionTD.appendChild(createHiddenTextBox("keyspaceName" + i, null, value));
    actionTD.appendChild(createShareLink(i, null));
    actionTD.appendChild(createEditLink(i, null, "keyspace"));
    actionTD.appendChild(createDeleteLink(i, null, "keyspace"));
    keyspaceRaw.appendChild(nameTD);
    keyspaceRaw.appendChild(actionTD);
    keyspacetbody.appendChild(keyspaceRaw);
    return true;
}

function add_cf_raw(value, keyspace) {

    var cfCount = document.getElementById("cfCount");
    var i = cfCount.value;

    var currentCount = parseInt(i);

    currentCount = currentCount + 1;

    cfCount.value = currentCount;

    var cftable = document.getElementById("cfTable");
    cftable.style.display = "";
    var cftbody = document.getElementById("cfBody");

    var cfRaw = document.createElement("tr");
    cfRaw.setAttribute("id", "cfRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.id = 'cfTD' + i;
    nameTD.appendChild(createGotoLink(i, value, "cf", keyspace));
    var actionTD = document.createElement("td");
    actionTD.appendChild(createHiddenTextBox("cfName" + i, null, value));
    actionTD.appendChild(createShareLink(i, keyspace));
    actionTD.appendChild(createEditLink(i, keyspace, "cf"));
    actionTD.appendChild(createDeleteLink(i, keyspace, "cf"));
    cfRaw.appendChild(nameTD);
    cfRaw.appendChild(actionTD);
    cftbody.appendChild(cfRaw);
    return true;
}

function add_cl_raw(value) {

    var clCount = document.getElementById("clCount");
    var i = clCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;
    clCount.value = currentCount;

    var cltable = document.getElementById("clTable");
    cltable.style.display = "";
    var clButtonRaw = document.getElementById("clButtonRaw");
    clButtonRaw.style.display = "";
    var cltbody = document.getElementById("clBody");

    var clRaw = document.createElement("tr");
    clRaw.setAttribute("id", "clRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.id = 'clTD' + i;
    nameTD.appendChild(document.createTextNode(value));
    var actionTD = document.createElement("td");
    actionTD.appendChild(createHiddenTextBox("clName" + i, null, value));
    actionTD.appendChild(createEditLink(i, null, "cl"));
    actionTD.appendChild(createDeleteLink(i, null, "cl"));
    clRaw.appendChild(nameTD);
    clRaw.appendChild(actionTD);
    cltbody.appendChild(clRaw);
    return true;
}

function createDeleteLink(i, keyspace, type) {
    // Create the element:
    var cassandraDeleteLink = document.createElement('a');
    // Set some properties:
    cassandraDeleteLink.setAttribute("href", "#");
    //    cassandraDeleteLink.style.paddingLeft = '40px';
    cassandraDeleteLink.className = "delete-icon-link";
    cassandraDeleteLink.appendChild(document.createTextNode(cassandrajsi18n["cassandra.action.delete"]));
    cassandraDeleteLink.onclick = function () {
        if ("keyspace" == type) {
            deleteKeyspace(i);
        } else if ("cf" == type) {
            deletecf(keyspace, i);
        } else {
            deleteCL(i);
        }
    };
    return cassandraDeleteLink;
}

function createEditLink(i, keyspace, type) {
    // Create the element:
    var editLink = document.createElement('a');
    // Set some properties:
    editLink.setAttribute("href", "#");
    //    editLink.style.paddingLeft = '40px';
    editLink.className = "edit-icon-link";
    editLink.appendChild(document.createTextNode(cassandrajsi18n["cassandra.action.edit"]));
    editLink.onclick = function () {
        if ("keyspace" == type) {
            showKeyspaceEditor(i);
        } else if ("cf" == type) {
            showCFEditor(keyspace, i);
        } else {
            showCLEditor("edit", i);
        }
    };
    return editLink;
}

function createShareLink(i, keyspace) {
    // Create the element:
    var cassandraShareLink = document.createElement('a');
    // Set some properties:
    cassandraShareLink.setAttribute("href", "#");
    //    cassandraShareLink.style.paddingLeft = '40px';
    cassandraShareLink.className = "edit-icon-link";
    cassandraShareLink.appendChild(document.createTextNode(cassandrajsi18n["cassandra.action.share"]));
    cassandraShareLink.onclick = function () {
        if (keyspace == null) {
            showShareKeyspaceEditor(i)
        } else {
            showShareCFEditor(keyspace, i)
        }
    };
    return cassandraShareLink;
}

function createGotoLink(i, value, type, keyspace) {
    // Create the element:
    var gotCF = document.createElement('a');
    gotCF.setAttribute("href", "#");
    gotCF.setAttribute("id", type + "TDLink" + i);
    // Set some properties:
    gotCF.appendChild(document.createTextNode(value));
    gotCF.onclick = function () {
        if (type == "keyspace") {
            viewFCs(i);
        } else {
            viewCLs(keyspace, i);
        }
    };
    return gotCF;
}

function deleteRaw(type, i) {
    var propRow = document.getElementById(type + "Raw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!hasChildElements(parentTBody)) {
                var parentTable = document.getElementById(type + "Table");
                parentTable.style.display = 'none';
                var clButtonRaw = document.getElementById("clButtonRaw");
                if (clButtonRaw != undefined && clButtonRaw != null) {
                    clButtonRaw.style.display = 'none';
                }
            }
        }
    }
}

//function hasChildElements(parent) {
//    var hasChildElements, child;
//    hasChildElements = false;
//    for (child = parent.firstChild; child; child = child.nextSibling) {
//        if (child.nodeType == 1) { // 1 == Element
//            hasChildElements = true;
//            break;
//        }
//    }
//    return hasChildElements;
//}

//new fix to remove empty table headers

function hasChildElements(parent) {
    var hasChildElements, child;
        hasChildElements = false;
        nodeCount = 0;

        for (child = parent.firstChild; child; child = child.nextSibling) {
            if (child.nodeType == 1) { // 1 == Element
                nodeCount ++;
            }
         }

        if(nodeCount <= 1){
            hasChildElements = false;
           return hasChildElements;
         }

         hasChildElements = true;
    return hasChildElements;
}

function createHiddenTextBox(id, classOfTextBox, value) {
    // Create the element:
    var input = document.createElement('input');

    // Set some properties:
    input.setAttribute("type", 'hidden');
    input.setAttribute("id", id);
    input.setAttribute("name", id);
    if (value != null) {
        input.value = value;
    }
    if (classOfTextBox != null) {
        input.className = classOfTextBox;
    }
    return input;
}

function getSelectedValue(id) {
    var variableType = document.getElementById(id);
    var variableType_indexstr = null;
    var variableType_value = "";
    if (variableType != null) {
        variableType_indexstr = variableType.selectedIndex;
        if (variableType_indexstr != null) {
            variableType_value = variableType.options[variableType_indexstr].value;
        }
    }
    return variableType_value;
}

function forward(destinationJSP) {
    location.href = destinationJSP;
}

function selectCLType() {
    var columnType = getSelectedValue("cf_editor_column_type");
    var subColumnComparatorRaw = document.getElementById("sub_column_comparator_raw");
    if (columnType == "Super") {
        subColumnComparatorRaw.style.display = "";
    } else {
        subColumnComparatorRaw.style.display = "none";
    }
}

function showErrorDialog(message, callbackUrl){
    jQuery(document).ready(function() {
        function handleOK() {
            window.location = callbackUrl;
        }
        CARBON.showErrorDialog(escapeText(message), handleOK);
    });
}

function escapeText(message){
    return message.replace("<","&lt;").replace(">","&gt;");
}

function validateReplicationFactorTable(){
    var rfRowInputs = document.getElementById("rfTable").getElementsByTagName("input");
    var inputName = "";
    for(var i=0; i<rfRowInputs.length; i++){
        if(rfRowInputs[i].value == ""){
            return "Data Center Name or Replication Factor cannot be empty.";
        }
    }
    return "true";
}

var rfRowNum = 1;

function addDCRow() {
    var validationResult = validateReplicationFactorTable();
    if(validationResult == "true"){
        rfRowNum++;
        var sId = "rfTable_" + rfRowNum;
        var tableContent = "<tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "<input type=\"text\" id=\"dcName_" + rfRowNum + "\" name=\"dcName_" + rfRowNum + "\" value=\"\">\n" +
                           "</td>\n" +
                           "<td>\n" +
                           "<input type=\"text\" id=\"rfCount_" + rfRowNum + "\" name=\"rfCount_" + rfRowNum + "\" value=\"\">\n" +
                           "</td>" +
                           "<td>\n" +
                           "<span><a onClick='removeDCRow(\"" + sId + "\")'" +
                           "style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove</a></span>\n" +
                            "</td>" +
                            "</tr>";

        jQuery("#rfTable").append(tableContent);
    } else {
        CARBON.showWarningDialog(validationResult);
    }
}

function removeDCRow(id) {
    jQuery("#" + id).remove();
}

function onTopologyChanged(){
    var topology = getSelectedValue("ks_editor_rs");;
    if(topology == "network"){
        jQuery(".simpleStrategy").hide();
        jQuery(".networkStrategy").show();
    } else {
        jQuery(".simpleStrategy").show();
        jQuery(".networkStrategy").hide();
    }
}