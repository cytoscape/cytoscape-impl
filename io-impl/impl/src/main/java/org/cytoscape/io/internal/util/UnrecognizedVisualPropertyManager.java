package org.cytoscape.io.internal.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;


public class UnrecognizedVisualPropertyManager implements NetworkViewAboutToBeDestroyedListener {
	public static final String RENDERER_TABLE_TITLE = "UnrecognizedRenderer";
	public static final String VISUAL_PROPERTY_TABLE_TITLE = "UnrecognizedVisualProperties";

	private static final String RENDERER_TABLE_PK = CyIdentifiable.SUID;
	private static final String VISUAL_PROPERTY_TABLE_PK = CyIdentifiable.SUID;

	private final CyTableFactory tableFactory;
	private final CyTableManager tableMgr;

	Map<Long/*netViewId*/, CyTable> rendererTablesMap;
	Map<Long/*netViewId*/, CyTable> vpTablesMap;

	public UnrecognizedVisualPropertyManager(final CyTableFactory tableFactory, final CyTableManager tableMgr) {
		this.tableFactory = tableFactory;
		this.tableMgr = tableMgr;
		
		rendererTablesMap = new HashMap<Long, CyTable>();
		vpTablesMap = new HashMap<Long, CyTable>();
	}

	/**
	 * Add the visual property which is not recognized by the rendering engine to a global table,
	 * for future references.
	 * @param netView The CyNetworkView which contains the node or edge view.
	 * @param view The node or edge view which has the unrecognized visual property.
	 * @param attName The visual property name (e.g. an XGMML or GML visual attribute name).
	 * @param attValue The visual property string value.
	 */
	public void addUnrecognizedVisualProperty(CyNetworkView netView,
											  View<? extends CyIdentifiable> view,
											  String attName,
											  String attValue) {
		if (netView == null) throw new IllegalArgumentException("The 'netView' argument cannot be null");
		if (view == null) throw new IllegalArgumentException("The 'view' argument cannot be null");
		if (attName == null) throw new IllegalArgumentException("The 'attName' argument cannot be null");

		// 1. Store the unrecognized renderer
		CyTable rendererTbl = rendererTablesMap.get(netView.getSUID());

		if (rendererTbl == null) {
			createTables(netView.getSUID());
			rendererTbl = rendererTablesMap.get(netView.getSUID());
		}

		Collection<CyRow> rendererRows = rendererTbl.getMatchingRows("att_name", attName);
		String targetType = getTargetType(view);
		Long rendererId = null;

		for (CyRow r : rendererRows) {
			// att_name + target_type should be unique
			if (targetType.equals(r.get("target_type", String.class))) {
				// there is already a row for this renderer: just get its PK
				rendererId = rendererRows.iterator().next().get(RENDERER_TABLE_PK, Long.class);
				break;
			}
		}

		if (rendererId == null) {
			// att_name + target_type NOT found: add new renderer row
			rendererId = SUIDFactory.getNextSUID();
			CyRow newRow = rendererTbl.getRow(rendererId);
			newRow.set("att_name", attName);
			newRow.set("target_type", targetType);
		}

		// 2. Store the visual property value for this element
		CyTable vpTbl = vpTablesMap.get(netView.getSUID());

		CyRow newRow = vpTbl.getRow(SUIDFactory.getNextSUID());
		newRow.set("att_id", rendererId);
		newRow.set("att_value", attValue);
		newRow.set("target_id", view.getSUID());
	}

	/**
	 * Return all the unrecognized visual property names and values of a network, node or edge.
	 * @param netView The CyNetworkView which contains the node or edge view.
	 * @param view Node or edge view.
	 * @return A map which has unrecognized visual property names as keys and visual property string values as values.
	 */
	public Map<String, String> getUnrecognizedVisualProperties(CyNetworkView netView, View<? extends CyIdentifiable> view) {
		if (netView == null) throw new IllegalArgumentException("The 'netView' argument cannot be null");
		if (view == null) throw new IllegalArgumentException("The 'view' argument cannot be null");

		Map<String, String> map = new HashMap<String, String>();

		CyTable rendererTbl = rendererTablesMap.get(netView.getSUID());
		CyTable vpTbl = vpTablesMap.get(netView.getSUID());

		if (rendererTbl != null && vpTbl != null) {
			String targetType = getTargetType(view);
			Collection<CyRow> rows = vpTbl.getMatchingRows("target_id", view.getSUID());

			for (CyRow r : rows) {
				// also make sure the target type is the same
				Long rendererId = r.get("att_id", Long.class);
				CyRow rendererRow = rendererTbl.getRow(rendererId);

				if (targetType.equals(rendererRow.get("target_type", String.class))) {
					String attName = r.get("att_name_vc", String.class); // virtual column
					String attValue = r.get("att_value", String.class);
					map.put(attName, attValue);
				}
			}
		}

		return map;
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		dropTables(e.getNetworkView());
	}
	
	private String getTargetType(View<? extends CyIdentifiable> view) {
		CyIdentifiable model = view.getModel();

		String type = "network";
		if (model instanceof CyNode) type = "node";
		if (model instanceof CyEdge) type = "edge";

		return type;
	}

	private void createTables(Long netViewId) {
		/*
		 * Table which stores all unrecognized property renderers.
		 * 
		 * - "SUID" (Long): Primary key
		 * - "att_name" (String): The unrecognized visual property name
		 * - "target_type"(String): One of "network", "node", "edge"
		 */
		CyTable rendererTbl = tableFactory.createTable(RENDERER_TABLE_TITLE + netViewId,
		                                               RENDERER_TABLE_PK, Long.class, false,
		                                               true);
		tableMgr.addTable(rendererTbl);
		rendererTbl.setSavePolicy(SavePolicy.DO_NOT_SAVE);
		rendererTbl.createColumn("att_name", String.class, false);
		rendererTbl.createColumn("target_type", String.class, false);

		/*
		 * Table which stores all unrecognized visual properties (and values) for each node or edge.
		 * 
		 * - "SUID" (Long): Primary key
		 * - "att_value" (String): The visual property value
		 * - "target_id" (Long): CyNode/CyEdge SUID
		 * - "att_id" (Long): The "att_name_vc" virtual column join key
		 */
		CyTable vpTbl = tableFactory.createTable(VISUAL_PROPERTY_TABLE_TITLE + netViewId,
		                                         VISUAL_PROPERTY_TABLE_PK, Long.class, false,
		                                         true);
		tableMgr.addTable(vpTbl);
		vpTbl.setSavePolicy(SavePolicy.DO_NOT_SAVE);
		vpTbl.createColumn("att_id", Long.class, false);
		vpTbl.createColumn("att_value", String.class, false);
		vpTbl.createColumn("target_id", Long.class, false);
		vpTbl.addVirtualColumn("att_name_vc", "att_name", rendererTbl, "att_id", false);

		// add tables to the internal maps
		rendererTablesMap.put(netViewId, rendererTbl);
		vpTablesMap.put(netViewId, vpTbl);
	}

	private void dropTables(CyNetworkView view) {
		Long netViewId = view.getSUID();

		CyTable vpTbl = vpTablesMap.get(netViewId);

		if (vpTbl != null) {
			tableMgr.deleteTable(vpTbl.getSUID());
			vpTablesMap.remove(netViewId);
		}

		CyTable rendererTbl = rendererTablesMap.get(netViewId);

		if (rendererTbl != null) {
			tableMgr.deleteTable(rendererTbl.getSUID());
			rendererTablesMap.remove(netViewId);
		}
	}
}
