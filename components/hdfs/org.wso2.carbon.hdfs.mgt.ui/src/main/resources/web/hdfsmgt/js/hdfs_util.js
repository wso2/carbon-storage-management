var browserName = navigator.appName;
var resourceType = "file";
var ASSOCIATION_TYPE01 = "depends";
var myEditor = null;
var textContentEditor = null;
var textContentUpdator = null;
var filterShown = false;
var isMediationLocalEntrySelected = false;
var emptyErrorMessage


function getParentPath(srcPath){
	var parentPath = srcPath.substr(0, srcPath.lastIndexOf("/"));
	if(parentPath == null || parentPath == ""){
		parentPath = "/";
	}
	return parentPath;	
}

function refreshPermissionsSection() {
    sessionAwareFunction(function() {
        var random = getRandom();
        new Ajax.Request('../resources/add_role_for_user.jsp', {
            method: 'get',
            parameters: {path: path,random:random},
            onSuccess: function(transport) {
                var perDiv = $('permissionsDiv');
                perDiv.innerHTML = transport.responseText;
                YAHOO.util.Event.onAvailable('perExpanded', function() {
                    $('perIconExpanded').style.display = "";
                    $('perIconMinimized').style.display = "none";
                    $('perExpanded').style.display = "";
                });
            }
        });
    }, org_wso2_carbon_registry_resource_ui_jsi18n["session.timed.out"]); 
}

function showHide(toShowHide) {
    var resource_div = document.getElementById('add-resource-div');
    var folder_div = document.getElementById('add-folder-div');
    var link_div = document.getElementById('add-link-div');
    if (toShowHide == 'add-resource-div') {
        if (resource_div!= null && resource_div.style.display == 'block') {
            resource_div.style.display = 'none';
        }
        else {
            resource_div.style.display = 'block';
            if (folder_div!= null && folder_div.style.display == 'block') folder_div.style.display = 'none';
            if (link_div!=null && link_div.style.display == 'block') link_div.style.display = 'none';
        }
    }

    if (toShowHide == 'add-folder-div') {
        if (folder_div!=null && folder_div.style.display == 'block') {
            folder_div.style.display = 'none';
        }
        else {
            if(folder_div!=null) { 
            	folder_div.style.display = 'block'; 
            }
            if (resource_div!=null && resource_div.style.display == 'block') resource_div.style.display = 'none';
            if (link_div != null && link_div.style.display == 'block') link_div.style.display = 'none';
        }
    }

    if (toShowHide == 'add-link-div') {
        if (link_div!=null && link_div.style.display == 'block') {
            link_div.style.display = 'none';
        }
        else {
            if(link_div!=null){link_div.style.display = 'block';}
            if (resource_div!=null && resource_div.style.display == 'block') resource_div.style.display = 'none';
            if (folder_div !=null && folder_div.style.display == 'block') folder_div.style.display = 'none';
        }
    }
}

