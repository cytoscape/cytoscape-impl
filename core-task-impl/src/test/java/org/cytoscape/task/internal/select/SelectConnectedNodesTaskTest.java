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


public class SelectConnectedNodesTaskTest extends AbstractSelectTaskTester {
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		final CyTable nodeTable = mock(CyTable.class);
		when(net.getDefaultNodeTable()).thenReturn(nodeTable);
		UndoSupport undoSupport = mock(UndoSupport.class);

		// more setup
		when(r1.get("selected", Boolean.class)).thenReturn(true);
		when(r2.get("selected", Boolean.class)).thenReturn(false);

		final CyTable edgeTable = mock(CyTable.class);
		when(net.getDefaultEdgeTable()).thenReturn(edgeTable);
		
		Set<CyRow> selectedEdges = new HashSet<CyRow>();
		selectedEdges.add(r1);
		when(edgeTable.getMatchingRows(CyNetwork.SELECTED, true)).thenReturn(selectedEdges);

		when (r1.get(CyNetwork.SUID, Long.class)).thenReturn(1L);
		when (net.getEdge(1L)).thenReturn(e1);

		when(e1.getSource()).thenReturn(e3);
		when(e1.getTarget()).thenReturn(e4);

		// run the task
		Task t = new SelectConnectedNodesTask(undoSupport, net, networkViewManager, eventHelper);
		t.run(tm);

		// check that the expected rows were set
		verify(r3, times(1)).set("selected", true);
		verify(r4, times(1)).set("selected", true);
	}
}
