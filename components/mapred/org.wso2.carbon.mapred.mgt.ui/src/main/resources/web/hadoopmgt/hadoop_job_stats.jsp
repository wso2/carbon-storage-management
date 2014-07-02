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

<%
   HadoopJobRunnerProxy proxy = new HadoopJobRunnerProxy(request);
   final String JOBNAME = "jobName";
   final String JOBID = "jobId";
   final String PROGRESS = "progress";
   final String JOBCOMPLETE = "jobIsComplete";
   final String JOBSUCCESSFUL = "jobIsSuccessful";
   
   String serviceName = request.getParameter("serviceName");
   String key = (String)session.getAttribute("serviceKey");
   if (serviceName.equalsIgnoreCase(JOBNAME))
	   out.println(JOBNAME+":"+proxy.getJobName(key));
%>