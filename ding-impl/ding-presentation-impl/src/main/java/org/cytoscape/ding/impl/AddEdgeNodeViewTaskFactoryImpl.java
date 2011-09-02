package org.cytoscape.ding.impl; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;


public class AddEdgeNodeViewTaskFactoryImpl implements DropNodeViewTaskFactory {
	private View<CyNode> nv;
	private CyNetworkView view;
	private Point2D xformPt;
	private Point2D javaPt;
	private final CyNetworkManager netMgr;

	public AddEdgeNodeViewTaskFactoryImpl(CyNetworkManager netMgr) {
		this.netMgr = DIUtil.stripProxy(netMgr);
	}

	public void setNodeView(View<CyNode> nv, CyNetworkView view) {
		this.view = view;
		this.nv = nv;
	}

	public void setDropInformation(Transferable t, Point2D javaPt, Point2D xformPt) {
		this.javaPt = javaPt;
		this.xformPt = xformPt;

		AddEdgeStateMonitor.setSourcePoint(view,javaPt);

		// Because the transferable may be null, we leave that
		// tracking to the AddEdgeStateMonitor.
		AddEdgeStateMonitor.setTransferable(view,t);
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new AddEdgeTask(nv, view, AddEdgeStateMonitor.getTransferable(view)));
	}
}
