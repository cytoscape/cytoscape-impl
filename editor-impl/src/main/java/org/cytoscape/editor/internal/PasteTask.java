package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

public class PasteTask extends AbstractNetworkViewTask{

	private final VisualMappingManager vmm;
	private final Point2D xformPt;
	private static int new_node_index =1;
	private final ClipboardManagerImpl clipMgr;
	
	public PasteTask(final VisualMappingManager vmm, final CyNetworkView view,final Point2D xformPt, 
	                 final ClipboardManagerImpl clip) {
		super(view);
		this.vmm = vmm;
		this.xformPt = xformPt;
		this.clipMgr = clip;
		
	}

	// TODO: add an isRead that is ready when we have something to paste

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (xformPt == null)
			clipMgr.paste(view, 0.0, 0.0, true);
		else
			clipMgr.paste(view, xformPt.getX(), xformPt.getY(), true);

		view.updateView();
		
		// Apply visual style
		VisualStyle vs = vmm.getVisualStyle(view);
		vs.apply(view);
	}
}
