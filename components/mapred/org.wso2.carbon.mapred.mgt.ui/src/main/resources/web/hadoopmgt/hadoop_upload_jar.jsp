<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="org.apache.commons.fileupload.util.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="java.util.* " %>
<%@ page import="java.io.* " %>
<%@ page import="org.wso2.carbon.mapred.mgt.ui.*" %>
<%@ page import="org.wso2.carbon.context.* " %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:setLocale value='si'  scope='session'/>
<%
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>
//Upload dialogue
<fmt:bundle basename="org.wso2.carbon.mapred.mgt.ui.i18n.Resources">
<form method="post"
      name="updateUploadForm"
      id="updateUploadForm"
      action="<%request.getRequestURI();%>"
      enctype="multipart/form-data" target="_self">

    <table class="normal">
        <tr>
            <td style="padding-top:5px"><fmt:message key="file"/> <span class="required">*</span></td>
            <td><input id="uResourceFile" type="file" name="upload"
                       style="background-color:#cccccc"/>
            </td>
            <td valign="middle" style="padding-left:5px;padding-top:2px">
                <input type='button' class='button' id ="uploadContentButtonID" onclick="document.updateUploadForm.submit()"
                       value='<fmt:message key="upload"/>'/>
            </td>
            <td valign="middle" style="padding-left:5px;padding-top:2px">
                <input type='button' class='button' id ="cancelUploadContentButtonID" onclick='cancelTextContentEdit()'
                       value='<fmt:message key="cancel"/>'/>
            </td>
        </tr>
    </table>
</form>
</fmt:bundle>

<%
	Enumeration headerNames = request.getHeaderNames();
	while (headerNames.hasMoreElements()) {
		String headerName = (String)headerNames.nextElement();
		String headerValue = request.getHeader(headerName);
		//System.out.println(headerName+": "+headerValue);
	}
	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	if (isMultipart) {
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload();
		// Parse the request
		FileItemIterator iter = upload.getItemIterator(request);
		while (iter.hasNext()) {
    		FileItemStream item = iter.next();
    		String name = item.getFieldName();
    		InputStream stream = item.openStream();
    		if (item.isFormField()) {
        		System.out.println("Form field " + name + " with value "
            		+ Streams.asString(stream) + " detected.");
    		} else {
        		System.out.println("File field " + name + " with file name "
            		+ item.getName() + " detected.");
        		// Process the input stream
        		HadoopJobRunnerProxy proxy = new HadoopJobRunnerProxy(request);
        		proxy.uploadJar(item.getName(), stream);
        		try {
        			stream.close();
        		}
        		catch (IOException ioe) {
        			ioe.printStackTrace();
        		}
    		}
		}
	}
%>
        