package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class AnnotationSelectionCanvas extends DingCanvas<NetworkImageBuffer> {

	private final DRenderingEngine re;
	private boolean show = true;
	
	public AnnotationSelectionCanvas(NetworkImageBuffer transform, DRenderingEngine re) {
		super(transform);
		this.re = re;
	}
	
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(!show)
			return;
		if(pm.isCancelled())
			return;
		
		var selection = re.getCyAnnotator().getAnnotationSelection();
		
		Graphics2D g = transform.getGraphics();
		selection.paint(g);		
		g.dispose();
	}

	public void show(boolean show) {
		this.show = show;
	}
}
