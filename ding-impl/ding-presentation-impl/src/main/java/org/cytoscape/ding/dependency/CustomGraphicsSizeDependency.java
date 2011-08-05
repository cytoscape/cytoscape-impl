package org.cytoscape.ding.dependency;

import org.cytoscape.ding.impl.DVisualLexicon;
import org.cytoscape.view.vizmap.gui.AbstractVisualPropertyDependency;

public class CustomGraphicsSizeDependency extends
		AbstractVisualPropertyDependency {
	
	private static final String NAME = "Synchronize Custom Graphics Size to Node Size";

	public CustomGraphicsSizeDependency() {
		super(NAME);
		
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_1);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_2);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_3);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_4);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_5);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_6);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_7);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_8);
		this.group.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_9);
	}
}
