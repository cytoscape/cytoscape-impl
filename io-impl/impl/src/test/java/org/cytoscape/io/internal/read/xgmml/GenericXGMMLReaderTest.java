package org.cytoscape.io.internal.read.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.HIDDEN_ATTRS;
import static org.cytoscape.model.CyNetwork.LOCAL_ATTRS;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_ATTRS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.GroupTestSupport;
import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
	CyNetworkTableManager netTablMgr;
	CyGroupManager groupMgr;
	CyTableFactory tableFactory;
	RenderingEngineManager renderingEngineMgr;
	ReadDataManager readDataMgr;
	ReadCache readCache;
	GroupUtil groupUtil;
	SUIDUpdater suidUpdater;
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
		netTablMgr = networkTestSupport.getNetworkTableManager();
		
		NetworkViewTestSupport networkViewTestSupport = new NetworkViewTestSupport();
		networkViewFactory = networkViewTestSupport.getNetworkViewFactory();
		
		GroupTestSupport groupTestSupport = new GroupTestSupport();
		CyGroupFactory grFactory = groupTestSupport.getGroupFactory();
		groupMgr = groupTestSupport.getGroupManager();
		
		readCache = new ReadCache(netTablMgr);
		groupUtil = new GroupUtil(groupMgr, grFactory);
		suidUpdater = new SUIDUpdater();
		readDataMgr = new ReadDataManager(readCache, suidUpdater, mock(EquationCompiler.class), networkFactory, 
				rootNetworkMgr, groupUtil);
		
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
	public void testSharedAndLocalAttributes() throws Exception {
		List<CyNetworkView> views = getViews("simple.xgmml");
		CyNetwork net = checkSingleNetwork(views, 1, 1);
		assertCustomColumnsAreMutable(net);
		
		CyTable defNodeTbl = net.getDefaultNodeTable();
		CyTable defEdgeTbl = net.getDefaultEdgeTable();
		CyTable defNetTbl = net.getDefaultNetworkTable();
		
		// Assert all custom Node/Edge columns are shared
		assertTrue(defNodeTbl.getColumn("node_att_1").getVirtualColumnInfo().isVirtual());
		assertTrue(defNodeTbl.getColumn("node_att_2").getVirtualColumnInfo().isVirtual());
		assertTrue(defEdgeTbl.getColumn("edge_att_1").getVirtualColumnInfo().isVirtual());
		assertTrue(defEdgeTbl.getColumn("edge_att_2").getVirtualColumnInfo().isVirtual());
		// Assert all custom network columns are local
		assertFalse(defNetTbl.getColumn("net_att_1").getVirtualColumnInfo().isVirtual());
		assertFalse(defNetTbl.getColumn("net_att_2").getVirtualColumnInfo().isVirtual());
		// Assert mandatory local attributes
		assertFalse(defNodeTbl.getColumn(CyNetwork.SELECTED).getVirtualColumnInfo().isVirtual());
		assertFalse(defNodeTbl.getColumn(CyNetwork.NAME).getVirtualColumnInfo().isVirtual());
		assertFalse(defEdgeTbl.getColumn(CyNetwork.SELECTED).getVirtualColumnInfo().isVirtual());
		assertFalse(defEdgeTbl.getColumn(CyNetwork.NAME).getVirtualColumnInfo().isVirtual());
		assertFalse(defEdgeTbl.getColumn(CyEdge.INTERACTION).getVirtualColumnInfo().isVirtual());
		assertFalse(defNetTbl.getColumn(CyNetwork.NAME).getVirtualColumnInfo().isVirtual());
		assertFalse(defNetTbl.getColumn(CyNetwork.SELECTED).getVirtualColumnInfo().isVirtual());
	}
	
	/**
	 * Make sure the custom attributes "cy:type" and "cy:elementType" are used
	 * to create the correct column types, when they are available (i.e. XGMML exported from Cytoscape v3.3+)
	 */
	@Test
	public void testCustomCyAttTypes() throws Exception {
		List<CyNetworkView> views = getViews("simple_3.3.xgmml");
		CyNetwork net = checkSingleNetwork(views, 1, 1);
		assertCustomColumnsAreMutable(net);
		
		CyTable defNodeTbl = net.getDefaultNodeTable();
		CyTable defEdgeTbl = net.getDefaultEdgeTable();
		CyTable defNetTbl = net.getDefaultNetworkTable();
		
		assertEquals(String.class, defNodeTbl.getColumn("node_att_1").getType());
		assertEquals(List.class, defNodeTbl.getColumn("node_att_2").getType());
		assertEquals(Long.class, defNodeTbl.getColumn("node_att_2").getListElementType());
		assertEquals(Integer.class, defNodeTbl.getColumn("node_att_3").getType());
		assertEquals(Long.class, defNodeTbl.getColumn("node_att_4").getType());
		
		assertEquals(Double.class, defEdgeTbl.getColumn("edge_att_1").getType());
		assertEquals(List.class, defEdgeTbl.getColumn("edge_att_2").getType());
		assertEquals(String.class, defEdgeTbl.getColumn("edge_att_2").getListElementType());
		
		assertEquals(Boolean.class, defNetTbl.getColumn("net_att_1").getType());
		assertEquals(List.class, defNetTbl.getColumn("net_att_2").getType());
		assertEquals(Integer.class, defNetTbl.getColumn("net_att_2").getListElementType());
	}
	
	@Test
	public void testIgnoreEmptyListAtt() throws Exception {
		List<CyNetworkView> views = getViews("listAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 0, 0);
		// The column should not be created, because the List type is not known
		assertNull(net.getDefaultNetworkTable().getColumn("null_value_list"));
	}
	
	@Test
	public void testCreateEmptyList1Att() throws Exception {
		List<CyNetworkView> views = getViews("listAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 0, 0);
		// In this case, it must contain an empty list: []
		assertTrue(net.getRow(net).getList("empty_list_1", String.class).isEmpty());
	}
	
	@Test
	public void testCreateEmptyList2Att() throws Exception {
		List<CyNetworkView> views = getViews("listAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 0, 0);
		// In this case, it must contain an empty list as well: []
		assertTrue(net.getRow(net).getList("empty_list_2", String.class).isEmpty());
	}
	
	@Test
	public void testCreateListAttWithEmptyString() throws Exception {
		List<CyNetworkView> views = getViews("listAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 0, 0);
		// In this case, it must contain a list with one element (an empty String): [""]
		List<String> list = net.getRow(net).getList("list_with_empty_string", String.class);
		assertEquals(1, list.size());
		assertTrue(list.get(0).isEmpty());
	}
	
	@Test
	public void testListAtt() throws Exception {
		List<CyNetworkView> views = getViews("listAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 0, 0);
		assertEquals(2, net.getRow(net).getList("int_list", Integer.class).size());
		assertEquals(3, net.getRow(net).getList("str_list", String.class).size());
	}
	
	@Test
	public void testParseHiddenAtt() throws Exception {
		List<CyNetworkView> views = getViews("hiddenAtt.xgmml");
		CyNetwork net = checkSingleNetwork(views, 2, 1);
		
		// Test CyTables
		CyTable defNetTbl = net.getDefaultNetworkTable();
		assertNotNull(defNetTbl.getColumn("test"));
		CyTable hiddenNetTbl = net.getRow(net, HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenNetTbl.getColumn("_private_int"));
		
		CyTable defNodeTbl = net.getDefaultNodeTable();
		assertNotNull(defNodeTbl.getColumn("name"));
		assertNotNull(defNodeTbl.getColumn("list_1"));
		CyTable hiddenNodeTbl = net.getRow(net.getNodeList().get(0), HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenNodeTbl.getColumn("_private_str"));
		assertNotNull(hiddenNodeTbl.getColumn("_private_list"));
		
		CyTable defEdgeTbl = net.getDefaultEdgeTable();
		assertNotNull(defEdgeTbl.getColumn("name"));
		CyTable hiddenEdgeTbl = net.getRow(net.getEdgeList().get(0), HIDDEN_ATTRS).getTable();
		assertNotNull(hiddenEdgeTbl.getColumn("_private_real"));
		
		assertCustomColumnsAreMutable(net);
	}
	
	@Test
	public void testUpdatedSUIDAttributes() throws Exception {
		final List<CyNetworkView> views = getViews("suid_metadata.xgmml");
		final CyNetwork net = checkSingleNetwork(views, 2, 2);
		
		final List<CyNode> nodes = net.getNodeList();
		final List<CyEdge> edges = net.getEdgeList();
		
		// Check network attributes
		Collection<CyColumn> columns = net.getRow(net).getTable().getColumns();
		System.out.println(columns);
		assertEquals(net.getSUID(), net.getRow(net).get("net_id.SUID", Long.class));
		
		// Hidden List att
		List<Long> netAttList = net.getRow(net, HIDDEN_ATTRS).getList("nodes.SUID", Long.class);
		assertEquals(2, netAttList.size());
		assertTrue(netAttList.contains(nodes.get(0).getSUID()));
		assertTrue(netAttList.contains(nodes.get(1).getSUID()));
		
		// Check node attributes
		CyTable hnt = net.getTable(CyNode.class, HIDDEN_ATTRS);
		// Don't force to Long just because ends the name ends with ".SUID"!
		// The type must be real to be an SUID-type column, to avoid conflicts
		assertEquals(String.class, hnt.getColumn("wrong_type_1.SUID").getType());
		assertEquals(String.class, hnt.getColumn("wrong_type_2.SUID").getType());
		// These are ok, because the att type is "real"
		assertEquals(nodes.get(1).getSUID(), net.getRow(nodes.get(0), HIDDEN_ATTRS).get("other_node.SUID", Long.class));
		assertEquals(nodes.get(0).getSUID(), net.getRow(nodes.get(1), HIDDEN_ATTRS).get("other_node.SUID", Long.class));
		// User List att
		List<Long> nodeAttList = net.getRow(nodes.get(1)).getList("edges.SUID", Long.class);
		assertEquals(2, nodeAttList.size());
		assertTrue(nodeAttList.contains(edges.get(0).getSUID()));
		assertTrue(nodeAttList.contains(edges.get(1).getSUID()));
		
		// Check edge attributes
		assertEquals(edges.get(0).getSource().getSUID(), net.getRow(edges.get(0)).get("source_node.SUID", Long.class));
		assertEquals(edges.get(1).getSource().getSUID(), net.getRow(edges.get(1)).get("source_node.SUID", Long.class));
		
		// Set null to invalid SUIDs
		assertNull(net.getRow(edges.get(0)).get("invalid.SUID", Long.class));
		// Don't add invalid SUIDs to list value
		assertEquals(1, net.getRow(edges.get(0)).getList("invalid_list_1.SUID", Long.class).size()); // one valid SUID
		assertTrue(net.getRow(edges.get(0)).getList("invalid_list_2.SUID", Long.class).isEmpty()); // all invalid
	}
	
	@Test
	public void testParseExpandedGroupFrom2x() throws Exception {
		List<CyNetworkView> views = getViews("group_2x_expanded.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		
		CyNetwork net = checkSingleNetwork(views, 3, 2);
		CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(net);
		CyNode grNode = getNodeByName(rootNet, "metanode 1");
		check2xGroupMetadata(net, grNode, true);
		
		assertCustomColumnsAreMutable(rootNetworkMgr.getRootNetwork(net));
		assertCustomColumnsAreMutable(net);
		assertCustomColumnsAreMutable(grNode.getNetworkPointer());
		
		assertEquals(1, groupMgr.getGroupSet(net).size());
		assertTrue(groupMgr.isGroup(grNode, net));
	}
	
	@Test
	public void testParseCollapsedGroupFrom2x() throws Exception {
		List<CyNetworkView> views = getViews("group_2x_collapsed.xgmml");
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		CyNetwork net = checkSingleNetwork(views, 2, 1);
		CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(net);
		CyNode grNode = getNodeByName(rootNet, "metanode 1");
		check2xGroupMetadata(net, grNode, false);
		
		// Check group network data
		CyNetwork grNet = grNode.getNetworkPointer();
		for (CyNode n : grNet.getNodeList()) {
			assertNotNull(grNet.getRow(n, HIDDEN_ATTRS).get("__metanodeHintX", Double.class));
			assertNotNull(grNet.getRow(n, HIDDEN_ATTRS).get("__metanodeHintY", Double.class));
		}
		
		assertCustomColumnsAreMutable(rootNet);
		assertCustomColumnsAreMutable(net);
		assertCustomColumnsAreMutable(grNet);
		
		assertEquals(1, groupMgr.getGroupSet(net).size());
		assertTrue(groupMgr.isGroup(grNode, net));
	}
	
	@Test
	public void testParseNestedGroupsFrom2x() throws Exception {
		List<CyNetworkView> views = getViews("nested_groups_283.xgmml");
		CyNetwork net = checkSingleNetwork(views, 3, 3);
		// The group network should not be registered, so the network list must contain only the base network
		assertEquals(1, reader.getNetworks().length);
		
		CyNode grNode2 = getNodeByName(net, "Meta 2");
		assertTrue(groupMgr.isGroup(grNode2, net));
		CyNode grNode1 = getNodeByName(grNode2.getNetworkPointer(), "Meta 1"); // Nested group node
		assertTrue(groupMgr.isGroup(grNode1, grNode2.getNetworkPointer()));
		
		assertEquals(1, groupMgr.getGroupSet(net).size());
		assertEquals(1, groupMgr.getGroupSet(grNode2.getNetworkPointer()).size());
	}

	@Test
	public void testIsLockedVisualProperty() throws Exception {
		reader = new GenericXGMMLReader(new ByteArrayInputStream("".getBytes("UTF-8")), netFactory,
				renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr, networkManager,
				rootNetworkManager, applicationManager);
		
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
	
	@Test(expected=IOException.class)
	public void testRepairBareAmpersandsPropertyFalse() throws Exception {
		System.setProperty(GenericXGMMLReader.REPAIR_BARE_AMPERSANDS_PROPERTY, "false");
		getViews("bare_ampersands.xgmml");
	}
	
	@Test
	public void testRepairBareAmpersandsPropertyTrue() throws Exception {
		System.setProperty(GenericXGMMLReader.REPAIR_BARE_AMPERSANDS_PROPERTY, "true");
		List<CyNetworkView> views = getViews("bare_ampersands.xgmml");
		CyNetwork net = checkSingleNetwork(views, 1, 1);
		assertEquals("&ABC", net.getRow(net).get("&net_att_1", String.class));
		assertEquals("CDE&", net.getRow(net.getNodeList().get(0)).get("node_att_$1", String.class));
		assertEquals(25, net.getRow(net.getEdgeList().get(0)).get("edge_att_1$", Integer.class).intValue());
	}
	
	private void assertCustomColumnsAreMutable(CyNetwork net) {
		// User or non-default columns should be immutable
		List<CyTable> tables = new ArrayList<>();
		tables.add(net.getTable(CyNetwork.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyNetwork.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyNetwork.class, DEFAULT_ATTRS));
		tables.add(net.getTable(CyNode.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyNode.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyNode.class, DEFAULT_ATTRS));
		tables.add(net.getTable(CyEdge.class, LOCAL_ATTRS));
		tables.add(net.getTable(CyEdge.class, HIDDEN_ATTRS));
		tables.add(net.getTable(CyEdge.class, DEFAULT_ATTRS));
		
		if (net instanceof CyRootNetwork) {
			tables.add(net.getTable(CyNetwork.class, SHARED_ATTRS));
			tables.add(net.getTable(CyNode.class, SHARED_ATTRS));
			tables.add(net.getTable(CyEdge.class, SHARED_ATTRS));
		}
		
		for (CyTable t : tables) {
			for (CyColumn c : t.getColumns()) {
				String name = c.getName();
				if (!name.equals(CyNetwork.SUID)
						&& !name.equals(NAME)
						&& !name.equals(CyNetwork.SELECTED)
						&& !name.equals(CyEdge.INTERACTION)
						&& !name.equals(CyRootNetwork.SHARED_NAME)
						&& !name.equals(CyRootNetwork.SHARED_INTERACTION)) {
					assertFalse("Column " + c.getName() + " should NOT be immutable", c.isImmutable());
				}
			}
		}
	}
	
	private void check2xGroupMetadata(final CyNetwork net, final CyNode gn, final boolean expanded) {
		assertNotNull("The group node is null", gn);
		
		// Test 2.x group parsed as network pointer
		if (!expanded) {
			for (CyNode n : net.getNodeList()) {
				if (net.getRow(n, CyNetwork.HIDDEN_ATTRS).isSet(GroupUtil.GROUP_STATE_ATTRIBUTE))
					assertNotNull(n.getNetworkPointer());
				else // The other nodes have no network pointer!
					assertNull(n.getNetworkPointer());
			}
		} else {
			CyRootNetwork rootNet = ((CySubNetwork)net).getRootNetwork();
			for (CyNode n : rootNet.getNodeList()) {
				if (rootNet.getRow(n, CyNetwork.HIDDEN_ATTRS).isSet(GroupUtil.GROUP_STATE_ATTRIBUTE))
					assertNotNull(n.getNetworkPointer());
				else // The other nodes have no network pointer!
					assertNull(n.getNetworkPointer());
			}
		}
		
		CyNetwork np = gn.getNetworkPointer();
		assertNotNull(np);
		assertEquals(2, np.getNodeCount());
		assertEquals(1, np.getEdgeCount());
		
		// Check if the nested graph's attribute was imported to the network pointer
		CyRow npRow = np.getRow(np);
		assertEquals("Lorem Ipsum", npRow.get("gr_att_1", String.class));
		
		// Check external edges metadata (must be added by the reader!)
		CyRow nphRow = np.getRow(np, HIDDEN_ATTRS);
		List<Long> extEdgeIds = nphRow.getList(GroupUtil.EXTERNAL_EDGE_ATTRIBUTE, Long.class);
		assertEquals(1, extEdgeIds.size());
		
		CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(np);
		assertNotNull(rootNet.getEdge(extEdgeIds.get(0)));
	}
	
	private List<CyNetworkView> getViews(String file) throws Exception {
		SessionUtil.setReadingSessionFile(false);
		
		File f = new File("./src/test/resources/testData/xgmml/" + file);
		reader = new GenericXGMMLReader(new FileInputStream(f), netFactory,
				renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr,
				networkManager, rootNetworkManager, applicationManager);
		reader.run(taskMonitor);

		final CyNetwork[] networks = reader.getNetworks();
		final List<CyNetworkView> views = new ArrayList<>();

		for (CyNetwork network : networks) {
			views.add(reader.buildCyNetworkView(network));
		}

		return views;
	}
}
