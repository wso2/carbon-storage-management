function displayNodeOperations(hostAddress,token,hostName,status) {
    if(status.toLowerCase()=="up")
    {
        location.href = 'node_operations.jsp?hostAddress=' + hostAddress+'&token='+token+'&hostName='+hostName;
    }
    else
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.node.not.available"], function() {
            location.href = 'node_operations.jsp?hostAddress=' + hostAddress+'&token='+token+'&hostName='+hostName;
        });
    }
}
function displayNodeStats(hostAddress,token,hostName,status) {
    if(status.toLowerCase()=="up")
    {
        location.href = 'node_stats.jsp?hostAddress=' + hostAddress+'&token='+token+'&hostName='+hostName;
    }
    else
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.node.not.available"], function() {
            location.href = 'node_stats.jsp?hostAddress=' + hostAddress+'&token='+token+'&hostName='+hostName;
        });
    }

}
function displayKeyspaceOperations(hostAddress,hostName) {
    location.href = 'keyspace_operations.jsp?hostAddress='+ hostAddress+'&hostName='+hostName;
}
function displayColumnFamilyStats(hostAddress,keyspace,hostName)
{
    location.href = 'column_family_stats.jsp?hostAddress=' + hostAddress+'&keyspace='+keyspace+'&hostName='+hostName;
}
function displayColumnFamlilyOperations(hostAddress,keyspace,hostName) {
    location.href = 'column_family_operations.jsp?hostAddress=' + hostAddress+'&keyspace='+keyspace+'&hostName='+hostName;
}

function decommissionNode(hostAddress,nodeCount)
{
    if(nodeCount>1)
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.decommission.node.warning"], function() {
            var url = 'decommision_node-ajaxprocessor.jsp?hostAddress='+hostAddress;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.decommission.node.fail"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.decommission.success"], function () {
                                       CARBON.closeWindow();
                                       location.href = 'cassandra_operation.jsp';
                                   }, function () {
                                       CARBON.closeWindow();
                                       location.href = 'cassandra_operation.jsp';

                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.decommission.node.fail"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tools.decommission.node.exists"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}

function drainNode(hostAddress)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.drain.node.warning"], function() {
        var url = 'drain_node-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.drain.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });

                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.drain.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return true;
}

function performGC(hostAddress)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.warning"], function() {
        var url = 'performGC-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.perform.garbage.collector.node.fail"]);
                               return false;
                           }

                       }
                   }, "json");
    });

    return false;
}

function moveNode(hostAddress)
{
    var newToken=jQuery('#newToken').val();
    if(newToken!=null &&newToken!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.move.node.warning"], function() {
            var url = 'move_node-ajaxprocessor.jsp?hostAddress=' + hostAddress+'&newToken='+newToken;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.error"]);
                               jQuery('#newToken').val("");
                               jQuery('#myDiv').hide('slow');
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.move.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });

                                   return true;
                               }
                               else
                               {
                                   jQuery('#newToken').val("");
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.token.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}

function showTokenForm()
{
    jQuery('#myDiv').slideToggle();

}
function showTakeSnapShotForm()
{
    jQuery('#clearSnapshotTag').val("");
    jQuery('#divClearSnapShot').hide('slow');
    jQuery('#divSnapShot').slideToggle();

}
function showClearSnapShotForm()
{
    jQuery('#snapshotTag').val("");
    jQuery('#divSnapShot').hide('slow');
    jQuery('#divClearSnapShot').slideToggle();

}
function showKSTakeSnapShotForm(keyspace)
{
    jQuery('#'+keyspace+'ClearTag').val("");
    jQuery('#'+keyspace+'DivClear').hide('slow');
    jQuery('#'+keyspace+'DivTake').slideToggle();

}
function showKSClearSnapShotForm(keyspace)
{
    jQuery('#'+keyspace+'TakeTag').val("");
    jQuery('#'+keyspace+'DivTake').hide('slow');
    jQuery('#'+keyspace+'DivClear').slideToggle();

}
function joinRing(hostAddress)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.warning"], function() {
        var url = 'joinRing-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               window.location.reload();
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.joinRing.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}

