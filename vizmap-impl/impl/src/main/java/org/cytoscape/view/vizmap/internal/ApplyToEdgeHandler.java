package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToEdgeHandler extends AbstractApplyHandler {

	ApplyToEdgeHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(View<?> view) {
		final View<CyEdge> edgeView = (View<CyEdge>) view;
		final Collection<VisualProperty<?>> edgeVP = lexManager.getEdgeVisualProperties();
		applyValues(edgeView, edgeVP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyMappedValue(final View<? extends CyTableEntry> edgeView, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {
		
		final View<CyEdge> view = (View<CyEdge>) edgeView;
		final CyEdge model = view.getModel();
		final CyNode sourceNode = model.getSource();
		final CyNetwork net = sourceNode.getNetworkPointer();
		// Default of this style
		final Object styleDefaultValue = style.getDefaultValue(vp);
		// Default of this Visual Property
		final Object vpDefault = vp.getDefault();

		mapping.apply(net.getRow(model), view);

		if (view.getVisualProperty(vp) == vpDefault)
			view.setVisualProperty(vp, styleDefaultValue);
	}

}
