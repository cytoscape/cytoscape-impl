package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToEdgeHandler extends AbstractApplyHandler<CyEdge> {


	ApplyToEdgeHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(final CyRow row, final View<CyEdge> edgeView) {
		final Collection<VisualProperty<?>> edgeVP = lexManager.getEdgeVisualProperties();
		applyValues(row, edgeView, edgeVP);
	}
}
