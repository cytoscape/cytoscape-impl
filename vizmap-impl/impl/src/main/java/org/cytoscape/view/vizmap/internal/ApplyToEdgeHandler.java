package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToEdgeHandler extends AbstractApplyHandler<CyEdge> {

	private final CyNetworkManager networkManager;
	
	ApplyToEdgeHandler(final VisualStyle style, final VisualLexiconManager lexManager, final CyNetworkManager networkManager) {
		super(style, lexManager);
		this.networkManager = networkManager;
	}

	@Override
	public void apply(final View<CyEdge> edgeView) {
		final Collection<VisualProperty<?>> edgeVP = lexManager.getEdgeVisualProperties();
		applyValues(edgeView, edgeVP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyMappedValue(final View<CyEdge> view, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {
		final CyEdge model = view.getModel();
		CyNetwork targetNetwork = null;
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		for (CyNetwork net : networks) {
			if (net.containsEdge(model)) {
				targetNetwork = net;
				break;
			}
		}
		
		if (targetNetwork==null) {
			throw new NullPointerException("Could'nt find network");
		}
		// Default of this style
		final Object styleDefaultValue = style.getDefaultValue(vp);
		// Default of this Visual Property
		final Object vpDefault = vp.getDefault();

		mapping.apply(targetNetwork.getRow(model), view);

		if (view.getVisualProperty(vp) == vpDefault)
			view.setVisualProperty(vp, styleDefaultValue);
	}

}
