package org.cytoscape.model;


import org.cytoscape.model.CyTableEntry;
import org.junit.Before;
import org.junit.After;

import java.util.Random;


/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class TableTestSupportTest extends AbstractCyTableTest {
	TableTestSupport support; 
	CyTableFactory factory;
	Random rand;

	public TableTestSupportTest() {
		support = new TableTestSupport();
		factory = support.getTableFactory();
		rand = new Random(15);
	}

	@Before
	public void setUp() {
		eventHelper = support.getDummyCyEventHelper(); 
		table = factory.createTable(Integer.toString( rand.nextInt(10000) ), CyTableEntry.SUID, Long.class, false, true);
		table2 = factory.createTable(Integer.toString( rand.nextInt(10000) ), CyTableEntry.SUID, Long.class, false, true);
		attrs = table.getRow(1l);
	}

	@After
	public void tearDown() {
		table = null;
		attrs = null;
	}
}
