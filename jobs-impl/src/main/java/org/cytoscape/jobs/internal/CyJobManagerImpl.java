package org.cytoscape.jobs.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

/*
 * #%L
 * Cytoscape Jobs Impl (jobs-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * An implementation of CyJobManager.
 */
public class CyJobManagerImpl implements CyJobManager, SessionAboutToBeSavedListener, SessionLoadedListener {
	
	private CyJobMonitor jobMonitor;
	final Logger logger;

	List<CyJob> jobsList;
	ConcurrentMap<CyJob, CyJobMonitor> jobMonitorMap;
	ConcurrentMap<CyJob, IntervalCounter> intervalMap;
	ConcurrentMap<CyJob, CyJobStatus> statusMap;
	ConcurrentMap<String, CyJobMonitor> monitorMap;
	ConcurrentMap<String, CyJobExecutionService> exServiceMap;
	Timer pollTimer;

	public CyJobManagerImpl() {
		logger = Logger.getLogger(CyUserLog.NAME);

		jobsList = new ArrayList<>();
		jobMonitorMap = new ConcurrentHashMap<>();
		intervalMap = new ConcurrentHashMap<>();
		statusMap = new ConcurrentHashMap<>();
		monitorMap = new ConcurrentHashMap<>();
		exServiceMap = new ConcurrentHashMap<>();

		pollTimer = new Timer("Job Status Poller", true);
	}

	public void setJobMonitor(CyJobMonitor monitor) {
		jobMonitor = monitor;
	}

	@Override
	public void addJob(CyJob job, CyJobMonitor jobMonitor, int pollInterval) {
		if (jobsList.size() == 0)
			pollTimer.schedule(new Poller(), 1000);
		synchronized (jobsList) {
			jobsList.add(job);
		}
		associateMonitor(job, jobMonitor, pollInterval);
	}

	@Override
	public void removeJob(CyJob job) {
		synchronized (jobsList) {
			jobsList.remove(job);
		}
		jobMonitorMap.remove(job);
		intervalMap.remove(job);
		if (jobsList.size() == 0) {
			resetTimer();
		}
	}

	@Override
	public void associateMonitor(CyJob job, CyJobMonitor jobMonitor, int pollInterval) {
		if (jobsList.contains(job)) {
			if (jobMonitor == null) {
				jobMonitor = job.getJobMonitor();
			}

			if (jobMonitor != null) {
				jobMonitorMap.put(job, jobMonitor);
				if (pollInterval <= 0)
					pollInterval = job.getPollInterval();

				intervalMap.put(job, new IntervalCounter(pollInterval));
			}
		}
	}

	@Override
	public void associateMonitor(CyJob job, String jobMonitorName, int pollInterval) {
		if (monitorMap.containsKey(jobMonitorName)) {
			associateMonitor(job, monitorMap.get(jobMonitorName), pollInterval);
		}
	}

	@Override
	public CyJobStatus cancelJob(CyJob job) {
		removeJob(job);
		return job.getJobExecutionService().cancelJob(job);
	}

	@Override
	public List<CyJob> getJobs() {
		return jobsList;
	}

	public CyJob getJob(String jobId) {
		for (CyJob job: jobsList) {
			if (job.getJobId().equals(jobId))
				return job;
		}
		return null;
	}

	/*
	 * OSGi interfaces to track the list of CyJobMonitors and CyJobSessionHandlers
	 */
	public void addJobMonitor(CyJobMonitor monitor, Map<?, ?> properties) {
		monitorMap.put(monitor.getClass().getCanonicalName(), monitor);
	}

	public void removeJobMonitor(CyJobMonitor monitor, Map<?, ?> properties) {
		String clazz = monitor.getClass().getCanonicalName();
		if (monitorMap.containsKey(clazz))
			monitorMap.remove(clazz);
	}

	public void addExecutionService(CyJobExecutionService exService, Map<?, ?> properties) {
		exServiceMap.put(exService.getServiceName(), exService);
	}

	public void removeExecutionService(CyJobExecutionService exService, Map<?, ?> properties) {
		String clazz = exService.getServiceName();
		if (exServiceMap.containsKey(clazz))
			exServiceMap.remove(clazz);
	}

	public void resetTimer() {
		pollTimer.cancel();
		pollTimer = new Timer("Job Status Poller", true);
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		// Cancel our timer
		resetTimer();

		String tmpDir = System.getProperty("java.io.tmpdir");
		List<File> jobFiles = new ArrayList<>();

		// Go through all of our jobs, create a file for each job and let
		// the session handler write it
		for (CyJob job: jobsList) {
			CyJobExecutionService exService = job.getJobExecutionService();

			File jobFile;
			try {
				jobFile = new File(tmpDir, exService.getClass().getCanonicalName()+"_CyJob_"+job.getJobId());
				exService.saveJobInSession(job, jobFile);
			} catch (Exception ioe) {
				logger.error("Failed to save job "+job.getJobId()+" in session: "+ioe.getMessage());
				continue;
			}
			jobFiles.add(jobFile);
		}
		if (jobFiles.size() > 0) {
			try {
				e.addAppFiles("CyJobs", jobFiles);
			} catch (Exception ioe) {
				logger.error("Failed to save jobs in session: "+ioe.getMessage());
			}
		}

		// Restart our timer
		if (jobsList.size() > 0)
			pollTimer.schedule(new Poller(), 1000);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Cancel our timer
		resetTimer();

		CySession session = e.getLoadedSession();
		Map<String, List<File>> appFileList = session.getAppFileListMap();
		if (appFileList.containsKey("CyJobs")) {
			List<File> jobFiles = appFileList.get("CyJobs");
			for (File jobFile: jobFiles) {
				String name = jobFile.getName();
				// First, separate the sessionHandler part from the job ID
				String parts[] = name.split("_CyJob_");
				if (exServiceMap.containsKey(parts[0])) {
					CyJob newJob = exServiceMap.get(parts[0]).restoreJobFromSession(session, jobFile);
					// If we already have this job in our running jobs list, don't add it again
					if (getJob(newJob.getJobId()) == null) {
						addJob(newJob, null, -1);
					}
				}
			}
		}
	}

	class Poller extends TimerTask {
		public void run() {
			resetTimer();
			List<CyJob> orphans = new ArrayList<>();
			for (CyJob job: intervalMap.keySet()) {
				if (intervalMap.get(job).ready()) {
					CyJobStatus status = job.getJobExecutionService().checkJobStatus(job);
					if (statusMap.containsKey(job) && statusMap.get(job).equals(status))
						continue;

					if (jobMonitorMap.containsKey(job))
						jobMonitorMap.get(job).jobStatusChanged(job, status);

					if (status.isDone())
						// Orphan
						orphans.add(job);

					statusMap.put(job, status);
					// The jobMonitor should call loadResults
					jobMonitor.jobStatusChanged(job, status);
				}
			}
			for (CyJob job: orphans) removeJob(job);
			if (jobsList.size() > 0)
				pollTimer.schedule(new Poller(), 1000);
		}
	}

	class IntervalCounter {
		int currentInterval = 0;
		final int pollInterval;

		public IntervalCounter(final int pollInterval) {
			this.pollInterval = pollInterval;
		}

		public boolean ready() {
			currentInterval++;
			if (currentInterval >= pollInterval) {
				currentInterval = 0;
				return true;
			}
			return false;
		}
	}

}
