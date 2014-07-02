var step1 = "Step 1 : Hadoop Jar";
var step2 = "Step 2 : Class Name";
var step3 = "Step 3 : Input File";
var step4 = "Step 4 : Output File";
function doPre() {
    if (jQuery('#step4').is(":visible")) {
        jQuery('#wizardTable tr').hide();
        jQuery('#step3').show();
        jQuery('#whatStep').html(step3);
        jQuery('#nextButton').removeAttr("disabled");
        jQuery('#finishButton').attr("disabled", "disabled");
    } else if (jQuery('#step3').is(":visible")) {
        jQuery('#wizardTable tr').hide();
        jQuery('#step2').show();
        jQuery('#whatStep').html(step2);
    } else if (jQuery('#step2').is(":visible")) {
        jQuery('#wizardTable tr').hide();
        jQuery('#step1').show();
        jQuery('#preButton').attr("disabled", "disabled");
        jQuery('#whatStep').html(step1);
    }
}
function doNext() {
    if (jQuery('#step1').is(":visible")) {
        if (jQuery('#hadoopJarPath').val() == "") {
            CARBON.showErrorDialog("Please give Hadoop Jar Name ");
            return;
        }
        jQuery('#wizardTable tr').hide();
        jQuery('#step2').show();
        jQuery('#whatStep').html(step2);
        jQuery('#preButton').removeAttr("disabled");
    } else if (jQuery('#step2').is(":visible")) {
        if (jQuery('#hadoopClassName').val() == "") {
            CARBON.showErrorDialog("Please give Hadoop Class Name");
            return;
        }
        jQuery('#wizardTable tr').hide();
        jQuery('#step3').show();
        jQuery('#whatStep').html(step3);
    } else if (jQuery('#step3').is(":visible")) {
        if (jQuery('#hadoopInFile').val() == "") {
            CARBON.showErrorDialog("Please give  Input File Path");
            return;
        }
        jQuery('#wizardTable tr').hide();
        jQuery('#step4').show();
        jQuery('#finishButton').removeAttr("disabled");
        jQuery('#nextButton').attr("disabled", "disabled");
        jQuery('#whatStep').html(step4);
    }
}
function doCancel() {
    location.href = '../hadoopmgt/hadoop_job_list.jsp?region=region1&item=hadoop_job_list_menu';
}
jQuery(document).ready(function() {
    jQuery('#finishButton').click(
                                 function() {
                                     if (jQuery('#hadoopOutFile').val() == "") {
                                         CARBON.showErrorDialog("Please give Output File");
                                         return;
                                     }
                                     jQuery('#hadoopForm').submit();
                                     var key = jQuery.session("serviceKey");
                                     jQuery.get("../hadoopmgt/hadoop_job_stats.jsp", {serviceName: "jobName"}, function (data){alert("Job Name: "+data);});
                                 }
            );
    jQuery('#nextButton').removeAttr("disabled");
    jQuery('#finishButton').attr("disabled", "disabled");
    jQuery('#preButton').attr("disabled", "disabled");
});