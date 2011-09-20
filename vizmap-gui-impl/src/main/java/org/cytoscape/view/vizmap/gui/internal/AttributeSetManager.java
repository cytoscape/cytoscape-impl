package org.cytoscape.view.vizmap.gui.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AttributeSetManager implements ColumnDeletedListener,
		ColumnCreatedListener, NetworkAddedListener
{
	private static final Logger logger = LoggerFactory.getLogger(AttributeSetManager.class);

	private static final Set<Class<? extends CyTableEntry>> GRAPH_OBJECTS;
	static {
		GRAPH_OBJECTS = new HashSet<Class<? extends CyTableEntry>>();
		GRAPH_OBJECTS.add(CyNode.class);
		GRAPH_OBJECTS.add(CyEdge.class);
		GRAPH_OBJECTS.add(CyNetwork.class);
	}

	private final CyNetworkTableManager tableMgr;

	private final Map<CyNetwork, Map<Class<? extends CyTableEntry>, AttributeSet>> attrSets;
	private final Map<CyNetwork, Map<Class<? extends CyTableEntry>, Set<CyTable>>> tableSets;

	public AttributeSetManager(final CyNetworkTableManager tableMgr) {
		this.tableMgr = tableMgr;

		this.attrSets = new HashMap<CyNetwork, Map<Class<? extends CyTableEntry>, AttributeSet>>();
		this.tableSets = new HashMap<CyNetwork, Map<Class<? extends CyTableEntry>, Set<CyTable>>>();
		

//		final Set<CyNetwork> networkSet = networkMgr.getNetworkSet();
//		for (final CyNetwork net : networkSet) {
//			final Map<String, CyTable> tableMap = tableMgr.getTableMap(name, net);
//			targetTables.addAll(tableMap.values());
//
//			for (final CyTable table : tableMap.values())
//				attrNames.addAll(table.getColumnMap().keySet());
//		}
	}

	public AttributeSet getAttributeSet(final CyNetwork network, final Class<? extends CyTableEntry> objectType) {
		if (network == null || objectType == null)
			throw new NullPointerException(
					"Both parameters should not be null.");

		final Map<Class<? extends CyTableEntry>, AttributeSet> attrSetMap = this.attrSets.get(network);
		if (attrSetMap == null)
			throw new NullPointerException(
					"No such network registered in this mamager: " + network);

		return attrSetMap.get(objectType);
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();
		
		logger.debug("@@@@@@ Attr Set manager got new network." + network.getSUID());
		
		final Map<Class<? extends CyTableEntry>, Set<CyTable>> object2tableMap = new HashMap<Class<? extends CyTableEntry>, Set<CyTable>>();
		final Map<Class<? extends CyTableEntry>, AttributeSet> attrSetMap = new HashMap<Class<? extends CyTableEntry>, AttributeSet>();

		for (final Class<? extends CyTableEntry> objectType : GRAPH_OBJECTS) {
			final Map<String, CyTable> tableMap = tableMgr.getTables(network, objectType);
			final Collection<CyTable> tables = tableMap.values();

			object2tableMap.put(objectType, new HashSet<CyTable>(tables));

			final AttributeSet attrSet = new AttributeSet(objectType);
			for (CyTable table : tables) {
				final Collection<CyColumn> columns = table.getColumns();
				for (final CyColumn column : columns) {
					final Class<?> type = column.getType();
					attrSet.getAttrMap().put(column.getName(), type);
				}
			}
			attrSetMap.put(objectType, attrSet);

		}
		this.attrSets.put(network, attrSetMap);
		this.tableSets.put(network, object2tableMap);
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		
		final String newAttrName = e.getColumnName();
		final CyTable table = e.getSource();
		
		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyTableEntry>, Set<CyTable>> tMap = tableSets.get(network);
			for (final Class<? extends CyTableEntry> objectType : GRAPH_OBJECTS) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				if (!targetTables.contains(table))
					continue;

				this.attrSets.get(network)
					.get(objectType)
					.getAttrMap()
					.put(newAttrName,
					     table.getColumn(newAttrName).getType());
				return;
			}
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyTableEntry>, Set<CyTable>> tMap = tableSets.get(network);
			for (final Class<? extends CyTableEntry> objectType : GRAPH_OBJECTS) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				if (!targetTables.contains(table))
					continue;

				this.attrSets.get(network).get(objectType).getAttrMap()
						.remove(e.getColumnName());
				return;
			}
		}
	}

}
