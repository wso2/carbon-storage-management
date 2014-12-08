/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.rssmanager.core.util.databasemanagement;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SSHConnection {
    private static final Log log = LogFactory.getLog(SSHConnection.class);
    private String host;
    private int port;
    private String username;
    private String privateKeyPath;
    private String passPhrase;

    public SSHConnection(String host, int port, String username, String privateKeyPath, String passPhrase) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.privateKeyPath = privateKeyPath;
        this.passPhrase = passPhrase;
    }

    public String executeCommand(String command) throws RSSManagerException {
        return this.executeCommand(command, null);
    }

    public String executeCommand(String command, String password) throws RSSManagerException {
        String COMMAND_EXEC = "exec";
        StringBuilder commandOutput;
        StringBuilder errorOutput;
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath, passPhrase);
            Session session = jsch.getSession(username, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel(COMMAND_EXEC);
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(null);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            OutputStream out = channel.getOutputStream();
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(((ChannelExec) channel).getErrStream()));
            channel.connect();
            if (password != null) {
                out.write((password + "\n").getBytes());
                out.flush();
            }
            String incomingLine;
            commandOutput = new StringBuilder();
            while ((incomingLine = inputReader.readLine()) != null) {
                commandOutput.append(incomingLine);
                commandOutput.append('\n');
            }
            errorOutput = new StringBuilder();
            while ((incomingLine = errorReader.readLine()) != null) {
                errorOutput.append(incomingLine);
                errorOutput.append('\n');
            }
            inputReader.close();
            errorReader.close();
            if (out != null) {
                out.close();
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception ex) {
            String message = "Error occurred while executing ssh command.";
            log.error(message, ex);
            throw new RSSManagerException(message, ex);
        }
        if (!errorOutput.toString().isEmpty()) {
            String error = errorOutput.toString().substring(0, errorOutput.toString().length() - 1);
            String message = "Error occurred while executing ssh command. " + error;
            throw new RSSManagerException(message);
        }
        String output = commandOutput.toString();
        if (!output.isEmpty()) {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }
}
