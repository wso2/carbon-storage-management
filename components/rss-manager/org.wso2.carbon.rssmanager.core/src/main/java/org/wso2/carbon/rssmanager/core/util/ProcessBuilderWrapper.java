package org.wso2.carbon.rssmanager.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;

public class ProcessBuilderWrapper {
    private static final Log log = LogFactory.getLog(ProcessBuilderWrapper.class);
    private StringWriter info;
    private StringWriter errors;
    private int status;

    public ProcessBuilderWrapper() {
        info = new StringWriter();
        errors = new StringWriter();
    }

    public int execute(List command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        StreamHandler infoSH = new StreamHandler(process.getInputStream(), new PrintWriter(info, true));
        StreamHandler errorSH = new StreamHandler(process.getErrorStream(), new PrintWriter(errors, true));
        infoSH.start();
        errorSH.start();
        status = process.waitFor();
        infoSH.join();
        errorSH.join();
        return status;
    }

    public String getErrors() {
        return errors.toString();
    }

    public String getInfo() {
        return info.toString();
    }

    public int getStatus() {
        return status;
    }

    class StreamHandler extends Thread {
        private InputStream in;
        private PrintWriter pw;

        StreamHandler(InputStream in, PrintWriter pw) {
            this.in = in;
            this.pw = pw;
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
            } catch (Exception e) {
                log.error("Error occurred while reading from process.", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
    }
}