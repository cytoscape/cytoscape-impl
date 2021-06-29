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

	@Override
	public GraphLOD faster() {
		return new BirdsEyeViewLOD(source.faster());
	}
	
	@Override
	public RenderEdges renderEdges(final int visibleNodeCount, final int totalNodeCount, final int totalEdgeCount) {
		return source.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
	}

	@Override
	public boolean detail(final int renderNodeCount, final int renderEdgeCount) {
		boolean sourceDetail = source.detail(renderNodeCount, renderEdgeCount);
		if (sourceDetail && (renderNodeCount + renderEdgeCount) < 10000) {
			return true;
		}
		return false;
	}

	@Override
	public boolean nodeBorders(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeBorders(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean nodeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeLabels(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean customGraphics(final int renderNodeCount, final int renderEdgeCount) {
		return source.customGraphics(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeArrows(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeArrows(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean dashedEdges(final int renderNodeCount, final int renderEdgeCount) {
		return source.dashedEdges(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeAnchors(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeAnchors(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeLabels(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean isEdgeBufferPanEnabled() {
		return source.isEdgeBufferPanEnabled();
	}
	
	@Override
	public boolean isLabelCacheEnabled() {
		return source.isLabelCacheEnabled();
	}
	
	@Override
	public boolean isHidpiEnabled() {
		return source.isHidpiEnabled();
	}
	
	@Override
	public boolean textAsShape(final int renderNodeCount, final int renderEdgeCount) {
		return false;
	}

	@Override
	public double getNestedNetworkImageScaleFactor() {
		return source.getNestedNetworkImageScaleFactor();
	}

}
