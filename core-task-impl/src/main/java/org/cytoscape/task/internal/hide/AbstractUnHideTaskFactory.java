package org.cytoscape.task.internal.hide;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
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
		return new TaskIterator(new UnHideTask(description, unhideNodes, unhideEdges, view, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(final CyNetworkView networkView) {
		if (super.isReady(networkView)) {
			// Also check whether or not there is at least one hidden element...
			final List<View<? extends CyIdentifiable>> views = new ArrayList<>();
			
			if (unhideNodes)
				views.addAll(networkView.getNodeViews());
			if (unhideEdges)
				views.addAll(networkView.getEdgeViews());
			
			for (View<? extends CyIdentifiable> v : views) {
				final VisualProperty<?> vp = v.getModel() instanceof CyNode ? 
						BasicVisualLexicon.NODE_VISIBLE : BasicVisualLexicon.EDGE_VISIBLE;
				
				if (v.getVisualProperty(vp) == Boolean.FALSE)
					return true;
			}
		}
		
		return false;
	}
	
	public String getDescription() {
		return description;
	}
}
