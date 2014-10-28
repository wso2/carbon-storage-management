/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mapred.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.mapreduce.JobReporterRegistry;
import org.wso2.carbon.hadoop.security.HadoopCarbonSecurity;
import org.wso2.carbon.mapred.mgt.api.CarbonMapRedJob;
import org.wso2.carbon.mapred.reporting.CarbonJobReporter;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HadoopJobRunnerThread extends Thread {

	private String args;
	private String jarName;
	private String className;
	private Log log = LogFactory.getLog(HadoopJobRunner.class);
	private CarbonJobReporter carbonJobReporter;
    
	public HadoopJobRunnerThread(String jarName, String className, String args) {
		this.jarName = jarName;
		this.args = args;
		this.className = className;
		this.carbonJobReporter = null;
	}
	
	@Override
	public void run() {
		JarFile jarFile = null;
		String mainClassName = null;
		File file = new File(jarName);
		Properties hadoopConf = new Properties();
		try {
			hadoopConf.load(new FileInputStream(HadoopJobRunner.HADOOP_CONFIG));
			jarFile = new JarFile(HadoopJobRunner.DEFAULT_HADOOP_JAR_PATH+"/"+jarName);
			mainClassName = className;
			mainClassName = mainClassName.replaceAll("/", ".");
			File tmpDir = new File(hadoopConf.getProperty("taskcontroller.job.dir"));
			tmpDir.mkdirs();
			if (!tmpDir.isDirectory()) {
				System.err.println("Mkdirs failed to create " + tmpDir);
				return;
			}
			final File workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
			workDir.delete();
			workDir.mkdirs();
			if (!workDir.isDirectory()) {
				log.error("Mkdirs failed to create " + workDir);
				return;
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						FileUtil.fullyDelete(workDir);
					} catch (IOException e) {
						log.error("Error while deleting file "+e);
					}
				}
			});
			unJar(file, workDir);

			ArrayList<URL> classPath = new ArrayList<URL>();
			classPath.add(new File(workDir+"/").toURL());
			classPath.add(file.toURL());
			classPath.add(new File(workDir, "classes/").toURL());
			File[] libs = new File(workDir, "lib").listFiles();
			if (libs != null) {
				for (int i = 0; i < libs.length; i++) {
					classPath.add(libs[i].toURL());
				}
			}
			String[] classPathsVars = System.getProperty("java.class.path").split(":");
			for (int i=0; i<classPathsVars.length; i++) {
				classPath.add(new File(classPathsVars[i]).toURL());
			}
			ClassLoader loader =
				new URLClassLoader(classPath.toArray(new URL[classPath.size()]), getClass().getClassLoader());
			Thread.currentThread().setContextClassLoader(loader);
			Class<?> mainClass = Class.forName(mainClassName, true, loader);
			
			CarbonMapRedJob carbonMapRedJob = (CarbonMapRedJob)mainClass.newInstance();
			Configuration conf = new Configuration(HadoopJobRunner.getConf());
			//Allways sanitize Configuration object before passing to client.
			HadoopJobRunner.sanitizeConfiguration(conf);
			carbonMapRedJob.setConfiguration(conf);
			String[] newArgs = args.split(" ");
			this.carbonJobReporter = new CarbonJobReporter();
			JobReporterRegistry.setReporter(carbonJobReporter);
			synchronized (this) {
				this.notify();
			}
			carbonMapRedJob.run(newArgs);
			HadoopCarbonSecurity.clean();
		} catch (IOException io) {
			log.error("Error opening job jar: " + jarName, io);
		} catch (ClassNotFoundException noClass) {
			log.error("Cannot find the class"+ className +" in "+jarName, noClass);
		} catch (IllegalAccessException illegalAccess) {
			log.error("Unable to access main method in "+className+" in "+jarName, illegalAccess);
		} catch (IllegalArgumentException illegalArg) {
			log.error(illegalArg.getMessage(), instantiation);
		} catch (InstantiationException instantiation) {
			log.error(instantiation.getMessage(). instantiation);
		}
	}

	private void unJar(File jarFile, File toDir) throws IOException {
		JarFile jar = new JarFile(jarFile);
		try {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = (JarEntry)entries.nextElement();
				if (!entry.isDirectory()) {
					InputStream in = jar.getInputStream(entry);
					try {
						File file = new File(toDir, entry.getName());
						if (!file.getParentFile().mkdirs()) {
							if (!file.getParentFile().isDirectory()) {
								log.error("Mkdirs failed to create "+file.getParentFile().toString());
								throw new IOException("Mkdirs failed to create " +
										file.getParentFile().toString());
							}
						}
						OutputStream out = new FileOutputStream(file);
						try {
							byte[] buffer = new byte[8192];
							int i;
							while ((i = in.read(buffer)) != -1) {
								out.write(buffer, 0, i);
							}
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				}
			}
		} finally {
			jar.close();
		}
	}
	
	public CarbonJobReporter getCarbonJobReporter() {
		return this.carbonJobReporter;
	}
}
