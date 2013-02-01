package org.cytoscape.ding;

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

import java.util.EventObject;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

/**
 * The event source must be the GraphPerspective that changed.
 */
public abstract class GraphViewChangeEvent extends EventObject {

	private static final long serialVersionUID = 7718801897482070648L;

	public static final int NODES_RESTORED_TYPE = 1;
	public static final int EDGES_RESTORED_TYPE = 2;
	public static final int NODES_HIDDEN_TYPE = 4;
	public static final int EDGES_HIDDEN_TYPE = 8;
	public static final int NODES_SELECTED_TYPE = 16;
	public static final int NODES_UNSELECTED_TYPE = 32;
	public static final int EDGES_SELECTED_TYPE = 64;
	public static final int EDGES_UNSELECTED_TYPE = 128;

	public GraphViewChangeEvent(final GraphView source) {
		super(source);
	}

	public abstract int getType();

	public abstract boolean isNodesRestoredType();

	public abstract boolean isEdgesRestoredType();

	public abstract boolean isNodesHiddenType();

	public abstract boolean isEdgesHiddenType();

	public abstract boolean isNodesSelectedType();

	public abstract boolean isNodesUnselectedType();

	public abstract boolean isEdgesSelectedType();

	public abstract boolean isEdgesUnselectedType();

	public abstract CyNode[] getRestoredNodes();

	public abstract CyEdge[] getRestoredEdges();

	public abstract CyNode[] getHiddenNodes();

	public abstract CyEdge[] getHiddenEdges();

	public abstract CyNode[] getSelectedNodes();

	public abstract CyNode[] getUnselectedNodes();

	public abstract CyEdge[] getSelectedEdges();

	public abstract CyEdge[] getUnselectedEdges();

}
