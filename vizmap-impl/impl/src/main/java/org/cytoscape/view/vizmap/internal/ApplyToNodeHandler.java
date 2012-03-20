package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToNodeHandler extends AbstractApplyHandler {

	ApplyToNodeHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(View<?> view) {
		final View<CyNode> nodeView = (View<CyNode>) view;
		final Collection<VisualProperty<?>> nodeVP = lexManager.getNodeVisualProperties();
		applyValues(nodeView, nodeVP);
	}

	/**
	 * Apply mapped values if mapping exists for the given VP.
	 * 
	 * @param view
	 * @param vp
	 */
	@Override
	protected void applyMappedValue(final View<? extends CyTableEntry> nodeView, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {
		
		final View<CyNode> view = (View<CyNode>) nodeView;
		final CyNode model = view.getModel();
		final CyNetwork net = model.getNetworkPointer();
		// Default of this style
		final Object styleDefaultValue = style.getDefaultValue(vp);
		// Default of this Visual Property
		final Object vpDefault = vp.getDefault();

		mapping.apply(net.getRow(model), view);

		if (view.getVisualProperty(vp) == vpDefault)
			view.setVisualProperty(vp, styleDefaultValue);
	}
}
