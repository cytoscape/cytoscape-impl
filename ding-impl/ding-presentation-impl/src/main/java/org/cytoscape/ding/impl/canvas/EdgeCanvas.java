package org.cytoscape.ding.impl.canvas;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class EdgeCanvas<T extends NetworkTransform> extends DingCanvas<T> {

	private final DRenderingEngine re;
	
	public EdgeCanvas(T t, DRenderingEngine re) {
		super(t);
		this.re = re;
	}

	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		var netViewSnapshot = re.getViewModelSnapshot();
		var graphics = new GraphGraphics(transform);
		var edgeDetails = re.getEdgeDetails();
		var nodeDetails = re.getNodeDetails();
		
		if(pm.isCancelled())
			return;
		
		GraphRenderer.renderEdges(pm, graphics, netViewSnapshot, flags, nodeDetails, edgeDetails);
	}
}
