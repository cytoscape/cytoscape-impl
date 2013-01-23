package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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


import org.apache.lucene.store.RAMDirectory;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.TaskMonitor;

public class ReindexTask extends AbstractNetworkTask {

	private CyNetwork network;
	private EnhancedSearch enhancedSearch;
	private boolean interrupted = false;

	public ReindexTask(final CyNetwork network, EnhancedSearch enhancedSearch) {
		// Will set a CyNetwork field called "net".
		super(network);
		this.enhancedSearch = enhancedSearch;
		this.network = network;
	}

	// Executes Task: Reindex
	public void run(final TaskMonitor taskMonitor) {

		// Index the given network or use existing index
		RAMDirectory idx = null;

		taskMonitor.setStatusMessage("Re-indexing network");
		EnhancedSearchIndex indexHandler = new EnhancedSearchIndex(network, taskMonitor);
		idx = indexHandler.getIndex();
		enhancedSearch.setNetworkIndex(network, idx);

		if (interrupted) {
			return;
		}

		taskMonitor.setProgress(1);
		taskMonitor.setStatusMessage("Network re-indexed successfuly");

	}

	public void cancel() {
		this.interrupted = true;
	}

}