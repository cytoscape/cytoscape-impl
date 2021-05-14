package org.cytoscape.view.vizmap.internal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ApplyToNetworkHandler extends AbstractApplyHandler<CyNetwork> {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	ApplyToNetworkHandler(final VisualStyle style, final CyServiceRegistrar serviceRegistrar) {
		super(style, serviceRegistrar, CyNetwork.class, BasicVisualLexicon.NETWORK);
	}

	@Override
	public void apply(final CyRow row, final View<CyNetwork> view) {
		final CyNetworkView netView = (CyNetworkView) view;
		final Collection<View<CyNode>> nodeViews = netView.getNodeViews();
		final Collection<View<CyEdge>> edgeViews = netView.getEdgeViews();
		final Collection<View<CyNetwork>> networkViewSet = new HashSet<>();
		networkViewSet.add(netView);
		
		// Make sure the dependency maps are up to date
		updateDependencyMaps();

		// Clear visual properties from all views first
		view.clearVisualProperties();
		
		for (final View<?> v : nodeViews)
			v.clearVisualProperties();
		
		for (final View<?> v : edgeViews)
			v.clearVisualProperties();
		
		// Get current Visual Lexicon
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final VisualLexicon lexicon = appMgr.getCurrentNetworkViewRenderer()
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		
		applyDefaultsInParallel(netView, lexicon.getVisualLexiconNode(BasicVisualLexicon.NODE));
		applyDefaultsInParallel(netView, lexicon.getVisualLexiconNode(BasicVisualLexicon.EDGE));
		applyDefaultsInParallel(netView, lexicon.getVisualLexiconNode(BasicVisualLexicon.NETWORK));

		applyDependencies(netView);
		
		ExecutorService exe = Executors.newCachedThreadPool();
		Future<?> nodeFuture = exe.submit(new ApplyMappingsTask(netView, nodeViews, BasicVisualLexicon.NODE, lexicon));
		Future<?> edgeFuture = exe.submit(new ApplyMappingsTask(netView, edgeViews, BasicVisualLexicon.EDGE, lexicon));
		Future<?> netwFuture = exe.submit(new ApplyMappingsTask(netView, networkViewSet, BasicVisualLexicon.NETWORK, lexicon));
		
		try {
			exe.shutdown();
			try {
				nodeFuture.get();
			} catch(ExecutionException e) {
				logger.error("Error applying node visual properties", e);
			}
			try {
				edgeFuture.get();
			} catch(ExecutionException e) {
				logger.error("Error applying edge visual properties", e);
			}
			try {
				netwFuture.get();
			} catch(ExecutionException e) {
				logger.error("Error applying network visual properties", e);
			}
			exe.awaitTermination(15, TimeUnit.MINUTES);
		} catch (Exception ex) {
			logger.warn("Create apply operation failed.", ex);
		}
	}
	
	private void applyDefaultsInParallel(final CyNetworkView netView, final VisualLexiconNode rootNode) {
		final ExecutorService exe = Executors.newCachedThreadPool();
		final Deque<VisualLexiconNode> deque = new ArrayDeque<>();
		deque.addAll(rootNode.getChildren());
		
		while (!deque.isEmpty()) {
			final VisualLexiconNode node = deque.pop();
			final VisualProperty<?> vp = node.getVisualProperty();
			
			if (vp.getTargetDataType() != rootNode.getVisualProperty().getTargetDataType())
				continue;
			
			final Collection<VisualLexiconNode> children = node.getChildren();

			if (children.isEmpty()) {
				Object defaultValue = style.getDefaultValue(vp);
	
				if (defaultValue == null) {
					((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
					defaultValue = style.getDefaultValue(vp);
				}
				
				exe.submit(new ApplyDefaultTask(netView, vp, defaultValue));
			}
			
			deque.addAll(children);
		}
		
		try {
			exe.shutdown();
			exe.awaitTermination(10, TimeUnit.MINUTES);
		} catch (Exception ex) {
			logger.warn("Create apply default failed", ex);
		} finally {

		}
	}
	
	private final class ApplyDefaultTask implements Runnable {
		
		private final CyNetworkView netView;
		private final VisualProperty<?> vp;
		private final Object defaultValue;
		
		ApplyDefaultTask(final CyNetworkView netView, final VisualProperty<?> vp, final Object defaultValue) {
			this.vp = vp;
			this.netView = netView;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public void run() {
			netView.setViewDefault(vp, defaultValue);
		}
	}

	private final class ApplyMappingsTask implements Runnable {

		private final CyNetworkView netView;
		private final Collection<? extends View<? extends CyIdentifiable>> views;
		private final VisualProperty<?> rootVisualProperty;
		private final VisualLexicon lexicon;
		
		ApplyMappingsTask(final CyNetworkView netView,
						  final Collection<? extends View<? extends CyIdentifiable>> views,
						  final VisualProperty<?> rootVisualProperty,
						  final VisualLexicon lexicon) {
			this.netView = netView;
			this.views = views;
			this.rootVisualProperty = rootVisualProperty;
			this.lexicon = lexicon;
		}
		
		@Override
		public void run() {
			final Class<? extends CyIdentifiable> targetDataType = rootVisualProperty.getTargetDataType();
			final LinkedList<VisualLexiconNode> descendants = new LinkedList<>();
			descendants.addAll(lexicon.getVisualLexiconNode(rootVisualProperty).getChildren());
			
			while (!descendants.isEmpty()) {
				final VisualLexiconNode node = descendants.pop();
				final VisualProperty<?> vp = node.getVisualProperty();
				
				if (vp.getTargetDataType() != targetDataType)
					continue; // Because NETWORK has node/edge properties as descendants as well
				
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

				if (mapping != null) {
					final CyNetwork net = netView.getModel();
					final Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(vp);

					for (final View<? extends CyIdentifiable> view : views) {
						final Object value = mapping.getMappedValue(net.getRow(view.getModel()));
						
						if (value != null) {
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
					}
				}
				
				descendants.addAll(node.getChildren());
			}
		}
	}
	
	private void applyDependencies(final CyNetworkView netView) {
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			final VisualProperty<?> parentVP = dep.getParentVisualProperty();
			
			if (dep.isDependencyEnabled()) {
				// Dependency is enabled.  Need to use parent value instead.
				final Set<VisualProperty<?>> vpSet = new HashSet<>(dep.getVisualProperties());
				vpSet.add(parentVP);
				
				Object defaultValue = style.getDefaultValue(parentVP);
				
				if (defaultValue == null)
					defaultValue = parentVP.getDefault();
				
				for (VisualProperty<?> vp : vpSet)
					netView.setViewDefault(vp, defaultValue);
			}
		}
	}
}
