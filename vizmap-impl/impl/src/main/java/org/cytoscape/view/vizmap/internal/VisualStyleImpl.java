package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedListener;
import org.cytoscape.view.vizmap.events.VisualPropertyDependencyChangedEvent;
import org.cytoscape.view.vizmap.events.VisualPropertyDependencyChangedListener;
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2019 The Cytoscape Consortium
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

public class VisualStyleImpl
		implements VisualStyle, VisualMappingFunctionChangedListener, VisualPropertyDependencyChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private static final String DEFAULT_TITLE = "?";

	private final Map<VisualProperty<?>, VisualMappingFunction<?, ?>> mappings = new HashMap<>();
	private final Map<VisualProperty<?>, Object> styleDefaults = new HashMap<>();

	private final ApplyToNetworkHandler applyToNetworkHandler;
	private final ApplyToNodeHandler applyToNodeHandler;
	private final ApplyToEdgeHandler applyToEdgeHandler;
	private final ApplyToColumnHandler applyToColumnHandler;

	private String title;
	private final CyEventHelper eventHelper;

	private final Set<VisualPropertyDependency<?>> dependencies = new HashSet<>();

	private final Object lock = new Object();
	

	public VisualStyleImpl(final String title, final CyServiceRegistrar registrar) {
		this.title = title == null ? DEFAULT_TITLE : title;
		this.eventHelper = registrar.getService(CyEventHelper.class);

		applyToNetworkHandler = new ApplyToNetworkHandler(this, registrar);
		applyToNodeHandler = new ApplyToNodeHandler(this, registrar);
		applyToEdgeHandler = new ApplyToEdgeHandler(this, registrar);
		applyToColumnHandler = new ApplyToColumnHandler(this, registrar);
		
		// Listening to dependencies
		registrar.registerServiceListener(this, "registerDependencyFactory", "unregisterDependencyFactory", VisualPropertyDependencyFactory.class);
		registrar.registerService(this, VisualMappingFunctionChangedListener.class, new Properties());
		registrar.registerService(this, VisualPropertyDependencyChangedListener.class, new Properties());
	}
	
	private void setUpdateDependencyMaps() {
		applyToNetworkHandler.setUpdateDependencyMaps();
		applyToNodeHandler.setUpdateDependencyMaps();
		applyToEdgeHandler.setUpdateDependencyMaps();
	}
	
	private void fireVisualStyleChange() {
		setUpdateDependencyMaps();
		eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
	}
	
	@Override
	public void addVisualMappingFunction(final VisualMappingFunction<?, ?> mapping) {
		boolean changed = false;

		synchronized (lock) {
			VisualMappingFunction<?, ?> oldMapping = mappings.get(mapping.getVisualProperty());
			changed = !mapping.equals(oldMapping);
		}

		if (changed) {
			// Flush payload events to make sure any VisualMappingFunctionChangedEvents
			// from this mapping are fired now (before the mapping is added to this style),
			// which will prevent this style from receiving it later
			// and then firing unnecessary VisualStyleChangedEvents, as consequence.
			eventHelper.flushPayloadEvents();
			
			synchronized (lock) {
				mappings.put(mapping.getVisualProperty(), mapping);
			}
			
			fireVisualStyleChange();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(VisualProperty<V> t) {
		synchronized (lock) {
			return (VisualMappingFunction<?, V>) mappings.get(t);
		}
	}

	@Override
	public void removeVisualMappingFunction(VisualProperty<?> t) {
		boolean changed = false;
		
		synchronized (lock) {
			VisualMappingFunction<?, ?> oldMapping = mappings.remove(t);
			changed = oldMapping != null;
		}
		
		if (changed)
			fireVisualStyleChange();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDefaultValue(final VisualProperty<V> vp) {
		synchronized (lock) {
			return (V) styleDefaults.get(vp);
		}
	}

	@Override
	public <V, S extends V> void setDefaultValue(final VisualProperty<V> vp, final S value) {
		boolean changed = false;
		
		synchronized (lock) {
			boolean containsKey = styleDefaults.containsKey(vp);
			Object oldValue = styleDefaults.put(vp, value);
			changed = !containsKey || (value == null && oldValue != null) || (value != null && !value.equals(oldValue));
		}
		
		if (changed)
			fireVisualStyleChange();
	}

	@Override
	public void apply(final CyNetworkView networkView) {
		eventHelper.flushPayloadEvents();
		applyToNetworkHandler.apply(null, networkView);
	}
	
	@Override
	public void apply(final View<CyColumn> columnView) {
		eventHelper.flushPayloadEvents();
		applyToColumnHandler.apply(null, columnView);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void apply(final CyRow row, final View<? extends CyIdentifiable> view) {
		if (view == null) {
			logger.warn("Tried to apply Visual Style to null view");
			return;
		}

		ApplyHandler handler = null;

		synchronized (lock) {
			var viewClass = view.getModel().getClass();
			if(CyNetwork.class.isAssignableFrom(viewClass)) {
				handler = applyToNetworkHandler;
			} else if(CyNode.class.isAssignableFrom(viewClass)) {
				handler = applyToNodeHandler;
			} else if(CyEdge.class.isAssignableFrom(viewClass)) {
				handler = applyToEdgeHandler;
			} else if(CyColumn.class.isAssignableFrom(viewClass)) {
				handler = applyToColumnHandler;
			}
		}

		if (handler == null)
			throw new IllegalArgumentException("This view type is not supported: " + view.getClass());

		handler.apply(row, view);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return this.title;
	}

	@Override
	public Collection<VisualMappingFunction<?, ?>> getAllVisualMappingFunctions() {
		synchronized (lock) {
			return mappings.values();
		}
	}

	Map<VisualProperty<?>, Object> getStyleDefaults() {
		return this.styleDefaults;
	}

	@Override
	public Set<VisualPropertyDependency<?>> getAllVisualPropertyDependencies() {
		return Collections.unmodifiableSet(dependencies);
	}

	/**
	 * Manually add dependency.
	 */
	@Override
	public void addVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
		boolean changed = false;
		
		synchronized (lock) {
			changed = dependencies.add(dependency);
		}
		
		if (changed) {
			dependency.setEventHelper(eventHelper);
			fireVisualStyleChange();
		}
	}

	@Override
	public void removeVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
		boolean changed = false;
		
		synchronized (lock) {
			changed = dependencies.remove(dependency);
		}
		
		if (changed)
			fireVisualStyleChange();
	}

	public void registerDependencyFactory(VisualPropertyDependencyFactory<?> dependencyFactory, Map<?, ?> props) {
		if (dependencyFactory != null)
			addVisualPropertyDependency(dependencyFactory.createVisualPropertyDependency());
	}

	public void unregisterDependencyFactory(VisualPropertyDependencyFactory<?> dependencyFactory, Map<?, ?> props) {
		// FIXME
		// if(dependencyFactory != null)
		// removeVisualPropertyDependency(dependency);
	}

	@Override
	public void handleEvent(VisualMappingFunctionChangedEvent e) {
		final VisualMappingFunction<?, ?> mapping = e.getSource();
		boolean hasMapping = false;
		
		synchronized (lock) {
			hasMapping = mapping == mappings.get(mapping.getVisualProperty());
		}
		
		if (hasMapping)
			fireVisualStyleChange();
	}

	@Override
	public void handleEvent(VisualPropertyDependencyChangedEvent e) {
		final VisualPropertyDependency<?> dep = e.getSource();
		
		if (dependencies.contains(dep))
			fireVisualStyleChange();
	}
}
