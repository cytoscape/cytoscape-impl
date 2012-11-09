package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

		final VisualLexicon lex = lexManager.getAllVisualLexicon().iterator().next();
		applyDefaultsInParallel(netView, lexManager.getNodeVisualProperties(), lex);
		applyDefaultsInParallel(netView, lexManager.getEdgeVisualProperties(), lex);
		applyDefaultsInParallel(netView, lexManager.getNetworkVisualProperties(), lex);

		final Map<VisualProperty<?>, VisualPropertyDependency<?>> dependencyMap = applyDependencies(netView);
		
		ExecutorService exe = Executors.newCachedThreadPool();
		exe.submit(new ApplyMappingsTask(netView, nodeViews, lexManager.getNodeVisualProperties(), dependencyMap));
		exe.submit(new ApplyMappingsTask(netView, edgeViews, lexManager.getEdgeVisualProperties(), dependencyMap));
		exe.submit(new ApplyMappingsTask(netView, networkViewSet, lexManager.getNetworkVisualProperties(), dependencyMap));
		
		try {
			exe.shutdown();
		} catch (Exception ex) {
			logger.warn("Create apply operation failed.", ex);
		} finally {

		}
	}
	

	private void applyDefaultsInParallel(final CyNetworkView netView, final Collection<VisualProperty<?>> vps, final VisualLexicon lex) {
		final ExecutorService exe = Executors.newCachedThreadPool();
		
		for (final VisualProperty<?> vp : vps) {
			final VisualLexiconNode node = lex.getVisualLexiconNode(vp);
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
		private final Collection<VisualProperty<?>> visualProperties;
		private final Map<VisualProperty<?>, VisualPropertyDependency<?>> dependencyMap;
		
		ApplyMappingsTask(final CyNetworkView netView,
				final Collection<? extends View<? extends CyIdentifiable>> views,
				final Collection<VisualProperty<?>> visualProperties,
				final Map<VisualProperty<?>, VisualPropertyDependency<?>> dependencyMap) {
			this.netView = netView;
			this.views = views;
			this.visualProperties = visualProperties;
			this.dependencyMap = dependencyMap;
		}
		
		@Override
		public void run() {
			for (VisualProperty<?> vp : visualProperties) {
				final VisualPropertyDependency<?> dep = dependencyMap.get(vp);
				
				if (dep != null)
					continue; // Already handled when applying dependencies
				
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

				if (mapping != null) {
					final CyNetwork net = netView.getModel();

					for (final View<? extends CyIdentifiable> v : views) {
						Object value = mapping.getMappedValue(net.getRow(v.getModel()));
						
						if (value != null)
							v.setVisualProperty(vp, value);
					}
				}
			}
		}
	}
	
	private Map<VisualProperty<?>, VisualPropertyDependency<?>> applyDependencies(final CyNetworkView netView) {
		
		final Map<VisualProperty<?>, VisualPropertyDependency<?>> dependencyMap = 
				new HashMap<VisualProperty<?>, VisualPropertyDependency<?>>();
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			final VisualProperty<?> parentVP = dep.getParentVisualProperty();
			dependencyMap.put(parentVP, dep); // Index the dependencies by their visual properties
			
			if (dep.isDependencyEnabled()) {
				// Dependency is enabled.  Need to use parent value instead.
				final Set<VisualProperty<?>> vpSet = new HashSet<VisualProperty<?>>(dep.getVisualProperties());
				vpSet.add(parentVP);
				
				Object defaultValue = style.getDefaultValue(parentVP);
				if (defaultValue == null)
					defaultValue = parentVP.getDefault();
				
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(parentVP);
				
				for (VisualProperty<?> vp : vpSet) {
					dependencyMap.put(vp, dep);
					
					netView.setViewDefault(vp, defaultValue);
					
					if (mapping != null) {
						final CyNetwork net = netView.getModel();
						Collection<View<? extends CyIdentifiable>> views = null;

						if (vp.getTargetDataType() == CyNode.class)
							views = (Collection) netView.getNodeViews();
						else if (vp.getTargetDataType() == CyEdge.class)
							views = (Collection) netView.getEdgeViews();
						
						if (views != null) {
							for (final View<? extends CyIdentifiable> v : views) {
								Object value = mapping.getMappedValue(net.getRow(v.getModel()));
								
								if (value != null)
									v.setVisualProperty(vp, value);
							}
						}
					}
				}
			}
		}
		
		return dependencyMap;
	}
}
