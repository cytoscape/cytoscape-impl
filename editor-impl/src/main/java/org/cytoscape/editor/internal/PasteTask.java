package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class PasteTask extends AbstractNetworkViewTask {

	private final VisualMappingManager vmm;
	private final Point2D xformPt;
	private final ClipboardManagerImpl clipMgr;
	private final UndoSupport undoSupport;

	public PasteTask(final VisualMappingManager vmm, final CyNetworkView view, final Point2D xformPt,
			final ClipboardManagerImpl clip, final UndoSupport undoSupport) {
		super(view);
		this.vmm = vmm;
		this.xformPt = xformPt;
		this.clipMgr = clip;
		this.undoSupport = undoSupport;

	}

	// TODO: add an isRead that is ready when we have something to paste

	@Override
	public void run(TaskMonitor tm) throws Exception {
		List<CyIdentifiable> pastedObjects;
		if (xformPt == null)
			pastedObjects = clipMgr.paste(view, 0.0, 0.0);
		else
			pastedObjects = clipMgr.paste(view, xformPt.getX(), xformPt.getY());

		undoSupport.postEdit(new PasteEdit(vmm, view, xformPt, clipMgr, pastedObjects));
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(view);

		view.updateView();
	}
}
