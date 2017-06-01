package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
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
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class VisualStyleImpl implements VisualStyle, VisualMappingFunctionChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(VisualStyleImpl.class);

	private static final String DEFAULT_TITLE = "?";

	private final Map<VisualProperty<?>, VisualMappingFunction<?, ?>> mappings;
	private final Map<VisualProperty<?>, Object> styleDefaults;

	private final Map<Class<? extends CyIdentifiable>, ApplyHandler> applyHandlersMap;

	private String title;
	private final CyEventHelper eventHelper;

	private final Set<VisualPropertyDependency<?>> dependencies;

	private final Object lock = new Object();
	
	/**
	 * @param title Title of the new Visual Style
	 * @param lexManager
	 */
	public VisualStyleImpl(final String title, final CyServiceRegistrar serviceRegistrar) {
 		if (title == null)
			this.title = DEFAULT_TITLE;
		else
			this.title = title;

		this.eventHelper = serviceRegistrar.getService(CyEventHelper.class);;

		mappings = new HashMap<>();
		styleDefaults = new HashMap<>();

		// Init Apply handlers for node, egde and network.
		final ApplyToNetworkHandler applyToNetworkHandler = new ApplyToNetworkHandler(this, serviceRegistrar);
		final ApplyToNodeHandler applyToNodeHandler = new ApplyToNodeHandler(this, serviceRegistrar);
		final ApplyToEdgeHandler applyToEdgeHandler = new ApplyToEdgeHandler(this, serviceRegistrar);
		
		serviceRegistrar.registerAllServices(applyToNetworkHandler, new Properties());
		serviceRegistrar.registerAllServices(applyToNodeHandler, new Properties());
		serviceRegistrar.registerAllServices(applyToEdgeHandler, new Properties());
		
		applyHandlersMap = new HashMap<>();
		applyHandlersMap.put(CyNetwork.class, applyToNetworkHandler);
		applyHandlersMap.put(CyNode.class, applyToNodeHandler);
		applyHandlersMap.put(CyEdge.class, applyToEdgeHandler);

		dependencies = new HashSet<>();

		// Listening to dependencies
		serviceRegistrar.registerServiceListener(this, "registerDependencyFactory", "unregisterDependencyFactory",
				VisualPropertyDependencyFactory.class);
		serviceRegistrar.registerService(this, VisualMappingFunctionChangedListener.class, new Properties());
		logger.info("New Visual Style Created: Style Name = " + this.title);
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
			
			eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
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
			eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
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
			eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(final CyNetworkView networkView) {
		// This is always safe.
		final ApplyHandler<CyNetwork> networkViewHandler;
		synchronized (lock) {
			networkViewHandler = applyHandlersMap.get(CyNetwork.class);
		}
		eventHelper.flushPayloadEvents();
		networkViewHandler.apply(null, networkView);
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
			for (final Class<?> viewType : applyHandlersMap.keySet()) {
				if (viewType.isAssignableFrom(view.getModel().getClass())) {
					handler = applyHandlersMap.get(viewType);
					break;
				}
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
		
		if (changed)
			eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
	}

	@Override
	public void removeVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
		boolean changed = false;
		
		synchronized (lock) {
			changed = dependencies.remove(dependency);
		}
		
		if (changed)
			eventHelper.addEventPayload(this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
	}

	/**
	 * Register dependency service.
	 * 
	 * @param dependency
	 * @param props
	 */
	public void registerDependencyFactory(VisualPropertyDependencyFactory<?> dependencyFactory, Map props) {
		if (dependencyFactory != null)
			addVisualPropertyDependency(dependencyFactory.createVisualPropertyDependency());
	}

	public void unregisterDependencyFactory(VisualPropertyDependencyFactory<?> dependencyFactory, Map props) {
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
			eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
	}
}
