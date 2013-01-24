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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewEmptyNetworkTaskTest {

	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();

	private CyNetworkFactory cnf = support.getNetworkFactory();
	private CyNetworkViewFactory cnvf = viewSupport.getNetworkViewFactory();
	private CyRootNetworkManager cyroot = support.getRootNetworkFactory();
	
	private CyApplicationManager appManager = mock(CyApplicationManager.class);
	
	@Mock
	private CyNetworkManager netMgr;
	@Mock
	private CyNetworkViewManager netViewMgr;
	@Mock
	private CyNetworkNaming namingUtil;
	@Mock
	private VisualMappingManager vmm;
	@Mock
	private VisualStyle currentStyle;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		when(vmm.getCurrentVisualStyle()).thenReturn(currentStyle);
	}

	@Test
	public void testNewEmptyNetworkTask() throws Exception {
		final NewEmptyNetworkTask task = new NewEmptyNetworkTask(cnf, cnvf, netMgr, netViewMgr, namingUtil, vmm, cyroot, appManager);
		final TaskMonitor taskMonitor = mock(TaskMonitor.class);
		task.run(taskMonitor);

		verify(netMgr, times(1)).addNetwork(any(CyNetwork.class));
		verify(netViewMgr, times(1)).addNetworkView(any(CyNetworkView.class));
		verify(vmm, times(1)).setVisualStyle(eq(currentStyle), any(CyNetworkView.class));
	}
}
