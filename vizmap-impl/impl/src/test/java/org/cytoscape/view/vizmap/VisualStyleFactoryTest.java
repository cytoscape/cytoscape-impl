package org.cytoscape.view.vizmap;

import static org.mockito.Mockito.mock;

import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.junit.After;
import org.junit.Before;

public class VisualStyleFactoryTest extends AbstractVisualStyleFactoryTest {


	@Before
	public void setUp() throws Exception {
		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);
		factory = new VisualStyleFactoryImpl(lexManager);
	}

	@After
	public void tearDown() throws Exception {
	}

}
