/*
 Copyright (c) 2008, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.subnetwork;


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
