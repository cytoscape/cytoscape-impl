package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;

public class CyNetworkTableManagerImpl implements CyNetworkTableManager {

	private final Map<CyNetwork, Map<Class<? extends CyIdentifiable>, Map<String, CyTable>>> tables;
	
	public CyNetworkTableManagerImpl() {
		// Use WeakReferences for CyNetworks because we can't get notified
		// when detached networks are no longer in use.  Use WeakReferences
		// for the CyTable maps too because CyNetworks may be holding a
		// reference to them.  This set up allows us to automatically clean
		// up this map whenever CyNetworks get garbage collected.
		tables = new WeakHashMap<CyNetwork, Map<Class<? extends CyIdentifiable>, Map<String, CyTable>>>();
	}
	
	@Override
	public void setTable(CyNetwork network, Class<? extends CyIdentifiable> type, String namespace, CyTable table) {		
		// Null checks.  All parameters should not be null.
		if (network == null)
			throw new IllegalArgumentException("network cannot be null");
		
		if (type == null)
			throw new IllegalArgumentException("type cannot be null");
		
		if (namespace == null)
			throw new IllegalArgumentException("namespace cannot be null");
		
		if (table == null)
			throw new IllegalArgumentException("table cannot be null");
		
		Map<Class<? extends CyIdentifiable>, Map<String, CyTable>> byType = tables.get(network);
		if (byType == null) {
			byType = new HashMap<Class<? extends CyIdentifiable>, Map<String,CyTable>>();
			final Map<String, CyTable> type2Tables = new HashMap<String, CyTable>();
			type2Tables.put(namespace, table);
			byType.put(type, type2Tables);
			tables.put(network, byType);
			return;
		}
		
		Map<String, CyTable> reference = byType.get(type);
		if (reference == null) {
			final Map<String, CyTable> type2Tables = new HashMap<String, CyTable>();
			type2Tables.put(namespace, table);
			byType.put(type, type2Tables);
			tables.put(network, byType);
			return;
		
		}

		if (namespace.equals(CyNetwork.DEFAULT_ATTRS) && reference.get(CyNetwork.DEFAULT_ATTRS) != null)
			throw new IllegalArgumentException("cannot overwrite default tables");
		
		reference.put(namespace, table);
	}

	@Override
	public CyTable getTable(CyNetwork network, Class<? extends CyIdentifiable> type, String namespace) {
		Map<Class<? extends CyIdentifiable>, Map<String, CyTable>> byType = tables.get(network);
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (namespace == null)
			throw new IllegalArgumentException("namespace cannot be null");

		if (byType == null)
			return null;

		final Map<String, CyTable> reference = byType.get(type);
		if (reference == null)
			return null;

		return reference.get(namespace);
	}

	@Override
	public void removeTable(CyNetwork network,
			Class<? extends CyIdentifiable> type, String namespace) {
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (namespace == null) {
			throw new IllegalArgumentException("namespace cannot be null");
		}

		if (namespace.equals(CyNetwork.DEFAULT_ATTRS)) {
			throw new IllegalArgumentException("cannot remove default tables");
		}
		
		Map<Class<? extends CyIdentifiable>, Map<String, CyTable>> byType = tables.get(network);
		if (byType == null) {
			return;
		}
		Map<String, CyTable> reference = byType.get(type);
		if (reference == null) {
			return;
		}

		reference.remove(namespace);
	}

	@Override
	public Map<String, CyTable> getTables(CyNetwork network, Class<? extends CyIdentifiable> type) {
		if (network == null)
			throw new IllegalArgumentException("network cannot be null");
		
		if (type == null)
			throw new IllegalArgumentException("type cannot be null");

		final Map<Class<? extends CyIdentifiable>, Map<String, CyTable>> byType = tables.get(network);
		if (byType == null)
			return Collections.emptyMap();
		
		final Map<String, CyTable> namespace2tableMap = byType.get(type);
		
		if (namespace2tableMap == null)
			return Collections.emptyMap();
		
		return Collections.unmodifiableMap(namespace2tableMap);
	}

	@Override
	public void reset() {
		tables.clear();
	}

	@Override
	public Set<CyNetwork> getNetworkSet() {
		return Collections.unmodifiableSet(tables.keySet());
	}
	
	@Override
	public void removeAllTables(CyNetwork network) {
		tables.remove(network);
	}
}
