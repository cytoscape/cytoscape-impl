package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.VisualProperty;

public class CytoscapeJsStyleConverter {

	private final Map<VisualProperty<?>, CytoscapeJsToken> vp2tag = new HashMap<VisualProperty<?>, CytoscapeJsToken>();
	private final Map<CytoscapeJsToken, VisualProperty<?>> tag2nodeVp = new HashMap<CytoscapeJsToken, VisualProperty<?>>();
	private final Map<CytoscapeJsToken, VisualProperty<?>> tag2edgeVp = new HashMap<CytoscapeJsToken, VisualProperty<?>>();

	private CytoscapeJsStyleConverter() {
	
		buildV2TMap();
	}
	
	private final void buildV2TMap() {
		vp2tag.put(NODE_FILL_COLOR, BACKGROUND_COLOR);
		vp2tag.put(NODE_LABEL_COLOR, COLOR);
		vp2tag.put(NODE_WIDTH, WIDTH);
		vp2tag.put(NODE_HEIGHT, HEIGHT);
		vp2tag.put(NODE_SHAPE, SHAPE);
		vp2tag.put(NODE_BORDER_PAINT, BORDER_COLOR);
		vp2tag.put(NODE_BORDER_WIDTH, BORDER_WIDTH);
		vp2tag.put(NODE_BORDER_TRANSPARENCY, BORDER_OPACITY);
		vp2tag.put(NODE_LABEL_FONT_SIZE, FONT_SIZE);
	}

	private final void buildT2VMap() {
		// For nodes
		tag2nodeVp.put(COLOR, NODE_LABEL_COLOR);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
		tag2nodeVp.put(BORDER_COLOR, NODE_BORDER_PAINT);
	
	
		tag2nodeVp.put(BACKGROUND_COLOR, NODE_PAINT);
		tag2nodeVp.put(WIDTH, NODE_SIZE);
		tag2nodeVp.put(SHAPE, NODE_SHAPE);
		tag2nodeVp.put(VISIBLE, NODE_VISIBLE);
		tag2nodeVp.put(SELECTED, NODE_SELECTED);
		tag2nodeVp.put(POSITION_X, NODE_X_LOCATION);
		tag2nodeVp.put(POSITION_Y, NODE_Y_LOCATION);
		tag2nodeVp.put(CONTENT, NODE_LABEL);	
		tag2nodeVp.put(BORDER_WIDTH, NODE_BORDER_WIDTH);
		tag2nodeVp.put(OPACITY, NODE_TRANSPARENCY);
		tag2nodeVp.put(BORDER_OPACITY, NODE_BORDER_TRANSPARENCY);
//		tag2nodeVp.put(NODE_LABEL_FONT_FACE);
		tag2nodeVp.put(TEXT_OPACITY, NODE_LABEL_TRANSPARENCY);
	
	}
	
	
	public <T extends CyIdentifiable> VisualProperty<?> getVisualProperty(CytoscapeJsToken tag, Class<T> type) {
		return null;
	}


	public CytoscapeJsToken getTag(final VisualProperty<?> vp) {
		return null;
	}
}