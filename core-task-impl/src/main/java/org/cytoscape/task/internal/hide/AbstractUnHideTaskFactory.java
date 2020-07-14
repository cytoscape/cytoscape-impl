package org.cytoscape.task.internal.hide;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

public abstract class AbstractUnHideTaskFactory extends AbstractNetworkViewTaskFactory {

	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final boolean justSelected;
	private final CyServiceRegistrar serviceRegistrar;

	public AbstractUnHideTaskFactory(
			final String description,
			final boolean unhideNodes,
			final boolean unhideEdges,
			final boolean justSelected,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.justSelected = justSelected;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyNetworkView view) {
		return new TaskIterator(new UnHideFromSelectionTask(description, unhideNodes, unhideEdges, justSelected, view, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(final CyNetworkView networkView) {
		if(super.isReady(networkView)) {
			if(unhideNodes && hasHiddenNodes(networkView)) {
				return true;
			}
			if(unhideEdges && hasHiddenEdges(networkView)) {
				return true;
			}
		}
		return false;
	}
	
	
	private boolean hasHiddenNodes(CyNetworkView networkView) {
		// fast path
		if(networkView.supportsSnapshots()) {
			CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
			if(snapshot.isTrackedNodeKey("HIDDEN_NODES")) {
				return snapshot.getTrackedNodeCount("HIDDEN_NODES") > 0;
			}
		} 
		// slow path
		var views = networkView.getNodeViewsIterable();
		return hasHidden(views, BasicVisualLexicon.NODE_VISIBLE);
	}
	
	private boolean hasHiddenEdges(CyNetworkView networkView) {
		// fast path
		if(networkView.supportsSnapshots()) {
			CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
			if(snapshot.isTrackedEdgeKey("HIDDEN_EDGES")) {
				return snapshot.getTrackedEdgeCount("HIDDEN_EDGES") > 0;
			}
		} 
		
		// Maybe bail out, checking all edges for visibility doesn't scale for large networks
		if(networkView.getModel().getEdgeCount() > 400000) {
			return true;
		}
		
		// slow path
		var views = networkView.getEdgeViewsIterable();
		return hasHidden(views, BasicVisualLexicon.EDGE_VISIBLE);
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
