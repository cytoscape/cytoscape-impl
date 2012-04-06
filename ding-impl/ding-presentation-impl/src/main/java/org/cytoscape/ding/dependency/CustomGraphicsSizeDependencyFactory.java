package org.cytoscape.ding.dependency;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class CustomGraphicsSizeDependencyFactory implements VisualPropertyDependencyFactory<Double> {

	private final VisualLexicon lexicon;
	
	public CustomGraphicsSizeDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Double> createVisualPropertyDependency() {
		
		final Set<VisualProperty<Double>> customGraphicsSizeDependency = new HashSet<VisualProperty<Double>>();
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_1);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_2);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_3);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_4);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_5);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_6);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_7);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_8);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_9);
		
		return new VisualPropertyDependency<Double>("Fit Custom Graphics to node", customGraphicsSizeDependency, lexicon);
	}

}
