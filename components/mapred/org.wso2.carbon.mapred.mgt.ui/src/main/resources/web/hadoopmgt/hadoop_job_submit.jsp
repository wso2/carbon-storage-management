<%@ page import="org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub" %>
<%@ page import="org.wso2.carbon.mapred.mgt.ui.*" %>
<%@ page import="org.apache.axis2.*" %>
<%@ page import="org.apache.axis2.context.MessageContext" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
	// Set standard HTTP/1.1 no-cache headers.
	response.setHeader("Cache-Control",
			"no-store, max-age=0, no-cache, must-revalidate");
	// Set IE extended HTTP/1.1 no-cache headers.
	response.addHeader("Cache-Control", "post-check=0, pre-check=0");
	// Set standard HTTP/1.0 no-cache header.
	response.setHeader("Pragma", "no-cache");
%>
<script type="text/javascript" src="js/hadoopmgt.js"></script>
<div id="middle">
    <h2>New Hadoop Job</h2>

    <div id="workArea">
        <h3 id="whatStep">Step 1 : Hadoop Jar</h3>

        <form method="post" id="hadoopForm">
            <table width="60%" id="userAdd" class="styledLeft">
                <thead>
                    <tr>
                        <th></th>
                    </tr>
                </thead>
                <tbody><tr>
                    <td class="formRaw">
                        <table class="normal" id="wizardTable">
                            <tbody>
                            <tr id="step1">
                                <td>Hadoop Jar:<font color="red">*</font>
                                </td>
                                <td><input type="text" name="hadoopJarPath" id="hadoopJarPath"/></td>
                            </tr>
                            <tr id="step2" style="display:none">
                                <td>Class Name:<font color="red">*</font></td>
                                <td><input type="text" name="hadoopClassName" id="hadoopClassName"/></td>
                            </tr>
                            <tr id="step3" style="display:none">
                                <td>Input File:<font color="red">*</font></td>
                                <td><input type="text" name="hadoopInFile" id="hadoopInFile"/></td>
                            </tr>
                            <tr id="step4" style="display:none">
                                <td>Output File:<font color="red">*</font></td>
                                <td><input type="text" name="hadoopOutFile" id="hadoopOutFile"/></td>
                            </tr>
                        </tbody></table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" onclick="doPre();" value="&lt; Pre" disabled="" class="button" id="preButton" />
                        <input type="button" onclick="doNext();" value="Next &gt;" class="button" id="nextButton" />
                        <input type="button" class="button" disabled="" value="Finish" id="finishButton" />
                        <input type="button" onclick="doCancel();" value="Cancel" class="button">
                    </td>
                </tr>
            </tbody></table>
        </form>
    </div>
    <p>&nbsp;</p>
</div>




<%
	String jarPath = request.getParameter("hadoopJarPath");
	String className = request.getParameter("hadoopClassName");
	String inFile = request.getParameter("hadoopInFile");
	String outFile = request.getParameter("hadoopOutFile");
	Cookie[] cookies = request.getCookies();
	String sessionID = null;
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
	String key = null;
	if (jarPath != null) {
		HadoopJobRunnerProxy proxy = new HadoopJobRunnerProxy(request);
		key = proxy.submitJob(jarPath, className, inFile + " "
				+ outFile);
		String currentStatus = null;
		while ((currentStatus = proxy.getJobStatus(key)) != null) {
			out.print("<br>" + currentStatus + "</br>");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
%>
