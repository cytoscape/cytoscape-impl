package org.cytoscape.model.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;

/** 
 * When nodes/edges are deleted from a subnetwork they need to be deleted from the root network.
 * Otherwise there will be memory leaks and the session file never gets smaller.
 * 
 * The general solution is to auto-delete the nodes/edges from the root network when they are no longer
 * contained in any of its sub-networks. However, the CySubNetwork.addNode(CyNode) and CySubNetwork.addEdge(CyEdge)
 * methods are problematic because they allow nodes/edges to be restored to the subnetwork
 * after they have been removed. (These methods are used by the Undo task.)
 * 
 * This cache remembers the attributes for nodes/edges that have been auto-deleted
 * from the root network, so that those attributes can be restored. If all the strong references
 * to a CyNode or CyEdge are gone, then the attributes can be removed from this cache by
 * the Java garbage collector (using WeakReferences). Note, its important that there aren't
 * memory leaks elsewhere in Cytoscape or the attributes in this cache won't get cleaned up.
 * 
 * Fix for bug 3135.
 */
public class RemovedAttributesCache {
	
	private final String[] namespaces = { CyNetwork.DEFAULT_ATTRS, CyRootNetwork.SHARED_ATTRS, CyNetwork.HIDDEN_ATTRS };
	
	private final WeakHashMap<CyIdentifiable,AttributesCache> cachedElements;
	private final CyNetwork network;
	
	
	
	/**
	 * An implementation of CyRow that gets its attributes from this cache.
	 * Note: The column names are not currently being normalized.
	 * 
	 * MKTODO: If I don't need to change CySubNetworkImpl.copyTableData then I don't need this wrapper class.
	 * I can just keep a reference to Map<String,Object> directly.
	 */
	private static class CachedRow implements CyRow {
		
		private final Map<String,Object> values;
		
		public CachedRow(Map<String,Object> values) {
			this.values = new HashMap<>(values);
		}
		
		public CachedRow(CyRow row) {
			this(row.getAllValues());
		}
		
		@Override
		public <T> T get(String columnName, Class<? extends T> type) {
			return type.cast(values.get(columnName));
		}

		@Override
		public <T> T get(String columnName, Class<? extends T> type, T defaultValue) {
			return type.cast(values.getOrDefault(columnName, defaultValue));
		}

		@Override
		public <T> List<T> getList(String columnName, Class<T> listElementType) {
			return (List<T>) values.get(columnName);
		}

		@Override
		public <T> List<T> getList(String columnName, Class<T> listElementType, List<T> defaultValue) {
			return (List<T>) values.getOrDefault(columnName, defaultValue);
		}

		@Override
		public <T> void set(String columnName, T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isSet(String columnName) {
			return values.containsKey(columnName);
		}

		@Override
		public Map<String, Object> getAllValues() {
			return Collections.unmodifiableMap(values);
		}

		@Override
		public Object getRaw(String columnName) {
			return values.get(columnName);
		}

		@Override
		public CyTable getTable() {
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static class AttributesCache {
		
		private Map<String,CachedRow> allAttributes;
		
		AttributesCache() {
			allAttributes = new HashMap<>();
		}
		
		void put(String namespace, CyRow row) {
			allAttributes.put(namespace, new CachedRow(row));
		}
		
		CyRow get(String namespace) {
			return allAttributes.get(namespace);
		}
	}
	
	
	
	public RemovedAttributesCache(CyNetwork network) {
		this.cachedElements = new WeakHashMap<>();
		this.network = network;
	}
	
	
	public void cache(Collection<? extends CyIdentifiable> elements) {
		// The CyRow objects get cleared out when they get removed from the table, so we need to hold onto the raw attribute values
		for(CyIdentifiable element : elements) {
			AttributesCache attributesCache = new AttributesCache();
			for(String namespace : namespaces) {
				attributesCache.put(namespace, network.getRow(element, namespace));
			}
			cachedElements.put(element, attributesCache);
		}
	}
	
	
	public void restore(CyIdentifiable element) {
		AttributesCache attributesCache = cachedElements.remove(element);
		if(attributesCache != null) {
			for(String namespace : namespaces) {
				CyRow cachedRow = attributesCache.get(namespace);
				Map<String,Object> attributes = cachedRow.getAllValues();
				CyRow realRow = getRow(element, namespace);
				if(realRow != null) {
					for(Map.Entry<String, Object> attribute : attributes.entrySet()) {
						realRow.set(attribute.getKey(), attribute.getValue());
					}
				}
			}
		}
	}
	
	
	private CyRow getRow(CyIdentifiable element, String namespace) {
		CyTable table = null;
		if (element instanceof CyNode)
			table = network.getTable(CyNode.class, namespace);
		else if (element instanceof CyEdge)
			table = network.getTable(CyEdge.class, namespace);
		
		return table == null ? null : table.getRow(element.getSUID());
	}
	
	public void evict(CyIdentifiable element) {
		cachedElements.remove(element);
	}
	
	public void dispose() {
		cachedElements.clear();
	}
	
	public boolean contains(CyIdentifiable elemement) {
		return cachedElements.containsKey(elemement);
	}
	
	public CyRow getAttributes(CyIdentifiable element, String namespace) {
		AttributesCache attributesCache = cachedElements.get(element);
		if(attributesCache != null) {
			return attributesCache.get(namespace);
		}
		return null;
	}


	public int size() {
		return cachedElements.size();
	}
	
	@Override
	public String toString() {
		long nodeCount = cachedElements.keySet().stream().filter(x -> CyNode.class.isAssignableFrom(x.getClass())).count();
		long edgeCount = cachedElements.keySet().stream().filter(x -> CyEdge.class.isAssignableFrom(x.getClass())).count();
		return "RemovedAttributesCache[nodes=" + nodeCount + ", edges=" + edgeCount +"]";
	}
}
