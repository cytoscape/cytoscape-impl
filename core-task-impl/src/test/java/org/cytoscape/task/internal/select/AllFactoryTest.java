/*
  Copyright (c) 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.select;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class AllFactoryTest {
	CyNetworkManager netmgr;
	CyNetworkViewManager networkViewManager;
	CyNetwork net;
	CyTable edgeTable;
	CyEventHelper eventHelper;
	UndoSupport undoSupport;

	@Mock
	TunableSetter ts;
	
	@Before
	public void setUp() throws Exception {
		net = mock(CyNetwork.class);
		netmgr = mock(CyNetworkManager.class);
		networkViewManager = mock(CyNetworkViewManager.class);
		eventHelper = mock(CyEventHelper.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
	}

	@Test
	public void testDeselectAllEdgesTaskFactory() {
		executeTest(new DeselectAllEdgesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testDeselectAllNodesTaskFactory() {
		executeTest(new DeselectAllNodesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testDeselectAllTaskFactory() {
		executeTest(new DeselectAllTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testInvertSelectedEdgesTaskFactory() {
		executeTest(new InvertSelectedEdgesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testInvertSelectedNodesTaskFactory() {
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		executeTest(new InvertSelectedNodesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectAdjacentEdgesTaskFactory() {
		executeTest(new SelectAdjacentEdgesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectAllEdgesTaskFactory() {
		executeTest(new SelectAllEdgesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectAllNodesTaskFactory() {
		executeTest(new SelectAllNodesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectAllTaskFactory() {
		executeTest(new SelectAllTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectConnectedNodesTaskFactory() {
		executeTest(new SelectConnectedNodesTaskFactoryImpl(undoSupport, networkViewManager, eventHelper));
	}

	@Test
	public void testSelectFirstNeighborsTaskFactory() {
		executeTest(new SelectFirstNeighborsTaskFactoryImpl(undoSupport, networkViewManager, eventHelper, CyEdge.Type.ANY));
	}

	@Test
	public void testSelectFromFileListTaskFactory() {
		executeTest(new SelectFromFileListTaskFactoryImpl(undoSupport, networkViewManager, eventHelper, ts));
	}

	private void executeTest(NetworkTaskFactory ntf) {
		TaskIterator ti = ntf.createTaskIterator(net);
		assertNotNull(ti);
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);
	}
}
