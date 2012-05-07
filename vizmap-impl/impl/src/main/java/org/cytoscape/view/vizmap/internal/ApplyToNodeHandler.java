package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToNodeHandler extends AbstractApplyHandler<CyNode> {

	ApplyToNodeHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(final CyRow row, final View<CyNode> nodeView) {
		final Collection<VisualProperty<?>> nodeVP = lexManager.getNodeVisualProperties();
		applyValues(row, nodeView, nodeVP);
	}
}
