package org.cytoscape.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
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

public class CyNetworkTableManagerTest extends AbstractCyNetworkTableManagerTest {
		
	private CyNetworkFactory networkFactory;
	private CyTableFactory tableFactory;
	private CyTableManagerImpl tableManager;
	
	private CyNetworkManager networkManager;
	
	private CyEventHelper eh = new DummyCyEventHelper();
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
	private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	private EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));


	@Before
	public void setUp() {
		super.setUp();
		
		final Interpreter interpreter = new InterpreterImpl();
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eh);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		when(serviceRegistrar.getService(Interpreter.class)).thenReturn(interpreter);
		
		this.mgr = new CyNetworkTableManagerImpl();
		this.networkManager = new CyNetworkManagerImpl(serviceRegistrar);
		this.tableManager = new CyTableManagerImpl(mgr, networkManager, serviceRegistrar);
		this.tableFactory = new CyTableFactoryImpl(eh, serviceRegistrar);
		
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
//	@Test
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