function disableRPC(hsotName){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.warning"], function() {
        var url = 'diableRPCServerStatus-ajaxprocessor.jsp?hostAddress='+hsotName;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               window.location.reload();
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopRPC.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}
function enableRPC(hostAddress){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.warning"], function() {
        var url = 'enableRPCServerStatus-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               window.location.reload();
                               return true;

                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startRPC.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}

function disableGossip(hostAddress){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.warning"], function() {
        var url = 'disableGossipServerStatus-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               window.location.reload();
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}

function enableGossip(hostAddress){
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopGossip.node.warning"], function() {
        var url = 'enableGossipServerStatus-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startGossip.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               window.location.reload();
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startGossip.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}

function enableIncrementalBackUp(hostAddress)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.warning"], function() {
        var url = 'enableIncrementalBackup-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                                   window.location.reload();
                               }, function () {
                                   CARBON.closeWindow();
                                   window.location.reload();
                               });

                               return true;

                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.startIBackup.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false;
}
function disableIncrementalBackUp(hostAddress)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.warning"], function() {
        var url = 'disableIncrementalBackup-ajaxprocessor.jsp?hostAddress='+hostAddress;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                                   window.location.reload();
                               }, function () {
                                   CARBON.closeWindow();
                                   window.location.reload();
                               });
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.stopIBackup.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    });

    return false
}



function takeNodeSnapShot(hostAddress)
{
    var snapshotTag=jQuery('#snapshotTag').val();
    if(snapshotTag!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.warning"], function() {
            var url = 'takeSnapShot_node-ajaxprocessor.jsp?hostAddress='+hostAddress+'&snapshotTag='+snapshotTag;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                               jQuery('#snapshotTag').val("");
                               jQuery('#divSnapShot').hide('slow');
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });

                                   return true;
                               }
                               else{
                                   jQuery('#snapshotTag').val("");
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                                   return false;
                               }

                           }
                       }, "json");
        });
    }
    else
    {
        jQuery('#snapshotTag').val("");
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
}
function clearNodeSnapShot(hostAddress)
{
    var clearSnapshotTag=jQuery('#clearSnapshotTag').find(":selected").text();
    if(clearSnapshotTag!="Not Available")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.warning"], function() {
            var url = 'clearSnapShot_node-ajaxprocessor.jsp?hostAddress='+hostAddress+'&clearSnapshotTag='+clearSnapshotTag;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                               jQuery('#clearSnapshotTag').val("");
                               jQuery('#divClearSnapShot').hide('slow');
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}

function repairKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.repair.keyspace.warning"], function() {
        var url = 'repair_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog("cassandra.cluster.repair.keyspace.error");
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function cleanUpKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.warning"], function() {
        var url = 'cleanUp_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               return false;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function flushKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.warning"], function() {
        var url = 'flush_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function scrubKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.warning"], function() {
        var url = 'scrub_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               })
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function upgradeSSTablesKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.warning"], function() {
        var url = 'upgradeSSTables_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function compactKeyspace(hostAddress,keyspace)
{
    CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.warning"], function() {
        var url = 'compact_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.error"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.keyspace.error"]);
                               return false;
                           }
                       }
                   }, "json");
    });
    return true;
}

function compactColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=$(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });
    if(selectedColumnFamilies!=null &&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.warning"], function() {
            var url = 'compact_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.compact.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}

function flushColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=$(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });
    if(selectedColumnFamilies!=null&&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.warning"], function() {
            var url = 'flush_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}

function repairColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=$(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });

    if(selectedColumnFamilies!=null&&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.warning"], function() {
            var url = 'repair_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.repair.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}
function cleanUpColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=$(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });
    if(selectedColumnFamilies!=null&&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.warning"], function() {
            var url = 'cleanUp_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.cleanup.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}
function scrubColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=$(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });

    if(selectedColumnFamilies!=null&&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.warning"], function() {
            var url = 'scrub_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                  CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.scrub.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}

function upgradeSSTablesColumnFamily(hostAddress,keyspace)
{
    var init = null;
    var selectedColumnFamilies="";
    jQuery("input:checkbox[name=columnFamily]:checked").each(function() {
        init=jQuery(this).val();
        selectedColumnFamilies+=init;
        selectedColumnFamilies+="_separator";
    });
    if(selectedColumnFamilies!=null&&selectedColumnFamilies!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.flush.column.family.warning"], function() {
            var url = 'upgradeSSTables_cf-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+"&columnFamilies="+selectedColumnFamilies;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.column.family.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                                   return true;
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.upgradeSSTable.column.family.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.select.columnFamily"]);
        return false;
    }
    return true;
}

function takeKSNodeSnapShot(hostAddress,keyspace)
{
    var snapshotTag=jQuery('#'+keyspace+'TakeTag').val();
    if(snapshotTag!="" &&snapshotTag!=null)
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.warning"], function() {
            var url = 'takeSnapShot_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&snapshotTag='+snapshotTag+"&keyspace="+keyspace;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               jQuery('#'+keyspace+'TakeTag').val("");
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                               }
                               else
                               {
                                   jQuery('#'+keyspace+'TakeTag').val("");
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        jQuery('#'+keyspace+'TakeTag').val("");
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}

function clearKSNodeSnapShot(hostAddress,keyspace)
{
    var clearSnapshotTag=jQuery('#ClearSnapshotTagKS').find(":selected").text();
    if(clearSnapshotTag!="Not Available")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.warning"], function() {
            var url = 'clearSnapShot_ks-ajaxprocessor.jsp?hostAddress='+hostAddress+'&clearSnapshotTag='+clearSnapshotTag+"&keyspace="+keyspace;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   }, function () {
                                       CARBON.closeWindow();
                                       window.location.reload();
                                   });
                               }
                               else
                               {
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.clear.snapshot.node.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return false;
}

function invalidateCache(hostAddress)
{
    var type=jQuery('#invalidateCacheType').find(":selected").text();
    var url = 'invalidateCache-ajaxprocessor.jsp?hostAddress='+hostAddress+'&type='+type;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           jQuery('#invalidateCacheForm').hide('slow');
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    jQuery('#invalidateCacheForm').hide('slow');
    return false;
}

function setCacheCapacity(hostAddress)
{
    var type=jQuery('#cacheType').find(":selected").text();
    var capacity=jQuery('#cacheCapacity').val();

    if(capacity!=null && capacity!="")
    {
        var url = 'setCacheCapacity-ajaxprocessor.jsp?hostAddress='+hostAddress+'&type='+type+'&capacity='+capacity;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               jQuery('#cacheCapacity').val("");
                               jQuery('#setCacheForm').hide('slow');
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                           }
                           else
                           {
                               jQuery('#cacheCapacity').val("");
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    }
    else
    {
        jQuery('#cacheCapacity').val("");
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    }
    return false;
}

function setThroughput(hostAddress,type)
{
    var throughput;
    if(type=="stream")
    {
        throughput=jQuery('#streamThroughput').val();
    }
    else
    {
        throughput=jQuery('#compactionThroughput').val();
    }
    if(throughput!=null && throughput!="")
    {
        var url = 'setThroughputs-ajaxprocessor.jsp?hostAddress='+hostAddress+'&type='+type+'&throughput='+throughput;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {

                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                               if(type=="stream")
                               {
                                   jQuery('#streamThroughput').val("");
                                   jQuery('#setStreamThroughputForm').hide('slow');
                               }
                               else
                               {
                                   jQuery('#compactionThroughput').val("");
                                   jQuery('#setCompactionThroughputForm').hide('slow');
                               }
                           }
                           else
                           {

                               if(type=="stream")
                               {
                                   jQuery('#streamThroughput').val("");
                               }
                               else
                               {
                                   jQuery('#compactionThroughput').val("");
                               }
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);

                           }
                       }
                   }, "json");
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    }
    return false;
}
function showCacheFrom()
{
    jQuery('#invalidateCacheForm').hide('slow');
    jQuery('#setCacheForm').slideToggle();
}

