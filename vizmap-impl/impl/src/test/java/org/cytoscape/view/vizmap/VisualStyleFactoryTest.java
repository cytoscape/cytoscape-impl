package org.cytoscape.view.vizmap;

import static org.mockito.Mockito.mock;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.junit.After;
import org.junit.Before;

public class VisualStyleFactoryTest extends AbstractVisualStyleFactoryTest {


	@Before
	public void setUp() throws Exception {
		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final CyNetworkManager cyNetworkManagerServiceRef = mock(CyNetworkManager.class);
		factory = new VisualStyleFactoryImpl(lexManager, serviceRegistrar, cyNetworkManagerServiceRef);
	}

	@After
	public void tearDown() throws Exception {
	}

}
