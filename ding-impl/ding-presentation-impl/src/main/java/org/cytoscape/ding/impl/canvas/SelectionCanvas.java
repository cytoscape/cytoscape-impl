package org.cytoscape.ding.impl.canvas;

import java.awt.Graphics2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class SelectionCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	public static final String DEBUG_NAME = "Selection";
	
	private final DRenderingEngine re;
	private boolean show = true;
	
	public SelectionCanvas(GP graphics, DRenderingEngine re) {
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
		
		var annotationSelection = re.getCyAnnotator().getAnnotationSelection();
		annotationSelection.paint(g);
		
		var labelSelection = re.getLabelSelectionManager();
		labelSelection.paint(g);
		
		g.dispose();
	}

	public void show(boolean show) {
		this.show = show;
	}
}
