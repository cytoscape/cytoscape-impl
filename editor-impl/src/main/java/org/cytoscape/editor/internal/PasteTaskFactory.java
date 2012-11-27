package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class PasteTaskFactory extends AbstractNetworkViewLocationTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final ClipboardManagerImpl clipMgr;
	private final UndoSupport undoSupport;
	
	public PasteTaskFactory(final ClipboardManagerImpl mgr, CyEventHelper eh, 
	                        UndoSupport undoSupport, VisualMappingManager vmm) {
		this.clipMgr = mgr;
		this.eh = eh;
		this.vmm = vmm;
		this.undoSupport = undoSupport;
	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		if (networkView == null)
			return false;

		return clipMgr.clipboardHasData();
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, 
	                                       Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new PasteTask(vmm, networkView, xformPt, clipMgr, undoSupport));
	}

}
