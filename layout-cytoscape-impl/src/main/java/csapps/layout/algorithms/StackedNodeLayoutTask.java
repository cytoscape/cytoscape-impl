package csapps.layout.algorithms;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.AbstractBasicLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;

public class StackedNodeLayoutTask extends AbstractBasicLayoutTask {

	private double y_start_position;
	private double x_position;
	private Collection nodeViews;
	private Collection nodes;
	private boolean selectedOnly = false;
	
	public StackedNodeLayoutTask(final CyNetworkView networkView, final String name,
			  final boolean selectedOnly, final Set<View<CyNode>> staticNodes,
			  double x_position, double y_start_position)

	{
		super(networkView, name, selectedOnly, staticNodes);
		
		this.selectedOnly = selectedOnly;
		
		this.x_position =x_position;
		this.y_start_position=y_start_position;
		this.nodeViews = staticNodes;
		
	}

	final protected void doLayout(final TaskMonitor taskMonitor) {

		if (selectedOnly){
			nodes = CyTableUtil.getNodesInState(networkView.getModel(),"selected",true);
		}
		else {
			// select all nodes from the view
			nodes = networkView.getModel().getNodeList();			
		}
		construct();
	}
	
	/**
	 *  DOCUMENT ME!
	 */
	public void construct() {
		double yPosition = y_start_position;
		
		Iterator it = nodes.iterator();

		while (it.hasNext()) {
			CyNode node = (CyNode) it.next();
			View<CyNode> nodeView = networkView.getNodeView(node);
			
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x_position);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPosition);
			
			int y = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).toString())).intValue();
			
			yPosition += y * 2;
		}
	}
}
