package org.cytoscape.ding.dependency;

import org.cytoscape.ding.impl.DVisualLexicon;
import org.cytoscape.view.vizmap.gui.AbstractVisualPropertyDependency;

public class EdgePaintToArrowHeadPaintDependency extends
		AbstractVisualPropertyDependency {

	private static final String NAME = "Make arrow color matches to edge color";

	public EdgePaintToArrowHeadPaintDependency() {
		super(NAME);

		group.add(DVisualLexicon.EDGE_SOURCE_ARROW_SELECTED_PAINT);
		group.add(DVisualLexicon.EDGE_TARGET_ARROW_SELECTED_PAINT);
		group.add(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		group.add(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT);
		
		group.add(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
		group.add(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
	}
}