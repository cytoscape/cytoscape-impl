package org.cytoscape.graph.render.stateful;

import java.awt.geom.Rectangle2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.graph.render.stateful.GraphLOD.RenderEdges;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.spacial.NetworkSpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

/**
 * The GraphLOD combined with the number of visible nodes/edges tells us exactly what level of
 * detail to actually render.
 */
public class RenderDetailFlags {
	
	// Level of detail
	public final static int LOD_HIGH_DETAIL     = 1 << 1;
	public final static int LOD_NODE_BORDERS    = 1 << 2;
	public final static int LOD_NODE_LABELS     = 1 << 3;
	public final static int LOD_EDGE_ARROWS     = 1 << 4;
	public final static int LOD_DASHED_EDGES    = 1 << 5;
	public final static int LOD_EDGE_ANCHORS    = 1 << 6;
	public final static int LOD_EDGE_LABELS     = 1 << 7;
	public final static int LOD_TEXT_AS_SHAPE   = 1 << 8;
	public final static int LOD_CUSTOM_GRAPHICS = 1 << 9;
	// Optimizations
	public final static int OPT_EDGE_BUFF_PAN   = 1 << 10;
	public final static int OPT_LABEL_CACHE     = 1 << 11;
	public final static int OPT_PDF_FONT_HACK   = 1 << 12;

	
	private final int lodBits;
	private final RenderEdges renderEdges;
	private final int nodeCount;
	private final int edgeCountEstimate;
	
	private RenderDetailFlags(int lodBits, RenderEdges renderEdges, int nodeCount, int edgeCountEstimate) {
		this.lodBits = lodBits;
		this.renderEdges = renderEdges;
		this.nodeCount = nodeCount;
		this.edgeCountEstimate = edgeCountEstimate;
	}
	
	public RenderDetailFlags add(int opts) {
		return new RenderDetailFlags(this.lodBits | opts, this.renderEdges, this.nodeCount, this.edgeCountEstimate);
	}
	
	public RenderEdges renderEdges() {
		return renderEdges;
	}
	
	public int getVisibleNodeCount() {
		return nodeCount;
	}
	
	public int getEstimatedEdgeCount() {
		return edgeCountEstimate;
	}
	
	public boolean not(int flag) {
		return (lodBits & flag) == 0;
	}
	
	public boolean has(int flag) {
		return (lodBits & flag) != 0;
	}
	
	public boolean all(int ... flags) {
		for(int flag : flags) {
			if(not(flag)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean any(int ... flags) {
		for(int flag : flags) {
			if(has(flag)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean treatNodeShapesAsRectangle() {
		return not(LOD_HIGH_DETAIL);
	}
	
	
	public static RenderEdges renderEdges(CyNetworkViewSnapshot netView, NetworkTransform transform, GraphLOD lod) {
		Rectangle2D.Float area = transform.getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = netView.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		final int visibleNodeCount = nodeHits.size();
		final int totalNodeCount = netView.getNodeCount();
		final int totalEdgeCount = netView.getEdgeCount();
		
		return lod.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
	}
	
	
	public static RenderDetailFlags create(CyNetworkViewSnapshot netView, NetworkTransform transform, GraphLOD lod, UpdateType updateType) {
		Rectangle2D.Float area = transform.getNetworkVisibleAreaNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = netView.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		final int visibleNodeCount = nodeHits.size();
		return create(netView, visibleNodeCount, lod, updateType);
	}
	
	
	public static RenderDetailFlags create(CyNetworkViewSnapshot netView, int visibleNodeCount, GraphLOD lod) {
		return create(netView, visibleNodeCount, lod, UpdateType.ALL_FULL);
	}
	
	
	private static RenderDetailFlags create(CyNetworkViewSnapshot netView, int visibleNodeCount, GraphLOD lod, UpdateType updateType) {
		final int totalNodeCount = netView.getNodeCount();
		final int totalEdgeCount = netView.getEdgeCount();
		final RenderEdges renderEdges = lod.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
		
		int renderEdgeCount;
		if(renderEdges == RenderEdges.ALL)
			renderEdgeCount = totalEdgeCount;
		else if(renderEdges == RenderEdges.NONE)
			renderEdgeCount = 0;
		else // visible nodes
			renderEdgeCount = estimateEdgeCount(totalNodeCount, visibleNodeCount, totalEdgeCount);
		
		int lodBits = lodToBits(netView, visibleNodeCount, renderEdgeCount, lod, updateType);
		return new RenderDetailFlags(lodBits, renderEdges, visibleNodeCount, renderEdgeCount);
	}
	
	
	private static int estimateEdgeCount(int totalNodeCount, int visibleNodeCount, int totalEdgeCount) {
		if(visibleNodeCount <= 0) {
			return 0;
		} else  {
			// Use a simple heuristic
			int estimate = 2 * (int)(totalEdgeCount * ((double)visibleNodeCount / (double)totalNodeCount));
			return Math.min(totalEdgeCount, estimate);
		}
	}
	
	public static int[] countNodesEdges(DRenderingEngine re) {
		Rectangle2D.Float area = re.getTransform().getNetworkVisibleAreaNodeCoords();
		
		CyNetworkViewSnapshot netView = re.getViewModelSnapshot();
		NetworkSpacialIndex2D spacialIndex = netView.getSpacialIndex2D();
		
		var nodeHits = spacialIndex.queryOverlapNodes(area.x, area.y, area.x + area.width, area.y + area.height, null);
		int nodeCount = nodeHits.size();
		
		var edgeHits = spacialIndex.queryOverlapEdges(area.x, area.y, area.x + area.width, area.y + area.height, null);
		int edgeCount = edgeHits.size();
		
		return new int[] { nodeCount, edgeCount };
	}
	
	
	private static int lodToBits(CyNetworkViewSnapshot netView, int renderNodeCount, int renderEdgeCount, GraphLOD lod, UpdateType updateType) {
		int lodbits = 0;
		
		// detail bits
		final boolean highDetail = lod.detail(renderNodeCount, renderEdgeCount);
		
		if (highDetail) {
			lodbits |= LOD_HIGH_DETAIL;
			if (lod.nodeBorders(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_NODE_BORDERS;
			if (lod.nodeLabels(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_NODE_LABELS;
			if (lod.edgeArrows(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_EDGE_ARROWS;
			if (lod.dashedEdges(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_DASHED_EDGES;
			if (lod.edgeAnchors(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_EDGE_ANCHORS;
			if (lod.edgeLabels(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_EDGE_LABELS;
			if ((((lodbits & LOD_NODE_LABELS) != 0) || ((lodbits & LOD_EDGE_LABELS) != 0)) && lod.textAsShape(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_TEXT_AS_SHAPE;
			if (lod.customGraphics(renderNodeCount, renderEdgeCount))
				lodbits |= LOD_CUSTOM_GRAPHICS;
		}
		
		// optimization bits
		if (lod.isEdgeBufferPanEnabled())
			lodbits |= OPT_EDGE_BUFF_PAN;
		if (lod.isLabelCacheEnabled())
			lodbits |= OPT_LABEL_CACHE;
		
		return lodbits;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lodBits;
		result = prime * result + renderEdges.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RenderDetailFlags) {
			RenderDetailFlags other = (RenderDetailFlags) obj;
			return lodBits == other.lodBits && renderEdges == other.renderEdges;
		}
		return false;
	}

}
