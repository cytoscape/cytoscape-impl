package org.cytoscape.task.internal.hide;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

public abstract class AbstractUnHideTaskFactory extends AbstractNetworkViewTaskFactory {

	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final CyServiceRegistrar serviceRegistrar;

	public AbstractUnHideTaskFactory(
			final String description,
			final boolean unhideNodes,
			final boolean unhideEdges,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyNetworkView view) {
		return new TaskIterator(new UnHideAllTask(description, unhideNodes, unhideEdges, view, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(final CyNetworkView networkView) {
		if(super.isReady(networkView)) {
			if(unhideNodes) {
				var views = networkView.getNodeViewsIterable();
				if(hasHidden(views, BasicVisualLexicon.NODE_VISIBLE)) {
					return true;
				}
			}
			if(unhideEdges) {
				// Checking all edges for visibility doesn't scale for large networks, just bail out
				if(networkView.getModel().getEdgeCount() > 500000) {
					return true;
				}
				var views = networkView.getEdgeViewsIterable();
				if(hasHidden(views, BasicVisualLexicon.EDGE_VISIBLE)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private <T> boolean hasHidden(Iterable<View<T>> views, VisualProperty<?> vp) {
		for(var v : views) {
			if(v.getVisualProperty(vp) == Boolean.FALSE) {
				return true;
			}
		}
		return false;
	}
	
	
	public String getDescription() {
		return description;
	}
}
