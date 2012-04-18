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


public class ISOMLayout extends AbstractLayoutAlgorithm<ISOMLayoutContext> {
	/**
	 * Creates a new ISOMLayout object.
	 */
	public ISOMLayout() {
		super("isom", "Inverted Self-Organizing Map Layout");
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, ISOMLayoutContext context, Set<View<CyNode>> nodesToLayOut) {
		return new TaskIterator(new ISOMLayoutTask(getName(), networkView, nodesToLayOut, getSupportedNodeAttributeTypes(), getSupportedEdgeAttributeTypes(), getInitialAttributeList(), context));
	}
	
	@Override
	public ISOMLayoutContext createLayoutContext() {
		return new ISOMLayoutContext();
	}
}
