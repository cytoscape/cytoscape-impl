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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;


public class SUIDToEdgeMapper implements AddedEdgesListener, AboutToRemoveEdgesListener {
	private final Map<Long, CyEdge> suidToEdgeMap = new HashMap<Long, CyEdge>();

	public void handleEvent(final AddedEdgesEvent event) {
		for (CyEdge edge : event.getPayloadCollection()) {
			suidToEdgeMap.put(edge.getSUID(), edge);
		}
	}

	public void handleEvent(final AboutToRemoveEdgesEvent event) {
		for (CyEdge edge : event.getEdges()) {
			suidToEdgeMap.remove(edge.getSUID());
		}
	}

	public CyEdge getEdge(final Long id) {
		return suidToEdgeMap.get(id);
	}
}
