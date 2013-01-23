package org.cytoscape.task.internal.hide;

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



import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkViewTaskTest;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class HideSelectedEdgesTaskTest extends AbstractNetworkViewTaskTest {
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkView view = viewSupport.getNetworkView();
	@Mock TaskMonitor tm;
	@Mock CyEventHelper eventHelper;
	@Mock UndoSupport undoSupport;
	@Mock VisualMappingManager vmMgr;
	
	CyEdge edge1;
	CyEdge edge2;
	CyEdge edge3;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		final VisualStyle vs = mock(VisualStyle.class);
		when(vmMgr.getVisualStyle(any(CyNetworkView.class))).thenReturn(vs);
		
		final CyNetwork network = view.getModel();
		final CyNode n1 = network.addNode();
		final CyNode n2 = network.addNode();
		final CyNode n3 = network.addNode();
		
		edge1 = view.getModel().addEdge(n1, n2, true);
		edge2 = network.addEdge(n1, n3, true);
		edge3 = network.addEdge(n2, n3, true);
		
		view.getModel().getRow(edge1).set(CyNetwork.SELECTED, true);
		view.getModel().getRow(edge3).set(CyNetwork.SELECTED, true);
	}
	
	@Test(expected=NullPointerException.class)
	public void testHideSelectedEdgesTask() throws Exception {
		final HideSelectedEdgesTask task = new HideSelectedEdgesTask(undoSupport, eventHelper, vmMgr, view);
		task.run(tm);
	}
}
