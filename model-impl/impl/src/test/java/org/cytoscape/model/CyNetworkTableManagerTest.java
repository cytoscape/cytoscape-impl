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
import org.cytoscape.model.Identifiable;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.internal.ArrayGraph;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;


public class CyNetworkTableManagerTest extends AbstractCyNetworkTableManagerTest {
	CyTableManagerImpl mgrImpl;
	CyNetworkTableManagerImpl networkTableMgr;

	@Before
	public void setUp() {
		super.setUp();
		CyEventHelper eh = new DummyCyEventHelper();
		mgrImpl = new CyTableManagerImpl(eh, networkTableMgr, null);
		networkTableMgr = new CyNetworkTableManagerImpl();
		mgr = networkTableMgr;
		final Interpreter interpreter = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		goodNetwork =
			new ArrayGraph(eh, mgrImpl, networkTableMgr,
			               new CyTableFactoryImpl(eh, interpreter, serviceRegistrar),
			               serviceRegistrar, true).getBaseNetwork();
	}

	@After
	public void tearDown() {
		mgr = null;
		goodNetwork = null;
	}
}
