package org.cytoscape.task.internal.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class NewNetworkSelectedNodesOnlyTaskFactoryTest {
	
	@Test
	public void testObserver() throws Exception {
		var viewSupport = new NetworkViewTestSupport();
		var networkSupport = new NetworkTestSupport();
		
		var appMgr = mock(CyApplicationManager.class);
		var networkFactory = networkSupport.getNetworkFactory();
		var undoSupport = mock(UndoSupport.class);
		var rootNetMgr = new CyRootNetworkManagerImpl();
		var netViewFactory = viewSupport.getNetworkViewFactory();
		var netMgr = mock(CyNetworkManager.class);
		var netViewMgr = mock(CyNetworkViewManager.class);
		var namingUtil = mock(CyNetworkNaming.class);
		var visMapMgr = mock(VisualMappingManager.class);
		var eventHelper = mock(CyEventHelper.class);
		var groupMgr = mock(CyGroupManager.class);
		var tableViewManager = mock(CyTableViewManager.class);
		var renderingEngineMgr = mock(RenderingEngineManager.class);
		
		var defLayout = mock(CyLayoutAlgorithm.class);
		var layoutMgr = mock(CyLayoutAlgorithmManager.class);
		when(layoutMgr.getDefaultLayout()).thenReturn(defLayout);
		
		when(renderingEngineMgr.getRenderingEngines(any(View.class))).thenReturn(Collections.emptyList());
		
		var serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(networkFactory);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(netViewFactory);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineMgr);
		when(serviceRegistrar.getService(CyGroupManager.class)).thenReturn(groupMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visMapMgr);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
        when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
        when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
        when(serviceRegistrar.getService(CyTableViewManager.class)).thenReturn(tableViewManager);
		
		var factory = new NewNetworkSelectedNodesOnlyTaskFactoryImpl(serviceRegistrar);

		var network = networkFactory.createNetwork();
		var node = network.addNode();
		network.getRow(node).set(CyNetwork.SELECTED, true);
		
		var observer = mock(TaskObserver.class);
		var taskMonitor = mock(TaskMonitor.class);
		var iterator = factory.createTaskIterator(network);
		
		while (iterator.hasNext()) {
			var t = iterator.next();
			
			t.run(taskMonitor);
			
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		verify(observer, times(1)).taskFinished(any(ObservableTask.class));
	}
}
