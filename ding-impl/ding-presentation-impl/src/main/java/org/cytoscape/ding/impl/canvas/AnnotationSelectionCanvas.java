package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class AnnotationSelectionCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	public static final String DEBUG_NAME = "Annotation sel";
	
	private final DRenderingEngine re;
	private boolean show = true;
	
	public AnnotationSelectionCanvas(GP graphics, DRenderingEngine re) {
		super(graphics);
		this.re = re;
	}
	
	@Override
	public String getCanvasDebugName() {
		return DEBUG_NAME;
	}
	
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		if(!show)
			return;
		
		Graphics2D g = graphicsProvider.getGraphics(true);
		if(g == null)
			return;
		
		var selection = re.getCyAnnotator().getAnnotationSelection();
		selection.paint(g);		
		
		g.dispose();
	}

	public void show(boolean show) {
		this.show = show;
	}
}