//used
function showHideTreeView(path,obj) {
    sessionAwareFunction(function() {
        var stdView = document.getElementById('stdView');
        var treeView = document.getElementById('treeView');
        var random = getRandom();
        var clickedon = obj.id;
        if (stdView && YAHOO.util.Dom.hasClass(stdView,"stdView-notSelected") && clickedon=="stdView") {
            YAHOO.util.Dom.removeClass(stdView,"stdView-notSelected");
            YAHOO.util.Dom.addClass(stdView,"stdView-Selected");

            YAHOO.util.Dom.removeClass(treeView,"treeView-Selected");
            YAHOO.util.Dom.addClass(treeView,"treeView-notSelected");
            new Ajax.Updater('viewPanel', '../hdfsmgt/standard_view_ajaxprocessor.jsp', { method: 'get', parameters: {path: path,random:random}, evalScripts:true });

        } else if(treeView && YAHOO.util.Dom.hasClass(treeView,"treeView-notSelected") && clickedon=="treeView") {
            YAHOO.util.Dom.removeClass(treeView,"treeView-notSelected");
            YAHOO.util.Dom.addClass(treeView,"treeView-Selected");

            YAHOO.util.Dom.removeClass(stdView,"stdView-Selected");
            YAHOO.util.Dom.addClass(stdView,"stdView-notSelected");
            new Ajax.Updater('viewPanel', '../hdfsmgt/tree_view_ajaxprocessor.jsp', { method: 'get', parameters: {path: path, treeNavigationPath: path, reference: "compute",random:random}, evalScripts:true });
        }
    },org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
}

function setTreeNavigationPath(treeNavigationPath, reference) {
    new Ajax.Request('../hdfsmgt/tree_view_ajaxprocessor.jsp',
    {
        method:'get',
        parameters: {treeNavigationPath: treeNavigationPath, reference: reference, random:getRandom()},

        onSuccess: function() {
        },

        onFailure: function() {
        }
    });
}

function setStyleDisplayNone(div)
{
	if(div != null)
		{
			div.style.display = "none";
		}
}

function hideOthers(id, type) {
	
    var renamePanel = document.getElementById("rename_panel" + id);
    var createPanel = document.getElementById("create_panel" + id);
    var uploadPanel = document.getElementById("upload_panel" + id);
    var symLinkPanel = document.getElementById("symlinkContentUI" + id);
    
    setStyleDisplayNone(renamePanel);	
    setStyleDisplayNone(createPanel);
    setStyleDisplayNone(uploadPanel);
    setStyleDisplayNone(symLinkPanel);

    if(type != 'del'){
		var panelToView = document.getElementById(type + id);
	    panelToView.style.display = "";
		}
}

function renameItem(srcPath, resourceEditDivID, isFolder, pageNumber) {
		sessionAwareFunction(function() {
         var reason = "";
         var enteredvalue = ltrim(document.getElementById(resourceEditDivID).value);
        document.getElementById(resourceEditDivID).value = enteredvalue;
        if(enteredvalue == ""){
        	reason = "The value entered is empty";
        }
       if (reason == "") {
            reason += validateIllegal(resourceEditDivID);
        }
        if (reason == "") {
            reason += validateResourcePathAndLength(document.getElementById(resourceEditDivID));
        }
        var parentPath = getParentPath(srcPath);
        var resourcePathPrefix = parentPath;
        if(parentPath != "/"){
        	resourcePathPrefix = parentPath + "/";
        }
        var resourcePath= resourcePathPrefix + document.getElementById(resourceEditDivID).value;
        resourcePath = resourcePath.replace("//", "/");
        if (reason != "") {
            CARBON.showWarningDialog(reason, function(){
            	reActivateRenameButton(resourceEditDivID);
            	});
            return false;
          }else {
            var successMsg = org_wso2_carbon_hdfs_mgt_ui_jsi18n["file.rename.successful"];
            if(isFolder)
            	{
            		successMsg = org_wso2_carbon_hdfs_mgt_ui_jsi18n["folder.rename.successful"];
            	}

            new Ajax.Request('../hdfsmgt/rename_item_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {srcPath: srcPath, dstPath: resourcePath, isFolder: isFolder},

                onSuccess: function() {
                	 CARBON.showInfoDialog(successMsg, function(){
                		 fillContentSection(parentPath, pageNumber)
            		 });
                
                     
                },

                onFailure: function(transport) {
                	   CARBON.showErrorDialog(trim(transport.responseText),function(){
                		   window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + getParentPath(srcPath);
                	          return;
                	      });
                }
            });
        }
		},org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
    loadData();
    return true;

}

function reActivateRenameButton(renameDivId){
	document.getElementById(renameDivId+"Button").disabled = false;
}
	

function validateIllegal(fld){
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([~!@#;%^*+={}\|\\<>\"\',])/; // disallow ~!@#$;%^*+={}|\<>"',
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = "illegal input"
    } 
    return error;
}

function createFolder(srcPath, createDivID, pageNumber)
{
	sessionAwareFunction(function() {
	var newName = document.getElementById(createDivID).value;
	var newFolder;
	if(srcPath == "/")
	{
		newFolder = srcPath + newName;
	}else
	{
		newFolder = srcPath+'/'+newName; 
	}
	
	new Ajax.Request('../hdfsmgt/create_folder_ajaxprocessor.jsp',
	         {
	                method:'post',
	                parameters: {folderPath: newFolder},

	                onSuccess: function() {
	                	 CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["folder.create.successful"], function(){
	                		 fillContentSection(srcPath, pageNumber)
	                		 });
	                    
	                },

	                onFailure: function(transport) {
	                	   CARBON.showErrorDialog(trim(transport.responseText),function(){
	                		   window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + getParentPath(srcPath);
	                	          return;
	                	      });
	                }
	          });
    },org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
}

function deleteFolder(folderToDelete, pageNumber) {
	
	 CARBON.showConfirmationDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["folder.delete.confirmation"], function(){
		    new Ajax.Request('../hdfsmgt/delete_folder_ajaxprocessor.jsp',
	       	         {
		    				method:'post',
	       	                parameters: {folderPath: folderToDelete},
	
	       	                onSuccess: function() {
	       	                	CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["folder.delete.successful"], function(){
	   	                		 fillContentSection(getParentPath(folderToDelete), pageNumber)
		                		 });
	       	                   
	       	                },
	
	       	                onFailure: function(transport) {
	       	                 CARBON.showErrorDialog(trim(transport.responseText),function(){
		                		   window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + getParentPath(folderToDelete);
		                	          return;
		                	      });
	       	                }
	       	            });
			 },org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
	 loadData();
}

function deleteFile(filePath, pageNumber) {

	CARBON.showConfirmationDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["file.delete.confirmation"], function(){
	new Ajax.Request('../hdfsmgt/delete_file_ajaxprocessor.jsp',
  	         {
   				method:'post',
  	                parameters: {filePath: filePath},

  	                onSuccess: function() {
  	                	CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["file.delete.successful"], function(){
	                		 fillContentSection(getParentPath(filePath), pageNumber)
               		 });
  	                    
  	                },

  	                onFailure: function(transport) {
  	                  CARBON.showErrorDialog(trim(transport.responseText),function(){
               		   window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + getParentPath(filePath);
               	          return;
               	      });
  	                }
  	            });
	 },org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
	 loadData();
}

