package org.cytoscape.search.internal;

import java.util.Iterator;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeAndEdgeSelectorImpl implements NodeAndEdgeSelector {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	/**
	 * Denotes how frequently the code checks to see if the task
	 * was cancelled when deselecting edges and nodes. A value of 50,000
	 * means after deselecting 50,000 nodes or edges check the cancelled
	 * flag.
	 */
	public static final long UNSET_NETWORK_CHECK_CANCEL_FREQ = 50000;

	private boolean unselectNodes(CyNetwork network, IndexAndSearchTask task) {
		List<CyNode> nodeList = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		long counter = 0;
		for (CyNode n : nodeList) {
			network.getRow(n).set(CyNetwork.SELECTED,false);
			counter += 1;
			if (counter % UNSET_NETWORK_CHECK_CANCEL_FREQ == 0) {
				if (task.isCancelled())
					return false;
			}
		}
		return true;
	}
	
	private boolean unselectEdges(CyNetwork network, IndexAndSearchTask task) {
		List<CyEdge> edgeList = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		long counter = 0;
		for (CyEdge e : edgeList) {
			network.getRow(e).set(CyNetwork.SELECTED, false);
			counter += 1;
			if (counter % UNSET_NETWORK_CHECK_CANCEL_FREQ == 0) {
				if (task.isCancelled())
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Unselects any selected nodes and edges. If task is cancelled this method
	 * will return and there is no guarantee that all edges/nodes have been
	 * unselected
	 * @param taskMonitor
	 */
	private boolean unselectNodesAndEdges(CyNetwork network, IndexAndSearchTask task, TaskMonitor taskMonitor) {
		taskMonitor.setStatusMessage("Unsetting any existing network selections");
		long startTime = System.currentTimeMillis();
		if (!this.unselectNodes(network, task))
			return false;
		
		if (!this.unselectEdges(network, task))
			return false;

		taskMonitor.setStatusMessage("Unsetting any existing network selections completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
		return true;
	}

	/**
	 * Unselects nodes and edges and then selects nodes and edges as found in {@code searchResulst}
	 * returning prematurely if the {@code task} has been cancelled
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
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " nodes and " + edgeHitCount + " edges");

		List<String> nodeHits = searchResults.getNodeHits();
		List<String> edgeHits = searchResults.getEdgeHits();

		long startTime = System.currentTimeMillis();
		selectNodes(nodeHits, nodeHitCount, network, task, taskMonitor);
		selectEdges(edgeHits, edgeHitCount, network, task, taskMonitor);
		
		if (task.isCancelled()) {
			return;
		}
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " nodes and "
		                             + edgeHitCount + " edges completed in "
				                     + Long.toString(System.currentTimeMillis() - startTime) + " ms");
	}
	
	private void selectNodes(final List<String> nodeHits, int nodeHitCount, CyNetwork network,
			IndexAndSearchTask task, TaskMonitor taskMonitor) {
		final Iterator<String> nodeIt = nodeHits.iterator();
		int numCompleted = 0;

		while (nodeIt.hasNext() && !task.isCancelled()) {
			int currESPIndex = Integer.parseInt(nodeIt.next().toString());
			CyNode currNode = network.getNode(currESPIndex);

			if (currNode != null)
				network.getRow(currNode).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown node identifier " + (currESPIndex));

			taskMonitor.setProgress(numCompleted++ / nodeHitCount);
		}
	}
	
	private void selectEdges(final List<String> edgeHits, int edgeHitCount, CyNetwork network,
			IndexAndSearchTask task, TaskMonitor taskMonitor) {
		final Iterator<String> edgeIt = edgeHits.iterator();
		int numCompleted = 0;

		while (edgeIt.hasNext() && !task.isCancelled()) {
			int currESPIndex = Integer.parseInt(edgeIt.next().toString());
			CyEdge currEdge = network.getEdge(currESPIndex);
			
			if (currEdge != null)
				network.getRow(currEdge).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown edge identifier " + (currESPIndex));

			taskMonitor.setProgress(++numCompleted / edgeHitCount);
		}
	}
}
