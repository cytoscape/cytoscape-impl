package org.cytoscape.task.internal.network;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class DestroyNetworkTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	
	private CyServiceRegistrar serviceRegistrar;
	private CyNetworkManager netMgr;
	private TaskMonitor tm = mock(TaskMonitor.class);
	
	@Before
	public void setUp() throws Exception {
		netMgr = mock(CyNetworkManager.class);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDestroyNetworkTask() throws Exception {
		final CyNetwork network1 = support.getNetwork();
		final CyNetwork network2 = support.getNetwork();
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(network1);
		networks.add(network2);
		
		final DestroyNetworkTask task = new DestroyNetworkTask(networks, serviceRegistrar);
		task.run(tm);
		
		verify(netMgr, times(1)).destroyNetwork(network1);
		verify(netMgr, times(1)).destroyNetwork(network2);
	}
}
