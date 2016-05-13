package org.cytoscape.task.internal.creation;

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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateNetworkViewTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkViewFactory viewFactory = viewSupport.getNetworkViewFactory();

	@Mock private CyNetworkViewManager networkViewManager;
	@Mock private RenderingEngineManager renderingEngineManager;
	@Mock private UndoSupport undoSupport;
	@Mock private TaskMonitor tm;
	@Mock private CyEventHelper eventHelper;
	@Mock private VisualMappingManager vmm;
	@Mock private CyApplicationManager appManager;
	@Mock private VisualStyle currentStyle;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(vmm.getCurrentVisualStyle()).thenReturn(currentStyle);
		when(renderingEngineManager.getRenderingEngines(any(View.class))).thenReturn(Collections.EMPTY_LIST);
	}
	
	@Test
	public void testCreateNetworkViewTask() throws Exception {
		final Set<CyNetwork> networks = new HashSet<>();
		networks.add(support.getNetwork());
		final CreateNetworkViewTask task = new CreateNetworkViewTask(undoSupport, networks, viewFactory,
				networkViewManager, null, eventHelper, vmm, renderingEngineManager, appManager, null);

		task.run(tm);
		verify(networkViewManager, times(1)).addNetworkView(any(CyNetworkView.class), eq(false));
	}

	@Test
	public void testShouldCreateMultipleViewsPerNetwork() throws Exception {
		final Set<CyNetwork> networks = new HashSet<>();
		final CyNetworkView view = viewSupport.getNetworkView();
		networks.add(support.getNetwork());
		networks.add(view.getModel());
		when(networkViewManager.getNetworkViews(view.getModel())).thenReturn(Arrays.asList(new CyNetworkView[]{ view }));
		
		final CreateNetworkViewTask task = new CreateNetworkViewTask(undoSupport, networks, viewFactory,
				networkViewManager, null, eventHelper, vmm, renderingEngineManager, appManager, null);
		
		task.run(tm);
		verify(networkViewManager, times(2)).addNetworkView(any(CyNetworkView.class), eq(false));
		verify(vmm, times(2)).setVisualStyle(eq(currentStyle), any(CyNetworkView.class));
	}
}
