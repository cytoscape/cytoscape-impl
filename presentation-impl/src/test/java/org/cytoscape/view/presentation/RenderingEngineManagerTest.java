package org.cytoscape.view.presentation;

import static org.mockito.Mockito.mock;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.internal.RenderingEngineManagerImpl;
import org.junit.Before;

public class RenderingEngineManagerTest extends AbstractRenderingEngineManagerTest {
	
	@Before
	public void setUp() throws Exception {
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		manager = new RenderingEngineManagerImpl(eventHelper);
	}
}