function fillFileUploadDetails(formname) {

    var filepath = document.forms[formname].uploadFile.value;

    var filename = "";
    if (filepath.indexOf("\\") != -1) {
        filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
    } else {
        filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
    }
    document.forms[formname].uploadedFileName.value = filename;
}

function submitUploadUpdatedContentForm() {

    var rForm = document.forms["updateUploadForm"];
    /* Validate the form before submit */

    var reason = "";
    if (rForm.upload.value.length == 0) {
        reason = "Empty value entered."
    }

    if (reason != "") {
        CARBON.showWarningDialog(reason);
        return false;
    } else {
        rForm.submit();
    }
    return true;
}

function submitUploadContentForm(formName) {
   sessionAwareFunction(function() {
	    var rForm = document.forms[formName];
	    var file = document.forms[formName].uploadFile.value;
		var fileName = document.forms[formName].uploadedFileName.value;
		var reason = "";

        if(file.length == 0){
        	reason +="No file has been selected" +"<br />";
        }
        else if (fileName.length == 0) {
            reason +=  "File name not entered" + "<br />";
        }
        
        if (reason != "") {
           document.getElementById('whileUpload').style.display = "none";
            CARBON.showWarningDialog(reason);
            return false;
        } else {
        	rForm.submit();
         }
            }, org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
    return true;
}

function whileUpload(){
    if(document.getElementById('whileUpload')!=null){
    	document.getElementById('whileUpload').style.display = "";
    }

}
//function submitSymlinkContentForm() {
//
//    if(!validateTextForIllegal(document.forms["symlinkContentForm1"].targetpath)){
//      CARBON.showWarningDialog(org_wso2_carbon_registry_common_ui_jsi18n["the"] + " "+ "symlink content"+" " + org_wso2_carbon_registry_common_ui_jsi18n["contains.illegal.chars"]);
//        document.getElementById('add-link-div').style.display = "";
//        document.getElementById('whileUpload').style.display = "none";
//      return false;
//    }
//
//    sessionAwareFunction(function() {
//        var rForm = document.forms["symlinkContentForm1"];
//
//        /* Validate the form before submit */
//        var reason = "";
//        reason += validateEmpty(rForm.filename, org_wso2_carbon_hdfs_mgt_ui_jsi18n["name"]);
//        if (reason == "") {
//            reason += validateForInput(rForm.filename, org_wso2_carbon_hdfs_mgt_ui_jsi18n["name"]);
//        }
//        if (reason == "") {
//            reason += validateIllegal(rForm.filename, org_wso2_carbon_hdfs_mgt_ui_jsi18n["name"]);
//        }
//        if (reason == "") {
//            reason += validateResourcePathAndLength(rForm.filename);
//        }
//        if (reason == "") {
//            reason += validateEmpty(rForm.targetpath, org_wso2_carbon_hdfs_mgt_ui_jsi18n["path"]);
//        }
//        if (reason == "" && rForm.targetpath.value == '/') {
//            reason += org_wso2_carbon_hdfs_mgt_ui_jsi18n["unable.create.symlink.to.root"];
//        }
//        var resourcePath= rForm.path.value + '/' + rForm.filename.value;
//        resourcePath = resourcePath.replace("//", "/");
//     
//        if (reason != "") {
//            CARBON.showWarningDialog(reason);
//            document.getElementById('whileUpload').style.display = "none";
//            return false;
//        }
//
//        var parentPath = document.getElementById('srParentPath').value;
//        var name = document.getElementById('srFileName').value;
//        var targetPath = document.getElementById('srPath').value;
//        new Ajax.Request('../resources/add_symlink_ajaxprocessor.jsp',
//        {
//            method:'post',
//            parameters: {parentPath: parentPath, name: name, targetPath: targetPath,random:getRandom()},
//
//            onSuccess: function() {
//                refreshMetadataSection(parentPath);
//                refreshContentSection(parentPath);
//                document.getElementById('whileUpload').style.display = "none";
//                CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["successfully.created.symbolic.link"],loadData);
//            },
//
//            onFailure: function(transport) {
//                document.getElementById('whileUpload').style.display = "none";
//                CARBON.showErrorDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["failed.to.create.symbolic.link"] + transport.responseText,loadData);
//            }
//        });
//    }, org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
//    return true;
//}

function fillContentSection(path, pageNumber, viewMode, consumerID,
		targetDivID, iperPg) {
	var itemsPerPage = iperPg == null ? 10 : iperPg;
	var random = getRandom();
	window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=hdfs_list_menu&viewType=std&path="
			+ path + "&requested_page=" + pageNumber + "&itemCount=" + itemsPerPage;
}

function showHDFSTreeWithLoadFunction(loadFunction, textBoxId, onOKCallback, rootPath, relativeRoot, displayRootPath) {

    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + "HDFS Folder Tree Loading"+ '</span> </div>';
    CARBON.showPopupDialog(loadingContent, "HDFS Folder Tree", 500, false);
    var random = getRandom();
    if (onOKCallback) {
        if (typeof onOKCallback == "function") {
            onOKCallback = onOKCallback.toString().substring('function '.length);
            onOKCallback = onOKCallback.substring(0, onOKCallback.indexOf("("));
            if (rootPath) {
                if (relativeRoot) {
                    new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                        method: 'get',
                        parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,displayRootPath:displayRootPath,onOKCallback:onOKCallback,random:random},
                        onSuccess: function(transport) {
                            var dialog = $('dialog');
                            dialog.innerHTML = transport.responseText;
                            if (loadFunction) {
                                loadSubTree(rootPath, 'root', textBoxId, 'false');
                            }
                        }
                    });
                } else {
                    new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                        method: 'get',
                        parameters: {textBoxId:textBoxId,rootPath:rootPath,displayRootPath:displayRootPath,onOKCallback:onOKCallback,random:random},
                        onSuccess: function(transport) {
                            var dialog = $('dialog');
                            dialog.innerHTML = transport.responseText;
                            if (loadFunction) {
                                loadSubTree(rootPath, 'root', textBoxId, 'false');
                            }
                        }
                    });
                }
            } else {
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree('/', 'root', textBoxId, 'false');
                        }
                    }
                });
            }
        } else {
            if (relativeRoot) {
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,displayRootPath:displayRootPath,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'false');
                        }
                    }
                });
            } else {
                if (!rootPath) {
                    rootPath = onOKCallback;
                }
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,displayRootPath:displayRootPath,random:random},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'false');
                        }
                    }
                });
            }
        }
        //new Ajax.Updater('dialog', '../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random} });
    } else {
        new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
            method: 'get',
            parameters: {textBoxId:textBoxId,random:random},
            onSuccess: function(transport) {
                var dialog = $('dialog');
                dialog.innerHTML = transport.responseText;
                if (loadFunction) {
                    loadSubTree('/', 'root', textBoxId, 'false');
                }
            }
        });
        //new Ajax.Updater('dialog', '../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,random:random} });
    }
}

