package org.cytoscape.view.vizmap.gui.internal.model;

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
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.puremvc.java.multicore.patterns.proxy.Proxy;


public class AttributeSetProxy extends Proxy
							   implements ColumnDeletedListener, ColumnCreatedListener,ColumnNameChangedListener,
										  NetworkAddedListener, NetworkAboutToBeDestroyedListener {

	public static final String NAME = "AttributeSetProxy";
	
	private final Set<Class<? extends CyIdentifiable>> graphObjects;
	private final Map<CyNetwork, Map<Class<? extends CyIdentifiable>, AttributeSet>> attrSets;
	private final Map<CyNetwork, Map<Class<? extends CyIdentifiable>, Set<CyTable>>> tableSets;
	private Class<?> currentMappingType;
	
	private final ServicesUtil servicesUtil;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	@SuppressWarnings("unchecked")
	public AttributeSetProxy(final ServicesUtil servicesUtil) {
		super(NAME, new HashSet<Class<? extends CyIdentifiable>>());
		
		graphObjects = (Set<Class<? extends CyIdentifiable>>) getData();
		graphObjects.add(CyNode.class);
		graphObjects.add(CyEdge.class);
		graphObjects.add(CyNetwork.class);
		
		this.servicesUtil = servicesUtil;
		this.attrSets = new WeakHashMap<CyNetwork, Map<Class<? extends CyIdentifiable>, AttributeSet>>();
		this.tableSets = new WeakHashMap<CyNetwork, Map<Class<? extends CyIdentifiable>, Set<CyTable>>>();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public void setCurrentMappingType(final Class<?> mappingType) {
		this.currentMappingType = mappingType;
	}
	
	public Class<?> getCurrentMappingType() {
		return currentMappingType;
	}
	
	public AttributeSet getAttributeSet(final CyNetwork network, final Class<? extends CyIdentifiable> objectType) {
		if (network == null || objectType == null)
			throw new NullPointerException("Both parameters should not be null.");

		final Map<Class<? extends CyIdentifiable>, AttributeSet> attrSetMap = this.attrSets.get(network);
		
		if (attrSetMap == null)
			throw new NullPointerException("No such network registered in this mamager: " + network);

		AttributeSet attributeSet = attrSetMap.get(objectType);
		
		// Remove the attributes that don't make sense for the current mapping type
		if (currentMappingType == ContinuousMapping.class) {
			// Create another attribute set first
			final AttributeSet newAttributeSet = new AttributeSet(objectType);
			
			// Add only the numeric attributes
			for (final Map.Entry<String, Class<?>> entry : attributeSet.getAttrMap().entrySet()) {
				if (Number.class.isAssignableFrom(entry.getValue()))
					newAttributeSet.getAttrMap().put(entry.getKey(), entry.getValue());
			}
			
			// Use the new attribute set instead
			attributeSet = newAttributeSet;
		}
		
		return attributeSet;
	}

	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();

		final Map<Class<? extends CyIdentifiable>, Set<CyTable>> object2tableMap =
				new HashMap<Class<? extends CyIdentifiable>, Set<CyTable>>();
		final Map<Class<? extends CyIdentifiable>, AttributeSet> attrSetMap =
				new HashMap<Class<? extends CyIdentifiable>, AttributeSet>();

		final CyNetworkTableManager netTblMgr = servicesUtil.get(CyNetworkTableManager.class);
		
		for (final Class<? extends CyIdentifiable> objectType : graphObjects) {
			final Map<String, CyTable> tableMap = netTblMgr.getTables(network, objectType);
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
		
		attrSets.put(network, attrSetMap);
		tableSets.put(network, object2tableMap);
	}

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent e) {
		CyNetwork network = e.getNetwork();
		attrSets.remove(network);
		tableSets.remove(network);
	}
	
	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		final String newAttrName = e.getColumnName();
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyIdentifiable>, Set<CyTable>> tMap = tableSets.get(network);
			
			for (final Class<? extends CyIdentifiable> objectType : graphObjects) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				
				if (targetTables.contains(table)) {
					attrSets.get(network)
							.get(objectType)
							.getAttrMap()
							.put(newAttrName, table.getColumn(newAttrName).getType());
					return;
				}
			}
		}
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyIdentifiable>, Set<CyTable>> tMap = tableSets.get(network);
			
			for (final Class<? extends CyIdentifiable> objectType : graphObjects) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				
				if (targetTables.contains(table)) {
					attrSets.get(network).get(objectType).getAttrMap().remove(e.getColumnName());
					
					return;
				}
			}
		}
	}
	
	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		final CyTable table = e.getSource();

		for (CyNetwork network : tableSets.keySet()) {
			Map<Class<? extends CyIdentifiable>, Set<CyTable>> tMap = tableSets.get(network);
			
			for (final Class<? extends CyIdentifiable> objectType : graphObjects) {
				final Set<CyTable> targetTables = tMap.get(objectType);
				
				if (targetTables.contains(table)) {
					attrSets.get(network)
							.get(objectType)
							.getAttrMap()
							.remove(e.getOldColumnName());
					attrSets.get(network)
							.get(objectType)
							.getAttrMap()
							.put(e.getNewColumnName(), table.getColumn(e.getNewColumnName()).getType());
					
					return;
				}
			}
		}
	}
}
