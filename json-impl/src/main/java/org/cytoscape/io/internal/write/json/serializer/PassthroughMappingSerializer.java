package org.cytoscape.io.internal.write.json.serializer;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class PassthroughMappingSerializer implements VisualMappingSerializer<PassthroughMapping<?, ?>> {
	
	private static final Set<VisualProperty<?>> COMPATIBLE_VP = new HashSet<VisualProperty<?>>();
	
	static {
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_LABEL);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_LABEL);
		
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_BORDER_WIDTH);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_SIZE);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_WIDTH);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_HEIGHT);

		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_WIDTH);
	}


	@Override
	public String serialize(final PassthroughMapping<?, ?> mapping) {
		
		final VisualProperty<?> vp = mapping.getVisualProperty();
	
		if(COMPATIBLE_VP.contains(vp) == false) {
			return null;
		}
		
		return "data(" + mapping.getMappingColumnName() + ")";
	}
}