function showHDFStree(textBoxId, onOKCallback, rootPath) {
    showHDFSTreeWithLoadFunction(true, textBoxId, onOKCallback, rootPath, null, "false");
}

function showCollectionTreeWithLoadFunction(loadFunction, textBoxId, onOKCallback, rootPath, relativeRoot) {

    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource.tree.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource.tree"], 500, false);
    var random = getRandom();
    if (onOKCallback) {
        if (typeof onOKCallback == "function") {
            onOKCallback = onOKCallback.toString().substring('function '.length);
            onOKCallback = onOKCallback.substring(0, onOKCallback.indexOf("("));
            if (rootPath) {
                if (relativeRoot) {
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                     parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            } else {
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                     parameters: {textBoxId:textBoxId,rootPath:rootPath,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                         onSuccess: function(transport) {
                             var dialog = $('dialog');
                             dialog.innerHTML = transport.responseText;
                             if (loadFunction) {
                                 loadSubTree(rootPath, 'root', textBoxId, 'true');
                             }
                         }
                     });
                 }
             } else {
                 new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                     method: 'get',
                    parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree('/', 'root', textBoxId, 'true');
                        }
                    }
                });
            }
        } else {
            if (relativeRoot) {
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,relativeRoot:relativeRoot,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            } else {
                if (!rootPath) {
                    rootPath = onOKCallback;
                }
                new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
                    method: 'get',
                    parameters: {textBoxId:textBoxId,rootPath:rootPath,random:random,hideResources:'true'},
                    onSuccess: function(transport) {
                        var dialog = $('dialog');
                        dialog.innerHTML = transport.responseText;
                        if (loadFunction) {
                            loadSubTree(rootPath, 'root', textBoxId, 'true');
                        }
                    }
                });
            }
        }
        //new Ajax.Updater('dialog', '../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,onOKCallback:onOKCallback,random:random,hideResources:'true'} });
    } else {
        new Ajax.Request('../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', {
            method: 'get',
            parameters: {textBoxId:textBoxId,random:random,hideResources:'true'},
            onSuccess: function(transport) {
                var dialog = $('dialog');
                dialog.innerHTML = transport.responseText;
                if (loadFunction) {
                    loadSubTree('/', 'root', textBoxId, 'true');
                }
            }
        });
        //new Ajax.Updater('dialog', '../hdfsmgt/hdfs_tree_ajaxprocessor.jsp', { method: 'get', parameters: {textBoxId:textBoxId,random:random,hideResources:'true'} });
    }
}

function showCollectionTree(textBoxId, onOKCallback, rootPath) {
    showCollectionTreeWithLoadFunction(true, textBoxId, onOKCallback, rootPath);
}

function pickPath(path, textBoxId, reference, isfolder) {
    if (!textBoxId || textBoxId == 'null') {
        setTreeNavigationPath(path, reference);
        window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + path.replace(/&/g, "%26")+"&isfolder="+isfolder;
        return;
    }
    document.getElementById('pickedPath').value = path;
    document.getElementById('pickedPath').focus();
    //document.getElementById(textBoxId).value = document.getElementById('pickedPath').value;
    //This is a hack to fix the ie7 issue with not getting the object using it's id
    //if (textBoxId == "associationPaths") document.forms.assoForm.associationPaths.value = document.getElementById('pickedPath').value;
}

