package org.cytoscape.ding.impl.canvas;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.LabelInfoProvider;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class EdgeCanvas<GP extends GraphicsProvider> extends DingCanvas<GP> {

	private final DRenderingEngine re;
	private final GraphGraphics graphGraphics;
	
	public EdgeCanvas(GP graphics, DRenderingEngine re) {
		super(graphics);
		this.re = re;
		this.graphGraphics = new GraphGraphics(graphics);
	}
	
	@Override
	public String getCanvasDebugName() {
		return "Edges";
	}
	
	@Override
	public void paint(ProgressMonitor pm, RenderDetailFlags flags) {
		var netViewSnapshot = re.getViewModelSnapshot();
		var edgeDetails = re.getEdgeDetails();
		var nodeDetails = re.getNodeDetails();
		var labelProvider = flags.has(RenderDetailFlags.OPT_LABEL_CACHE) ? re.getLabelCache() : LabelInfoProvider.NO_CACHE;
		
		graphGraphics.update(flags, true);
		
		GraphRenderer.renderEdges(pm, graphGraphics, netViewSnapshot, flags, nodeDetails, edgeDetails, labelProvider);
	}
	
}
