package org.cytoscape.task.internal.select;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

public class AllFactoryTest {
	
	private CyNetworkManager netmgr;
	private CyNetworkViewManager networkViewManager;
	private CyNetwork net;
	private CyEventHelper eventHelper;
	private UndoSupport undoSupport;
	private TunableSetter ts;
	private CyServiceRegistrar serviceRegistrar;
	
	@Before
	public void setUp() throws Exception {
		net = mock(CyNetwork.class);
		netmgr = mock(CyNetworkManager.class);
		networkViewManager = mock(CyNetworkViewManager.class);
		eventHelper = mock(CyEventHelper.class);
		undoSupport = mock(UndoSupport.class);
		ts = mock(TunableSetter.class);
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netmgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(TunableSetter.class)).thenReturn(ts);
	}
	

	@Test
	public void testDeselectAllEdgesTaskFactory() {
		executeTest(new DeselectAllEdgesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testDeselectAllNodesTaskFactory() {
		executeTest(new DeselectAllNodesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testDeselectAllTaskFactory() {
		executeTest(new DeselectAllTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testInvertSelectedEdgesTaskFactory() {
		executeTest(new InvertSelectedEdgesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testInvertSelectedNodesTaskFactory() {
		executeTest(new InvertSelectedNodesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectAdjacentEdgesTaskFactory() {
		executeTest(new SelectAdjacentEdgesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectAllEdgesTaskFactory() {
		executeTest(new SelectAllEdgesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectAllNodesTaskFactory() {
		executeTest(new SelectAllNodesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectAllTaskFactory() {
		executeTest(new SelectAllTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectConnectedNodesTaskFactory() {
		executeTest(new SelectConnectedNodesTaskFactoryImpl(serviceRegistrar));
	}

	@Test
	public void testSelectFirstNeighborsTaskFactory() {
		executeTest(new SelectFirstNeighborsTaskFactoryImpl(CyEdge.Type.ANY, serviceRegistrar));
	}

	@Test
	public void testSelectFromFileListTaskFactory() {
		executeTest(new SelectFromFileListTaskFactoryImpl(serviceRegistrar));
	}

	private void executeTest(NetworkTaskFactory ntf) {
		TaskIterator ti = ntf.createTaskIterator(net);
		assertNotNull(ti);
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);
	}
}
