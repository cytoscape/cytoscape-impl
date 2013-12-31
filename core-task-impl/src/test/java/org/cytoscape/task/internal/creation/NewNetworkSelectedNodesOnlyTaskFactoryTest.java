package org.cytoscape.task.internal.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.cytoscape.session.CyNetworkNaming;
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

public class NewNetworkSelectedNodesOnlyTaskFactoryTest {
	@Test
	public void testObserver() throws Exception {
		NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
		NetworkTestSupport networkSupport = new NetworkTestSupport();
		CyNetworkFactory networkFactory = networkSupport.getNetworkFactory();
		
		UndoSupport undoSupport = mock(UndoSupport.class);
		CyRootNetworkManager crnf = new CyRootNetworkManagerImpl();
		CyNetworkViewFactory cnvf = viewSupport.getNetworkViewFactory();
		CyNetworkManager netmgr = mock(CyNetworkManager.class);
		CyNetworkViewManager networkViewManager = mock(CyNetworkViewManager.class);
		CyNetworkNaming naming = mock(CyNetworkNaming.class);
		VisualMappingManager vmm = mock(VisualMappingManager.class);
		CyApplicationManager appManager = mock(CyApplicationManager.class);
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		CyGroupManager groupMgr = mock(CyGroupManager.class);
		RenderingEngineManager renderingEngineMgr = mock(RenderingEngineManager.class);
		NewNetworkSelectedNodesOnlyTaskFactoryImpl factory = 
				new NewNetworkSelectedNodesOnlyTaskFactoryImpl(undoSupport, crnf, cnvf, netmgr, networkViewManager,
						naming, vmm, appManager, eventHelper, groupMgr, renderingEngineMgr);
		
		CyNetwork network = networkFactory.createNetwork();
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
