package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;

public class AttributeSetManager implements ColumnDeletedListener, ColumnCreatedListener, NetworkAddedListener, NetworkAboutToBeDestroyedListener {

	private static final Set<Class<? extends CyIdentifiable>> GRAPH_OBJECTS;

	static {
		GRAPH_OBJECTS = new HashSet<Class<? extends CyIdentifiable>>();
		GRAPH_OBJECTS.add(CyNode.class);
		GRAPH_OBJECTS.add(CyEdge.class);
		GRAPH_OBJECTS.add(CyNetwork.class);
	}

	private final CyNetworkTableManager tableMgr;

	private final Map<CyNetwork, Map<Class<? extends CyIdentifiable>, AttributeSet>> attrSets;
	private final Map<CyNetwork, Map<Class<? extends CyIdentifiable>, Set<CyTable>>> tableSets;
	
	public AttributeSetManager(final CyNetworkTableManager tableMgr) {
		this.tableMgr = tableMgr;

		this.attrSets = new WeakHashMap<CyNetwork, Map<Class<? extends CyIdentifiable>, AttributeSet>>();
		this.tableSets = new WeakHashMap<CyNetwork, Map<Class<? extends CyIdentifiable>, Set<CyTable>>>();
	}

	public AttributeSet getAttributeSet(final CyNetwork network, final Class<? extends CyIdentifiable> objectType) {
		if (network == null || objectType == null)
			throw new NullPointerException("Both parameters should not be null.");

		final Map<Class<? extends CyIdentifiable>, AttributeSet> attrSetMap = this.attrSets.get(network);
		if (attrSetMap == null)
			throw new NullPointerException("No such network registered in this mamager: " + network);

		return attrSetMap.get(objectType);
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();

		final Map<Class<? extends CyIdentifiable>, Set<CyTable>> object2tableMap = new HashMap<Class<? extends CyIdentifiable>, Set<CyTable>>();
		final Map<Class<? extends CyIdentifiable>, AttributeSet> attrSetMap = new HashMap<Class<? extends CyIdentifiable>, AttributeSet>();

		for (final Class<? extends CyIdentifiable> objectType : GRAPH_OBJECTS) {
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
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		CyNetwork network = e.getNetwork();
		attrSets.remove(network);
		tableSets.remove(network);
	}
	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {

		final String newAttrName = e.getColumnName();
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyIdentifiable>, Set<CyTable>> tMap = tableSets.get(network);
			for (final Class<? extends CyIdentifiable> objectType : GRAPH_OBJECTS) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				if (!targetTables.contains(table))
					continue;

				this.attrSets.get(network).get(objectType).getAttrMap()
						.put(newAttrName, table.getColumn(newAttrName).getType());
				return;
			}
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyIdentifiable>, Set<CyTable>> tMap = tableSets.get(network);
			for (final Class<? extends CyIdentifiable> objectType : GRAPH_OBJECTS) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				if (!targetTables.contains(table))
					continue;

				this.attrSets.get(network).get(objectType).getAttrMap().remove(e.getColumnName());
				return;
			}
		}
	}

}
