/*
 File: FlagAndSelectionHandler.java

 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.GraphViewChangeEvent;
import org.cytoscape.ding.GraphViewChangeListener;
import org.cytoscape.ding.NodeView;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class synchronizes the flagged status of nodes and edges as held by a
 * SelectFilter object of a network with the selection status of the
 * corresponding node and edge views in a GraphView. An object will be selected
 * in the view iff the matching object is flagged in the SelectFilter. This
 * class is only used by PhoebeNetworkView, which no longer used anywhere.
 * 
 */
public class FlagAndSelectionHandler implements GraphViewChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(FlagAndSelectionHandler.class);

	private final GraphView view;
	private final CyNetwork network;

	/**
	 * Standard constructor takes the flag filter and the view that should be
	 * synchronized. On construction, this object will synchronize the filter
	 * and view by turning on flags or selections that are currently on in one
	 * of the two objects.
	 */
	public FlagAndSelectionHandler(final GraphView view) {
		this.view = view;
		this.network = view.getNetwork();

		syncFilterAndView();
		view.addGraphViewChangeListener(this);
	}

	private Set<CyNode> getSelectedNodes() {
		final Set<CyNode> selectedNodes = new HashSet<CyNode>();

		for (final CyNode n : network.getNodeList()) {
			final Boolean selected = network.getRow(n).get(CyNetwork.SELECTED, Boolean.class);
			if (selected != null && selected)
				selectedNodes.add(n);
		}

		return selectedNodes;
	}

	private Set<CyEdge> getSelectedEdges() {
		final Set<CyEdge> selectedEdges = new HashSet<CyEdge>();

		for (final CyEdge n : network.getEdgeList()) {
			final Boolean selected = network.getRow(n).get(CyNetwork.SELECTED, Boolean.class);
			if (selected != null && selected)
				selectedEdges.add(n);
		}

		return selectedEdges;
	}

	/**
	 * Synchronizes the filter and view of this object by selecting every object
	 * that is currently flagged and vice versa.
	 */
	private void syncFilterAndView() {
		final Set<CyNode> flaggedNodes = getSelectedNodes();
		final Set<CyEdge> flaggedEdges = getSelectedEdges();

		final List<CyNode> selectedNodes = view.getSelectedNodes();
		final List<CyEdge> selectedEdges = view.getSelectedEdges();

		// select all nodes that are flagged but not currently selected
		for (final CyNode node : flaggedNodes) {
			final NodeView nv = view.getDNodeView(node);

			if ((nv == null) || nv.isSelected())
				continue;

			nv.setSelected(true);
		}

		// select all edges that are flagged but not currently selected
		for (final CyEdge edge : flaggedEdges) {
			final EdgeView ev = view.getDEdgeView(edge);

			if ((ev == null) || ev.isSelected())
				continue;

			ev.setSelected(true);
		}

		// flag all nodes that are selected but not currently flagged
		select(selectedNodes, true);

		// flag all edges that are selected but not currently flagged
		select(selectedEdges, true);
	}

	private void select(final Collection<? extends CyTableEntry> nodesOrEdges, final boolean selected) {
		if (nodesOrEdges.isEmpty())
			return;
		
		for (final CyTableEntry nodeOrEdge : nodesOrEdges)
			network.getRow(nodeOrEdge).set(CyNetwork.SELECTED, selected);		
	}

	/**
	 * Responds to selection events from the view by setting the matching
	 * flagged state in the SelectFilter object.
	 */
	public void graphViewChanged(final GraphViewChangeEvent event) {

		// GINY bug: the event we get frequently has the correct indices
		// but incorrect Node and Edge objects. For now we get around this
		// by converting indices to graph objects ourselves

		final long start = System.currentTimeMillis();

		if (event.isNodesSelectedType()) {
			final CyNode[] selectedNodes = event.getSelectedNodes();
			select(Arrays.asList(selectedNodes), true);
		} else if (event.isNodesUnselectedType() || event.isNodesHiddenType()) {
			final CyNode[] objIndecies;
			if (event.isNodesUnselectedType())
				objIndecies = event.getUnselectedNodes();
			else
				objIndecies = event.getHiddenNodes();

			select(Arrays.asList(objIndecies), false);
		} else if (event.isEdgesSelectedType()) {
			final CyEdge[] objIndecies = event.getSelectedEdges();
			select(Arrays.asList(objIndecies), true);
		} else if (event.isEdgesUnselectedType() || event.isEdgesHiddenType()) {
			final CyEdge[] objIndecies;
			if (event.isEdgesUnselectedType())
				objIndecies = event.getUnselectedEdges();
			else
				objIndecies = event.getHiddenEdges();

			select(Arrays.asList(objIndecies), false);
		}

		logger.debug("Finished selection: Time = " + (System.currentTimeMillis() - start)
				+ " msec.");
	}
}
