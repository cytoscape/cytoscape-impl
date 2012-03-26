/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CyNetworkTableManagerTest extends AbstractCyNetworkTableManagerTest {
		
	private CyNetworkFactory networkFactory;
	private CyTableFactory tableFactory;
	private CyTableManagerImpl tableManager;
	
	private CyNetworkManager networkManager;
	
	private CyEventHelper eh = new DummyCyEventHelper();
	private  CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);

	@Before
	public void setUp() {
		super.setUp();
		
		final Interpreter interpreter = new InterpreterImpl();
		this.networkManager = new CyNetworkManagerImpl(eh);
		this.tableManager = new CyTableManagerImpl(eh, mgr, networkManager);
		this.tableFactory = new CyTableFactoryImpl(eh, interpreter, serviceRegistrar);
		
		this.mgr = new CyNetworkTableManagerImpl();
		this.networkFactory = new CyNetworkFactoryImpl(eh, tableManager, mgr, tableFactory, serviceRegistrar);

		goodNetwork = networkFactory.createNetwork();
	}

	@After
	public void tearDown() {
		mgr = null;
		goodNetwork = null;
	}
	
	/*
	 * Once network key has no strong reference, it should be marked for GC.
	 */
	@Test
	public void testWeakReferences() throws Exception {
		mgr = new CyNetworkTableManagerImpl();
		
		final CyNetwork[] keys = new CyNetwork[2];
		
		final String tableName1 = "sample1";
		final String tableName2 = "sample2";
		
		// Store keys in external array
		keys[0] = mock(CyNetwork.class);
		keys[1] = mock(CyNetwork.class);
		
		CyTable sampleT1 = tableFactory.createTable(tableName1, CyNetwork.NAME, String.class, true, true);
		CyTable sampleT2 = tableFactory.createTable(tableName2, CyNetwork.NAME, String.class, true, true);
		
		mgr.setTable(keys[0], CyNetwork.class, tableName1, sampleT1);
		mgr.setTable(keys[1], CyNetwork.class, tableName2, sampleT2);
		
		assertNotNull(mgr.getTable(keys[0], CyNetwork.class, tableName1));
		assertNotNull(mgr.getTable(keys[1], CyNetwork.class, tableName2));
		
		assertEquals(2, mgr.getNetworkSet().size());
		
		// remove the key and run GC
		keys[0] = null;
		System.gc();
		Thread.sleep(100);
		
		assertEquals(1, mgr.getNetworkSet().size());

		// remove the key and run GC again
		keys[1] = null;
		System.gc();
		Thread.sleep(100);
		
		assertEquals(0, mgr.getNetworkSet().size());
	}
	
	@Test
	public void testNoneDefaultTables() throws Exception {
		final String noneDefTableName = "external";
		final String extraColName = "new column";
		final Integer testValue = 22;
		
		CyTable extTable = tableFactory.createTable(noneDefTableName, CyIdentifiable.SUID, Long.class, true, true);
		extTable.createColumn(extraColName, Integer.class, false);
		mgr.setTable(goodNetwork, CyNetwork.class, noneDefTableName, extTable);
		
		final CyRow extRow = goodNetwork.getRow(goodNetwork, noneDefTableName);
		assertNotNull(extRow);
		
		extRow.set(extraColName, testValue);
		
		assertEquals(testValue, goodNetwork.getRow(goodNetwork, noneDefTableName).get(extraColName, Integer.class));
	}
}
