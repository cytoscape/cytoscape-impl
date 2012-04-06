package org.cytoscape.ding.dependency;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class NodeSizeDependencyFactory implements VisualPropertyDependencyFactory<Double> {

private final VisualLexicon lexicon;
	
	public NodeSizeDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Double> createVisualPropertyDependency() {
		// Node Size Dependency
		final Set<VisualProperty<Double>> nodeSizeVisualProperties = new HashSet<VisualProperty<Double>>();
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_WIDTH);
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_HEIGHT);

		return new VisualPropertyDependency<Double>("Lock node width and height", nodeSizeVisualProperties, lexicon);
	}

}
