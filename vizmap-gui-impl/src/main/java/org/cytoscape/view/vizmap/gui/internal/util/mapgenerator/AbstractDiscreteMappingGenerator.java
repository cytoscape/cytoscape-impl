package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;

public abstract class AbstractDiscreteMappingGenerator<V> implements
		DiscreteMappingGenerator<V> {

	private final Class<V> type;
	
	public AbstractDiscreteMappingGenerator(final Class<V> type) {
		this.type = type;
	}
	
	@Override
	public Class<V> getDataType() {
		return type;
	}

}
