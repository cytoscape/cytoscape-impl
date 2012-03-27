package org.cytoscape.view.presentation;

import org.cytoscape.view.presentation.internal.RenderingEngineManagerImpl;
import org.junit.Before;

public class RenderingEngineManagerTest extends AbstractRenderingEngineManagerTest {
	
	@Before
	public void setUp() throws Exception {
		manager = new RenderingEngineManagerImpl();
	}
}
