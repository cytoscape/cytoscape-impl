/*
 Copyright (c) 2006, 2007, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.search.internal;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndexAndSearchTask extends AbstractNetworkTask {
	
	private static final Logger logger = LoggerFactory.getLogger(IndexAndSearchTask.class);
	
	private boolean interrupted = false;
	
	private final EnhancedSearch enhancedSearch;
	private final CyNetworkViewManager viewManager;
	private final CyApplicationManager appManager;

	public String query;

	/**
	 * The constructor. Any necessary data that is <i>not</i> provided by
	 * the user should be provided as arguments to the constructor.
	 */
	public IndexAndSearchTask(final CyNetwork network, final EnhancedSearch enhancedSearch,
			final String query, final CyNetworkViewManager viewManager,
			final CyApplicationManager appManager) {

		// Will set a CyNetwork field called "net".
		super(network);
		this.enhancedSearch = enhancedSearch;
		this.query = query;
		this.viewManager = viewManager;
		this.appManager = appManager;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		
		logger.debug("Index and search start.");
		
		// Give the task a title.
		taskMonitor.setTitle("Searching the network");

		// Index the given network or use existing index
		RAMDirectory idx = null;

		final String status = enhancedSearch.getNetworkIndexStatus(network);
		logger.debug("Index status = " + status);
		
		if (status != null && status.equalsIgnoreCase(EnhancedSearch.INDEX_SET) && !EnhancedSearchPlugin.attributeChanged)
		{
			idx = enhancedSearch.getNetworkIndex(network);
		}
		else {
			taskMonitor.setStatusMessage("Indexing network");
			final EnhancedSearchIndex indexHandler = new EnhancedSearchIndex(network, taskMonitor);
			idx = indexHandler.getIndex();
			enhancedSearch.setNetworkIndex(network, idx);
			EnhancedSearchPlugin.attributeChanged = false;
		}

		if (interrupted)
			return;

		// Execute query
		taskMonitor.setStatusMessage("Executing query");
		EnhancedSearchQuery queryHandler = new EnhancedSearchQuery(network, idx);
		queryHandler.executeQuery(query);

		if (interrupted)
			return;
		
		showResults(queryHandler, taskMonitor);
		updateView();
	}

	/**
	 * If view(s) exists for the current network, update them.
	 */
	private void updateView() {
		final CyNetworkView targetView = viewManager.getNetworkView(network.getSUID());
		if(targetView != null)
			targetView.updateView();
		
		final CyNetworkView view = appManager.getCurrentNetworkView();
		if(view != null )
			view.updateView();
	}

	// Display results
	private void showResults(final EnhancedSearchQuery queryHandler, final TaskMonitor taskMonitor) {
		if (network == null || network.getNodeList().size() == 0)
			return;

		List<CyNode> nodeList = network.getNodeList();
		for (CyNode n : nodeList) {
			network.getRow(n).set(CyNetwork.SELECTED,false);
		}
		List<CyEdge> edgeList = network.getEdgeList();
		for (CyEdge e : edgeList) {
			network.getRow(e).set(CyNetwork.SELECTED, false);
		}

		int nodeHitCount = queryHandler.getNodeHitCount();
		int edgeHitCount = queryHandler.getEdgeHitCount();
		if (nodeHitCount == 0 && edgeHitCount == 0)
			return;

		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " and " + edgeHitCount + " edges");

		ArrayList<String> nodeHits = queryHandler.getNodeHits();
		ArrayList<String> edgeHits = queryHandler.getEdgeHits();

		final Iterator<String> nodeIt = nodeHits.iterator();
		int numCompleted = 0;
		while (nodeIt.hasNext() && !interrupted) {
			int currESPIndex = Integer.parseInt(nodeIt.next().toString());
			CyNode currNode = network.getNode(currESPIndex);
			if (currNode != null)
				network.getRow(currNode).set(CyNetwork.SELECTED, true);
			else
				System.out.println("Unknown node identifier " + (currESPIndex));

			taskMonitor.setProgress(numCompleted++ / nodeHitCount);
		}

		final Iterator<String> edgeIt = edgeHits.iterator();
		numCompleted = 0;
		while (edgeIt.hasNext() && !interrupted) {
			int currESPIndex = Integer.parseInt(edgeIt.next().toString());
			CyEdge currEdge = network.getEdge(currESPIndex);
			if (currEdge != null)
				network.getRow(currEdge).set(CyNetwork.SELECTED, true);
			else
				System.out.println("Unknown edge identifier " + (currESPIndex));

			taskMonitor.setProgress(++numCompleted / edgeHitCount);
		}
	}

	@Override
	public void cancel() {
		this.interrupted = true;
	}
}
