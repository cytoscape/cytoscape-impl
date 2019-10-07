package org.cytoscape.search.internal;

import java.util.Collection;

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


import java.util.Iterator;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that deselects and selects nodes/edges on a network
 *
 * @author churas
 */
public class NodeAndEdgeSelectorImpl implements NodeAndEdgeSelector {

	private static final Logger logger = LoggerFactory.getLogger(NodeAndEdgeSelectorImpl.class);

	/**
	 * Denotes how frequently the code checks to see if the task
	 * was cancelled when deselecting edges and nodes. A value of 50,000
	 * means after deselecting 50,000 nodes or edges check the cancelled
	 * flag.
	 */
	public static final long UNSET_NETWORK_CHECK_CANCEL_FREQ = 50000;

	public static final int PROGRESS_UPDATE_FREQ = 10000;

	/**
	 * Unselects nodes and edges on {@code network} and then selects nodes and edges on
	 * that {@code network} as found in {@code searchResults}. If the {@code network}
	 * is null or has no nodes this method just returns. If {@code task.isCancelled()} is
	 * {@code true} this method returns immediately and can leave the {@code network} in an inconsistent
	 * state
	 * @param network The network to adjust selections on
	 * @param searchResults the nodes and edges to select
	 * @param task Task this is running under
	 * @param taskMonitor monitor used to let caller know status
	 */
	@Override
	public void selectNodesAndEdges(CyNetwork network, SearchResults searchResults, IndexAndSearchTask task, TaskMonitor taskMonitor) {
		if (network == null || network.getNodeList().size() == 0)
			return;

		int nodeHitCount = searchResults.getNodeHitCount();
		int edgeHitCount = searchResults.getEdgeHitCount();
		
		if (nodeHitCount == 0 && edgeHitCount == 0) {
			taskMonitor.setStatusMessage("Could not find any match.");
			unselectNodesAndEdges(network, task, taskMonitor);
			taskMonitor.setTitle("Search Finished");
			taskMonitor.setProgress(1.0);
			return;
		}
		if (!unselectNodesAndEdges(network, task, taskMonitor)) {
			return;
		}
		String nodeplural = "s";
		if (nodeHitCount == 1) {
			nodeplural = "";
		}
		String edgeplural = "s";
		if (edgeHitCount == 1) {
			edgeplural = "";
		}
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " node" + nodeplural + " and " + edgeHitCount + " edge" + edgeplural);

		List<String> nodeHits = searchResults.getNodeHits();
		List<String> edgeHits = searchResults.getEdgeHits();
		int totalHitCount = nodeHitCount + edgeHitCount;
		long startTime = System.currentTimeMillis();
		selectNodes(nodeHits, nodeHitCount, totalHitCount, network, task, taskMonitor);
		selectEdges(edgeHits, edgeHitCount, totalHitCount, network, task, taskMonitor);
		
