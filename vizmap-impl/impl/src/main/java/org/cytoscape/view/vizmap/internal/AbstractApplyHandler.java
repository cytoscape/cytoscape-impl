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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;

public abstract class AbstractApplyHandler<T extends CyIdentifiable> implements ApplyHandler<T>,
																				VisualStyleChangedListener {

	protected final VisualStyle style;
	protected final CyServiceRegistrar serviceRegistrar;
	protected final Class<T> targetDataType;
	protected final VisualProperty<?> rootVisualProperty;
	
	/** Index the Dependencies by the parent Visual Property. */
	protected final Map<VisualProperty<?>, Set<VisualPropertyDependency<?>>> dependencyParents;
	/** Index the Dependencies by child Visual Properties. */
	protected final Map<VisualProperty<?>, Set<VisualPropertyDependency<?>>> dependencyChildren;
	
	protected volatile boolean updateDependencyMaps = true;

	AbstractApplyHandler(
			final VisualStyle style,
			final CyServiceRegistrar serviceRegistrar,
			final Class<T> targetDataType
	) {
		this.style = style;
		this.serviceRegistrar = serviceRegistrar;
		this.targetDataType = targetDataType;
		
		if (targetDataType == CyNode.class)
			rootVisualProperty = BasicVisualLexicon.NODE;
		else if (targetDataType == CyEdge.class)
			rootVisualProperty = BasicVisualLexicon.EDGE;
		else
			rootVisualProperty = BasicVisualLexicon.NETWORK;
		
		dependencyParents = new ConcurrentHashMap<>(16, 0.75f, 2);
		dependencyChildren = new ConcurrentHashMap<>(16, 0.75f, 2);
	}

	@Override
	public void handleEvent(final VisualStyleChangedEvent e) {
		updateDependencyMaps = true;
	}

	@Override
	public void apply(final CyRow row, final View<T> view) {
		if (updateDependencyMaps)
			updateDependencyMaps();
		
		// Clear visual properties first
		view.clearVisualProperties();
		
		// Get current Visual Lexicon
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final VisualLexicon lexicon = appMgr.getCurrentNetworkViewRenderer()
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		
		final LinkedList<VisualLexiconNode> descendants = new LinkedList<>();
		descendants.addAll(lexicon.getVisualLexiconNode(rootVisualProperty).getChildren());
		
		while (!descendants.isEmpty()) {
			final VisualLexiconNode node = descendants.pop();
			final VisualProperty<?> vp = node.getVisualProperty();
			
			if (vp.getTargetDataType() != targetDataType)
				continue; // Because NETWORK has node/edge properties as descendants as well
			
			final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
			final Object value = mapping != null ? mapping.getMappedValue(row) : null;
			
			if (value == null) {
				// Apply the default value...
				applyDefaultValue(view, vp, lexicon);
			} else {
				// Apply the mapped value...
				applyMappedValue(view, vp, value);
			}
			
			descendants.addAll(node.getChildren());
		}
	}

	private void applyDefaultValue(final View<T> view, final VisualProperty<?> vp, final VisualLexicon lexicon) {
		// This is the view default value.
		Object value = style.getDefaultValue(vp);

		if (value == null) {
			((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
			value = style.getDefaultValue(vp);
		}
		
		final Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(vp);
		
		// If this property has already received a propagated value from a previous
		// enabled dependency, do not apply this mapping's value over it.
		if (!isParentOfDependency(vp) && !isChildOfEnabledDependency(vp)) {
			// TODO: Is this correct? Shouldn't default values be applied through CyNetworkView.setViewDefault instead?
			if (!vp.shouldIgnoreDefault())
				view.setVisualProperty(vp, value);
		} else if (depSet != null) {
			for (final VisualPropertyDependency<?> dep : depSet) {
				// The dependency has a higher priority over children's mappings when enabled.
				if (dep.isDependencyEnabled())
					propagateValue(view, vp, value, dep.getVisualProperties(), true);
			}
		}
	}

	private void applyMappedValue(final View<T> view, final VisualProperty<?> vp, final Object value) {
		final Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(vp);
		
		// If this property has already received a propagated value from a previous
		// enabled dependency, do not apply this mapping's value over it.
		if (!isParentOfDependency(vp) && !isChildOfEnabledDependency(vp)) {
			view.setVisualProperty(vp, value);
		} else if (depSet != null) {
			for (final VisualPropertyDependency<?> dep : depSet) {
				// The dependency has a higher priority over children's mappings when enabled.
				if (dep.isDependencyEnabled())
					propagateValue(view, vp, value, dep.getVisualProperties(), false);
			}
		}
	}
	
	protected void propagateValue(final View<? extends CyIdentifiable> view,
								  final VisualProperty<?> parent,
								  final Object value,
								  final Set<VisualProperty<?>> children,
								  final boolean isDefaultValue) {
		for (final VisualProperty<?> vp : children) {
			// Prevent ClassCastExceptions (the child property can have a different value type)
			if (parent.getClass() == vp.getClass() && !(isDefaultValue && vp.shouldIgnoreDefault()))
				view.setVisualProperty(vp, value);
		}
	}
	
	/**
	 * Re-index the dependency maps.
	 */
	protected synchronized void updateDependencyMaps() {
		dependencyParents.clear();
		dependencyChildren.clear();
		
		for (final VisualPropertyDependency<?> dep : style.getAllVisualPropertyDependencies()) {
			final VisualProperty<?> parent = dep.getParentVisualProperty();
			Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(parent);
			
			if (depSet == null)
				dependencyParents.put(parent, 
						depSet = Collections.synchronizedSet(new HashSet<>()));
			
			depSet.add(dep);
			
			for (final VisualProperty<?> child : dep.getVisualProperties()) {
				Set<VisualPropertyDependency<?>> childSet = dependencyChildren.get(child);
				
				if (childSet == null)
					dependencyChildren.put(child,
							childSet = Collections.synchronizedSet(new HashSet<>()));
				
				childSet.add(dep);
			}
		}
		
		updateDependencyMaps = false;
	}
	
	/**
	 * @param vp
	 * @return true if the {@link VisualProperty} has any {@link VisualPropertyDependency}.
	 */
	protected boolean isParentOfDependency(final VisualProperty<?> vp) {
		final Set<VisualPropertyDependency<?>> set = dependencyParents.get(vp);
		
		return set != null && !set.isEmpty();
	}
	
	/**
	 * @param vp
	 * @return true if the {@link VisualProperty} belongs to any enabled {@link VisualPropertyDependency}'s 
	 *              properties set.
	 */
	protected boolean isChildOfEnabledDependency(final VisualProperty<?> vp) {
		final Set<VisualPropertyDependency<?>> set = dependencyChildren.get(vp);
		
		if (set != null) {
			for (final VisualPropertyDependency<?> dep : set) {
				if (dep.isDependencyEnabled())
					return true;
			}
		}
		
		return false;
	}
}
