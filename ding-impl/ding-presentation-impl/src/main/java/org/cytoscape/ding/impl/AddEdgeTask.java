
package org.cytoscape.ding.impl; 

import java.awt.Point;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddEdgeTask extends AbstractNodeViewTask {

	private static final Logger logger = LoggerFactory.getLogger(AddEdgeTask.class);

	public AddEdgeTask(View<CyNode> nv, CyNetworkView view){
		super(nv,view);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		
		CyNode sourceNode = AddEdgeStateMonitor.getSourceNode(netView);
		if ( sourceNode == null ) {
			AddEdgeStateMonitor.setSourceNode(netView,nodeView.getModel());
			double[] coords = new double[2];
			coords[0] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			coords[1] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			((DGraphView)netView).xformNodeToComponentCoords(coords);
			Point sourceP = new Point();
			sourceP.setLocation(coords[0], coords[1]);
			AddEdgeStateMonitor.setSourcePoint(netView,sourceP);
		} else {
			CyNetwork net = netView.getModel();
			CyNode targetNode = nodeView.getModel();
			
			net.addEdge(sourceNode,targetNode,true);
			netView.updateView();
			AddEdgeStateMonitor.setSourceNode(netView,null);
		}
	}
}
