package org.cytoscape.view.vizmap;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualMappingManagerImpl;
import org.junit.After;
import org.junit.Before;

public class VisualMappingManagerTest extends AbstractVisualMappingManagerTest {
	
	@Before
	public void setUp() throws Exception {
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final VisualStyleFactory factory = mock(VisualStyleFactory.class);
		final VisualStyle dummyDefaultStyle = mock(VisualStyle.class);
		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);
		when(factory.createVisualStyle(VisualMappingManagerImpl.DEFAULT_STYLE_NAME)).thenReturn(dummyDefaultStyle);
		
		vmm = new VisualMappingManagerImpl(eventHelper, factory, lexManager);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	

}
