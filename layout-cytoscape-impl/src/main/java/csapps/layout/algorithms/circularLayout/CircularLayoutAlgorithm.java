package csapps.layout.algorithms.circularLayout;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class CircularLayoutAlgorithm extends AbstractLayoutAlgorithm {
	/**
	 * Creates a new Layout object.
	 */
	public CircularLayoutAlgorithm(UndoSupport undo) {
		super("circular", "Circular Layout", undo);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		return new TaskIterator(new CircularLayoutAlgorithmTask(getName(), networkView, nodesToLayOut, (CircularLayoutContext)context, undoSupport));
	}
	
	@Override
	public CircularLayoutContext createLayoutContext() {
		return new CircularLayoutContext();
	}
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
