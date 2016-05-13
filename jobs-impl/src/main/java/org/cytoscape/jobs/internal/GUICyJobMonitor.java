package org.cytoscape.jobs.internal;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import org.apache.log4j.Logger;

/**
 * An implementation of CyJobManager.
 */
public class GUICyJobMonitor extends AbstractTaskFactory implements CyJobMonitor {
	final Logger logger;
	final CyServiceRegistrar serviceRegistrar;
	final ConcurrentMap<CyJob, CyJobStatus> statusMap;
	final GUIJobDialog dialog;
	final JobStatusBar statusBar;
	final CyJobManagerImpl jobManager;

	public GUICyJobMonitor(CyServiceRegistrar registrar, CyJobManagerImpl jobManager, JobStatusBar statusBar) {
		this.serviceRegistrar = registrar;
		this.jobManager = jobManager;
		this.statusBar = statusBar;
		jobManager.setJobMonitor(this);
		logger = Logger.getLogger(CyUserLog.NAME);
		statusMap = new ConcurrentHashMap<>();
		CySwingApplication swingApp = registrar.getService(CySwingApplication.class);
		dialog = new GUIJobDialog(serviceRegistrar, swingApp, statusMap, jobManager, this);
		statusBar.setDialog(dialog);
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AbstractTask() {
			public void run(TaskMonitor monitor) {
				dialog.setVisible(true);
			}
		});
	}

	public void jobStatusChanged(CyJob job, CyJobStatus status) {
		String jobId = job.getJobId();
		Status stat = status.getStatus();
		if (statusMap.containsKey(job) && statusMap.get(job).getStatus().equals(stat))
			return;

		statusMap.put(job, status);
		dialog.mapChanged();
		statusBar.updateIcon(statusMap.values());

		// Temporary - for debugging purposes
		switch(stat) {
			case FAILED:
				logger.error("JobManager: Job "+jobId+" has failed!");
				break;
			case ERROR:
				logger.error("JobManager: Job "+jobId+" has experienced an error!");
				break;

			case CANCELED:
				logger.warn("JobManager: Job "+jobId+" has been canceled!");
				break;
			case PURGED:
				logger.warn("JobManager: Job "+jobId+" has been purged!");
				break;
			case TERMINATED:
				logger.warn("JobManager: Job "+jobId+" was terminated");
				break;

			case FINISHED:
				logger.info("JobManager: Job "+jobId+" has finished");
				break;
			case SUBMITTED:
				logger.info("JobManager: Job "+jobId+" was submitted");
				break;
			case QUEUED:
				logger.info("JobManager: Job "+jobId+" has been queued");
				break;
			case RUNNING:
				logger.info("JobManager: Job "+jobId+" is running");
				break;
		}
	}

	public void updateIcon() {
		statusBar.updateIcon(statusMap.values());
	}

	// This isn't used by us.
	public void loadData(CyJob job, TaskMonitor monitor) {}

}
