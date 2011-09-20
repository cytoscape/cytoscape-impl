package org.cytoscape.model.internal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;

public class CyNetworkTableManagerImpl implements CyNetworkTableManager, NetworkAboutToBeDestroyedListener {

	Map<CyNetwork, Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>>> tables;
	
	public CyNetworkTableManagerImpl() {
		// Use WeakReferences for CyNetworks because we can't get notified
		// when detached networks are no longer in use.  Use WeakReferences
		// for the CyTable maps too because CyNetworks may be holding a
		// reference to them.  This set up allows us to automatically clean
		// up this map whenever CyNetworks get garbage collected.
		tables = new WeakHashMap<CyNetwork, Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>>>();
	}
	
	@Override
	public void setTable(CyNetwork network, Class<? extends CyTableEntry> type,
			String namespace, CyTable table) {
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (namespace == null) {
			throw new IllegalArgumentException("namespace cannot be null");
		}
		if (table == null) {
			throw new IllegalArgumentException("table cannot be null");
		}
		
		if (namespace.equals(CyNetwork.DEFAULT_ATTRS)) {
			throw new IllegalArgumentException("cannot overwrite default tables");
		}
		
		Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>> byType = tables.get(network);
		if (byType == null) {
			throw new IllegalStateException("network table maps are missing for network: " + network);
		}
		Reference<Map<String, CyTable>> reference = byType.get(type);
		if (reference == null) {
			throw new IllegalStateException("network table maps are missing for network: " + network);
		}
		Map<String, CyTable> byNamespace = reference.get();
		if (byNamespace == null) {
			throw new IllegalStateException("network table maps are missing for network: " + network);
		}
		byNamespace.put(namespace, table);
	}

	@Override
	public CyTable getTable(CyNetwork network,
			Class<? extends CyTableEntry> type, String namespace) {
		Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>> byType = tables.get(network);
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (namespace == null) {
			throw new IllegalArgumentException("namespace cannot be null");
		}

		if (byType == null) {
			return null;
		}
		Reference<Map<String, CyTable>> reference = byType.get(type);
		if (reference == null) {
			return null;
		}
		Map<String, CyTable> byNamespace = reference.get();
		if (byNamespace == null) {
			return null;
		}
		return byNamespace.get(namespace);
	}

	@Override
	public void removeTable(CyNetwork network,
			Class<? extends CyTableEntry> type, String namespace) {
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
		
		Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>> byType = tables.get(network);
		if (byType == null) {
			return;
		}
		Reference<Map<String, CyTable>> reference = byType.get(type);
		if (reference == null) {
			return;
		}
		Map<String, CyTable> byNamespace = reference.get();
		if (byNamespace == null) {
			return;
		}
		byNamespace.remove(namespace);
	}

	@Override
	public Map<String, CyTable> getTables(CyNetwork network,
			Class<? extends CyTableEntry> type) {
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}

		Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>> byType = tables.get(network);
		if (byType == null) {
			return Collections.emptyMap();
		}
		Reference<Map<String, CyTable>> reference = byType.get(type);
		if (reference == null) {
			return Collections.emptyMap();
		}
		Map<String, CyTable> byNamespace = reference.get();
		if (byNamespace == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(byNamespace);
	}

	@Override
	public void reset() {
		tables.clear();
	}

	void setTableMap(Class<? extends CyTableEntry> type, CyNetwork network, Map<String, CyTable> tableMap) {
		if (network == null) {
			throw new IllegalArgumentException("network cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (tableMap == null) {
			throw new IllegalArgumentException("table map cannot be null");
		}

		Map<Class<? extends CyTableEntry>, Reference<Map<String, CyTable>>> byType = tables.get(network);
		if (byType == null) {
			byType = new HashMap<Class<? extends CyTableEntry>, Reference<Map<String,CyTable>>>();
			tables.put(network, byType);
		}
		byType.put(type, new WeakReference<Map<String,CyTable>>(tableMap));
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		tables.remove(e.getNetwork());
	}
}
