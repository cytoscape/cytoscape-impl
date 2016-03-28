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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.work.TaskMonitor;

import org.apache.log4j.Logger;

/**
 * An implementation of CyJobManager.
 */
public class SimpleCyJobMonitor implements CyJobMonitor {
	final Logger logger;
	public SimpleCyJobMonitor() {
		logger = Logger.getLogger(CyUserLog.NAME);
	}

	@Override
	public void jobStatusChanged(CyJob job, CyJobStatus status) {
		String jobId = job.getJobId();
		Status stat = status.getStatus();
		switch(stat) {
			case FAILED:
				logger.error("Job "+jobId+" has failed!");
				break;
			case ERROR:
				logger.error("Job "+jobId+" has experienced an error!");
				break;

			case CANCELED:
				logger.warn("Job "+jobId+" has been canceled!");
				break;
			case PURGED:
				logger.warn("Job "+jobId+" has been purged!");
				break;
			case TERMINATED:
				logger.warn("Job "+jobId+" was terminated");
				break;

			case FINISHED:
				logger.info("Job "+jobId+" has finished");
				// The final version of this will call loadResults
				break;
			case SUBMITTED:
				logger.info("Job "+jobId+" was submitted");
				break;
			case QUEUED:
				logger.info("Job "+jobId+" has been queued");
				break;
			case RUNNING:
				logger.info("Job "+jobId+" is running");
				break;
		}
	}

	public void loadData(CyJob job, TaskMonitor monitor) {}
}
