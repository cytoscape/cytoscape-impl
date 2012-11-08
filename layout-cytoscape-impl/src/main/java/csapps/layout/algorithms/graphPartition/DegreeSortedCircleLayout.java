package csapps.layout.algorithms.graphPartition;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class DegreeSortedCircleLayout extends AbstractLayoutAlgorithm{
	/**
	 * Creates a new DegreeSortedCircleLayout object.
	 */
	public DegreeSortedCircleLayout(UndoSupport undo) {
		super("degree-circle", "Degree Sorted Circle Layout", undo);
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		return new TaskIterator(new DegreeSortedCircleLayoutTask(toString(), networkView, nodesToLayOut, (DegreeSortedCircleContext)context, attrName, undoSupport));
	}
	
	@Override
	public Object createLayoutContext() {
		return new DegreeSortedCircleContext();
	}
	
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
