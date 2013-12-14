package org.cytoscape.task.internal.creation;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.utils.SessionUtils;
import org.cytoscape.view.model.CyNetworkView;
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
		
		CyNetworkManager networkMgr = Mockito.mock(CyNetworkManager.class);
		CyNetworkViewManager networkViewMgr = Mockito.mock(CyNetworkViewManager.class);
		VisualMappingManager vmm = Mockito.mock(VisualMappingManager.class);
		CyNetworkFactory netFactory = networkSupport.getNetworkFactory();
		CyNetworkViewFactory netViewFactory = viewSupport.getNetworkViewFactory();
		CyNetworkNaming naming = Mockito.mock(CyNetworkNaming.class);
		CyApplicationManager appMgr = Mockito.mock(CyApplicationManager.class);
		CyNetworkTableManager netTableMgr = Mockito.mock(CyNetworkTableManager.class);
		CyRootNetworkManager rootNetMgr = Mockito.mock(CyRootNetworkManager.class);
		CyGroupManager groupMgr = Mockito.mock(CyGroupManager.class);
		CyGroupFactory groupFactory = Mockito.mock(CyGroupFactory.class);
		RenderingEngineManager renderingEngineMgr = Mockito.mock(RenderingEngineManager.class);
		CyNetworkViewFactory nullNetworkViewFactory = new NullCyNetworkViewFactory();
		CloneNetworkTaskFactoryImpl factory = new CloneNetworkTaskFactoryImpl(networkMgr, networkViewMgr, vmm, 
				netFactory, netViewFactory, naming, appMgr, netTableMgr, rootNetMgr, groupMgr, groupFactory, 
				renderingEngineMgr, nullNetworkViewFactory, new SessionUtils());
		
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
