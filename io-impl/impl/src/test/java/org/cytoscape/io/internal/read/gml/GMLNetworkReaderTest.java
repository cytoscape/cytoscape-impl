package org.cytoscape.io.internal.read.gml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GMLNetworkReaderTest extends AbstractNetworkReaderTest {
	@Mock private RenderingEngineManager renderingEngineManager;
	@Mock private VisualLexicon lexicon;
	private UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		MockitoAnnotations.initMocks(this);
		
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		
		// FIXME
		//when(lexicon.getVisualProperties(any(String.class))).thenReturn(new LinkedList<VisualProperty<?>>());
		
		TableTestSupport tblTestSupport = new TableTestSupport();
		CyTableFactory tableFactory = tblTestSupport.getTableFactory();
		CyTableManager tableMgr= mock(CyTableManager.class);
		
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);
	}
	
	@Test
	public void testLoadGml() throws Exception {
		File file = new File("src/test/resources/testData/gml/example1.gml");
		GMLNetworkReader reader = new GMLNetworkReader(new FileInputStream(file), netFactory, viewFactory,
													   renderingEngineManager, unrecognizedVisualPropertyMgr);
		reader.run(taskMonitor);
		
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetworkView[] networkViews = new CyNetworkView[networks.length];
		int i = 0;
		for(CyNetwork network: networks) {
			networkViews[i] = reader.buildCyNetworkView(network);
			i++;
		}
		
		assertNotNull(networkViews);
		assertEquals(1, networkViews.length);
		
		CyNetworkView view = networkViews[0];
		assertNotNull(view);
		
		CyNetwork model = view.getModel();
		assertNotNull(model);
		
		assertEquals(3, model.getNodeCount());
		assertEquals(3, model.getEdgeCount());
	}
}
