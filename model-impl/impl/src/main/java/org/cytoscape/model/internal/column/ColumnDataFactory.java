package org.cytoscape.model.internal.column;

import java.util.List;

public interface ColumnDataFactory {

	ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize);
	
	List<?> createList(Class<?> elementType, List<?> data);

	void clearCache();

	
	public static ColumnDataFactory createDefaultFactory() {
		return new ColumnDataFactoryFastUtil();
	}
}
