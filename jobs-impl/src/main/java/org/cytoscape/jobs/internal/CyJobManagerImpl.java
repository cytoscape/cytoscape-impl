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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobHandler;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * An implementation of CyJobManager.
 */
public class CyJobManagerImpl implements CyJobManager {
	private final CyServiceRegistrar cyServiceRegistrar;
	private CyEventHelper cyEventHelper;
	CyJobHandler jobMonitor;

	List<CyJob> jobsList;
	Map<CyJob, CyJobHandler> handlerMap;
	Map<CyJob, IntervalCounter> intervalMap;
	Map<CyJob, CyJobStatus> statusMap;
	final Timer pollTimer;

	/**
	 * 
	 * @param cyServiceRegistrar
	 * @param cyEventHelper
	 */
	public CyJobManagerImpl(final CyServiceRegistrar cyServiceRegistrar, 
		                      final CyEventHelper cyEventHelper, 
													final CyJobHandler jobMonitor) {
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cyEventHelper = cyEventHelper;
		this.jobMonitor = jobMonitor;

		jobsList = new ArrayList<>();
		handlerMap = new HashMap<>();
		intervalMap = new HashMap<>();
		statusMap = new HashMap<>();

		pollTimer = new Timer("Job Status Poller", true);
	}

	@Override
	public void addJob(CyJob job, CyJobHandler jobHandler, int pollInterval) {
		if (jobsList.size() == 0)
			pollTimer.schedule(new Poller(), 1000);
		jobsList.add(job);
		associateHandler(job, jobHandler, pollInterval);
	}

	// Add to API
	public void removeJob(CyJob job) {
		jobsList.remove(job);
		handlerMap.remove(job);
		intervalMap.remove(job);
		if (jobsList.size() == 0)
			pollTimer.cancel();
	}

	@Override
	public void associateHandler(CyJob job, CyJobHandler jobHandler, int pollInterval) {
		if (jobsList.contains(job)) {
			if (jobHandler == null) {
				String handler = job.getJobHandler();
				// Find from OSGi?
			}

			if (jobHandler != null) {
				handlerMap.put(job, jobHandler);
				if (pollInterval <= 0)
					pollInterval = job.pollInterval();

				intervalMap.put(job, new IntervalCounter(pollInterval));
			}
		}
	}

	@Override
	public CyJobStatus cancelJob(CyJob job) {
		removeJob(job);
		return job.cancelJob();
	}

	@Override
	public List<CyJob> getJobs() {
		return jobsList;
	}

	class Poller extends TimerTask {
		public void run() {
			List<CyJob> orphans = new ArrayList<>();
			for (CyJob job: intervalMap.keySet()) {
				if (intervalMap.get(job).ready()) {
					CyJobStatus status = job.getJobStatus();
					if (statusMap.containsKey(job) && statusMap.get(job).equals(status))
						continue;

					if (handlerMap.containsKey(job))
						handlerMap.get(job).handleJob(job, status);
					else if (CyJobStatus.isDone(status))
						// Orphan
						orphans.add(job);

					statusMap.put(job, status);
					jobMonitor.handleJob(job, status);
				}
			}
			for (CyJob job: orphans) removeJob(job);
			if (jobsList.size() > 0)
				pollTimer.schedule(this, 1000);
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
