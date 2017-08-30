package org.cytoscape.task.internal.vizmap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class ApplyVisualStyleTaskTest {

	private CyServiceRegistrar serviceRegistrar;
	
	@Before
	public void setUp() throws Exception {
		VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() throws Exception {
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		
		TaskMonitor tm = mock(TaskMonitor.class);
		
		final CyNetworkView view = nvts.getNetworkView();
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(view);
		
		ApplyVisualStyleTask task = new ApplyVisualStyleTask(views, serviceRegistrar);
		
		final List<VisualStyle> vsList = new ArrayList<VisualStyle>();
		VisualStyle style1 = mock(VisualStyle.class);
		vsList.add(style1);
		task.styles = new ListSingleSelection<VisualStyle>(vsList);
		task.styles.setSelectedValue(style1);
		task.run(tm);
		
		verify(style1, times(1)).apply(view);
	}
}
