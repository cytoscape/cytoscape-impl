package org.cytoscape.view.vizmap.gui.dependency;

import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.gui.AbstractVisualPropertyDependency;

public class NodeSizeDependency extends AbstractVisualPropertyDependency {
	
	private static final String NAME = "Lock node width and height";
		
	public NodeSizeDependency() {
		super(NAME);
		
		group.add(MinimalVisualLexicon.NODE_WIDTH);
		group.add(MinimalVisualLexicon.NODE_HEIGHT);
	}
}
