package org.cytoscape.editor.internal;

//import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class PasteTaskFactory extends AbstractNetworkViewLocationTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final ClipboardManagerImpl clipMgr;
	
	public PasteTaskFactory(final ClipboardManagerImpl mgr, CyEventHelper eh, VisualMappingManager vmm) {
		this.clipMgr = mgr;
		this.eh = eh;
		this.vmm = vmm;
	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		if (!super.isReady(networkView, javaPt, xformPt))
			return false;

		return clipMgr.clipboardHasData();
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, 
	                                       Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new PasteTask(vmm, networkView, xformPt, clipMgr));
	}

}
