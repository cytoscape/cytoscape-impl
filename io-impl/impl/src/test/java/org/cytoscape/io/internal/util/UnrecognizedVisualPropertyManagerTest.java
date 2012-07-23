package org.cytoscape.io.internal.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.junit.Before;
import org.junit.Test;

public class UnrecognizedVisualPropertyManagerTest {

	CyTableFactory tableFactory;
	UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	
	private CyNetworkView netView;

	@Before
	public void setUp() throws Exception {
		TableTestSupport tblTestSupport = new TableTestSupport();
		tableFactory = tblTestSupport.getTableFactory();
		CyTableManager tableMgr = mock(CyTableManager.class);
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);

		netView = mock(CyNetworkView.class);
		when(netView.getSUID()).thenReturn(101L);
		
		addUnrecognizedVisualProperties();
	}

	@Test
	public void testTablesSavePolicy() {
		CyTable rendererTbl = unrecognizedVisualPropertyMgr.rendererTablesMap.get(netView.getSUID());
		assertEquals(SavePolicy.DO_NOT_SAVE, rendererTbl.getSavePolicy());
		
		CyTable vpTbl = unrecognizedVisualPropertyMgr.vpTablesMap.get(netView.getSUID());
		assertEquals(SavePolicy.DO_NOT_SAVE, vpTbl.getSavePolicy());
	}
	
	@Test
	public void testAddUnrecognizedVisualProperty() {
		CyTable rendererTbl = unrecognizedVisualPropertyMgr.rendererTablesMap.get(netView.getSUID());
		List<CyRow> rendererRows = rendererTbl.getAllRows();
		assertEquals(4, rendererRows.size());
		
		CyTable vpTbl = unrecognizedVisualPropertyMgr.vpTablesMap.get(netView.getSUID());
		List<CyRow> vpRows = vpTbl.getAllRows();
		assertEquals(5, vpRows.size());
	}

	@Test
	public void testGetUnrecognizedVisualProperties() {
		Map<String, String> map = null;
		View<CyNode> n = null;
		View<CyEdge> e = null;

		n = mockView(1L, CyNode.class);
		map = unrecognizedVisualPropertyMgr.getUnrecognizedVisualProperties(netView, n);
		assertEquals("val_1", map.get("att_A"));

		n =  mockView(2L, CyNode.class);
		map = unrecognizedVisualPropertyMgr.getUnrecognizedVisualProperties(netView, n);
		assertEquals("val_2", map.get("att_B"));
		assertEquals("val_3", map.get("att_C"));

		n = mockView(3L, CyNode.class);
		map = unrecognizedVisualPropertyMgr.getUnrecognizedVisualProperties(netView, n);
		assertEquals("val_4", map.get("att_C"));

		e = mockView(3L, CyEdge.class);
		map = unrecognizedVisualPropertyMgr.getUnrecognizedVisualProperties(netView, e);
		assertEquals("val_5", map.get("att_A"));
	}

	private void addUnrecognizedVisualProperties() {
		// add some unrecognized visual properties
		// nodes
		View<CyNode> n = null;
		View<CyEdge> e = null;

		n = mockView(1L, CyNode.class);
		unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, n, "att_A", "val_1");

		n = mockView(2L, CyNode.class);
		unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, n, "att_B", "val_2");
		unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, n, "att_C", "val_3");

		n = mockView(3L, CyNode.class);
		unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, n, "att_C", "val_4");

		// edges
		e = mockView(3L, CyEdge.class);
		unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, e, "att_A", "val_5");
	}

	@SuppressWarnings("unchecked")
	private <T extends CyIdentifiable> View<T> mockView(final Long id, final Class<T> type) {
		View<T> view = mock(View.class);
		when(view.getSUID()).thenReturn(id);

		if (type == CyNode.class) {
			CyNode n = mock(CyNode.class);
			when(n.getSUID()).thenReturn(id);
			when(view.getModel()).thenReturn((T) n);
		} else if (type == CyEdge.class) {
			CyEdge e = mock(CyEdge.class);
			when(e.getSUID()).thenReturn(id);
			when(view.getModel()).thenReturn((T) e);
		}

		return view;
	}
}
