
package org.cytoscape.ding.impl; 

import java.awt.Point;

import javax.swing.RootPaneContainer;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddEdgeTask extends AbstractNodeViewTask {

	private static final Logger logger = LoggerFactory.getLogger(AddEdgeTask.class);
	private static int numberofedges = 1;
	
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
		
			// set the name attribute for the new node
			
			CyNetwork net = netView.getModel();
			CyNode targetNode = nodeView.getModel();
		
			CyEdge newEdge = net.addEdge(sourceNode,targetNode,true);
			final String interaction = "interaction";
			String edgeName =  net.getRow(sourceNode).get(CyRootNetwork.SHARED_NAME, String.class); 
			edgeName+=" (" + interaction + ") ";
			edgeName+= net.getRow(targetNode).get(CyRootNetwork.SHARED_NAME, String.class);
			
			CyRow edgeRow =  net.getRow(newEdge, CyNetwork.DEFAULT_ATTRS);
			edgeRow.set(CyNetwork.NAME, edgeName);
			edgeRow.set(CyEdge.INTERACTION, interaction);
			
			netView.updateView();
			AddEdgeStateMonitor.setSourceNode(netView,null);
		}
	}
}
