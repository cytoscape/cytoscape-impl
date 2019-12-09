package org.cytoscape.task.internal.network;

import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.internal.NullCyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.junit.Test;
import org.mockito.Mockito;

public class CloneNetworkTaskFactoryTest {
	
	@Test
	public void testObserver() throws Exception {
		NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		
		var netMgr = Mockito.mock(CyNetworkManager.class);
		var netViewMgr = Mockito.mock(CyNetworkViewManager.class);
		var vmm = Mockito.mock(VisualMappingManager.class);
		var netFactory = networkSupport.getNetworkFactory();
		var netViewFactory = viewSupport.getNetworkViewFactory();
		var netNaming = Mockito.mock(CyNetworkNaming.class);
		var appMgr = Mockito.mock(CyApplicationManager.class);
		var netTableMgr = Mockito.mock(CyNetworkTableManager.class);
		var rootNetMgr = new CyRootNetworkManagerImpl();
		var groupMgr = Mockito.mock(CyGroupManager.class);
		var groupFactory = Mockito.mock(CyGroupFactory.class);
		var renderingEngineMgr = Mockito.mock(RenderingEngineManager.class);
		var nullNetViewFactory = new NullCyNetworkViewFactory();
		
		CyServiceRegistrar serviceRegistrar = Mockito.mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(CyNetworkTableManager.class)).thenReturn(netTableMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(netViewFactory);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(netNaming);
		when(serviceRegistrar.getService(CyGroupManager.class)).thenReturn(groupMgr);
		when(serviceRegistrar.getService(CyGroupFactory.class)).thenReturn(groupFactory);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineMgr);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)")).thenReturn(nullNetViewFactory);
		
		var factory = new CloneNetworkTaskFactoryImpl(serviceRegistrar);
		
		CyNetwork network = netFactory.createNetwork();
		TaskObserver observer = Mockito.mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(network);

		TaskMonitor taskMonitor = Mockito.mock(TaskMonitor.class);
		
		while (iterator.hasNext()) {
			Task t = iterator.next();
			t.run(taskMonitor);
			
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		Mockito.verify(observer, Mockito.times(1)).taskFinished(Mockito.any(ObservableTask.class));
	}
}
