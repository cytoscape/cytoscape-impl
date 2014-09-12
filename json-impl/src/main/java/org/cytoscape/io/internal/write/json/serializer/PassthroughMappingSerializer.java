package org.cytoscape.io.internal.write.json.serializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;

public class PassthroughMappingSerializer implements VisualMappingSerializer<PassthroughMapping<?, ?>> {
	
	private static final Pattern REPLACE_INVALID_JS_CHAR_PATTERN = Pattern.compile("^[^a-zA-Z_]+|[^a-zA-Z_0-9]+");

	/**
	 * Map from Visual Property to equivalent cytoscape.js tag.
	 */
	private static final Set<VisualProperty<?>> COMPATIBLE_VP = new HashSet<VisualProperty<?>>();
	
	private final CytoscapeJsStyleConverter converter = new CytoscapeJsStyleConverter();
	
	static {
		// Text labels
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_LABEL);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_TOOLTIP);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_LABEL);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_TOOLTIP);
		
		// Numbers
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_BORDER_WIDTH);
		// TODO: Need special handler.
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_SIZE);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_WIDTH);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_HEIGHT);
		
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_WIDTH);
		
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_TRANSPARENCY);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_TRANSPARENCY);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY);
		
		// Colors
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_FILL_COLOR);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_SELECTED_PAINT);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_PAINT);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_LABEL_COLOR);
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_BORDER_PAINT);
		
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_LABEL_COLOR);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_SELECTED_PAINT);
		
		// Shapes
		COMPATIBLE_VP.add(BasicVisualLexicon.NODE_SHAPE);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		COMPATIBLE_VP.add(BasicVisualLexicon.EDGE_LINE_TYPE);
	}


	@Override
	public String serialize(final PassthroughMapping<?, ?> mapping) {
		
		final VisualProperty<?> vp = mapping.getVisualProperty();
	
		if(COMPATIBLE_VP.contains(vp) == false) {
			return null;
		}
		 
		String columnName = mapping.getMappingColumnName();
		final Matcher matcher = REPLACE_INVALID_JS_CHAR_PATTERN.matcher(columnName);
		columnName = matcher.replaceAll("_");
		return "data(" + columnName + ")";
	}
}
