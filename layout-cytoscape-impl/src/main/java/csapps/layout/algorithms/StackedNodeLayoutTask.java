package csapps.layout.algorithms;

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class StackedNodeLayoutTask extends AbstractLayoutTask {

	private StackedNodeLayoutContext context;
	
	public StackedNodeLayoutTask(final String name, CyNetworkView networkView, final StackedNodeLayoutContext context, Set<View<CyNode>> nodesToLayOut, String attr, UndoSupport undo) {
		super(name, networkView, nodesToLayOut, attr, undo);
		this.context = context;
	}

	final protected void doLayout(final TaskMonitor taskMonitor) {
		construct(nodesToLayOut);
	}
	
	/**
	 *  DOCUMENT ME!
	 * @param nodes 
	 */
	public void construct(Set<View<CyNode>> nodes) {
		double yPosition = context.y_start_position;
		
		for (View<CyNode> nodeView : nodes) {
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, context.x_position);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPosition);
			
			int y = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).toString())).intValue();
			
			yPosition += y * 2;
		}
	}
}
