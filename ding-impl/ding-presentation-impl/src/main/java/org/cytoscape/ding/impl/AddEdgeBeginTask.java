package org.cytoscape.ding.impl;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddEdgeBeginTask extends AbstractNodeViewTask {

	private final CyServiceRegistrar serviceRegistrar;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public AddEdgeBeginTask(final View<CyNode> nv, final CyNetworkView view, final CyServiceRegistrar serviceRegistrar) {
		super(nv, view);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(netView);
		InputHandlerGlassPane glassPane = re.getInputHandlerGlassPane();
		glassPane.beginAddingEdge(nodeView);
	}
		
//		
//		View<CyNode> sourceNode = AddEdgeStateMonitor.getSourceNode(netView);
//		
//		if (sourceNode == null) {
//			AddEdgeStateMonitor.setSourceNode(netView, nodeView);
//			double[] coords = new double[2];
//			coords[0] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
//			coords[1] = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
//			DRenderingEngine re = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(netView);
//			re.xformNodeToComponentCoords(coords);
//			
//			Point sourceP = new Point();
//			sourceP.setLocation(coords[0], coords[1]);
//			AddEdgeStateMonitor.setSourcePoint(netView, sourceP);
//		} else {
//			// set the name attribute for the new node
//			CyNetwork net = netView.getModel();
//			CyNode targetNode = nodeView.getModel();
//
//			// MKTODO check for exception?
//			final CyEdge newEdge = net.addEdge(sourceNode.getModel(), targetNode, true);
//			final String interaction = "interacts with";
//			String edgeName = net.getRow(sourceNode).get(CyRootNetwork.SHARED_NAME, String.class);
//			edgeName += " (" + interaction + ") ";
//			edgeName += net.getRow(targetNode).get(CyRootNetwork.SHARED_NAME, String.class);
//
//			CyRow edgeRow = net.getRow(newEdge, CyNetwork.DEFAULT_ATTRS);
//			edgeRow.set(CyNetwork.NAME, edgeName);
//			edgeRow.set(CyEdge.INTERACTION, interaction);
//
//			AddEdgeStateMonitor.setSourceNode(netView, null);
//			
//			// Apply visual style
//			
//			// To make sure the edge view is created before applying the style
//			serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
//			
//			VisualStyle vs = serviceRegistrar.getService(VisualMappingManager.class).getVisualStyle(netView);
//			View<CyEdge> edgeView = netView.getEdgeView(newEdge);
//			
//			if (edgeView != null)
//				vs.apply(edgeRow, edgeView);
//			
//			netView.updateView();
//		}
}
