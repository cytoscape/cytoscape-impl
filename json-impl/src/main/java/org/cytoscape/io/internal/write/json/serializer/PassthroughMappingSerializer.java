package org.cytoscape.io.internal.write.json.serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class PassthroughMappingSerializer implements VisualMappingSerializer<PassthroughMapping<?, ?>> {
	
	private static final Pattern REPLACE_INVALID_JS_CHAR_PATTERN = Pattern.compile("^[^a-zA-Z_]+|[^a-zA-Z_0-9]+");

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
		 
		String columnName = mapping.getMappingColumnName();
		final Matcher matcher = REPLACE_INVALID_JS_CHAR_PATTERN.matcher(columnName);
		columnName = matcher.replaceAll("_");
		return "data(" + columnName + ")";
	}
}
