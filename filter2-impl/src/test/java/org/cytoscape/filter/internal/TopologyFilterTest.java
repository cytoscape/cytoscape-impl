package org.cytoscape.filter.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.internal.filters.topology.TopologyFilter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class TopologyFilterTest {

	@Test
	public void testTopologyFilter() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("s", String.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyNode n7 = network.addNode();
		CyNode n8 = network.addNode();
		network.addEdge(n1, n2, false);
		network.addEdge(n1, n3, false);
		network.addEdge(n1, n4, false);
		network.addEdge(n2, n5, false);
		network.addEdge(n3, n6, false);
		network.addEdge(n4, n7, false);
		network.addEdge(n7, n8, false);
		
		TopologyFilter topologyFilter = new TopologyFilter();
		topologyFilter.setThreshold(6);
		topologyFilter.setDistance(2);
		topologyFilter.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
		
		assertTrue(topologyFilter.accepts(network, n1));
		topologyFilter.setDistance(1);
		assertFalse(topologyFilter.accepts(network, n1));
		
		network.getRow(n2).set("s", "a");
		network.getRow(n6).set("s", "a");
		
		ColumnFilter columnFilter = new ColumnFilter("s", Predicate.IS, "a");
		assertTrue(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n6));
		
		topologyFilter.append(columnFilter);
		topologyFilter.setThreshold(2);
		topologyFilter.setDistance(2);
		
		assertTrue(topologyFilter.accepts(network, n1));
		topologyFilter.setDistance(1);
		assertFalse(topologyFilter.accepts(network, n1));
	}

	
	@Test
	public void testTopologyFilterCycle() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("s", String.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.addEdge(n1, n2, false);
		network.addEdge(n2, n3, false);
		network.addEdge(n3, n1, false);
		network.addEdge(n3, n1, false);
		network.addEdge(n3, n4, false);
		
		network.getRow(n4).set("s", "a");
		
		TopologyFilter topologyFilter = new TopologyFilter();
		topologyFilter.setThreshold(1);
		topologyFilter.setDistance(100);
		topologyFilter.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
		
		ColumnFilter columnFilter = new ColumnFilter("s", Predicate.IS, "a");
		topologyFilter.append(columnFilter);
		
		assertTrue(topologyFilter.accepts(network, n1)); // not getting caught in a cycle!
		
		topologyFilter.setThreshold(10);
		topologyFilter.setDistance(1000);
		topologyFilter.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
		
		assertFalse(topologyFilter.accepts(network, n1)); // not getting caught in a cycle!
	}
}
