package org.cytoscape.model.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;

public class CyTableMetadataImpl implements CyTableMetadata {

	private final Class<?> type;
	private final CyTable table;
	private final CyNetwork network;
	private final String namespace;

	public CyTableMetadataImpl(Class<?> type, CyTable table, CyNetwork network, String namespace) {
		this.type = type;
		this.table = table;
		this.network = network;
		this.namespace = namespace;
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public CyTable getTable() {
		return table;
	}

	@Override
	public CyNetwork getNetwork() {
		return network;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

}
