package org.cytoscape.io.internal.read.session;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;

public class CyTableMetadataImpl implements CyTableMetadata {

	private final Class<?> type;
	private final CyTable table;
	private final CyNetwork network;
	private final String namespace;

	public CyTableMetadataImpl(CyTableMetadataBuilder builder) {
		this.type = builder.type;
		this.table = builder.table;
		this.network = builder.network;
		this.namespace = builder.namespace;
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

	public static class CyTableMetadataBuilder implements CyTableMetadata {
		private Class<?> type;
		private CyTable table;
		private CyNetwork network;
		private String namespace;
		
		public CyTableMetadataBuilder setCyTable(CyTable table) {
			this.table = table;
			return this;
		}
		
		public CyTableMetadataBuilder setNetwork(CyNetwork network) {
			this.network = network;
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
		public CyTable getTable() {
			return table;
		}

		@Override
		public String getNamespace() {
			return namespace;
		}
		
		@Override
		public CyNetwork getNetwork() {
			return network;
		}
		
		@Override
		public Class<?> getType() {
			return type;
		}
	}
}
