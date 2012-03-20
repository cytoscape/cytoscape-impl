/*
 * This is based on the ISOMLayout from the JUNG project.
 */
package csapps.layout.algorithms.graphPartition;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;


public class ISOMLayout extends AbstractLayoutAlgorithm<ISOMLayoutContext> {
	/**
	 * Creates a new ISOMLayout object.
	 */
	public ISOMLayout() {
		super("isom", "Inverted Self-Organizing Map Layout", true);
	}

	public TaskIterator createTaskIterator(ISOMLayoutContext context) {
		return new TaskIterator(
			new ISOMLayoutTask(getName(), context));
	}
	
	@Override
	public ISOMLayoutContext createLayoutContext() {
		return new ISOMLayoutContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
