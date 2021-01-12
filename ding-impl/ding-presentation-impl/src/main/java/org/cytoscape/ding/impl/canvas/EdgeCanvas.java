package org.cytoscape.ding.impl.canvas;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.canvas.NetworkTransform.Snapshot;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class EdgeCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	private final DRenderingEngine re;
	
	private Snapshot transformSnapshot;
	private ImageGraphicsProvider slowEdgeCanvas;
	
	
	public EdgeCanvas(GP graphics, DRenderingEngine re) {
		super(graphics);
		this.re = re;
	}
	
	@Override
	public String getCanvasDebugName() {
		return "Edges";
	}
	
	public void setBufferPanOnNextPaint(Snapshot transformSnapshot, ImageGraphicsProvider slowEdgeCanvas) {
		this.transformSnapshot = transformSnapshot;
		this.slowEdgeCanvas = slowEdgeCanvas;
	}

	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
//		if(transformSnapshot != null && graphicsProvider instanceof NetworkImageBuffer) {
//			((NetworkImageBuffer) graphicsProvider).bufferTransform(transformSnapshot, slowEdgeCanvas);
//		} 
//		else {
			var graphics = new GraphGraphics(graphicsProvider);
			
			var netViewSnapshot = re.getViewModelSnapshot();
			var edgeDetails = re.getEdgeDetails();
			var nodeDetails = re.getNodeDetails();
			
			GraphRenderer.renderEdges(pm, graphics, netViewSnapshot, flags, nodeDetails, edgeDetails);
//		}
		
		transformSnapshot = null;
		slowEdgeCanvas = null;
	}
	
}
