package org.wso2.carbon.mapred.mgt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.JobReporterRegistry;
import org.apache.hadoop.mapreduce.JobReporter;

import org.wso2.carbon.mapred.mgt.api.CarbonMapRedJob;
import org.wso2.carbon.mapred.reporting.CarbonJobReporter;
import org.wso2.carbon.hadoop.security.HadoopCarbonSecurity;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;

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
			log.error("Error opening job jar: " + jarName);
			io.printStackTrace();
			return;
		} catch (ClassNotFoundException noClass) {
			log.error("Cannot find the class"+ className +" in "+jarName);
			noClass.printStackTrace();
			return;
		} catch (IllegalAccessException illegalAccess) {
			log.error("Unable to access main method in "+className+" in "+jarName);
			illegalAccess.printStackTrace();
			return;
		} catch (IllegalArgumentException illegalArg) {
			illegalArg.printStackTrace();
			illegalArg.getCause().getMessage();
		} catch (InstantiationException instantiation) {
			instantiation.printStackTrace();
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
