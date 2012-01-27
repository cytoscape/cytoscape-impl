package org.cytoscape.psi_mi.internal.data_mapper;

public interface SchemaMapper<T> extends Mapper {
	T getModel();
	String getSchemaNamespace();
}
