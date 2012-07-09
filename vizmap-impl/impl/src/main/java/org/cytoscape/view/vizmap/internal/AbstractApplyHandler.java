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
			if (!view.isValueLocked(vp)) {
				// check mapping exists or not
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
	
				if (mapping != null) {
					Object value = mapping.getMappedValue(row);
					
					if (value != null)
						view.setVisualProperty(vp, value);
				} else {
					applyDefaultToView(view, vp);
				}
			}
		}

		override(row, view);
	}

	protected void applyDefaultToView(final View<T> view, final VisualProperty<?> vp) {
		final Set<VisualLexicon> lexSet = lexManager.getAllVisualLexicon();
		
		if (lexSet.size() != 0)
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

	private void override(final CyRow row, final View<T> view) {
		this.dependencies = style.getAllVisualPropertyDependencies();
		
		// Override dependency
		for (final VisualPropertyDependency<?> dep : dependencies) {
			if (dep.isDependencyEnabled()) {
				final Set<VisualProperty<?>> vpSet = dep.getVisualProperties();
				
				// Pick parent
				VisualProperty<?> visualProperty = vpSet.iterator().next();
				final VisualProperty<?> parentVP = dep.getParentVisualProperty();
				
				Object defaultValue = style.getDefaultValue(parentVP);

				if (defaultValue == null) {
					((VisualStyleImpl) style).getStyleDefaults().put(visualProperty, visualProperty.getDefault());
					defaultValue = style.getDefaultValue(visualProperty);
				}
				
				// check mapping exists or not
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(parentVP);
				
				for (VisualProperty<?> vp : vpSet) {
					if (mapping != null) {
						Object value = mapping.getMappedValue(row);
						
						if (value != null)
							view.setVisualProperty(vp, value);
					} else {
						view.setVisualProperty((VisualProperty<?>) vp, defaultValue);
					}
				}
			}
		}
	}
}
