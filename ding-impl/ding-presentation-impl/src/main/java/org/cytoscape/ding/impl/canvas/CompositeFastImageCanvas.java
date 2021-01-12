package org.cytoscape.ding.impl.canvas;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;

public class CompositeFastImageCanvas extends CompositeImageCanvas {

	private final CompositeImageCanvas slowCanvas;
	private NetworkTransform.Snapshot transformSnapshot;
	
	public CompositeFastImageCanvas(DRenderingEngine re, GraphLOD lod, int w, int h, CompositeImageCanvas slowCanvas) {
		super(re, lod, w, h);
		this.slowCanvas = slowCanvas;
	}

	
	private void takeTransformSnapshot() {
		if(transformSnapshot == null) {
			transformSnapshot = getTransform().snapshot();
		}
	}
	
	private void clearTransformSnapshot() {
		transformSnapshot = null;
	}
	
	@Override
	public void setCenter(double x, double y) {
		takeTransformSnapshot();
		super.setCenter(x, y);
	}

	public ImageFuture paint(ProgressMonitor pm) {
		clearTransformSnapshot();
		return paintFuture(pm, null);
	}
	
	@Override
	public ImageFuture paintInteractivePan(ProgressMonitor pm) {
		var edgeCanvas = getEdgeCanvas();
		edgeCanvas.setBufferPanOnNextPaint(transformSnapshot, slowCanvas.getEdgeCanvas().getGraphicsProvier());
		var future = super.paint(pm);
		return future;
	}
	
}
