package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToNodeHandler extends AbstractApplyHandler<CyNode> {

	private final CyNetworkManager networkManager;

	ApplyToNodeHandler(final VisualStyle style, final VisualLexiconManager lexManager,
			final CyNetworkManager networkManager) {
		super(style, lexManager);
		this.networkManager = networkManager;
	}

	@Override
	public void apply(final View<CyNode> nodeView) {
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
	protected void applyMappedValue(final View<CyNode> view, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {
		final CyNode model = view.getModel();
		CyNetwork targetNetwork = null;
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		for (CyNetwork net : networks) {
			if (net.containsNode(model)) {
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
