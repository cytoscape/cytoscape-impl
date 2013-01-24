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

import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.GraphViewChangeEvent;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;


abstract class GraphViewChangeEventAdapter extends GraphViewChangeEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6878247224212872761L;

	GraphViewChangeEventAdapter(GraphView source) {
		super(source);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public abstract int getType();

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isNodesRestoredType() {
		return (getType() & NODES_RESTORED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isEdgesRestoredType() {
		return (getType() & EDGES_RESTORED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isNodesHiddenType() {
		return (getType() & NODES_HIDDEN_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isEdgesHiddenType() {
		return (getType() & EDGES_HIDDEN_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isNodesSelectedType() {
		return (getType() & NODES_SELECTED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isNodesUnselectedType() {
		return (getType() & NODES_UNSELECTED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isEdgesSelectedType() {
		return (getType() & EDGES_SELECTED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final boolean isEdgesUnselectedType() {
		return (getType() & EDGES_UNSELECTED_TYPE) != 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyNode[] getRestoredNodes() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyEdge[] getRestoredEdges() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyNode[] getHiddenNodes() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyEdge[] getHiddenEdges() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyNode[] getSelectedNodes() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyNode[] getUnselectedNodes() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyEdge[] getSelectedEdges() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CyEdge[] getUnselectedEdges() {
		return null;
	}

}
