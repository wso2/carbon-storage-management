package org.wso2.carbon.rssmanager.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ProcessBuilderWrapper {
    private static final Log log = LogFactory.getLog(ProcessBuilderWrapper.class);
    private StringWriter errors;

    public ProcessBuilderWrapper() {
        errors = new StringWriter();
    }

    public int execute(List command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        StreamHandler errorSH = new StreamHandler(process.getErrorStream(), new PrintWriter(errors, true));
        errorSH.start();
        int status = process.waitFor();
        errorSH.join();
        return status;
    }

    public String getErrors() {
        return errors.toString();
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