

function deleteFile(filePath) {
    var opCount = document.getElementById("keysp");
    opCount.value = parseInt(opCount.value) - 1;
    CARBON.showConfirmationDialog(hdfsjsi18n["hdfs.file.delete.confirmation"], function() {
        var ksName = document.getElementById("keyspaceName" + index).value;
        var url = 'ks-delete-ajaxprocessor.jsp?name=' + ksName;
        jQuery.get(url, ({}),
                   function(data, status) {
                       if (status != "success") {
                           CARBON.showWarningDialog(cassandrajsi18n['cassandra.delete.ks.error.occurred']);
                           return false;
                       } else {
                           deleteRaw('keyspace', index);
                       }
                   });
    });

    return false;
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