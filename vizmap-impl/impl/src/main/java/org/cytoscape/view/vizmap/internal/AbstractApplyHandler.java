package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;

public abstract class AbstractApplyHandler<T extends CyIdentifiable> implements ApplyHandler<T> {

	protected final VisualStyle style;
	protected final VisualLexiconManager lexManager;

	private VisualLexicon lex;
	private Set<VisualPropertyDependency<?>> dependencies;

	AbstractApplyHandler(final VisualStyle style, final VisualLexiconManager lexManager) {
		this.lexManager = lexManager;
		this.style = style;
	}

	protected void applyValues(final CyRow row, final View<T> view, final Collection<VisualProperty<?>> vps) {
		for (final VisualProperty<?> vp : vps) {
			// check mapping exists or not
			final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

			if (mapping != null)
				applyMappedValue(row, view, vp, mapping);
			else
				applyDefaultToView(view, vp);
		}

		override(view);
	}

	// private void applyDefaultValue(final View<T> view, final
	// VisualProperty<?> vp) {
	// Object defaultValue = style.getDefaultValue(vp);
	//
	// if (defaultValue == null) {
	// ((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
	// defaultValue = style.getDefaultValue(vp);
	// }
	//
	// if (!vp.shouldIgnoreDefault())
	// view.setVisualProperty(vp, defaultValue);
	// }

	private void applyDefaultToView(final View<T> view, final VisualProperty<?> vp) {
		final Set<VisualLexicon> lexSet = lexManager.getAllVisualLexicon();
		if(lexSet.size() != 0)
			this.lex = lexSet.iterator().next();
		final VisualLexiconNode vlNode = lex.getVisualLexiconNode(vp);
		final Collection<VisualLexiconNode> children = vlNode.getChildren();

		if (children.size() != 0)
			return;

		Object defaultValue = style.getDefaultValue(vp);

		if (defaultValue == null) {
			((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
			defaultValue = style.getDefaultValue(vp);
		}

		if (!vp.shouldIgnoreDefault())
			view.setVisualProperty(vp, defaultValue);

	}

	private void override(final View<T> view) {
		this.dependencies = style.getAllVisualPropertyDependencies();
		// Override dependency
		for (final VisualPropertyDependency<?> dep : dependencies) {
			if (dep.isDependencyEnabled()) {
				final Set<?> vpSet = dep.getVisualProperties();
				// Pick parent
				VisualProperty<?> visualProperty = (VisualProperty<?>) vpSet.iterator().next();
				final VisualLexiconNode node = lex.getVisualLexiconNode(visualProperty);
				final VisualProperty<?> parentVP = node.getParent().getVisualProperty();
				Object defaultValue = style.getDefaultValue(parentVP);

				if (defaultValue == null) {
					((VisualStyleImpl) style).getStyleDefaults().put(visualProperty, visualProperty.getDefault());
					defaultValue = style.getDefaultValue(visualProperty);
				}
				for (Object vp : vpSet)
					view.setVisualProperty((VisualProperty<?>) vp, defaultValue);
			}
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
