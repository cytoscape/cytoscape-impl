package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashSet;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApplyToNetworkHandler extends AbstractApplyHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyToNetworkHandler.class);

	ApplyToNetworkHandler(VisualStyle style, VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(final View<?> view) {
		final long start = System.currentTimeMillis();

		CyNetworkView networkView = (CyNetworkView) view;
		final Collection<View<CyNode>> nodeViews = networkView.getNodeViews();
		final Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
		final Collection<View<CyNetwork>> networkViewSet = new HashSet<View<CyNetwork>>();
		networkViewSet.add(networkView);

		applyViewDefaults(networkView, lexManager.getNodeVisualProperties());
		applyViewDefaults(networkView, lexManager.getEdgeVisualProperties());
		applyViewDefaults(networkView, lexManager.getNetworkVisualProperties());

		// Current visual prop tree.
		applyImpl(networkView, nodeViews, lexManager.getNodeVisualProperties());
		applyImpl(networkView, edgeViews, lexManager.getEdgeVisualProperties());
		applyImpl(networkView, networkViewSet, lexManager.getNetworkVisualProperties());

		logger.info("Visual Style applied in " + (System.currentTimeMillis() - start) + " msec.");
	}

	private void applyImpl(final CyNetworkView networkView, final Collection<? extends View<?>> views,
			final Collection<VisualProperty<?>> visualProperties) {

		for (VisualProperty<?> vp : visualProperties)
			applyToView(networkView, views, vp);
	}

	private void applyToView(final CyNetworkView networkView, final Collection<? extends View<?>> views,
			final VisualProperty<?> vp) {

		final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

		if (mapping != null) {

			// Default of this style
			final Object styleDefaultValue = style.getDefaultValue(vp);
			// Default of this Visual Property
			final Object vpDefault = vp.getDefault();
			final CyNetwork net = networkView.getModel();

			for (View<?> v : views) {
				View<? extends CyTableEntry> view = (View<? extends CyTableEntry>) v;
				mapping.apply(net.getRow(view.getModel()), view);

				if (view.getVisualProperty(vp) == vpDefault)
					view.setVisualProperty(vp, styleDefaultValue);
			}
		}
	}

	private void applyViewDefaults(final CyNetworkView view, final Collection<VisualProperty<?>> vps) {

		for (VisualProperty<?> vp : vps) {
			Object defaultValue = style.getDefaultValue(vp);

			if (defaultValue == null) {
				((VisualStyleImpl)style).getStyleDefaults().put(vp, vp.getDefault());
				defaultValue = style.getDefaultValue(vp);
			}

			view.setViewDefault(vp, defaultValue);
		}
	}
}