function loadSubTree(path, parentId, textBoxId, hideResources, callback) {
    var theImg = document.getElementById("plus_" + parentId);
    sessionAwareFunction(function() {
        if (theImg) {
            var theDiv = document.getElementById('child_' + parentId);
            if (theDiv) {
                theDiv.style.display = '';
            }
            var isPlus = theImg.style.display != 'none';
            if (isPlus) {
                while (path.indexOf(" ") > 0) {
                    path = path.replace(" ", "%20")
                }
                while (path.indexOf("&") > 0) {
                    path = path.replace(/&/g, "%26")
                }
                var url = '../hdfsmgt/hdfs_sub_tree_ajaxprocessor.jsp?path=' + path + '&parentId=' + parentId + '&textBoxId=' + textBoxId + (hideResources == 'true' ? '&hideResources=true' : '');
                jQuery("#child_" + parentId).load(url, null,
                        function(data, status, t) {
                            if (status != "success") {
                                //CARBON.showWarningDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["error.occured"]);
                                document.getElementById("local-registry-td").style.display = "none";
                            }
                            if (callback && typeof callback == "function") {
                                callback();
                            }
                            // Add necessary logic to handle these scenarios if needed
                            if (data || t) {}
                        });
            } else {
                if (theDiv) {
                    theDiv.innerHTML = "";
                    theDiv.style.display = 'none';
                }
            }
            showHideCommon('plus_' + parentId);
            showHideCommon('minus_' + parentId);
        }

    }, org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
}

function getWindowWidth() {
    var crossWidth = 0; //crossHeight = 0;
    if (typeof( window.innerWidth ) == 'number') {
        //Non-IE
        crossWidth = window.innerWidth;
        //crossHeight = window.innerHeight;
    } else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight )) {
        //IE 6+ in 'standards compliant mode'
        crossWidth = document.documentElement.clientWidth;
        //crossHeight = document.documentElement.clientHeight;
    } else if (document.body && ( document.body.clientWidth || document.body.clientHeight )) {
        //IE 4 compatible
        crossWidth = document.body.clientWidth;
        //crossHeight = document.body.clientHeight;
    }
    return(crossWidth);
}

function fixResourceTable() {
    var wdWidth = getWindowWidth();
    var colWidth = (wdWidth * 65) / 100 - 560;
    if (document.getElementById('resourceSizer') == null) return;

    document.getElementById('resourceSizer').width = colWidth + "px";

    //Calculate the truncate length for each resource name
    var truncateLength = parseInt(colWidth / 12 + (wdWidth - 1016) / 9);

    //Truncate the resource names by getting there elements by class name
    /*var classnameRef = "__resourceNameRef";*/
    var classname = "__resourceName";

    var node = document.getElementById("entryList");

    var re = new RegExp('\\b' + classname + '\\b');
    var els = node.getElementsByTagName("*");
    var elsRef = node.getElementsByTagName("*");

    for (var i = 0,j = els.length; i < j; i++) {

        if (re.test(els[i].className)) els[i].innerHTML = elsRef[i - 1].innerHTML.truncate(truncateLength);

    }

}

function validateWSDL() {
    showHideCommon('validationDiv');
}

function loadResourcePage(path, viewMode, consumerID, targetDiv) {
    return loadJSPPage('hdfs_dashboard', path, viewMode, consumerID, targetDiv);
}

//private
function loadJSPPage(pagePrefixName, path, viewMode, consumerID, targetDiv) {

    if (viewMode != undefined && viewMode != null && 'inlined' == viewMode) {

        var suffix = "&resourceViewMode=inlined";
        if (consumerID != undefined && consumerID != null && consumerID != "") {
            suffix += "&resourcePathConsumer=" + consumerID;
        }

        if (targetDiv == null || targetDiv == undefined || targetDiv == "" || targetDiv == "null") {
            targetDiv = 'registryBrowser';
        }

        suffix += "&targetDivID=" + targetDiv;

        if (path == "#") {
            path = '/';
        }
        path = path.replace(/&/g, "%26");

        var url = '../hdfsmgt/' + pagePrefixName + '_ajaxprocessor.jsp?path=' + path + suffix;

        jQuery("#popupContent").load(url, null,
                function(res, status, t) {
                    if (status != "success") {
                        //CARBON.showWarningDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["error.occured"]);
                    }
                    // Add necessary logic to handle these scenarios if needed
                    if (res || t) {}
                });
        return false;
    } else {
        path = path.replace(/&/g, "%26");
        if (pagePrefixName == 'resource') {
            document.location.href = '../hdfsmgt/' + pagePrefixName + '.jsp?region=region3&item=hdfs_list_menu&path=' + path + "&screenWidth=" + screen.width;
        } else {
            document.location.href = '../hdfsmgt/' + pagePrefixName + '.jsp?path=' + path + "&screenWidth=" + screen.width;

        }
    }
    return true;
}

