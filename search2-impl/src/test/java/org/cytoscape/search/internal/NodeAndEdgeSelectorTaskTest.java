package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.search.internal.search.NodeAndEdgeSelectorTask;
import org.cytoscape.search.internal.search.SearchResults;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;


public class NodeAndEdgeSelectorTaskTest {


	@Test
	public void testNoHitsNothingSelectedBefore() {
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		network.getRow(network).set(CyNetwork.NAME, "My network");
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "node1");

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		NodeAndEdgeSelectorTask selector = new NodeAndEdgeSelectorTask(network, SearchResults.empty());
		selector.selectNodesAndEdges(mockMonitor);

		verify(mockMonitor).setStatusMessage("Could not find any match.");
		verify(mockMonitor).setTitle("Search Finished");
		verify(mockMonitor).setProgress(1.0);
		assertEquals(0, CyTableUtil.getSelectedNodes(network).size());
	}

	@Test
	public void testNoHitsOnlyNodeSelectedBefore() {
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		network.getRow(network).set(CyNetwork.NAME, "My network");
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "node1");
		network.getRow(node1).set(CyNetwork.SELECTED, true);

		CyNode node2 = network.addNode();
		network.getRow(node2).set(CyNetwork.NAME, "node2");
		network.getRow(node2).set(CyNetwork.SELECTED, true);

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		NodeAndEdgeSelectorTask selector = new NodeAndEdgeSelectorTask(network, SearchResults.empty());
		selector.selectNodesAndEdges(mockMonitor);

		verify(mockMonitor).setStatusMessage("Could not find any match.");
		verify(mockMonitor).setStatusMessage("Unsetting any existing selected nodes");
		verify(mockMonitor).setTitle("Search Finished");
		verify(mockMonitor, times(2)).setProgress(0.0);
		verify(mockMonitor).setProgress(1.0);
		assertEquals(0, CyTableUtil.getSelectedNodes(network).size());
	}

	@Test
	public void testTwoHitsNoNodesSelectedBefore() {
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		network.getRow(network).set(CyNetwork.NAME, "My network");
		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "node1");

		CyNode node2 = network.addNode();
		network.getRow(node2).set(CyNetwork.NAME, "node2");

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		SearchResults results = SearchResults.networkResults(network,
				Arrays.asList(node1.getSUID().toString(), node2.getSUID().toString()), new ArrayList<String>());
		
		NodeAndEdgeSelectorTask selector = new NodeAndEdgeSelectorTask(network, results);
		selector.selectNodesAndEdges(mockMonitor);

		//verify(mockMonitor).setStatusMessage("Unsetting any existing network selections");
		verify(mockMonitor).setStatusMessage("Selecting 2 nodes and 0 edges");
		assertEquals(2, CyTableUtil.getSelectedNodes(network).size());
	}

	@Test
	public void testTwoHitsAndEdgeWithNodesSelectedBefore() {
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		network.getRow(network).set(CyNetwork.NAME, "My network");

		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "node1");
		network.getRow(node1).set(CyNetwork.SELECTED, true);

		CyNode node2 = network.addNode();
		network.getRow(node2).set(CyNetwork.NAME, "node2");
		network.getRow(node2).set(CyNetwork.SELECTED, true);

		CyEdge edge1 = network.addEdge(node1, node2, false);
		network.getRow(edge1).set(CyNetwork.SELECTED, true);

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		SearchResults results = SearchResults.networkResults(network,
				Arrays.asList(node1.getSUID().toString(), node2.getSUID().toString()), 
				Arrays.asList(edge1.getSUID().toString()));
		
		NodeAndEdgeSelectorTask selector = new NodeAndEdgeSelectorTask(network, results);
		selector.selectNodesAndEdges(mockMonitor);

		verify(mockMonitor).setStatusMessage("Unsetting any existing selected nodes");
		verify(mockMonitor).setStatusMessage("Unsetting any existing selected edges");

		verify(mockMonitor).setStatusMessage("Selecting 2 nodes and 1 edge");
		
		assertEquals(2, CyTableUtil.getSelectedNodes(network).size());
		assertEquals(1, CyTableUtil.getSelectedEdges(network).size());
	}

	@Test
	public void testThreeHitsAndTwoEdgesWithNothingSelectedBefore() {
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		network.getRow(network).set(CyNetwork.NAME, "My network");

		CyNode node1 = network.addNode();
		network.getRow(node1).set(CyNetwork.NAME, "node1");

		CyNode node2 = network.addNode();
		network.getRow(node2).set(CyNetwork.NAME, "node2");

		CyNode node3 = network.addNode();
		network.getRow(node3).set(CyNetwork.NAME, "node3");

		CyEdge edge1 = network.addEdge(node1, node2, false);

		CyEdge edge2 = network.addEdge(node1, node3, false);

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		SearchResults results = SearchResults.networkResults(network,
				Arrays.asList(node1.getSUID().toString(), node2.getSUID().toString()), 
				Arrays.asList(edge1.getSUID().toString(), edge2.getSUID().toString()));
		
		NodeAndEdgeSelectorTask selector = new NodeAndEdgeSelectorTask(network, results);
		selector.selectNodesAndEdges(mockMonitor);

		verify(mockMonitor).setStatusMessage("Unsetting any existing selected nodes");
		verify(mockMonitor).setStatusMessage("Unsetting any existing selected edges");
		verify(mockMonitor).setStatusMessage("Selecting 2 nodes and 2 edges");
		assertEquals(2, CyTableUtil.getSelectedNodes(network).size());
		assertEquals(2, CyTableUtil.getSelectedEdges(network).size());
	}
}
