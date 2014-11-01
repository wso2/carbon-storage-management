
function viewExplorer(keyspace, columnFamily) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" +
        columnFamily;
}
function viewRowExplorer(keyspace, columnFamily) {
    location.href = 'row_explorer.jsp?ordinal=1&keyspace=' + keyspace + "&columnFamily=" +
                    columnFamily;
}

function getDataForRow(keyspace, cf, rowID) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID="
        + rowID;
}

function getDataForColumn(keyspace, cf, rowID, columnKey) {
    location.href = 'column_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID="
        + rowID +
        "&columnKey=" + columnKey;
}

function getDataPageForRow(keyspace, cf, rowID) {
    location.href = 'column_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID="
        + rowID;
}

function reloadDataTable(keyspace, cf) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf;
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
