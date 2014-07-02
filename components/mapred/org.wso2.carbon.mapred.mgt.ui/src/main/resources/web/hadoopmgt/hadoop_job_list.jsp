<%@ page import="org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub" %>
<%@ page import="org.wso2.carbon.mapred.mgt.ui.*" %>
<%@ page import="org.apache.axis2.*" %>
<%@ page import="org.apache.axis2.context.MessageContext" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>
<h2>List Hadoop Jobs</h2>

<%
HadoopJobRunnerProxy proxy = new HadoopJobRunnerProxy(request);
String[] jobList = proxy.getFinalReportsList(0);
for (int i=0; i<jobList.length; i++) {
	out.println("<br>"+proxy.getJobFinalReport(jobList[i])+"</br>");
}
%>
