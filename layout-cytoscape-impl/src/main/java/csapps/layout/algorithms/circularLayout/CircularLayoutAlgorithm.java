package csapps.layout.algorithms.circularLayout;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class CircularLayoutAlgorithm extends AbstractLayoutAlgorithm<CircularLayoutContext> {
	/**
	 * Creates a new Layout object.
	 */
	public CircularLayoutAlgorithm() {
		super("circular", "Circular Layout");
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, CircularLayoutContext context, Set<View<CyNode>> nodesToLayOut) {
		return new TaskIterator(new CircularLayoutAlgorithmTask(getName(), networkView, nodesToLayOut, getSupportedNodeAttributeTypes(), getSupportedEdgeAttributeTypes(), getInitialAttributeList(), context));
	}
	
	@Override
	public CircularLayoutContext createLayoutContext() {
		return new CircularLayoutContext();
	}
}
