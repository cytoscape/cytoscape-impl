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
