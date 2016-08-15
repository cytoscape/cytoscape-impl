package org.cytoscape.model.subnetwork;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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
import org.cytoscape.model.AbstractCyTableManagerTest;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.After;
import org.junit.Before;


/**
 * The purpose of this test is to validate that everything that holds true
 * for CyNetworks in CyTableManager also holds true for CySubNetworks!
 */
public class CySubNetworkCyTableManagerTest extends AbstractCyTableManagerTest {
	
	@Before
	public void setUp() {
		final CyEventHelper eh = new DummyCyEventHelper();
		final CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(eh));

		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eh);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		
		networkTableMgr = new CyNetworkTableManagerImpl();
		networkManager = new CyNetworkManagerImpl(serviceRegistrar);
		mgr = new CyTableManagerImpl(eh, networkTableMgr, networkManager, compiler);
		
		final Interpreter interpreter = new InterpreterImpl();
		CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eh, interpreter, serviceRegistrar);
		CyRootNetworkImpl baseNet = new CyRootNetworkImpl(eh, (CyTableManagerImpl) mgr, networkTableMgr, tableFactory,
				serviceRegistrar, true, SavePolicy.DO_NOT_SAVE);
		// This is a different subnetwork and not "baseNetwork" in ArrayGraph.
		goodNetwork = baseNet.addSubNetwork(); 
		
		globalTable = tableFactory.createTable("test table", CyIdentifiable.SUID, Long.class, true, true);
		
		networkManager.addNetwork(goodNetwork);
		
		assertNotNull(globalTable);
		assertNotNull(goodNetwork);

		assertEquals(1, networkManager.getNetworkSet().size());
	}

	@After
	public void tearDown() {
		mgr = null;
		goodNetwork = null;
	}
}
