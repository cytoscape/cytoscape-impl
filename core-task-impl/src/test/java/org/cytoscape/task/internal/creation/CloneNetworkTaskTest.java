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

import static org.mockito.Mockito.mock;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;


public class CloneNetworkTaskTest {
	@Test
	public void runTest() {
		final CyNetworkManager netmgr = mock(CyNetworkManager.class);
		final CyNetworkViewManager networkViewManager = mock(CyNetworkViewManager.class);
		final CyNetworkViewFactory netViewFactory = mock(CyNetworkViewFactory.class);
		final RenderingEngineManager reManager = mock(RenderingEngineManager.class);
		final CyNetworkNaming naming = mock(CyNetworkNaming.class);
		final TaskMonitor tm = mock(TaskMonitor.class);
	}
}