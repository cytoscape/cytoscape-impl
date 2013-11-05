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

	/**
	 * 
	 * @param title
	 *            Title of the new Visual Style
	 * @param lexManager
	 */
	public VisualStyleImpl(final String title, final VisualLexiconManager lexManager,
			final CyServiceRegistrar serviceRegistrar, final CyEventHelper eventHelper) {

		if (lexManager == null)
			throw new NullPointerException("Lexicon Manager is missing.");

		if (title == null)
			this.title = DEFAULT_TITLE;
		else
			this.title = title;

		this.eventHelper = eventHelper;

		mappings = new HashMap<VisualProperty<?>, VisualMappingFunction<?, ?>>();
		styleDefaults = new HashMap<VisualProperty<?>, Object>();

		// Init Apply handlers for node, egde and network.
		applyHandlersMap = new HashMap<Class<? extends CyIdentifiable>, ApplyHandler>();
		applyHandlersMap.put(CyNetwork.class, new ApplyToNetworkHandler(this, lexManager));
		applyHandlersMap.put(CyNode.class, new ApplyToNodeHandler(this, lexManager));
		applyHandlersMap.put(CyEdge.class, new ApplyToEdgeHandler(this, lexManager));

		dependencies = new HashSet<VisualPropertyDependency<?>>();

		// Listening to dependencies
		serviceRegistrar.registerServiceListener(this, "registerDependencyFactory", "unregisterDependencyFactory",
				VisualPropertyDependencyFactory.class);
		serviceRegistrar.registerService(this, VisualMappingFunctionChangedListener.class, new Properties());
		logger.info("New Visual Style Created: Style Name = " + this.title);
	}

	@Override
	public void addVisualMappingFunction(final VisualMappingFunction<?, ?> mapping) {
		mappings.put(mapping.getVisualProperty(), mapping);
		eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(),
				VisualStyleChangedEvent.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(VisualProperty<V> t) {
		return (VisualMappingFunction<?, V>) mappings.get(t);
	}

	@Override
	public void removeVisualMappingFunction(VisualProperty<?> t) {
		mappings.remove(t);
		eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(),
				VisualStyleChangedEvent.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDefaultValue(final VisualProperty<V> vp) {
		return (V) styleDefaults.get(vp);
	}

	@Override
	public <V, S extends V> void setDefaultValue(final VisualProperty<V> vp, final S value) {
		styleDefaults.put(vp, value);
		eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(),
				VisualStyleChangedEvent.class);
	}

	@Override
	public void apply(final CyNetworkView networkView) {
		@SuppressWarnings("unchecked")
		// This is always safe.
		final ApplyHandler<CyNetwork> networkViewHandler = applyHandlersMap.get(CyNetwork.class);
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

		for (final Class<?> viewType : applyHandlersMap.keySet()) {
			if (viewType.isAssignableFrom(view.getModel().getClass())) {
				handler = applyHandlersMap.get(viewType);
				break;
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
		return mappings.values();
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
		dependencies.add(dependency);
		eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(),
				VisualStyleChangedEvent.class);
	}

	@Override
	public void removeVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
		dependencies.remove(dependency);
		eventHelper.addEventPayload((VisualStyle) this, new VisualStyleChangeRecord(),
				VisualStyleChangedEvent.class);
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
		if (this.mappings.containsValue(mapping)) {
			eventHelper.addEventPayload((VisualStyle)this, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
		}
	}
}
