package org.cytoscape.task.internal.select;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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
