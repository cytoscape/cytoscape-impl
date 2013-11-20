package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;

public class CytoscapeJsStyleConverter {

	private final Map<VisualProperty<?>, CytoscapeJsToken> vp2tag = new HashMap<VisualProperty<?>, CytoscapeJsToken>();
	private final Map<CytoscapeJsToken, VisualProperty<?>> tag2nodeVp = new HashMap<CytoscapeJsToken, VisualProperty<?>>();
	private final Map<CytoscapeJsToken, VisualProperty<?>> tag2edgeVp = new HashMap<CytoscapeJsToken, VisualProperty<?>>();


	public CytoscapeJsStyleConverter() {
		buildV2TMap();
		buildT2VMap();
	}


	/**
	 * Map from Cytoscape Visual Properties to Cytoscape.js tag
	 * This map will be used when Cytoscape generate JSON file 
	 * FROM existing CYtoscape Visual Style.
	 */
	private final void buildV2TMap() {

		///////////////// For nodes ///////////////////
		vp2tag.put(NODE_BORDER_PAINT, BORDER_COLOR);
		vp2tag.put(NODE_BORDER_TRANSPARENCY, BORDER_OPACITY);
		vp2tag.put(NODE_BORDER_WIDTH, BORDER_WIDTH);

		vp2tag.put(NODE_FILL_COLOR, BACKGROUND_COLOR);
		vp2tag.put(NODE_TRANSPARENCY, BACKGROUND_OPACITY);

		vp2tag.put(NODE_LABEL, CONTENT);
		vp2tag.put(NODE_LABEL_COLOR, COLOR);
		vp2tag.put(NODE_LABEL_FONT_SIZE, FONT_SIZE);
		vp2tag.put(NODE_LABEL_FONT_FACE, FONT_FAMILY);
		vp2tag.put(NODE_LABEL_TRANSPARENCY, TEXT_OPACITY);

		vp2tag.put(NODE_SHAPE, SHAPE);

		vp2tag.put(NODE_HEIGHT, HEIGHT);
		vp2tag.put(NODE_WIDTH, WIDTH);

		// For selected
		vp2tag.put(NODE_SELECTED_PAINT, BACKGROUND_COLOR);

		// TODO: Ding-dependent visual properties?
		
		// For edges
		vp2tag.put(EDGE_WIDTH, WIDTH);

		vp2tag.put(EDGE_LABEL, CONTENT); 
		vp2tag.put(EDGE_LABEL_FONT_SIZE, FONT_SIZE);
		vp2tag.put(EDGE_LABEL_FONT_FACE, FONT_FAMILY);
		vp2tag.put(EDGE_LABEL_COLOR, COLOR);
		vp2tag.put(EDGE_LABEL_TRANSPARENCY, TEXT_OPACITY);

		vp2tag.put(EDGE_LINE_TYPE, LINE_STYLE);

		vp2tag.put(EDGE_STROKE_UNSELECTED_PAINT, LINE_COLOR);
		vp2tag.put(EDGE_TRANSPARENCY, OPACITY);

		vp2tag.put(EDGE_SOURCE_ARROW_SHAPE, SOURCE_ARROW_SHAPE);
		vp2tag.put(EDGE_TARGET_ARROW_SHAPE, TARGET_ARROW_SHAPE);
		
		// For selected
		vp2tag.put(EDGE_SELECTED_PAINT, LINE_COLOR);
	}


	/**
	 * Map from Cytoscape.js tag to Visual Property
	 * 
	 */
	private final void buildT2VMap() {
		// For nodes
		tag2nodeVp.put(COLOR, NODE_LABEL_COLOR);
		tag2nodeVp.put(BACKGROUND_COLOR, NODE_PAINT);
		tag2nodeVp.put(WIDTH, NODE_WIDTH);
		tag2nodeVp.put(HEIGHT, NODE_HEIGHT);
		tag2nodeVp.put(SHAPE, NODE_SHAPE);
		tag2nodeVp.put(VISIBLE, NODE_VISIBLE);
		tag2nodeVp.put(SELECTED, NODE_SELECTED);
		tag2nodeVp.put(POSITION_X, NODE_X_LOCATION);
		tag2nodeVp.put(POSITION_Y, NODE_Y_LOCATION);
		tag2nodeVp.put(CONTENT, NODE_LABEL);	
		tag2nodeVp.put(BORDER_WIDTH, NODE_BORDER_WIDTH);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(OPACITY, NODE_TRANSPARENCY);
		tag2nodeVp.put(BACKGROUND_OPACITY, NODE_TRANSPARENCY);
		tag2nodeVp.put(BORDER_OPACITY, NODE_BORDER_TRANSPARENCY);
		tag2nodeVp.put(TEXT_OPACITY, NODE_LABEL_TRANSPARENCY);
		tag2nodeVp.put(FONT_SIZE, NODE_LABEL_FONT_SIZE);

		// For edges
		tag2edgeVp.put(COLOR, EDGE_LABEL_COLOR);
		tag2edgeVp.put(LINE_COLOR, EDGE_UNSELECTED_PAINT);
		tag2edgeVp.put(VISIBILITY, EDGE_VISIBLE);
		tag2edgeVp.put(SELECTED, EDGE_SELECTED);
		tag2edgeVp.put(WIDTH, EDGE_WIDTH);
		tag2edgeVp.put(CONTENT, EDGE_LABEL);
		tag2edgeVp.put(LINE_STYLE, EDGE_LINE_TYPE);
		tag2edgeVp.put(FONT_SIZE, EDGE_LABEL_FONT_SIZE);
		tag2edgeVp.put(TEXT_OPACITY, EDGE_LABEL_TRANSPARENCY);
		tag2edgeVp.put(OPACITY, EDGE_TRANSPARENCY);
		tag2edgeVp.put(SOURCE_ARROW_SHAPE, EDGE_SOURCE_ARROW_SHAPE);
		tag2edgeVp.put(TARGET_ARROW_SHAPE, EDGE_TARGET_ARROW_SHAPE);
	}


	public <T extends CyIdentifiable> VisualProperty<?> getVisualProperty(CytoscapeJsToken tag, Class<T> type) {
		if(type == CyNode.class) {
			return tag2nodeVp.get(tag);
		} else {
			return tag2edgeVp.get(tag);
		}
	}

	public CytoscapeJsToken getTag(final VisualProperty<?> vp) {
		if(vp.getIdString().equals("EDGE_TARGET_ARROW_UNSELECTED_PAINT"))
			return TARGET_ARROW_COLOR;
		else if(vp.getIdString().equals("EDGE_SOURCE_ARROW_UNSELECTED_PAINT"))
			return SOURCE_ARROW_COLOR;
		else
			return vp2tag.get(vp);
	}
}