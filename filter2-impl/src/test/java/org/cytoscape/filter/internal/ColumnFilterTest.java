package org.cytoscape.filter.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class ColumnFilterTest {

	@Test
	public void testColumnFilterString() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("s", String.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.getRow(n1).set("s", "aaa");
		network.getRow(n2).set("s", "bbb");
		network.getRow(n3).set("s", "ccc");
		
		ColumnFilter columnFilter = new ColumnFilter("s", Predicate.CONTAINS, "a");
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		assertFalse(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.DOES_NOT_CONTAIN, "a");
		
		assertFalse(columnFilter.accepts(network, n1));
		assertTrue(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n3));
		assertTrue(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.IS, "a");
		
		assertFalse(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		assertFalse(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.IS, "aaa");
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		assertFalse(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.IS_NOT, "aaa");
		
		assertFalse(columnFilter.accepts(network, n1));
		assertTrue(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n3));
		assertTrue(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.REGEX, ".a.");
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		assertFalse(columnFilter.accepts(network, n4));
	}
	
	
	@Test
	public void testColumnFilterInteger() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("i", Integer.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.getRow(n1).set("i", 111);
		network.getRow(n2).set("i", 222);
		network.getRow(n3).set("i", 333);
		
		ColumnFilter columnFilter = new ColumnFilter("i", Predicate.BETWEEN, new Number[] {200, 300});
		
		assertFalse(columnFilter.accepts(network, n1));
		assertTrue(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		assertFalse(columnFilter.accepts(network, n4));
		
		columnFilter.setPredicateAndCriterion(Predicate.IS_NOT_BETWEEN, new Number[] {200, 300});
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n3));
		assertTrue(columnFilter.accepts(network, n4));
	}
	
	
	@Test
	public void testColumnFilterBoolean() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createColumn("b", Boolean.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		network.getRow(n1).set("b", true);
		network.getRow(n2).set("b", false);
		
		ColumnFilter columnFilter = new ColumnFilter("b", Predicate.IS, true);
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
		
		columnFilter.setPredicateAndCriterion(Predicate.IS, false);
		
		assertFalse(columnFilter.accepts(network, n1));
		assertTrue(columnFilter.accepts(network, n2));
		assertFalse(columnFilter.accepts(network, n3));
	}
	
	
	@Test
	public void testColumnFilterList() {
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		network.getDefaultNodeTable().createListColumn("l", String.class, false);
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		network.getRow(n1).set("l", Arrays.asList("a","b","c"));
		network.getRow(n2).set("l", Arrays.asList("c","d"));
		network.getRow(n3).set("l", Arrays.asList("a", "a"));
		
		ColumnFilter columnFilter = new ColumnFilter("l", Predicate.IS, "a");
		columnFilter.setAnyMatch(true);
		
		assertTrue(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n3));
		
		columnFilter.setAnyMatch(false);
		
		assertFalse(columnFilter.accepts(network, n1));
		assertFalse(columnFilter.accepts(network, n2));
		assertTrue(columnFilter.accepts(network, n3));
	}
	
	
	@Test
	public void testColumnFilterAlwaysFalse() {
		ColumnFilter columnFilter = new ColumnFilter();
		assertTrue(columnFilter.isAlwaysFalse());
		columnFilter.setPredicateAndCriterion(Predicate.IS, "a");
		assertTrue(columnFilter.isAlwaysFalse());
		columnFilter.setColumnName("c");
		assertFalse(columnFilter.isAlwaysFalse());
	}

}
