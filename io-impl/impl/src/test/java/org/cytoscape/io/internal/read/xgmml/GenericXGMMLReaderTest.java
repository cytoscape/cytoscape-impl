package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Before;
import org.junit.Test;

public class GenericXGMMLReaderTest extends AbstractNetworkReaderTest {

	CyNetworkViewFactory networkViewFactory;
	CyNetworkFactory networkFactory;
	CyRootNetworkManager rootNetworkMgr;
	CyTableFactory tableFactory;
	RenderingEngineManager renderingEngineMgr;
	ReadDataManager readDataMgr;
	ReadCache readCache;
	UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	XGMMLParser parser;
	GenericXGMMLReader reader;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		renderingEngineMgr = mock(RenderingEngineManager.class);
		when(renderingEngineMgr.getDefaultVisualLexicon())
				.thenReturn(new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT",
																			"Minimal Root Visual Property")));

		TableTestSupport tblTestSupport = new TableTestSupport();
		tableFactory = tblTestSupport.getTableFactory();
		
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		networkFactory = networkTestSupport.getNetworkFactory();
		rootNetworkMgr = networkTestSupport.getRootNetworkFactory();
		
		NetworkViewTestSupport networkViewTestSupport = new NetworkViewTestSupport();
		networkViewFactory = networkViewTestSupport.getNetworkViewFactory();
		
		readCache = new ReadCache();
		readDataMgr = new ReadDataManager(readCache, mock(EquationCompiler.class), networkFactory, rootNetworkMgr);
		HandlerFactory handlerFactory = new HandlerFactory(readDataMgr);
		handlerFactory.init();
		parser = new XGMMLParser(handlerFactory, readDataMgr);

		CyTableManager tableMgr= mock(CyTableManager.class);
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);
		
		SessionUtil.setReadingSessionFile(false);
	}

	@Test
	public void testReadFromTypicalFile() throws Exception {
		List<CyNetworkView> views = getViews("galFiltered.xgmml");
		CyNetwork net = checkSingleNetwork(views, 331, 362);
		findInteraction(net, "YGR136W", "YGR058W", "pp", 1);
		assertCustomColumnsAreMutable(net);
	}
	
	@Test
	public void testParseHiddenAtt() throws Exception {
		List<CyNetworkView> views = getViews("hiddenAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 2, 1);
		
		// Test CyTables
		CyTable defNetTbl = net.getDefaultNetworkTable();
		assertNotNull(defNetTbl.getColumn("test"));
		CyTable hiddenNetTbl = net.getRow(net, CyNetwork.HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenNetTbl.getColumn("_private_int"));
		
		CyTable defNodeTbl = net.getDefaultNodeTable();
		assertNotNull(defNodeTbl.getColumn("name"));
		assertNotNull(defNodeTbl.getColumn("list_1"));
		CyTable hiddenNodeTbl = net.getRow(net.getNodeList().get(0), CyNetwork.HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenNodeTbl.getColumn("_private_str"));
		assertNotNull(hiddenNodeTbl.getColumn("_private_list"));
		
		CyTable defEdgeTbl = net.getDefaultEdgeTable();
		assertNotNull(defEdgeTbl.getColumn("name"));
		CyTable hiddenEdgeTbl = net.getRow(net.getEdgeList().get(0), CyNetwork.HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenEdgeTbl.getColumn("_private_real"));
		
		assertCustomColumnsAreMutable(net);
	}
	
	@Test
	public void testParseExpandedGroupFrom2x() throws Exception {
		List<CyNetworkView> views = getViews("group_2x_expanded.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		CyNetwork net = checkSingleNetwork(views, 4, 2);
		check2xGroupMetadata(net);
	}
	
	@Test
	public void testParseCollapsedGroupFrom2x() throws Exception {
		List<CyNetworkView> views = getViews("group_2x_collapsed.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		CyNetwork net = checkSingleNetwork(views, 2, 1);
		CyNode grNode = check2xGroupMetadata(net);
		// Check group network data
		CyNetwork grNet = grNode.getNetworkPointer();
		for (CyNode n : grNet.getNodeList()) {
			assertNotNull(grNet.getRow(n, CyNetwork.HIDDEN_ATTRS).get("__metanodeHintX", Double.class));
			assertNotNull(grNet.getRow(n, CyNetwork.HIDDEN_ATTRS).get("__metanodeHintY", Double.class));
		}
		assertCustomColumnsAreMutable(net);
		assertCustomColumnsAreMutable(grNet);
	}

	@Test
	public void testIsLockedVisualProperty() throws Exception {
		reader = new GenericXGMMLReader(new ByteArrayInputStream("".getBytes("UTF-8")), viewFactory, netFactory,
				renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr);
		
		CyNetwork network = mock(CyNetwork.class);
		assertFalse(reader.isLockedVisualProperty(network, "GRAPH_VIEW_ZOOM"));
		assertFalse(reader.isLockedVisualProperty(network, "GRAPH_VIEW_CENTER_X"));
		assertFalse(reader.isLockedVisualProperty(network, "GRAPH_VIEW_CENTER_Y"));
		assertTrue(reader.isLockedVisualProperty(network, "backgroundColor"));

		CyNode node = mock(CyNode.class);
		assertFalse(reader.isLockedVisualProperty(node, "x"));
		assertFalse(reader.isLockedVisualProperty(node, "y"));
		assertFalse(reader.isLockedVisualProperty(node, "z"));
		assertTrue(reader.isLockedVisualProperty(node, "type"));
		assertTrue(reader.isLockedVisualProperty(node, "w"));
		assertTrue(reader.isLockedVisualProperty(node, "h"));
		assertTrue(reader.isLockedVisualProperty(node, "fill"));
		assertTrue(reader.isLockedVisualProperty(node, "width"));
		assertTrue(reader.isLockedVisualProperty(node, "outline"));
		assertTrue(reader.isLockedVisualProperty(node, "nodeTransparency"));
		assertTrue(reader.isLockedVisualProperty(node, "nodeLabelFont"));
		assertTrue(reader.isLockedVisualProperty(node, "borderLineType"));
		assertTrue(reader.isLockedVisualProperty(node, BasicVisualLexicon.NODE_X_LOCATION.getIdString()));
		assertTrue(reader.isLockedVisualProperty(node, BasicVisualLexicon.NODE_Y_LOCATION.getIdString()));
		assertTrue(reader.isLockedVisualProperty(node, BasicVisualLexicon.NODE_FILL_COLOR.getIdString()));

		CyEdge edge = mock(CyEdge.class);
		assertTrue(reader.isLockedVisualProperty(edge, "width"));
		assertTrue(reader.isLockedVisualProperty(edge, "fill"));
		assertTrue(reader.isLockedVisualProperty(edge, "sourceArrow"));
		assertTrue(reader.isLockedVisualProperty(edge, "targetArrow"));
		assertTrue(reader.isLockedVisualProperty(edge, "sourceArrowColor"));
		assertTrue(reader.isLockedVisualProperty(edge, "targetArrowColor"));
		assertTrue(reader.isLockedVisualProperty(edge, "edgeLabelFont"));
		assertTrue(reader.isLockedVisualProperty(edge, "edgeLineType"));
		assertTrue(reader.isLockedVisualProperty(edge, "curved"));
		assertTrue(reader.isLockedVisualProperty(edge, BasicVisualLexicon.EDGE_WIDTH.getIdString()));
	}

	@Test
	public void testIsXGMMLTransparency() {
		assertTrue(GenericXGMMLReader.isXGMMLTransparency("nodeTransparency"));
		assertTrue(GenericXGMMLReader.isXGMMLTransparency("edgeTransparency"));
	}

	@Test
	public void testIsOldFont() {
		assertTrue(GenericXGMMLReader.isOldFont("nodeLabelFont"));
		assertTrue(GenericXGMMLReader.isOldFont("cy:nodeLabelFont"));
		assertTrue(GenericXGMMLReader.isOldFont("edgeLabelFont"));
		assertTrue(GenericXGMMLReader.isOldFont("cy:edgeLabelFont"));
	}

	@Test
	public void testConvertXGMMLTransparencyValue() {
		assertEquals("0", GenericXGMMLReader.convertXGMMLTransparencyValue("0"));
		assertEquals("0", GenericXGMMLReader.convertXGMMLTransparencyValue("0.0"));
		assertEquals("255", GenericXGMMLReader.convertXGMMLTransparencyValue("1.0"));
		assertEquals("26", GenericXGMMLReader.convertXGMMLTransparencyValue("0.1"));
		assertEquals("128", GenericXGMMLReader.convertXGMMLTransparencyValue("0.5"));
	}

	@Test
	public void testConvertOldFontValue() {
		assertEquals("ACaslonPro,bold,18", GenericXGMMLReader.convertOldFontValue("ACaslonPro-Bold-0-18"));
		assertEquals("SansSerif,plain,12", GenericXGMMLReader.convertOldFontValue("SansSerif-0-12.1"));
		assertEquals("SansSerif,bold,12", GenericXGMMLReader.convertOldFontValue("SansSerif.bold-0.0-12.0"));
		assertEquals("SansSerif,bold,12", GenericXGMMLReader.convertOldFontValue("SansSerif,bold,12"));
	}
	
	private void assertCustomColumnsAreMutable(CyNetwork net) {
		// User or non-default columns should be immutable
		CyTable[] tables = new CyTable[] {
			net.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS),
			net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS),
			net.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS),
			net.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS),
			net.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS),
			net.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS)
		};
		for (CyTable t : tables) {
			for (CyColumn c : t.getColumns()) {
				String name = c.getName();
				if (!name.equals(CyNetwork.SUID)     && !name.equals(CyNetwork.NAME) && 
					!name.equals(CyNetwork.SELECTED) && !name.equals(CyEdge.INTERACTION) &&
					!name.equals(CyRootNetwork.SHARED_NAME) && !name.equals(CyRootNetwork.SHARED_INTERACTION)) {
					assertFalse("Column " + c.getName() + " should NOT be immutable", c.isImmutable());
				}
			}
		}
	}
	
	private CyNode check2xGroupMetadata(final CyNetwork net) {
		// Test 2.x group parsed as network pointer
		CyNode gn = null;
		int npCount = 0;
		
		for (CyNode n : net.getNodeList()) {
			if (net.getRow(n, CyNetwork.HIDDEN_ATTRS).isSet("__groupState")) {
				gn = n;
				if (++npCount > 1) fail("There should be only one group node!");
			} else { // The other nodes have no network pointer!
				assertNull(n.getNetworkPointer());
			}
		}
		
		assertNotNull("The group node cannot be found", gn);
		CyNetwork np = gn.getNetworkPointer();
		assertNotNull(np);
		assertEquals(2, np.getNodeCount());
		assertEquals(1, np.getEdgeCount());
		
		// Check if the nested graph's attribute was imported to the network pointer
		CyRow grNetrow = np.getRow(np);
		assertEquals("Lorem Ipsum", grNetrow.get("gr_att_1", String.class));
		
		// Check external edges metadata (must be added by the reader!)
		CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(np);
		CyRow rnRow = rootNet.getRow(gn, CyNetwork.HIDDEN_ATTRS);
		List<String> extEdgeIds = rnRow.getList("__externalEdges", String.class);
		
		assertNotNull(extEdgeIds);
		assertEquals(1, extEdgeIds.size());
		assertEquals("node1 (DirectedEdge) node2", extEdgeIds.get(0));
		
		return gn;
	}
	
	private List<CyNetworkView> getViews(String file) throws Exception {
		File f = new File("./src/test/resources/testData/xgmml/" + file);
		reader = new GenericXGMMLReader(new FileInputStream(f), viewFactory, netFactory,
				renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr);
		reader.run(taskMonitor);

		final CyNetwork[] networks = reader.getNetworks();
		final List<CyNetworkView> views = new ArrayList<CyNetworkView>();

		for (CyNetwork network : networks) {
			views.add(reader.buildCyNetworkView(network));
		}

		return views;
	}
}
