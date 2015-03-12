package org.cytoscape.io.webservice.biomart.task;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class LoadRepositoryTask extends AbstractTask {

	private static final Logger logger = LoggerFactory
			.getLogger(LoadRepositoryTask.class);

	private final BiomartRestClient client;

	private Map<String, Map<String, String>> reg;
	private final Map<String, String> name2DatasourceMap;

	private LoadRepositoryResult result;

	// These databases are not compatible with this UI.
	private static final List<String> databaseFilter = new ArrayList<String>();

	static {
		// Database on this list will not appear on the list.
		databaseFilter.add("compara_mart_pairwise_ga_47");
		databaseFilter.add("compara_mart_multiple_ga_47");
		databaseFilter.add("dicty");
		databaseFilter.add("Pancreatic_Expression");
	}

	private Map<String, String> datasourceMap;
	private List<String> dsList;
	
	@Tunable(description="Please select services you want to use:")
	public ListMultipleSelection<String> services;

	public LoadRepositoryTask(final BiomartRestClient client) {
		this.client = client;
		this.name2DatasourceMap = new HashMap<String, String>();
		try {
			initServiceList();
		} catch (Exception e) {
			throw new IllegalStateException("Could not obtain registry", e);
		}
	}
	
	private final void initServiceList() throws IOException, ParserConfigurationException, SAXException {
		reg = client.getRegistry();
		
		for (String databaseName : reg.keySet()) {

			final Map<String, String> detail = reg.get(databaseName);
			if (detail.get("visible").equals("1")
					&& (databaseFilter.contains(databaseName) == false)) {
				String dispName = detail.get("displayName");
				name2DatasourceMap.put(dispName, databaseName);
			}
		}
		final TreeSet<String> sortedSet = new TreeSet<String>(name2DatasourceMap.keySet());
		services = new ListMultipleSelection<String>(new ArrayList<String>(sortedSet));
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, ParserConfigurationException, SAXException {

		final List<String> selected = services.getSelectedValues();
		
		taskMonitor.setTitle("Loading list of available BioMart Services.  Please wait...");
		taskMonitor.setStatusMessage("Loading list of available Marts...");
		
		dsList = new ArrayList<String>();
		datasourceMap = new HashMap<String, String>();

		logger.debug("Loading Repository...");
		
		taskMonitor.setProgress(0.1);
		final int registryCount = selected.size();
		float increment = 0.9f / registryCount;
		float percentCompleted = 0.1f;

		taskMonitor.setProgress(percentCompleted);
		Map<String, String> datasources;

		for (String databaseName : reg.keySet()) {

			Map<String, String> detail = reg.get(databaseName);

			// Add the datasource if its visible
			if (detail.get("visible").equals("1")
					&& (databaseFilter.contains(databaseName) == false)) {
				String dispName = detail.get("displayName");
				if (selected.contains(dispName)) {
					try {
						datasources = client.getAvailableDatasets(databaseName);
					} catch (IOException e) {
						// If timeout/connection error is found, skip the
						// source.
						percentCompleted += increment;
						continue;
					}

					for (String key : datasources.keySet()) {
						final String dataSource = dispName + " - " + datasources.get(key);
						dsList.add(dataSource);
						datasourceMap.put(dataSource, key);
						taskMonitor.setStatusMessage("Loading Data Source: " + dataSource);
					}
				}
			}

			percentCompleted += increment;
			taskMonitor.setProgress(percentCompleted);
		}

		Collections.sort(dsList);
		
		taskMonitor.setStatusMessage("Finished: " + dsList.size());
		taskMonitor.setProgress(1.0);
		
		result = new LoadRepositoryResult(this.datasourceMap, this.dsList);
	}
	
	public LoadRepositoryResult getResult() {
		return result;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Select Services";
	}
}
