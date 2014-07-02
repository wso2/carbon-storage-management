package org.wso2.carbon.mapred.reporting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapreduce.JobReporter;
import org.apache.log4j.Logger;
import org.compass.core.executor.workmanager.RunnableCallableAdapter;

public class CarbonJobReporter extends JobReporter {

	private final Logger log = Logger.getLogger(CarbonJobReporter.class);
	private String jobId = null;
	private String jobName = null;
	//private Counters counters = null;
	RunningJob runningJob = null;
	private long lastAccessed = System.currentTimeMillis();

	public void init() {
		runningJob = getRunningJob();
		//try {
			jobId = runningJob.getID().getJtIdentifier();
			jobName = runningJob.getJobName();
			//counters = runningJob.getCounters();
			updateTimestap();
			synchronized (this) {
				this.notify();
			}
		//} catch (IOException e) {
			//log.warn(e.getMessage());
		//}
	}

	public float getMapProgress() {
		float progress = -1;
		try {
			progress = runningJob.mapProgress();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		updateTimestap();
		return progress;
	}

	public float getReduceProgress() {
		float progress = -1;
		try {
			progress = runningJob.reduceProgress();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		updateTimestap();
		return progress;
	}

	public String getJobId() {
		updateTimestap();
		return this.jobId;
	}

	public String getJobName() {
		updateTimestap();
		return this.jobName;
	}

	/*public long getCounter(Enum key) {
		updateTimestap();
		return this.counters.getCounter(key);
	}*/

	public boolean isJobComplete() {
		boolean status = false;
		try {
			status = runningJob.isComplete();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		updateTimestap();
		return status;
	}

	public boolean isJobSuccessful() {
		boolean status = false;
		try {
			status = runningJob.isSuccessful();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		updateTimestap();
		return status;
	}
	
	public String getFailureInfo() {
		String info = null;
		try {
			info = runningJob.getFailureInfo();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		return info;
	}
	
	public String getStatus() {
		String status = null;
		try {
			status = JobStatus.getJobRunState(runningJob.getJobState());
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		return status;
	}
	
	public void updateTimestap() {
		this.lastAccessed = System.currentTimeMillis();
	}
	
	public long getLastAccessedTime() {
		return this.lastAccessed;
	}
	
	private boolean isJobCompleteNoTS() {
		boolean status = false;
		try {
			status = runningJob.isComplete();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		return status;
	}
	
	public Counters getCounters() {
		try {
			return this.runningJob.getCounters();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		return null;
	}

	public static class CarbonJobReporterMap implements Runnable {
		private final Logger log = Logger.getLogger(CarbonJobReporterMap.class);
		private static HashMap<UUID, CarbonJobReporter> reporterMap;
		private final static long MAX_CACHED_TIME_MS = 1000 * 60 * 5;
		static {
			reporterMap = new HashMap<UUID, CarbonJobReporter>();
		}

		public static void putCarbonHadoopJobReporter(UUID uuid,
				CarbonJobReporter reporter) {
			reporterMap.put(uuid, reporter);
		}

		public static CarbonJobReporter getCarbonHadoopJobReporter(UUID uuid) {
			return reporterMap.get(uuid);
		}
		
		public static void removecarbonJobReporter(UUID uuid) {
			reporterMap.remove(uuid);
		}

		@Override
		public void run() {
			log.info("Starting CarbonJobReporter cleaner thread");
			while (true) {
				Iterator<Entry<UUID, CarbonJobReporter>>  iter = reporterMap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<UUID, CarbonJobReporter> pair = (Entry<UUID, CarbonJobReporter>)iter.next();
					CarbonJobReporter reporter = (CarbonJobReporter) pair.getValue();
					UUID uuid = (UUID) pair.getKey();
					if (reporter.isJobCompleteNoTS() && (System.currentTimeMillis() - reporter.getLastAccessedTime() >= MAX_CACHED_TIME_MS)) {
						reporterMap.remove(uuid);
						log.info("Removing Job: "+reporter.getJobId());
					}
					Thread.yield();
				}
				try {
					Thread.sleep(MAX_CACHED_TIME_MS * 2);
				} catch (InterruptedException e) {
					log.warn(e.getMessage());
				}
			}
		}
	}
}
