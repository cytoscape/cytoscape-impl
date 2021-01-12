package org.cytoscape.ding.impl.canvas;

import java.util.Objects;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;

public class CompositeFastImageCanvas extends CompositeImageCanvas {

	private final CompositeImageCanvas slowCanvas;
	
	private NetworkTransform.Snapshot slowCanvasLastPaintSnapshot;
	private NetworkTransform.Snapshot fastCanvasPanStartedSnapshot;
	
	
	public CompositeFastImageCanvas(DRenderingEngine re, GraphLOD lod, int w, int h, CompositeImageCanvas slowCanvas) {
		super(re, lod, w, h);
		this.slowCanvas = slowCanvas;
		
		// Take a snapshot of what the transform was the last time the slow canvas painted.
		slowCanvas.addPaintListener(new CanvasPaintListener() {
			@Override
			public void beforePaint() {
				slowCanvasLastPaintSnapshot = slowCanvas.getTransform().snapshot();
			}
		});
	}
	
	
	public void startPan() {
		fastCanvasPanStartedSnapshot = getTransform().snapshot();
	}
	
	public void endPan() {
		slowCanvasLastPaintSnapshot = null;
		fastCanvasPanStartedSnapshot = null;
	}
	
	
	@Override
	public ImageFuture paint(ProgressMonitor pm) {
		if(fastCanvasPanStartedSnapshot != null && Objects.equals(slowCanvasLastPaintSnapshot, fastCanvasPanStartedSnapshot)) {
			// instead of painting normally, which may involve hiding edges, pan the edge canvas by moving the buffer pixels
			var edgeCanvas = getEdgeCanvas();
			edgeCanvas.setBufferPanOnNextPaint(fastCanvasPanStartedSnapshot, slowCanvas.getEdgeCanvas().getGraphicsProvier());
		}
		
		return super.paint(pm);
	}
	
}
