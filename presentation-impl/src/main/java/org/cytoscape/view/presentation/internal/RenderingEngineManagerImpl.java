package org.cytoscape.view.presentation.internal;

/*
 * #%L
 * Cytoscape Presentation Impl (presentation-impl)
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderingEngineManagerImpl implements RenderingEngineManager, NetworkViewAboutToBeDestroyedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RenderingEngineManagerImpl.class);

	private final Map<View<?>, Collection<RenderingEngine<?>>> renderingEngineMap;
	
	private static final String FACTORY_ID_TAG = "id";
	private static final String DEFAULT_FACTORY_ID = "ding";
	
	private VisualLexicon defaultLexicon;

	private final Map<String, RenderingEngineFactory<?>> factoryMap;
	
	private final CyEventHelper eventHelper;
	
	private final Object lock = new Object();
	
	/**
	 * Create an instance of rendering engine manager. This implementation
	 * listens to Presentation events and update its map based on them.
	 */
	public RenderingEngineManagerImpl(final CyEventHelper eventHelper) {
		if(eventHelper == null)
			throw new IllegalArgumentException("CyEventHelper cannot be null.");
		
		this.eventHelper = eventHelper;
		this.renderingEngineMap = new WeakHashMap<View<?>, Collection<RenderingEngine<?>>>();
		this.factoryMap = new HashMap<String, RenderingEngineFactory<?>>();
	}

	/**
	 * This method never returns null.
	 */
	@Override
	public Collection<RenderingEngine<?>> getRenderingEngines(final View<?> viewModel) {
		synchronized (lock) {
			if(renderingEngineMap.containsKey(viewModel) == false)
				return Collections.emptySet();
			else
				return renderingEngineMap.get(viewModel);
		}
	}

	@Override
	public Collection<RenderingEngine<?>> getAllRenderingEngines() {
		final Set<RenderingEngine<?>> allEngines = new HashSet<RenderingEngine<?>>();
		
		synchronized (lock) {
			for(Collection<RenderingEngine<?>> engines: renderingEngineMap.values())
				allEngines.addAll(engines);
		}
		
		return allEngines;
	}
	

	@Override
	public void addRenderingEngine(final RenderingEngine<?> renderingEngine) {
		final View<?> viewModel = renderingEngine.getViewModel();
		synchronized (lock) {
			Collection<RenderingEngine<?>> currentVals = renderingEngineMap.get(viewModel);
			
			if(currentVals == null)
				currentVals = new HashSet<RenderingEngine<?>>();
			
			currentVals.add(renderingEngine);
			
			this.renderingEngineMap.put(viewModel, currentVals);
		}
	}
	

	@Override
	public void removeRenderingEngine(final RenderingEngine<?> renderingEngine) {
		eventHelper.fireEvent(new RenderingEngineAboutToBeRemovedEvent(this, renderingEngine));
		
		synchronized (lock) {
			final View<?> viewModel = renderingEngine.getViewModel();
			final Collection<RenderingEngine<?>> currentEngines = renderingEngineMap.get(viewModel);
			if (currentEngines != null) {
				currentEngines.remove(renderingEngine);
			}
		}
		
		renderingEngine.dispose();
	}
	

	@Override
	public VisualLexicon getDefaultVisualLexicon() {
		synchronized (lock) {
			if(defaultLexicon == null)
				throw new IllegalStateException("Lexicon is not ready yet.");
			
			return this.defaultLexicon;
		}
	}
	
	
	public void addRenderingEngineFactory(
			final RenderingEngineFactory<?> factory, Map metadata) {
		final Object idObject = metadata.get(FACTORY_ID_TAG);

		if (idObject == null)
			throw new IllegalArgumentException(
					"Could not add factory: ID metadata is missing for RenderingEngineFactory.");

		final String id = idObject.toString();

		synchronized (lock) {
			this.factoryMap.put(id, factory);
			
			// Register default lexicon
			if(id.equals(DEFAULT_FACTORY_ID))
				defaultLexicon = factory.getVisualLexicon();
		}
		
		logger.debug("New engine registered: " + factory.getClass());
	}

	public void removeRenderingEngineFactory(
			final RenderingEngineFactory<?> factory, Map metadata) {
		final Object idObject = metadata.get(FACTORY_ID_TAG);

		if (idObject == null)
			throw new IllegalArgumentException(
					"Could not remove factory: ID metadata is missing for RenderingEngineFactory.");

		final String id = idObject.toString();

		synchronized (lock) {
			this.factoryMap.remove(id);
		}
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		Collection<RenderingEngine<?>> engines;
		synchronized (lock) {
			engines = renderingEngineMap.remove(e.getNetworkView());
		}
		if (engines == null)
			return;
		for (RenderingEngine<?> engine : engines)
			engine.dispose();
	}
}
