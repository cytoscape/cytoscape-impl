package org.cytoscape.task.internal.destruction;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DestroyNetworkViewTaskTest {
	
	private final NetworkViewTestSupport support = new NetworkViewTestSupport();
	
	private CyNetworkViewManager viewManager;
	private TaskMonitor tm = mock(TaskMonitor.class);
	
	@Before
	public void setUp() throws Exception {
		viewManager = mock(CyNetworkViewManager.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDestroyNetworkTask() throws Exception {
		final CyNetworkView view1 = support.getNetworkView();
		final CyNetworkView view2 = support.getNetworkView();
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(view1);
		views.add(view2);
		
		final DestroyNetworkViewTask task = new DestroyNetworkViewTask(views, viewManager);
		task.run(tm);
		
		verify(viewManager, times(1)).destroyNetworkView(view1);
		verify(viewManager, times(1)).destroyNetworkView(view2);
	}
	
}
