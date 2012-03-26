package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public abstract class AbstractApplyHandler implements ApplyHandler {

	protected final VisualStyle style;
	protected final VisualLexiconManager lexManager;

	AbstractApplyHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		this.lexManager = lexManager;
		this.style = style;
	}

	
	protected void applyValues(final View<? extends CyIdentifiable> view, final Collection<VisualProperty<?>> vps) {

		for (VisualProperty<?> vp : vps) {
			// check mapping exists or not
			final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
			if (mapping != null) {
				applyMappedValue(view, vp, mapping);
				continue;
			}
			Object defaultValue = style.getDefaultValue(vp);

			if (defaultValue == null) {
				((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
				defaultValue = style.getDefaultValue(vp);
			}

			view.setVisualProperty(vp, defaultValue);
		}
	}

	protected void applyMappedValue(final View<? extends CyIdentifiable> nodeView, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {}

}
