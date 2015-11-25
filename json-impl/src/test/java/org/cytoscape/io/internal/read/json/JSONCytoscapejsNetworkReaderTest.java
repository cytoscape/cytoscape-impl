package org.cytoscape.io.internal.read.json;

import static org.cytoscape.model.CyEdge.Type.DIRECTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JSONCytoscapejsNetworkReaderTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();
	private final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	private final CyNetworkManager networkManager = support.getNetworkManager();

	@Mock private TaskMonitor tm;
	@Mock private CyApplicationManager appManager;
	@Mock private NetworkViewRenderer netViewRenderer;
	@Mock private CyRootNetworkManager rootNetworkManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(netViewRenderer.getNetworkViewFactory()).thenReturn(viewFactory);
		when(appManager.getDefaultNetworkViewRenderer()).thenReturn(netViewRenderer);
	}

	@After
	public void tearDown() throws Exception {
	}

	private CyNetworkView loadNetwork(final File testFile) throws Exception {

		InputStream is = new FileInputStream(testFile);
		CytoscapeJsNetworkReader reader = new CytoscapeJsNetworkReader(null, is, appManager, networkFactory,
				networkManager, rootNetworkManager);
		reader.run(tm);
		is.close();
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);

		final CyNetwork network = networks[0];
		assertNotNull(network);

		final CyNetworkView view = reader.buildCyNetworkView(network);
		assertNotNull(view);

		return view;
	}

	@Test
	public void testYeastNetwork() throws Exception {
		// galFiltered Cytoscape.js JSON file
		final File testFile = new File("src/test/resources/testData/galFiltered.json");
		final CyNetworkView view = loadNetwork(testFile);
		testYeast(view);
	}

	@Test
	public void testNumberParsing() throws Exception {
		final File testFile = new File("src/test/resources/testData/bug-test.json");
		final CyNetworkView view = loadNetwork(testFile);
		testNumberParsers(view);
	}

	@Test
	public void testComplexData() throws Exception {
		final File testFile = new File("src/test/resources/testData/complex-data.json");
		final CyNetworkView view = loadNetwork(testFile);
		testComplex(view);
	}
	
	@Test
	public void testElementList() throws Exception {
		// Element list generated with Cytoscape.js ( cy.elements().jsons() )
		final File testFile = new File("src/test/resources/testData/element_list.json");
		final CyNetworkView view = loadNetwork(testFile);
		testElement(view);
	}
	
	private final void testComplex(final CyNetworkView view) {
		final CyNetwork network = view.getModel();

		// Check network table
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final String networkSharedName = network.getRow(network).get("shared_name", String.class);
		assertEquals("Barabasi graph", networkSharedName);
		assertEquals("BA 1", networkName);

		final Double number1 = network.getRow(network).get("numberInt1", Double.class);
		assertTrue(1.0 == number1);
		assertTrue(1 == number1);

		final Boolean boolTest = network.getRow(network).get("bool", Boolean.class);
		assertFalse(boolTest);
		
		// Empty list should be handled as String list
		final List<String> kw = network.getRow(network).getList("keywords", String.class);
		assertFalse(kw.isEmpty());
		assertEquals(2, kw.size());
		
		final List<Double> complex = network.getRow(network).getList("mixedNumberList", Double.class);
		assertFalse(complex.isEmpty());
		assertEquals(10, complex.size());
		// List order must be conserved.
		assertTrue(1.0 == complex.get(0));
		assertTrue(0 == complex.get(1));
		assertTrue(2.3d == complex.get(2));
		assertTrue(1.3E8 == complex.get(3));
		assertTrue(-22.23456 == complex.get(7));
		assertTrue(1.0000d == complex.get(9));
		
		final int nodeCount = network.getNodeCount();
		final int edgeCount = network.getEdgeCount();
		assertEquals(2, nodeCount);
		assertEquals(1, edgeCount);
		
		final Collection<CyRow> match1 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "25");
		assertEquals(1, match1.size());
		final CyRow row1 = match1.iterator().next();
		assertEquals("a", row1.get("id", String.class));
		assertTrue(1 == row1.get("a", Double.class));
		assertTrue(1 == row1.get("b", Double.class));
		
		// SUID should be ignored.
		assertFalse(57908l == row1.get("SUID", Long.class));
		assertEquals("25", row1.get("name", String.class));
		
		final Collection<CyRow> match2 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "2");
		assertEquals(1, match2.size());
		final CyRow row2 = match2.iterator().next();
		assertEquals("2", row2.get("id", String.class));
		assertTrue(null == row2.get("a", Double.class));
		assertTrue(1 == row2.get("b", Double.class));
		
		// SUID should be ignored.
		assertFalse(57908l == row2.get("SUID", Long.class));
		assertEquals("2", row2.get("name", String.class));
		
		
		final CyEdge edge1 = getEdge(network, "a", "2");
		assertNotNull(edge1);
		assertEquals(null, network.getRow(edge1).get(CyEdge.INTERACTION, String.class));
	}
	
	private final void testNumberParsers(final CyNetworkView view) {
		final CyNetwork network = view.getModel();

		// Check network table
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final String networkSharedName = network.getRow(network).get("shared_name", String.class);
		assertEquals("Barabasi graph", networkName);
		assertEquals("BA graph", networkSharedName);

		final Double power = network.getRow(network).get("power", Double.class);
		assertTrue(1.0 == power);
		assertTrue(1 == power);

		final Boolean boolTest = network.getRow(network).get("boolTest", Boolean.class);
		assertTrue(boolTest);
		
		// Empty list should be handled as String list
		final List<String> empty = network.getRow(network).getList("__Annotations", String.class);
		assertTrue(empty.isEmpty());
		
		final int nodeCount = network.getNodeCount();
		final int edgeCount = network.getEdgeCount();
		assertEquals(25, nodeCount);
		assertEquals(24, edgeCount);
		
		// Duplicate ID test		
		assertNotNull(network.getDefaultNodeTable().getColumn("id"));
		assertEquals(String.class, network.getDefaultNodeTable().getColumn("id").getType());
		assertNotNull(network.getDefaultNodeTable().getColumn(CyNetwork.NAME));
		assertNotNull(network.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME));
		assertNotNull(network.getDefaultNodeTable().getColumn("a"));
		assertNotNull(network.getDefaultNodeTable().getColumn("b"));
		assertNotNull(network.getDefaultNodeTable().getColumn("c"));
		assertNotNull(network.getDefaultNodeTable().getColumn("d"));
		assertNotNull(network.getDefaultNodeTable().getColumn("e"));
		
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("a").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("b").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("c").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("d").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("e").getType());

		final Collection<CyRow> match1 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "25");
		assertEquals(1, match1.size());
		final CyRow row1 = match1.iterator().next();
		assertEquals("57908", row1.get("id", String.class));
		assertTrue(2.00122 == row1.get("a", Double.class));
		assertEquals(null, row1.get("b", Double.class));
		assertTrue(0.01 == row1.get("c", Double.class));
		assertEquals("25", row1.get("name", String.class));
		
		final Collection<CyRow> match2 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "57907");
		assertEquals(1, match2.size());
		final CyRow row2 = match2.iterator().next();
		assertEquals("57907", row2.get("id", String.class));
		assertEquals("57907", row2.get("name", String.class));
		
		// Test edges
		final CyEdge edge1 = getEdge(network, "57892", "57886");
		assertTrue(1 == network.getRow(edge1).get("number", Double.class));
		assertTrue(1.00f == network.getRow(edge1).get("number", Double.class));
		assertTrue(1.00d == network.getRow(edge1).get("number", Double.class));
		
		final CyEdge edge2 = getEdge(network, "57887", "57886");
		assertTrue(0.1 == network.getRow(edge2).get("number", Double.class));
		assertTrue(0.1d == network.getRow(edge2).get("number", Double.class));
	}
	
	private final CyEdge getEdge(CyNetwork network, String source, String target) {
		final Collection<CyRow> sRows = network.getDefaultNodeTable().getMatchingRows("id", source);
		final Collection<CyRow> tRows = network.getDefaultNodeTable().getMatchingRows("id", target);
		final CyRow s = sRows.iterator().next();
		final CyRow t = tRows.iterator().next();
		Long sid = s.get(CyIdentifiable.SUID, Long.class);
		Long tid = t.get(CyIdentifiable.SUID, Long.class);
		List<CyEdge> edges = network.getConnectingEdgeList(network.getNode(sid), network.getNode(tid), Type.ANY);
		assertFalse(edges.isEmpty());
		CyEdge edge = edges.iterator().next();
		return edge;
	}
	
	private final void testYeast(final CyNetworkView view) {
		final CyNetwork network = view.getModel();

		// Check network table
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final String networkSharedName = network.getRow(network).get("shared_name", String.class);
		assertEquals("Yeast Network Sample", networkName);
		assertEquals("Yeast Sample", networkSharedName);
		
		
		// Type checking
		final CyColumn longColumn = network.getDefaultNetworkTable().getColumn("longTest");
		assertNotNull(longColumn);
		assertEquals(Long.class, longColumn.getType());
		
		final CyColumn longListColumn = network.getDefaultNetworkTable().getColumn("longList");
		assertNotNull(longListColumn);
		assertEquals(List.class, longListColumn.getType());
		assertEquals(Long.class, longListColumn.getListElementType());
		List<Long> longList = network.getRow(network).getList("longList", Long.class);
		assertFalse(longList.isEmpty());
		assertEquals(3, longList.size());
		assertEquals((Long)322222l, longList.get(2));
		
		final List<Double> numbers = network.getRow(network).getList("numberList", Double.class);
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
		assertEquals(Long.class, network.getDefaultNodeTable().getColumn("Eccentricity").getType());
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
	
	
	private final void testElement(final CyNetworkView view) {
		final CyNetwork network = view.getModel();

		// Check network table
		final String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		final String networkSharedName = network.getRow(network).get("shared_name", String.class);
		assertEquals(null, networkName);
		assertEquals(null, networkSharedName);
		
		// Type checking
		final CyColumn idColumn = network.getDefaultNodeTable().getColumn("id");
		assertNotNull(idColumn);
		assertEquals(String.class, idColumn.getType());
		
		assertEquals(100, network.getNodeCount());
		assertEquals(100, network.getEdgeCount());
	}
}
