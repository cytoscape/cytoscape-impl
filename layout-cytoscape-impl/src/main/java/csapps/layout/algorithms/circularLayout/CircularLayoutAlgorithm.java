package csapps.layout.algorithms.circularLayout;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;


public class CircularLayoutAlgorithm extends AbstractLayoutAlgorithm<CircularLayoutContext> {
	/**
	 * Creates a new Layout object.
	 */
	public CircularLayoutAlgorithm() {
		super("circular", "Circular Layout", false);
	}

	@Override
	public TaskIterator createTaskIterator(CircularLayoutContext context) {
		return new TaskIterator(
				new CircularLayoutAlgorithmTask(getName(), context));
	}
	
	@Override
	public CircularLayoutContext createLayoutContext() {
		return new CircularLayoutContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
