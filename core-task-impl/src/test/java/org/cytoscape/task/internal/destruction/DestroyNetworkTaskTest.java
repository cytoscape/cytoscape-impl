package org.cytoscape.task.internal.destruction;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DestroyNetworkTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	
	private CyNetworkManager netmgr;
	private TaskMonitor tm = mock(TaskMonitor.class);
	
	@Before
	public void setUp() throws Exception {
		netmgr = mock(CyNetworkManager.class);
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
		
		final DestroyNetworkTask task = new DestroyNetworkTask(networks, netmgr);
		task.run(tm);
		
		verify(netmgr, times(1)).destroyNetwork(network1);
		verify(netmgr, times(1)).destroyNetwork(network2);
	}
	
}
