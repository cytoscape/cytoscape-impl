package org.cytoscape.task.internal.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;
import org.mockito.Mockito;

public class NewNetworkSelectedNodesEdgesTaskFactoryTest {
	
	@Test
	public void testObserver() throws Exception {
		NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
		NetworkTestSupport networkSupport = new NetworkTestSupport();

		CyApplicationManager appMgr = Mockito.mock(CyApplicationManager.class);
		CyNetworkFactory netFactory = networkSupport.getNetworkFactory();
		UndoSupport undoSupport = mock(UndoSupport.class);
		CyRootNetworkManager rootNetMgr = new CyRootNetworkManagerImpl();
		CyNetworkManager netMgr = mock(CyNetworkManager.class);
		CyNetworkViewManager netViewMgr = mock(CyNetworkViewManager.class);
		CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
		VisualMappingManager visMapMgr = mock(VisualMappingManager.class);
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		CyGroupManager groupMgr = mock(CyGroupManager.class);
		RenderingEngineManager renderingEngineMgr = mock(RenderingEngineManager.class);
		
		CyLayoutAlgorithm defLayout = mock(CyLayoutAlgorithm.class);
		CyLayoutAlgorithmManager layoutMgr = mock(CyLayoutAlgorithmManager.class);
		when(layoutMgr.getDefaultLayout()).thenReturn(defLayout);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(viewSupport.getNetworkViewFactory());
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineMgr);
		when(serviceRegistrar.getService(CyGroupManager.class)).thenReturn(groupMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visMapMgr);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
        when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
		
		var factory = new NewNetworkSelectedNodesEdgesTaskFactoryImpl(serviceRegistrar);
		
		CyNetwork network = netFactory.createNetwork();
		CyNode node = network.addNode();
		network.getRow(node).set(CyNetwork.SELECTED, true);
		
		TaskObserver observer = mock(TaskObserver.class);
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		TaskIterator iterator = factory.createTaskIterator(network);
		
		while (iterator.hasNext()) {
			Task t = iterator.next();
			t.run(taskMonitor);
			
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		verify(observer, times(1)).taskFinished(any(ObservableTask.class));
	}
}
