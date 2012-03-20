package csapps.layout.algorithms.graphPartition;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;


public class DegreeSortedCircleLayout extends AbstractLayoutAlgorithm<DegreeSortedCircleContext> {
	/**
	 * Creates a new DegreeSortedCircleLayout object.
	 */
	public DegreeSortedCircleLayout() {
		super("degree-circle", "Degree Sorted Circle Layout", true);
	}

	public TaskIterator createTaskIterator(DegreeSortedCircleContext context) {
		return new TaskIterator(
			new DegreeSortedCircleLayoutTask(getName(), context));
	}
	
	@Override
	public DegreeSortedCircleContext createLayoutContext() {
		return new DegreeSortedCircleContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
