package org.cytoscape.editor.internal;


import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class DropNodeViewTaskFactoryImpl implements DropNodeViewTaskFactory {
	private final CyNetworkManager netMgr;

	public DropNodeViewTaskFactoryImpl(CyNetworkManager netMgr) {
		this.netMgr = netMgr;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nv, CyNetworkView view, Transferable t, Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new AddNestedNetworkTask(nv, view, netMgr, t));
	}
	
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView, Transferable t, Point2D javaPt, Point2D xformPt) {
		return true;
	}
}
