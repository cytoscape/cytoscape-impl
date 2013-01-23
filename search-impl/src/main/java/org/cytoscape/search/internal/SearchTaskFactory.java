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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;


public class SearchTaskFactory extends AbstractNetworkTaskFactory {
	private EnhancedSearch searchMgr;
	private String query;
	
	private final CyNetworkViewManager viewManager;
	private final CyApplicationManager appManager;
	
	public SearchTaskFactory(EnhancedSearch searchMgr, String query,
			final CyNetworkViewManager viewManager, final CyApplicationManager appManager) {
		this.searchMgr = searchMgr;
		this.query = query;
		this.viewManager = viewManager;
		this.appManager = appManager;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new IndexAndSearchTask(network, searchMgr, query, viewManager, appManager));
	}
}
