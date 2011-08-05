package org.cytoscape.io.internal.read.session;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;

public class CyTableMetadataImpl implements CyTableMetadata {

	private final Class<?> type;
	private final CyTable table;
	private final Set<CyNetwork> networks;
	private final String namespace;

	public CyTableMetadataImpl(CyTableMetadataBuilder builder) {
		this.type = builder.type;
		this.table = builder.table;
		this.networks = builder.networks;
		this.namespace = builder.namespace;
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

	public static class CyTableMetadataBuilder implements CyTableMetadata {
		private Class<?> type;
		private CyTable table;
		private Set<CyNetwork> networks;
		private String namespace;
		
		public CyTableMetadataBuilder setCyTable(CyTable table) {
			this.table = table;
			return this;
		}
		
		public CyTableMetadataBuilder setNetworks(Set<CyNetwork> networks) {
			this.networks = networks;
			return this;
		}
		
		public CyTableMetadataBuilder setType(Class<?> type) {
			this.type = type;
			return this;
		}
		
		public CyTableMetadataBuilder setNamespace(String namespace) {
			this.namespace = namespace;
			return this;
		}
				
		public CyTableMetadata build() {
			return new CyTableMetadataImpl(this);
		}

		@Override
		public CyTable getCyTable() {
			return table;
		}

		@Override
		public String getNamespace() {
			return namespace;
		}
		
		@Override
		public Set<CyNetwork> getCyNetworks() {
			return networks;
		}
		
		@Override
		public Class<?> getType() {
			return type;
		}
	}
}
