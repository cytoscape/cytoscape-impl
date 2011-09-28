package org.cytoscape.task.internal.creation;

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