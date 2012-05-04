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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Task;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class SelectAllTaskTest extends AbstractSelectTaskTester {
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		final CyTable nodeTable = mock(CyTable.class);
		when(net.getDefaultNodeTable()).thenReturn(nodeTable);
		final CyTable edgeTable = mock(CyTable.class);
		when(net.getDefaultEdgeTable()).thenReturn(edgeTable);
		UndoSupport undoSupport = mock(UndoSupport.class);

		Set<CyRow> deselectedNodes = new HashSet<CyRow>();
		deselectedNodes.add(r3);
		deselectedNodes.add(r4);
		when(nodeTable.getMatchingRows(CyNetwork.SELECTED, false)).thenReturn(deselectedNodes);
		
		when (r3.get(CyNetwork.SUID, Long.class)).thenReturn(3L);
		when (net.getNode(3L)).thenReturn(e3);
		when (r4.get(CyNetwork.SUID, Long.class)).thenReturn(4L);
		when (net.getNode(4L)).thenReturn(e4);

		Set<CyRow> deselectedEdges = new HashSet<CyRow>();
		deselectedEdges.add(r1);
		deselectedEdges.add(r2);
		when(edgeTable.getMatchingRows(CyNetwork.SELECTED, false)).thenReturn(deselectedEdges);
		
		when (r1.get(CyNetwork.SUID, Long.class)).thenReturn(1L);
		when (net.getEdge(1L)).thenReturn(e1);
		when (r2.get(CyNetwork.SUID, Long.class)).thenReturn(2L);
		when (net.getEdge(2L)).thenReturn(e2);

		// run the task
		Task t = new SelectAllTask(undoSupport, net, networkViewManager, eventHelper);
		t.run(tm);

		// check that the expected rows were set
		verify(r1, times(1)).set("selected", true);
		verify(r2, times(1)).set("selected", true);
		verify(r3, times(1)).set("selected", true);
		verify(r4, times(1)).set("selected", true);
	}
}
