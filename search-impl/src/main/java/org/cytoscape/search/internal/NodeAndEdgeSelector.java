package org.cytoscape.search.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
 * Defines methods to unselect and select nodes and edges
 * @author churas
 *
 */
public interface NodeAndEdgeSelector {

	/**
	 * Selects all nodes and edges contained in 'searchResults' passed in
	 * on 'network' passed in
	 * @param network the network to modify
	 * @param searchResults edges and nodes to select
	 * @param task The task under which this node selection is occurring
	 * @param taskMonitor a monitor that lets caller know status
	 */
	public void selectNodesAndEdges(CyNetwork network, SearchResults searchResults, IndexAndSearchTask task, TaskMonitor taskMonitor);

}
