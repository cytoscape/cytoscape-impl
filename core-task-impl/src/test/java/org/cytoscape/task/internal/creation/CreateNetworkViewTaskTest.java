package org.cytoscape.task.internal.creation;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateNetworkViewTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private CyNetworkViewFactory viewFactory = viewSupport.getNetworkViewFactory();

	@Mock private CyNetworkViewManager networkViewManager;
	@Mock private UndoSupport undoSupport;
	@Mock private TaskMonitor tm;
	@Mock private CyEventHelper eventHelper;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCreateNetworkViewTask() throws Exception {
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(support.getNetwork());
		final CreateNetworkViewTask task = new CreateNetworkViewTask(undoSupport, networks, viewFactory,
				networkViewManager, null, eventHelper);

		task.run(tm);
		verify(networkViewManager, times(1)).addNetworkView(any(CyNetworkView.class));
	}
	
	@Test
	public void testShouldNotCreateMultipleViewsPerNetwork() throws Exception {
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		final CyNetworkView view = viewSupport.getNetworkView();
		networks.add(support.getNetwork());
		networks.add(view.getModel());
		when(networkViewManager.getNetworkViews(view.getModel())).thenReturn(Arrays.asList(new CyNetworkView[]{ view }));
		
		final CreateNetworkViewTask task = new CreateNetworkViewTask(undoSupport, networks, viewFactory,
				networkViewManager, null, eventHelper);
		
		task.run(tm);
		verify(networkViewManager, times(1)).addNetworkView(any(CyNetworkView.class));
	}

}
