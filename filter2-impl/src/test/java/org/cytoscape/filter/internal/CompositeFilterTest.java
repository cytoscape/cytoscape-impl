package org.cytoscape.filter.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.CompositeFilter.Type;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class CompositeFilterTest {

	@Test
	public void testCompositeFilterString() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("s", String.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.getRow(n1).set("s", "abc");
		network.getRow(n2).set("s", "def");
		network.getRow(n3).set("s", "aa");
		
		ColumnFilter filterForA = new ColumnFilter("s", Predicate.CONTAINS, "a");
		ColumnFilter filterForB = new ColumnFilter("s", Predicate.CONTAINS, "b");
		ColumnFilter filterForC = new ColumnFilter("s", Predicate.CONTAINS, "c");
		
		CompositeFilter<CyNetwork,CyIdentifiable> composite = new CompositeFilterImpl<>(CyNetwork.class, CyIdentifiable.class);
		composite.append(filterForA);
		composite.append(filterForB);
		composite.append(filterForC);
		
		composite.setType(Type.ANY);
		
		assertTrue(composite.accepts(network, n1));
		assertFalse(composite.accepts(network, n2));
		assertTrue(composite.accepts(network, n3));
		assertFalse(composite.accepts(network, n4));
		
		composite.setType(Type.ALL);
		
		assertTrue(composite.accepts(network, n1));
		assertFalse(composite.accepts(network, n2));
		assertFalse(composite.accepts(network, n3));
		assertFalse(composite.accepts(network, n4));
	}
	
	
	@Test
	public void testCompositeFilterEmpty() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("s", String.class, false);
		
		CyNode n1 = network.addNode();
		network.getRow(n1).set("s", "abc");
		
		CompositeFilter<CyNetwork,CyIdentifiable> composite = new CompositeFilterImpl<>(CyNetwork.class, CyIdentifiable.class);
		
		composite.setType(Type.ANY);
		assertTrue(composite.accepts(network, n1));
		composite.setType(Type.ALL);
		assertTrue(composite.accepts(network, n1));
	}
	
	
	@Test
	public void testCompositeFilterAlwaysFalse() {
		CompositeFilter<CyNetwork,CyIdentifiable> composite = new CompositeFilterImpl<>(CyNetwork.class, CyIdentifiable.class);
		ColumnFilter filterForA = new ColumnFilter();
		ColumnFilter filterForB = new ColumnFilter();
		ColumnFilter filterForC = new ColumnFilter();
		composite.append(filterForA);
		composite.append(filterForB);
		composite.append(filterForC);
		
		assertTrue(filterForA.isAlwaysFalse());
		assertTrue(filterForB.isAlwaysFalse());
		assertTrue(filterForC.isAlwaysFalse());
		
		composite.setType(Type.ALL);
		assertTrue(composite.isAlwaysFalse());
		composite.setType(Type.ANY);
		assertTrue(composite.isAlwaysFalse());
		
		filterForA.setColumnName("s");
		filterForA.setPredicateAndCriterion(Predicate.CONTAINS, "a");
		assertFalse(filterForA.isAlwaysFalse());
		
		composite.setType(Type.ALL);
		assertTrue(composite.isAlwaysFalse());
		composite.setType(Type.ANY);
		assertFalse(composite.isAlwaysFalse());
		
		filterForB.setColumnName("s");
		filterForB.setPredicateAndCriterion(Predicate.CONTAINS, "a");
		filterForC.setColumnName("s");
		filterForC.setPredicateAndCriterion(Predicate.CONTAINS, "a");
		
		composite.setType(Type.ALL);
		assertFalse(composite.isAlwaysFalse());
		composite.setType(Type.ANY);
		assertFalse(composite.isAlwaysFalse());
	}

}
