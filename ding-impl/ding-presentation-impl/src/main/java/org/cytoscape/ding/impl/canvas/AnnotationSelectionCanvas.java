package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class AnnotationSelectionCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	private final DRenderingEngine re;
	private boolean show = true;
	
	public AnnotationSelectionCanvas(GP graphics, DRenderingEngine re) {
		super(graphics);
		this.re = re;
	}
	
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(!show)
			return;
		if(pm.isCancelled())
			return;
		
		Graphics2D g = graphicsProvider.getGraphics();
		if(g == null)
			return;
		
		var selection = re.getCyAnnotator().getAnnotationSelection();
		selection.paint(g);		
		
		pm.done();
		g.dispose();
	}

	public void show(boolean show) {
		this.show = show;
	}
}