function showInvalidateCacheForm()
{
    jQuery('#cacheCapacity').val("");
    jQuery('#setCacheForm').hide('slow');
    jQuery('#invalidateCacheForm').slideToggle();
}
function stopCompaction(hostAddress)
{
    var type;
    type=jQuery('#compactionTypes').find(":selected").text();
    if(type!=null)
    {
        var url = 'stopCompaction-ajaxprocessor.jsp?hostAddress='+hostAddress+'&type='+type;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               jQuery('#stopCompactionForm').hide('slow');
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                               return false;
                           }
                       }
                   }, "json");
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    }
    return false;
}

function showCompactionForm()
{
    jQuery('#datacenterName').val("");
    jQuery('#rebuildForm').hide('slow');
    jQuery('#streamThroughput').val("");
    jQuery('#setStreamThroughputForm').hide('slow');
    jQuery('#compactionThroughput').val("");
    jQuery('#setCompactionThroughput').hide('slow');
    jQuery('#stopCompactionForm').slideToggle();
}

function showStreamFrom()
{
    jQuery('#datacenterName').val("");
    jQuery('#rebuildForm').hide('slow');
    jQuery('#stopCompactionForm').hide('slow');
    jQuery('#compactionThroughput').val("");
    jQuery('#setCompactionThroughput').hide('slow');
    jQuery('#setStreamThroughputForm').slideToggle();
}

function showRebuildForm()
{
    jQuery('#streamThroughput').val("");
    jQuery('#setStreamThroughputForm').hide('slow');
    jQuery('#stopCompactionForm').hide('slow');
    jQuery('#compactionThroughput').val("");
    jQuery('#setCompactionThroughputForm').hide('slow');
    jQuery('#rebuildForm').slideToggle();
}

function showCompactionTForm()
{
    jQuery('#rebuildForm').hide('slow');
    jQuery('#stopCompactionForm').hide('slow');
    jQuery('#streamThroughput').val("");
    jQuery('#setStreamThroughputForm').hide('slow');
    jQuery('#setCompactionThroughputForm').slideToggle();
}

function removeToken(hostAddress)
{
    location.href = 'removeTokenForm.jsp?hostAddress=' + hostAddress;
}