function setPathURL(divNum)
{
	onceDone = 0;
	j = 2;
	fullString = "";

	screenWidth = screen.width;

	switch (screenWidth)
	{
	case 1024:
	  maxChars = 90;
	  break;
	default:
	  maxChars = screenWidth / 1024 * 90;
	}

	while (j < divNum)
	{
		divID = "pathResult" + j;
		fullString = fullString + document.getElementById(divID).innerHTML;


		if ((fullString.length > maxChars) && (onceDone<8))
		{
			document.getElementById(divID).innerHTML = "<br/>"+document.getElementById(divID).innerHTML;
			onceDone = onceDone + 1;
            fullString = "";
		}
		j = j + 1;
	}

}

function showInLinedRegistryBrowser(id) {
    showInLinedRegistryBrowserOnDiv('registryBrowser', id);
}

function showInLinedRegistryBrowserOnDiv(divID, id) {
	//TODO check on this

    if (id == null || id == undefined || id == "") {
        CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["registry.key.cannot.be.null.or.empty"]);
    }

    var url = '../hdfsmgt/hdfs_tree_ajaxprocessor.jsp?resourceViewMode=inlined&resourcePathConsumer=' + id + '&targetDivID=' + divID + '&random=' + random + '&textBoxId=' + id;

      //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' + org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource.tree.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent, org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource.tree"], 500, false);
    var random = getRandom();
    new Ajax.Updater('dialog', url, { method: 'get', parameters:{random:getRandom()} ,evalScripts:true});
}

function hideInLinedRegistryBrowser(divID) {

    if (divID == null || divID == undefined || divID == "" || divID == "null") {
        divID = 'registryBrowser';
    }
    var nsDiv = document.getElementById(divID);
    if (nsDiv != null && nsDiv != undefined) {
        nsDiv.style.display = "none";
        nsDiv.innerHTML = "";
    }

    return false;
}


function setResolvedResourcePathOnConsumer(consumerID, synapseRoot) {

    if (consumerID == undefined || consumerID == null || "" == consumerID) {
        return false;
    }

    var path = document.getElementById("pickedPath").value;

    if (path == null || path == undefined || path == "" || path == "null") {
        return false;
    }
    var suffix = "&resourceViewMode=inlined";
    if (synapseRoot != undefined && synapseRoot != null && synapseRoot != "") {
        suffix += "&synapseroot=" + synapseRoot;
    } else {
        return false;
    }

    if (path == "#") {
        path = '/';
    }

    var url = '../resources/save-path_ajaxprocessor.jsp?path=' + path + suffix;

    jQuery.get(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["error.occured"]);
                } else {
                    setResourcePathOnConsumer(consumerID, trim(data));
                }
            });
    return false;
}
function ltrim(str) {
    for (var k = 0; k < str.length && str.charAt(k) <= " "; k++) {}
    return str.substring(k, str.length);
}
function rtrim(str) {
    for (var j = str.length - 1; j >= 0 && str.charAt(j) <= " "; j--) {}
    return str.substring(0, j + 1);
}

//This function accepts a String and trims the string in both sides of the string ignoring space characters
function trim(stringValue) {
    return ltrim(rtrim(stringValue));
}

function showHideCommon(divId){
	var theDiv = document.getElementById(divId);
	if(theDiv.style.display=="none"){
		theDiv.style.display="";
	}else{
		theDiv.style.display="none";
	}
}
function blockManual(e){
    if (e) {
        //handle-event logic
    }
	var path=document.getElementById('uResourceFile');
	 return (path.value.length > 2);
}
function handleRichText(){
	var radioObj = document.textContentForm.richText;
	var selected = "";
	for(var i=0;i<radioObj.length;i++){
		if(radioObj[i].checked)selected = radioObj[i].value;
	}

	var textAreaPanel = $('textAreaPanel');
	var trPlainContent = $('trPlainContent');
	var content = "";
	if (textContentEditor) {
	        textContentEditor.saveHTML();
	        content = textContentEditor.get('textarea').value;
	}
	if(selected=="plain"){
		trPlainContent.style.display = "";
		textAreaPanel.style.display = "none";
		trPlainContent.value=textContentEditor.get('textarea').value;
	}
	if(selected=="rich"){
		trPlainContent.style.display = "none";
		textAreaPanel.style.display = "";
		textContentEditor.setEditorHTML(trPlainContent.value);
		textContentEditor.saveHTML();
		textContentEditor.render();
	}
}

function handleUpdateRichText(){
    var radioObj = new Array();
    radioObj[0] = $('editTextContentIDRichText0');
    radioObj[1] = $('editTextContentIDRichText1');
	var selected = "";
	for(var i=0;i<radioObj.length;i++){
		if(radioObj[i].checked)selected = radioObj[i].value;
	}

	var textAreaPanel = $('editTextContentTextAreaPanel');
	var trPlainContent = $('editTextContentIDPlain');
	var content = "";
	if (textContentUpdator) {
	        textContentUpdator.saveHTML();
	        content = textContentUpdator.get('textarea').value;
	}
	if(selected=="plain"){
		trPlainContent.style.display = "";
		textAreaPanel.style.display = "none";
  		trPlainContent.value=textContentUpdator.get('textarea').value;
	}
	if(selected=="rich"){
		trPlainContent.style.display = "none";
		textAreaPanel.style.display = "";
		textContentUpdator.setEditorHTML(trPlainContent.value);
		textContentUpdator.saveHTML();
		textContentUpdator.render();
	}
}

