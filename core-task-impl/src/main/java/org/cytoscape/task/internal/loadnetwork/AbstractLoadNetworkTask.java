package org.cytoscape.task.internal.loadnetwork;

import java.net.URI;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
 * Task to load a new network.
 */
abstract public class AbstractLoadNetworkTask extends AbstractTask {
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network";
	}
	
	private final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;
	
	protected int viewThreshold;
	
	protected URI uri;
	protected TaskMonitor taskMonitor;
	protected String name;
	protected boolean interrupted;
	protected final CyServiceRegistrar serviceRegistrar;

	public AbstractLoadNetworkTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		viewThreshold = getViewThreshold();
	}

	protected void loadNetwork(final CyNetworkReader reader) throws Exception {
		if (reader == null)
			throw new IllegalArgumentException("Could not read file: Network Reader is null.");

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Reading in Network Data...");
			taskMonitor.setProgress(0.0);
			taskMonitor.setStatusMessage("Creating Cytoscape Network...");
		}
		
		GenerateNetworkViewsTask generateViewsTask =
				new GenerateNetworkViewsTask(name, reader, viewThreshold, serviceRegistrar);
		insertTasksAfterCurrentTask(reader, generateViewsTask);
		
		if (taskMonitor != null)
			taskMonitor.setProgress(1.0);
	}

	private int getViewThreshold() {
		final Properties props = (Properties)
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		final String vts = props.getProperty(VIEW_THRESHOLD);
		int threshold;
		
		try {
			threshold = Integer.parseInt(vts);
		} catch (Exception e) {
			threshold = DEF_VIEW_THRESHOLD;
		}

		return threshold;
	}
}