function rebuildCF(hostAddress,keyspace,columnFamily)
{
    var url = 'rebuildCF-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}

function showIndexForm(columnFamily)
{
    jQuery('#'+columnFamily+'minThreshold').val("");
    jQuery('#'+columnFamily+'maxThreshold').val("");
    jQuery('#'+columnFamily+'setCompactionThresholdForm').hide('slow');
    jQuery('#'+columnFamily+'CFSnapshotTag').val("");
    jQuery('#'+columnFamily+'CFSnapshotForm').hide('slow');
    jQuery('#'+columnFamily+'rebuildWithIndexForm').slideToggle();
}

function showCompactionThresholdForm(columnFamily)
{
    jQuery('#'+columnFamily+'beginIndex').val("");
    jQuery('#'+columnFamily+'endIndex').val("");
    jQuery('#'+columnFamily+'CFSnapshotTag').val("");
    jQuery('#'+columnFamily+'CFSnapshotForm').hide('slow');
    jQuery('#'+columnFamily+'rebuildWithIndexForm').hide('slow');
    jQuery('#'+columnFamily+'setCompactionThresholdForm').slideToggle();
}
function showCFSnapshotForm(columnFamily)
{
    jQuery('#'+columnFamily+'beginIndex').val("");
    jQuery('#'+columnFamily+'endIndex').val("");
    jQuery('#'+columnFamily+'minThreshold').val("");
    jQuery('#'+columnFamily+'maxThreshold').val("");
    jQuery('#'+columnFamily+'setCompactionThresholdForm').hide('slow');
    jQuery('#'+columnFamily+'rebuildWithIndexForm').hide('slow');
    jQuery('#'+columnFamily+'CFSnapshotForm').slideToggle();
}

function refreshCF(hostAddress,keyspace,columnFamily)
{
    var url = 'refreshCF-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {

                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}



function rebuildWithIndex(hostAddress,keyspace,columnFamily)
{
    var beginIndex=jQuery('#'+columnFamily+'beginIndex').val();
    var endIndex=jQuery('#'+columnFamily+'endIndex').val();
    if(beginIndex!=null && endIndex!=null)
    {
        var url = 'rebuildWithIndex-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily+"&beginIndex="+beginIndex+"&endIndex="+endIndex;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                   CARBON.closeWindow();
                               }, function () {
                                   CARBON.closeWindow();
                               });
                               jQuery('#'+columnFamily+'beginIndex').val("");
                               jQuery('#'+columnFamily+'endIndex').val("");
                               jQuery('#'+columnFamily+'rebuildWithIndexForm').hide("slow");
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                               jQuery('#'+columnFamily+'beginIndex').val("");
                               jQuery('#'+columnFamily+'endIndex').val("");
                               return false;
                           }
                       }
                   }, "json");
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    }
}

function setCompactionThresholds(hostAddress,keyspace,columnFamily)
{
    var minT=jQuery('#'+columnFamily+'minThreshold').val();
    var maxT=jQuery('#'+columnFamily+'maxThreshold').val();
    if(minT!=null && maxT!=null)
    {
        var url = 'setCompactionThreshold-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily+"&minT="+minT+"&maxT="+maxT;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                       else
                       {
                           if(data.success=="yes")
                           {
                               CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                               jQuery('#'+columnFamily+'minThreshold').val("");
                               jQuery('#'+columnFamily+'maxThreshold').val("");
                               jQuery('#'+columnFamily+'setCompactionThresholdForm').hide("slow");
                               return true;
                           }
                           else
                           {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                               jQuery('#'+columnFamily+'minThreshold').val("");
                               jQuery('#'+columnFamily+'maxThreshold').val("");
                               return false;
                           }
                       }
                   }, "json");
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    }
}
function forceRemoveToken(hostAddress)
{
    var url = 'forcrRemoveToken-ajaxprocessor.jsp?hostAddress='+hostAddress;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"]);
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}

function showTokenRemovalForm()
{
    jQuery('#selectTokenForm').slideToggle();
}

function removeSelectedToken(hostAddress)
{
    var token=jQuery('#availableTokens').val();
    var url = 'removeToken-ajaxprocessor.jsp?hostAddress='+hostAddress+"&token="+token;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           jQuery("#selectTokenForm").hide('slow');
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                               CARBON.closeWindow();
                           }, function () {
                               CARBON.closeWindow();
                           });
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}
function showVersion(hostAddress)
{
    var url = 'showVersion-ajaxprocessor.jsp?hostAddress='+hostAddress;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.version"]+':'+data.versionC);
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}
function showGossipInfo(hostAddress,hostName)
{
    location.href= 'showGossipInfo.jsp?hostAddress='+hostAddress+'&hostName='+hostName;
}

function rangeKeySample(hostAddress,hostName)
{
    location.href= 'rangekey.jsp?hostAddress='+hostAddress+'&hostName='+hostName;
}
function stowNetstat(hostAddress,hostName)
{
    var selectedHost=jQuery('#availableHosts').find(":selected").text();
    jQuery('#netstatForm').hide('slow');
    location.href='netstat.jsp?hostAddress='+hostAddress+'&selectedHost='+selectedHost+'&hostName='+hostName;

}
function showNetstatForm()
{
    jQuery('#netstatForm').slideToggle();
}

