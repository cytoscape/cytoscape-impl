package org.cytoscape.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyTableManagerTest extends AbstractCyTableManagerTest {
	
	private CyEventHelper eventHelper = new DummyCyEventHelper();
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
	private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	private EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));

	@Before
	public void setUp() {
		final Interpreter interpreter = new InterpreterImpl();
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		when(serviceRegistrar.getService(Interpreter.class)).thenReturn(interpreter);
		
		networkTableMgr = new CyNetworkTableManagerImpl();
		networkManager = new CyNetworkManagerImpl(serviceRegistrar);
		mgr = new CyTableManagerImpl(networkTableMgr, networkManager, serviceRegistrar);
		
		assertNotNull(mgr);
		assertEquals(0, mgr.getAllTables(true).size());

		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper, serviceRegistrar);
		goodNetwork = new CyRootNetworkImpl(eventHelper, (CyTableManagerImpl) mgr, networkTableMgr, tableFactory,
				serviceRegistrar, true, SavePolicy.DO_NOT_SAVE).getBaseNetwork();
		networkManager.addNetwork(goodNetwork);
		
		globalTable = tableFactory.createTable("test table", CyIdentifiable.SUID, Long.class, true, true);
		
		assertNotNull(globalTable);
		assertNotNull(goodNetwork);

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
