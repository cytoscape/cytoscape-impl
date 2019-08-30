package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class AnnotationSelectionCanvas extends DingCanvas<NetworkImageBuffer> {

	private final DRenderingEngine re;
	
	public AnnotationSelectionCanvas(NetworkImageBuffer transform, DRenderingEngine re) {
		super(transform);
		this.re = re;
	}
	
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(pm.isCancelled())
			return;
		
		var selection = re.getCyAnnotator().getAnnotationSelection();
		if(selection == null || selection.isEmpty())
			return;
		
		Graphics2D g = transform.getGraphics();
		g.setTransform(transform.getAffineTransform());
//		g.translate(selection.getX(), selection.getY());
		selection.paint(g);
		g.dispose();
	}
}
