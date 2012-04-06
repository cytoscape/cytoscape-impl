package org.cytoscape.ding.dependency;

import java.awt.Paint;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class EdgeColorDependencyFactory implements VisualPropertyDependencyFactory<Paint> {

private final VisualLexicon lexicon;
	
	public EdgeColorDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Paint> createVisualPropertyDependency() {
		
		// Create Visual Property Dependencies
		final Set<VisualProperty<Paint>> edgeColorDependency = new HashSet<VisualProperty<Paint>>();
		
		edgeColorDependency.add(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorDependency.add(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		edgeColorDependency.add(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT);
		
		return new VisualPropertyDependency<Paint>("Edge color to arrows", edgeColorDependency, lexicon);
	}

}
