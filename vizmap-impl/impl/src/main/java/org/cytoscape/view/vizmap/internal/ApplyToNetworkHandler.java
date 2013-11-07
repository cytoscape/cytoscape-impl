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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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


public class ApplyToNetworkHandler extends AbstractApplyHandler<CyNetwork> {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyToNetworkHandler.class);

	
	ApplyToNetworkHandler(VisualStyle style, VisualLexiconManager lexManager) {
		super(style, lexManager);
	}

	@Override
	public void apply(final CyRow row, final View<CyNetwork> view) {
		final CyNetworkView netView = (CyNetworkView) view;
		final Collection<View<CyNode>> nodeViews = netView.getNodeViews();
		final Collection<View<CyEdge>> edgeViews = netView.getEdgeViews();
		final Collection<View<CyNetwork>> networkViewSet = new HashSet<View<CyNetwork>>();
		networkViewSet.add(netView);

		// TODO: what if there is another Lexicon?
		final VisualLexicon lex = lexManager.getAllVisualLexicon().iterator().next();
		
		applyDefaultsInParallel(netView, lexManager.getNodeVisualProperties(), lex);
		applyDefaultsInParallel(netView, lexManager.getEdgeVisualProperties(), lex);
		applyDefaultsInParallel(netView, lexManager.getNetworkVisualProperties(), lex);

		applyDependencies(netView);
		
		final ExecutorService exe = Executors.newCachedThreadPool();
		exe.submit(new ApplyMappingsTask(netView, nodeViews, BasicVisualLexicon.NODE, lex));
		exe.submit(new ApplyMappingsTask(netView, edgeViews, BasicVisualLexicon.EDGE, lex));
		exe.submit(new ApplyMappingsTask(netView, networkViewSet, BasicVisualLexicon.NETWORK, lex));
		
		try {
			exe.shutdown();
			exe.awaitTermination(15, TimeUnit.MINUTES);
		} catch (Exception ex) {
			logger.warn("Create apply operation failed.", ex);
		}
	}
	
	private void applyDefaultsInParallel(final CyNetworkView netView, final Collection<VisualProperty<?>> vps, final VisualLexicon lex) {
		final ExecutorService exe = Executors.newCachedThreadPool();
		
		for (final VisualProperty<?> vp : vps) {
			final VisualLexiconNode node = lex.getVisualLexiconNode(vp);
			
			if (node == null) {
				continue;
			}
			
			final Collection<VisualLexiconNode> children = node.getChildren();

			if (children.isEmpty()) {
				Object defaultValue = style.getDefaultValue(vp);
	
				if (defaultValue == null) {
					((VisualStyleImpl) style).getStyleDefaults().put(vp, vp.getDefault());
					defaultValue = style.getDefaultValue(vp);
				}
				exe.submit(new ApplyDefaultTask(netView, vp, defaultValue));
			}
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
		private final Map<VisualProperty<?>, VisualPropertyDependency<?>> dependencyMap;
		private final Set<String> propagated;
		private final VisualLexicon lexicon;
		
		ApplyMappingsTask(final CyNetworkView netView,
						  final Collection<? extends View<? extends CyIdentifiable>> views,
						  final VisualProperty<?> rootVisualProperty,
						  final VisualLexicon lexicon) {
			this.netView = netView;
			this.views = views;
			this.rootVisualProperty = rootVisualProperty;
			this.lexicon = lexicon;
			this.dependencyMap = getDependencyMap();
			propagated = new HashSet<String>();
		}
		
		@Override
		public void run() {
			final Class<? extends CyIdentifiable> targetDataType = rootVisualProperty.getTargetDataType();
			final LinkedList<VisualLexiconNode> descendants = new LinkedList<VisualLexiconNode>();
			descendants.addAll(lexicon.getVisualLexiconNode(rootVisualProperty).getChildren());
			
			while (!descendants.isEmpty()) {
				final VisualLexiconNode node = descendants.pop();
				final VisualProperty<?> vp = node.getVisualProperty();
				
				if (vp.getTargetDataType() != targetDataType)
					continue; // Because NETWORK has node/edge properties as descendants as well
				
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

				if (mapping != null) {
					final CyNetwork net = netView.getModel();
					final VisualPropertyDependency<?> dep = dependencyMap.get(vp);

					for (final View<? extends CyIdentifiable> view : views) {
						final Object value = mapping.getMappedValue(net.getRow(view.getModel()));
						
						if (value != null) {
							// If this property has already received a propagated value from a previous
							// enabled dependency, do not apply this mapping's value over it;
							// the dependency has a higher priority over children's mappings when enabled.
							if (!propagated.contains(vp.getIdString())
									&& (dep == null
											|| !vp.equals(dep.getParentVisualProperty())
											|| dep.isDependencyEnabled())) {
								view.setVisualProperty(vp, value);
							}
							
							propagateMappedValues(view, node, value);
						}
					}
				}
				
				descendants.addAll(node.getChildren());
			}
		}
		
		private void propagateMappedValues(final View<? extends CyIdentifiable> view,
										   final VisualLexiconNode rootNode,
										   final Object value) {
			final LinkedList<VisualLexiconNode> parentNodes = new LinkedList<VisualLexiconNode>();
			parentNodes.add(rootNode);
			
			while (!parentNodes.isEmpty()) {
				final VisualLexiconNode pn = parentNodes.pop();
				
				final VisualPropertyDependency<?> dep = dependencyMap.get(pn.getVisualProperty());
				final Collection<VisualLexiconNode> children = pn.getChildren();
				
				for (final VisualLexiconNode cn : children) {
					final VisualProperty<?> vp = cn.getVisualProperty();
					
					// Prevent ClassCastExceptions (the child property can have a different value type)
					if (rootNode.getVisualProperty().getClass() == vp.getClass()) {
						// Do not propagate to this child VisualProperty if it belongs to a disabled Dependency
						if (dep == null || dep.isDependencyEnabled() || !dep.getVisualProperties().contains(vp)) {
							view.setVisualProperty(vp, value);
							propagated.add(vp.getIdString()); // Save the properties that received values from a parent
						}
					}
				}
				
				// The current child nodes now become parents, to keep searching down the tree
				parentNodes.addAll(children);
			}
		}
		
		private Map<VisualProperty<?>, VisualPropertyDependency<?>> getDependencyMap() {
			final Map<VisualProperty<?>, VisualPropertyDependency<?>> map = 
					new HashMap<VisualProperty<?>, VisualPropertyDependency<?>>();
			final Class<? extends CyIdentifiable> targetDataType = rootVisualProperty.getTargetDataType();
			
			for (final VisualPropertyDependency<?> dep : style.getAllVisualPropertyDependencies()) {
				if (dep.getParentVisualProperty().getTargetDataType() == targetDataType)
					map.put(dep.getParentVisualProperty(), dep);
			}
			
			return map;
		}
	}
	
	private void applyDependencies(final CyNetworkView netView) {
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			final VisualProperty<?> parentVP = dep.getParentVisualProperty();
			
			if (dep.isDependencyEnabled()) {
				// Dependency is enabled.  Need to use parent value instead.
				final Set<VisualProperty<?>> vpSet = new HashSet<VisualProperty<?>>(dep.getVisualProperties());
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
