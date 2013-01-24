package org.cytoscape.model;

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


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class TestCyNetworkFactory {
	
	@Mock private CyServiceRegistrar serviceRegistrar;
	private CyEventHelper eh;
	private CyTableManagerImpl tblMgr;
	private CyNetworkManager netMgr;
	private CyNetworkTableManager netTblMgr;
	private CyTableFactory tblFactory;
	private CyNetworkFactoryImpl netFactory;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		Interpreter interp = new InterpreterImpl();
		eh = new DummyCyEventHelper(false);
		netTblMgr = new CyNetworkTableManagerImpl();
		netMgr = new CyNetworkManagerImpl(eh);
		tblMgr = new CyTableManagerImpl(eh, netTblMgr, netMgr);
		tblFactory = new CyTableFactoryImpl(eh, interp, serviceRegistrar);
		
		netFactory = new CyNetworkFactoryImpl(eh, tblMgr, netTblMgr, tblFactory, serviceRegistrar);
	}

	@Test
	public void testCreatePublicNetwork() throws Exception {
		CyNetwork n = netFactory.createNetwork();
		assertNetworkTablesVisibility(n, true);
		assertEquals(SavePolicy.SESSION_FILE, n.getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, ((CySubNetwork)n).getRootNetwork().getSavePolicy());
	}
	
	@Test
	public void testCreatePublicNetworkWithSavePolicy() throws Exception {
		CyNetwork n = netFactory.createNetwork(SavePolicy.DO_NOT_SAVE);
		assertNetworkTablesVisibility(n, true);
		assertEquals(SavePolicy.DO_NOT_SAVE, n.getSavePolicy());
		assertEquals(SavePolicy.DO_NOT_SAVE, ((CySubNetwork)n).getRootNetwork().getSavePolicy());
	}
	
	@Test
	public void testCreateNetworkWithPrivateTables() throws Exception {
		CyNetwork n = netFactory.createNetworkWithPrivateTables();
		assertNetworkTablesVisibility(n, false);
		assertEquals(SavePolicy.SESSION_FILE, n.getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, ((CySubNetwork)n).getRootNetwork().getSavePolicy());
	}
	
	@Test
	public void testCreateNetworkWithPrivateTablesAndSavePolicy() throws Exception {
		CyNetwork n = netFactory.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		assertNetworkTablesVisibility(n, false);
		assertEquals(SavePolicy.DO_NOT_SAVE, n.getSavePolicy());
		assertEquals(SavePolicy.DO_NOT_SAVE, ((CySubNetwork)n).getRootNetwork().getSavePolicy());
	}
	
	public static CyNetwork getInstance() {
		return getPublicRootInstance().getBaseNetwork();
	}

	public static CyNetwork getInstanceWithPrivateTables() {
		return getPrivateRootInstance().getBaseNetwork();
	}

	public static CyRootNetwork getPublicRootInstance() {	
		return getPublicRootInstance(new DummyCyEventHelper());
	}
	
	public static CyRootNetwork getPublicRootInstance(SavePolicy policy) {	
		return getPublicRootInstance(new DummyCyEventHelper(), policy);
	}

	public static CyRootNetwork getPublicRootInstance(DummyCyEventHelper deh) {	
		return getPublicRootInstance(deh, SavePolicy.SESSION_FILE);
	}
	
	public static CyRootNetwork getPublicRootInstance(DummyCyEventHelper deh, SavePolicy policy) {	
		final CyNetworkTableManagerImpl ntm = new CyNetworkTableManagerImpl();
		final CyTableManagerImpl tm = new CyTableManagerImpl(deh, ntm, new CyNetworkManagerImpl(deh));
		
		final Interpreter interp = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(deh, interp, serviceRegistrar);
		return new CyRootNetworkImpl(deh, tm, ntm, tableFactory, serviceRegistrar, true, policy);
	}

	public static CyRootNetwork getPrivateRootInstance() {	
		return getPrivateRootInstance(SavePolicy.SESSION_FILE); 
	}
	
	public static CyRootNetwork getPrivateRootInstance(SavePolicy policy) {	
		DummyCyEventHelper deh = new DummyCyEventHelper();
		CyNetworkTableManagerImpl ntm = new CyNetworkTableManagerImpl();
		CyTableManagerImpl tm = new CyTableManagerImpl(deh, ntm, new CyNetworkManagerImpl(deh));
		Interpreter interp = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		CyRootNetworkImpl ar =
				new CyRootNetworkImpl(deh, tm, ntm, new CyTableFactoryImpl(deh, interp, serviceRegistrar),
						serviceRegistrar, false, policy);
		return ar; 
	}
	
	private void assertNetworkTablesVisibility(CyNetwork n, boolean isPublic) {
		assertEquals(isPublic, n.getDefaultNetworkTable().isPublic());
		assertEquals(isPublic, n.getDefaultNodeTable().isPublic());
		assertEquals(isPublic, n.getDefaultEdgeTable().isPublic());
	}
}

