package org.cytoscape.task.internal.destruction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DestroyNetworkViewTaskTest {
	
	private final NetworkViewTestSupport support = new NetworkViewTestSupport();
	
	private CyNetworkViewManager viewManager;

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
		task.run(null);
		
		verify(viewManager, times(1)).destroyNetworkView(view1);
		verify(viewManager, times(1)).destroyNetworkView(view2);
	}
	
}
