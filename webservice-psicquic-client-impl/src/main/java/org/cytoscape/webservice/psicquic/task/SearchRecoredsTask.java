package org.cytoscape.webservice.psicquic.task;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.Collection;
import java.util.Map;

import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchRecoredsTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(SearchRecoredsTask.class);
	
	private final PSICQUICRestClient client;
	private final SearchMode mode;

	private String query;

	private Collection<String> targetServices;

	private Map<String, Long> result;
	
	public SearchRecoredsTask(final PSICQUICRestClient client, final SearchMode mode) {
		this.client = client;
		this.mode = mode;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Searching PSICQUIC Services");
		taskMonitor.setStatusMessage("Searching databases.  Please wait...");
		taskMonitor.setProgress(0.05d);
		if (query == null)
			throw new NullPointerException("Query is null");
		if (targetServices == null)
			throw new NullPointerException("Target service set is null");

		result = client.search(query, targetServices, mode, taskMonitor);
		taskMonitor.setProgress(1.0d);
	}
	
	@Override
	public void cancel() {
		client.cancel();
	}
	
	public void setTargets(final Collection<String> targetServices) {
		this.targetServices = targetServices;
	}

	public void setQuery(final String query) {
		this.query = query;
	}
	
	public Map<String, Long> getResult() {
		return result;
	}

}