function loadActionPane(rowNum,type){

        var clickedTD = "actionPaneHelper"+rowNum;
        var actionLink = type+"Link"+rowNum;
        var toShow=type+"Pane"+rowNum;
        var todo = "toShow";
        if($(toShow).style.display != "none"){
        	todo ="toHide";
        }
	var allElms = document.getElementById("entryList").getElementsByTagName("*");

	for (var i = 0; i < allElms.length; i++) {
		if(YAHOO.util.Dom.hasClass(allElms[i], "actionPaneSelector")){
			allElms[i].style.display="none";
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "action-pane-helper")){
			YAHOO.util.Dom.removeClass(allElms[i],"actionSelected");
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "entryName-expanded")){
			YAHOO.util.Dom.removeClass(allElms[i],"entryName-expanded");
		}
		if(YAHOO.util.Dom.hasClass(allElms[i], "copy-move-panel")){
			allElms[i].style.display="none";
		}
	}
	if(todo == "toShow"){
		YAHOO.util.Dom.addClass(clickedTD,"actionSelected");
		$(toShow).style.display = "";

		YAHOO.util.Dom.addClass(actionLink,"entryName-expanded");
	}
	else{
		$(toShow).style.display = "none";

		YAHOO.util.Dom.addClass(actionLink,"entryName-contracted");
	}
}
function validateResourcePathAndLength(fld) {

    var error = "";
    var regx = RegExp("(//)");
    if (fld.value.match(regx)) {
        error = "The given name" + " '<strong>" + fld.value + "</strong>' " + "is not a valid path" + "<br />";
    } else if (fld.value.length > 256) {
        error = "The given name" + " '<strong>" + fld.value + "</strong>' " + " is too long" + "<br />";
    }
    return error;
}

function validateDescriptionLength(fld) {

    var error = "";
    if (fld.value.length > 1000) {
        error = org_wso2_carbon_hdfs_mgt_ui_jsi18n["the.given.description"] + " '<strong>" + fld.value + "</strong>' " + org_wso2_carbon_hdfs_mgt_ui_jsi18n["cannot.contain.more.than2"] + "<br />";
    }
    return error;
}

function loadFromPath() {
    var pickedPath = $('uLocationBar');
    var pathValue = null;
    if(pickedPath != null || pickedPath != "")
    	{
   // 		pathvalue = pickedPath.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '').replace(/&/g, "%26");
    	}
    window.location = "../hdfsmgt/hdfs_dashboard.jsp?region=region3&item=resource_browser_menu&viewType=std&path=" + pickedPath.value;
}

function validateProvidedResoucePath(pickedPath, errorMessage) {
    var reason = "";
	var result = false;

	pickedPath.value = pickedPath.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
	if (pickedPath.value.length == 0) {
	        reason = errorMessage + "<br />";
	}
	if(reason ==""){
        new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {pickedPath: pickedPath.value,random:getRandom()},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ResourceExists----/) == -1){
                    CARBON.showWarningDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource.does.not.exit"] + " <strong>" + pickedPath.value + "</strong>.");
                    result = false;
                } else {
                    result = true;
                }
            },
            onFailure: function() {

            }
        });
	} else{
		CARBON.showWarningDialog(reason);
		result = false;
	}
	return result;
}

function validateResoucePath(){
	var pickedPath=$('pickedPath');
    return validateProvidedResoucePath(pickedPath, org_wso2_carbon_hdfs_mgt_ui_jsi18n["picked.path.empty"]);
}


YAHOO.util.Event.addListener(window, 'resize', truncateResourceNames);
YAHOO.util.Event.addListener(window, 'load', loadData);
var oldNodes = new Array();
var firstRun = true;
function truncateResourceNames(){
    if(document.getElementById('pointA') == null || $("entryList") == null){
		return;
	}
    var allNodes = YAHOO.util.Dom.getElementsByClassName("trimer");

	var wpWidth = YAHOO.util.Dom.getViewportWidth();
	var textAreaSize;
	if($('pointA') != null && $('pointA').style.display != "none"){
		textAreaSize = wpWidth - 770;
	}else{
		textAreaSize = wpWidth - 1030;
	}
	var textSize = (textAreaSize-(textAreaSize%6))/6;
    if(textSize <14){
        textSize = 14;
    }
    if(firstRun){
        oldNodes = new Array();
    }
	for (var i = 0; i < allNodes.length; i++) {
        var toTrim="";
        if(firstRun){
            oldNodes.push(allNodes[i].innerHTML);
            toTrim="" + allNodes[i].innerHTML;
        }else{
            toTrim="" + oldNodes[i];
        }

		if(toTrim.length>15) {
			allNodes[i].innerHTML = toTrim.truncate(textSize,'..');//toTrim.length+ " -- " +textSize + "/n";
		}

	}
    firstRun = false;
}
function loadData(){
    firstRun = true;
    truncateResourceNames();
}

