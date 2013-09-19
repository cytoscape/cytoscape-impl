package org.cytoscape.io.internal.write.json.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class PassthroughMappingSerializer implements VisualMappingSerializer<PassthroughMapping<?, ?>> {
	
	/**
	 * Map from Visual Property to equivalent cytoscape.js tag.
	 */
	private static final Map<VisualProperty<?>, String> COMPATIBLE_VP = new HashMap<VisualProperty<?>, String>();
	
	static {
		COMPATIBLE_VP.put(BasicVisualLexicon.NODE_LABEL, "content");
		COMPATIBLE_VP.put(BasicVisualLexicon.EDGE_LABEL, "content");
		
		COMPATIBLE_VP.put(BasicVisualLexicon.NODE_BORDER_WIDTH, "border-width");
		COMPATIBLE_VP.put(BasicVisualLexicon.NODE_SIZE, "width,height");
		COMPATIBLE_VP.put(BasicVisualLexicon.NODE_WIDTH, "width");
		COMPATIBLE_VP.put(BasicVisualLexicon.NODE_HEIGHT, "height");

		COMPATIBLE_VP.put(BasicVisualLexicon.EDGE_WIDTH, "width");
	}


	@Override
	public String serialize(final PassthroughMapping<?, ?> mapping) {
		
		final VisualProperty<?> vp = mapping.getVisualProperty();
	
		Set<VisualProperty<?>> terms = COMPATIBLE_VP.keySet();
		if(terms.contains(vp) == false) {
			return null;
		}
		
		return "data(" + mapping.getMappingColumnName() + ")";
	}
}
