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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;


public class CyTableManagerTest extends AbstractCyTableManagerTest {
	
	private CyEventHelper eventHelper = new DummyCyEventHelper();
	

	@Before
	public void setUp() {
		networkTableMgr = new CyNetworkTableManagerImpl();
		networkManager = new CyNetworkManagerImpl(eventHelper);
		mgr = new CyTableManagerImpl(eventHelper, networkTableMgr, networkManager);
		
		assertNotNull(mgr);
		assertEquals(0, mgr.getAllTables(true).size());

		final Interpreter interpreter = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper, interpreter, serviceRegistrar);
		goodNetwork = new CyRootNetworkImpl(eventHelper, (CyTableManagerImpl) mgr, networkTableMgr, tableFactory, serviceRegistrar, true)
				.getBaseNetwork();
		networkManager.addNetwork(goodNetwork);
		
		globalTable = tableFactory.createTable("test table", CyIdentifiable.SUID, Long.class, true, true);
		
		assertNotNull(globalTable);
		assertNotNull(goodNetwork);

		assertEquals(9, mgr.getAllTables(true).size());
		assertEquals(1, networkManager.getNetworkSet().size());
	}

	@After
	public void tearDown() {
		mgr = null;
		goodNetwork = null;
	}

 
	@Test
	public void immutableTableTest() {
		mgr.addTable(goodNetwork.getDefaultNodeTable());
		boolean exceptionWasThrown = false;
		try {
			mgr.deleteTable(goodNetwork.getDefaultNodeTable().getSUID());
		} catch (IllegalArgumentException e) {
			exceptionWasThrown = true;
		}
		assertTrue(exceptionWasThrown);	
	}

	@Test
	public void tableWithVirtColumnDeletionTest() {
		final Interpreter interpreter = new InterpreterImpl();
		CyTable table = new CyTableImpl("homer", CyIdentifiable.SUID, Long.class, true, true, SavePolicy.SESSION_FILE,
				eventHelper, interpreter, 1000);
		CyTable table2 = new CyTableImpl("marge", CyIdentifiable.SUID, Long.class, true, true, SavePolicy.SESSION_FILE,
				eventHelper, interpreter, 1000);

		table.createColumn("x", Long.class, false);
		CyColumn column = table.getColumn("x");
		assertNull(column.getVirtualColumnInfo().getSourceTable());
		table2.createListColumn("b", Boolean.class, false);
		table.addVirtualColumn("b1", "b", table2, "x", true);

		mgr.addTable(table2);
		boolean caughtException = false;
		try {
			mgr.deleteTable(table2.getSUID());
		} catch (IllegalArgumentException e) {
			caughtException = true;
		}
		assertTrue(caughtException);
		table2.deleteColumn("b1");
		mgr.deleteTable(table.getSUID());
	}
}
