package org.cytoscape.ding.impl;

import java.awt.Image;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class EdgeCanvas extends DingCanvas {

	private final DRenderingEngine re;
	
	public EdgeCanvas(DRenderingEngine re, int width, int height) {
		super(width, height);
		this.re = re;
	}

	@Override
	public Image paintImage(ProgressMonitor pm, RenderDetailFlags flags) {
		var netViewSnapshot = re.getViewModelSnapshot();
		var graphics = new GraphGraphics(image);
		var edgeDetails = re.getEdgeDetails();
		var nodeDetails = re.getNodeDetails();
		
		if(pm.isCancelled())
			return null;
		
		GraphRenderer.renderEdges(pm, graphics, netViewSnapshot, flags, nodeDetails, edgeDetails);
		
		return image.getImage();
	}

}
