
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.ding.impl.events;

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
