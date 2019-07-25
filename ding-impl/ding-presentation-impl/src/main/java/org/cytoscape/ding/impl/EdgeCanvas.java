package org.cytoscape.ding.impl;

import java.awt.Image;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.view.model.CyNetworkViewSnapshot;

public class EdgeCanvas extends DingCanvas {

	private final DRenderingEngine re;
	
	public EdgeCanvas(CompositeCanvas parent, DRenderingEngine re) {
		super(parent.getTransform().getWidth(), parent.getTransform().getHeight());
		this.re = re;
	}

	@Override
	public Image paintImage(RenderDetailFlags flags) {
		CyNetworkViewSnapshot netViewSnapshot = re.getViewModelSnapshot();
		GraphGraphics graphics = new GraphGraphics(image);
		EdgeDetails edgeDetails = re.getEdgeDetails();
		NodeDetails nodeDetails = re.getNodeDetails();
		
		GraphRenderer.renderEdges(graphics, netViewSnapshot, flags, nodeDetails, edgeDetails);
		
		return image.getImage();
	}

}
