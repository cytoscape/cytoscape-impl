package org.cytoscape.ding.impl.events;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.util.List;

import org.cytoscape.ding.GraphView;
import org.cytoscape.model.CyEdge;


public final class GraphViewEdgesHiddenEvent extends GraphViewChangeEventAdapter {
	private final static long serialVersionUID = 120241651250257L;
	private final GraphView m_view;
	private final List<CyEdge> m_hiddenEdgeInx;

	public GraphViewEdgesHiddenEvent(GraphView view, List<CyEdge> hiddenEdgeInx) {
		super(view);
		m_view = view;
		m_hiddenEdgeInx = hiddenEdgeInx;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final int getType() {
		return EDGES_HIDDEN_TYPE;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final CyEdge[] getHiddenEdges() {
		final CyEdge[] returnThis = new CyEdge[m_hiddenEdgeInx.size()];

		for (int i = 0; i < returnThis.length; i++)
			returnThis[i] = m_hiddenEdgeInx.get(i);

		return returnThis;
	}

}
