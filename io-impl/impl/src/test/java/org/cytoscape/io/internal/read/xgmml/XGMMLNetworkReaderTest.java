package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
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
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Before;
import org.junit.Test;

public class XGMMLNetworkReaderTest extends AbstractNetworkReaderTest {

	CyNetworkViewFactory networkViewFactory;
	CyNetworkFactory networkFactory;
	CyRootNetworkManager rootNetworkMgr;
	CyTableFactory tableFactory;
	RenderingEngineManager renderingEngineMgr;
	ReadDataManager readDataMgr;
	ReadCache readCache;
	UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	XGMMLParser parser;
	XGMMLNetworkReader reader;

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
		CyNetworkView[] views = getViews("galFiltered.xgmml");
		CyNetwork net = checkSingleNetwork(views, 331, 362);
		findInteraction(net, "YGR136W", "YGR058W", "pp", 1);
	}
	
	@Test
	public void testParseHiddenAtt() throws Exception {
		CyNetworkView[] views = getViews("hiddenAtt.xgmml");
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
	}
	
	@Test
	public void testParseExpandedGroupFrom2x() throws Exception {
		CyNetworkView[] views = getViews("group_2x_expanded.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		CyNetwork net = checkSingleNetwork(views, 4, 2);
		
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
	}
	
	@Test
	public void testParseCollapsedGroupFrom2x() throws Exception {
		CyNetworkView[] views = getViews("group_2x_collapsed.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		CyNetwork net = checkSingleNetwork(views, 2, 1);
		
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
	}

	@Test
	public void testIsLockedVisualProperty() throws Exception {
		reader = new XGMMLNetworkReader(new ByteArrayInputStream("".getBytes("UTF-8")), viewFactory, netFactory,
				renderingEngineMgr, rootNetworkMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr);
		
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

	private CyNetworkView[] getViews(String file) throws Exception {
		File f = new File("./src/test/resources/testData/xgmml/" + file);
		reader = new XGMMLNetworkReader(new FileInputStream(f), viewFactory, netFactory,
				renderingEngineMgr, rootNetworkMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr);
		reader.run(taskMonitor);

		final CyNetwork[] networks = reader.getNetworks();
		final CyNetworkView[] views = new CyNetworkView[networks.length];
		int i = 0;

		for (CyNetwork network : networks) {
			views[i] = reader.buildCyNetworkView(network);
			i++;
		}

		return views;
	}
}