function showCompactionThreshoulds(hostAddress,keyspace,columnFamily)
{
    var url = 'showCompactionThresholds-ajaxprocessor.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.minCompactionThreshold"]+':'+data.min+'<br>'+cassandrajsi18n["cassandra.cluster.node.maxCompactionThreshold"]+':'+data.max);
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
    return true;
}

function showEndpointsForm()
{
    jQuery('#sstablesKey').val("");
    jQuery('#showSSTables').hide('slow');
    jQuery('#showEndpoints').slideToggle();
}
function showSSTablesForm()
{
    jQuery('#endpointKey').val("");
    jQuery('#showEndpoints').hide('slow');
    jQuery('#showSSTables').slideToggle();
}
function showEndpoint(hostAddress,keyspace,columnFamily)
{
    var key=jQuery('#endpointKey').val();
    if(key!="")
    {
        jQuery('#showEndpoints').hide('slow');
        location.href='showEnpoints.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily+'&key='+key;
        return true;
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
}
function showSSTables(hostAddress,keyspace,columnFamily)
{
    var key=jQuery('#sstablesKey').val();
    if(key!="")
    {
        jQuery('#showSSTables').hide('slow');
        location.href='showSSTables.jsp?hostAddress='+hostAddress+'&keyspace='+keyspace+'&columnFamily='+columnFamily+'&key='+key;
        return true;
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.tag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
}

function takeCFNodeSnapShot(hostAddress,keyspace,columnFamily)
{
    var snapshotTag=jQuery('#'+columnFamily+'CFSnapshotTag').val();
    if(snapshotTag!="")
    {
        CARBON.showConfirmationDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.warning"], function() {
            var url = 'takeCFSnapshot-ajaxprocessor.jsp?hostAddress='+hostAddress+'&snapshotTag='+snapshotTag+"&keyspace="+keyspace+"&columnFamily="+columnFamily;
            jQuery.get(url, ({}),
                       function(data, status) {
                           if (status != "success") {
                               CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                               jQuery('#'+columnFamily+'CFSnapshotTag').val("");
                               jQuery('#'+columnFamily+'CFSnapshotForm').hide('slow');
                               return false;
                           }
                           else
                           {
                               if(data.success=="yes")
                               {
                                   CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                                       CARBON.closeWindow();
                                   }, function () {
                                       CARBON.closeWindow();
                                   });
                                   jQuery('#'+columnFamily+'CFSnapshotTag').val("");
                                   jQuery('#'+columnFamily+'CFSnapshotForm').hide('slow');
                                   return true;
                               }
                               else
                               {
                                   jQuery('#'+columnFamily+'CFSnapshotTag').val("");
                                   CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.snapshot.node.error"]);
                                   return false;
                               }
                           }
                       }, "json");
        });
    }
    else
    {
        CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.move.node.snapshotTag.empty"], function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
        return false;
    }
    return true;
}

function rebuild(hostAddress)
{
    var datacenter=jQuery('#availableDataCenters').find(":selected").text();
    var url = 'rebuild-ajaxprocessor.jsp?hostAddress='+hostAddress+"&datacenter="+datacenter;
    jQuery.get(url, ({}),
               function(data, status) {
                   if (status != "success") {
                       CARBON.shoErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                       return false;
                   }
                   else
                   {
                       if(data.success=="yes")
                       {
                           jQuery('#rebuildForm').hide('slow');
                           CARBON.showInfoDialog(cassandrajsi18n["cassandra.cluster.node.success"], function () {
                               CARBON.closeWindow();
                           }, function () {
                               CARBON.closeWindow();
                           });
                           return true;
                       }
                       else
                       {
                           CARBON.showErrorDialog(cassandrajsi18n["cassandra.cluster.node.fail"]);
                           return false;
                       }
                   }
               }, "json");
}
