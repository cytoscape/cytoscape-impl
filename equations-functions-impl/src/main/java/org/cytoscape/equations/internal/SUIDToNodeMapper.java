package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;


public class SUIDToNodeMapper implements AddedNodesListener, AboutToRemoveNodesListener {
	private final Map<Long, CyNode> suidToNodeMap = new HashMap<Long, CyNode>();

	public void handleEvent(final AddedNodesEvent event) {
		for (CyNode node : event.getPayloadCollection()) {
			suidToNodeMap.put(node.getSUID(), node);
		}
	}

	public void handleEvent(final AboutToRemoveNodesEvent event) {
		for (CyNode node : event.getNodes()) {
			suidToNodeMap.remove(node.getSUID());
		}
	}

	public CyNode getNode(final Long id) {
		return suidToNodeMap.get(id);
	}
}
