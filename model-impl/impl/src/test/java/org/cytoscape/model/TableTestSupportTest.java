package org.cytoscape.test.support;


import org.cytoscape.model.AbstractCyTableTest;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;

import org.junit.Before;
import org.junit.After;

import java.util.Random;


/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class DataTableTestSupportTest extends AbstractCyTableTest {
	DataTableTestSupport support; 
	CyTableFactory factory;
	Random rand;

	public DataTableTestSupportTest() {
		support = new DataTableTestSupport();
		factory = support.getDataTableFactory();
		rand = new Random(15);
	}

	@Before
	public void setUp() {
		eventHelper = support.getDummyCyEventHelper(); 
		table = factory.createTable(Integer.toString( rand.nextInt(10000) ), "SUID", Long.class, false, true);
		table2 = factory.createTable(Integer.toString( rand.nextInt(10000) ), "SUID", Long.class, false, true);
		attrs = table.getRow(1l);
	}

	@After
	public void tearDown() {
		table = null;
		attrs = null;
	}
}
