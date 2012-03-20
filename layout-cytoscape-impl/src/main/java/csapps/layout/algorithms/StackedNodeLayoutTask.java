package csapps.layout.algorithms;

import java.util.Iterator;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.AbstractBasicLayoutTask;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;

public class StackedNodeLayoutTask extends AbstractBasicLayoutTask {

	private StackedNodeLayoutContext context;
	
	public StackedNodeLayoutTask(final String name, final StackedNodeLayoutContext context) {
		super(name, context);
		this.context = context;
	}

	final protected void doLayout(final TaskMonitor taskMonitor) {

		List<CyNode> nodes;
		if (selectedOnly){
			nodes = CyTableUtil.getNodesInState(networkView.getModel(),"selected",true);
		}
		else {
			// select all nodes from the view
			nodes = networkView.getModel().getNodeList();			
		}
		construct(nodes);
	}
	
	/**
	 *  DOCUMENT ME!
	 * @param nodes 
	 */
	public void construct(List<CyNode> nodes) {
		double yPosition = context.y_start_position;
		
		Iterator it = nodes.iterator();

		while (it.hasNext()) {
			CyNode node = (CyNode) it.next();
			View<CyNode> nodeView = networkView.getNodeView(node);
			
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, context.x_position);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPosition);
			
			int y = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).toString())).intValue();
			
			yPosition += y * 2;
		}
	}
}
