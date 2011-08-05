package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.AbstractNetworkViewReaderTester;
import org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.test.support.DataTableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Before;
import org.junit.Test;

public class XGMMLNetworkReaderTest extends AbstractNetworkViewReaderTester {

	CyNetworkViewFactory networkViewFactory;
	CyNetworkFactory networkFactory;
	CyTableFactory tableFactory;
	RenderingEngineManager renderingEngineMgr;
	ReadDataManager readDataMgr;
	AttributeValueUtil attributeValueUtil;
	UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	XGMMLParser parser;
	XGMMLNetworkReader reader;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		renderingEngineMgr = mock(RenderingEngineManager.class);
		when(renderingEngineMgr.getDefaultVisualLexicon())
				.thenReturn(new MinimalVisualLexicon(new NullVisualProperty("MINIMAL_ROOT",
																			"Minimal Root Visual Property")));

		readDataMgr = new ReadDataManager(mock(EquationCompiler.class));
		ObjectTypeMap objectTypeMap = new ObjectTypeMap();
		attributeValueUtil = new AttributeValueUtil(objectTypeMap, readDataMgr);
		HandlerFactory handlerFactory = new HandlerFactory(readDataMgr, attributeValueUtil);
		parser = new XGMMLParser(handlerFactory, readDataMgr);

		networkViewFactory = mock(CyNetworkViewFactory.class);
		networkFactory = mock(CyNetworkFactory.class);
		
		DataTableTestSupport tblTestSupport = new DataTableTestSupport();
		tableFactory = tblTestSupport.getDataTableFactory();

		ByteArrayInputStream is = new ByteArrayInputStream("".getBytes("UTF-8")); // TODO: use XGMML string or load from file

		reader = new XGMMLNetworkReader(is, networkViewFactory, networkFactory, renderingEngineMgr, readDataMgr,
										parser, unrecognizedVisualPropertyMgr);

		CyTableManager tableMgr= mock(CyTableManager.class);
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);
	}

	@Test
	public void testReadFromTypicalFile() throws Exception {
		CyNetworkView[] views = getViews("galFiltered.xgmml");
		CyNetwork net = checkSingleNetwork(views, 331, 362);

		findInteraction(net, "YGR136W", "YGR058W", "pp", 1);
	}

	@Test
	public void testIsXGMMLTransparency() {
		assertTrue(XGMMLNetworkReader.isXGMMLTransparency("nodeTransparency"));
		assertTrue(XGMMLNetworkReader.isXGMMLTransparency("edgeTransparency"));
	}

	@Test
	public void testIsOldFont() {
		assertTrue(XGMMLNetworkReader.isOldFont("nodeLabelFont"));
		assertTrue(XGMMLNetworkReader.isOldFont("cy:nodeLabelFont"));
		assertTrue(XGMMLNetworkReader.isOldFont("edgeLabelFont"));
		assertTrue(XGMMLNetworkReader.isOldFont("cy:edgeLabelFont"));
	}

	@Test
	public void testConvertXGMMLTransparencyValue() {
		assertEquals("0", XGMMLNetworkReader.convertXGMMLTransparencyValue("0"));
		assertEquals("0", XGMMLNetworkReader.convertXGMMLTransparencyValue("0.0"));
		assertEquals("255", XGMMLNetworkReader.convertXGMMLTransparencyValue("1.0"));
		assertEquals("26", XGMMLNetworkReader.convertXGMMLTransparencyValue("0.1"));
		assertEquals("128", XGMMLNetworkReader.convertXGMMLTransparencyValue("0.5"));
	}

	@Test
	public void testConvertOldFontValue() {
		assertEquals("ACaslonPro,bold,18", XGMMLNetworkReader.convertOldFontValue("ACaslonPro-Bold-0-18"));
		assertEquals("SansSerif,plain,12", XGMMLNetworkReader.convertOldFontValue("SansSerif-0-12.1"));
		assertEquals("SansSerif,bold,12", XGMMLNetworkReader.convertOldFontValue("SansSerif.bold-0.0-12.0"));
		assertEquals("SansSerif,bold,12", XGMMLNetworkReader.convertOldFontValue("SansSerif,bold,12"));
	}

	@Test
	public void testIsLockedVisualProperty() {
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
		assertTrue(reader.isLockedVisualProperty(node, MinimalVisualLexicon.NODE_X_LOCATION.getIdString()));
		assertTrue(reader.isLockedVisualProperty(node, MinimalVisualLexicon.NODE_Y_LOCATION.getIdString()));
		assertTrue(reader.isLockedVisualProperty(node, MinimalVisualLexicon.NODE_FILL_COLOR.getIdString()));

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
		assertTrue(reader.isLockedVisualProperty(edge, MinimalVisualLexicon.EDGE_WIDTH.getIdString()));
	}

	private CyNetworkView[] getViews(String file) throws Exception {
		File f = new File("./src/test/resources/testData/xgmml/" + file);
		XGMMLNetworkReader snvp = new XGMMLNetworkReader(new FileInputStream(f), viewFactory, netFactory,
														 renderingEngineMgr, readDataMgr, parser,
														 unrecognizedVisualPropertyMgr);
		snvp.run(taskMonitor);

		final CyNetwork[] networks = snvp.getCyNetworks();
		final CyNetworkView[] views = new CyNetworkView[networks.length];
		int i = 0;

		for (CyNetwork network : networks) {
			views[i] = snvp.buildCyNetworkView(network);
			i++;
		}

		return views;
	}
}
