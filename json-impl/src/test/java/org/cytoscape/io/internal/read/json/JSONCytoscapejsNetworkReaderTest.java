package org.cytoscape.io.internal.read.json;

import static org.cytoscape.model.CyEdge.Type.DIRECTED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JSONCytoscapejsNetworkReaderTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();
	private final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	private final CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
	private final CyNetworkManager networkManager =  support.getNetworkManager();
	
	private TaskMonitor tm;

	@Before
	public void setUp() throws Exception {
		this.tm = mock(TaskMonitor.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNetworkViewReader() throws Exception {

		// Test Cytoscape.js JSON file
		final File cyjs1 = new File("src/test/resources/testData/galFiltered.cyjs");

		InputStream is = new FileInputStream(cyjs1);
		CytoscapeJsNetworkReader reader = new CytoscapeJsNetworkReader(is, viewFactory, networkFactory, networkManager, rootNetworkManager);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		
		final CyNetwork network = networks[0];
		assertNotNull(network);
		
		final CyNetworkView view = reader.buildCyNetworkView(network);
		assertNotNull(view);
		testLoadedNetwork(view);
		is.close();
	}

	private final void testLoadedNetwork(final CyNetworkView view) {
		final CyNetwork network = view.getModel();
		
		// Check network table
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final String networkSharedName = network.getRow(network).get("shared_name", String.class);
		assertEquals("Yeast Network Sample", networkName);
		assertEquals("Yeast Sample", networkSharedName);
		
		final List<Integer> numbers = network.getRow(network).getList("numberList", Integer.class);
		assertEquals(4, numbers.size());
		assertTrue(200 == numbers.get(1));
		
		final List<Double> doubles = network.getRow(network).getList("floatList", Double.class);
		assertEquals(3, doubles.size());
		assertTrue(0.30 == doubles.get(1));

		final int nodeCount = network.getNodeCount();
		final int edgeCount = network.getEdgeCount();
		assertEquals(331, nodeCount);
		assertEquals(362, edgeCount);
		
		assertNull(network.getDefaultNodeTable().getColumn("foo"));
		assertNotNull(network.getDefaultNodeTable().getColumn(CyNetwork.NAME));
		assertNotNull(network.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME));

		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("gal1RGexp").getType());
		assertEquals(Integer.class, network.getDefaultNodeTable().getColumn("Degree").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("ClusteringCoefficient").getType());
		assertEquals(Boolean.class, network.getDefaultNodeTable().getColumn("IsSingleNode").getType());
		assertEquals(Long.class, network.getDefaultNodeTable().getColumn("SUID").getType());
		assertEquals(List.class, network.getDefaultNodeTable().getColumn("alias").getType());
	
		Class<?> listType = network.getDefaultNodeTable().getColumn("alias").getListElementType();
		assertEquals(String.class, listType); 
	
		// test nodes
		final Collection<CyRow> match1 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "YFL017C");
		assertEquals(1, match1.size());
		final CyRow row1 = match1.iterator().next();
		
		// Test List columns
		final List<String> listAttr = row1.getList("alias", String.class);
		assertEquals(4, listAttr.size());
		assertTrue(listAttr.contains("PAT1"));
		assertFalse(listAttr.contains("dummy"));
		assertTrue(listAttr.contains("S000001877"));
		
		Long suid1 = row1.get(CyIdentifiable.SUID, Long.class);
		CyNode node1 = network.getNode(suid1);
		assertNotNull(node1);
	
		// Check connection
		assertEquals(5, network.getAdjacentEdgeList(node1, DIRECTED).size());
	
		final Collection<CyRow> match2 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "YFL017C");
		assertNotNull(match2);
		assertEquals(1, match2.size());
	
		// Test node view
		final CyNode node = network.getNode(match2.iterator().next().get(CyIdentifiable.SUID, Long.class));
		
		final View<CyNode> nodeView = view.getNodeView(node);
		assertNotNull(nodeView);
		Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		
		assertTrue(x == 2540.8436768462666);
		assertTrue(y == 1112.9282691176754);
		
		// Test edge view
		List<CyEdge> edges = network.getAdjacentEdgeList(node, DIRECTED);
		assertEquals(5, edges.size());
		final View<CyEdge> edgeView = view.getEdgeView(edges.get(0));
		assertNotNull(edgeView);

		final Collection<CyRow> match3 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "foo");
		assertNotNull(match3);
		assertEquals(0, match3.size());
	}
}
