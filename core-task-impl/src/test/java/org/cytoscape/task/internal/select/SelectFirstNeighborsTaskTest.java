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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Task;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class SelectFirstNeighborsTaskTest extends AbstractSelectTaskTester {
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		UndoSupport undoSupport = mock(UndoSupport.class);

		// more setup
		when(r3.get("selected", Boolean.class)).thenReturn(true);
		when(r4.get("selected", Boolean.class)).thenReturn(false);

		Set<Long> selectedNodes = new HashSet<>();
		selectedNodes.add(r3.get(CyNetwork.SUID, Long.class));
		when(nodeTable.getMatchingKeys(CyNetwork.SELECTED, true, Long.class)).thenReturn(selectedNodes);
		
		List<CyNode> nl = new ArrayList<CyNode>();
		nl.add(e4);
		when(net.getNeighborList(e3, CyEdge.Type.ANY)).thenReturn(nl);

		// run the task
		Task t = new SelectFirstNeighborsTask(undoSupport, net, networkViewManager, eventHelper, CyEdge.Type.ANY);
		t.run(tm);

		// check that the expected rows were set
		verify(r4, times(1)).set("selected", true);
	}
}
