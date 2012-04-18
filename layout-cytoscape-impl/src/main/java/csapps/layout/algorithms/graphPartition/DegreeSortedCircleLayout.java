package csapps.layout.algorithms.graphPartition;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class DegreeSortedCircleLayout extends AbstractLayoutAlgorithm<DegreeSortedCircleContext> {
	/**
	 * Creates a new DegreeSortedCircleLayout object.
	 */
	public DegreeSortedCircleLayout() {
		super("degree-circle", "Degree Sorted Circle Layout");
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, DegreeSortedCircleContext context, Set<View<CyNode>> nodesToLayOut) {
		return new TaskIterator(new DegreeSortedCircleLayoutTask(getName(), networkView, nodesToLayOut, getSupportedNodeAttributeTypes(), getSupportedEdgeAttributeTypes(), getInitialAttributeList(), context));
	}
	
	@Override
	public DegreeSortedCircleContext createLayoutContext() {
		return new DegreeSortedCircleContext();
	}
}
