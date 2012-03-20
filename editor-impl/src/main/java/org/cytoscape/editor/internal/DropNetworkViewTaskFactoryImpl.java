package org.cytoscape.editor.internal;


import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;


public class DropNetworkViewTaskFactoryImpl implements DropNetworkViewTaskFactory {
	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	final CyRootNetworkManager rnm;
	
	public DropNetworkViewTaskFactoryImpl(CyEventHelper eh, VisualMappingManager vmm, CyRootNetworkManager rnm) {
		this.eh = eh;
		this.vmm = vmm;
		this.rnm = rnm;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, Transferable t, Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new DropNetworkViewTask(vmm, rnm, view, t, xformPt, eh));
	}
	
	@Override
	public boolean isReady(CyNetworkView networkView, Transferable t, Point2D javaPt, Point2D xformPt) {
		return true;
	}
}
