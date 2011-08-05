package org.cytoscape.view.presentation;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.internal.RenderingEngineManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RenderingEngineManagerTest {
	
	private RenderingEngineManager manager;

	@Before
	public void setUp() throws Exception {
		
		manager = new RenderingEngineManagerImpl();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRenderingEngineManagerImpl() {
		assertNotNull(manager);
	}

	@Test
	public void testGetRendringEngines() {
		
		// First, create mock view models.
		final CyNetworkView networkView1 = mock(CyNetworkView.class);
		final CyNetworkView networkView2 = mock(CyNetworkView.class);
				
		final RenderingEngine<CyNetwork> engine1 = mock(RenderingEngine.class);
		when(engine1.getViewModel()).thenReturn(networkView1);
		final RenderingEngine<CyNetwork> engine2 = mock(RenderingEngine.class);
		when(engine2.getViewModel()).thenReturn(networkView1);
		final RenderingEngine<CyNetwork> engine3 = mock(RenderingEngine.class);
		when(engine3.getViewModel()).thenReturn(networkView2);
		
		final RenderingEngine<?> engine = manager.getRendringEngine(networkView1);
		assertNull(engine);
		
		when(engine1.getViewModel()).thenReturn(networkView1);
		manager.addRenderingEngine(engine1);

		assertEquals(engine1, manager.getRendringEngine(networkView1));
		
		// Remove from manager
		manager.removeRenderingEngine(engine1);
		
		assertNull(manager.getRendringEngine(networkView1));
		
	}
}
