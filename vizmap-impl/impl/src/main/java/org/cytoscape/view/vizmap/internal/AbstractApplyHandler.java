package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
					// Mapping exists
					final Object value = mapping.getMappedValue(row);
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

		// This is the view default value.
		Object defaultValue = style.getDefaultValue(vp);

		if (defaultValue == null) {
			((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
			defaultValue = style.getDefaultValue(vp);
		}
		
		// TODO: Is this correct? Shouldn't default values be applied through CyNetworkView.setViewDefault instead?
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
