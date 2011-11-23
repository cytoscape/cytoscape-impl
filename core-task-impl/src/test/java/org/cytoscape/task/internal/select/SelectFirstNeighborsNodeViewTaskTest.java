/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Task;
import org.junit.Before;
import org.junit.Test;

public class SelectFirstNeighborsNodeViewTaskTest extends AbstractSelectTaskTester {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		// more setup
		List<CyNode> nl = new ArrayList<CyNode>();
		nl.add(e4);
		when(net.getNeighborList(e3, CyEdge.Type.ANY)).thenReturn(nl);

		View<CyNode> nv = (View<CyNode>) mock(View.class);
		when(nv.getModel()).thenReturn(e3);
		CyNetworkView netView = mock(CyNetworkView.class);
		when(netView.getModel()).thenReturn(net);

		// run the task
		Task t = new SelectFirstNeighborsNodeViewTask(nv, netView, CyEdge.Type.ANY);
		t.run(tm);

		// check that the expected rows were set
		verify(r4, times(1)).set("selected", true);
	}

	@Test(expected = NullPointerException.class)
	public void testNullNetworkView() throws Exception {
		View<CyNode> nv = (View<CyNode>) mock(View.class);

		// run the task
		Task t = new SelectFirstNeighborsNodeViewTask(nv, null, CyEdge.Type.ANY);
		t.run(tm);
	}

	@Test(expected = NullPointerException.class)
	public void testNullNodeView() throws Exception {
		CyNetworkView netView = mock(CyNetworkView.class);

		// run the task
		Task t = new SelectFirstNeighborsNodeViewTask(null, netView, CyEdge.Type.ANY);
		t.run(tm);
	}
}
