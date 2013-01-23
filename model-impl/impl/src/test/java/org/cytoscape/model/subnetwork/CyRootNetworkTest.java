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


import static org.junit.Assert.*;

import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.TestCyNetworkFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CyRootNetworkTest extends AbstractCyRootNetworkTest {
	@Before
	public void setUp() {
		root = TestCyNetworkFactory.getPublicRootInstance();
		root2 = TestCyNetworkFactory.getPublicRootInstance();
	}

	@After
	public void tearDown() {
		root = null;
		root2 = null;
	}
	
	@Test
	public void testAddSubNetwork() {
		CySubNetwork sn1 = TestCyNetworkFactory.getPublicRootInstance(SavePolicy.SESSION_FILE).addSubNetwork();
		CySubNetwork sn2 = TestCyNetworkFactory.getPublicRootInstance(SavePolicy.DO_NOT_SAVE).addSubNetwork();
		assertEquals("Subnetwork inherits save policy from its root network", SavePolicy.SESSION_FILE, sn1.getSavePolicy());
		assertEquals("Subnetwork inherits save policy from its root network", SavePolicy.DO_NOT_SAVE, sn2.getSavePolicy());
	}
	
	@Test
	public void testAddSubNetworkWithDifferentSavePolicy() {
		CySubNetwork sn = TestCyNetworkFactory.getPublicRootInstance(SavePolicy.SESSION_FILE).addSubNetwork(SavePolicy.DO_NOT_SAVE);
		assertEquals("New subnetwork can have a different save policy", SavePolicy.DO_NOT_SAVE, sn.getSavePolicy());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddSubNetworkWithDifferentSavePolicyThrowsException() {
		TestCyNetworkFactory.getPublicRootInstance(SavePolicy.DO_NOT_SAVE).addSubNetwork(SavePolicy.SESSION_FILE);
	}
	
	@Test
	public void testAddSubNetworkWithNullSavePolicy() {
		CySubNetwork sn1 = TestCyNetworkFactory.getPublicRootInstance(SavePolicy.DO_NOT_SAVE).addSubNetwork(null);
		assertEquals(SavePolicy.DO_NOT_SAVE, sn1.getSavePolicy());
		CySubNetwork sn2 = TestCyNetworkFactory.getPublicRootInstance(SavePolicy.SESSION_FILE).addSubNetwork(null);
		assertEquals(SavePolicy.SESSION_FILE, sn2.getSavePolicy());
	}
}