		if (task.isCancelled()) {
			return;
		}
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " nodes and "
		                             + edgeHitCount + " edges completed in "
				                     + Long.toString(System.currentTimeMillis() - startTime) + " ms");
	}

	/**
	 * Unselects any selected nodes
	 * @param network The network with nodes to unselect
	 * @param task Invoking task used to see if this method should return early
	 * @return true upon success or false if method exited early cause {@code task.isCancelled()} returned true
	 */
	private boolean unselectNodes(CyNetwork network, IndexAndSearchTask task, TaskMonitor taskMonitor) {
		taskMonitor.setStatusMessage("Unsetting any existing selected nodes");
		long startTime = System.currentTimeMillis();
		//bypassing CyTableUtil to skip the allocation of a new ArrayList
		Collection<Long> suids = network.getDefaultNodeTable().getMatchingKeys(CyNetwork.SELECTED, 
				true, Long.class);
		long counter = 0;
		int numNodes = suids.size();
		taskMonitor.setProgress(0.0);
		for (Long suid : suids) {
			CyNode n = network.getNode(suid);
			if (n == null) {
				continue;
			}
			network.getRow(n).set(CyNetwork.SELECTED,false);
			counter += 1;
			if (counter % UNSET_NETWORK_CHECK_CANCEL_FREQ == 0) {
				if (task.isCancelled())
					return false;
			}
			if (counter % PROGRESS_UPDATE_FREQ == 0) {
				taskMonitor.setProgress((double)counter / (double)numNodes);
			}
		}
		taskMonitor.setStatusMessage("Unsetting any existing selected nodes completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");

		return true;
	}
	
	/**
	 * Unselects any selected edges
	 * @param network The network with edges to unselect
	 * @param task Invoking task used to see if this method should return early
	 * @return true upon success or false if method exited early cause {@code task.isCancelled()} returned true
	 */
	private boolean unselectEdges(CyNetwork network, IndexAndSearchTask task, TaskMonitor taskMonitor) {
		taskMonitor.setStatusMessage("Unsetting any existing selected edges");
		long startTime = System.currentTimeMillis();
		//bypassing CyTableUtil to skip the allocation of a new ArrayList
		Collection<Long> suids = network.getDefaultEdgeTable().getMatchingKeys(CyNetwork.SELECTED, 
				true, Long.class);
		long counter = 0;
		taskMonitor.setProgress(0.0);
		int numEdges = suids.size();
		for (Long suid: suids) {
			CyEdge e = network.getEdge(suid);
			if (e == null) {
				continue;
			}
			network.getRow(e).set(CyNetwork.SELECTED, false);
			counter += 1;
			if (counter % UNSET_NETWORK_CHECK_CANCEL_FREQ == 0) {
				if (task.isCancelled())
					return false;
			}
			if (counter % PROGRESS_UPDATE_FREQ == 0) {
				taskMonitor.setProgress((double)counter / (double)numEdges);
			}
		}
		taskMonitor.setStatusMessage("Unsetting any existing selected edges completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
		return true;
	}
	
	/**
	 * Unselects any selected nodes and edges. If task is cancelled this method
	 * will return and there is no guarantee that all edges/nodes have been
	 * unselected
	 * @param taskMonitor
	 */
	private boolean unselectNodesAndEdges(CyNetwork network, IndexAndSearchTask task, TaskMonitor taskMonitor) {
		if (!this.unselectNodes(network, task, taskMonitor))
			return false;
		
		if (!this.unselectEdges(network, task, taskMonitor))
			return false;
		return true;
	}

	/**
	 * Iterates through the list of {@code nodeHits} and selects those nodes
	 * in the {@code network} passed in
	 * @param nodeHits list of node hits
	 * @param nodeHitCount number of elements in nodeHits
	 * @param network network to update
	 * @param task the task requesting the update
	 * @param taskMonitor task monitor that updates the user as to status
	 */
	private void selectNodes(final List<String> nodeHits, int nodeHitCount, int totalHitCount, CyNetwork network,
			IndexAndSearchTask task, TaskMonitor taskMonitor) {
		final Iterator<String> nodeIt = nodeHits.iterator();
		int counter = 0;
		taskMonitor.setProgress(0.0);
		while (nodeIt.hasNext() && !task.isCancelled()) {
			int currESPIndex = Integer.parseInt(nodeIt.next());
			CyNode currNode = network.getNode(currESPIndex);

			if (currNode != null)
				network.getRow(currNode).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown node identifier " + (currESPIndex));
			counter++;
			if (counter % PROGRESS_UPDATE_FREQ == 0)
				taskMonitor.setProgress(counter / totalHitCount);
		}
	}
	
	/**
	 * Iterates through the list of {@code edgeHits} and selects those nodes
	 * in the {@code network} passed in
	 * @param edgeHits list of edge hits
	 * @param edgeHitCount number of elements in edgeHits
	 * @param network network to update
	 * @param task the task requesting the update
	 * @param taskMonitor task monitor that updates the user as to status
	 */
	private void selectEdges(final List<String> edgeHits, int edgeHitCount, int totalHitCount, CyNetwork network,
			IndexAndSearchTask task, TaskMonitor taskMonitor) {
		final Iterator<String> edgeIt = edgeHits.iterator();
		int counter = totalHitCount - edgeHitCount;

		while (edgeIt.hasNext() && !task.isCancelled()) {
			int currESPIndex = Integer.parseInt(edgeIt.next());
			CyEdge currEdge = network.getEdge(currESPIndex);
			
			if (currEdge != null)
				network.getRow(currEdge).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown edge identifier " + (currESPIndex));
			counter++;
			if (counter % PROGRESS_UPDATE_FREQ == 0)
				taskMonitor.setProgress(counter / totalHitCount);
		}
	}
}
