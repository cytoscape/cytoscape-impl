package org.cytoscape.view.vizmap.internal;

import java.util.Collection;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;

public abstract class AbstractApplyHandler<T extends CyIdentifiable> implements ApplyHandler<T> {

	protected final VisualStyle style;
	protected final VisualLexiconManager lexManager;

	AbstractApplyHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		this.lexManager = lexManager;
		this.style = style;
	}

	
	protected void applyValues(final CyRow row, final View<T> view, final Collection<VisualProperty<?>> vps) {
		for (final VisualProperty<?> vp : vps) {
			// check mapping exists or not
			final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
			if (mapping != null) {
				applyMappedValue(row, view, vp, mapping);
				continue;
			}
			Object defaultValue = style.getDefaultValue(vp);

			if (defaultValue == null) {
				((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
				defaultValue = style.getDefaultValue(vp);
			}

			if(!vp.shouldIgnoreDefault())
				view.setVisualProperty(vp, defaultValue);
		}
	}

	private void applyMappedValue(final CyRow row, final View<T> view, final VisualProperty<?> vp,
			final VisualMappingFunction<?, ?> mapping) {
		// Default of this style
		final Object styleDefaultValue = style.getDefaultValue(vp);
		// Default of this Visual Property
		final Object vpDefault = vp.getDefault();

		mapping.apply(row, view);

		if (view.getVisualProperty(vp) == vpDefault)
			view.setVisualProperty(vp, styleDefaultValue);

	}

}
