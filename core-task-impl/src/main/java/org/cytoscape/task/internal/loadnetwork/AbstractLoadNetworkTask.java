package org.cytoscape.task.internal.loadnetwork;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.net.URI;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

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
	
	protected CyNetworkReader reader;
	protected URI uri;
	protected TaskMonitor taskMonitor;
	protected String name;
	protected boolean interrupted = false;
	protected CyNetworkReaderManager mgr;
	protected CyNetworkManager networkManager;
	protected CyNetworkViewManager networkViewManager;
	protected Properties props;
	protected CyNetworkNaming namingUtil;
	protected final VisualMappingManager vmm;
	protected final CyNetworkViewFactory nullNetworkViewFactory;

	public AbstractLoadNetworkTask(
			final CyNetworkReaderManager mgr,
			final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager,
			final Properties props,
			final CyNetworkNaming namingUtil,
			final VisualMappingManager vmm,
			final CyNetworkViewFactory nullNetworkViewFactory
	) {
		this.mgr = mgr;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.props = props;
		this.namingUtil = namingUtil;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;

		this.viewThreshold = getViewThreshold();
	}

	protected void loadNetwork(final CyNetworkReader viewReader) throws Exception {
		if (viewReader == null)
			throw new IllegalArgumentException("Could not read file: Network View Reader is null.");

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Reading in Network Data...");
			taskMonitor.setProgress(0.0);
			taskMonitor.setStatusMessage("Creating Cytoscape Network...");
		}
		
		GenerateNetworkViewsTask generateViewsTask = new GenerateNetworkViewsTask(name, viewReader, networkManager,
				networkViewManager, namingUtil, viewThreshold, vmm, nullNetworkViewFactory);
		insertTasksAfterCurrentTask(viewReader, generateViewsTask);
		
		if (taskMonitor != null)
			taskMonitor.setProgress(1.0);
	}

	private int getViewThreshold() {
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
