package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColumnDataFactoryHashMap implements ColumnDataFactory {


	@Override
	public ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize) {
		return new MapColumn(new HashMap<>(defaultInitSize));
	}

	@Override
	public List<?> createList(Class<?> elementType, List<?> data) {
		return new ArrayList<>(data);
	}

	@Override
	public void clearCache() {
	}

}
