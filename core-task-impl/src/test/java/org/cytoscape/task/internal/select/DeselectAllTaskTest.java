package org.cytoscape.task.internal.select;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class DeselectAllTaskTest extends AbstractSelectTaskTester {
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		var selectedEdges = new HashSet<Long>();
		selectedEdges.add(r1.get(CyNetwork.SUID, Long.class));
		selectedEdges.add(r2.get(CyNetwork.SUID, Long.class));
		when(edgeTable.getMatchingKeys(CyNetwork.SELECTED, true, Long.class)).thenReturn(selectedEdges);
		
		var selectedNodes = new HashSet<Long>();
		selectedNodes.add(r3.get(CyNetwork.SUID, Long.class));
		selectedNodes.add(r4.get(CyNetwork.SUID, Long.class));
		when(nodeTable.getMatchingKeys(CyNetwork.SELECTED, true, Long.class)).thenReturn(selectedNodes);
		
		// run the task
		var t = new DeselectAllTask(net, serviceRegistrar);
		t.run(tm);

		// check that the expected rows were set
		verify(r1, times(1)).set("selected", false);
		verify(r2, times(1)).set("selected", false);
		verify(r3, times(1)).set("selected", false);
		verify(r4, times(1)).set("selected", false);
		
		// check annotations have been deselected
		verify(a3, times(1)).setSelected(false);
		verify(a4, times(1)).setSelected(false);
	}
}
