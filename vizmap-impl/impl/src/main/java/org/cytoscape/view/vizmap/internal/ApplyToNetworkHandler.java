package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApplyToNetworkHandler extends AbstractApplyHandler<CyNetwork> {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyToNetworkHandler.class);

	ApplyToNetworkHandler(VisualStyle style, VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(final View<CyNetwork> view) {
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
		if (mapping == null)
			return;

		// Default of this style
		final Object styleDefaultValue = style.getDefaultValue(vp);
		// Default of this Visual Property
		final Object vpDefault = vp.getDefault();
		final CyNetwork net = networkView.getModel();

		for (View<?> v : views) {
			View<? extends CyIdentifiable> view = (View<? extends CyIdentifiable>) v;
			mapping.apply(net.getRow(view.getModel()), view);

			if (view.getVisualProperty(vp) == vpDefault)
				view.setVisualProperty(vp, styleDefaultValue);
		}
	}

	private void applyViewDefaults(final CyNetworkView view, final Collection<VisualProperty<?>> vps) {
		final VisualLexicon lex = lexManager.getAllVisualLexicon().iterator().next();
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (VisualProperty<?> vp : vps) {
			final VisualLexiconNode node = lex.getVisualLexiconNode(vp);
			final Collection<VisualLexiconNode> children = node.getChildren();
			
			if(children.size() != 0)
				continue;

			Object defaultValue = style.getDefaultValue(vp);

			if (defaultValue == null) {
				((VisualStyleImpl)style).getStyleDefaults().put(vp, vp.getDefault());
				defaultValue = style.getDefaultValue(vp);
			}

			view.setViewDefault(vp, defaultValue);
		}
		
		// Override dependency
		for(final VisualPropertyDependency<?> dep: dependencies) {
			if(dep.isDependencyEnabled()) {
				final Set<?> vpSet = dep.getVisualProperties();
				// Pick parent
				VisualProperty<?> visualProperty = (VisualProperty<?>) vpSet.iterator().next();
				final VisualLexiconNode node = lex.getVisualLexiconNode(visualProperty);
				final VisualProperty<?> parentVP = node.getParent().getVisualProperty();
				Object defaultValue = style.getDefaultValue(parentVP);
				
				if (defaultValue == null) {
					((VisualStyleImpl)style).getStyleDefaults().put(visualProperty, visualProperty.getDefault());
					defaultValue = style.getDefaultValue(visualProperty);
				}
				for(Object vp: vpSet)
					view.setViewDefault((VisualProperty<?>)vp, defaultValue);
			}
		}
	}
}
