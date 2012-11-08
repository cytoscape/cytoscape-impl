/*
 * This is based on the ISOMLayout from the JUNG project.
 */
package csapps.layout.algorithms.graphPartition;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class ISOMLayout extends AbstractLayoutAlgorithm {
	/**
	 * Creates a new ISOMLayout object.
	 */
	public ISOMLayout(UndoSupport undo) {
		super("isom", "Inverted Self-Organizing Map Layout", undo);
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,String attrName) {
		return new TaskIterator(new ISOMLayoutTask(toString(), networkView, nodesToLayOut, (ISOMLayoutContext) context, attrName, undoSupport));
	}
	
	@Override
	public Object createLayoutContext() {
		return new ISOMLayoutContext();
	}
	
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