function displaySuccessMessage(pickedPath, operation) {
    var message = "";
    var differentiate = "differentiate";
    new Ajax.Request('../resources/resource_exists_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {pickedPath: pickedPath, differentiate: differentiate,random:getRandom()},
        asynchronous:false,
        onSuccess: function(transport) {
            var returnValue = transport.responseText;
            if (returnValue.search(/----ResourceExists----/) != -1){
                message = org_wso2_carbon_hdfs_mgt_ui_jsi18n["successfully"] + " " + operation + " " + org_wso2_carbon_hdfs_mgt_ui_jsi18n["resource"] + ".";
            } else if (returnValue.search(/----CollectionExists----/) != -1){
                message = org_wso2_carbon_hdfs_mgt_ui_jsi18n["successfully"] + " " + operation + " " + org_wso2_carbon_hdfs_mgt_ui_jsi18n["collection"] + ".";
            }
        },
        onFailure: function() {

        }
    });
    CARBON.showInfoDialog(message);
}

function handleWindowOk(textBoxId, onOK){
     handleRelativeWindowOk("", textBoxId, onOK);
 }

function handleRelativeWindowOk(path, textBoxId, onOK) {
    if (!textBoxId) {
        return;
    }
    var theTextBox = document.getElementById(textBoxId);
    var pickedValue = document.getElementById('pickedPath').value;
    if (path != "") {
        pickedValue = pickedValue.replace(path, "");
    }
	if (textBoxId == "associationPaths") {
        // This is a hack to fix the ie7 issue with not getting the object using it's id
         document.forms['assoForm'].associationPaths.value = pickedValue;
    } else {
         theTextBox.value = pickedValue;
    }
    if (onOK && typeof onOK == "function") {
        onOK();
    }
}
function getRandom(){
    return Math.floor(Math.random() * 2000);
}

function toggleSaveMediaType() {
    if (jQuery('#toggleSaveMediaType_view').is(":visible")) {
        jQuery('#toggleSaveMediaType_view').hide();
        jQuery('#toggleSaveMediaType_edit').show();
        jQuery('#toggleSaveMediaType_editBtn').hide();
        jQuery('#toggleSaveMediaType_saveBtn').show();
        jQuery('#toggleSaveMediaType_cancelBtn').show();
    } else {
        jQuery('#toggleSaveMediaType_view').show();
        jQuery('#toggleSaveMediaType_edit').hide();
        jQuery('#toggleSaveMediaType_editBtn').show();
        jQuery('#toggleSaveMediaType_saveBtn').hide();
        jQuery('#toggleSaveMediaType_cancelBtn').hide();
    }
}

/**
 * Delete Row after deleting the item.
 * @param type
 * @param i
 */
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


function navigatePages(wantedPage, resourcePath, viewMode, consumerID,
		targetDivID) {
	fillContentSection(resourcePath, wantedPage, viewMode, consumerID,
			targetDivID);
	YAHOO.util.Event.onAvailable("xx" + wantedPage, loadData);
}


function addRole(roleNamedivId) {
	sessionAwareFunction(function() {
		var userName = document.getElementById('user').value;
		var roleName = document.getElementById(roleNamedivId).value;
		var readAllowed = document.getElementById('readAllowed').value;
		var writeAllowed = document.getElementById('writeAllowed').value;
		var executeAllowed = document.getElementById('executeAllowed').value;
		
		if(readAllowed == "ra"){
			readAllowed = true;
		}if(writeAllowed == "wa"){
			writeAllowed = true;
		}if(executeAllowed == "da"){
			executeAllowed = true;
		}
           var reason = "";
		       reason += validateString(roleNamedivId);
		   if (reason == "") {
	            reason += validateIllegal(roleNamedivId);
	        }
	        if (reason == "") {
	            reason += validateResourcePathAndLength(document.getElementById(roleNamedivId));
	        }
	       
	        if (reason != "") {
	            CARBON.showWarningDialog(reason);
	            return false;
	        }else{
		
				new Ajax.Request('../hdfsmgt/permissions_ajaxprocessor.jsp',
				         {
					method:'post',
		            parameters: {userName:userName,roleName:roleName,readAllowed:readAllowed,writeAllowed:writeAllowed,executeAllowed:executeAllowed},
		
		             onSuccess: function() {
		             	 CARBON.showInfoDialog(org_wso2_carbon_hdfs_mgt_ui_jsi18n["add.role.successful"], function(){
		             		 window.location = '../hdfsmgt/hdfs_roles.jsp';
		             		 });
		                 
		             },
		
		             onFailure: function(transport) {
		             	   CARBON.showErrorDialog(trim(transport.responseText),function(){
		            	          return;
		             	      });
		             }
				          });
	        	}
			    },org_wso2_carbon_hdfs_mgt_ui_jsi18n["session.timed.out"]);
}

function handlePeerCheckbox(myID, peerID) {

    //Only one is checked always, so it is safe to make the peer false
    if (myID !=  peerID) {
        var peer = document.getElementById(peerID);
        peer.checked = false;
    }
}

function validateString(fld1name) {
    var stringValue = document.getElementsByName(fld1name)[0].value;
    var errorMessage = "";
    if (stringValue=="" || stringValue == '') {
    	errorMessage = "Please fill empty fields.";
    }
    return errorMessage;
}

