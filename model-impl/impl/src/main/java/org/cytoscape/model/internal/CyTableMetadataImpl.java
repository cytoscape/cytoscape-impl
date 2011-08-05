package org.cytoscape.model.internal;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;

public class CyTableMetadataImpl implements CyTableMetadata {

	private final Class<?> type;
	private final CyTable table;
	private final Set<CyNetwork> networks;
	private final String namespace;

	public CyTableMetadataImpl(Class<?> type, CyTable table, Set<CyNetwork> networks, String namespace) {
		this.type = type;
		this.table = table;
		this.networks = networks;
		this.namespace = namespace;
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public CyTable getCyTable() {
		return table;
	}

	@Override
	public Set<CyNetwork> getCyNetworks() {
		return networks;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

}
