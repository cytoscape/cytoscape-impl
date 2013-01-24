package org.cytoscape.task.internal.select;

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

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;

final class SelectUtils {

	void setSelectedNodes(final CyNetwork network, final Collection<CyNode> nodes, final boolean select) {
		setSelected(network,nodes, select);
	}

	void setSelectedEdges(final CyNetwork network, final Collection<CyEdge> edges, final boolean select) {
		setSelected(network,edges, select);
	}

	private void setSelected(final CyNetwork network, final Collection<? extends CyIdentifiable> objects, final boolean select) {

		for (final CyIdentifiable nodeOrEdge : objects)
			network.getRow(nodeOrEdge).set(CyNetwork.SELECTED, select);
	}
}
