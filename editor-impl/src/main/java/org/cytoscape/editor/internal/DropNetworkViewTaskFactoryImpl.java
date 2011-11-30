package org.cytoscape.editor.internal;


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;


public class DropNetworkViewTaskFactoryImpl implements DropNetworkViewTaskFactory {
	private CyNetworkView view;
	private Transferable t;
	private Point2D javaPt;
	private Point2D xformPt;
	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	
	public DropNetworkViewTaskFactoryImpl(CyEventHelper eh, VisualMappingManager vmm) {
		this.eh = eh;
		this.vmm = vmm;
	}

	public void setNetworkView(CyNetworkView view) {
		this.view = view;
	}

	public void setDropInformation(Transferable t, Point2D javaPt, Point2D xformPt) {
		this.t = t;
		this.javaPt = javaPt;
		this.xformPt = xformPt;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DropNetworkViewTask(vmm, view, t, xformPt, eh));
	}
}
