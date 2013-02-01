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


import static org.mockito.Mockito.*;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
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


public class HideSelectedNodesTaskTest extends AbstractNetworkViewTaskTest {
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkView view = viewSupport.getNetworkView();
	@Mock TaskMonitor tm;
	@Mock CyEventHelper eventHelper;
	@Mock UndoSupport undoSupport;
	@Mock VisualMappingManager vmMgr;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		final VisualStyle vs = mock(VisualStyle.class);
		when(vmMgr.getVisualStyle(any(CyNetworkView.class))).thenReturn(vs);
	}
	
	@Test
	public void testHideSelectedEdgesTask() throws Exception {
		final HideSelectedNodesTask task = new HideSelectedNodesTask(undoSupport, eventHelper, vmMgr, view);
		task.run(tm);
	}

}
