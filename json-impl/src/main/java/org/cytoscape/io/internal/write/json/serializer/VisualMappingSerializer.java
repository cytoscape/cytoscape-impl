package org.cytoscape.io.internal.write.json.serializer;

import org.cytoscape.view.vizmap.VisualMappingFunction;

public interface VisualMappingSerializer <T extends VisualMappingFunction<?, ?>> {

	String serialize(final T mapping);
	
	String getTag(final T Mapping);
	
}
