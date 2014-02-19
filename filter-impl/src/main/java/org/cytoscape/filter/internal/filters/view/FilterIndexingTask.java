package org.cytoscape.filter.internal.filters.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2013 The Cytoscape Consortium
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

import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

final class FilterIndexingTask extends AbstractTask {
	private final CyNetwork network;
	private final QuickFind quickFind;

	FilterIndexingTask(final QuickFind quickFind, final CyNetwork network) {
		this.network = network;
		this.quickFind = quickFind;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		quickFind.addNetwork(network, taskMonitor);
	}
}
