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

import org.cytoscape.equations.Interpreter;
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
import org.junit.After;
import org.junit.Before;


/**
 * The purpose of this test is to validate that everything that holds true
 * for CyNetworks in CyTableManager also holds true for CySubNetworks!
 */
public class CySubNetworkCyTableManagerTest extends AbstractCyTableManagerTest {
	
	@Before
	public void setUp() {
		CyEventHelper eh = new DummyCyEventHelper();
		
		networkTableMgr = new CyNetworkTableManagerImpl();
		networkManager = new CyNetworkManagerImpl(eh);
		mgr = new CyTableManagerImpl(eh, networkTableMgr, networkManager);
		
		final Interpreter interpreter = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
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
