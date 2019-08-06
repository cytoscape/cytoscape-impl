package org.cytoscape.ding.impl;

import org.cytoscape.graph.render.stateful.GraphLOD;

/**
 * Level of Details object for Ding.
 * 
 * TODO: design and implement event/listeners for this.
 * 
 */
public class BirdsEyeViewLOD implements GraphLOD {
	private final GraphLOD source;

	public BirdsEyeViewLOD(GraphLOD source) {
		this.source = source;
	}

	public byte renderEdges(final int visibleNodeCount, final int totalNodeCount, final int totalEdgeCount) {
		return source.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
	}

	public boolean detail(final int renderNodeCount, final int renderEdgeCount) {
		boolean sourceDetail = source.detail(renderNodeCount, renderEdgeCount);
		if (sourceDetail && (renderNodeCount + renderEdgeCount) < 10000) {
			return true;
		}
		return false;
	}

	public boolean nodeBorders(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeBorders(renderNodeCount, renderEdgeCount);
	}

	public boolean nodeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeLabels(renderNodeCount, renderEdgeCount);
	}

	public boolean customGraphics(final int renderNodeCount, final int renderEdgeCount) {
		return source.customGraphics(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeArrows(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeArrows(renderNodeCount, renderEdgeCount);
	}

	public boolean dashedEdges(final int renderNodeCount, final int renderEdgeCount) {
		return source.dashedEdges(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeAnchors(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeAnchors(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeLabels(renderNodeCount, renderEdgeCount);
	}

	public boolean textAsShape(final int renderNodeCount, final int renderEdgeCount) {
		return false;
	}

	public double getNestedNetworkImageScaleFactor() {
		return source.getNestedNetworkImageScaleFactor();
	}

}
