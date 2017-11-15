package org.cytoscape.task.internal.title;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class EditNetworkTitleTaskTest {

	@Mock CyNetworkManager netMgr;
	@Mock CyNetworkNaming netNaming;
	@Mock UndoSupport undoSupport;
	@Mock CyServiceRegistrar serviceRegistrar;
	@Mock private TaskMonitor tm;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);

		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(netNaming);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
	}
	
	@Test
	public void testRun() throws Exception {
		CyNetwork net = mock(CyNetwork.class);
		CyRow r1 = mock(CyRow.class);
		when(net.getRow(net)).thenReturn(r1);
		when(r1.get("name", String.class)).thenReturn("title");

		EditNetworkTitleTask t = new EditNetworkTitleTask(net, serviceRegistrar);
		t.run(tm);

		verify(r1, times(1)).set("name", "title");
	}
}
