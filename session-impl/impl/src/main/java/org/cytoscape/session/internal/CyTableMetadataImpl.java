package org.cytoscape.session.internal;

/*
 * #%L
 * Cytoscape Session Impl (session-impl)
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
